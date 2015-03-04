/*
 * Copyright (C) 2014 Daniel Vimont
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

package org.commonvox.indexedcollectionmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Daniel Vimont
 */
public class KeyArray 
        implements Iterable<Key>{
    private Key[] array;
    
    @SafeVarargs
    public KeyArray (Key<?>... keyArray) {
        this.array = keyArray;
    }

    public int getLength () {
        return array.length;
    }
    
    protected void putTopKey(Key newTopKey) {
        Key[] newArray = new Key[array.length + 1];
        newArray[0] = newTopKey;
        for (int i = 1 ; i < newArray.length; i++) {
            newArray[i] = array[i - 1];
        }
        array = newArray;
    }
    
    public Key getTopKey() {
        if (array.length == 0) {
            return null;
        } else {
            return array[0];
        }
    }
    
    public boolean containsNulls () {
        for (Key key : array) {
            if (key == null || key.getKeyItem() == null) {
                return true;
            }
        }
        return false;
    }
    
    public KeyArray getLowerKeyArray() {
        if (array.length <= 1) {
            return new KeyArray(new Key<?>[0]); // empty array
        } else {
            return new KeyArray(Arrays.copyOfRange(array, 1, array.length));
        }
    }
    
    @Override
    public Iterator<Key> iterator() {
        List<Key> list = new ArrayList<Key>(Arrays.asList(array));
        return list.iterator();
    }
}
