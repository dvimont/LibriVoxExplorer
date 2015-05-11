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
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author Daniel Vimont
 */
public class MyList 
        implements PersistedUserSelectedCollection, Comparable<MyList> {
    private final TreeSet<String> authorIdSet = new TreeSet<>();
    private final TreeSet<String> readerIdSet = new TreeSet<>();
    private final TreeSet<String> audiobookIdSet = new TreeSet<>();
    
    private static final String AUTHOR_CLASS_SIMPLE_NAME = "Author";
    private static final String READER_CLASS_SIMPLE_NAME = "Reader";
    private static final String AUDIOBOOK_CLASS_SIMPLE_NAME = "Audiobook";
    private static final String COMMA = ",";
    
    private static final Preferences USER_LIST_PREFERENCES 
                        = Preferences.userNodeForPackage(MyList.class);
    private static final String AUTHOR_IDS_PREFERENCE_KEY = "authorIds";
    private static final String READER_IDS_PREFERENCE_KEY = "readerIds";
    private static final String AUDIOBOOK_IDS_PREFERENCE_KEY = "audiobookIds";
    
    /**
     *
     */
    public MyList () {
        String authorIdsCSV 
                = USER_LIST_PREFERENCES.get(AUTHOR_IDS_PREFERENCE_KEY, "");
        if (authorIdsCSV != null && !authorIdsCSV.isEmpty()) {
            authorIdSet.addAll(Arrays.asList(authorIdsCSV.split(COMMA)));
        }
        String readerIdsCSV 
                = USER_LIST_PREFERENCES.get(READER_IDS_PREFERENCE_KEY, "");
        if (readerIdsCSV != null && !readerIdsCSV.isEmpty()) {
            readerIdSet.addAll(Arrays.asList(readerIdsCSV.split(COMMA)));
        }
        String audiobookIdsCSV 
                = USER_LIST_PREFERENCES.get(AUDIOBOOK_IDS_PREFERENCE_KEY, "");
        if (audiobookIdsCSV != null && !audiobookIdsCSV.isEmpty()) {
            audiobookIdSet.addAll(Arrays.asList(audiobookIdsCSV.split(COMMA)));
        }
    }
    
    /**
     *
     * @param object
     * @return
     */
    @Override
    public boolean contains (HasLibrivoxId object) {
        switch (object.getClass().getSimpleName()) {
            case AUTHOR_CLASS_SIMPLE_NAME :
                return authorIdSet.contains(object.getId());
            case READER_CLASS_SIMPLE_NAME :
                return readerIdSet.contains(object.getId());
            case AUDIOBOOK_CLASS_SIMPLE_NAME :
                return audiobookIdSet.contains(object.getId());
        }
        return false;
    }
    
    /**
     *
     * @param submittedClass
     * @return
     */
    @Override
    public int size (Class<? extends HasLibrivoxId> submittedClass) {
        switch (submittedClass.getSimpleName()) {
            case AUTHOR_CLASS_SIMPLE_NAME :
                return authorIdSet.size();
            case READER_CLASS_SIMPLE_NAME :
                return readerIdSet.size();
            case AUDIOBOOK_CLASS_SIMPLE_NAME :
                return audiobookIdSet.size();
        }
        return 0;
    }
    
    /**
     *
     * @param object
     */
    public void add (HasLibrivoxId object) {
        if (this.contains(object)) {
            return;
        }
        switch (object.getClass().getSimpleName()) {
            case AUTHOR_CLASS_SIMPLE_NAME :
                authorIdSet.add(object.getId());
                break;
            case READER_CLASS_SIMPLE_NAME :
                readerIdSet.add(object.getId());
                break;
            case AUDIOBOOK_CLASS_SIMPLE_NAME :
                audiobookIdSet.add(object.getId());
                break;
        }
        persist();
    }
    
    /**
     *
     * @param object
     */
    @Override
    public void remove (HasLibrivoxId object) {
        switch (object.getClass().getSimpleName()) {
            case AUTHOR_CLASS_SIMPLE_NAME :
                authorIdSet.remove(object.getId());
                break;
            case READER_CLASS_SIMPLE_NAME :
                readerIdSet.remove(object.getId());
                break;
            case AUDIOBOOK_CLASS_SIMPLE_NAME :
                audiobookIdSet.remove(object.getId());
                break;
        }
        persist();
    }
    
    private void persist() {
        StringBuilder authorIdsCSV = new StringBuilder();
        for (String authorId : authorIdSet) {
            if (authorId == null || authorId.isEmpty()) {
                continue;
            }
            authorIdsCSV.append(authorId).append(COMMA);
        }
        USER_LIST_PREFERENCES.put
                    (AUTHOR_IDS_PREFERENCE_KEY, authorIdsCSV.toString());

        StringBuilder readerIdsCSV = new StringBuilder();
        for (String readerId : readerIdSet) {
            if (readerId == null || readerId.isEmpty()) {
                continue;
            }
            readerIdsCSV.append(readerId).append(COMMA);
        }
        USER_LIST_PREFERENCES.put
                    (READER_IDS_PREFERENCE_KEY, readerIdsCSV.toString());

        StringBuilder audiobookIdsCSV = new StringBuilder();
        for (String audiobookId : audiobookIdSet) {
            if (audiobookId == null || audiobookId.isEmpty()) {
                continue;
            }
            audiobookIdsCSV.append(audiobookId).append(COMMA);
        }
        USER_LIST_PREFERENCES.put
                    (AUDIOBOOK_IDS_PREFERENCE_KEY, audiobookIdsCSV.toString());

        try {
            USER_LIST_PREFERENCES.flush();
        } 
        catch (BackingStoreException be) {
            System.out.println("BackingStoreException encountered when "
                    + "saving MyList data to Preferences using flush method.");
        }
    }
    
    /**
     *
     * @param submittedClass
     */
    @Override
    public void clear (Class<? extends HasLibrivoxId> submittedClass) {
        switch (submittedClass.getSimpleName()) {
            case AUTHOR_CLASS_SIMPLE_NAME :
                authorIdSet.clear();
                break;
            case READER_CLASS_SIMPLE_NAME :
                readerIdSet.clear();
                break;
            case AUDIOBOOK_CLASS_SIMPLE_NAME :
                audiobookIdSet.clear();
                break;
        }
        persist();
    }
    
    public String getKeyItem() {
        return null;
    }
    
    @Override
    public String toString() {
        return "MY AUDIOBOOKS";
    }
    
    @Override
    public int compareTo(MyList other) {
        return 0;
    }
}
