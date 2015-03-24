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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author dvimont
 */
@XmlRootElement(name = "author")
public class Author 
        extends Contributor 
        implements IndexedKey, Comparable<Author> {
    
    /** regular expression to find any leading char(s) that is whitespace, 
     * hyphen, quote, period, apostrophe, or single-quote. */
    private static final String REGEX_TRIM_LEADING_SPECIAL_CHARS 
                                            = "^[\\s\\-\"\\.'‘]+";

    private String uniqueKey;
    /*
    // multi part path index fields for Audiobook indexing
    private String titleForIndex;
    private String publicationDateForIndex;
    private int downloadsForIndex;
    */
    private void setKeyItem () {
        if (this.firstName == null && this.lastName == null) {
            uniqueKey = "NO_NAME";
            return;
        }
        String name;
        if (this.firstName == null 
                || this.firstName.isEmpty()) {
            name = this.lastName.trim().toLowerCase(); 
        } else {
            name = this.lastName.trim().toLowerCase() + ", " 
                        + this.firstName.trim().toLowerCase();
        }
        uniqueKey = name.replaceAll(REGEX_TRIM_LEADING_SPECIAL_CHARS, "");
        
    }
    
    @Override
    @XmlTransient
    public String getKeyItem () {
        /*
        if (this.librivoxId == null || this.librivoxId.isEmpty()
                || this.lastName == null 
                || this.lastName.isEmpty()) {
            return null;
        }
        */
        if (uniqueKey == null) {
            setKeyItem();
        }
        return uniqueKey;
    }
    
    public boolean hasName() {
        return !((this.firstName == null || this.firstName.isEmpty()) 
                && (this.lastName == null || this.lastName.isEmpty()));
    }
    
    @Override
    public int compareTo(Author other) {
        return this.multiKeyAscendingOrder(other);
    }

    @Override
    public boolean equals(Object object)
    {
        boolean areEqual = false;
        if (object != null && object instanceof Author) {
            areEqual = this.getKeyItem() == ((Author) object).getKeyItem();
        }
        return areEqual;
    }

    // hashCode method generated by NetBeans v8.0.1
    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
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
