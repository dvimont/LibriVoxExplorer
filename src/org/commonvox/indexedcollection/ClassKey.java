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
 * This class used in construction of {@link IndexedMetaCollection}
 * @author Daniel Vimont
 */
class ClassKey 
        implements KeyWrapper<Class>, Comparable<ClassKey> {
    
    private final Class<?> wrappedClass;
    
    /**
     *
     * @param wrappedClass the Class object that is to be wrapped by this object
     */
    public ClassKey(Class<?> wrappedClass) {
        this.wrappedClass = wrappedClass;
    }
    
    /**
     * unused utility method; uncomment if this comes to be potentially useful
     */
    /*
    public static ClassKey[] getClassKeyArray (Class... keyClassArray) {
        ClassKey[] classKeyArray = new ClassKey[keyClassArray.length];
        for (int i=0; i < keyClassArray.length; i++) {
            classKeyArray[i] = new ClassKey(keyClassArray[i]);
        }
        return classKeyArray;
    }
    */
    
    /**
     * Get name of wrapped Class
     * @return name of wrapped Class
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
