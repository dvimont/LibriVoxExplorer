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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author dvimont
 */
public class LeCatalog {
    static String function;
    static String projectPathString; 
           // = "C:\\Users\\DanUltra\\Documents\\javaLibriVoxFiles\\";
    static String previousBuildPathString; //  = PROJECTPATH + "round0006\\";
    static String buildPathString; //           = PROJECTPATH + "round0010\\"; 
    static String jpegPermanentPathString; 
                        //    = PROJECTPATH + "jpegs\\fullSize\\coverArt\\";
    static String jpegNewImagesPathString; // = buildPathString + "newJpegs\\";
    static String logOutputPathString;
    static File logOutputDirectory = null;
    static int startingAudiobookId = 1;
    static int processingLimit = 0;
    static boolean deltaSuppressCoverArtChange = false;
    private static final String ASSEMBLE_ALL_FUNCTION = "assembleAll";
    private static final String ASSEMBLE_CATALOG_FUNCTION = "assembleCatalog";
    private static final String ASSEMBLE_DELTA_CATALOG_FUNCTION 
                                                    = "assembleDeltaCatalog";
    private static final String DOWNLOAD_NEW_JPEGS_FUNCTION = "downloadNewJpegs";

    /**
     * @param args command line arguments -- <br><br>
     * Valid arguments and values are as follows:<br>
     FUNCTION=[assembleAll|assembleCatalog|assembleDeltaCatalog|downloadNewJpegs]<br>
     PROJECT_PATH=[valid existing locally-accessible path]<br>
     PREVIOUS_BUILD_FOLDER=[valid existing folder in Project Path]<br>
     BUILD_FOLDER=[valid folder in Project Path, must already exist only if FUNCTION=assembleDeltaCatalog]<br>
     JPEG_PERMANENT_FOLDER=[valid existing folder in Project Path]<br>
     JPEG_NEW_IMAGES_SUBFOLDER=[valid folder within Build Folder, not necessarily existing]<br>
     LOG_OUTPUT_SUBFOLDER=[valid folder within Build Folder, not necessarily existing]<br>
     STARTING_AUDIOBOOK_ID=[optional integer ID value for partial test processing]<br>
     PROCESSING_LIMIT=[optional integer for partial test processing]
     DELTA_SUPPRESS_COVER_ART_CHANGE=[Y|N]
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        
        boolean argsAreValid = parseAndValidate(false, args);
        if (!argsAreValid) {
            return;
        }
        
        /** Create new directories */
        if (!Files.exists(Paths.get(buildPathString))) {
            new File(buildPathString).mkdir();
        }
        if (logOutputDirectory != null
                && !Files.exists(logOutputDirectory.toPath())) {
            logOutputDirectory.mkdir();
        }
 
        /** CATALOG ASSEMBLY PROCESSING */
        if (function.equalsIgnoreCase(ASSEMBLE_ALL_FUNCTION)
                || function.equalsIgnoreCase(ASSEMBLE_CATALOG_FUNCTION)) {
            if (!Files.exists(Paths.get(buildPathString))) {
                new File(buildPathString).mkdir();
            }
            CatalogAssembler.assembleCompleteCatalog
                (buildPathString, startingAudiobookId, processingLimit);
        }
        
        /** DELTA PROCESSING */
        if (function.equalsIgnoreCase(ASSEMBLE_ALL_FUNCTION)
                || function.equalsIgnoreCase(ASSEMBLE_DELTA_CATALOG_FUNCTION)) {
            CatalogAssembler.assembleDeltaCatalog
                            (previousBuildPathString, buildPathString,
                                    deltaSuppressCoverArtChange);
        }
        
        /** JPEG PROCESSING */
        if (function.equalsIgnoreCase(ASSEMBLE_ALL_FUNCTION)
                || function.equalsIgnoreCase(DOWNLOAD_NEW_JPEGS_FUNCTION)) {
            CatalogAssembler.assembleNewJpegs
                        (buildPathString, jpegPermanentPathString, jpegNewImagesPathString);
        }
    }

    public static boolean parseAndValidate (String[] args) {
        return parseAndValidate(false, args);
    }
    
    public static boolean parseAndValidate (boolean verbose, String[] args) {
        boolean invalidArgFound = false;
        PrintStream verbosePrintStream; 
        if (verbose) {
            /* all messages printed */
            verbosePrintStream = System.out;
        } else {
            /* only error/exception messages printed */
            verbosePrintStream = new PrintStream(new DummyOutputStream());
        }
        verbosePrintStream.println("The following runtime arguments for "
                + "CATALOG ASSEMBLY were submitted:");
        for (String arg : args) {
            verbosePrintStream.println("     " + arg);
        }
        verbosePrintStream.println();
        
        Map<String,String> argValueMap = new HashMap<>();
        for (String arg : args) {
            String[] argValuePair = arg.split("=");
            if (argValuePair.length != 2) {
                invalidArgFound = true;
                System.out.println("***INVALID ARGUMENT (FORMAT) SUBMITTED***\n" 
                                        + "     " + arg + "\n"
                                        + "     [Required:  Argument + Value "
                                        + "delimited by '=' sign.]\n");
                continue;
            }
            argValueMap.put(argValuePair[0], argValuePair[1]);
        }
        
        for (Entry<String,String> entry : argValueMap.entrySet()) {
            switch (entry.getKey()) {
                case "FUNCTION":
                    function = entry.getValue();
                    if (function.equalsIgnoreCase(ASSEMBLE_ALL_FUNCTION)
                            || function.equalsIgnoreCase(ASSEMBLE_CATALOG_FUNCTION)
                            || function.equalsIgnoreCase(ASSEMBLE_DELTA_CATALOG_FUNCTION)
                            || function.equalsIgnoreCase(DOWNLOAD_NEW_JPEGS_FUNCTION)) {
                    } else {
                        invalidArgFound = true;
                        printInvalidArgument(entry, false);
                        return false;
                    }
                    break;
                case "PROJECT_PATH":
                    if (Files.exists(Paths.get(entry.getValue()))) {
                        projectPathString = entry.getValue();
                    } else {
                        invalidArgFound = true;
                        printInvalidArgument(entry, true);
                        return false;
                    }
                    break;
            }
        }
        for (Entry<String,String> entry : argValueMap.entrySet()) {
            switch (entry.getKey()) {
                case "BUILD_FOLDER": // might not already exist
                    try { 
                        buildPathString 
                            = Paths.get(projectPathString, entry.getValue()).toString();
                    } catch (InvalidPathException e) {
                        invalidArgFound = true;
                        printInvalidArgument(entry, false);
                    }
                    if (function.equalsIgnoreCase(ASSEMBLE_DELTA_CATALOG_FUNCTION)
                            && !Files.exists(new File(buildPathString).toPath())) {
                        invalidArgFound = true;
                        printInvalidArgument(entry, true);
                    }
                    break;
            }
        }
        for (Entry<String,String> entry : argValueMap.entrySet()) {
            switch (entry.getKey()) {
                case "FUNCTION": case "PROJECT_PATH": case "BUILD_FOLDER":
                    break;
                case "PREVIOUS_BUILD_FOLDER": // MUST already exist
                    if (Files.exists(Paths.get(projectPathString, entry.getValue()))) {
                        previousBuildPathString 
                            = Paths.get(projectPathString, entry.getValue()).toString();
                    } else {
                        invalidArgFound = true;
                        printInvalidArgument(entry, true);
                    }
                    break;
                case "JPEG_PERMANENT_FOLDER": // MUST already exist
                    if (Files.exists(Paths.get(projectPathString, entry.getValue()))) {
                        jpegPermanentPathString 
                            = Paths.get(projectPathString, entry.getValue()).toString();
                    } else {
                        invalidArgFound = true;
                        printInvalidArgument(entry, true);
                    }
                    break;
                case "JPEG_NEW_IMAGES_SUBFOLDER": // might not already exist
                    try { 
                        jpegNewImagesPathString 
                            = Paths.get(buildPathString, entry.getValue()).toString();
                    } catch (InvalidPathException e) {
                        invalidArgFound = true;
                        printInvalidArgument(entry, false);
                    }
                    break;
                case "LOG_OUTPUT_SUBFOLDER": // might not already exist
                    try { 
                        logOutputPathString 
                            = Paths.get(buildPathString,
                                                entry.getValue()).toString();
                        logOutputDirectory = new File(logOutputPathString);
                    } catch (InvalidPathException e) {
                        invalidArgFound = true;
                        printInvalidArgument(entry, false);
                    }
                    break;
                case "STARTING_AUDIOBOOK_ID":
                    try {
                        startingAudiobookId = Integer.parseInt(entry.getValue());
                    } catch (NumberFormatException e) {
                        invalidArgFound = true;
                        printInvalidArgument(entry, false);
                    }
                    break;
                case "PROCESSING_LIMIT":
                    try {
                        processingLimit = Integer.parseInt(entry.getValue());
                    } catch (NumberFormatException e) {
                        invalidArgFound = true;
                        printInvalidArgument(entry, false);
                    }
                    break;
                case "DELTA_SUPPRESS_COVER_ART_CHANGE":
                    if (!(entry.getValue().toLowerCase().equals("y")
                            || entry.getValue().toLowerCase().equals("n"))) {
                        invalidArgFound = true;
                        printInvalidArgument(entry, false);
                    }
                    if (entry.getValue().toLowerCase().equals("y")) {
                        deltaSuppressCoverArtChange = true;
                    }
                    break;
                default:
                    invalidArgFound = true;
                    System.out.println("***INVALID ARGUMENT TYPE SUBMITTED***\n" 
                                        + "     " + entry.getKey() + "\n");
            }
        }

        if (invalidArgFound) {
            return false;
        }
        verbosePrintStream.println("===================\n" +
                "All runtime arguments for CATALOG ASSEMBLY are valid.");
        return true;
    }
    
    public static File getLogOutputDirectory() {
        return logOutputDirectory;
    }
    
    private static void printInvalidArgument 
                            (Entry<String,String> entry, boolean mustExist) {
        System.out.println
            ("***Invalid " + entry.getKey() + " value submitted***\n" 
                    + "     Value submitted = " + entry.getValue());
        if (mustExist) {
            System.out.println("     This folder must already exist when "
                                + "FUNCTION=" + function + ".");
        }
        System.out.println();
    }
    
    private static class DummyOutputStream extends OutputStream {
        @Override
        public void write(int i) throws IOException {
        }
    }

}
