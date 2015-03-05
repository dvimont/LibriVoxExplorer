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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.commonvox.indexedcollection.IndexedCollectionBuildFailureException;
import org.commonvox.indexedcollection.InvalidMultiKeyException;
import org.commonvox.indexedcollection.InvalidIndexedCollectionQueryException;

/**
 *
 * @author Daniel Vimont
 */
public class CatalogMarshaller {
    private static final String CATALOG_XML_RESOURCE = "data/catalog.xml";
    private static final String REMOTE_DATA_PATH
            ="https://dl.dropboxusercontent.com/u/2023253/le_data/";
    public static final String DEFAULT_URL_STRING_NEW_AUDIOBOOKS 
                                    = REMOTE_DATA_PATH + "updates.xml";
    public static final String DEFAULT_URL_STRING_CORRECTIONS 
                                = REMOTE_DATA_PATH + "corrections.xml";
    public static final String DEFAULT_URL_STRING_GOOGLE_API_KEY
                                = REMOTE_DATA_PATH + "googleApiKey.txt";
    public static final String DEFAULT_URL_STRING_LATEST_VERSION
                                = REMOTE_DATA_PATH + "leCurrentVersion.txt";
    public static final String DEFAULT_URL_BITLY_STRING_LATEST_VERSION
                                = "http://bit.ly/1zx7isX";
    
    public static Catalog unmarshalCatalogFromXml() 
            throws JAXBException {
        return unmarshalCatalogFromXml
            (new File(CatalogMarshaller.class
                            .getResource(CATALOG_XML_RESOURCE).getFile()));
    }
    
    public static Catalog unmarshalCatalogFromXml (CatalogCallback callback) 
            throws JAXBException, InterruptedException
                //, MetadataExtractionException
    {
        if (callback != null) {
            callback.updateTaskMessage("Accessing internal Catalog XML file.");
        }
        URL catalogXmlUrl = CatalogMarshaller.class.getResource(CATALOG_XML_RESOURCE);
        if (callback != null) {
            //throw new MetadataExtractionException("Intentionally thrown for test");
            callback.updateTaskMessage("Unmarshalling Catalog from XML.");
        }
        Catalog catalog = CatalogMarshaller.unmarshalCatalogFromXml(catalogXmlUrl);
        if (callback != null) {
            callback.updateTaskMessage("Finished unmarshalling Catalog from XML.");
        }
        if (Thread.interrupted()) {
            return null;
        }
        return catalog;
    }

    public static Catalog unmarshalCatalogFromXml (File file) 
            throws JAXBException {
        Catalog catalog = (Catalog) 
                JAXBContext.newInstance(Catalog.class).createUnmarshaller()
                        .unmarshal(file);
        return catalog;
    }
    
    public static Catalog unmarshalCatalogFromXml (URL url) 
            throws JAXBException {
        return (Catalog) 
                JAXBContext.newInstance(Catalog.class).createUnmarshaller()
                        .unmarshal(url);
    }
    
    public static Catalog unmarshalCatalogFromXml
                        (String urlString, CatalogCallback callback) 
            throws JAXBException, InterruptedException, MalformedURLException {
        String fileName = "";
        if (callback != null) {
            fileName = new File(urlString).getName();
            if (fileName == null) {
                fileName = "";
            }
            callback.updateTaskMessage("Unmarshalling Catalog: " + fileName);
        }
        if (InterruptibleDownloader.getFileSize(urlString) < 0) {
            if (callback != null) {
                callback.updateTaskMessage
                        ("Catalog file " + fileName + " not accessible.");
            }
            return null;
        }
        Catalog catalog 
            = (Catalog)JAXBContext.newInstance(Catalog.class)
                    .createUnmarshaller().unmarshal(new URL(urlString));
        if (Thread.interrupted()) {
            return null;
        }
        return catalog;
    }
    
    public static Catalog unmarshalAndBootupCatalog (File file) 
            throws JAXBException, 
                    InvalidIndexedCollectionQueryException, 
                    IllegalAccessException,
                    InvocationTargetException,
                    InvalidMultiKeyException,
                    IndexedCollectionBuildFailureException,
                    InterruptedException {
        
        Catalog catalog = (Catalog) 
                JAXBContext.newInstance(Catalog.class).createUnmarshaller()
                        .unmarshal(file);
        catalog.bootUp(null);
        return catalog;
    }

    public static Catalog unmarshalAndBootupCatalog (CatalogCallback callback) 
            throws JAXBException, 
                    InvalidIndexedCollectionQueryException, 
                    IllegalAccessException,
                    InvocationTargetException,
                    InvalidMultiKeyException,
                    IndexedCollectionBuildFailureException,
                    InterruptedException {
        if (callback != null) {
            callback.updateTaskMessage("Unmarshalling Catalog from XML.");
        }
        Catalog catalog = CatalogMarshaller.unmarshalCatalogFromXml();
        if (Thread.interrupted()) {
            return null;
        }
        catalog.bootUp(callback);
        return catalog;
    }

    public static Catalog unmarshalCatalogFromXml (String urlString) 
            throws JAXBException, MalformedURLException {
        URL url = new URL(urlString);
        return unmarshalCatalogFromXml(url);
    }

    /*
    public static Catalog unmarshalCatalogFromXml() 
            throws JAXBException,
                    InvalidIndexedCollectionQueryException, 
                    IllegalAccessException,
                    InvocationTargetException,
                    InvalidMultiKeyException,
                    IndexedCollectionBuildFailureException,
                    MalformedURLException {
        return unmarshalCatalogFromXml
            (CatalogMarshaller.class
                            .getResource(CATALOG_XML_RESOURCE).getFile());
    }
    */
    public static void marshalCatalogToXml (Catalog catalog, String filePath) 
            throws JAXBException {
        marshalCatalogToXml(catalog, new File(filePath), false);
    }
    
    public static void marshalCatalogToXml (Catalog catalog, String filePath, boolean formatted) 
            throws JAXBException {
        marshalCatalogToXml(catalog, new File(filePath), formatted);
    }
    
    public static void marshalCatalogToXml (Catalog catalog, File file) 
            throws JAXBException {
        marshalCatalogToXml (catalog, file, false);
    }
    
    public static void marshalCatalogToXml (Catalog catalog, File file, boolean formatted) 
            throws JAXBException {
        Marshaller marshaller 
                = JAXBContext.newInstance(Catalog.class).createMarshaller();
        if (formatted) {
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        }
        marshaller.marshal(catalog, file);
    }

    public static Catalog getTestCatalog (Catalog masterCatalog, int sizeOfTestCatalog)
            throws JAXBException {
        int audiobookCount = 0;
        Catalog testCatalog = new Catalog();
        for (Audiobook audiobook : masterCatalog.audiobooks) {
            if (++audiobookCount > sizeOfTestCatalog) {
                break;
            }
            testCatalog.audiobooks.add(audiobook);
        }
        
        return testCatalog.cloneWithJAXB();
    }
    
    public static void serializeCatalog (Catalog catalog, String fileName)
            throws IOException {
        try (ObjectOutputStream objectOutputStream 
                    = new ObjectOutputStream(new FileOutputStream(fileName));) {
            objectOutputStream.writeObject(catalog);
        }
    }

    public static Catalog deserializeCatalog (String fileName) 
            throws IOException, ClassNotFoundException {
        Catalog catalog;
        try (ObjectInputStream objectInputStream
                = new ObjectInputStream(new FileInputStream(fileName));) {
            catalog = (Catalog) objectInputStream.readObject();
        }
        return catalog;
    }
}
