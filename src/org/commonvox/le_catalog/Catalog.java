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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.commonvox.indexedcollection.CompositeIndex;
import org.commonvox.indexedcollection.IndexedCollection;
/**
 *
 * @author Daniel Vimont
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement(name="xml") //, namespace="org.commonvox.librivox.audiobooks")
public class Catalog 
        implements Mergeable, Cloneable, Serializable {
    private static String latestVersion = "";
    public final static boolean DIAGNOSTIC_MODE = true;
    @XmlElementWrapper(name = "books")
    @XmlElement(name = "book")
    protected List<Audiobook> audiobooks = new ArrayList<>();
    @XmlTransient
    protected List<Audiobook> m4bAudiobooks = new ArrayList<>();
    protected static final Map<String,String> SUBGENRE_MAP = new TreeMap<>(); // v1.5.1
    private final Map<String,Audiobook> m4bAudiobooksUrlMap = new TreeMap<>();
    private Audiobook[] m4bAudiobooksArray;
    private static final Random RANDOM_NUMBER_GENERATOR = new Random();
    public enum ReaderWorksOption // v1.3.3
        {ALL_WORKS(""), 
        SOLO_WORKS(" <SOLO WORKS>"), 
        GROUP_WORKS(" <GROUP WORKS>");
        private final String title;
        ReaderWorksOption(String title) {
            this.title = title;
        }
        public String getTitle() {
            return this.title;
        }
    };

    private final TreeSet<Author> uniqueAuthorList = new TreeSet<>();
    private final TreeSet<Reader> uniqueReaderList = new TreeSet<>();
    private final TreeSet<Genre> uniqueGenreList = new TreeSet<>();
    private final TreeSet<Language> uniqueLanguageList = new TreeSet<>();
    private final int CALLBACK_DIVISOR = 20;
    private static final String GOOGLE_API_URL_STRING 
            = "https://www.googleapis.com/customsearch/v1?";
    private static final String GOOGLE_SEARCH_ENGINE_ID
                = "&cx=004351109951561212012:uioc4qsspce";
    private static final String GOOGLE_API_KEY_DELIMITER = "key=";    
    private static String googleApiKey;
    private static final String PROB_REPORT_URL_STRING 
            = "https://commonvox.wufoo.com/forms/zbp0sc31oiwiel/";
    private static final String START_TEMPLATE = "&start=%d";
    private static final int NUMBER_PER_CALL = 10;
    private static final String NUM_PARM = "&num=" + NUMBER_PER_CALL;
    private static final String FIELDS_PARM 
                = "&fields=searchInformation(totalResults),items(link)";
    private static final String QUERY_DELIMITER = "&q=";
    public static final String UTF8_CHARSET = "UTF-8";
    public static final String LIBRIVOX_DOMAIN = "librivox.org";
    /*
    NOTE: As of 2014-12-15, the following query filter parameters were included
    in a Google Custom Search Engine, and so are now external to this application
    for purposes of API invocation. (These parms are still used for invocation of
    search via Web browser.) */
    public static final String GOOGLE_QUERY_FILTER 
                    = " site:librivox.org -site:*.librivox.org "
                    + "-site:librivox.org/rss -site:librivox.org/api "
                    + "-site:librivox.org/author -site:librivox.org/category "
                    + "-site:librivox.org/search -site:librivox.org/feed "
                    + "-site:librivox.org/reader "
                    + "-site:librivox.org/2005 -site:librivox.org/2006 "
                    + "-site:librivox.org/2007 -site:librivox.org/2008 "
                    + "-site:librivox.org/2009 -site:librivox.org/2010 "
                    + "-site:librivox.org/2011 -site:librivox.org/2012 "
                    + "-site:librivox.org/2013 -site:librivox.org/2014 "
                    + "-site:librivox.org/2015 -site:librivox.org/2016 "
                    + "-site:librivox.org/2017 -site:librivox.org/2018 "
                    + "-site:librivox.org/2019 -site:librivox.org/2020 "
                    + "-site:librivox.org/2021 -site:librivox.org/2022 "
                    + "-site:librivox.org/public_readers_iframe " 
                    + "-site:librivox.org/add_project -site:librivox.org/*.xml";
    protected static final String DEFAULT_DB_PATH = "database/catalog.odb";

    @XmlTransient
    protected Timer timer = new Timer();
    private static IndexedCollection<Work> DIRECTORY;
 
    protected void addAudiobook (String lvCatalogUrlString, List<String> m4bUrlStrings) {
        audiobooks.add(new Audiobook(lvCatalogUrlString, m4bUrlStrings));
    }
    
    public void append (Catalog catalogToAppend) {
        if (catalogToAppend == null) {
            return;
        }
        audiobooks.addAll(catalogToAppend.audiobooks);
    }
    
    public void merge (Catalog updatesCatalog) {
        if (updatesCatalog == null) {
            return;
        }
        Set<Audiobook> audiobookSet = new TreeSet<>(audiobooks);
        for (Audiobook updatedAudiobook : updatesCatalog.audiobooks) {
            if (audiobookSet.contains(updatedAudiobook)) {
                audiobookSet.remove(updatedAudiobook);
            } 
            audiobookSet.add(updatedAudiobook);
        }
        audiobooks = new ArrayList<>(audiobookSet);
    }
    
    protected void sortAudiobooks() {
        audiobooks = new ArrayList<>(new TreeSet<Audiobook>(audiobooks));
    }

    /** Cleanly clones a catalog using JAXB marshal and unmarshal facilities
     * @return 
     * @throws javax.xml.bind.JAXBException */
    protected Catalog cloneWithJAXB () throws JAXBException  {
        Marshaller marshaller 
            = JAXBContext.newInstance(Catalog.class).createMarshaller();
        //marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(this, stringWriter);
        
        return (Catalog) 
                JAXBContext.newInstance(Catalog.class).createUnmarshaller()
                        .unmarshal(new StringReader(stringWriter.toString()));
    }
    
    /**
     *
     * @param callback
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InterruptedException
     * @throws JAXBException
     * @throws java.io.IOException
     */
    public void bootUp(CatalogCallback callback) 
            throws IllegalAccessException,
                    InvocationTargetException,
                    InterruptedException,
                    JAXBException,
                    IOException {
        if (Thread.interrupted()) { throw new InterruptedException(); }
        // instantiate dummy callback, if necessary
        if (callback == null) {
            callback = new CatalogCallback() 
            { @Override public void updateTaskProgress(long l1, long l2){}
              @Override public void updateTaskMessage(String s){
                    System.out.println("*\n" + s);}
            }; 
        }
        timer.reset();
        timer.start();
        printMemoryUsage("At start of bootup process", DIAGNOSTIC_MODE);
        Catalog.printHeadingWithTimestamp
            ("CATALOG MAPPING (INDEXING) CYCLE INITIATED.");
        callback.updateTaskMessage("Building list of M4B audiobooks.");
        buildM4bAudiobookList();
        callback.passbackObject(m4bAudiobooks);
        callback.updateTaskMessage("Clearing incomplete data from catalog.");
        removeNoNameAuthorsAndReaders(callback);
        callback.updateTaskMessage("Building internal data links.");
        setSectionParentFields();
        buildSubgenreMap(); // v1.5.1
        callback.updateTaskMessage("Propagating author/reader/genre data.");
        setAudiobookAuthorReaderGenre(callback);
        Catalog.printHeadingWithTimestamp
            ("Creating master directory of IndexedCollections.");
        callback.updateTaskMessage("Building catalog's indexes.");
        printMemoryUsage("After data propagation", DIAGNOSTIC_MODE);
        buildDirectory();
        printMemoryUsage("After directory built", DIAGNOSTIC_MODE);
        Catalog.printHeadingWithTimestamp
            ("Mapping (indexing) of Works (audiobooks & sections) and " 
                    + "IndexedKeys (authors, readers, etc.) initiated.");
        callback.updateTaskMessage("Populating catalog indexes.");
        autofillDirectory(callback);
        printMemoryUsage("After directory populated", DIAGNOSTIC_MODE);
        callback.updateTaskMessage("Configuring Google search facility.");
        this.fetchGoogleApiKey();
        callback.updateTaskMessage("Fetching latest version number.");
        this.fetchLatestVersionNumber();
        callback.updateTaskMessage
                ("Building of catalog is complete. IT'S SHOWTIME, FOLKS!");
        timer.stop();
        long elapsedMilliseconds = timer.get();
        Catalog.printHeadingWithTimestamp
            (String.format("CATALOG MAPPING (INDEXING) CYCLE COMPLETED (in %d:%02d).",
                TimeUnit.MILLISECONDS.toMinutes(elapsedMilliseconds),
                TimeUnit.MILLISECONDS.toSeconds(elapsedMilliseconds) - 
                TimeUnit.MINUTES.toSeconds
                    (TimeUnit.MILLISECONDS.toMinutes(elapsedMilliseconds))));
        System.out.println();
    }
    
    private void buildDirectory() {
        DIRECTORY = new IndexedCollection<>
            (Work.class, //"Indexes of Work Collection", 
            new CompositeIndex<>
                (Audiobook.class, "Audiobooks by Title", 
                    Title.class, PublicationDate.class, Audiobook.class),
            new CompositeIndex<>
                (Audiobook.class, "Audiobooks by Publication Date", 
                    PublicationDate.class, Title.class, Audiobook.class),
            new CompositeIndex<>
                (Audiobook.class, "Audiobooks by Downloads", 
                        Downloads.class, Title.class, Audiobook.class),
            new CompositeIndex<>
                (Audiobook.class, "Audiobooks by Author, Title", 
                                Author.class, Title.class, Audiobook.class),
            new CompositeIndex<>
                (Audiobook.class, "Audiobooks by Author, Publication Date", 
                        Author.class, PublicationDate.class, Title.class, Audiobook.class),
            new CompositeIndex<>
                (Audiobook.class, "Audiobooks by Author, Downloads", 
                        Author.class, Downloads.class, Title.class, Audiobook.class),
            new CompositeIndex<>
                (Audiobook.class, "Audiobooks by Genre, Title", 
                        Genre.class, Title.class, Audiobook.class),
            new CompositeIndex<>
                (Audiobook.class, "Audiobooks by Genre, Publication Date", 
                        Genre.class, PublicationDate.class, Title.class, Audiobook.class),
            new CompositeIndex<>
                (Audiobook.class, "Audiobooks by Genre, Downloads", 
                        Genre.class, Downloads.class, Title.class, Audiobook.class),
            new CompositeIndex<>
                (Audiobook.class, "Audiobooks by Reader, Title", 
                                Reader.class, Title.class, Audiobook.class),
            new CompositeIndex<>
                (Audiobook.class, "Audiobooks by Reader, Publication Date", 
                        Reader.class, PublicationDate.class, Title.class, Audiobook.class),
            new CompositeIndex<>
                (Audiobook.class, "Audiobooks by Reader, Downloads", 
                        Reader.class, Downloads.class, Title.class, Audiobook.class),
            new CompositeIndex<>
                (Audiobook.class, "Audiobooks by Language, Title", 
                        Language.class, Title.class, Audiobook.class),
            new CompositeIndex<>
                (Audiobook.class, "Audiobooks by Language, Publication Date", 
                    Language.class, PublicationDate.class, Title.class, Audiobook.class),
            new CompositeIndex<>
                (Audiobook.class, "Audiobooks by Language, Downloads", 
                    Language.class, Downloads.class, Title.class, Audiobook.class));

    }
    
    public void shutDownDatabase() {
    }
    
    public void buildM4bAudiobookList() 
            throws InterruptedException {
        sortAudiobooks();
        m4bAudiobooks = audiobooks; // 2015-01-30 decision made for catalog to provide ALL audiobooks

        m4bAudiobooksArray = new Audiobook[m4bAudiobooks.size()];
        m4bAudiobooksArray = m4bAudiobooks.toArray(m4bAudiobooksArray);
        buildUrlMap();  // v1.4.2
    }
    
    /** added v1.4.2 */
    private void buildUrlMap () {
        for (Audiobook audiobook : m4bAudiobooks) {
            m4bAudiobooksUrlMap.put
                            (getUrlKey(audiobook.getUrlLibrivox()), audiobook);
        }
    }
    
    /** added v1.4.2<p>
     * Trims down a full URL string value to remove protocol indicator and 
     * librivox domain at start and optional slash at end. Used to create Map 
     * keyed by URL of LibriVox webpage.
     * @param fullUrlString Full URL string value
     * @return Trimmed URL string (trimmed of starting protocol/domain and ending slash) */
    protected static String getUrlKey (String fullUrlString) {
        if (fullUrlString == null) {
            return null;
        }
        String urlKey = fullUrlString.toLowerCase();
        urlKey = urlKey.replace("https://", "");
        urlKey = urlKey.replace("http://", "");
        urlKey = urlKey.replace("www.librivox.org/", "");
        urlKey = urlKey.replace("librivox.org/", "");
        if (urlKey.endsWith("/")) {
            urlKey = urlKey.substring(0, urlKey.length() - 1);
        }
        return urlKey;
    }
    
    private void removeNoNameAuthorsAndReaders(CatalogCallback callback)
            throws InterruptedException {
        long audiobookCount = 0;
        if (callback != null) {
            callback.updateTaskProgress(0, m4bAudiobooks.size());
        }
        List<Author> cleanedAuthors;
        List<Reader> cleanedReaders;
        for (Audiobook audiobook : m4bAudiobooks) {
            if (Thread.interrupted()) { throw new InterruptedException(); }
            audiobookCount++;
            if (callback != null && audiobookCount % CALLBACK_DIVISOR == 0) {
                callback.updateTaskProgress(audiobookCount, audiobooks.size());
            }
            cleanedAuthors = new ArrayList<>();
            if (audiobook.getAuthors() != null) {
                for (Author author : audiobook.getAuthors()) {
                    if (author.hasName()) {
                        cleanedAuthors.add(author);
                    }
                }
                audiobook.setAuthors(cleanedAuthors);
            }
            if (audiobook.getSections() == null) {
                continue;
            }
            for (Section section : audiobook.getSections()) {
                if (section.getAuthors() != null) {
                    cleanedAuthors = new ArrayList<>();
                    for (Author sectionAuthor : section.getAuthors()) {
                        if (sectionAuthor.hasName()) {
                            cleanedAuthors.add(sectionAuthor);
                        }
                    }
                    section.setAuthors(cleanedAuthors);
                }
                
                if (section.getReaders() != null) {
                    cleanedReaders = new ArrayList<>();
                    for (Reader reader : section.getReaders()) {
                        if (reader.getDisplayName() != null
                                && !reader.getDisplayName().isEmpty()) {
                            cleanedReaders.add(reader);
                        }
                    }
                    section.setReaders(cleanedReaders);
                }
            }
        }
        callback.updateTaskProgress(m4bAudiobooks.size(), m4bAudiobooks.size());
    }
    
    private void setSectionParentFields() 
            throws InterruptedException {
        for (Audiobook audiobook : m4bAudiobooks) {
            if (Thread.interrupted()) { throw new InterruptedException(); }
            if (audiobook.getSections() != null) {
                for (Section section : audiobook.getSections()) {
                    section.setParentAudiobookId(audiobook);
                    if (!audiobook.isVariousAuthorsWork()
                            && section.getAuthors() == null) {
                        section.setAuthors(audiobook.getAuthors());
                    }
                }
            }
        }
    }
    
    private void buildUniqueIndexedKeyLists(CatalogCallback callback) 
            throws InterruptedException{
        long audiobookCount = 0;
        if (callback != null) {
            callback.updateTaskProgress(0, m4bAudiobooks.size());
        }
        for (Audiobook audiobook : m4bAudiobooks) {
            if (Thread.interrupted()) { throw new InterruptedException(); }
            audiobookCount++;
            if (callback != null && audiobookCount % CALLBACK_DIVISOR == 0) {
                callback.updateTaskProgress(audiobookCount, m4bAudiobooks.size());
            }
            if (audiobook.getAuthors() != null) {
                for (Author author : audiobook.getAuthors()) {
                    if (!uniqueAuthorList.contains(author)) {
                        uniqueAuthorList.add(author);
                    }
                }
            }
            if (audiobook.getReaders() != null) {
                for (Reader reader : audiobook.getReaders()) {
                    if (!uniqueReaderList.contains(reader)) {
                        uniqueReaderList.add(reader);
                    }
                }
            }
            if (audiobook.getGenres() != null) {
                for (Genre genre : audiobook.getGenres()) {
                    if (!uniqueGenreList.contains(genre)) {
                        uniqueGenreList.add(genre);
                    }
                }
            }
            if (audiobook.getLanguage() != null
                    && !audiobook.getLanguage().getLanguage().isEmpty()) {
                if (!uniqueLanguageList.contains(audiobook.getLanguage())) {
                    uniqueLanguageList.add(audiobook.getLanguage());
                }
            }
            if (audiobook.getSections() != null) {
                for (Section section : audiobook.getSections()) {
                    if (section.getAuthors() != null) {
                        for (Author author : section.getAuthors()) {
                            if (!uniqueAuthorList.contains(author)) {
                                uniqueAuthorList.add(author);
                            }
                        }
                    }
                    if (section.getReaders() != null) {
                        for (Reader reader : section.getReaders()) {
                            if (!uniqueReaderList.contains(reader)) {
                                uniqueReaderList.add(reader);
                            }
                        }
                    }
                    if (section.getLanguage() != null
                            && !section.getLanguage().getLanguage().isEmpty()) {
                        if (!uniqueLanguageList.contains(section.getLanguage())) {
                            uniqueLanguageList.add(section.getLanguage());
                        }
                    }
                }
            }
        }
        callback.updateTaskProgress(m4bAudiobooks.size(), m4bAudiobooks.size());

    }
    
    private void buildSubgenreMap() 
            throws IOException {
        try(BufferedReader br 
                = new BufferedReader
                        (new InputStreamReader
                            (CatalogMarshaller.class.getResourceAsStream
                                        (CatalogMarshaller.GENRE_CSV_RESOURCE)))) {
            String line;
            while ((line = br.readLine()) != null) { 
                int firstComma = line.indexOf(',');
                String id = line.substring(0, firstComma);
                String name = line.substring(firstComma + 1, line.length());
                SUBGENRE_MAP.put(id, name);
            }  
        } catch (IOException ioe) {
            System.out.println
                ("**Serious IOException encountered while reading Genre CVS file resource.");
            throw ioe;
        }
    }
    
    private void setAudiobookAuthorReaderGenre(CatalogCallback callback) 
            throws InterruptedException {
        long audiobookCount = 0;
        if (callback != null) {
            callback.updateTaskProgress(0, m4bAudiobooks.size());
        }
        
        final Genre NO_GENRE_ASSIGNED = new Genre();
        NO_GENRE_ASSIGNED.setId(Audiobook.NO_GENRE_INDICATOR);
        NO_GENRE_ASSIGNED.setName(Audiobook.NO_GENRE_INDICATOR);
        List<Genre> NO_GENRE_ASSIGNED_LIST = new ArrayList<>();
        NO_GENRE_ASSIGNED_LIST.add(NO_GENRE_ASSIGNED);

        for (Audiobook audiobook : m4bAudiobooks) {
            if (Thread.interrupted()) { throw new InterruptedException(); }
            audiobookCount++;
            if (callback != null && audiobookCount % CALLBACK_DIVISOR == 0) {
                callback.updateTaskProgress(audiobookCount, audiobooks.size());
            }
            audiobook.addAuthorsFromSections();
            audiobook.setReaders();
            if (audiobook.getGenres() == null || audiobook.getGenres().isEmpty()) {
                audiobook.setGenres(NO_GENRE_ASSIGNED_LIST);
            }
        }
        if (callback != null) {
            callback.updateTaskProgress(m4bAudiobooks.size(), m4bAudiobooks.size());
        }
    }
    
    /** Populates CompositeIndex indexes: (1) one set for MasterClass objects (which 
 in this application are Work (audiobook and section) class objects; and
 (2) one set for IndexedKey-implementing objects (which in this application
 are objects such as author, reader, etc.).
     * N.B.: for purposes of this application, only audiobooks with M4B 
     * files are mapped (indexed) for retrieval.
     * @param callback
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.reflect.InvocationTargetException
     * @throws java.lang.InterruptedException */
    public void autofillDirectory (CatalogCallback callback) 
            throws IllegalAccessException,
                    InvocationTargetException,
                    InterruptedException     {
        long audiobookCount = 0;
        if (callback != null) {
            callback.updateTaskProgress(0, m4bAudiobooks.size());
        }
        for (Audiobook audiobook : m4bAudiobooks) {
            if (Thread.interrupted()) { throw new InterruptedException(); }
            audiobookCount++;
            if (callback != null && audiobookCount % CALLBACK_DIVISOR == 0) {
                callback.updateTaskProgress(audiobookCount, m4bAudiobooks.size());
            }
            DIRECTORY.add(audiobook);
        }
        if (callback != null) {
            callback.updateTaskProgress(m4bAudiobooks.size(), m4bAudiobooks.size());
        }
        System.out.println(DIRECTORY);
    }
    
    /** Get all works of (sub)class specified by workClass,
     * ordered by the submitted keyClass(es).
     * @param workClass
     * @param keyClassArray
     * @return 
     */
    @SafeVarargs
    public final List<Work> getWorks 
            (Class<? extends Work> workClass, 
                            Class<? extends Comparable>... keyClassArray) {
                
        timer.start();
        List<Work> returnList
                = DIRECTORY.getValues(workClass, keyClassArray);
        timer.stop();
        return returnList;
    }
    
    /** Get all works of (sub)class (specified by workClass) that are associated
 with the submitted instance of a IndexedKey-implementing class. (E.g.,
     * get all works of the Audiobook class that are associated with a specific
     * Author.)
     * @param workClass - Class that extends Work class
     * @param indexedKey - Instance of IndexedKey-implementing class
     * @param readerWorksOption - optionally limit list to solo, group works of reader
     * @return List of Work objects that are associated with the submitted 
     * object (e.g., works written by Author, read by Reader, etc.).
     */
    public List<Work> getWorks 
                    (Class<? extends Work> workClass, Comparable indexedKey,
                            ReaderWorksOption readerWorksOption) {
        //timer.start();
        List<Work> fullList = DIRECTORY.getValues(workClass, indexedKey);
        List<Work> returnList = applyReaderWorksOption(fullList, readerWorksOption);
        //timer.stop();
        return returnList;
    }
   
    /** Get all works of (sub)class (specified by workClass) that are associated
 with the submitted instance of a IndexedKey-implementing class. (E.g.,
     * get all works of the Audiobook class that are associated with a specific
     * Author.) Order of list is stipulated by vararg array of keyClass instances.
     * @param workClass - Class that extends Work class
     * @param indexedKey - Instance of IndexedKey-implementing class
     * @param readerWorksOption
     * @param keyClassArray - vararg array of keyClass instances, indicating
     * order in which list is to be returned.
     * @return List of Work objects that are associated with the submitted 
     * object (e.g., works written by Author, read by Reader, etc.).
     * @throws org.commonvox.indexedcollection.InvalidQueryException
     */
    @SafeVarargs
    public final List<Work> getWorks 
                    (Class<? extends Work> workClass, 
                            Comparable indexedKey,
                            ReaderWorksOption readerWorksOption,
                            Class<? extends Comparable>... keyClassArray) {
        //timer.start();
        if (PersistedUserSelectedCollection.class.isAssignableFrom(indexedKey.getClass())) {
            return getWorksFromPersistedUserPreferences
                (workClass, (PersistedUserSelectedCollection)indexedKey, keyClassArray);
        } else {
            List<Work> fullList 
                    = DIRECTORY.getValues(workClass, indexedKey, keyClassArray);
            List<Work> returnList 
                    = applyReaderWorksOption(fullList, readerWorksOption);
            //timer.stop();
            return returnList;
        }
    }
                    
    private List<Work> applyReaderWorksOption 
                    (List<Work> fullList,ReaderWorksOption readerWorksOption) {
        List<Work> returnList = new ArrayList<>();
        switch (readerWorksOption) {
            case ALL_WORKS:
                returnList = fullList;
                break;
            case SOLO_WORKS:
                for (Work work : fullList) {
                    if (work.getReaders() != null && work.getReaders().size() == 1) {
                        returnList.add(work);
                    }
                }
                break;
            case GROUP_WORKS:
                for (Work work : fullList) {
                    if (work.getReaders() != null && work.getReaders().size() > 1) {
                        returnList.add(work);
                    }
                }
                break;
        }
        return returnList;
    }
                    
    @SafeVarargs
    private final List<Work> getWorksFromPersistedUserPreferences 
                    (Class<? extends Work> workClass, 
                            PersistedUserSelectedCollection persistedUserPreferences,
                            Class<? extends Comparable>... keyClassArray) {
        List<Work> fullList = getWorks(workClass, keyClassArray);
        List<Work> returnList = new ArrayList<>();
        for (Work work : fullList) {
            if (persistedUserPreferences.contains(work)) {
                returnList.add(work);
            }
        }
        return returnList;
    }

    public Audiobook getAudiobook (String librivoxId) {
        if (librivoxId == null) {
            return null;
        }
        Audiobook searchAudiobook = new Audiobook();
        searchAudiobook.setId(librivoxId);
        int returnValue = Arrays.binarySearch
                                    (m4bAudiobooksArray, searchAudiobook);
        if (returnValue < 0) {
            return null;
        } else {
            return m4bAudiobooksArray[returnValue];
        }
    }
    
    public List<Work> getRandomAudiobooks (int numberRequested) {
        return getRandomAudiobooks(numberRequested,false);
    }
   
    public List<Work> getRandomAudiobooks 
            (int numberRequested, boolean suppressIfNoCoverArt) {
        Set<Work> randomSet = new LinkedHashSet<>();
        while (randomSet.size() < numberRequested) {
            Audiobook randomAudiobook 
                    = m4bAudiobooks.get
                        (RANDOM_NUMBER_GENERATOR.nextInt(m4bAudiobooks.size()));
            if (suppressIfNoCoverArt 
                    && (randomAudiobook.getUrlLocalCoverArt() == null
                        || randomAudiobook.getUrlLocalCoverArt().isEmpty())) {
                continue;
            }
            if (randomAudiobook.getDaysAvailable() 
                                == Audiobook.NOT_YET_AVAILABLE_INDICATOR) {
                continue;
            }
            randomSet.add(randomAudiobook); // LinkedHashSet assures no duplicates
        }
        return new ArrayList<Work>(randomSet);
    }
    
    /** Get list of instances of a specified IndexedKey-implementing class
     * (e.g., a list of Authors, a list of Readers, etc.). 
     * @param indexedKeyClass designates the Class of objects to be 
     * returned. (Must be a non-abstract subclass of 
     * {@link  org.commonvox.indexedcollection.IndexedKey IndexedKey}.)  
     * @return list of instances of the specified IndexedKey-implementing class
     * (e.g., a list of Authors, a list of Readers, etc.).
     * @throws org.commonvox.indexedcollection.InvalidQueryException
     */
    public List<Comparable> getIndexedKeyValueList
            (Class<? extends Comparable> indexedKeyClass) {
        //timer.start();
        List<Comparable> returnList 
                = DIRECTORY.getAttributeValues(indexedKeyClass);
        //timer.stop();
        return returnList;
    }
    
    /**
     *
     * @param searchParms
     * @param callback
     * @return
     * @throws InterruptedException
     */
    public SearchParameters searchForWorks 
            (SearchParameters searchParms, CatalogCallback callback) 
                throws InterruptedException {
        /*
        System.out.println("Beginning searchForWorks method");
        System.out.println("Number requested = " + searchParms.numberRequested);
        System.out.println("Search string = " + searchParms.searchString);
        System.out.println("Start marker = " + searchParms.startMarker);
        */
        callback.updateTaskMessage("Search in progress");
        callback.updateTaskProgress(0, searchParms.numberRequested);
        final int MAX_LIBRIVOX_IOEXCEPTION_COUNT = 10;
        int librivoxIOExceptionCount = 0;
        searchParms.returnedWorks = new ArrayList<>();
        searchLoop:
        while (searchParms.returnedWorks.size() < searchParms.numberRequested
                && !searchParms.endOfSearchEngineResultSet) {
            if (Thread.interrupted()) { throw new InterruptedException(); }
            if (searchParms.librivoxUrlStrings.isEmpty()) {
                String googleResponseString = "";
                try {
                    URL searchUrl 
                        = new URL((GOOGLE_API_URL_STRING + googleApiKey 
                                + GOOGLE_SEARCH_ENGINE_ID 
                                + String.format(START_TEMPLATE,(searchParms.startMarker)) 
                                + NUM_PARM + FIELDS_PARM + QUERY_DELIMITER)
                            + URLEncoder.encode((searchParms.searchString),UTF8_CHARSET));
                    googleResponseString 
                                = CatalogAssembler.getHttpContent(searchUrl);
                } catch (IOException e) {
                    searchParms.responseStatus 
                            = SearchParameters.RESPONSE_STATUS.SEARCH_IO_ERROR;
                    searchParms.errorMessage = e.getMessage();
                    return searchParms;
                }
                if (Thread.interrupted()) { throw new InterruptedException(); }
                //System.out.println(googleResponseString); // temp for testing
                ParsedSearchResponse googleResponse
                        = parseGoogleResponseString(googleResponseString);
                searchParms.startMarker += googleResponse.searchResultsSize;
                searchParms.librivoxUrlStrings.addAll(googleResponse.librivoxUrlStrings);
                if (searchParms.startMarker >= googleResponse.totalResultsSize) {
                    searchParms.endOfSearchEngineResultSet = true;
                }
            }
            while (!searchParms.librivoxUrlStrings.isEmpty()) {
                if (Thread.interrupted()) { throw new InterruptedException(); }
                /* MUCH more efficient audiobook lookup -- v1.4.2 */
                Audiobook audiobook
                    = m4bAudiobooksUrlMap.get
                        (getUrlKey(searchParms.librivoxUrlStrings.removeFirst()));
                if (audiobook == null) { 
                    //System.out.println
                    //    ("*** M4B audiobook not found with ID: " + librivoxId);
                } else {
                    searchParms.returnedWorks.add(audiobook);
                    callback.updateTaskProgress
                        (searchParms.returnedWorks.size(), 
                                    searchParms.numberRequested);
                    if (searchParms.returnedWorks.size() 
                                    >= searchParms.numberRequested) {
                        break searchLoop;
                    }
                }
            }
        }
        return searchParms;
    }
            
    private ParsedSearchResponse parseGoogleResponseString 
                                            (String googleResponseString) {
        ParsedSearchResponse googleResponse = new ParsedSearchResponse();

        final String JSON_KEYNAME_totalResults = "totalResults";
        final String JSON_KEYNAME_link = "link";
        String currentKeyName = "";
        JsonParser parser = Json.createParser(new StringReader(googleResponseString));
        while (parser.hasNext()) {
            switch(parser.next()) {
                case KEY_NAME:
                    currentKeyName = parser.getString();
                    if (currentKeyName.equals(JSON_KEYNAME_link)) {
                        googleResponse.searchResultsSize++;
                    }
                    break;
                case VALUE_STRING:
                    switch (currentKeyName) {
                        case JSON_KEYNAME_link:
                            googleResponse.librivoxUrlStrings.add(parser.getString());
                            break;
                        case JSON_KEYNAME_totalResults:
                            googleResponse.totalResultsSize 
                                = Integer.parseUnsignedInt(parser.getString());
                            break;
                    }
                    break;
            }
        }
        return googleResponse;
    }
    
    protected void fetchGoogleApiKey() {
        try {
            googleApiKey 
                = GOOGLE_API_KEY_DELIMITER 
                    + CatalogAssembler.getHttpContent
                        (CatalogMarshaller.DEFAULT_URL_STRING_GOOGLE_API_KEY);
        } catch (IOException e) {
            googleApiKey = "";
        }
    }

    private void fetchLatestVersionNumber() {
        try {
            latestVersion 
                = CatalogAssembler.getHttpContent
                        (CatalogMarshaller.DEFAULT_URL_STRING_LATEST_VERSION);
            System.out.println("Latest publicly available version of this "
                                + "application = " + latestVersion);
        } catch (IOException e2) {
            System.out.println("IO exception fetching latest version"); // temp
            latestVersion = "";
        }
    }

    @XmlTransient
    private class ParsedSearchResponse {
        List<String> librivoxUrlStrings = new ArrayList<>();
        int searchResultsSize = 0;
        int totalResultsSize = 0;
    }
    
    /**
     * Get latest version of this application as retrieved from remote
     * file server
     * @return Latest version of this application
     */
    public static String getLatestVersion() {
        return latestVersion;
    }
    
    /**
     * Get URL string with extension structured for prefilling of the Wufoo
     * form on the webpage denoted by the private constant PROB_REPORT_URL_STRING.
     * @param operatingSystem Operating system that this application is running in
     * @return URL string with extension structured for prefilling of the Wufoo
     * form on the webpage denoted by the private constant PROB_REPORT_URL_STRING.
     */
    public static String getProblemReportUrlString (String operatingSystem) {
        return getWufooUrlString ("", "", "", "", "", "", operatingSystem);
    }
    
    /**
     * Get URL string with extension structured for prefilling of the Wufoo
     * form on the webpage denoted by the private constant PROB_REPORT_URL_STRING.
     * @param comments User comments (may be empty String)
     * @param technicalDetails Intended to contain stack trace caught by UI 
     * layer's exception handler
     * @param operatingSystem Operating system that this application is running in
     * @return URL string with extension structured for prefilling of the Wufoo
     * form on the webpage denoted by the private constant PROB_REPORT_URL_STRING.
     */
    public static String getProblemReportUrlString 
            (String comments, String technicalDetails, String operatingSystem) {
        // length of URL limited to approx 2K chars
        final int MAX_TECHNICAL_DETAILS_LENGTH = 1700;
        if (technicalDetails.length() > MAX_TECHNICAL_DETAILS_LENGTH) {
            technicalDetails 
                    = technicalDetails.substring(0, MAX_TECHNICAL_DETAILS_LENGTH);
        }
        return getWufooUrlString ("", comments, technicalDetails, "", "", "", 
                                                            operatingSystem);
    }
    
    private static String getWufooUrlString (String... fields) {
        /*
        wufoo form is at PROB_REPORT_URL_STRING 
        wufoo fields =
            field1 -- email address
            field2 -- comments
            field3 -- technical details (i.e., stacktrace, etc.)
            field7 -- operating system
        */
        StringBuilder formPrefillUrlExtension = new StringBuilder();
        try {
            if (fields.length > 0) {
                int fieldCount = 0;
                for (String field : fields) {
                    ++fieldCount;
                    if (field.isEmpty()) {
                        continue;
                    }
                    if (formPrefillUrlExtension.length() > 0) {
                        formPrefillUrlExtension.append("&");
                    }
                    formPrefillUrlExtension.append("field").append(fieldCount)
                        .append("=")
                        .append(applyWufooFilter(URLEncoder.encode(field, "UTF-8")));
                }
            }
        } catch (UnsupportedEncodingException e) {}
        return PROB_REPORT_URL_STRING + "def/" + formPrefillUrlExtension;
    }
    
    /** The Wufoo http interface has idiosyncracies (inability to deal
     * with standard URI encoding) that must be "filtered out" for us to
     * be able to submit prefilling string values to a wufoo form. See:
     * http://help.wufoo.com/articles/en_US/SurveyMonkeyArticleType/URL-Modifications#format 
     * Additional undocumented "filtration" is required: [1] the wufoo URL
     * parser cannot handle a standard "line feed" (i.e., "%0A"). All instances
     * must be replaced by a standard "carriage return" (i.e., "%0D"). 
     * [2] the wufoo URL parser cannot handle an encoded right slash ("%2F").
     * All instances of "%2F" must be changed back to a literal right slash; then
     * as per wufoo documentation at the above-listed link, any contiguous pairs
     * of slashes must be separated with a filler character. */
    private static String applyWufooFilter (String urlExtension) {
        return urlExtension.replace("%0D%0A","%0D").replace("%0A","%0D")
                .replace("%2F","/").replace("//","/_/")
                .replace("%23","%5Bhash%5D").replace("%26","%5Bamper%5D");
    }
    
    protected static void printHeading(String headingTitle) {
        String headingBorder 
            = new String(new char[headingTitle.length()]).replace("\0", "=");

        System.out.println(headingBorder);
        System.out.println(headingTitle);
        System.out.println(headingBorder);
    }

    protected static void printHeadingWithTimestamp(String headingTitle) {
        printHeadingWithTimestamp(headingTitle, 0);
    }
    
    protected static void printHeadingWithTimestamp
                        (String headingTitle, int indexSize) {
        String headingBorder 
            = new String(new char[headingTitle.length()]).replace("\0", "=");

        System.out.println(headingBorder);
        System.out.println(headingTitle + "  (" 
                    + new Timestamp(System.currentTimeMillis()) + ")");
        if (indexSize > 0) {
            System.out.println("(SIZE OF INDEX = " + indexSize + " ENTRIES)");
        }
        System.out.println(headingBorder);
    }
                        
    public void printAudiobooksWithNoM4b () {
        int count = 0;
        System.out.println("===========================================");
        System.out.println("AUDIOBOOKS WITH NO M4B LINKS IN M4B CATALOG");
        System.out.println("===========================================");
        for (Audiobook audiobook : audiobooks) {
            if (audiobook.getUrlM4bFiles().isEmpty()) {
                count++;
                System.out.println(audiobook);
            }
        }  
        System.out.println("===========================================");
        System.out.println(count + " audiobook listings found WITHOUT links to M4B file(s).");
    }

    private String getDataRetrievalTimeString (long milliseconds) {
        return "Data retrieval took " 
                + ((milliseconds > 0) ? milliseconds : "less than 1") 
                + " milliseconds.";
    }
    
    public void generateNoGenreReportCSV (String outputPathString) 
            throws IOException {
        generateNoGenreReportCSV(new File(outputPathString));
    }
    
    public void generateNoGenreReportCSV (File outputFile) 
            throws IOException {
        System.out.println("** Generating NO GENRE report **");
        final char COMMA = ',';
        final char QUOTE = '"';
        final String LINE_FEED = "\n";
        StringBuilder csvReportOutput = new StringBuilder();
        // append CSV header
        csvReportOutput.append("librivox_id").append(COMMA)
                .append("librivox_url").append(COMMA)
                .append("title").append(LINE_FEED);
        for (Audiobook audiobook : m4bAudiobooks) {
            if (audiobook.getDaysAvailable() 
                    == Audiobook.NOT_YET_AVAILABLE_INDICATOR) {
                continue;
            }
            if (audiobook.getGenres() == null || audiobook.getGenres().isEmpty()
                    || (audiobook.getGenres().size() == 1
                        && audiobook.getGenres().get(0).getKeyItem()
                                        .equals(Audiobook.NO_GENRE_INDICATOR))) {
                String librivoxUrl 
                        = (audiobook.getUrlLibrivox() == null
                            || audiobook.getUrlLibrivox().isEmpty() 
                                    ? "NO_URL" : audiobook.getUrlLibrivox());
                String title
                        = (audiobook.getTitle() == null
                            || audiobook.getTitle().isEmpty() 
                                    ? "NO_TITLE" : audiobook.getTitle());
                csvReportOutput.append(audiobook.librivoxId).append(COMMA)
                        .append(librivoxUrl).append(COMMA)
                        .append(QUOTE).append(title).append(QUOTE).append(LINE_FEED);
            }
        }
        System.out.println("** Outputting NO GENRE report to: " + outputFile.getPath());
        FileWriter writer = new FileWriter(outputFile);
        writer.write(csvReportOutput.toString());
        writer.flush();
        writer.close();
        System.out.println("** REPORT GENERATION IS COMPLETE **");
    }
    
    @XmlTransient
    protected class Timer {
        private long timerMilliseconds = 0;
        private long elapsedMilliseconds = 0;
        
        protected void reset() {
            elapsedMilliseconds = 0;
        }

        protected void start () {
            timerMilliseconds = System.currentTimeMillis();
        }

        protected void stop () {
            elapsedMilliseconds += (System.currentTimeMillis() - timerMilliseconds);
        }

        protected long get () {
            long returnMilliseconds = elapsedMilliseconds;
            reset();
            return returnMilliseconds;
        }
    }
    public static void printMemoryUsage(String subTitle, boolean garbageCollect) {
        if (garbageCollect) {
            System.gc();
        }
        System.out.println("MEMORY USAGE -- " + subTitle 
                        + (garbageCollect? " GARBAGE COLLECT SUGGESTED" : ""));
        long freeMemory = java.lang.Runtime.getRuntime().freeMemory();
        long totalMemory = java.lang.Runtime.getRuntime().totalMemory();
        System.out.println
            ("  Used memory: " 
                + String.format("%,13d",(totalMemory - freeMemory) ));
        System.out.println
            ("  Free memory: " 
                + String.format("%,13d",freeMemory));
        System.out.println
            (" Total memory: " 
                + String.format("%,13d",totalMemory));
//        System.out.println
//            ("Number of active nodes in IndexedCollections: " 
//                                    + CompositeIndex.getNodeCount());
    }
}
