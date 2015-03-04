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

/**
 *
 * @author Daniel Vimont
 * @param <K> the type of key (i.e., the class of object returned by 
 * the getKeyItem() method)
 */
public interface Key<K extends Comparable<K>> {
    public K getKeyItem();
    
    default public int multiKeyAscendingOrder (Key<K> other) {
        return this.getKeyItem().compareTo(other.getKeyItem());
    }
    
    default public int multiKeyDescendingOrder (Key<K> other) {
        return other.getKeyItem().compareTo(this.getKeyItem());
    }
    
}
