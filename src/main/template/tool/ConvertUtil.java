package ui.tool;

public class ConvertUtil {
    final ConvertedMap _convertedMap;
    private IConverted _convertor;

    public ConvertUtil(){
        _convertedMap = new ConvertedMap();
        _convertor = new StringConverted();
    }

    public void setConvertor(String type){
        for (int i = 0; i < _convertedMap.getList().size(); i++) {
            if(_convertedMap.getList().get(type) != null){
                _convertor = _convertedMap.getList().get(type);
            }
        }
    }

    public Object ConvertToObject(String type, String value){
        setConvertor(type);
       return _convertor.convertEntity(value);
    }
}
