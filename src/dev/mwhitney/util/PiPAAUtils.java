package dev.mwhitney.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import dev.mwhitney.main.Initializer;

/**
 * Utility functions for the entire application.
 * 
 * @author mwhitney57
 */
public class PiPAAUtils {
    /**
     * Returns a "human readable" representation of the passed byte count long.
     * This method was discovered as a replacement for Apache Commons IO's
     * <code>FileUtils.byteCountToDisplaySize(long)</code>, since that
     * method rounds the value and is therefore not ideal.
     * 
     * @param bytes - a long with the total bytes.
     * @return a String, human readable representation of the passed bytes.
     * @author <a href="https://stackoverflow.com/u/276052">aioobe</a>
     * @see <a href="https://stackoverflow.com/a/3758880">Code Source</a>
     */
    public static String humanReadableByteCountSI(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        java.text.CharacterIterator ci = new java.text.StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }
    
    /**
     * Prunes/deletes every empty directory within the passed directory, then ultimately deletes the directory itself (if it is empty.)
     * This method will recursively search directories and remove any empty ones.
     * 
     * @param folder - the File with folder to prune (if empty) and any empty directories within it.
     * @throws IOException if there was a problem ensuring the directory is empty.
     */
    public static void pruneFolder(File folder) throws IOException {
        final File[] files = folder.listFiles();
        if(files != null) // Null if error or is a file instead of a directory.
            for(final File f : files)
                if(f.isDirectory())
                    pruneFolder(f);
        
        // Finally, delete folder itself if empty.
        if (folder.isDirectory() && FileUtils.isEmptyDirectory(folder))
            folder.delete();
    }
    
    /**
     * Prunes/deletes every empty directory within the application cache directory, then ultimately deletes the directory itself (if it is empty.)
     * This method will recursively search directories and remove any empty ones.
     * 
     * @throws IOException if there was a problem ensuring the directory is empty.
     */
    public static void pruneCacheFolder() throws IOException {
        pruneFolder(new File(Initializer.APP_CACHE_FOLDER));
    }
}
