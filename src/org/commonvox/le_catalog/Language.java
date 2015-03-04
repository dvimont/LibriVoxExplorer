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

import org.commonvox.indexedcollectionmanager.IndexedKey;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author Daniel Vimont
 */
@XmlJavaTypeAdapter(LanguageXmlAdapter.class)
public class Language 
        implements IndexedKey, Comparable<Language>, Mergeable {
    protected String language;
    private String uniqueKey;
    /*
    // multi part path index fields for Audiobook indexing
    private String titleForIndex;
    private String publicationDateForIndex;
    private int downloadsForIndex;
    */
    public static Language getInstance (String languageString) {
        return new Language(languageString);
    }
    
    public Language (String languageString) {
        this.language = languageString;
        //this.id = languageString;
    }
    
    public String getLanguage () {
        return language;
    }
    
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
    
    protected void setKeyItem() {
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
    /*
    protected void setAudiobookIndexFields(Audiobook audiobook) {
        if (uniqueKey == null) {
            setKeyItem();
        }
        titleForIndex = audiobook.getTitleKey().getKeyItem();
        publicationDateForIndex = audiobook.getPublicationDateKey().getKeyItem();
        downloadsForIndex = audiobook.getDownloadsKey().getKeyItem();
    }
    */
}
