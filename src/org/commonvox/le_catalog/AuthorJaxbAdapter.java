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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This JAXB adapter used to unmarshal {@link Author} objects from xml 
 * returned by the LibriVox API.
 * @author Daniel Vimont
 */
@XmlRootElement(name="xml")
public class AuthorJaxbAdapter {
    private static final String LV_AUTHOR_API_CALL
        = "https://librivox.org/api/feed/authors/?id=%s&extended=1&format=xml";
    
    private List<Author> authors;
    
    /**
     * Method required for JAXB processing
     * @return list of {@link Author}s
     */
    @XmlElementWrapper(name = "authors")
    @XmlElement(name = "author")
    public List<Author> getAuthors () {
        return authors;
    }
    
    /**
     * Method required for JAXB processing
     * @param authors list of {@link Author}s
     */
    public void setAuthors (List<Author> authors) {
        this.authors = authors;
    }
    
    /**
     * Get {@link Author} object via JAXB unmarshalling of LibriVox API's 
     * xml output
     * @param authorId
     * @return {@link Author} object unmarshalled from LibriVox API's xml output
     * @throws JAXBException if JAXB unmarshal fails
     * @throws MalformedURLException if LibriVox API url is malformed
     * @throws RemoteApiProcessingException if LibriVox API returns unexpected data
     */
    public static Author getAuthorViaApi (String authorId) 
            throws JAXBException, MalformedURLException, RemoteApiProcessingException {
        String apiCallString = String.format(LV_AUTHOR_API_CALL, authorId);
        URL url = new URL(apiCallString);
        AuthorJaxbAdapter authorsList = (AuthorJaxbAdapter) JAXBContext.newInstance
                                        (AuthorJaxbAdapter.class).createUnmarshaller()
                                        .unmarshal(url);

        if (authorsList.getAuthors().size() != 1) {
            throw new RemoteApiProcessingException();
        }
        
        return authorsList.getAuthors().get(0);
    }

}
