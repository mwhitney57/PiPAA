package dev.mwhitney.update;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.function.BooleanSupplier;

import javax.swing.JOptionPane;

import org.jetbrains.annotations.NotNull;

import dev.mwhitney.exceptions.PiPUpdateException;
import dev.mwhitney.main.Binaries;
import dev.mwhitney.main.Initializer;
import dev.mwhitney.main.PiPProperty.FREQUENCY_OPTION;
import dev.mwhitney.main.PiPProperty.PropDefault;
import dev.mwhitney.main.PiPProperty.TYPE_OPTION;
import dev.mwhitney.update.api.APICommunicator;
import dev.mwhitney.update.api.APICommunicator.UpdatePayload;
import dev.mwhitney.update.api.Version;

/**
 * Handles updates for the application binaries and the application itself (as a whole).
 * Results are returned as {@link PiPUpdateResult} records.
 * 
 * @author mwhitney57
 */
public class PiPUpdater {
    /**
     * An update result record with a few basic attributes relating to how the
     * update process went.
     * 
     * @author mwhitney57
     */
    public static class PiPUpdateResult {
        /** If an update check was performed at all. */
        private boolean checked;
        /** If the user was asked if they wanted to update. */
        private boolean userPrompted;
        /** If the updater tried updating. */
        private boolean triedUpdating;
        /** If the updater completed the update process. Does <b>not</b> guarantee that said process succeeded. */
        private boolean updated;
        /** Any exception thrown during the update process. */
        private PiPUpdateException exception;
        
        /** Creates a new update result, with all default <code>false</code> or <code>null</code>. */
        public PiPUpdateResult() {
            this(false, false, false, false, null);
        }
        /** Creates a new update result with the passed boolean attribute values. */
        private PiPUpdateResult(boolean checked, boolean userPrompted, boolean triedUpdating, boolean updated) {
            this(checked, userPrompted, triedUpdating, updated, null);
        }
        /** Creates a new update result with the passed boolean attribute values and the passed exception. */
        public PiPUpdateResult(boolean checked, boolean userPrompted, boolean triedUpdating, boolean updated, PiPUpdateException e) {
            this.checked = checked;
            this.userPrompted = userPrompted;
            this.triedUpdating = triedUpdating;
            this.updated = updated;
            this.exception = e;
        }
        /* Checked Attribute */
        /** Whether or not an update check was performed at all. */
        public boolean checked() { return checked; }
        /** Set whether or not an update check was performed. */
        public PiPUpdateResult setChecked(boolean checked) {
            this.checked = checked;
            return this;
        }
        /* User Prompted Attribute */
        /** Whether or not the user was asked if they wanted to update. */
        public boolean userPrompted() { return userPrompted; }
        /** Sets whether or not the user was asked if they wanted to update. */
        public PiPUpdateResult setUserPrompted(boolean userPrompted) {
            this.userPrompted = userPrompted;
            return this;
        }
        /* Tried Updating Attribute */
        /** Whether or not the updater tried updating. */
        public boolean triedUpdating() { return triedUpdating; }
        /** Sets whether or not the update tried updating. */
        public PiPUpdateResult setTriedUpdating(boolean triedUpdating) {
            this.triedUpdating = triedUpdating;
            return this;
        }
        /* Updated Attribute */
        /** Whether or not the updater completed the update process, which does not guarantee the process succeeded. */
        public boolean updated() { return updated; }
        /** Sets whether or not the updater completed the update process, which does not guarantee the process succeeded. */
        public PiPUpdateResult setUpdated(boolean updated) {
            this.updated = updated;
            return this;
        }
        /* Exception */
        /** Did an exception occur during the update process? */
        public boolean hasException() { return (this.exception != null); }
        /** Get the exception that occurred during the update process, if there is one. */
        public PiPUpdateException exception() { return exception; }
        /** Set the exception that occurred during the update process. */
        public PiPUpdateResult setException(PiPUpdateException exception) {
            this.exception = exception;
            return this;
        }
    }
    
    /** A boolean that is only <code>true</code> when performing or checking for updates. */
    public static volatile boolean APP_UPDATING, BIN_UPDATING = false;
    
    /**
     * Checks if the updater should run based on the passed {@link FREQUENCY_OPTION}
     * and a String with the last time an update check was performed.
     * 
     * @param frequency - the {@link FREQUENCY_OPTION} which determines how often
     *                  updates should be checked for.
     * @param lastCheck - a String with the last time an update check was performed.
     * @return <code>true</code> if the updater should run; <code>false</code>
     *         otherwise.
     */
    private static boolean shouldUpdate(FREQUENCY_OPTION frequency, String lastCheck) {
        if (frequency == null) return true;
        
        final LocalDateTime now = LocalDateTime.now();
        LocalDateTime last = null;
        try {
            last = (lastCheck != null ? LocalDateTime.parse(lastCheck) : null);
        } catch (DateTimeParseException dtpe) { System.err.println("Last update check's LocalDateTime invalid: Using default if necessary."); }

        return switch (frequency) {
        case NEVER    -> false;
        case ALWAYS   -> true;
        case DAILY    -> (last == null || now.isAfter(last.plusDays(1))   ? true : false);
        case WEEKLY   -> (last == null || now.isAfter(last.plusWeeks(1))  ? true : false);
        case MONTHLY  -> (last == null || now.isAfter(last.plusMonths(1)) ? true : false);
        };
    }
    
    /**
     * Updates the entire application, depending on the passed parameters.
     * <p>
     * First, the method will use the passed <code>frequency</code> and
     * <code>lastCheck</code> Strings to determine if it should check for an
     * application update. If so, an API request will be made using the
     * {@link APICommunicator} using the passed {@link TYPE_OPTION}. A {@link Build}
     * will be retrieved and used for comparison. If one of the following conditions
     * are met, the user will be asked if they would like to update:
     * <pre>
     * - The update build is of the same type and the version is newer.
     * - The update build is NOT more stable, older, and of the same type as the least stable-allowed build AND...
     *      - The update build is less stable, but the version is newer.
     *      - The update build is more stable, but the version is older.
     * </pre>
     * If the update's version is newer than the current version, the user will be
     * prompted if they would like to update.
     * <p>
     * If all of the above conditions have been met and the user confirmed, the
     * {@link PiPUpdateResult#updated()} method will be return <code>true</code>.
     * The application will attempt to update itself automatically in this case, but
     * it must be closed in order to do so. <b>It is up to the caller of this method
     * to exit the application in a reasonable amount of time after the
     * {@link PiPUpdateResult} is returned.</b> If the application remains open, the
     * update attempt will eventually timeout.
     * 
     * @param frequency - a String for how often updates should be checked for.
     *                  Should match a {@link FREQUENCY_OPTION} or the default will
     *                  be used.
     * @param lastCheck - a String for when the last update check was performed.
     *                  Should be parsable by
     *                  {@link LocalDateTime#parse(CharSequence)} or this method may
     *                  default to checking for an update.
     * @param type      - a {@link TYPE_OPTION} for the update release type to check
     *                  for.
     * @param force     - a boolean for if the update prompt should forced to be shown.
     * @return a {@link PiPUpdateResult} with the update process results.
     * @see {@link #updateApp(TYPE_OPTION, boolean)} to assume the app should check for an update.
     */
    public static PiPUpdateResult updateApp(@NotNull String frequency, @NotNull String lastCheck, @NotNull TYPE_OPTION type, boolean force) {
        // Setup Result
        final PiPUpdateResult result = new PiPUpdateResult(
            shouldUpdate(PropDefault.FREQUENCY_APP.matchAny(frequency), lastCheck), false, false, false
        );

        // Should NOT Check for Update? Return Result Early
        if (!result.checked()) return result;
        
        try {
            // Get Latest Version from API
            final UpdatePayload update = APICommunicator.request(type);
            if (update == null)    return result.setException(new PiPUpdateException("Could not fetch latest update."));
            if (!update.hasLink()) return result.setException(new PiPUpdateException("Latest update contains no download link with current file extension."));
            
            // Compare Current Version to Version from API -- Get User Confirmation Before Proceeding
            final Version currVersion = Initializer.APP_BUILD.version();
            final StringBuilder prompt = new StringBuilder();
            final boolean updVerNewer =  update.build().version().newerThan(currVersion),
                    updVerSameOrNewer = !update.build().version().olderThan(currVersion);
            
            // Only update to a less stable type if the version is newer and the type configuration covers it.
            if (update.build().lessStableThan(Initializer.APP_BUILD) && updVerNewer && type.covers(update.build().type()))
                prompt.append("A newer, but less stable update is available!");
            // Always update to a more stable type, except when the new version is older and the current type isn't covered by type configuration.
            else if (update.build().moreStableThan(Initializer.APP_BUILD) && (updVerSameOrNewer || !type.covers(Initializer.APP_BUILD.type())))
                prompt.append("An older, but more stable update is available!");
            // Only update when the version is newer and the type is the same.
            else if (update.build().type() == Initializer.APP_BUILD.type() && updVerNewer)
                prompt.append("A new update is available!");
            // Prompt anyway if forced by passed boolean.
            else if (force)
                prompt.append("App update prompt forced.");
            
            // Set user prompted and prompt user.
            final BooleanSupplier userConfirm = () -> {
                result.setUserPrompted(true);
                return JOptionPane.showConfirmDialog(null, prompt.toString() + "\nCurrent: " + Initializer.APP_BUILD + "\nLatest: " + update.build()
                + "\n\nWould you like to update? PiPAA will restart automatically.", "PiPAA Update Available", JOptionPane.YES_NO_OPTION) == 0;
            };
            // Only process the update if the prompt was given a value and the user confirmed.
            final boolean processUpdate = ( !prompt.isEmpty() && userConfirm.getAsBoolean() );
            if (processUpdate) {
                // Attempt Update -- Determine Variables then Proceed with Execution
                result.setTriedUpdating(true);
                final long pid = ProcessHandle.current().pid();
                final String currExecPath  = new File(PiPUpdater.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
                final String finalExecCMD = currExecPath.toLowerCase().endsWith("jar") ? ("javaw -jar \"" + currExecPath + "\"") : ("start /B \"\" \"%windir%\\explorer.exe\" \"" + currExecPath + "\"");
                
                // Loop Sleep Until This Process has Closed --> Download New Version and Replace --> Run New Version
                final String args = "for /L %i in (1,1,15) do @tasklist /FI \"PID eq " + pid + "\" | find /c \"" + pid + "\" >nul || (curl -L --clobber -o \"" + currExecPath + "\" " + update.link() + " && " + finalExecCMD + " & exit) & timeout /T 1 >nul";
                new ProcessBuilder("cmd", "/C", args).redirectOutput(Redirect.DISCARD).redirectError(Redirect.DISCARD).start();
                result.setUpdated(true);
            }
        } catch (Exception e) { result.setException(new PiPUpdateException("Update process failed unexpectedly.", e)); }
        return result;
    }
    
    /**
     * Updates the entire application, depending on the passed parameters.
     * <p>
     * This method automatically assumes an update should be checked for. An API
     * request will be made using the {@link APICommunicator} using the passed
     * {@link TYPE_OPTION}. A {@link Build} will be retrieved and used for
     * comparison. If one of the following conditions are met, the user will be
     * asked if they would like to update:
     * <pre>
     * - The update build is of the same type and the version is newer.
     * - The update build is NOT more stable, older, and of the same type as the least stable-allowed build AND...
     *      - The update build is less stable, but the version is newer.
     *      - The update build is more stable, but the version is older.
     * </pre>
     * If the update's version is newer than the current version, the user will be
     * prompted if they would like to update.
     * <p>
     * If all of the above conditions have been met and the user confirmed, the
     * {@link PiPUpdateResult#updated()} method will be return <code>true</code>.
     * The application will attempt to update itself automatically in this case, but
     * it must be closed in order to do so. <b>It is up to the caller of this method
     * to exit the application in a reasonable amount of time after the
     * {@link PiPUpdateResult} is returned.</b> If the application remains open, the
     * update attempt will eventually timeout.
     * 
     * @param type  - a {@link TYPE_OPTION} for the update release type to check
     *              for.
     * @param force - a boolean for if the update prompt should forced to be shown.
     * @return a {@link PiPUpdateResult} with the update process results.
     * @see {@link #updateApp(String, String, TYPE_OPTION, boolean)} to have the
     *      method also calculate if the app should check for an update.
     */
    public static PiPUpdateResult updateApp(@NotNull TYPE_OPTION type, boolean force) {
        return updateApp(null, null, type, force);
    }
    
    /**
     * Updates the application's binaries which support over-the-air (OTA) updating.
     * This includes {@link Binaries.Bin#YT_DLP} and
     * {@link Binaries.Bin#GALLERY_DL}.
     * <p>
     * If either of the passed parameters is <code>null</code>, then the method will
     * check for an update.
     * 
     * @param frequency - how often updates should be checked for.
     * @param lastCheck - when the last update check was performed.
     * @return <code>true</code> if an update check was performed, even if nothing
     *         was updated; <code>false</code> otherwise.
     */
    public static boolean updateBin(@NotNull String frequency, @NotNull String lastCheck) {
        if (shouldUpdate(PropDefault.FREQUENCY_BIN.matchAny(frequency), lastCheck)) {
            // Run then Print Result
            System.out.println("Automatic binary update check results: \n" + Binaries.updateAll().toString());
            return true;
        }
        return false;
    }
}
