package dev.mwhitney.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

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
    
    /**
     * Checks for a duplicate {@link File} within the
     * {@link Initializer#APP_CLIPBOARD_FOLDER}. In this context, a duplicate file
     * is <b>not</b> simply referring to a matching filename. A duplicate file is
     * one which is truly a copy of another. The data within the file matches.
     * 
     * @param f - the {@link File} to check for duplicates for.
     * @return the first duplicate {@link File} found, or <code>null</code> if none
     *         exists.
     */
    public static File fileDupeInCache(final File f) {
        if (f == null) return null;
        
        final Collection<File> files = FileUtils.listFiles(new File(Initializer.APP_CLIPBOARD_FOLDER), new String[] { FilenameUtils.getExtension(f.getName()) }, true);
        for (final File file : files) {
            if (file.getPath().equals(f.getPath())) continue;
            
            try {
                if (FileUtils.contentEquals(file, f)) return file;
            } catch (IOException ioe) { /* Ignore -- Not a problem. */ }
        }
        return null;
    }
    
    /**
     * Performs a file cache check operation. The passed {@link File}'s contents or
     * data are compared against others within the
     * {@link Initializer#APP_CLIPBOARD_FOLDER}. If an exact match is found, this
     * method simply returns the {@link File} representation of that duplicate.
     * Otherwise, the passed file is given a randomly-generated number, added to its
     * name, which ensures it does not conflict with another file of the same name
     * within the cache directory.
     * 
     * @param f - the {@link File} to run the cache check on. Must not be
     *          <code>null</code>.
     * @return the {@link File} within the cache directory, potentially adjusted
     *         with a new, random ID in the filename to make it unique.
     * @throws NullPointerException if the passed {@link File} is <code>null</code>.
     */
    public static File fileCacheCheck(final File f) {
        Objects.requireNonNull(f, "Cannot perform cache check on a NULL File.");
        
        // Check for duplicate file in cache, return it if match found.
        final File dupe = fileDupeInCache(f);
        if (dupe != null) return dupe;
        
        // Split up parts of file from file name.
        final String name      = f.getName();
        final String nameNoExt = FilenameUtils.removeExtension(name);
        final String nameExt   = FilenameUtils.getExtension(name);
        // Set would-be cache file, then ensure the name will not conflict.
        File outF = new File(Initializer.APP_CLIPBOARD_FOLDER + "/" + name);
        while (outF == null || outF.exists()) {
            outF = new File(Initializer.APP_CLIPBOARD_FOLDER + "/" + nameNoExt + (int) (Math.random() * 100000) + "." + nameExt);
        }
        return outF;
    }
}
