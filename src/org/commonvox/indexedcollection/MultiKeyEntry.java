/*
 * Copyright (C) 2015 Daniel Vimont
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.commonvox.indexedcollection;

/**
 *
 * @author Daniel Vimont
 */
class MultiKeyEntry <V> {
    private KeyArray keyArray;
    private V value;

    public MultiKeyEntry(V value, KeyArray keyArray) {
        this.keyArray = keyArray;
        this.value = value;
    }
    
    @SafeVarargs
    public MultiKeyEntry(V value, Key<?>... keyArray) {
        this(value, new KeyArray(keyArray));
    }
    
    public int getKeyArrayLength() {
        return keyArray.getLength();
    }
    
    protected void putTopKey (Key newTopKey) {
        keyArray.putTopKey(newTopKey);
    }
    
    public Key getTopKey () {
        return keyArray.getTopKey();
    }
    
    public KeyArray getKeyArray() {
        return this.keyArray;
    }

    public V getValue () {
        return value;
    }
    
    public boolean containsNulls () {
        if (keyArray.containsNulls() || value == null) {
            return true;
        }
        return false;
    }
    
    public MultiKeyEntry <V> getLowerMultiKeyEntry() {
        return new MultiKeyEntry <V>(value, keyArray.getLowerKeyArray());
    }
    /*
    protected Class<? extends Key>[] getMultiKeyClassArray() {
        return keyArray.getMultiKeyClassArray();
    }
    */
}
