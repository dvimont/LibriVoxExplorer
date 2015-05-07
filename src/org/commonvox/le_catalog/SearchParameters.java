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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Daniel Vimont
 */
public class SearchParameters {
    public enum RESPONSE_STATUS {OK, SEARCH_IO_ERROR, LIBRIVOX_IO_ERROR};

    public String searchString;
    public int numberRequested;
    public int startMarker;
    public boolean endOfSearchEngineResultSet = false;
    public LinkedList<String> librivoxUrlStrings = new LinkedList<>();
    public List<Work> returnedWorks = new ArrayList<>();
    public RESPONSE_STATUS responseStatus = RESPONSE_STATUS.OK;
    public String errorMessage = "";
    
    public SearchParameters(String searchString, int numberRequested,
            int startMarker) {
        this.searchString = searchString;
        this.numberRequested = numberRequested;
        this.startMarker = startMarker;
    }
    
    public SearchParameters(String searchString, int numberRequested) {
        this(searchString, numberRequested, 1);
    }
    
    public SearchParameters() {
        this("", 0, 1);
    }
}
