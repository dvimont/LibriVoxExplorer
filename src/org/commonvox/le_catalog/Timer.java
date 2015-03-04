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

/**
 *
 * @author Daniel Vimont
 */
public class Timer {
    private long timerMilliseconds = 0;
    private long elapsedMilliseconds = 0;

    public void reset() {
        elapsedMilliseconds = 0;
    }

    public Timer start() {
        timerMilliseconds = System.currentTimeMillis();
        return this;
    }

    public long stop() {
        return elapsedMilliseconds += (System.currentTimeMillis() - timerMilliseconds);
    }

    public long get() {
        long returnMilliseconds = elapsedMilliseconds;
        this.reset();
        return returnMilliseconds;
    }
}
