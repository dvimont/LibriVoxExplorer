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
package org.commonvox.le_browser;

import java.util.prefs.Preferences;

/**
 *
 * @author Daniel Vimont
 */
public class PersistedAppSettings {
    private static final Preferences APPLICATION_SETTINGS 
                    = Preferences.userNodeForPackage(LeBrowser.class);
    private static final String COVER_ART_DISPLAY_SETTING_KEY = "hiDefDisplayOption";
    protected enum CoverArtDisplaySetting {ENABLE_ALL, DISABLE_DOWNLOAD, DISABLE_ALL;
        private static CoverArtDisplaySetting getDefault() {
            return ENABLE_ALL;
        }
    };
    private static final String ROW_AUTOCLOSE_SETTING_KEY = "rowAutoCloseOption";
    protected enum RowAutocloseSetting {AUTOCLOSE_TRUE("Y"), AUTOCLOSE_FALSE("N");
        private String abbr;
        RowAutocloseSetting (String abbr) {
           this.abbr = abbr; 
        }
        private static RowAutocloseSetting getDefault() {
            return AUTOCLOSE_FALSE;
        }
        @Override
        public String toString() {
           return abbr; 
        }
    };
    //private static final String ROW_AUTOCLOSE_SETTING_KEY = "rowAutoCloseOption";
    //private static final String AUTOCLOSE_TRUE = "Y";
    //private static final String AUTOCLOSE_FALSE = "N"; // default
    private static final String BOOKMARK_SUPPRESS_SETTING_KEY 
                                            = "bookmarkSuppressOption";
    protected enum BookmarkSuppressSetting {BOOKMARKS_SUPPRESS("Y"), BOOKMARKS_ALLOW("N");
        private String abbr;
        BookmarkSuppressSetting (String abbr) {
           this.abbr = abbr; 
        }
        private static BookmarkSuppressSetting getDefault() {
            return BOOKMARKS_ALLOW;
        }
        @Override
        public String toString() {
           return abbr; 
        }
    };

    protected static CoverArtDisplaySetting getCoverArtDisplaySetting () {
        String currentSetting = APPLICATION_SETTINGS.get
                                    (COVER_ART_DISPLAY_SETTING_KEY, 
                                        CoverArtDisplaySetting.getDefault().toString());
        for (CoverArtDisplaySetting setting : CoverArtDisplaySetting.values()) {
            if (setting.toString().equals(currentSetting)) {
                return setting;
            }
        }
        return CoverArtDisplaySetting.getDefault();
    }

    protected static void setCoverArtDisplaySetting (CoverArtDisplaySetting setting) {
        APPLICATION_SETTINGS.put
                    (COVER_ART_DISPLAY_SETTING_KEY, setting.toString());
    }

    protected static RowAutocloseSetting getRowAutocloseSetting () {
        String currentSetting = APPLICATION_SETTINGS.get
                                    (ROW_AUTOCLOSE_SETTING_KEY, 
                                        RowAutocloseSetting.getDefault().toString());
        for (RowAutocloseSetting setting : RowAutocloseSetting.values()) {
            if (setting.toString().equals(currentSetting)) {
                return setting;
            }
        }
        return RowAutocloseSetting.getDefault();
    }

    protected static void setRowAutocloseSetting (RowAutocloseSetting setting) {
        APPLICATION_SETTINGS.put
                    (ROW_AUTOCLOSE_SETTING_KEY, setting.toString());
    }

    protected static BookmarkSuppressSetting getBookmarkSuppressSetting () {
        String currentSetting = APPLICATION_SETTINGS.get
                                    (BOOKMARK_SUPPRESS_SETTING_KEY, 
                                        BookmarkSuppressSetting.getDefault().toString());
        for (BookmarkSuppressSetting setting : BookmarkSuppressSetting.values()) {
            if (setting.toString().equals(currentSetting)) {
                return setting;
            }
        }
        return BookmarkSuppressSetting.getDefault();
    }

    protected static void setBookmarkSuppressSetting 
                                    (BookmarkSuppressSetting setting) {
        APPLICATION_SETTINGS.put
                    (BOOKMARK_SUPPRESS_SETTING_KEY, setting.toString());
    }

    
}
