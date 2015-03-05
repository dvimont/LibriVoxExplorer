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
 * Wrapper interface provided for any class for which the Key interface is
 * unusable (e.g., for any class which does not and cannot be made to implement
 * the Comparable interface).
 * @author DanUltra
 * @param <K> the type of key (i.e., the class of object to be wrapped for usage
 * as a Key in an {@link org.commonvox.indexedcollection.IndexNode IndexNode}).
 * {@link org.commonvox.indexedcollection.ClassKey ClassKey} provides an example of
 * implementation of this interface.
 */
public interface KeyWrapper<K>
        extends Key<String> {
    
}
