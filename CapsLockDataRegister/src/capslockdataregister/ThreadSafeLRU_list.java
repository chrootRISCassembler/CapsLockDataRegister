package capslockdataregister;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Least Recently Used list
 */
class ThreadSafeLRU_list<K, V> extends LinkedHashMap<K, V>{
    private static final int MAX_SIZE = 10;

    ThreadSafeLRU_list() {
        super(MAX_SIZE, 0.75F, true);
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        if(size() > MAX_SIZE){
            onRemoveEntry(eldest);
            return true;
        }
        return false;
    }
    
    
    protected void onRemoveEntry(Map.Entry<K, V> eldest){}
}
