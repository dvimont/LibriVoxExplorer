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
import java.util.List;
import org.commonvox.indexedcollectionmanager.IndexedCollection;

/**
 *
 * @author dvimont
 */
public class LeCatalog {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        
        final String PROJECTPATH 
                = "C:\\Users\\DanUltra\\Documents\\javaLibriVoxFiles\\";
        final String DEV_PATH             = PROJECTPATH + "dev\\";
        final String BUILD_PATH           = PROJECTPATH + "round9\\"; 
        final String PREVIOUS_BUILD_PATH  = PROJECTPATH + "round6\\";
        final String JPEG_PERMANENT_PATH 
                                = PROJECTPATH + "jpegs\\fullSize\\coverArt\\";
        final String JPEG_NEW_IMAGES_PATH = BUILD_PATH + "newJpegs\\";

        //File stage01XmlFile = new File(BUILD_PATH + "stage01.xml");
        //File stage01XmlFileRestart = new File(BUILD_PATH + "stage01.restart.xml");
        //File stage02XmlFile = new File(BUILD_PATH + "stage02.xml");
        //File stage03XmlFile = new File(DEV_PATH + "stage03.xml");
        //File stage04XmlFile = new File(DEV_PATH + "stage04.xml");
        //File stage05XmlFile = new File(BUILD_PATH + "stage05.xml");
        //File stage06XmlFile = new File(DEV_PATH + "stage06.xml");
        //File previousStage06XmlFile = new File(PREVIOUS_BUILD_PATH + "stage06.xml");
        //File stage06XmlFormattedFile = new File(DEV_PATH + "stage06.formatted.xml");
        //File stage07XmlFile = new File(BUILD_PATH + "stage07.xml");
        //File stage07XmlFormattedFile = new File(BUILD_PATH + "stage07.formatted.xml");
        //File deltaXmlFile = new File(BUILD_PATH + "delta.xml");
        //File deltaXmlFormattedFile = new File(BUILD_PATH + "delta.formatted.xml");
        //File testFile = new File(BUILD_PATH + "test.xml");
        //String serializedCatalog = BUILD_PATH + "catalog.ser";
        //Catalog stage01Catalog;
        //Catalog stage01CatalogRestart;
        //Catalog stage02Catalog;
        //Catalog stage03Catalog;
        //Catalog stage04Catalog;
        //Catalog stage05Catalog;
        //Catalog stage06Catalog;
        //Catalog previousStage06Catalog;
        //Catalog stage07Catalog;
        //Catalog deltaCatalog;
        
        /** COMPLETE PROCESSING */
        //CatalogAssembler.assembleCompleteCatalog(BUILD_PATH, 1, 0);
        //CatalogAssembler.assembleCompleteCatalog(DEV_PATH, 5986, 1);
        
        /** DELTA PROCESSING */
        //CatalogAssembler.assembleDeltaCatalog(PREVIOUS_BUILD_PATH, BUILD_PATH);
        
        /** JPEG PROCESSING */
        CatalogAssembler.assembleNewJpegs
                        (BUILD_PATH, JPEG_PERMANENT_PATH, JPEG_NEW_IMAGES_PATH);
        
        /** GENERATE NO-GENRE REPORT */
        //Catalog catalog = CatalogMarshaller.unmarshalCatalogFromXml();
        //catalog.bootUp(null);
        //catalog.generateNoGenreReportCSV(PROJECTPATH + "noGenres.csv");
        
        /** STAGE 1 PROCESSING: Use regular expression processing to extract
         * URL metadata from webpages on the LibriVox wiki site. 
         * For each audiobook, extract:
         * (1) URL to the item's LibriVox catalog page;
         * (2) URLs to the item's M4B file(s).
         */  
        ////stage01Catalog = CatalogMarshaller.unmarshalCatalogFromXml(stage01XmlFileRestart);
        /*
        stage01Catalog = CatalogAssembler.assembleCatalogStage01(null, 8573);
        CatalogMarshaller.marshalCatalogToXml(stage01Catalog, stage01XmlFile);
        */
        
        /** STAGE 2 PROCESSING: For each audiobook with multiple authors, 
         * make call to the audiobook's LibriVox webpage to extract author metadata
         * for each section, as well as URL for text for each section. URLs to
         * cover art must also be retrieved here, since the Internet Archive pages
         * for audiobooks do not necessarily include links to the cover art.
         */
        //stage01Catalog = CatalogMarshaller.unmarshalCatalogFromXml(stage01XmlFile);
        /*
        stage02Catalog = CatalogAssembler.assembleCatalogStage02(stage01Catalog);
        CatalogMarshaller.marshalCatalogToXml(stage02Catalog, stage02XmlFile);
        */
        
        /** STAGE 3 PROCESSING: For each audiobook, find download count and mp3
         * URLs on the audiobook's corresponding Internet Archive webpage, retrieved
         * in JSON format. */
        //stage02Catalog = CatalogMarshaller.unmarshalCatalogFromXml(stage02XmlFile);
        /*
        stage03Catalog = CatalogAssembler.assembleCatalogStage03(stage02Catalog);
        CatalogMarshaller.marshalCatalogToXml(stage03Catalog, stage03XmlFile);
        */
        
        /** STAGE 4 PROCESSING: Use regular expression processing to extract
         * URL metadata from webpages on the LibriVox wiki site. 
         * For each audiobook, extract:
         * (1) URL to the item's LibriVox catalog page;
         * (2) URLs to the item's M4B file(s).
         */  
        /*
        stage04Catalog = CatalogAssembler.assembleCatalogStage04(null);
        CatalogMarshaller.marshalCatalogToXml(stage04Catalog, stage04XmlFile);
        */
        
        /** STAGE 5 PROCESSING: Use regular expression processing to extract
         * audiobook ID from LibriVox webpage for every audiobook processed
         * in Stage 4. 
         */
        //stage04Catalog = CatalogMarshaller.unmarshalCatalogFromXml(stage04XmlFile);
        /*
        stage05Catalog = CatalogAssembler.assembleCatalogStage05(stage04Catalog);
        CatalogMarshaller.marshalCatalogToXml(stage05Catalog, stage05XmlFile);
        */
        
        /** Customized post-Stage-5 processing to clean up dirty data. */
        /*
        String dirtyUrl = "http://librivox.orga/the-tiger-of-mysore-by-g-a-henty/";
        String correctedUrl = "http://librivox.org/the-tiger-of-mysore-by-g-a-henty/";
        String correctedId = "4522";
        stage05Catalog = CatalogAssembler.unmarshalCatalogFromXml(stage05XmlFile);
        for (Audiobook audiobook : stage05Catalog.audiobooks) {
            if (audiobook.getUrlLibrivox().equals(dirtyUrl)) {
                audiobook.setUrlLibrivox(correctedUrl);
                audiobook.setId(correctedId);
                System.out.println("found it and fixed it");
            }
        }
        CatalogAssembler.marshalCatalogToXml(stage05Catalog, stage05XmlFileCorrected);
        */
        
        /** STAGE 6 PROCESSING: Merge M4B metadata from Stage 5 into master
         * catalog from Stage 3. This stage completes metadata acquisition. */
        //stage03Catalog = CatalogMarshaller.unmarshalCatalogFromXml(stage03XmlFile);
        //stage04Catalog = CatalogMarshaller.unmarshalCatalogFromXml(stage04XmlFile);
        ///*
        //stage06Catalog = CatalogAssembler.assembleCatalogStage06
        //                                    (stage03Catalog, stage04Catalog);
        //CatalogMarshaller.marshalCatalogToXml(stage06Catalog, stage06XmlFile);
        //CatalogMarshaller.marshalCatalogToXml(stage06Catalog, stage06XmlFormattedFile, true);
        //*/
        
        /** STAGE 7 PROCESSING: Remove all audiobooks that have no M4B metadata. */
        /*
        stage06Catalog = CatalogMarshaller.unmarshalCatalogFromXml(stage06XmlFile);
        stage07Catalog = CatalogAssembler.assembleCatalogStage07(stage06Catalog);
        CatalogMarshaller.marshalCatalogToXml(stage07Catalog, stage07XmlFile);
        CatalogMarshaller.marshalCatalogToXml(stage07Catalog, stage07XmlFormattedFile, true);
        */

        /** DELTA processing - find new audiobooks since previous round. */
        /*
        previousStage06Catalog 
                = CatalogMarshaller.unmarshalCatalogFromXml(previousStage06XmlFile);
        stage07Catalog
                = CatalogMarshaller.unmarshalCatalogFromXml(stage07XmlFile);
        deltaCatalog
                = CatalogAssembler.getDeltaCatalog
                            (previousStage06Catalog, stage07Catalog);
        CatalogMarshaller.marshalCatalogToXml(deltaCatalog, deltaXmlFile);
        CatalogMarshaller.marshalCatalogToXml(deltaCatalog, deltaXmlFormattedFile, true);
        */
        
        /** JPEG processing -- download JPEG files not already downloaded */
        /*
        stage06Catalog = CatalogMarshaller.unmarshalAndBootupCatalog(stage06XmlFile);
        CatalogAssembler.downloadJpegs
            (stage06Catalog, jpegDownloadDirectory, jpegDownloadNew);
        */
        
        /** BOOTUP PROCESSING: Consolidation and indexing of catalog objects. 
         */
        ////stage06Catalog = CatalogMarshaller.unmarshalAndBootupCatalog(stage06XmlFile);
        //CatalogManager.marshalCatalogToXml
        //    (stage06Catalog, stage06XmlFormattedFile, true);
        //stage06Catalog.printIndex(Work.class);
        //stage06Catalog.printWorksByTitle();
        
        //Request testRequest = new Request(Request.RequestFor.WORKS,
        //        23, Request.OrderBy.TITLE, "tr");
        
        /*
        Request testRequest 
                = new Request(Work.class, 23, Request.OrderBy.GENRE, "tr");
        int itemCount = 0;
        Response<Work> testResponse;
        do {
            testResponse = stage06Catalog.getWorks(testRequest);
            for (Work work : testResponse.getResponseList()) {
                System.out.println(work);
                itemCount++;
            }
        } while (!testResponse.atEndOfList());
        System.out.println("\nItem count: " + itemCount + "\n");
        */
        
        // Request attributeRequest = new Request(Genre.class, 0, "B");
        /*
        Request attributeRequest = new Request(Genre.class, 23);
        int itemCount = 0;
        Response<Attribute> attributeResponse;
        do {
            attributeResponse = stage06Catalog.getAttributeValues(attributeRequest);
            for (Attribute attribute : attributeResponse.getResponseList()) {
                System.out.println(attribute);
                itemCount++;
                Request workRequest = new Request(Work.class, attribute);
                Response<Work> workResponse 
                        = stage06Catalog.getWorks(workRequest);
                for (Work work : workResponse.getResponseList()) {
                    System.out.println(work);
                }
                break;
                
            }
            break;
        } while (!attributeResponse.atEndOfList());
        System.out.println("\nItem count: " + itemCount + "\n");
        */
        //stage06Catalog.printWorks(Audiobook.class, Author.class);
        //stage06Catalog.dumpMultiKeyMap(Work.class, Language.class);
        //stage06Catalog.printWorks(Audiobook.class, Reader.class, "2519");
        //stage06Catalog.printWorksWithoutSubheadings
        //        (Audiobook.class, Author.class);
        //for (Audiobook audiobook : stage06Catalog.m4bAudiobooks) {
        //    System.out.println("**********\n" + audiobook.toStringVerbose());
            //for (Section section : audiobook.getSections()) {
            //    System.out.println("---\n==>> " + section.toStringVerbose());
            //}
        //}
        //stage06Catalog.printMappedKeyInstances(Author.class);

        
        /*
        List<Attribute> attributeList 
                = stage06Catalog.getAttributeValues(Reader.class);
        for (Attribute attribute : attributeList) {
            System.out.println(attribute + "  <<== attribute value");
            /*
            List<Work> works = stage06Catalog.getWorks(attribute);
            for (Work work : works) {
                System.out.println(work);
            }
            break;
            */
        //}
        
        /*
        Response testResponse2 
                = stage06Catalog.getWorks(testResponse.getNextRequest());
        for (Work work : testResponse2.getWorks()) {
            System.out.println(work);
        }
*/
        /*
        for (Work work : testRequest.getNext()) {
            System.out.println(work);
            //if (testSelector.endOfList()) {
            //}
        }
        */

        
        /*
        stage05Catalog 
                = CatalogAssembler.unmarshalAndBootupCatalog(stage05NoExceptionTextXmlFile);
        stage06Catalog
                = CatalogAssembler.assembleCatalogStage06(stage05Catalog);
        CatalogAssembler.marshalCatalogToXml(stage06Catalog, testFile); // should be identical to stage 5
        */
        

        /** FORMAT xml */
        //stage05Catalog = CatalogAssembler.unmarshalAndBootupCatalog(stage05Rerun2XmlFile);
        //CatalogManager.marshalCatalogToXml
        //            (stage05Catalog, stage05Rerun2XmlFileFormatted, true);
        
        /** Test catalog serialization */
        //stage05Catalog = CatalogAssembler.unmarshalAndBootupCatalog(stage05NoExceptionTextXmlFile);
        //CatalogManager.serializeCatalog(stage05Catalog, serializedCatalog);
        //Catalog deserializedCatalog = CatalogAssembler.deserializeCatalog(serializedCatalog);
        //CatalogManager.marshalCatalogToXml(deserializedCatalog, stage05SerializationTestXmlFile);
        //System.out.println(deserializedCatalog);
        
        /** Create test file */
        //stage03Catalog = CatalogAssembler.unmarshalCatalogFromXml(stage03XmlFile);
        //testCatalog = CatalogAssembler.getTestCatalog(stage03Catalog, 50);
        //CatalogManager.marshalCatalogToXml(testCatalog, testStage03XmlFile);
        
        /** Print "missing IDs" report */
        //stage05Catalog = CatalogAssembler.unmarshalCatalogFromXml(stage05Rerun2XmlFile);
        //CatalogManager.printMissingIdReport(stage05Catalog);
        
        /** Print "audiobooks with multiple authors" report */
        //stage03Catalog = CatalogAssembler.unmarshalCatalogFromXml(stage03XmlFile);
        //CatalogManager.printAudiobooksMultipleAuthors(stage03Catalog);
        
        /*
        for (String fontName : Font.getFontNames()) {
            System.out.println(fontName);
        }
        System.out.println("DEFAULT IS: " + 
        Font.getDefault().getName());
        System.out.println(System.getProperty("file.encoding"));    
        */
        
        /* Persist catalog in ObjectDB database */
        /*
        if (!new File(Catalog.DEFAULT_DB_PATH).exists()) {
            Catalog catalog = CatalogMarshaller.unmarshalCatalogFromXml();
            catalog.buildDatabase(null, null);
        }
        
        EntityManagerFactory dbConnectionFactory 
            = Persistence.createEntityManagerFactory(Catalog.DEFAULT_DB_PATH);
        EntityManager dbConnection = dbConnectionFactory.createEntityManager();
        TypedQuery<Audiobook> query =
            dbConnection.createNamedQuery("Audiobook.getAll", Audiobook.class);
        List<Audiobook> allAudiobooks = query.getResultList();
        */
        //CatalogJPA catalog = new CatalogJPA();
        //MyBookmarks myBookmarks = new MyBookmarks();
        //myBookmarks.dumpContents();
        //Catalog catalog = CatalogMarshaller.unmarshalCatalogFromXml();
        //catalog.bootUp(null);
        //catalog.showMetamodel();
        /*
        List<MappedKey> mappedKeyValueList 
                = catalog.getMappedKeyValueList(Language.class);
        
        for (MappedKey mappedKeyValue : mappedKeyValueList) {
            System.out.println(mappedKeyValue.toString());
        }
        */
        /*
        Genre ancientGenre = new Genre();
        ancientGenre.name = "Animals";
        List<Work> audiobooks 
                = catalog.getWorks(Audiobook.class, ancientGenre, Title.class);
        for (Work audiobook : audiobooks) {
            System.out.println(audiobook.librivoxId + ": " + audiobook.title);
            if (audiobook.getAuthors() == null) {
                System.out.println("  NULL authors!!");
            } else {
                for (Author author : audiobook.getAuthors()) {
                    System.out.println("  Author: " + author.getKeyItem());
                } 
            }
        }
        System.out.println(audiobooks.size() + " audiobooks returned");
        System.out.println("******");
        */
        //System.out.println(catalog.m4bAudiobooks.size() + " audiobooks in database.");
        //Catalog.printMemoryUsage("After garbage collect suggestion", true);
    }
}
