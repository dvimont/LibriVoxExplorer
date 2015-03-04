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

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author dvimont
 */
@XmlRootElement(name = "section")
@XmlType(propOrder = { "sectionNumber", "urlForListening", "durationInSeconds",
                        "readers"})
public class Section 
        extends Work 
        implements Mergeable, Serializable
//, Comparable<Section> 
{
    protected int sectionNumber;
    protected String urlForListening;
    protected int durationInSeconds;
    protected List<Reader> readers;
    private String parentAudiobookId;
        
    @XmlElement(name = "section_number")
    public int getSectionNumber () {
        return sectionNumber;
    }
    
    public void setSectionNumber (int sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    @XmlElement(name = "listen_url")
    public String getUrlForListening () {
        // librivox servers should NOT be accessed for listening!!
        if (urlForListening == null
                || urlForListening.toLowerCase().contains(Catalog.LIBRIVOX_DOMAIN)) {
            return null;
        }
        return urlForListening;
    }
    
    public void setUrlForListening (String urlString) {
        urlForListening = urlString;
    }

    @Override
    @XmlElement(name = "playtime")
    public int getDurationInSeconds () {
        return durationInSeconds;
    }
    
    public void setDurationInSeconds (int seconds) {
        durationInSeconds = seconds;
    }

    @Override
    @XmlElementWrapper(name = "readers")
    @XmlElement(name = "reader")
    public List<Reader> getReaders () {
        return readers;
    }
    
    public void setReaders (List<Reader> readers) {
        this.readers = readers;
    }

    /*
    @Override
    @XmlTransient
    public List<Genre> getGenres () {
        if (this.getParentAudiobook() == null) {
            return null;
        }
        return this.getParentAudiobook().getGenres();
    }
    */
    @Override
    @XmlTransient
    public String getKeyItem () {
        return String.format
            ("%08d%08d", Integer.parseInt(parentAudiobookId),
                        Integer.parseInt(librivoxId));
    }
    
    @XmlTransient
    private String getParentAudiobookId () {
        return parentAudiobookId;
    }
    
    protected void setParentAudiobookId (Audiobook audiobook) {
        this.parentAudiobookId = audiobook.getId();
    }
    
    @Override
    @XmlTransient
    public Title getTitleKey () {
        if (getCleanedTitle() == null || getCleanedTitle().isEmpty()) {
            return null;
        } else {
            return new Title(getCleanedTitle()
                .replaceAll(REGEX_TRIM_LEADING_SPECIAL_CHARS_AND_DIGITS, "")
                .replaceAll(REGEX_TRIM_LEADING_ARTICLE, "")
                .replaceAll(REGEX_TRIM_LEADING_SPECIAL_CHARS, "")
                    .toLowerCase());
        }
    }
    
    @Override
    public String toStringVerbose() {
        return "";
    }

    /*
    @Override
    @XmlTransient
    public int getDownloadCountInternetArchive () {
        return this.getParentAudiobook().getDownloadCountInternetArchive();
    }
    
    @Override
    @XmlTransient
    public double getDownloadsPerDay () {
        return this.getParentAudiobook().getDownloadsPerDay();
    }
    
    @Override
    @XmlTransient
    public int getDaysAvailable () {
        return this.getParentAudiobook().getDaysAvailable();
    }
    
    @Override
    @XmlTransient
    public String getPublicationDateInternetArchive () {
        return this.getParentAudiobook().getPublicationDateInternetArchive();
    }
    */
    /*
    public List<Author> getAuthors () {
        if (this.getParentAudiobook().isVariousAuthorsWork()) {
            return authors;
        } else {
            return this.getParentAudiobook().getAuthors();
        }
    }
    */
}
