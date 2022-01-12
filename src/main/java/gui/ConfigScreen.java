package gui;

import com.jfoenix.controls.*;
import gradleGenerate.GradleGen;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import orm.column.Column;
import orm.SqlDatabase;
import orm.SqlServer;
import orm.table.Table;
import orm.config.PersistenceConfig;
import ui.generator.*;
import ui.scene.SceneUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigScreen implements Initializable {
    @FXML
    private StackPane _root;

    @FXML
    private JFXButton _generateButton;

    @FXML
    private JFXComboBox<String> _databaseComboBox;

    @FXML
    private JFXTextField _destinationInput;

    @FXML
    private JFXButton _browseButton;
    @FXML
    private JFXButton _backButton;

    private SqlServer _sqlServer;

    @FXML
    void chooseFolder(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            _destinationInput.setText(selectedDirectory.getPath());
        }
    }

    @FXML
    void generate(ActionEvent event) {
        this._generateButton.setDisable(true);
        this._generateButton.setGraphicTextGap(16);
        JFXSpinner spinner = new JFXSpinner();
        spinner.setMaxWidth(20);
        this._generateButton.setGraphic(spinner);

        new Thread(() -> {
            try {
                String databaseName = _databaseComboBox.getSelectionModel().getSelectedItem();
                SqlDatabase sqlDatabase = _sqlServer.connectToDatabase(databaseName);
                String pathDest = _destinationInput.getText() + '\\' + databaseName;
                File fileDest = new File(pathDest);
                sqlDatabase.generate(fileDest);

                List<String> entityClasses = new ArrayList<>();

                sqlDatabase.get_tableList().stream().map(Table::get_className).forEach(entityClasses::add);

                PersistenceConfig persistenceConfig =
                        new PersistenceConfig(entityClasses, _sqlServer.get_user(), _sqlServer.get_password(), SqlServer._className,
                                _sqlServer.get_baseUrl() + "/" + databaseName);
                File metaInfFolder = new File(pathDest + "\\src\\main\\resources\\META-INF");
                metaInfFolder.mkdirs();
                persistenceConfig.generate(metaInfFolder);

                GradleGen gradleGen = new GradleGen(databaseName);
                gradleGen.generate(fileDest);
                sqlDatabase._tableList.remove(sqlDatabase._tableList.size() - 1);

                // Generate UI
                new File(fileDest.getAbsolutePath() + "\\src\\main\\java\\ui").mkdir();
                new File(fileDest.getAbsolutePath() + "\\src\\main\\java\\ui\\scene").mkdir();
                new File(fileDest.getAbsolutePath() + "\\src\\main\\java\\ui\\viewmodel").mkdir();
                new File(fileDest.getAbsolutePath() + "\\src\\main\\java\\ui\\tool").mkdir();
                new ResGenerator().generate(fileDest);
                new ToolGenerator().generate(fileDest);
                new MemberGenerator(sqlDatabase._tableList.get(0).get_className().toLowerCase()).generate(fileDest);

                File base = new File("src\\main\\template\\ui\\BaseSceneTemplate.java");
                File targetBase = new File((fileDest.getAbsolutePath() + "\\src\\main\\java\\ui\\scene\\BaseSceneTemplate.java"));
                targetBase.createNewFile();
                FileUtils.copyFile(base, targetBase);

                List<String> listTableName =
                        sqlDatabase._tableList.stream().map(Table::get_className).collect(Collectors.toList());

                new UIGenerator(sqlDatabase._tableList.get(0).get_className())
                        .generate(new File(fileDest.getAbsoluteFile() + "\\src\\main\\java\\ui\\Main.java"));

                System.out.println(databaseName);
                for (Table table : sqlDatabase.get_tableList()) {
                    Map<String, Boolean> columnAutoGen = new HashMap<>();
                    table.get_columnList().forEach(col -> {
                        if (col.get_isAutoIncrement()){
                            columnAutoGen.put(col._fieldName, true);
                        }
                        else {
                            columnAutoGen.put(col._fieldName, false);
                        }
                    });
                    new FXMLGenerator(databaseName, table.get_className(), listTableName,
                            table.get_columnList().stream().map(Column::get_fieldName).collect(Collectors.toList()), columnAutoGen)
                            .generate(new File(fileDest.getAbsolutePath() + "\\src\\main\\resources\\fxml\\" +
                                    table.get_className().toLowerCase() + "Scene.fxml"));

                    Map<String, Boolean> columnAutoGens = new LinkedHashMap<>();
                    Map<String, String> columnTypes = new LinkedHashMap<>();
                    table.get_columnList().forEach((value) -> {
                        columnTypes.put(value._fieldName, value._className);
                        columnAutoGens.put(value._fieldName, value.get_isAutoIncrement());
                    });
                    new SceneGenerator(
                            table.get_className(),
                            listTableName,
                            columnTypes,
                            columnAutoGens)
                            .generate(new File(
                                    fileDest.getAbsoluteFile() + "\\src\\main\\java\\ui\\scene\\" + table.get_className() +
                                            "Scene.java"));

                    new ViewModelGenerator(table.get_className(), table.get_columnList().stream().map(Column::get_fieldName)
                            .collect(Collectors.toList()), columnAutoGens).generate(new File(
                            fileDest.getAbsolutePath() + "\\src\\main\\java\\ui\\viewmodel\\" + table.get_className() +
                                    "ViewModel.java"));
                }
                Platform.runLater(() -> SceneUtils.getInstance().showDialog(this._root, "Success", "Your project is generated successfully!"));
            } catch (Exception e) {
                Platform.runLater(() -> SceneUtils.getInstance().showDialog(this._root, "Failed", e.getMessage()));
                e.printStackTrace();
            } finally {
                this._generateButton.setDisable(false);
                spinner.managedProperty().bind(spinner.visibleProperty());
                spinner.setVisible(false);
            }
        }).start();
    }

    ObservableList<String> databaseList;

    public void setDatabaseList(List<String> databaseList) {
        _databaseComboBox.setItems(FXCollections.observableArrayList(databaseList));
    }

    public void setSqlServer(SqlServer sqlServer) {
        this._sqlServer = sqlServer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        _databaseComboBox.setItems(databaseList);
        _browseButton.styleProperty().bind(Bindings.when(_browseButton.hoverProperty())
                .then("-fx-background-radius: 10px; -fx-background-color: #0F054C; -fx-text-fill: #ffffff;")
                .otherwise("-fx-background-radius: 10px; -fx-background-color: #2B237C; -fx-text-fill: #ffffff;"));

        _generateButton.styleProperty().bind(Bindings.when(_generateButton.hoverProperty())
                .then("-fx-background-radius: 10px; -fx-background-color: #0F054C; -fx-text-fill: #ffffff;")
                .otherwise("-fx-background-radius: 10px; -fx-background-color: #2B237C; -fx-text-fill: #ffffff;"));
    }

    public void backToLogin(ActionEvent event) {
        new Thread(() -> {
            Platform.runLater(() -> {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginScreen.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Parent root = null;
                try {
                    root = loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                LoginScreen login = loader.getController();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/css/MainTemplate.css").toExternalForm());
                stage.setTitle("CRUD Generation");
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
            });
        }).start();
    }
}
