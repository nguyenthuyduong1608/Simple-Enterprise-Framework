package orm.config;

import generator.Generatable;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PersistenceConfig implements Generatable {
    private List<String> _entityClasses;
    private String _user;
    private String _passWord;
    private String _jdbcDriver;
    private String _url;

    public PersistenceConfig() {

    }

    public PersistenceConfig(List<String> entityClasses, String user, String passWord, String jdbcDriver, String url) {
        this._entityClasses = entityClasses;
        this._user = user;
        this._passWord = passWord;
        this._jdbcDriver = jdbcDriver;
        this._url = url;
    }

    @Override
    public void generate(File directory) {
        Path path = Paths.get("src\\main\\resources\\template\\PersistenceTemplate.txt");
        StringBuilder builder = new StringBuilder("");

        // Read persistence template file
        try {
            Files.lines(path).forEach((line) -> {
                builder.append(line + "\n");
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Convert entity classes name to right format
        String entities = _entityClasses.stream().map(classEntity -> "<class>entity." + classEntity + "</class>\n")
                                       .reduce("", (a, b) -> a + b);

        String finalPersistenceContent = builder.toString();

        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%entity%", entities);
        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%jdbcDriver%", _jdbcDriver);
        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%user%", _user);
        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%passWord%", _passWord);
        finalPersistenceContent = StringUtils.replace(finalPersistenceContent, "%url%", _url);

        try {
            FileWriter myWriter = new FileWriter(directory + "\\persistence.xml");
            myWriter.write(finalPersistenceContent);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getEntityClasses() {
        return _entityClasses;
    }

    public void setEntityClasses(List<String> entityClasses) {
        this._entityClasses = entityClasses;
    }

    public String getUser() {
        return _user;
    }

    public void setUser(String user) {
        this._user = user;
    }

    public String getPassWord() {
        return _passWord;
    }

    public void setPassWord(String passWord) {
        this._passWord = passWord;
    }

    public String getJdbcDriver() {
        return _jdbcDriver;
    }

    public void setJdbcDriver(String jdbcDriver) {
        this._jdbcDriver = jdbcDriver;
    }
}
