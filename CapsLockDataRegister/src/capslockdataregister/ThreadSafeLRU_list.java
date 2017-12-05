/*  
    This file is part of CapsLockDataRegister. CapsLockDataRegister is a JSON generator for CapsLock.
    Copyright (C) 2017 RISCassembler

    CapsLockDataRegister is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/

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
