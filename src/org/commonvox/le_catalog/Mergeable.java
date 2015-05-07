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

package org.commonvox.le_catalog;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 *
 * @author Daniel Vimont
 */
public interface Mergeable {
    /** Note that this is a "destructive merge", in that the value of any 
     * non-null field in otherObject will overlay the value of the corresponding
     * field in this object. */
    default void merge (Object otherObject) 
            throws IllegalAccessException {
        if (otherObject.getClass() != this.getClass()) {
            return; // throw InvalidMergeException?
        }
        
        Class nextClass = this.getClass();
        while(nextClass.getSuperclass()!=null){
            Field[] fields = nextClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (field.get(otherObject) != null) {
                    field.set(this, field.get(otherObject));
                }
            }
            nextClass = nextClass.getSuperclass();
        }
    }
}
