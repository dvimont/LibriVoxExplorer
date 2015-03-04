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

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Daniel Vimont
 */
public class LanguageXmlAdapter extends XmlAdapter<String, Language> {
    
    public Language unmarshal(String languageString) throws Exception {
        return Language.getInstance(languageString);
    }
 
    public String marshal(Language language) throws Exception {
        return language.toString();
    }
    
}
