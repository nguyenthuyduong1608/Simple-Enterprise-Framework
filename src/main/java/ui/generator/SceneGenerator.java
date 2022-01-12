package ui.generator;

import generator.Generatable;
import org.apache.commons.lang3.StringUtils;
import ui.ToolUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SceneGenerator implements Generatable {
    String _table;
    List<String> _listTable;
    Map<String, String> _columnMap;
    List<String> _field;
    List<String> _fieldClass;
    private Map<String, Boolean> _isAutoGenerate;

    public SceneGenerator(String table, List<String> listTable, Map<String, String> column, Map<String, Boolean> isAutoGenerate) {
        this._table = table;
        this._listTable = listTable;
        this._columnMap = column;
        this._field = new ArrayList<String>();
        this._field.addAll(_columnMap.keySet());
        this._fieldClass =  new ArrayList<String>();
        this._fieldClass.addAll(_columnMap.values());
        this._isAutoGenerate = isAutoGenerate;
    }

    @Override
    public void generate(File directory) {
        Path path = Paths.get("src/main/template/ui/SceneTemplate.txt");
        StringBuilder builder = new StringBuilder("");

        // Read persistence template file
        try {
            Files.lines(path).forEach((line) -> {
                builder.append(line + "\n");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        final int[] iCount = {-1};
        String switchFunction = _listTable
                .stream()
                .map(table ->{
                            iCount[0]++;
                            return "@FXML\n" +
                                    "private void switch_"+table+"_Scene(MouseEvent event) {\n" +
                                    "Main.setCurrentTab("+ iCount[0] +");\n" +
                                "   SceneUtils.getInstance().switchScreen(this.rootPane, \"/fxml/"+table.toLowerCase()+"Scene.fxml\", 100);\n" +
                                "   }\n\n\t";
                        }

                )
                .reduce("", (a, b) -> a + b);

        String column = _field
                .stream()
                .map(field ->
                        " TreeTableColumn<"+ _table +"ViewModel, String> col_"+field+" = new JFXTreeTableColumn<>(\""+ ToolUtils.convertProp(field)+"\");\n" +
                                "        col_"+field+".setSortable(false);" +
                                "        col_"+field+".setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue().get"+ToolUtils.convertProp(field)+"()));\n\n\t\t"
                )
                .reduce("", (a, b) -> a + b);

        String addColumn = _field
                .stream()
                .map(field->
                        "col_" + field + ", "
                        )
                .reduce("", (a, b) -> a + b);
        if (_field.size() > 0){
            addColumn = addColumn.substring(0, addColumn.length() - 2);
        }

        String getField = _field
                .stream()
                .map(field->"                model.get" + ToolUtils.convertProp(field) + "().toString(),\n")
                .reduce("", (a, b) -> a + b);
        getField = getField.substring(0, getField.length()-2);

        String annotationFXML = _field
                .stream()
                .map(field->{
                    if (!_isAutoGenerate.get(field)) {
                        return "@FXML\n" +
                                "    JFXTextField edt_"+ field +";";
                    }
                    return "";
                })
                .reduce("", (a, b) -> a + b);

        String getFieldOfData = _field
                .stream()
                .map(field-> {
                    if (!_isAutoGenerate.get(field)) {
                        return "\t\t\tthis.edt_"+field+".setText(data.get(index).get"+ToolUtils.convertProp(field)+"());";
                    }
                    return "";
                })
                .reduce("", (a, b) -> a + b);

        String getFieldToCreateVM = _field
                .stream()
                .map(field->{
                    if (!_isAutoGenerate.get(field)) {
                        return "\n\t\t\tthis.edt_"+field+".getText(),";
                    }
                    return "";
                })
                .reduce("", (a, b) -> a + b);
        getFieldToCreateVM = getFieldToCreateVM.substring(0,getFieldToCreateVM.length() - 1);

        String getFieldToConvertEntity = _field
                .stream()
                .map(field-> {
                    if (!this._isAutoGenerate.get(field)) {
                        return "\t\tres.set"+ToolUtils.convertProp(field)+"(("+ _columnMap.get(field)+") new ConvertUtil().ConvertToObject(\""+ _columnMap.get(field)+"\", model.get"+ToolUtils.convertProp(field)+"()));\n";
                    }
                    return "";
                })
                .reduce("", (a, b) -> a + b);


        String finalPersistenceContent = builder.toString();

        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%getFieldToConvertEntity%", getFieldToConvertEntity);
        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%getFieldToCreateVM%", getFieldToCreateVM);
        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%getFieldOfData%", getFieldOfData);
        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%annotationFXML%", annotationFXML);
        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%table%", _table);
        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%switchFunction%", switchFunction);
        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%column%", column);
        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%addColumn%", addColumn);
        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%getField%", getField);



        try {

            FileWriter myWriter = new FileWriter(directory);
            myWriter.write(finalPersistenceContent);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {

    }
}
