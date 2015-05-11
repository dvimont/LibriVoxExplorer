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

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.util.Duration;

/**
 *
 * @author Daniel Vimont
 */
public class MyBookmarks 
        implements PersistedUserSelectedCollection, Comparable<MyBookmarks>  {
    private final Map<String,String> bookmarkMap = new TreeMap<>();
    private static final Preferences USER_LIST_PREFERENCES 
                        = Preferences.userNodeForPackage(MyBookmarks.class);
    private static final String BOOKMARKS_PREFERENCE_KEY = "bookmarks";
    private static final String COMMA = ",";
    private static final String COLON = ":";
    private static final String SEMICOLON = ";";
    
    public MyBookmarks() {
        String bookmarksCSV 
                = USER_LIST_PREFERENCES.get(BOOKMARKS_PREFERENCE_KEY, "");
        Set<String> bookmarkSet = new TreeSet<>();
        if (bookmarksCSV != null && !bookmarksCSV.isEmpty()) {
            bookmarkSet.addAll(Arrays.asList(bookmarksCSV.split(COMMA)));
        }
        for (String bookmark : bookmarkSet) {
            String[] bookmarkComponents = bookmark.split(COLON);
            bookmarkMap.put(bookmarkComponents[0], bookmarkComponents[1]);
        }
    }
    
    @Override
    public boolean contains (HasLibrivoxId object) {
        if (Audiobook.class.isAssignableFrom(object.getClass())) {
            return bookmarkMap.containsKey(((Audiobook)object).getId());
        } else {
            return false;
        }
        
    }
    
    public Bookmark get (Audiobook audiobook) {
        if (!this.contains(audiobook)) {
            return null;
        }
        Bookmark bookmark = new Bookmark(audiobook);
        return bookmark;
    }
    
    public void add (Audiobook audiobook, int sectionIndex, Duration currentTime) {
        if (this.contains(audiobook)) {
            this.remove(audiobook);
        }
        bookmarkMap.put(audiobook.getId(), 
                            sectionIndex + SEMICOLON + currentTime.toMillis());
        persist();
    }
    
    @Override
    public void remove (HasLibrivoxId object) {
        if (Audiobook.class.isAssignableFrom(object.getClass())) {
            bookmarkMap.remove(((Audiobook)object).getId());
            persist();
        }
    }

    public void remove (Bookmark bookmark) {
        bookmarkMap.remove(bookmark.getAudiobook().getId());
        persist();
    }

    @Override
    public void clear(Class<? extends HasLibrivoxId> submittedClass) {
        if (Audiobook.class.isAssignableFrom(submittedClass)) {
            bookmarkMap.clear();
            persist();
        }
    }
    
    private void persist() {
        StringBuilder bookmarksCSV = new StringBuilder();
        for (Entry<String,String> bookmarkEntry : bookmarkMap.entrySet()) {
            bookmarksCSV.append(bookmarkEntry.getKey()).append(COLON)
                    .append(bookmarkEntry.getValue()).append(COMMA);
        }
        USER_LIST_PREFERENCES.put
                    (BOOKMARKS_PREFERENCE_KEY, bookmarksCSV.toString());

        try {
            USER_LIST_PREFERENCES.flush();
        } 
        catch (BackingStoreException be) {
            System.out.println("BackingStoreException encountered when "
                + "saving MyBookmarks data to Preferences using flush method.");
        }
    }
    
    public int size (Class<? extends HasLibrivoxId> submittedClass) {
        if (Audiobook.class.isAssignableFrom(submittedClass)) {
            return bookmarkMap.size();
        } else {
            return 0;
        }
    }
    
    public String getKeyItem() {
        return null;
    }
    
    @Override
    public String toString() {
        return "MY BOOKMARKED AUDIOBOOKS";
    }
    
    @Override
    public int compareTo(MyBookmarks other) {
        return 0;
    }

    
    protected void dumpContents () {
        System.out.println
                (USER_LIST_PREFERENCES.get(BOOKMARKS_PREFERENCE_KEY, ""));
    }
    
    public class Bookmark {
        protected Audiobook audiobook;
        protected String audiobookId;
        protected int sectionIndex;
        protected Duration currentTime;
        
        private Bookmark () { }
        
        private Bookmark (Audiobook audiobook) {
            if (bookmarkMap.containsKey(audiobook.getId())) {
                String[] sectionTimeComponents 
                    = bookmarkMap.get(audiobook.getId()).split(SEMICOLON);
                this.audiobook = audiobook;
                this.sectionIndex = Integer.parseInt(sectionTimeComponents[0]);
                this.currentTime 
                        = new Duration(Double.valueOf(sectionTimeComponents[1]));
            }
        }
        
        public Audiobook getAudiobook() {
            return this.audiobook;
        }
        
        public int getSectionIndex() {
            return this.sectionIndex;
        }
        
        public Duration getCurrentTime() {
            return this.currentTime;
        }
    }
}
