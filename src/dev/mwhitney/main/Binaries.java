package dev.mwhitney.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import dev.mwhitney.exceptions.UnsupportedBinActionException;
import dev.mwhitney.listeners.BinRunnable;
import dev.mwhitney.listeners.PropertyListener;
import net.codejava.utility.UnzipUtility;

/**
 * A helper class for handling binaries/executables used by PiPAA.
 * 
 * @author mwhitney57
 */
public class Binaries {
    /** A String with the path to the cookies file, which is used as the value to the cookies argument for some binaries. */
    public static final String COOKIES_PATH_ARG = "\"" + Initializer.APP_FOLDER + "/cookies.txt\"";
    /** A String with the path containing app ffmpeg binaries, which is used as the value to the '--ffmpeg-location' argument for some binaries. */
    public static final String FFMPEG_LOC_ARG = "\"" + System.getProperty("user.home") + "/AppData/Roaming/PiPAA/bin/ffmpeg/bin" + "\"";
    /** A <tt>String</tt> with the application's yt-dlp binary plugins folder location. */
    public static final String YTDLP_PLUGINS_FOLDER = Initializer.APP_BIN_FOLDER + "/yt-dlp-plugins";
    
    /** The set of binaries used by PiPAA. */
    public enum Bin {
        /** The yt-dlp executable for downloading media, typically videos. */
        YT_DLP("yt-dlp.exe", "stable@2024-11-18"),
        /** The gallery-dl executable for downloading media, typically images. */
        GALLERY_DL("gallery-dl.exe", "1.27.7"),
        /** The ffmpeg executable for media conversions. */
        FFMPEG("ffmpeg/bin/", "ffmpeg.exe", "7.0.1-essentials"),
        /** The ImageMagick executable for image/GIF conversions. */
        IMGMAGICK("imagemagick/", "magick.exe", "7.1.1-34");
        
        /** The path within the bin folder to the binary. */
        private String path;
        /** The executable name of the binary. */
        private String exe;
        /** The version of the binary. */
        private String version;
        
        /**
         * Creates a new Bin enum value with the passed executable name and version.
         * This constructor defaults the internal path to be empty, therefore assuming the
         * Bin is located directly within the application's bin folder.
         * 
         * @param s - the String executable filename.
         * @param v - the String version of the Bin.
         */
        private Bin(String s, String v) {
            this("", s, v);
        }
        
        /**
         * Creates a new Bin enum value with the passed path and executable name.
         * 
         * @param p - the String path within the bin folder to the Bin.
         * @param s - the String executable filename.
         * @param v - the String version of the Bin.
         */
        private Bin(String p, String s, String v) {
            this.path    = p;
            this.exe     = s;
            this.version = v;
        }
        
        /**
         * Gets a String path to the Bin within the application bin folder, including
         * the Bin itself.
         * 
         * <pre>
         * Example:
         * Bin.FFMPEG.toBin() == "folder/in/bin/ffmpeg.exe"
         * </pre>
         * 
         * @return a String with the path to the Bin.
         */
        public String toBin() {
            return (this.path + this.exe);
        }
        
        /**
         * Gets the version of the Bin.
         * 
         * @return a String with the binary's version.
         */
        public String version() {
            return this.version;
        }
        
        /**
         * Gets an <code>exe</code>-less version of the Bin's <code>toString()</code>.
         * 
         * @return a String with the Bin executable, without <code>.exe</code>.
         */
        public String exeless() {
            return this.toString().substring(0, this.toString().lastIndexOf('.'));
        }
        
        @Override
        public String toString() {
            return this.exe;
        }
    }
    
    /** A boolean for whether or not the <b>system</b> has yt-dlp installed and accessible. */
    public static boolean HAS_YTDLP     = false;
    /** A boolean for whether or not the <b>system</b> has gallery-dl installed and accessible. */
    public static boolean HAS_GALLERYDL = false;
    /** A boolean for whether or not the <b>system</b> has ffmpeg installed and accessible. */
    public static boolean HAS_FFMPEG    = false;
    /** A boolean for whether or not the <b>system</b> has ImageMagick installed and accessible. */
    public static boolean HAS_IMGMAGICK = false;
    
    /** The PropertyListener to get property states from. */
    private static PropertyListener propertyListener;
    
    /**
     * Returns the passed Bin binary command.
     * The appropriate command is chosen based on the user configuration.
     * 
     * @param b - the Bin to retrieve and return a proper executable header for.
     * @return a String with the result to be used in commands.
     */
    public static String bin(Bin b) {
        return bin(b, propertyListener.propertyState(PiPProperty.USE_SYS_BINARIES, Boolean.class));
    }
    
    /**
     * Returns the passed Bin binary command and specifies if system binaries should be used.
     * The appropriate command is chosen based on the passed boolean.
     * 
     * @param b - the Bin to retrieve and return a proper executable header for.
     * @param useSysBin - a boolean for whether or not system binaries should be used.
     * @return a String with the result to be used in commands.
     */
    public static String bin(Bin b, boolean useSysBin) {
        return switch(b) {
        case YT_DLP     -> (useSysBin && Binaries.HAS_YTDLP     ? b.exeless() : binned(b));
        case GALLERY_DL -> (useSysBin && Binaries.HAS_GALLERYDL ? b.exeless() : binned(b));
        case FFMPEG     -> (useSysBin && Binaries.HAS_FFMPEG    ? b.exeless() : binned(b));
        case IMGMAGICK  -> (useSysBin && Binaries.HAS_IMGMAGICK ? b.exeless() : binned(b));
        default -> null;
        };
    }
    
    /**
     * Returns the passed Bin, but "binned."
     * This simply means that the Bin will have the bin folder path prepended to it.
     * 
     * @param b - the Bin to have "binned."
     * @return a String with the "binned" result.
     */
    public static String binned(Bin b) {
        return binned(b.toBin());
    }
    
    /**
     * Returns the passed String, but "binned."
     * This simply means that the String will have the bin folder path prepended to it.
     * 
     * @param s - the String to have "binned."
     * @return a String with the "binned" result.
     */
    public static String binned(String s) {
        return new StringBuilder(Initializer.APP_BIN_FOLDER).append("/").append(s).toString();
    }
    
    /**
     * Executes a command with all of the passed String arguments.
     * 
     * @param args - one or more String arguments for the command.
     * @return an int which represents the exit value for the command execution. A
     *         value of <code>0</code> means the command executed successfully, but
     *         it does not necessarily imply that the command produced the desired
     *         result.
     * @throws InterruptedException if the command execution is interrupted.
     * @throws IOException          if there is an input and/or output error during
     *                              command execution.
     */
    public static int exec(String... args) throws InterruptedException, IOException {
        // Return -1 if arguments are null, else execute command.
        return (args == null ? -1 :
            new ProcessBuilder(args).redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT).start().waitFor());
    }
    
    /**
     * Executes a command with all of the passed String arguments. This method also
     * fetches the entire String output and returns it. However, if the execution
     * failed, then this method will return <code>null</code>. Keep in mind that an
     * empty String is technically still possible after <b>successfully</b>
     * executing a command, depending on the binary and the command arguments. Some
     * commands may not print anything to the console. Therefore, a
     * <code>null</code> return is the only reliable indicator of a command
     * succeeding.
     * 
     * @param redirError - a boolean for if the output should include error prints.
     * @param args - the array of String arguments for command execution.
     * @return a String with the output from the command, or <code>null</code> if
     *         command execution failed or threw an error.
     */
    public static String execAndFetch(boolean redirError, String... args) {
        // Do nothing if parameter is null.
        if (args == null)
            return null;
        
        final AtomicBoolean succeeded = new AtomicBoolean(false);
        final StringBuilder processOut = new StringBuilder();
        final ProcessBuilder processBuilder = new ProcessBuilder(args).redirectErrorStream(redirError);
        
        // Attempt command execution and store each line
        CFExec.run((BinRunnable) () -> {
            final Process process = processBuilder.start();
            
            // Iterate over each line from the process and store it.
            try (final InputStreamReader isr = new InputStreamReader(process.getInputStream()); final BufferedReader reader = new BufferedReader(isr);) {
                reader.lines().forEach(e -> processOut.append(e).append("\n"));
            } catch (IOException ioe) { ioe.printStackTrace(); }
            
            // Wait for process to complete and set if succeeded.
            if (process.waitFor() == 0) succeeded.set(true);
        }).excepts((i, ex) -> System.err.println("Unexpected Exception occurred during binary execAndFetch: " + ex.getMessage()));
        
        // Return result if command succeeded; null otherwise.
        return (succeeded.get() ? processOut.toString().trim() : null);
    }
    
    /**
     * Executes a command with all of the passed String arguments. This method also
     * fetches the entire String output and returns it safely. The unsafe version of
     * this method, {@link #execAndFetch(String...)}, can return <code>null</code>
     * if the command execution failed or threw an error. However, this method
     * serves as a wrapper and instead returns an empty string in this case.
     * Therefore, this method cannot return <code>null</code>. At worst, it will
     * return an empty String. If the delineation is needed between command
     * execution failure and successful command execution with no output, then use
     * {@link #execAndFetch(String...)}.
     * 
     * @param redirError - a boolean for if the output should include error prints.
     * @param args       - the array of String arguments for command execution.
     * @return a String with the output from the command, or an empty String if
     *         command execution failed or threw an error.
     */
    public static String execAndFetchSafe(boolean redirError, String... args) {
        return Objects.toString(Binaries.execAndFetch(redirError, args), "");
    }
    
    /**
     * Checks if the passed Bin exists in the app's bin folder.
     * 
     * @param b - the Bin to check for.
     * @return <code>true</code> if the binary exists; <code>false</code> otherwise.
     */
    public static boolean exists(Bin b) {
        return new File(binned(b)).exists();
    }
    
    /**
     * Checks if the passed Bin exists on the system.
     * 
     * @param b - the Bin to check for.
     * @return <code>true</code> if the binary exists; <code>false</code> otherwise.
     * @throws InterruptedException if the process checking for the binary is
     *                              interrupted.
     * @throws IOException          if there is an input and/or output error while
     *                              checking for the binary on the system.
     */
    public static boolean existsOnSys(Bin b) throws InterruptedException, IOException {
        return (Binaries.exec(b.exeless(), (b == Bin.FFMPEG || b == Bin.IMGMAGICK) ? "-version" : "--version") == 0);
    }
    
    /**
     * Extracts the passed Bin into the application bin folder. Additionally, this
     * method will extract the passed Bin's license or other necessary accompanying
     * files.
     * <p>
     * <b>Note:</b> This method, if called, will overwrite existing files if
     * necessary.
     * 
     * @param b - the Bin to extract.
     * @return <code>true</code> if the binary exists after attempting extraction;
     *         <code>false</code> otherwise.
     * @throws IOException if there's an input or output error during extraction.
     */
    public static boolean extract(Bin b) throws IOException {
        System.out.println("<!> Extracting bin: " + b.exeless() + "...");
        final StringBuilder pathIn  = new StringBuilder("/dev/mwhitney/resources/bin/");
        final StringBuilder pathOut = new StringBuilder(Initializer.APP_BIN_FOLDER).append("/");
        
        // Binary extraction
        switch (b) {
        case FFMPEG:
            pathIn.append("ffmpeg/ffmpeg.zip");
            pathOut.append("ffmpeg");
            break;
        case IMGMAGICK:
            pathIn.append("magick/");
            pathOut.append("imagemagick/");
            break;
        default:
            break;
        }
        
        new File(pathOut.toString()).mkdirs();
        
        // License and more -- return if N/A
        switch(b) {
        case FFMPEG    -> UnzipUtility.unzip(Initializer.class.getResourceAsStream(pathIn.toString()), pathOut.toString());
        case IMGMAGICK -> {
            Files.copy(Initializer.class.getResourceAsStream(pathIn.toString() + b),
                    Paths.get(Binaries.binned(b)), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Initializer.class.getResourceAsStream(pathIn.toString() + "LICENSE.txt"),
                    Paths.get(pathOut.toString() + "LICENSE.txt"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Initializer.class.getResourceAsStream(pathIn.toString() + "NOTICE.txt"),
                    Paths.get(pathOut.toString() + "NOTICE.txt"), StandardCopyOption.REPLACE_EXISTING);
        }
        default -> Files.copy(Initializer.class.getResourceAsStream(pathIn.toString() + b),
                Paths.get(Binaries.binned(b)), StandardCopyOption.REPLACE_EXISTING);
        }
        return exists(b);
    }
    
    /**
     * Attempts to update the passed binary.
     * If the command succeeds, then this method returns <code>true</code>.
     * 
     * @param b - a Bin for the binary to update.
     * @return <code>true</code> if the command succeeds; <code>false</code> otherwise.
     * @throws IOException if there's an input or output error.
     * @throws InterruptedException if the command is interrupted.
     * @throws UnsupportedBinActionException if the passed Bin cannot be updated in this way.
     */
    public static BinUpdateResult update(Bin b) throws IOException, InterruptedException, UnsupportedBinActionException {
        switch(b) {
        case YT_DLP:
        case GALLERY_DL:
            // Execute update command and retrieve command result.
            final String cmdResult = Binaries.execAndFetch(true, Binaries.binned(b), "-U");
            if (cmdResult == null) return new BinUpdateResult(b, false);
            
            // Check if updated at all and determine version retrieval regex.
            final boolean didUpdate = !cmdResult.contains("up to date");
            Pattern regex = null;
            if (b == Bin.YT_DLP) {
                if (didUpdate) regex = Pattern.compile(".*(?:Current version: )([a-zA-Z0-9\\.@]+).*(?:Latest version: )([a-zA-Z0-9\\.@]+).*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                else           regex = Pattern.compile(".*(?:Latest version: )([a-zA-Z0-9\\.@]+).*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            } else {
                if (didUpdate) regex = Pattern.compile(".*(?:Updating from )([a-zA-Z0-9\\\\.]+)(?: to )([a-zA-Z0-9\\\\.]+).*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                else           regex = Pattern.compile(".*(?:up to date \\()([a-zA-Z0-9\\.]+)(?:\\)).*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            }
            
            // Retrieve version number(s) using regex and return binary update result.
            final String oldVersion = regex.matcher(cmdResult).replaceAll("$1");
            final String newVersion = (didUpdate ? regex.matcher(cmdResult).replaceAll("$2") : oldVersion);
            return new BinUpdateResult(b, true, oldVersion, newVersion);
        default:
            throw new UnsupportedBinActionException("That binary cannot be updated automatically or OTA.");
        }
    }
    
    /**
     * Attempts to update all binaries which can be updated remotely. After all
     * binaries are done checking for updates, this method returns a String with the
     * results.
     * 
     * @return a String with the update results.
     */
    public static String updateAll() {
        // Initialize array, matches amount of binaries which can be updated.
        final BinUpdateResult[] results = new BinUpdateResult[2];
        
        // Check for Updates on Each Applicable Bin
        CFExec.run((BinRunnable) () -> { if (Binaries.exists(Bin.YT_DLP))     results[0] = Binaries.update(Bin.YT_DLP); },
                   (BinRunnable) () -> { if (Binaries.exists(Bin.GALLERY_DL)) results[1] = Binaries.update(Bin.GALLERY_DL); })
              .excepts((i, ex) -> results[i] = new BinUpdateResult(i == 0 ? Bin.YT_DLP : Bin.GALLERY_DL, false));
        
        // Form Result
        final StringBuilder out = new StringBuilder();
        for (final BinUpdateResult result : results) {
            if (result == null) continue;
            if (!out.isEmpty()) out.append("\n- ");
            else                out.append("- ");
            out.append(result.toString());
        }
        return out.toString();
    }
    
    /**
     * Refreshes the <code>boolean</code>s keeping track of whether or not each
     * binary exists on the system. This method will run
     * <code>existsOnSys(Bin)</code> for each binary and update the
     * <code>Binaries.HAS_[BINARY]</code> value. To check if an individual binary
     * exists on the system, use <code>existsOnSys(Bin)</code>. That method alone
     * will not updates the public values.
     * <p>
     * This method will attempt to run all checks concurrently. Therefore, the
     * method should take much less time to execute and complete. Otherwise, the
     * function would go one by one, checking for each binary's existence in order.
     * Asynchronously checking all of them simultaneously minimizes the runtime of
     * the function such that it is comparable to running a single binary existence
     * check.
     * 
     * @throws InterruptedException if the process checking for any of the
     *                              binaries on the system was interrupted.
     */
    public static void refreshOnSys() throws InterruptedException {
        // Run all asynchronously, leading to (basically) simultaneous execution.
        CFExec.run((BinRunnable) () -> Binaries.HAS_YTDLP     = (Binaries.HAS_YTDLP     || existsOnSys(Bin.YT_DLP)),
                   (BinRunnable) () -> Binaries.HAS_GALLERYDL = (Binaries.HAS_GALLERYDL || existsOnSys(Bin.GALLERY_DL)),
                   (BinRunnable) () -> Binaries.HAS_FFMPEG    = (Binaries.HAS_FFMPEG    || existsOnSys(Bin.FFMPEG)),
                   (BinRunnable) () -> Binaries.HAS_IMGMAGICK = (Binaries.HAS_IMGMAGICK || existsOnSys(Bin.IMGMAGICK)))
              .excepts((i, ex) -> System.err.println("Exception occurred while refreshing system binaries: " + ex.getMessage()))
              .throwAny(InterruptedException.class);
    }
    
    /**
     * Sets the PropertyListener used for checking property states.
     * 
     * @param pl - the PropertyListener to use.
     */
    public static void setPropertyListener(PropertyListener pl) {
        Binaries.propertyListener = pl;
    }
}
