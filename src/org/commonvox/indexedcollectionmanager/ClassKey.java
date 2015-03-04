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

import java.util.Arrays;

/**
 *
 * @author Daniel Vimont
 */
public class ClassKey 
        implements KeyWrapper<Class>, Comparable<ClassKey> {
    
    private final Class<?> wrappedClass;
    
    /**
     *
     * @param wrappedClass
     */
    public ClassKey(Class<?> wrappedClass) {
        this.wrappedClass = wrappedClass;
    }
    
    /**
     *
     * @param keyClassArray
     * @return
     */
    public static ClassKey[] getClassKeyArray (Class... keyClassArray) {
        ClassKey[] classKeyArray = new ClassKey[keyClassArray.length];
        for (int i=0; i < keyClassArray.length; i++) {
            classKeyArray[i] = new ClassKey(keyClassArray[i]);
        }
        return classKeyArray;
    }
    
    /**
     *
     * @return
     */
    @Override
    public String getKeyItem() {
        return wrappedClass.getName(); 
    }
    
    @Override
    public int compareTo (ClassKey otherClassKey) {
        return this.getKeyItem().compareTo(otherClassKey.getKeyItem());
    }
}
