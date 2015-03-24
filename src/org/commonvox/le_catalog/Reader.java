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

import org.commonvox.indexedcollection.IndexedKey;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author dvimont
 */
@XmlRootElement(name = "reader")
@XmlType(propOrder = { "id", "displayName"})
public class Reader 
        implements HasLibrivoxId, IndexedKey, Comparable<Reader>, 
                                            Mergeable, Serializable {
    /** regular expression to find any leading char(s) that is whitespace, 
     * hyphen, quote, period, apostrophe, or vertical-bar. */
    private static final String REGEX_TRIM_LEADING_SPECIAL_CHARS 
                                        = "^[\\s\\-\"\\.'|]+";
    protected String librivoxId;
    protected String displayName;
    protected String uniqueKey;
    /*
    // multi part path index fields for Audiobook indexing
    protected String titleForIndex;
    private String publicationDateForIndex;
    private int downloadsForIndex;
    */
    /** DUMMY_READER used as a workaround for an ObjectDB indexing bug. */
    //protected static final Reader DUMMY_READER = new Reader("$*DUMMY*$");

    
    public Reader() {}
    
    protected Reader (String displayName) {
        this.displayName = displayName;
    }
    
    @XmlElement(name = "reader_id")
    @Override
    public String getId () {
        return librivoxId;
    }
    
    @Override
    public void setId (String id) {
        this.librivoxId = id;
    }

    @XmlElement(name = "display_name")
    public String getDisplayName () {
        return displayName
                .replaceAll(REGEX_TRIM_LEADING_SPECIAL_CHARS, "");
    }
    
    public void setDisplayName (String displayName) {
        this.displayName = displayName;
    }
    
    @XmlTransient
    public String getKeyItem () {
        if (uniqueKey == null) {
            setKeyItem();
        }
        return uniqueKey;
    }
    
    private void setKeyItem() {
        uniqueKey = getDisplayName().toLowerCase() 
                + getId(); // ID suffix added to assure uniqueness v1.4.3
    }
    
    @Override
    public int compareTo(Reader otherReader) {
        return this.multiKeyAscendingOrder(otherReader);
    }
    
    @Override
    public String toString () {
        return displayName;
        //return this.getDisplayName();
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
