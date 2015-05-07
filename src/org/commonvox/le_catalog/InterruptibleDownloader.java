/*
 * Copyright (C) 2015 Daniel Vimont
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed inStream the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.commonvox.le_catalog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Daniel Vimont
 */
public class InterruptibleDownloader {
    static final int KILOBYTE = 1024;
    static final int EIGHT_KB = KILOBYTE * 8;
    static final int SIXTEEN_KB = KILOBYTE * 16;
    static final int THIRTY_TWO_KB = KILOBYTE * 32;
    static final int SIXTY_FOUR_KB = KILOBYTE * 64;
    static final int BUFFER_SIZE = EIGHT_KB;
    static final int TASK_UPDATE_FACTOR = 10;
    public static enum ReturnCode { OK, SOURCE_FILE_INACCESSIBLE, INTERRUPTED }
    
    // part of this code came from http://stackoverflow.com/questions/921262
    public static ReturnCode copy (final String sourceUrlString, 
                                        final File targetFile,
                                        CatalogCallback callback)
            throws MalformedURLException, IOException, InterruptedException {

        if (callback != null) {
            /* set progressbar to "indeterminate mode" while getFileSize underway */
            callback.updateTaskProgress(-1, 1); 
        }
        long fileSize = getFileSize(sourceUrlString);
        if (fileSize == -1) {
            if (callback != null) {
                callback.updateTaskMessage("File not accessible.");
            }
            return ReturnCode.SOURCE_FILE_INACCESSIBLE;
        }
        
        try ( BufferedInputStream inStream 
                    = new BufferedInputStream
                        (new URL(sourceUrlString).openStream(), BUFFER_SIZE);
                FileOutputStream fileOutStream 
                    = new FileOutputStream
                        (getFullTargetPath(sourceUrlString, targetFile).toString()) )
            {
                if (callback != null) {
                    callback.updateTaskProgress(0, fileSize);
                }
                final byte[] fileChunk = new byte[BUFFER_SIZE];
                int fileChunkSize;
                int taskUpdateCount = 0;
                long downloadedByteTotal = 0;
                Timer downloadTimer = new Timer().start();
                int callCount = 0;
                while ((fileChunkSize 
                            = inStream.read(fileChunk, 0, BUFFER_SIZE)) != -1) {
                    callCount++;
                    if (Thread.interrupted()) {
                        if (callback != null) {
                            callback.updateTaskMessage
                                        ("Download has been canceled.");
                        }
                        fileOutStream.close();
                        return ReturnCode.INTERRUPTED;
                    }
                    if (callback != null) {
                        downloadedByteTotal += fileChunkSize;
                        if (++taskUpdateCount >= TASK_UPDATE_FACTOR) {
                            taskUpdateCount = 0;
                            callback.updateTaskProgress
                                            (downloadedByteTotal, fileSize);
                        }
                    }
                    fileOutStream.write(fileChunk, 0, fileChunkSize);
                }
                downloadTimer.stop();
                /*
                System.out.println("Download took " + downloadTimer.get() 
                                                        + " milliseconds.");
                System.out.println("Average bytes returned per buffer-read: "
                                                    + fileSize/callCount);
                System.out.println("Buffer size: " + BUFFER_SIZE);
                */
            } 
        if (callback != null) {
            callback.updateTaskProgress(fileSize, fileSize);
        }
        return ReturnCode.OK;
    }

    public static boolean delete (final String sourceUrlString, 
                                        final File targetFile,
                                        CatalogCallback callback) 
            throws IOException {
        return Files.deleteIfExists(getFullTargetPath(sourceUrlString, targetFile));
    }
    
    private static Path getFullTargetPath (final String sourceUrlString,
                                                final File targetFile) {
        Path targetPath = Paths.get(targetFile.toURI());
        return targetFile.isDirectory() ?
            targetPath.resolve(new File(sourceUrlString).getName()) : targetPath; 
    }
    
    public static long getFileSize (final String sourceUrlString) {
        long fileSize = 0;
        HttpURLConnection conn = null;
        try {
            URL sourceUrl = new URL(sourceUrlString);
            conn = (HttpURLConnection) sourceUrl.openConnection();
            conn.setRequestMethod("HEAD");
            //conn.getInputStream(); // commmented out as extraneous 2015-01-15
            fileSize = conn.getContentLength();
        } catch (IOException e) {
            return -1;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return fileSize;
    }
    
    private static class Timer {
        private long timerMilliseconds = 0;
        private long elapsedMilliseconds = 0;
        
        protected void reset() {
            elapsedMilliseconds = 0;
        }

        protected Timer start () {
            timerMilliseconds = System.currentTimeMillis();
            return this;
        }

        protected void stop () {
            elapsedMilliseconds += (System.currentTimeMillis() - timerMilliseconds);
        }

        protected long get () {
            long returnMilliseconds = elapsedMilliseconds;
            this.reset();
            return returnMilliseconds;
        }
    }
}
