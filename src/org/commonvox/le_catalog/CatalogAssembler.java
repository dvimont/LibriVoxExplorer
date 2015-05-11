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
import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Daniel Vimont
 */
public class CatalogAssembler {
    private static final int MAX_LIBRIVOX_ID  = 9999;
    /** searches for "href" attribute with "http" or "https" url value 
     * NOTE: 2015-02-10 added \s to the regex to allow whitespace between 
     * quotation mark and http */
    private static final String REGEX_HREF = "href\\=\"\\s?https?://.*?\"";
    private static final Pattern HREF_PATTERN 
                = Pattern.compile(REGEX_HREF, Pattern.CASE_INSENSITIVE);
    private static Matcher hrefMatcher;
    private static final String IO_LABEL = "IO";
    private static final String METADATA_EXTRACTION_LABEL = "Metadata Extraction";
    private static final String JSON_KEYNAME_SUFFIX_jpg = ".jpg";
    private static final String JSON_KEYNAME_SUFFIX_m4b = ".m4b"; // added v1.5.1
    private static final Pattern A_TAG_PATTERN
                = Pattern.compile("\\<a .*?\\>", Pattern.CASE_INSENSITIVE);
    private static final Pattern DOWNLOAD_COVER_PATTERN
                = Pattern.compile("\\<.*?class=\"download-cover\".*?\\>", 
                                                Pattern.CASE_INSENSITIVE);
    private static final String STAGE01_FILE_NAME = "stage01.xml";
    private static final String STAGE02_FILE_NAME = "stage02.xml";
    private static final String STAGE03_FILE_NAME = "stage03.xml";
    private static final String STAGE04_FILE_NAME = "stage04.xml";
    //private static final String STAGE05_FILE_NAME = "stage05.xml";
    private static final String STAGE06_FILE_NAME = "stage06.xml";
    private static final String STAGE07_FILE_NAME = "stage07.xml";
    private static final String STAGE07_FORMATTED_FILE_NAME = "stage07.formatted.xml";
    private static final String DELTA_FILE_NAME = "delta.xml";
    private static final String DELTA_FORMATTED_FILE_NAME = "delta.formatted.xml";
    private static final String[] INVALID_LIBRIVOX_URL_PREFIXES
            = {"http://librivox.org/forum/", "https://librivox.org/forum/",
                    "http://forum.librivox.org/", "https://forum.librivox.org/"};
    private static final String LV_WIKI_M4B_URL_STRINGS
                            = "resources/librivox_wiki_m4b_webpages.txt";
    
    /** Following successful run of this method, the resultant stage 7 XML file
     * should replace the current "catalog.xml" in the project folder, and a
     * complete rebuild should be done (likely with incremented release/SM).
     *
     * @param currentBuildPath
     * @param startingId
     * @param processingLimit
     * @throws JAXBException
     * @throws MalformedURLException
     * @throws IllegalAccessException
     * @throws RemoteApiProcessingException
     * @throws IOException
     * @throws ParseException
     * @throws java.lang.InterruptedException
     */
    public static void assembleCompleteCatalog(String currentBuildPath, 
                                                int startingId, int processingLimit) 
            throws JAXBException, MalformedURLException,
                    IllegalAccessException, RemoteApiProcessingException,
                    IOException, ParseException, InterruptedException {
        
        File stage01XmlFile 
                    = Paths.get(currentBuildPath, STAGE01_FILE_NAME).toFile();
        File stage02XmlFile 
                    = Paths.get(currentBuildPath, STAGE02_FILE_NAME).toFile();
        File stage03XmlFile 
                    = Paths.get(currentBuildPath, STAGE03_FILE_NAME).toFile();
        File stage04XmlFile 
                    = Paths.get(currentBuildPath, STAGE04_FILE_NAME).toFile();
        File stage06XmlFile 
                    = Paths.get(currentBuildPath, STAGE06_FILE_NAME).toFile();
        File stage07XmlFile 
                    = Paths.get(currentBuildPath, STAGE07_FILE_NAME).toFile();
        File stage07XmlFormattedFile 
                = Paths.get(currentBuildPath, STAGE07_FORMATTED_FILE_NAME).toFile();
        
        
        System.out.println("** COMMENCING FULL ASSEMBLY OF CATALOG ** "
                            + new Timestamp(System.currentTimeMillis()));
        Catalog stage01Catalog = assembleCatalogStage01(startingId, processingLimit);
        CatalogMarshaller.marshalCatalogToXml(stage01Catalog, stage01XmlFile);
        Catalog stage02Catalog = assembleCatalogStage02(stage01Catalog);
        CatalogMarshaller.marshalCatalogToXml(stage02Catalog, stage02XmlFile);
        Catalog stage03Catalog = assembleCatalogStage03(stage02Catalog);
        CatalogMarshaller.marshalCatalogToXml(stage03Catalog, stage03XmlFile);
        if (processingLimit == 0) {
            Catalog stage04Catalog = assembleCatalogStage04(null);
            CatalogMarshaller.marshalCatalogToXml(stage04Catalog, stage04XmlFile);
            //Catalog stage05Catalog = assembleCatalogStage05(stage04Catalog);
            //CatalogMarshaller.marshalCatalogToXml(stage05Catalog, stage05XmlFile);
            Catalog stage06Catalog 
                        = assembleCatalogStage06(stage03Catalog, stage04Catalog);
            CatalogMarshaller.marshalCatalogToXml(stage06Catalog, stage06XmlFile);
            Catalog stage07Catalog = assembleCatalogStage07(stage06Catalog);
            CatalogMarshaller.marshalCatalogToXml(stage07Catalog, stage07XmlFile);
            CatalogMarshaller.marshalCatalogToXml(stage07Catalog, stage07XmlFormattedFile, true);
        }
        System.out.println("** COMPLETED FULL ASSEMBLY OF CATALOG ** "
                            + new Timestamp(System.currentTimeMillis()));
    }
    
    /** STAGE 1: Extract all available metadata for each work by accessing
     * the LibriVox API.
     * Important note: when the "offset" and "limit" options are used with the 
     * LibriVox API, the results returned vary wildly in count and order (e.g., 
     * set "limit=50" and get back 37 records in undefined order). Unfortunately,
     * this means that book records must be requested one at a time via "id=" 
     * parameter (extremely inefficient, but the only way to get all records).
     * @param currentCatalog
     * @param restartId
     * @param processingLimit
     * @return 
     * @throws javax.xml.bind.JAXBException 
     * @throws java.net.MalformedURLException 
     * @throws java.lang.InterruptedException 
     */
    protected static Catalog assembleCatalogStage01 (Catalog currentCatalog, 
                                                        int restartId, 
                                                        int processingLimit) 
            throws JAXBException, MalformedURLException, InterruptedException {
        final String LV_API_CALL
            = "https://librivox.org/api/feed/audiobooks/?id=%s&extended=1&format=xml";
        String apiCallString;
        String rePrefix = "";
        Catalog lvCatalog = new Catalog();
        int startingCatalogSize = 0;
        if (currentCatalog != null) {
            lvCatalog = currentCatalog;
            startingCatalogSize = lvCatalog.audiobooks.size();
            rePrefix = "RE";
        }
        if (restartId == 0) {
            restartId = 1;
        }
        
        System.out.println("==============================");
        System.out.println("Stage 1 processing " + rePrefix + "initiated. " 
                            + new Timestamp(System.currentTimeMillis()));
        System.out.println("Processing starting at ID = " + restartId);
        if (processingLimit != 0) {
            System.out.println("Processing limit = " + processingLimit);
        }
        System.out.println("==============================");
        
        int processingCount = 0;
        for (int idCount = restartId ; idCount <= MAX_LIBRIVOX_ID ; idCount++ ) {
            if (Thread.interrupted()) { throw new InterruptedException(); }
            apiCallString = String.format(LV_API_CALL, idCount);
            try {
                Catalog apiCatalog 
                    = CatalogMarshaller.unmarshalCatalogFromXml(apiCallString);
                lvCatalog.append(apiCatalog);
            } catch (JAXBException e) { 
                /** FileNotFoundException occurs if no record exists for a 
                 * requested ID. This occurs many times during normal processing. */
                if (!(e.getLinkedException() instanceof FileNotFoundException)) {
                    throw e;
                }
            }
            
            int workCount = lvCatalog.audiobooks.size() - startingCatalogSize;
            if ( workCount % 100 == 0 && workCount > 0 ) {
                System.out.println(workCount + " audiobooks processed so far. "
                                + new Timestamp(System.currentTimeMillis()));
            }
            if (processingLimit > 0
                    && ++processingCount >= processingLimit) {
                break;
            }
        }

        lvCatalog.sortAudiobooks();
        String highestId 
                = lvCatalog.audiobooks.get(lvCatalog.audiobooks.size() - 1).getId();
        
        // use standard Java logging facilities for these messages?
        System.out.println("=============================");
        System.out.println("Stage 1 processing completed. " 
                                    + new Timestamp(System.currentTimeMillis()));
        System.out.println((lvCatalog.audiobooks.size() - startingCatalogSize) 
                                                + " audiobooks processed.");
        System.out.println(lvCatalog.audiobooks.size() + " audiobooks in catalog.");
        System.out.println("Highest audiobook ID found: " + highestId);
        
        return lvCatalog;
    }
    
    protected static Catalog assembleCatalogStage01 (Catalog currentCatalog) 
            throws JAXBException, MalformedURLException, InterruptedException {
        String lastId 
                = currentCatalog.audiobooks.get(currentCatalog.audiobooks.size() - 1).getId();
        int restartId = Integer.parseInt(lastId) + 1;
        return assembleCatalogStage01(currentCatalog, restartId, 0);
    }

    protected static Catalog assembleCatalogStage01 (int restartId, int processingLimit) 
            throws JAXBException, MalformedURLException, InterruptedException {
        return assembleCatalogStage01(null, restartId, processingLimit);
    }
    
    /** STAGE 2: For each work, extract URL for cover art from the item's 
     * LibriVox webpage. For works with multiple authors, for each section 
     * extract the following from the item's LibriVox webpage: 
     * author ID, author name, and URL for text.
     * @param catalog
     * @return 
     * @throws java.lang.IllegalAccessException 
     * @throws javax.xml.bind.JAXBException 
     * @throws org.commonvox.le_catalog.RemoteApiProcessingException 
     * @throws java.lang.InterruptedException 
     */
    protected static Catalog assembleCatalogStage02 (Catalog catalog)
            throws IllegalAccessException, JAXBException, 
                    RemoteApiProcessingException, InterruptedException {
        int audiobooksProcessedCount = 0;
        int audiobooksCoverArtUrlFoundCount = 0;
        int audiobooksMultipleAuthorsCount = 0;
        int audiobooksWithoutLibriVoxUrlCount = 0;
        int audiobooksWithExceptionsCount = 0;
        int metadataExtractionExceptionCount = 0;
        int ioExceptionCount = 0;
        Audiobook audiobookFragment = null;
        List<Section> sectionsWithAuthorAndTextMetadata;
        
        System.out.println("=============================");
        System.out.println("Stage 2 processing initiated. " 
                            + new Timestamp(System.currentTimeMillis()));
        System.out.println("=============================");
        
        audiobookLoop:
        for (Audiobook audiobook : catalog.audiobooks) {
            if (Thread.interrupted()) { throw new InterruptedException(); }
            audiobooksProcessedCount++;
            if (audiobooksProcessedCount % 100 == 0) {
                System.out.println(audiobooksProcessedCount 
                        + " audiobooks processed so far. "
                        + new Timestamp(System.currentTimeMillis()));
            }
            if (audiobook.getId() == null || audiobook.getId().equals("")) {
                continue;
            }
            if (audiobook.getUrlLibrivox() != null) {
                for (String invalidUrlPrefix : INVALID_LIBRIVOX_URL_PREFIXES) {
                    if (audiobook.getUrlLibrivox().startsWith(invalidUrlPrefix)) {
                        audiobook.setUrlLibrivox(null);
                        break;
                    }
                }
            }
            if (audiobook.getUrlLibrivox() == null 
                        || audiobook.getUrlLibrivox().isEmpty()) {
                audiobooksWithoutLibriVoxUrlCount++;
                continue;
            }
            try {
                audiobookFragment = extractCoverArtUrls(audiobook.getUrlLibrivox());
                if (audiobookFragment == null 
                        || audiobookFragment.getUrlCoverArt() == null
                        || audiobookFragment.getUrlCoverArt().isEmpty()) {
                    throw new MetadataExtractionException
                        ("No CoverArt URL found for audiobook" 
                            + " with ID = " + audiobook.getId());
                } else {
                    audiobooksCoverArtUrlFoundCount++;
                    audiobook.setUrlCoverArt(audiobookFragment.getUrlCoverArt());
                }
            } catch (MetadataExtractionException | IOException e) {
                    audiobooksWithExceptionsCount++;
                    String exceptionLabel = "Exception";
                    if (e instanceof MetadataExtractionException) {
                        metadataExtractionExceptionCount++;
                        exceptionLabel = METADATA_EXTRACTION_LABEL;
                    } else if (e instanceof IOException) {
                        ioExceptionCount++;
                        exceptionLabel = IO_LABEL;
                    }
                    System.err.println
                        (">>>> " + exceptionLabel + " problem encountered while" 
                                + " processing audiobook with ID=" + audiobook.getId()
                                + " with LibriVox Catalog webpage at URL: " 
                                + audiobook.getUrlLibrivox());
                    // e.printStackTrace();
            }
            for (Author author : audiobook.getAuthors()) {
                if (!author.getId().equals("18")) {
                    continue audiobookLoop;
                }
                audiobooksMultipleAuthorsCount++;
                try {
                    sectionsWithAuthorAndTextMetadata 
                            = extractAuthorAndUrlTextMetadata(audiobook.getUrlLibrivox());
                    if (sectionsWithAuthorAndTextMetadata.isEmpty() 
                            || sectionsWithAuthorAndTextMetadata.size() 
                                    != audiobook.getSections().size()) {
                        throw new MetadataExtractionException
                            ("Discrepancy in section count (a) via API and" 
                                + " (b) via LV Catalog webpage for audiobook" 
                                + " with ID = " + audiobook.getId());
                    }
                    int sectionIndex = 0;
                    for (Section section : audiobook.getSections()) {
                        section.merge(sectionsWithAuthorAndTextMetadata.get(sectionIndex));
                        sectionIndex++;
                    }

                } catch (MetadataExtractionException | IOException e) {
                    audiobooksWithExceptionsCount++;
                    String exceptionLabel = "Exception";
                    if (e instanceof MetadataExtractionException) {
                        metadataExtractionExceptionCount++;
                        exceptionLabel = METADATA_EXTRACTION_LABEL;
                    } else if (e instanceof IOException) {
                        ioExceptionCount++;
                        exceptionLabel = IO_LABEL;
                    }
                    System.err.println
                        (">>>> " + exceptionLabel + " problem encountered while" 
                                + " processing audiobook with ID=" + audiobook.getId()
                                + " with LibriVox Catalog webpage at URL: " 
                                + audiobook.getUrlLibrivox());
                    // e.printStackTrace();
                }
                break;
            }
        }
        System.out.println("=============================");
        System.out.println("Stage 2 processing completed. "
                        + new Timestamp(System.currentTimeMillis()));
        System.out.println(audiobooksProcessedCount + " audiobooks processed.");
        System.out.println(audiobooksCoverArtUrlFoundCount
                + " audiobook cover art URLs found.");
        System.out.println(audiobooksMultipleAuthorsCount 
                + " audiobooks with multiple authors processed.");
        System.out.println(audiobooksWithoutLibriVoxUrlCount 
                + " audiobooks without valid LibriVox URL.");
        System.out.println(metadataExtractionExceptionCount 
                + " audiobooks with webpages from which metadata could not be extracted.");
        System.out.println(ioExceptionCount 
                + " audiobooks with webpages inaccessible.");
        return catalog;
    }

    protected static Catalog assembleCatalogStage03 (Catalog catalog) 
            throws IOException, ParseException, IllegalAccessException,
                InterruptedException {
        return assembleCatalogStage03(catalog, false);
    }
    
    /** STAGE 3: For each audiobook, retrieve the following from 
     * from the audiobook's Internet Archive webpage: (a) download-count, 
     * (b) cover art URLs, and (c) URLs for M4B files (which may be on pages 
     * in comments). Note that this method uses JSON parsing for data retrieval.
     * Since download-count metadata must be updated at regular intervals, this
     * method may be called in "overwrite" mode (via the boolean parameter).
     * @param catalog
     * @param overwrite
     * @return
     * @throws java.io.IOException
     * @throws java.text.ParseException
     * @throws java.lang.IllegalAccessException
     * @throws java.lang.InterruptedException  */
    protected static Catalog assembleCatalogStage03 (Catalog catalog, boolean overwrite) 
            throws IOException, ParseException, IllegalAccessException,
                InterruptedException {
        final String JSON_LABEL = "JSON Parsing";
        int audiobooksProcessedCount = 0;
        int audiobooksBypassedCount = 0;
        int audiobooksUpdatedCount = 0;
        int noUrlCount = 0;
        int exceptionCount = 0;
        int ioExceptionCount = 0;
        int jsonParsingExceptionCount = 0;
        int sectionSizeDiscrepancyCount = 0;
        String overwriteNotice = "";
        if (overwrite) {
            overwriteNotice = " in OVERWRITE mode";
        }
        System.out.println("=============================");
        System.out.println("Stage 3 processing initiated" + overwriteNotice
                + ". " + new Timestamp(System.currentTimeMillis()));
        System.out.println("=============================");
        
        audiobookLoop:
        for (Audiobook audiobook : catalog.audiobooks) {
            if (Thread.interrupted()) { throw new InterruptedException(); }
            audiobooksProcessedCount++;
            if (audiobooksProcessedCount % 100 == 0) {
                System.out.println(audiobooksProcessedCount 
                        + " audiobooks processed so far. "
                        + new Timestamp(System.currentTimeMillis()));
            }
            if (!overwrite) {
                if (audiobook.getDownloadCountInternetArchive() != 0) {
                    audiobooksBypassedCount++;
                    continue;
                }
            }
            if (audiobook.getUrlInternetArchive() == null 
                    || audiobook.getUrlInternetArchive().isEmpty()) {
                noUrlCount++;
                continue;
            }
            
            try {
                Audiobook audiobookArchiveData
                    = extractAudiobookMetadataFromIA(audiobook.getUrlInternetArchive());
                
                boolean unequalSectionsSizes = false;
                if (audiobook.getSections() == null || audiobook.getSections().isEmpty()) {
                    if (!audiobookArchiveData.getSections().isEmpty()) {
                        unequalSectionsSizes = true;
                        audiobook.setSections(audiobookArchiveData.getSections());
                    }
                } else if (!audiobookArchiveData.getSections().isEmpty()) {
                    if (audiobook.getSections().size()
                            != audiobookArchiveData.getSections().size()) {
                        unequalSectionsSizes = true;
                    }
                    int sectionCount = 
                        (audiobook.getSections().size() 
                                <= audiobookArchiveData.getSections().size())?
                        audiobook.getSections().size()
                            : audiobookArchiveData.getSections().size();
                    for (int i=0; i < sectionCount; i++) {
                        audiobook.getSections().get(i).setUrlForListening 
                            (audiobookArchiveData.getSections()
                                                .get(i).getUrlForListening());
                    }
                }
                if (unequalSectionsSizes) {
                    sectionSizeDiscrepancyCount++;
                    System.out.println
                        ("** WARNING: Section list sizes differ between LV and IA "
                                + "for audiobook: " + audiobook.getId());
                }
                audiobookArchiveData.setSections(null); // prevent overlay in merge
                
                audiobook.merge(audiobookArchiveData);
                /** clear displayTitle if extraneous */
                if (audiobook.getTitle() != null 
                        && audiobook.getDisplayTitle() != null
                        && audiobook.getDisplayTitle().toLowerCase().trim()
                                .equals(audiobook.getTitle().toLowerCase())) {
                    audiobook.setDisplayTitle(null);
                } 

                audiobooksUpdatedCount++;
            }
            catch (IOException | JsonParsingException e) {
                exceptionCount++;
                String exceptionLabel = "Exception";
                if (e instanceof JsonParsingException) {
                    jsonParsingExceptionCount++;
                    exceptionLabel = JSON_LABEL;
                } else if (e instanceof IOException) {
                    ioExceptionCount++;
                    exceptionLabel = IO_LABEL;
                }
                System.err.println
                    (">>>> " + exceptionLabel + " problem encountered while" 
                            + " processing audiobook with ID=" + audiobook.getId()
                            + " with Internet Archive webpage at URL: " 
                            + audiobook.getUrlInternetArchive());
                e.printStackTrace();
                //e.printStackTrace();
            }
        }
        System.out.println("=============================");
        System.out.println("Stage 3 processing completed. " 
                                + new Timestamp(System.currentTimeMillis()));
        System.out.println("=============================");
        System.out.println(audiobooksProcessedCount + " audiobooks processed.");
        System.out.println(audiobooksUpdatedCount + " audiobooks updated.");
        if (!overwrite) {
            System.out.println(audiobooksBypassedCount + " audiobooks bypassed.");
        }
        System.out.println(noUrlCount 
                + " audiobooks with no URL to an Internet Archive webpage.");
        System.out.println(jsonParsingExceptionCount 
                + " audiobooks with webpages with JSON parsing exception.");
        System.out.println(sectionSizeDiscrepancyCount 
                + " audiobooks with discrepancy in number of sections between " 
                + "LibriVox and Internet Archive.");
        System.out.println(ioExceptionCount 
                + " audiobooks with webpages inaccessible.");
        
        return catalog;
    }

    protected static Catalog assembleCatalogStage04 (String urlStringsFilePath)
            throws IOException, InterruptedException { 
        boolean showBadLinks;
        return CatalogAssembler.assembleCatalogStage04
                                ((showBadLinks = false), urlStringsFilePath);
    }   
    
    /** Extract the following from the LibriVox wiki pages which list M4B files:
     * (1) URL for item's LibriVox webpage;
     * (2) URL(s) for item's M4B file(s)
     * @param showBadLinks
     * @param urlStringsFilePath
     * @return 
     * @throws java.io.IOException 
     * @throws java.lang.InterruptedException 
     */
    protected static Catalog assembleCatalogStage04
            (boolean showBadLinks, String urlStringsFilePath)
                throws IOException, InterruptedException {
        /** searches for "li" element (non-greedily) */
        final String REGEX_LI_ELEMENT = "\\<li .*?\\</li\\>";
        /** searches inside "li" element for attribute 'class="gallerybox"' */
        final String REGEX_GALLERYBOX = "class=\"gallerybox\"";
        /** searches for "href" attribute with url in domain "librivox.org" 
         * ("www." optional) */
        final String REGEX_HREF_LIBRIVOX_DOMAIN
                    = "href\\=\"https?://(www\\.)?librivox\\.org.*?\"";
        /** searches for "href" attribute with url in domain "archive.org" 
         * ("www." optional) */
        final String REGEX_HREF_ARCHIVE_DOMAIN_M4B
            = "href\\=\"https?://(www\\.)?archive\\.org.*?\\.m4b\"";
        final String REGEX_P_ELEMENT_W_TITLE = "\\<p\\>.*?\\<";
        final String REGEX_DIV_ELEMENT_W_GALLERYTEXT
            = "\\<div class=\"gallerytext\"\\>\\<p\\>.*";
        /** searches for "href" attribute with url in domain "wiki.librivox.org" */
        final String REGEX_HREF_LIBRIVOX_WIKI_SUBDOMAIN
            = "href\\=\"https?://wiki\\.librivox\\.org.*?\"";
        
        final Pattern liElementPattern 
                = Pattern.compile(REGEX_LI_ELEMENT, Pattern.CASE_INSENSITIVE);
        Matcher liElementMatcher;
        final Pattern galleryboxPattern 
                = Pattern.compile(REGEX_GALLERYBOX, Pattern.CASE_INSENSITIVE);
        final Pattern lvCatalogHrefPattern 
                = Pattern.compile(REGEX_HREF_LIBRIVOX_DOMAIN, Pattern.CASE_INSENSITIVE);
        Matcher lvCatalogHrefMatcher;
        final Pattern m4bHrefPattern 
                = Pattern.compile(REGEX_HREF_ARCHIVE_DOMAIN_M4B, Pattern.CASE_INSENSITIVE);
        Matcher m4bHrefMatcher;
        final Pattern titlePattern 
                    = Pattern.compile(REGEX_P_ELEMENT_W_TITLE, Pattern.CASE_INSENSITIVE);
        Matcher titleMatcher;
        final Pattern gallerytextPattern 
                = Pattern.compile(REGEX_DIV_ELEMENT_W_GALLERYTEXT, Pattern.CASE_INSENSITIVE);
        Matcher gallerytextMatcher;
        final Pattern wikiHrefPattern 
                = Pattern.compile(REGEX_HREF_LIBRIVOX_WIKI_SUBDOMAIN, Pattern.CASE_INSENSITIVE);

        List<String> urlStrings = new ArrayList<>();
        Catalog catalog = new Catalog();
        String webpageContent;
        String itemTitle;
        int liElementCounter = 0;
        int galleryboxCounter = 0;
        int badCatalogLinkCounter = 0;
        int audiobooksProcessedCount = 0;
        
        System.out.println("=============================");
        System.out.println("Stage 4 processing initiated. "
                            + new Timestamp(System.currentTimeMillis()));
        System.out.println("=============================");

        /** Get URLs to LibriVox wiki pages with M4B links */
        if (urlStringsFilePath == null || urlStringsFilePath.isEmpty()) {
            try(BufferedReader br 
                    = new BufferedReader
                            (new InputStreamReader
                                (CatalogAssembler.class.getResourceAsStream
                                                (LV_WIKI_M4B_URL_STRINGS)))) {
                String line;
                while ((line = br.readLine()) != null) { 
                    urlStrings.add(line);
                }  
            }
        } else {
            try(BufferedReader br 
                    = new BufferedReader(new FileReader(urlStringsFilePath))) {
                String line;
                while ((line = br.readLine()) != null) { 
                    urlStrings.add(line);
                }  
            }
        }

        for (String urlString : urlStrings) {
            if (Thread.interrupted()) { throw new InterruptedException(); }
            webpageContent = CatalogAssembler.getHttpContent(urlString);

            /** Search html content for LibriVox Catalog for each item's 
             * (1) URL to main LibriVox webpage, and (2) URL(s) to M4B file(s).
             */
            liElementMatcher = liElementPattern.matcher(webpageContent);
            while (liElementMatcher.find()) {
                liElementCounter++;
                if (galleryboxPattern.matcher(liElementMatcher.group()).find()) {
                    galleryboxCounter++;
                } else {
                    continue;
                }

                /** get section with pertinent URLs */
                lvCatalogHrefMatcher =
                    lvCatalogHrefPattern.matcher(liElementMatcher.group());
                if (lvCatalogHrefMatcher.find()) {
                    /** get URL to LV Catalog */
                    String lvCatalogUrlString = lvCatalogHrefMatcher.group()
                            .substring(6, (lvCatalogHrefMatcher.group().length() - 1));
                    /** get URLs to m4b files */
                    List<String> m4bUrlStrings = new ArrayList<>();
                    m4bHrefMatcher = m4bHrefPattern.matcher(liElementMatcher.group());
                    while (m4bHrefMatcher.find()) {
                        m4bUrlStrings.add(m4bHrefMatcher.group()
                            .substring(6, (m4bHrefMatcher.group().length() - 1)));
                    }
                    catalog.addAudiobook
                        (lvCatalogUrlString, m4bUrlStrings);
                    audiobooksProcessedCount++;
                    if (audiobooksProcessedCount % 100 == 0) {
                        System.out.println(audiobooksProcessedCount 
                                + " audiobooks processed so far. "
                                + new Timestamp(System.currentTimeMillis()));
                    }
                } else if (showBadLinks) {
                    /** get item's title */
                    titleMatcher = titlePattern.matcher
                                        (liElementMatcher.group());
                    if (titleMatcher.find()) {
                        itemTitle = titleMatcher.group().substring
                                    (3, titleMatcher.group().length() - 1);
                    } 

                    gallerytextMatcher = gallerytextPattern.matcher
                                            (liElementMatcher.group());
                    if (gallerytextMatcher.find()) {
                        badCatalogLinkCounter++;
                        //System.out.println("TITLE: " + itemTitle);
                        /** output bad links */
                        hrefMatcher =
                            HREF_PATTERN.matcher(liElementMatcher.group());
                        boolean badLinkFound = false;
                        while (hrefMatcher.find()) {
                            if (!m4bHrefPattern.matcher(hrefMatcher.group()).find()
                                    && !wikiHrefPattern.matcher
                                                (hrefMatcher.group()).find())
                            {
                                badLinkFound = true;
                            }
                        }
                    }
                }
            }
        }
        System.out.println("=============================");
        System.out.println("Stage 4 processing completed. " 
                                + new Timestamp(System.currentTimeMillis()));
        System.out.println("=============================");
        System.out.println(audiobooksProcessedCount + " audiobooks processed.");
        if (showBadLinks) {
            System.out.println("Found " + badCatalogLinkCounter 
                    + " galleryboxes w/ BAD or NONEXISTENT catalog links.");
        }
        return catalog;
    }
        
    /* v1.4.3 bypass/deprecate Stage 5 processing; go straight from stage 4 to 6 */
    /** STAGE 6: Incorporate M4B metadata from Stage 4 into catalog from Stage 3.
     * @param stage3Catalog
     * @param stage4Catalog
     * @return
     * @throws java.lang.InterruptedException  */
    protected static Catalog assembleCatalogStage06 
                    (Catalog stage3Catalog, Catalog stage4Catalog) 
                        throws InterruptedException {
        
        System.out.println("=============================");
        System.out.println("Stage 6 processing initiated. "
                            + new Timestamp(System.currentTimeMillis()));
        System.out.println("=============================");

        int audiobooksProcessedCount = 0;
        int goodRecordCount = 0;
        int badRecordCount = 0;
        int missingFromStage3Count = 0;
        int noLibrivoxUrlCount = 0;
        Map<String,Audiobook> stage3AudiobooksUrlMap = new TreeMap<>();
        for (Audiobook audiobook : stage3Catalog.audiobooks) {
            if (Thread.interrupted()) { throw new InterruptedException(); }
            // note that audiobooks w/o LV url are here removed from processing
            if (audiobook.getUrlLibrivox() == null
                    || audiobook.getUrlLibrivox().isEmpty()) {
                noLibrivoxUrlCount++;
                continue;
            }
            stage3AudiobooksUrlMap.put
                    (Catalog.getUrlKey(audiobook.getUrlLibrivox()), audiobook);
        }

        for (Audiobook stage4Audiobook : stage4Catalog.audiobooks) {
            audiobooksProcessedCount++;
            if (stage4Audiobook.getUrlLibrivox() == null
                    || stage4Audiobook.getUrlLibrivox().isEmpty()
                    || stage4Audiobook.getUrlM4bFiles() == null
                    || stage4Audiobook.getUrlM4bFiles().isEmpty()) {
                badRecordCount++;
            } else {
                goodRecordCount++;
                Audiobook stage3Audiobook
                    = stage3AudiobooksUrlMap.get
                        (Catalog.getUrlKey(stage4Audiobook.getUrlLibrivox()));

                if (stage3Audiobook != null) {
                    if (stage3Audiobook.getUrlM4bFiles() == null
                            || stage3Audiobook.getUrlM4bFiles().isEmpty()) {
                        stage3Audiobook.setUrlM4bFiles
                                        (stage4Audiobook.getUrlM4bFiles());
                    }
                } else {
                    missingFromStage3Count++;
                    System.out.println(">>> Audiobook with URL = " 
                            + stage4Audiobook.getUrlLibrivox() 
                            + " not found in Stage 3 catalog.");
                }
            }
        }
        stage3Catalog.audiobooks 
                = new ArrayList<>(stage3AudiobooksUrlMap.values());
        System.out.println("=============================");
        System.out.println("Stage 6 processing completed. "
                                + new Timestamp(System.currentTimeMillis()));
        System.out.println(audiobooksProcessedCount + " stage 4 records processed.");
        System.out.println(goodRecordCount 
                + " records with M4B metadata and LibriVox URL.");
        System.out.println(badRecordCount 
                + " records without M4B metadata and/or LibriVox URL.");
        System.out.println(missingFromStage3Count + " records from stage 4 " 
                + "processing not found in master (stage 3) catalog.");
        System.out.println("*****");
        System.out.println(noLibrivoxUrlCount
                + " audiobooks without LibriVox URL removed from processing.");
        System.out.println(stage3Catalog.audiobooks.size() 
                + " audiobooks total now in master catalog.");
        
        return stage3Catalog;
    }

    /** STAGE 7: Remove from catalog all audiobooks that are missing
     * vital metadata.
     * @param catalog
     * @return
     * @throws java.lang.InterruptedException  */
    protected static Catalog assembleCatalogStage07 (Catalog catalog)
            throws InterruptedException {
        System.out.println("=============================");
        System.out.println("Stage 7 processing initiated. "
                            + new Timestamp(System.currentTimeMillis()));
        System.out.println("=============================");
        
        int audiobooksProcessedCount = 0;
        int goodRecordCount = 0;
        int badRecordCount = 0;

        Catalog stage07Catalog = new Catalog();
        for (Audiobook audiobook : catalog.audiobooks) {
            if (Thread.interrupted()) { throw new InterruptedException(); }
            audiobooksProcessedCount++;
            if (audiobook.getUrlLibrivox() == null
                    || audiobook.getUrlLibrivox().isEmpty()
                    || audiobook.getUrlInternetArchive() == null
                    || audiobook.getUrlInternetArchive().isEmpty()) {
            //if (audiobook.getUrlM4bFiles() == null
            //       || audiobook.getUrlM4bFiles().isEmpty()) {
                badRecordCount++;
            } else {
                goodRecordCount++;
                stage07Catalog.audiobooks.add(audiobook);
            }
        }

        System.out.println("=============================");
        System.out.println("Stage 7 processing completed. "
                                + new Timestamp(System.currentTimeMillis()));
        System.out.println(audiobooksProcessedCount + " stage 6 records processed.");
        System.out.println(goodRecordCount 
                + " records with LibriVox or Internet Archive URLs kept in catalog.");
        System.out.println(badRecordCount 
                + " records without LibriVox or Internet Archive URLs removed from catalog.");
        return stage07Catalog;
    }
    
    public static void assembleDeltaCatalog (String previousBuildPath, 
                                                String currentBuildPath,
                                                boolean deltaSuppressCoverArtChange) 
            throws JAXBException, InterruptedException {
        System.out.println("===========================");
        System.out.println("Delta processing initiated. "
                            + new Timestamp(System.currentTimeMillis()));
        System.out.println("===========================");
        File previousStage07XmlFile 
                    = Paths.get(previousBuildPath, STAGE07_FILE_NAME).toFile();
        File currentStage07XmlFile 
                    = Paths.get(currentBuildPath, STAGE07_FILE_NAME).toFile();
        File deltaXmlFile 
                    = Paths.get(currentBuildPath, DELTA_FILE_NAME).toFile();
        File deltaXmlFormattedFile 
                = Paths.get(currentBuildPath, DELTA_FORMATTED_FILE_NAME).toFile();
        Catalog previousStage07Catalog 
                = CatalogMarshaller.unmarshalCatalogFromXml(previousStage07XmlFile);
        Catalog currentStage07Catalog
                = CatalogMarshaller.unmarshalCatalogFromXml(currentStage07XmlFile);
        Catalog deltaCatalog = getDeltaCatalog
                                (previousStage07Catalog, currentStage07Catalog,
                                        deltaSuppressCoverArtChange);
        CatalogMarshaller.marshalCatalogToXml(deltaCatalog, deltaXmlFile);
        CatalogMarshaller.marshalCatalogToXml(deltaCatalog, deltaXmlFormattedFile, true);
    }
     
    protected static Catalog getDeltaCatalog (Catalog oldCatalog, 
                        Catalog newCatalog, boolean deltaSuppressCoverArtChange) 
            throws InterruptedException {
        System.out.println("Size of old audiobook array = " + oldCatalog.audiobooks.size());
        System.out.println("Size of new audiobook array = " + newCatalog.audiobooks.size());
        
        Catalog deltaCatalog = new Catalog();
        oldCatalog.sortAudiobooks();
        Audiobook[] oldAudiobookArray = new Audiobook[oldCatalog.audiobooks.size()];
        oldAudiobookArray = oldCatalog.audiobooks.toArray(oldAudiobookArray);
        for (Audiobook newAudiobook : newCatalog.audiobooks) {
            if (Thread.interrupted()) { throw new InterruptedException(); }
            int returnValue = Arrays.binarySearch(oldAudiobookArray, newAudiobook);
            if (returnValue < 0) {
                System.out.println("  New audiobook ID: " + newAudiobook.getId());
                deltaCatalog.audiobooks.add(newAudiobook);
            } else {
                Audiobook oldAudiobook = oldAudiobookArray[returnValue];
                if (newAudiobook.getUrlCoverArt() != null
                        && !newAudiobook.getUrlCoverArt().isEmpty()) {
                    if (oldAudiobook.getUrlCoverArt() == null
                            || (!deltaSuppressCoverArtChange &&
                                    !newAudiobook.getUrlCoverArt().equals
                                                (oldAudiobook.getUrlCoverArt()))) {
                        System.out.println
                            ("  Changed UrlCoverARt -- audiobook ID: " + newAudiobook.getId());
                        deltaCatalog.audiobooks.add(newAudiobook);
                        continue;
                    }
                }
                // v1.3.4 check to see whether m4b urls may have changed
                if (newAudiobook.getUrlM4bFiles() != null
                        && !newAudiobook.getUrlM4bFiles().isEmpty()) {
                    if (oldAudiobook.getUrlM4bFiles() == null) {
                        System.out.println
                            ("  Changed UrlM4bs -- audiobook ID: " + newAudiobook.getId());
                        deltaCatalog.audiobooks.add(newAudiobook);
                    } else if (newAudiobook.getUrlM4bFiles().size()
                                    != oldAudiobook.getUrlM4bFiles().size()) {
                        System.out.println
                            ("  Changed UrlM4bs -- audiobook ID: " + newAudiobook.getId());
                        deltaCatalog.audiobooks.add(newAudiobook);
                    } else {
                        int i = 0;
                        for (String oldUrlM4bFile : oldAudiobook.getUrlM4bFiles()) {
                            if (oldUrlM4bFile.equals
                                    (newAudiobook.getUrlM4bFiles().get(i++))) {
                                continue;
                            } else {
                                System.out.println
                                    ("  Changed UrlM4bs -- audiobook ID: " 
                                                    + newAudiobook.getId());
                                deltaCatalog.audiobooks.add(newAudiobook);
                                break;
                            }
                        }
                    }
                }
            }
        }
        System.out.println("===========================");
        System.out.println("Delta processing completed. "
                                + new Timestamp(System.currentTimeMillis()));
        System.out.println(deltaCatalog.audiobooks.size() + " new/modified audiobooks found.");
        
        return deltaCatalog;
    }
    
    /** Uses standard JSON parsing to extract Audiobook metadata from Internet 
     * Archive. */
    private static Audiobook extractAudiobookMetadataFromIA (String urlString) 
            throws IOException, ParseException {
        Audiobook audiobook = new Audiobook();
        final String JSON_FORMAT_REQUEST = "&output=json";
        final String JSON_KEYNAME_description = "description";
        final String JSON_KEYNAME_identifier = "identifier";
        final String JSON_KEYNAME_publicdate = "publicdate";
        final String JSON_KEYNAME_downloads = "downloads";
        // final String JSON_KEYNAME_subject = "subject"; // aborted enhancement
        final String JSON_KEYNAME_SUFFIX_mp3 = ".mp3";
        final String JSON_KEYNAME_format = "format";
        final String JSON_KEYNAME_metadata = "metadata";
        final String JSON_KEYNAME_title = "title";
        final String JSON_VALUE_STRING_JPEG = "JPEG";
        final String JSON_VALUE_STRING_JPEG_thumb = "JPEG Thumb";
        final String JSON_KEYNAME_source = "source";
        final String JSON_VALUE_STRING_original = "original";
        final String ARCHIVE_URL_DOWNLOAD_PREFIX = "http://archive.org/download/";
        /* // aborted enhancement
        final List<String> REJECTED_IA_KEYWORDS 
                = Arrays.asList("librivox","audiobook","audiobooks",
                        "audio book","audio books");
        */
        
        String archiveItemIdentifier = "";
        String currentFile = "";
        String bigJpegUrlSuffixString = "";
        String thumbnailJpegUrlSuffixString = "";
        List<String> mp3Urls = new ArrayList<>();
        List<String> m4bUrls = new ArrayList<>();
        String currentKeyName = "";
        boolean inMetadataObject = false;
        
        String webpageContent = getHttpContent(urlString + JSON_FORMAT_REQUEST);
        JsonParser parser = Json.createParser(new StringReader(webpageContent));
        
        while (parser.hasNext()) {
            switch(parser.next()) {
                case KEY_NAME:
                    currentKeyName = parser.getString();
                    if (currentKeyName.endsWith(JSON_KEYNAME_SUFFIX_jpg) 
                            || currentKeyName.endsWith(JSON_KEYNAME_SUFFIX_mp3)
                            || currentKeyName.endsWith(JSON_KEYNAME_SUFFIX_m4b)) {
                        currentFile = currentKeyName;
                    }
                    if (currentKeyName.equals(JSON_KEYNAME_metadata)) {
                        inMetadataObject = true;
                    }
                    break;
                case END_OBJECT:
                    currentFile = "";
                    inMetadataObject = false;
                    break;
                case VALUE_NUMBER:
                    if (currentKeyName.equals(JSON_KEYNAME_downloads)) {
                        audiobook.setDownloadCountInternetArchive(parser.getInt());
                    }
                    break;
                case VALUE_STRING:
                    switch (currentKeyName) {
                        case JSON_KEYNAME_title:
                            if (inMetadataObject) {
                                audiobook.setDisplayTitle(parser.getString());
                            }
                            break;
                        // some older IA pages list M4B files in description
                        case JSON_KEYNAME_description:
                            List<String> urlM4bFiles 
                                = getM4bUrlsFromArchiveDescription(parser.getString());
                            if (urlM4bFiles != null && !urlM4bFiles.isEmpty()) {
                                audiobook.setUrlM4bFiles(urlM4bFiles);
                            }
                            break;
                        case JSON_KEYNAME_identifier:
                            archiveItemIdentifier = parser.getString();
                            break;
                        case JSON_KEYNAME_publicdate:
                            audiobook.setPublicationDateInternetArchive
                                                        (parser.getString());
                            break;
                        case JSON_KEYNAME_format:
                            if (!currentFile.isEmpty()) {
                                switch (parser.getString()) {
                                    case JSON_VALUE_STRING_JPEG: 
                                        bigJpegUrlSuffixString = currentFile;
                                        currentFile = "";
                                        break;
                                    case JSON_VALUE_STRING_JPEG_thumb:
                                        thumbnailJpegUrlSuffixString = currentFile;
                                        currentFile = "";
                                        break;
                                }
                            }
                            break;
                        case JSON_KEYNAME_source:
                            if (!currentFile.isEmpty() 
                                    && parser.getString().equals
                                                (JSON_VALUE_STRING_original)) {
                                if (currentFile.endsWith
                                                (JSON_KEYNAME_SUFFIX_mp3)) {
                                    mp3Urls.add(currentFile);
                                } else if (currentFile.endsWith
                                                (JSON_KEYNAME_SUFFIX_m4b)) {
                                    m4bUrls.add(currentFile);
                                }  
                                currentFile = "";
                            }
                            break;
                        /*
                        case JSON_KEYNAME_subject:  // aborted enhancement
                            List<String> keywordStrings
                                = Arrays.asList(parser.getString().split(";"));
                            List<Keyword> keywords = new ArrayList<>();
                            for (String keywordString : keywordStrings) {
                                keywordString = keywordString.trim().toLowerCase();
                                if (!REJECTED_IA_KEYWORDS.contains(keywordString)) {
                                    keywords.add(new Keyword(keywordString));
                                }
                            }
                            audiobook.setKeywords(keywords);
                            break;
                        */
                    }
                    break;
            }
        }
        
        List<Section> sections = new ArrayList<>();
        if (!archiveItemIdentifier.isEmpty()) {
            if (!bigJpegUrlSuffixString.isEmpty()) {
                audiobook.setUrlCoverArt(ARCHIVE_URL_DOWNLOAD_PREFIX 
                        + archiveItemIdentifier + bigJpegUrlSuffixString);
            }
            if (!thumbnailJpegUrlSuffixString.isEmpty()) {
                audiobook.setUrlThumbnail(ARCHIVE_URL_DOWNLOAD_PREFIX 
                        + archiveItemIdentifier + thumbnailJpegUrlSuffixString);
            }
            if (!mp3Urls.isEmpty()) {
                for (String mp3Url : mp3Urls) {
                    Section section = new Section();
                    section.setUrlForListening(ARCHIVE_URL_DOWNLOAD_PREFIX 
                                            + archiveItemIdentifier + mp3Url);
                    sections.add(section);
                }
            }
        }
        audiobook.setSections(sections);
        // added v1.5.1 - new convention for m4b metadata in IA
        if (!m4bUrls.isEmpty()) {
            List<String> m4bUrlsComplete = new ArrayList<>();
            for (String m4bUrl : m4bUrls) {
                m4bUrlsComplete.add(ARCHIVE_URL_DOWNLOAD_PREFIX 
                                        + archiveItemIdentifier + m4bUrl);
            }
            audiobook.setUrlM4bFiles(m4bUrlsComplete);
        }
        return audiobook;
    }
    
    /* URLs for M4B files may be embedded in the "description" section of 
    * Internet Archive webpages for each item. As of 2014-11, LibriVox 
    * volunteer admins are migrating ALL m4b links to these locations, but
    * this is a work in progress. */
    private static List<String> getM4bUrlsFromArchiveDescription (String archiveDescription) {
        List<String> urlM4bFiles = new ArrayList<>();
        Matcher aTagMatcher = A_TAG_PATTERN.matcher(archiveDescription);
        aTagMatch:
        while (aTagMatcher.find()) {
            hrefMatcher = HREF_PATTERN.matcher(aTagMatcher.group());
            if (!hrefMatcher.find()) {
                continue;
            }
            String urlStringCandidate = hrefMatcher.group().substring
                                        (6, hrefMatcher.group().length() - 1);
            if (urlStringCandidate.endsWith(JSON_KEYNAME_SUFFIX_m4b)) {
                urlM4bFiles.add(urlStringCandidate.replace("https", "http"));
            }
        }
        if (urlM4bFiles.isEmpty()) {
            return null;
        } else {
            return urlM4bFiles;
        }
    }
    
    /** Uses regular-expression parsing to extract metadata from LibriVox pages. */
    private static List<Section> extractAuthorAndUrlTextMetadata (String urlString)
            throws IOException, MetadataExtractionException, JAXBException, 
                RemoteApiProcessingException, IllegalAccessException {
        final String REGEX_TABLE_ELEMENT = "\\<table(\\>| ).*?\\</table\\>";
        /** used to search inside "table" element for attribute 'class="chapter-download"' */
        final String REGEX_CHAPTER_DOWNLOAD = "class=\"chapter-download\"";
        /** used to search inside "table" element for "thead" element */
        final String REGEX_THEAD_ELEMENT = "\\<thead(\\>| ).*?\\</thead\\>";
        /** used to search inside "thead" element for "th" element */
        final String REGEX_TH_ELEMENT = "\\<th(\\>| ).*?\\</th\\>";
        /** used to search inside "table" element for "tbody" element */
        final String REGEX_TBODY_ELEMENT = "\\<tbody(\\>| ).*?\\</tbody\\>";
        /** used to search inside "tbody" element for "tr" element */
        final String REGEX_TR_ELEMENT = "\\<tr(\\>| ).*?\\</tr\\>";
        /** used to search inside "tr" element for "td" element */
        final String REGEX_TD_ELEMENT = "\\<td(\\>| ).*?\\</td\\>";
        final String REGEX_AUTHOR_ID_STRING = "author/.*";
        
        final Pattern tableElementPattern
                = Pattern.compile(REGEX_TABLE_ELEMENT, Pattern.CASE_INSENSITIVE);
        Matcher tableElementMatcher;
        final Pattern chapterDownloadPattern
            = Pattern.compile(REGEX_CHAPTER_DOWNLOAD, Pattern.CASE_INSENSITIVE);
        Matcher chapterDownloadMatcher;
        final Pattern theadElementPattern
            = Pattern.compile(REGEX_THEAD_ELEMENT, Pattern.CASE_INSENSITIVE);
        Matcher theadElementMatcher;
        final Pattern thElementPattern
            = Pattern.compile(REGEX_TH_ELEMENT, Pattern.CASE_INSENSITIVE);
        Matcher thElementMatcher;
        final Pattern tbodyElementPattern
            = Pattern.compile(REGEX_TBODY_ELEMENT, Pattern.CASE_INSENSITIVE);
        Matcher tbodyElementMatcher;
        final Pattern trElementPattern
            = Pattern.compile(REGEX_TR_ELEMENT, Pattern.CASE_INSENSITIVE);
        Matcher trElementMatcher;
        final Pattern tdElementPattern
            = Pattern.compile(REGEX_TD_ELEMENT, Pattern.CASE_INSENSITIVE);
        Matcher tdElementMatcher;
        final Pattern authorIdStringPattern
            = Pattern.compile(REGEX_AUTHOR_ID_STRING, Pattern.CASE_INSENSITIVE);
        Matcher authorIdStringMatcher;
        
        final String AUTHOR_LABEL = "Author"; 
        final String SOURCE_LABEL = "Source"; 
        int authorPositionInTable = 0;
        int sourcePositionInTable = 0;
        boolean authorPositionFound = false;
        boolean sourcePositionFound = false;
        
        List<Section> sectionsWithAuthorAndUrlTextMetadata = new ArrayList<>();
        String webpageContent = getHttpContent(urlString);

        tableElementMatcher = tableElementPattern.matcher(webpageContent);
        while (tableElementMatcher.find()) {
            chapterDownloadMatcher 
                    = chapterDownloadPattern.matcher(tableElementMatcher.group());
            if (!chapterDownloadMatcher.find()) {
                continue;
            }
            theadElementMatcher 
                    = theadElementPattern.matcher(tableElementMatcher.group());
            if (theadElementMatcher.find()) {
                trElementMatcher 
                        = trElementPattern.matcher(theadElementMatcher.group());
                while (trElementMatcher.find()) {
                    thElementMatcher 
                            = thElementPattern.matcher(trElementMatcher.group());
                    int thElementCount = 0;
                    while (thElementMatcher.find()) {
                        thElementCount++;
                        String thContent = thElementMatcher.group().substring
                                    (4, thElementMatcher.group().length() - 5);
                        if (thContent.equalsIgnoreCase(AUTHOR_LABEL)) {
                            authorPositionFound = true;
                            authorPositionInTable = thElementCount;
                        } else if (thContent.equalsIgnoreCase(SOURCE_LABEL)) {
                            sourcePositionFound = true;
                            sourcePositionInTable = thElementCount;
                        }
                    }
                }
            }
            if (!authorPositionFound && !sourcePositionFound) {
                throw new MetadataExtractionException
                    ("Position of neither author nor section metadata could be" 
                            + " determined on LV Catalog webpage.");
            }
            tbodyElementMatcher 
                    = tbodyElementPattern.matcher(tableElementMatcher.group());
            if (tbodyElementMatcher.find()) {
                trElementMatcher 
                        = trElementPattern.matcher(tbodyElementMatcher.group());
                while (trElementMatcher.find()) {
                    Section newSection = new Section();
                    sectionsWithAuthorAndUrlTextMetadata.add(newSection);
                    tdElementMatcher 
                            = tdElementPattern.matcher(trElementMatcher.group());
                    int tdElementCount = 0;
                    while (tdElementMatcher.find()) {
                        tdElementCount++;
                        if (authorPositionFound 
                                && tdElementCount == authorPositionInTable) {
                            Author newAuthor = new Author();
                            List<Author> newAuthors = new ArrayList<>();
                            newAuthors.add(newAuthor);
                            newSection.setAuthors(newAuthors);
                            hrefMatcher =
                                HREF_PATTERN.matcher(tdElementMatcher.group());
                            if (hrefMatcher.find()) {
                                authorIdStringMatcher =
                                    authorIdStringPattern.matcher
                                                        (hrefMatcher.group());
                                if (authorIdStringMatcher.find()) {
                                    String authorId 
                                        = authorIdStringMatcher.group().substring
                                        (7, authorIdStringMatcher.group().length() - 1);
                                    newAuthor.setId(authorId);
                                    Author authorViaAPI 
                                            = AuthorJaxbAdapter.getAuthorViaApi
                                                                    (authorId);
                                    newAuthor.merge(authorViaAPI);
                                }
                            }
                        } else if (sourcePositionFound 
                                && tdElementCount == sourcePositionInTable) {
                            hrefMatcher =
                                HREF_PATTERN.matcher(tdElementMatcher.group());
                            if (hrefMatcher.find()) {
                                newSection.setUrlTextSource
                                    (hrefMatcher.group().substring
                                        (6, hrefMatcher.group().length() - 1));
                            }
                        }
                    }
                }
            }
        }
        return sectionsWithAuthorAndUrlTextMetadata;
    }
    
    /** Uses regular-expression parsing to extract metadata from LibriVox pages. */
    private static Audiobook extractCoverArtUrls (String urlString)
            throws IOException, MetadataExtractionException, JAXBException, 
                RemoteApiProcessingException, IllegalAccessException {
        Audiobook audiobookFragment = new Audiobook();
        String webpageContent = getHttpContent(urlString);

        Matcher aTagMatcher = A_TAG_PATTERN.matcher(webpageContent);
        aTagMatch:
        while (aTagMatcher.find()) {
            Matcher downloadCoverMatcher 
                    = DOWNLOAD_COVER_PATTERN.matcher(aTagMatcher.group());
            while (downloadCoverMatcher.find()) {
                hrefMatcher = HREF_PATTERN.matcher(downloadCoverMatcher.group());
                if (!hrefMatcher.find()) {
                    continue;
                }
                String urlStringCandidate = hrefMatcher.group().substring
                                            (6, hrefMatcher.group().length() - 1);
                if (urlStringCandidate.endsWith(JSON_KEYNAME_SUFFIX_jpg)) {
                    audiobookFragment.setUrlCoverArt
                            (urlStringCandidate.replace("https", "http"));
                    break aTagMatch;
                }
            }
        }
        return audiobookFragment;
    }
    
    public static void printMissingIdReport (Catalog catalog) {
        int audiobooksProcessedCount = 0;
        int audiobooksWithMissingId = 0;
        Catalog missingIdCatalog = new Catalog();
        
        for (Audiobook audiobook : catalog.audiobooks) {
            audiobooksProcessedCount++;
            if (audiobook.getId() == null || audiobook.getId().isEmpty()) {
                missingIdCatalog.audiobooks.add(audiobook);
                audiobooksWithMissingId++;
            }
        }
        System.out.println("=======================================");
        System.out.println("CATALOG OF AUDIOBOOKS MISSING UNIQUE ID");
        System.out.println("=======================================");
        System.out.println(missingIdCatalog);
        System.out.println("============================");
        System.out.println("Report processing completed.");
        System.out.println(audiobooksProcessedCount + " audiobooks processed.");
        System.out.println(audiobooksWithMissingId 
                + " audiobooks missing unique ID.");
        
    }
    
    protected static String getHttpContent (String urlString)
            throws IOException {
        URL url = new URL(urlString);
        return getHttpContent(url);
    }
    
    protected static String getHttpContent (URL url)
            throws IOException {
        StringBuilder httpContent = new StringBuilder();
        String line;
        HttpURLConnection  connection;
        boolean redirect;
        int redirectCount = 0;
        do {
            redirect = false;
            connection = (HttpURLConnection) url.openConnection();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP
                    || connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM) {
                redirect = true;
                redirectCount++;
                url = new URL(connection.getHeaderField("Location"));
            }
        } while (redirect && (redirectCount < 5) ); // no more than 4 redirects
        try ( BufferedReader htmlReader 
            = new BufferedReader
              (new InputStreamReader(connection.getInputStream()));
            )
        {
            while ((line = htmlReader.readLine()) != null) {
                httpContent.append(line);
            }
        }
        return httpContent.toString();
    }

    /** After JPEG downloading is completed, all JPEGs in the target folder
     * must be copied into a "newJpegsReduced" folder and reduced using 
     * Microsoft Office Picture Manager "compress pictures" function to reduce 
     * them to "e-mail" format; then the full images should be copied into the 
     * folder denoted by permanentJegPath, and the reduced images should be 
     * copied into the permanent "reduced/coverArt" folder. 
     * Then, the complete "reduced/coverArt" folder should replace
     * the "coverArt" folder in the NetBeans project, followed by a rebuild 
     * (with SM incremented).
     *
     * @param currentBuildPath
     * @param permanentJpegPath
     * @param newJpegPath
     * @throws JAXBException
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public static void assembleNewJpegs (String currentBuildPath,
                                            String permanentJpegPath,
                                            String newJpegPath)  
                throws JAXBException, IOException, InterruptedException {
        File stage06XmlFile 
                    = Paths.get(currentBuildPath, STAGE06_FILE_NAME).toFile();
        Catalog stage06Catalog
                = CatalogMarshaller.unmarshalCatalogFromXml(stage06XmlFile);
        downloadJpegs(stage06Catalog, permanentJpegPath, newJpegPath);
        
    }
    
    
    /**
     *
     * @param catalog
     * @param permanentJpegPath
     * @param newJpegPath
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public static void downloadJpegs 
            (Catalog catalog, String permanentJpegPath, String newJpegPath) 
                    throws IOException, InterruptedException {
        if (newJpegPath == null || newJpegPath.isEmpty()) {
            newJpegPath = permanentJpegPath;
        } else {
            if (!Files.exists(Paths.get(newJpegPath))) {
                new File(newJpegPath).mkdir();
            }
        }
        System.out.println( "=============================\n"
                          + "DOWNLOAD OF JPEGs COMMENCING.\n"
                          + "=============================\n");
        int noUrlCount = 0;
        int downloadCount = 0;
        int previouslyDownloadedCount = 0;
        int invalidUrlCount = 0;
        for (Audiobook audiobook : catalog.audiobooks) {
            if (Thread.interrupted()) { throw new InterruptedException(); }
            if (audiobook.getUrlCoverArt() == null 
                        || audiobook.getUrlCoverArt().isEmpty()) {
                noUrlCount++;
                continue;
            }
            String coverArtFileName = new File(audiobook.getUrlCoverArt()).getName();
            if (Files.exists(Paths.get(permanentJpegPath, coverArtFileName))) {
                //System.out.println
                //        ("File already downloaded: " + coverArtFileName);
                previouslyDownloadedCount++;
                continue;
            }
            try {
                // Files.copy(source, target, replaceOption);
                Files.copy
                    (new URL(audiobook.getUrlCoverArt()).openStream(), 
                        Paths.get(newJpegPath, coverArtFileName),
                        StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Downloaded jpeg: " + coverArtFileName);
                downloadCount++;
            } catch (FileNotFoundException e) {
                System.out.println("Downloaded jpeg: FILE NOT FOUND at URL -- "
                            + audiobook.getUrlCoverArt());
                invalidUrlCount++;
            }
        }
        System.out.println("=============================================\n"
                + "DOWNLOAD OF JPEGs COMPLETED.\n"
                + downloadCount + " downloaded.\n"
                + previouslyDownloadedCount + " previously downloaded.\n"
                + noUrlCount + " audiobooks with NO jpeg URL.\n"
                + invalidUrlCount + " audiobooks with INVALID jpeg URL.");
    }
                
    public static void downloadJpegThumbnails 
                (Catalog catalog, String jpegDownloadDirectory) 
                    throws IOException {
        int noUrlCount = 0;
        int downloadCount = 0;
        int previouslyDownloadedCount = 0;
        int invalidUrlCount = 0;
        for (Audiobook audiobook : catalog.m4bAudiobooks) {
            if (audiobook.getUrlThumbnail() == null 
                        || audiobook.getUrlThumbnail().isEmpty()) {
                noUrlCount++;
                continue;
            }
            if (Files.exists(Paths.get(jpegDownloadDirectory
                        + new File(audiobook.getUrlThumbnail()).getName()))) {
                System.out.println("File already downloaded: "
                            + new File(audiobook.getUrlThumbnail()).getName());
                previouslyDownloadedCount++;
                continue;
            }
            try {
                // Files.copy(source, target, replaceOption);
                Files.copy
                    (new URL(audiobook.getUrlThumbnail()).openStream(), 
                        Paths.get(jpegDownloadDirectory
                            + new File(audiobook.getUrlThumbnail()).getName()),
                        StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Downloaded jpeg: " 
                        + new File(audiobook.getUrlThumbnail()).getName());
                downloadCount++;
            } catch (FileNotFoundException e) {
                System.out.println("Downloaded jpeg: FILE NOT FOUND at URL -- "
                            + audiobook.getUrlThumbnail());
                invalidUrlCount++;
            }
        }
        System.out.println("=============================================\n"
                + "DOWNLOAD OF JPEGs COMPLETED.\n"
                + downloadCount + " downloaded.\n"
                + previouslyDownloadedCount + " previously downloaded.\n"
                + noUrlCount + " audiobooks with NO thumbnail URL.\n"
                + invalidUrlCount + " audiobooks with INVALID thumbnail URL.");
    }
}

