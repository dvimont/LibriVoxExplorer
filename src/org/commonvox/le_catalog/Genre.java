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
import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Daniel Vimont
 */
@XmlRootElement(name = "genre")
@XmlType(propOrder = { "id", "name"})
public class Genre 
        implements HasLibrivoxId, IndexedKey, 
                        Comparable<Genre>, Mergeable, Serializable {
    /** regular expression to find any leading char(s) that is whitespace, 
     * hyphen, quote, period, apostrophe, or asterisk. */
    static final String REGEX_TRIM_LEADING_SPECIAL_CHARS = "^[\\s\\-\"\\.'\\*]+";
    protected String librivoxId;
    protected String name;
    private String uniqueKey;
    /*
    // multi part path index fields for Audiobook indexing
    private String titleForIndex;
    private String publicationDateForIndex;
    private int downloadsForIndex;
    */

    @XmlElement(name = "id")
    @Override
    public String getId () {
        return librivoxId;
    }
    
    @Override
    public void setId (String id) {
        this.librivoxId = id;
    }

    public String getName () {
        return name.replaceAll(REGEX_TRIM_LEADING_SPECIAL_CHARS, "");
    }
    
    public void setName (String inputString) {
        name = inputString;
    }
    
    @Override
    public int compareTo(Genre otherGenre) {
        return this.multiKeyAscendingOrder(otherGenre);
    }
    
    @Override
    @XmlTransient
    public String getKeyItem() {
        if (uniqueKey == null) {
            setKeyItem();
        }
        return uniqueKey;
    }
    
    protected void setKeyItem() {
        uniqueKey = this.getName();
    }
    
    @Override
    public String toString () {
        return this.getName();
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
