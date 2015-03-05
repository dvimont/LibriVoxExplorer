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
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javafx.scene.image.Image;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.commonvox.le_catalog.InterruptibleDownloader.ReturnCode;
import org.commonvox.indexedcollection.IndexedKey;

/**
 *
 * @author dvimont
 */
//@Entity
@XmlRootElement(name = "book")
@XmlType(propOrder = { "urlRss", "urlZipFile", "urlProject", "urlLibrivox",
                        "urlOther", "durationInSeconds", "urlInternetArchive",
                        "downloadCountInternetArchive", 
                        "publicationDateInternetArchive", "urlCoverArt",
                        "urlThumbnail", "urlM4bFiles", "sections" })
public class Audiobook 
        extends Work {
    private static final String IMAGES_PATH = "images/coverArt/";
    private String uniqueKey;
    protected String urlRss;
    protected String urlZipFile;
    protected String urlProject;
    protected String urlLibrivox;
    protected String urlOther;
    protected int durationInSeconds;
    protected String urlInternetArchive;
    protected List<Section> sections;
    protected String urlCoverArt;
    protected String urlThumbnail;
    protected List<String> urlM4bFiles;
    protected int downloadCountInternetArchive;
    protected String publicationDateInternetArchive;
    protected Set<Reader> readers;
    protected List<Language> languages;
    //private Image coverArtImage = null;
    //private Image localCoverArtImage = null;
    private boolean coverArtImageUnavailable = false;
    private boolean localCoverArtImageUnavailable = false;
    public static final int NOT_YET_AVAILABLE_INDICATOR = 5000;
    public static final String NO_GENRE_INDICATOR = "_No Genre Assigned";


    public Audiobook () {
    }    
    
    public Audiobook (String lvCatalogUrlString) {
        this();
        urlLibrivox = lvCatalogUrlString;
    }
    
    public Audiobook (String lvCatalogUrlString, List<String> m4bUrlStrings) {
        this(lvCatalogUrlString);
        this.setUrlM4bFiles(m4bUrlStrings);
    }
    
    @XmlElement(name = "url_rss")
    public String getUrlRss () {
        return urlRss;
    }
    
    public void setUrlRss (String urlString) {
        urlRss = urlString;
    }

    @XmlElement(name = "url_zip_file")
    public String getUrlZipFile () {
        return urlZipFile;
    }
    
    public void setUrlZipFile (String urlString) {
        urlZipFile = urlString;
    }

    @XmlElement(name = "url_project")
    public String getUrlProject () {
        return urlProject;
    }
    
    public void setUrlProject (String urlString) {
        urlProject = urlString;
    }

    @XmlElement(name = "url_librivox")
    public String getUrlLibrivox () {
        return urlLibrivox;
    }
    
    public void setUrlLibrivox (String urlString) {
        urlLibrivox = urlString;
    }
    
    @XmlTransient
    public String getUrlLibrivoxUriEncoded () {
        try {
            return URLEncoder.encode(urlLibrivox, Catalog.UTF8_CHARSET);
        } catch (UnsupportedEncodingException e) {
            return uriEncode(urlLibrivox);
        }
    }

    @XmlElement(name = "url_other")
    public String getUrlOther () {
        return urlOther;
    }
    
    public void setUrlOther (String urlString) {
        urlOther = urlString;
    }

    @Override
    @XmlElement(name = "totaltimesecs")
    public int getDurationInSeconds () {
        if (durationInSeconds > 0) {
            return durationInSeconds;
        } else {
            if (this.sections == null) {
                return 0;
            }
            int totalSeconds = 0;
            for (Section section : this.sections) {
                totalSeconds += section.getDurationInSeconds();
            }
            return totalSeconds;
        }
    }
    
    public void setDurationInSeconds (int seconds) {
        durationInSeconds = seconds;
    }

    @XmlElement(name = "url_iarchive")
    public String getUrlInternetArchive () {
        return trimUrl(urlInternetArchive);
    }
    
    public void setUrlInternetArchive (String urlString) {
        urlInternetArchive = trimUrl(urlString);
    }

    @XmlElementWrapper(name = "url_m4b_files")
    @XmlElement(name = "url_m4b_file")
    public List<String> getUrlM4bFiles () {
        return urlM4bFiles;
    }
    
    public void setUrlM4bFiles (List<String> urlStrings) {
        urlM4bFiles = urlStrings;
    }
    
    @XmlElement(name = "url_cover_art")
    public String getUrlCoverArt () {
        return urlCoverArt;
    }
    
    public void setUrlCoverArt (String urlString) {
        urlCoverArt = urlString;
    }
    
    @XmlElement(name = "url_thumbnail")
    public String getUrlThumbnail () {
        return urlThumbnail;
    }
    
    public void setUrlThumbnail (String urlString) {
        urlThumbnail = urlString;
    }
    
    @XmlElementWrapper(name = "sections")
    @XmlElement(name = "section")
    public List<Section> getSections () {
        return sections;
    }
    
    public void setSections (List<Section> sections) {
        this.sections = sections;
    }

    //@Override
    @XmlElement(name = "download_count_iarchive")
    public int getDownloadCountInternetArchive () {
        return downloadCountInternetArchive;
    }
    
    public void setDownloadCountInternetArchive (int downloadCount) {
        this.downloadCountInternetArchive = downloadCount;
    }
    
    @XmlTransient
    public Downloads getDownloadsKey () {
        return new Downloads(this.getDownloadCountInternetArchive());
    }
    
    //@Override
    @XmlElement(name = "pub_date_iarchive")
    public String getPublicationDateInternetArchive () {
        return publicationDateInternetArchive;
    }
    
    public void setPublicationDateInternetArchive (String dateString) 
            throws ParseException {
        Date date = DATE_VALIDATOR.parse(dateString);
        publicationDateInternetArchive = dateString;
    }
    
    @XmlTransient
    public PublicationDate getPublicationDateKey () {
        /*
        if (this.getPublicationDateInternetArchive() == null) {
            return null;
        } 
        */
        return new PublicationDate(String.format("%09d", this.getDaysAvailable()));
    }

    @Override
    @XmlTransient
    public String getKeyItem () {
        if (uniqueKey == null) {
            setKeyItem();
        }
        return uniqueKey;
    }
    
    protected void setKeyItem() {
        uniqueKey = String.format("%08d00000000", Integer.parseInt(librivoxId));
    }
    
    @Override
    @XmlTransient
    public Title getTitleKey () {
        if (getCleanedTitle() == null || getCleanedTitle().isEmpty()) {
            return null;
        } else {
            return new Title(getCleanedTitle()
                .replaceAll(REGEX_TRIM_LEADING_SPECIAL_CHARS, "")
                .replaceAll(REGEX_TRIM_LEADING_ARTICLE, "")
                .replaceAll(REGEX_TRIM_LEADING_SPECIAL_CHARS, "")
                    .toLowerCase());
        }
    }

    @Override
    @XmlTransient
    public List<Reader> getReaders () {
        if (readers == null) {
            return null;
        }
        return new ArrayList<Reader>(readers);
    }
    
    protected void setReaders() {
        readers = new TreeSet<Reader>();
        if (this.sections == null) {
            return;
        }
        for (Section section : this.sections) {
            if (section.getReaders() != null) {
                for (Reader reader : section.getReaders()) {
                    readers.add(reader);
                }
            }
        }
    }
    
    @XmlTransient
    public List<Language> getAllLanguages() {
        if (languages == null) {
            setLanguages();
        }
        return languages;
    }
    
    private void setLanguages () {
        if (this.getLanguage() == null || this.sections == null) {
            languages = null;
            return;
        }
        Set<Language> languageSet = new TreeSet<>();
        languageSet.add(this.getLanguage());
        /* dirty data problem: many audiobooks in non-English languages have
        * correct language in the audiobook record, but incorrectly have "English"
        * designated in each section of the audiobook! The following for loop
        * checks for this and sets the "includeSectionLanguages" boolean. */
        boolean includeSectionLanguages = false;
        final Language ENGLISH = new Language("English");
        if (this.getLanguage().compareTo(ENGLISH) == 0) {
            includeSectionLanguages = true;
        } else {
            for (Section section : this.sections) {
                if (section.getLanguage() == null) {
                    continue;
                }
                if (section.getLanguage().compareTo(ENGLISH) != 0) {
                    includeSectionLanguages = true;
                    break;
                }
            }
        }
        if (includeSectionLanguages) {
            for (Section section : this.sections) {
                if (section.getLanguage() == null) {
                    continue;
                }
                languageSet.add(section.getLanguage());
            }
        }
        languages = new ArrayList<Language>(languageSet);
    }
    
    protected void addAuthorsFromSections() {
        if (this.sections == null) {
            return;
        }
        Set<Author> authorSet = new TreeSet<>(this.authors);
        for (Section section : this.sections) {
            if (section.getAuthors() == null) {
                continue;
            }
            for (Author sectionAuthor : section.getAuthors()) {
                authorSet.add(sectionAuthor);
            }
        }
        this.authors.clear();
        this.authors.addAll(authorSet);
    }
    
    private static String trimUrl (String urlString) {
        if (urlString == null) {
            return null;
        }
        if (urlString.endsWith("/")) {
            return urlString.substring(0, urlString.length() - 1);
        } else {
            return urlString;
        }
    }

    //@Override
    @XmlTransient
    protected int getDaysAvailable () {
        long daysAvailable = 0;
        if (publicationDateInternetArchive != null
                && !publicationDateInternetArchive.isEmpty()) {
            try {
                Date publicationDate 
                        = DATE_VALIDATOR.parse(publicationDateInternetArchive);
                Date today
                        = Calendar.getInstance().getTime();
                daysAvailable
                        = (today.getTime() - publicationDate.getTime()) / 86400000;
            }
            catch (ParseException | NumberFormatException
                            | ArrayIndexOutOfBoundsException e) {
            }
        }
        if (daysAvailable == 0) {
            daysAvailable = NOT_YET_AVAILABLE_INDICATOR;
        }
        return (int) daysAvailable;
    }
    
    @XmlTransient
    public double getDownloadsPerDay () {
        double downloadsPerDay = 0;
        double daysAvailable = this.getDaysAvailable();
        if (daysAvailable != 0) {
            downloadsPerDay = (double) this.getDownloadCountInternetArchive()
                                    / daysAvailable;
        }

        return downloadsPerDay;
    }
    
    @XmlTransient
    public DownloadsPerDay getDownloadsPerDayKey () {
        return new DownloadsPerDay
                    (java.math.BigDecimal.valueOf(this.getDownloadsPerDay()));
    }
    
    @XmlTransient
    public String getUrlLocalCoverArt () {
        if (this.getUrlCoverArt() == null) {
            return null;
        }
        String fileName = new File(this.getUrlCoverArt()).getName();
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        URL resourceUrl = Audiobook.class.getResource(IMAGES_PATH + fileName);
        if (resourceUrl == null) {
            return null;
        }
        return resourceUrl.toString();
    }
    
    /**
     *
     * @return cover art image for the audiobook, which may still
     * be background loading when returned. Thus, when necessary, appropriate 
     * logic (listeners, etc.) should be employed to determine when image 
     * loading is complete (i.e., when image.getProgress() == 1).
     */
    @XmlTransient
    public Image getCoverArtImage () {
        /*
        if (coverArtImage != null) {
            if (coverArtImage.isError()) { // image loaded in previous call
                return null;
            } else {
                return coverArtImage;
            }
        }
        */
        if (coverArtImageUnavailable) {
            return null;
        }
        String urlString = this.getUrlCoverArt();
        Image coverArtImage;
        if (urlString == null || urlString.isEmpty()) {
            coverArtImageUnavailable = true;
            coverArtImage = null;
        } else {
            coverArtImage = new Image(urlString, true);
            if (coverArtImage == null) {
                coverArtImageUnavailable = true;
            }
        }
        return coverArtImage;
    }
    
    /**
     *
     * @return locally stored (thumbnail) cover art image for the audiobook, 
     * which may still be background loading when returned. Thus, when 
     * necessary, appropriate logic (listeners, etc.) should be employed to 
     * determine when image loading is complete (i.e., when 
     * image.getProgress() == 1).
     */
    @XmlTransient
    public Image getLocalCoverArtImage () {
        /*
        if (localCoverArtImage != null) {
            if (localCoverArtImage.isError()) { // image loaded in previous call
                return null;
            } else {
                return localCoverArtImage;
            }
        }
        */
        if (localCoverArtImageUnavailable) {
            return null;
        }
        String urlString = this.getUrlLocalCoverArt();
        Image localCoverArtImage;
        if (urlString == null || urlString.isEmpty()) {
            localCoverArtImageUnavailable = true;
            localCoverArtImage = null;
        } else {
            localCoverArtImage = new Image(urlString, true);
            if (localCoverArtImage == null) {
                localCoverArtImageUnavailable = true;
            }
        }
        return localCoverArtImage;
    }
    
    @XmlTransient
    public long getM4bSize () {
        long m4bSize = 0;
        for (String m4bUrlString : this.urlM4bFiles) {
            long fileSize = InterruptibleDownloader.getFileSize(m4bUrlString);
            if (fileSize <= 0) {
                return 0;
            }
            m4bSize += fileSize;
        }
        return m4bSize;
    }
    
    public void downloadM4bFiles (File targetPath, CatalogCallback callback) 
            throws MalformedURLException, IOException, InterruptedException {
        int fileCount = 0;
        ReturnCode downloadResult = null;
        for (String m4bUrlString : this.urlM4bFiles) {
            callback.updateSubtasks(fileCount++, this.urlM4bFiles.size());
            callback.updateTaskMessage
                        ("M4B audiobook file " + fileCount + " of " + 
                            this.urlM4bFiles.size() + " downloading...");
            downloadResult = InterruptibleDownloader.copy
                                    (m4bUrlString, targetPath, callback);
            if (!downloadResult.equals(ReturnCode.OK)) {
                break;
            }
        }
        if (downloadResult.equals(ReturnCode.OK)) {
            callback.updateTaskMessage
                        ("M4B audiobook file " + fileCount + " of " + 
                            this.urlM4bFiles.size() + " download completed.");
        } else {
            for (String m4bUrlString : this.urlM4bFiles) {
                InterruptibleDownloader.delete(m4bUrlString, targetPath, callback);
            }
        }
        if (downloadResult == ReturnCode.INTERRUPTED) {
            throw new InterruptedException();
        }
        callback.updateSubtasks(fileCount, this.urlM4bFiles.size());
    }
    
    public static String uriEncode (String rawUrl) {
        return rawUrl.replace(":", "%3A").replace("/","%2F");
    }
    
    @Override
    public String toStringVerbose() {
        StringBuilder output = new StringBuilder();
        output.append("  TITLE: ").append(this.getCleanedTitle()).append("\n");
        output.append("    Date of publication: ")
                .append(this.getPublicationDateInternetArchive())
                .append("  (LibriVox ID = " + this.getLibrivoxIdKey())
                .append(")\n");
        output.append(outputListHorizontally(Author.class));
        output.append(outputListHorizontally(Reader.class));
        output.append(outputListHorizontally(Genre.class));
        output.append(outputListHorizontally(Language.class));
        output.append("    Downloads / Downloads per day: ")
                .append(String.format("%,d",this.getDownloadCountInternetArchive()))
                .append(" / ")
                .append(String.format("%,5.4f",this.getDownloadsPerDay()))
                .append("\n");
        return output.toString();
    }
    private String outputListHorizontally 
            (Class<? extends IndexedKey> mappedKeyClass) {
        StringBuilder printedList = new StringBuilder();
        boolean labelPrinted = false;
        List<? extends IndexedKey> list = null;
        if (mappedKeyClass == Reader.class) {
            list = this.getReaders();
        } else if (mappedKeyClass == Genre.class) {
            list = this.getGenres();
        } else if (mappedKeyClass == Author.class) {
            list = this.getAuthors();
        } else if (mappedKeyClass == Language.class) {
            List<Language> languages = new ArrayList<>();
            languages.add(this.getLanguage());
            list = languages;
        }
        if (list == null || list.isEmpty()) {
            return printedList.toString();
        }
        for (IndexedKey mappedKeyItem : list) {
            if (!labelPrinted) {
                labelPrinted = true;
                printedList.append(super.BLANKS.substring
                    (mappedKeyClass.getSimpleName().length()));
                printedList.append
                    (mappedKeyClass.getSimpleName().toUpperCase());
                printedList.append("(s): ");
                printedList.append(mappedKeyItem.getKeyItem());
            } else {
                printedList.append("; ").append(mappedKeyItem.getKeyItem());
            }
        }
        if (labelPrinted) {
            printedList.append("\n");
        }
        return printedList.toString();
    }
    
    
}
