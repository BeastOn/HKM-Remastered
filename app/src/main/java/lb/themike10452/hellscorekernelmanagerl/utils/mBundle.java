package lb.themike10452.hellscorekernelmanagerl.utils;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Mike on 2/28/2015.
 */
public class mBundle implements Serializable {
    private Map<String, Object> objectMap;

    public mBundle() {
        objectMap = new TreeMap<>();
    }

    public void put(String key, Object object) {
        objectMap.put(key, object);
    }

    public Object get(String key) {
        return objectMap.get(key);
    }
}
