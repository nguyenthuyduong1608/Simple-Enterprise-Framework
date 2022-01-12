package ui.viewmodel;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;

public class ViewModelTemplate extends RecursiveTreeObject<ViewModelTemplate> {
    private SimpleStringProperty _field1 = new SimpleStringProperty();
    private SimpleStringProperty _field2 = new SimpleStringProperty();
    private SimpleStringProperty _field3 = new SimpleStringProperty();

    public ViewModelTemplate(String field1, String field2, String field3) {
        this._field1.set(field1);
        this._field2.set(field2);
        this._field3.set(field3);
    }

    public String getField1() {
        return _field1.get();
    }

    public SimpleStringProperty field1Property() {
        return _field1;
    }

    public void setField1(String field1) {
        this._field1.set(field1);
    }

    public String getField2() {
        return _field2.get();
    }

    public SimpleStringProperty field2Property() {
        return _field2;
    }

    public void setField2(String field2) {
        this._field2.set(field2);
    }

    public String getField3() {
        return _field3.get();
    }

    public SimpleStringProperty field3Property() {
        return _field3;
    }

    public void setField3(String field3) {
        this._field3.set(field3);
    }
}
