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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
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
    private StackPane root;

    @FXML
    private AnchorPane pneCenter;

    @FXML
    private JFXButton generateButton;

    @FXML
    private JFXComboBox<String> databaseComboBox;

    @FXML
    private JFXTextField destinationInput;

    @FXML
    private JFXButton browseButton;

    @FXML
    private JFXButton backButton;

    private SqlServer sqlServer;

    @FXML
    void chooseFolder(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            destinationInput.setText(selectedDirectory.getPath());
        }
    }

    @FXML
    void generate(ActionEvent event) {
        this.generateButton.setDisable(true);
        this.generateButton.setGraphicTextGap(16);
        JFXSpinner spinner = new JFXSpinner();
        spinner.setMaxWidth(20);
        this.generateButton.setGraphic(spinner);

        new Thread(() -> {
            try {
                String databaseName = databaseComboBox.getSelectionModel().getSelectedItem();
                SqlDatabase sqlDatabase = sqlServer.connectToDatabase(databaseName);
                String pathDest = destinationInput.getText() + '\\' + databaseName;
                File fileDest = new File(pathDest);
                sqlDatabase.generate(fileDest);

                List<String> entityClasses = new ArrayList<>();

                sqlDatabase.getTableList().stream().map(Table::getClassName).forEach(entityClasses::add);

                PersistenceConfig persistenceConfig =
                        new PersistenceConfig(entityClasses, sqlServer.getUser(), sqlServer.getPassword(), SqlServer.className,
                                sqlServer.getBaseUrl() + "/" + databaseName);
                File metaInfFolder = new File(pathDest + "\\src\\main\\resources\\META-INF");
                metaInfFolder.mkdirs();
                persistenceConfig.generate(metaInfFolder);

                GradleGen gradleGen = new GradleGen(databaseName);
                gradleGen.generate(fileDest);
                sqlDatabase.tableList.remove(sqlDatabase.tableList.size() - 1);

                // Generate UI
                new File(fileDest.getAbsolutePath() + "\\src\\main\\java\\ui").mkdir();
                new File(fileDest.getAbsolutePath() + "\\src\\main\\java\\ui\\scene").mkdir();
                new File(fileDest.getAbsolutePath() + "\\src\\main\\java\\ui\\viewmodel").mkdir();
                new File(fileDest.getAbsolutePath() + "\\src\\main\\java\\ui\\tool").mkdir();
                new ResGenerator().generate(fileDest);
                new ToolGenerator().generate(fileDest);
                new MemberGenerator(sqlDatabase.tableList.get(0).getClassName().toLowerCase()).generate(fileDest);

                File base = new File("src\\main\\template\\ui\\BaseSceneTemplate.java");
                File targetBase = new File((fileDest.getAbsolutePath() + "\\src\\main\\java\\ui\\scene\\BaseSceneTemplate.java"));
                targetBase.createNewFile();
                FileUtils.copyFile(base, targetBase);

                List<String> listTableName =
                        sqlDatabase.tableList.stream().map(Table::getClassName).collect(Collectors.toList());

                new UIGenerator(sqlDatabase.tableList.get(0).getClassName())
                        .generate(new File(fileDest.getAbsoluteFile() + "\\src\\main\\java\\ui\\Main.java"));

                System.out.println(databaseName);
                for (Table table : sqlDatabase.getTableList()) {
                    Map<String, Boolean> columnAutoGen = new HashMap<>();
                    table.getColumnList().forEach(col -> {
                        if (col.isAutoIncrement()){
                            columnAutoGen.put(col.fieldName, true);
                        }
                        else {
                            columnAutoGen.put(col.fieldName, false);
                        }
                    });
                    new FXMLGenerator(databaseName, table.getClassName(), listTableName,
                            table.getColumnList().stream().map(Column::getFieldName).collect(Collectors.toList()), columnAutoGen)
                            .generate(new File(fileDest.getAbsolutePath() + "\\src\\main\\resources\\fxml\\" +
                                    table.getClassName().toLowerCase() + "Scene.fxml"));

                    Map<String, Boolean> columnAutoGens = new LinkedHashMap<>();
                    Map<String, String> columnTypes = new LinkedHashMap<>();
                    table.getColumnList().forEach((value) -> {
                        columnTypes.put(value.fieldName, value.className);
                        columnAutoGens.put(value.fieldName, value.isAutoIncrement());
                    });
                    new SceneGenerator(
                            table.getClassName(),
                            listTableName,
                            columnTypes,
                            columnAutoGens)
                            .generate(new File(
                                    fileDest.getAbsoluteFile() + "\\src\\main\\java\\ui\\scene\\" + table.getClassName() +
                                            "Scene.java"));

                    new ViewModelGenerator(table.getClassName(), table.getColumnList().stream().map(Column::getFieldName)
                            .collect(Collectors.toList()), columnAutoGens).generate(new File(
                            fileDest.getAbsolutePath() + "\\src\\main\\java\\ui\\viewmodel\\" + table.getClassName() +
                                    "ViewModel.java"));
                }
                Platform.runLater(() -> SceneUtils.getInstance().showDialog(this.root, "Success", "Your project is generated successfully!"));
            } catch (Exception e) {
                Platform.runLater(() -> SceneUtils.getInstance().showDialog(this.root, "Failed", e.getMessage()));
                e.printStackTrace();
            } finally {
                this.generateButton.setDisable(false);
                spinner.managedProperty().bind(spinner.visibleProperty());
                spinner.setVisible(false);
            }
        }).start();
    }

    ObservableList<String> databaseList;

    public void setDatabaseList(List<String> databaseList) {
        databaseComboBox.setItems(FXCollections.observableArrayList(databaseList));
    }

    public void setSqlServer(SqlServer sqlServer) {
        this.sqlServer = sqlServer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        databaseComboBox.setItems(databaseList);
        browseButton.styleProperty().bind(Bindings.when(browseButton.hoverProperty())
                .then("-fx-background-color: #0F054C; -fx-text-fill: #ffffff;")
                .otherwise("-fx-background-color: #2B237C; -fx-text-fill: #ffffff;"));

        generateButton.styleProperty().bind(Bindings.when(generateButton.hoverProperty())
                .then("-fx-background-color: #0F054C; -fx-text-fill: #ffffff;")
                .otherwise("-fx-background-color: #2B237C; -fx-text-fill: #ffffff;"));
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
