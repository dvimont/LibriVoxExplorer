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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.commonvox.indexedcollection.Key;

/**
 *
 * @author dvimont
 */
@XmlType(propOrder = { "id", "title", "displayTitle", "description", 
                        "urlTextSource", "language",
                        "copyrightYear", "authors", "genres", "translators"})
public abstract class Work 
        implements HasLibrivoxId, Key<String>, Mergeable, Serializable, Comparable<Work> {
    protected static final String IA_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    protected static final SimpleDateFormat DATE_VALIDATOR 
            = new SimpleDateFormat(IA_DATE_FORMAT);

    protected String librivoxId;
    protected String title;
    protected String displayTitle;
    protected String description;
    protected String urlTextSource;
    protected Language language;
    protected String copyrightYear;
    protected List<Author> authors;
    protected List<Genre> genres;
    protected List<Translator> translators;
    private static final String VARIOUS_AUTHORS_ID = "18";
    protected static final String REGEX_TRIM_LEADING_ARTICLE 
            = "^(A |An |The |a |an |the |AN |THE )";
    /** regular expression to find any leading char(s) that is whitespace, 
     * hyphen, quote, period, or apostrophe. */
    protected static final String REGEX_TRIM_LEADING_SPECIAL_CHARS = "^[\\s\\-\"\\.']+";
    /** regular expression to find any leading char(s) that is whitespace, 
     * hyphen, quote, period, apostrophe, or digit. */
    protected static final String REGEX_TRIM_LEADING_SPECIAL_CHARS_AND_DIGITS 
            = "^[\\d\\s\\-\"\\.']+";
    private static String[] REGEX_TITLE_CLEANERS 
            = {"\\s*?\\-\\s*?\\(.*?([Vv]ersion).*?\\)"
                ,"\\(.*?([Vv]ersion).*?\\)"
                ,"\\<small\\>\\</small\\>?"
                ,"\\s*?\\-\\s*?[Vv]ersion\\s*?[0-9]"
                ,",\\s*?[Vv]ersion\\s*?[0-9]"
                ,"\\s*?[Vv]ersion\\s*?[0-9]"
                };
    protected static final String BLANKS = "          ";

    @XmlElement(name = "id")
    @Override
    public String getId () {
        return librivoxId;
    }
    
    @Override
    public void setId (String id) {
        this.librivoxId = id;
    }

    /** Data source problem: As of December 2014, the LibriVox API returns
     * incomplete title metadata for audiobooks. (Leading articles have already
     * been stripped out of the title element.) */
    public String getTitle () {
        if (title == null) {
            return null;
        }
        return title.trim();
    }
    
    public void setTitle (String title) {
        this.title = title.trim();
    }

    @XmlElement(name = "dt")
    public String getDisplayTitle() {
        return this.displayTitle;
    }
    
    protected void setDisplayTitle (String displayTitle) {
        this.displayTitle = displayTitle;
    }
    
    @XmlTransient
    public String getTitleForDisplay() {
        if (this.getDisplayTitle() == null || this.getDisplayTitle().isEmpty()) {
            return getTitle();
        }
        return this.getDisplayTitle();
    }
    
    @XmlTransient
    public String getCleanedTitle () {
        if (getTitle() == null) {
            return null;
        }
        String cleanedTitle = getTitle();
        for (String regex : REGEX_TITLE_CLEANERS){
            cleanedTitle = cleanedTitle.replaceAll(regex, "");
        }
        return cleanedTitle.trim();
    }
    
    public String getDescription () {
        return description;
    }
    
    public void setDescription (String description) {
        this.description = description;
    }

    @XmlElement(name = "url_text_source")
    public String getUrlTextSource () {
        return urlTextSource;
    }
    
    public void setUrlTextSource (String urlString) {
        urlTextSource = urlString;
    }

    @XmlJavaTypeAdapter(LanguageXmlAdapter.class)
    public Language getLanguage () {
        return language;
    }
    
    public void setLanguage (Language language) {
        this.language = language;
    }

    @XmlElement(name = "copyright_year")
    public String getCopyrightYear () {
        return copyrightYear;
    }
    
    public void setCopyrightYear (String copyrightYear) {
        this.copyrightYear = copyrightYear;
    }

    @XmlElementWrapper(name = "authors")
    @XmlElement(name = "author")
    public List<Author> getAuthors () {
        //if (authors == null) {
        //   return new ArrayList<Author>();
        //}
        return authors;
    }
    
    public void setAuthors (List<Author> authors) {
        this.authors = authors;
    }
    
    @XmlElementWrapper(name = "genres")
    @XmlElement(name = "genre")
    public List<Genre> getGenres () {
        //if (genres == null) {
        //    return new ArrayList<Genre>();
        //}
        return genres;
    }
    
    public void setGenres (List<Genre> genres) {
        this.genres = genres;
    }

    @XmlElementWrapper(name = "translators")
    @XmlElement(name = "translator")
    public List<Translator> getTranslators () {
        if (translators == null) {
            return new ArrayList<Translator>();
        }
        return translators;
    }
    
    public void setTranslators (List<Translator> translators) {
        this.translators = translators;
    }
    
    @XmlTransient
    public abstract List<Reader> getReaders();
    
    @XmlTransient
    public abstract int getDurationInSeconds();
    
    @XmlTransient
    protected boolean isVariousAuthorsWork () {
        for (Author author : authors){
            if (author.getId() != null 
                    && author.getId().equals(VARIOUS_AUTHORS_ID)) {
                return true;
            }
        }
        return false;
    }
    
    @XmlTransient
    public abstract Title getTitleKey ();
    
    //@XmlTransient
    //public abstract int getDownloadCountInternetArchive();
    
    //@XmlTransient
    //public Downloads getDownloadsKey () {
    //    return new Downloads(this.getDownloadCountInternetArchive());
    //}
    
    //@XmlTransient
    //public abstract double getDownloadsPerDay ();
    
    //@XmlTransient
    //public DownloadsPerDay getDownloadsPerDayKey () {
    //    return new DownloadsPerDay
    //                (java.math.BigDecimal.valueOf(this.getDownloadsPerDay()));
    //}
    
    //@XmlTransient
    //public abstract String getPublicationDateInternetArchive ();
    
    //@XmlTransient
    //protected abstract int getDaysAvailable ();
    /*
    @XmlTransient
    public PublicationDate getPublicationDateKey () {
        if (this.getPublicationDateInternetArchive() == null) {
            return null;
        } 
        
        return new PublicationDate(String.format("%09d", this.getDaysAvailable()));
    }
    */
    @Override
    public String toString() {
        return this.getId();
    }
    
    public abstract String toStringVerbose();

    @Override
    public int compareTo(Work otherWork) {
        return this.multiKeyAscendingOrder(otherWork);
    }
}
