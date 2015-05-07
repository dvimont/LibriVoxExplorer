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

import org.commonvox.indexedcollection.IndexedKey;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Language unmarshalled from LibriVox API output
 * @author Daniel Vimont
 */
@XmlJavaTypeAdapter(LanguageXmlAdapter.class)
public class Language 
        implements IndexedKey, Comparable<Language>, Mergeable {
    protected String language;
    private String uniqueKey;

    /**
     * Provides wrapping of String in instance of Language
     * @param languageString
     * @return Language instance
     */
    public static Language getInstance (String languageString) {
        return new Language(languageString);
    }
    
    /**
     * Standard constructor
     * @param languageString language String
     */
    public Language (String languageString) {
        this.language = languageString;
    }
    
    /**
     * Standard get method
     * @return language String
     */
    public String getLanguage () {
        return language;
    }
    
    /**
     * Standard set method
     * @param languageString language String
     */
    public void setLanguage (String languageString) {
        this.language = languageString;
    }
    
    @Override
    public String getKeyItem() {
        if (uniqueKey == null) {
            setKeyItem();
        }
        return uniqueKey;
    }
    
    private void setKeyItem() {
        uniqueKey = language;
    }
    
    @Override
    public int compareTo(Language otherLanguage) {
        return this.multiKeyAscendingOrder(otherLanguage);
    }
    
    @Override
    public String toString() {
        return language;
    }
}
