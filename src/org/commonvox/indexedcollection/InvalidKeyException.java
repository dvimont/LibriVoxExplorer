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
public class InvalidKeyException extends Exception {

    /**
     * Creates a new instance of <code>InvalidMultiKeyException</code> without
     * detail message.
     */
    public InvalidKeyException() {
    }

    /**
     * Constructs an instance of <code>InvalidMultiKeyException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidKeyException(String msg) {
        super(msg);
    }

    public InvalidKeyException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
