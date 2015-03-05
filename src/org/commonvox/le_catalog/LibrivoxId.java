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

package org.commonvox.le_catalog;

import org.commonvox.indexedcollection.Key;

/**
 *
 * @author Daniel Vimont
 */
public class LibrivoxId 
        implements Key<String>, Comparable<LibrivoxId> {
    
    private final String key;
    
    public LibrivoxId (String key) {
        this.key = key;
    }
    
    @Override
    public String getKeyItem () {
        return this.key;
    }
    
    @Override
    public int compareTo(LibrivoxId otherLibrivoxId) {
        return this.multiKeyAscendingOrder(otherLibrivoxId);
    }
    
    @Override
    public String toString() {
        return this.getKeyItem();
    }
}
