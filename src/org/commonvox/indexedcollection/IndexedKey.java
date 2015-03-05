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

package org.commonvox.indexedcollection;

/**
 *
 * @author Daniel Vimont
 A class which implements the IndexedKey interface is a special class of Key, 
 which not only serves as a Key in IndexNodes, but also is the mapped
 Value in one or more IndexNodes of its own (hence the name, "IndexedKey").
 For example, the Author class could implement the IndexedKey interface and
 serve as one of the Keys in one or more IndexNodes for the Book class 
 (indexing Books in Author order), and also have its own IndexNode(s) 
 (indexing Authors in name order or other orders).
 */
public interface IndexedKey extends Key<String> {
}
