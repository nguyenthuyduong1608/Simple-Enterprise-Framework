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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ViewModelGenerator implements Generatable {
    String table;
    List<String> listField;
    private Map<String, Boolean> isAutoGenerate;
    public ViewModelGenerator(String table, List<String> field, Map<String, Boolean> isAutoGenerate) {
        this.table = table;
        this.listField = field;
        this.isAutoGenerate = isAutoGenerate;
    }

    @Override
    public void generate(File directory) {
        Path path = Paths.get("src/main/template/ui/ViewModelTemplate.txt");
        StringBuilder builder = new StringBuilder("");

        // Read persistence template file
        try {
            Files.lines(path).forEach((line) -> {
                builder.append(line + "\n");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


        String strField = listField
                .stream()
                .map(field -> "private SimpleStringProperty "+field+" = new SimpleStringProperty();\n\t")
                .reduce("", (a, b) -> a + b);

        String support = listField
                .stream()
                .map(field ->{
                    String res = "public String get"+ ToolUtils.convertProp(field)+"() {\n" +
                            "        return "+field+".get();\n" +
                            "    }\n"+
                            "    public SimpleStringProperty "+ToolUtils.convertProp(field)+"Property() {\n" +
                            "        return "+field+";\n" +
                            "    }\n";
                    if (!isAutoGenerate.get(field)) {
                        res = res +
                                "    public void set"+ToolUtils.convertProp(field)+"(String field1) {\n" +
                                "        this."+field+".set(field1);\n" +
                                "    }\n";
                    }
                    return res + "\n\t";
                })
                .reduce("", (a, b) -> a + b);

        String strConstructorBegin = "public "+table+"ViewModel";
        String strConstructorParams = listField
                .stream()
                .map(field ->{
                    if (!isAutoGenerate.get(field)) {
                        return "String " + field +", ";
                    }
                    return "";
                })
                .reduce("", (a, b) -> a + b);
        strConstructorParams = strConstructorParams.substring(0, strConstructorParams.length() - 2);
        String strConstructorSet = listField
                .stream()
                .map(field ->{
                    if (!isAutoGenerate.get(field)) {
                        return "       this."+field+".set("+field+");\n";
                    }
                    return "";
                })
                .reduce("", (a, b) -> a + b);

        AtomicReference<Boolean> isNoFieldAutoGen = new AtomicReference<>(true);
        listField.forEach(field -> {
            if (isAutoGenerate.get(field)) {
                isNoFieldAutoGen.set(false);
                return;
            }
        });
        String strConstructorShort =  strConstructorBegin + "(" + strConstructorParams + "){\n"
                + strConstructorSet
                + "\t}";

        String strConstructorLong = "";
        if (!isNoFieldAutoGen.get()) {
            strConstructorParams = listField
                    .stream()
                    .map(field ->"String " + field +", ")
                    .reduce("", (a, b) -> a + b);
            strConstructorParams = strConstructorParams.substring(0, strConstructorParams.length() - 2);
            strConstructorSet = listField
                    .stream()
                    .map(field -> "       this."+field+".set("+field+");\n")
                    .reduce("", (a, b) -> a + b);

            strConstructorLong =  strConstructorBegin + "(" + strConstructorParams + "){\n"
                    + strConstructorSet
                    + "\t}";
        }

        String strConstructor = strConstructorShort + "\n\n    " + strConstructorLong;

        System.out.println(strConstructor);

        String finalPersistenceContent = builder.toString();

        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%table%", table);
        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%support%", support);
        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%constructor%", strConstructor);
        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%field%", strField);

        try {
            FileWriter myWriter = new FileWriter(directory);
            myWriter.write(finalPersistenceContent);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
