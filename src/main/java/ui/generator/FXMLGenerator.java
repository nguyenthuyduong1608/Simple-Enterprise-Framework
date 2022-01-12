package ui.generator;

import generator.Generatable;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class FXMLGenerator implements Generatable {
    private String _database;
    private String _table;
    private List<String> _listTable;
    private List<String> _listField;
    private Map<String, Boolean> _isAutoGen;

    public FXMLGenerator(String database, String table, List<String> listTable, List<String> listField, Map<String, Boolean> isAutoGen) {
        this._database = database;
        this._table = table;
        this._listTable = listTable;
        this._listField = listField;
        this._isAutoGen = isAutoGen;
    }

    @Override
    public void generate(File directory) {
        Path path = Paths.get("src/main/template/Main.txt");
        StringBuilder builder = new StringBuilder("");

        // Read persistence template file
        try {
            Files.lines(path).forEach((line) -> {
                builder.append(line + "\n");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

//        listEntity.stream().map().reduce("")
        // Convert entity classes name to right format
        String strField = _listField
                .stream()
                .map(field ->{
                    if (!_isAutoGen.get(field)) {
                        return "<JFXTextField fx:id=\"edt_" + field + "\" focusColor=\"#006a8b\" labelFloat=\"true\" promptText=\"" + field + "\">" + "\n"
                                +"<VBox.margin>" +"\n"
                                +    "<Insets />" +"\n"
                                +"</VBox.margin>" + "\n"
                                +"</JFXTextField>";
                    }
                    return "";
                })
                .reduce("", (a, b) -> a + b);

        String strListTable = _listTable
                .stream()
                .map(table ->
                               " <Label text=\""+table+"\" onMouseClicked=\"#switch_"+table+"_Scene\" prefHeight=\"32.0\" prefWidth=\"176.0\" />\n"
                )
                .reduce("", (a, b) -> a + b);

        String finalPersistenceContent = builder.toString();

        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%field%", strField);
        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%table%", _table);
        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%listTable%", strListTable);
        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%database%", _database);

        try {
            FileWriter myWriter = new FileWriter(directory);
            myWriter.write(finalPersistenceContent);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
