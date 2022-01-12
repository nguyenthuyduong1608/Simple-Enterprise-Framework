package ui.tool;

import java.util.*;

public class ConvertedMap{
    private HashMap<String, IConverted> _list;

    ConvertedMap(){
        _list = new HashMap<>();
        _list.put("java.lang.Integer", new IntegerConverted());
        _list.put("java.lang.String", new StringConverted());
        _list.put("java.lang.Boolean", new BooleanConverted());
        _list.put("java.lang.Float", new FloatConverted());
    }

    public HashMap<String, IConverted> getList() {
        return _list;
    }
}
