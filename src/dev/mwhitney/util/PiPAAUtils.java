package dev.mwhitney.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import dev.mwhitney.main.Initializer;
import dev.mwhitney.resources.AppRes;

/**
 * Utility functions for the entire application.
 * 
 * @author mwhitney57
 */
public class PiPAAUtils {
    /**
     * Ensures the existence of one or more folders at the specified String paths.
     * This method creates the directories, including their parent directories, if
     * they do not exist. None of the passed String objects can be null.
     * 
     * @param folders - one or more Strings corresponding to local directory
     *                path(s).
     */
    public static void ensureExistence(final String... folders) {
        for (final String s : Objects.requireNonNull(folders, "Cannot ensure the existence of null folders.")) {
            if (s == null) continue;
            final File folder = new File(s);
            folder.mkdirs();
        }
    }

    /**
     * Performs a simple slash fix on the passed String path, converting
     * <code>/</code> to <code>\</code> throughout.
     * 
     * @param path - the String path to fix.
     * @return the fixed String path.
     */
    public static String slashFix(final String path) {
        return path.replace('/', '\\');
    }
    
    /**
     * Gets an array this enum's values as String objects.
     * 
     * @return a String[] of this enum's values.
     */
    public static String[] toStringArray(final Class<? extends Enum<?>> e) {
        return Arrays.stream(e.getEnumConstants()).map(Enum::toString).toArray(String[]::new);
    }
    
    /**
     * Checks to see if the passed value is present in the passed array.
     * <p>
     * Since this method performs an <code>==</code> comparison, only the same
     * objects in memory will output <code>true</code> for this method. Therefore,
     * it is good for comparing primitive types or static final types such as enums.
     * <p>
     * This method will return <code>false</code> if either of the passed arguments
     * is <code>null</code>.
     * 
     * @param <A>   - the type of the passed object and object array.
     * @param value - the value to check for in the passed array.
     * @param array - the array to check for the value in.
     * @return <code>true</code> if the value is in the array; <code>false</code>
     *         otherwise.
     * @see {@link org.apache.commons.lang3.ArrayUtils#contains(Object[], Object)}
     *      for similar logic.
     */
    public static <A> boolean inArray(final A value, final A[] array) {
        // Return false if either argument is null.
        if (array == null || value == null) return false;
        
        // Return true for the first match found, if any.
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) return true;
        }
        return false;
    }
    
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
     * <b>The passed {@link File} must be non-<code>null</code>.</b>
     * 
     * @param folder - the File with folder to prune (if empty) and any empty directories within it.
     * @throws IOException if there was a problem ensuring the directory is empty.
     */
    public static void pruneFolder(File folder) throws IOException {
        final File[] files = Objects.requireNonNull(folder, "Cannot prune folders within a <null> File directory.").listFiles();
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
        pruneFolder(new File(AppRes.APP_CACHE_FOLDER));
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
        
        final Collection<File> files = FileUtils.listFiles(new File(AppRes.APP_CLIPBOARD_FOLDER), new String[] { FilenameUtils.getExtension(f.getName()) }, true);
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
        File outF = new File(AppRes.APP_CLIPBOARD_FOLDER + "/" + name);
        while (outF == null || outF.exists()) {
            outF = new File(AppRes.APP_CLIPBOARD_FOLDER + "/" + nameNoExt + (int) (Math.random() * 100000) + "." + nameExt);
        }
        return outF;
    }

    /**
     * Appends a series of Integers together. Whereas adding integers provides a
     * sum, this method simply connects them together. Leading zeroes are ignored,
     * but trailing zeroes are respected.
     * 
     * Negative values are not accepted by this method and will throw an
     * {@link IllegalArgumentException}. Valid Examples:
     * 
     * <pre>
     * { 0,  0, 2, 3, 9 } → 239
     * { 0,  0, 4, 0, 0 } → 4
     * { 1, 12, 4, 0, 0 } → 112400
     * </pre>
     * 
     * @param nums - one or more integers to append.
     * @return the appended Integer result, or <code>-1</code> if there were no ints
     *         to append.
     * @throws IllegalArgumentException if a negative number is passed.
     * @throws NumberFormatException    if, inexplicably, a non-int value is present
     *                                  that cannot be parsed into an int.
     * @author mwhitney57
     * @since 0.9.5
     */
    public static Integer appendInts(Integer... nums) {
        if (nums == null || nums.length < 1) return -1;
        
        final StringBuilder build = new StringBuilder(nums.length);
        for (final Integer i : nums) {
            if (i < 0) throw new IllegalArgumentException("Negative numbers cannot be appended: " + i);
            build.append(i);
        }
        return Integer.parseInt(build.toString());
    }

    /**
     * Splits an integer into individual digits, returning them all in an int array.
     * Negative integers are accepted, but they naturally lose the negative sign.
     * Examples:
     * <pre>
     * splitInt(123)  → { 1, 2, 3 }
     * splitInt(1)    → { 1 }
     * splitInt(0)    → { 0 }
     * splitInt(-123) → { 1, 2, 3 }
     * </pre>
     * 
     * @param number - the number to split.
     * @return an int array with the split digits of the passed number.
     * @author mwhitney57
     * @since 0.9.5
     */
    public static int[] splitInt(int number) {
        // Handle zero manually. Math log10 function cannot handle zero, returns negative infinity.
        if (number == 0) return new int[] {0};
        // Determine length mathematically. Should be more efficient than converting to String for length.
        final int   length = (int) Math.log10(Math.abs(number)) + 1;
        System.out.println("array length: " + length);
        // Initialize digits array of desired final length.
        final int[] digits = new int[length];
        // Works right to left extracting digits from the initial number until it reaches <= zero.
        for (int i = length - 1; i >= 0; i--) {
            digits[i] = number % 10;    // Gets remainder after removing all 10s.
            number /= 10;               // Remove set of 10s. Example: 123 -> 12.
        }
        return digits;
    }
    /**
     * Prints out a statement which verifies if the calling thread is the
     * event-dispatch thread (EDT).
     * 
     * @author mwhitney57
     * @since 0.9.5
     */
    public static void isEDT() {
        System.out.println("<?> Is on the EDT: " + SwingUtilities.isEventDispatchThread());
    }

    /**
     * Executes the passed {@link Supplier} on the event-dispatch thread (EDT),
     * making the component and returning the result. The returned result may be
     * <code>null</code> if the creation fails, or if the executed code naturally
     * returns that result.
     * <p>
     * This method <b>will run the {@link Supplier} on the EDT</b>, as it uses
     * {@link SwingUtilities#invokeAndWait(Runnable)}. The calling thread can be on
     * the EDT, such as through a {@link SwingUtilities#invokeLater(Runnable)} call,
     * in which case {@link Supplier#get()} will be called immediately.
     * 
     * @param <T>      - the return type, designed to be a Swing-related class which
     *                 must be created on the EDT.
     * @param supplier - the {@link Supplier} which executes on the EDT and supplies
     *                 the result.
     * @return the supplied result, or <code>null</code>.
     * @author mwhitney57
     * @since 0.9.5
     */
    public static <T> T makeOnEDT(Supplier<T> supplier) {
        final AtomicReference<T> ref = new AtomicReference<>();
        try {
            // Supplier may include Swing-related code that must run on EDT.
            invokeNowAndWait(() -> ref.set(supplier.get()));
        } catch (InvocationTargetException | InterruptedException e) {
            // Error with supplier execution. Return null due to interruption.
            return null;
        }
        // Get object from supplier and return.
        return ref.get();
    }

    /**
     * Executes the passed {@link Runnable} now if this method is called <b>off</b>
     * of the event-dispatch thread (EDT). Otherwise, it is run asynchronously via
     * {@link CompletableFuture#runAsync(Runnable)}. This method inconsistently
     * blocks, only doing so if called from outside the EDT.
     * <p>
     * This method helps ensure the passed code is executed off of the EDT. For
     * Swing adjustments, use {@link #invokeNowOrLater(Runnable)} or
     * {@link #invokeNowAndWait(Runnable)}.
     * 
     * @param run - the {@link Runnable} that should be executed off of the EDT.
     * @since 0.9.5
     */
    public static void invokeNowOrAsync(Runnable run) {
        if (SwingUtilities.isEventDispatchThread()) CompletableFuture.runAsync(run);
        else run.run();
    }

    /**
     * Executes the passed {@link Runnable} now if this method is called from the
     * event-dispatch thread (EDT). Otherwise, it is scheduled to run later via
     * {@link SwingUtilities#invokeLater(Runnable)}.
     * <p>
     * This method helps ensure the passed code is executed on the EDT. Do not
     * include code that would slow or block the EDT.
     * 
     * @param run - the {@link Runnable} that should be executed on the EDT.
     * @since 0.9.5
     */
    public static void invokeNowOrLater(Runnable run) {
        if (SwingUtilities.isEventDispatchThread()) run.run();
        else SwingUtilities.invokeLater(run);
    }
    
    /**
     * Executes the passed {@link Runnable} now if this method is called from the
     * event-dispatch thread (EDT). Otherwise, it runs soon, in sync with the EDT,
     * via {@link SwingUtilities#invokeAndWait(Runnable)} and blocks until
     * completion. Either way, this method returns once the code has finished
     * executing.
     * <p>
     * This method helps ensure the passed code is executed on the EDT. Do not
     * include code that would slow or block the EDT.
     * 
     * @param run - the {@link Runnable} that should be executed on the EDT.
     * @throws InterruptedException      if thrown by
     *                                   {@link SwingUtilities#invokeAndWait(Runnable)}.
     * @throws InvocationTargetException if thrown by
     *                                   {@link SwingUtilities#invokeAndWait(Runnable)}.
     * @since 0.9.5
     */
    public static void invokeNowAndWait(Runnable run) throws InvocationTargetException, InterruptedException {
        if (SwingUtilities.isEventDispatchThread()) run.run();
        else SwingUtilities.invokeAndWait(run);
    }
}
