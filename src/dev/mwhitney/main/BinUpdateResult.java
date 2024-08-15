package dev.mwhitney.main;

import java.util.Objects;

import dev.mwhitney.main.Binaries.Bin;

/**
 * The result of an attempt to update an application binary.
 * 
 * @author mwhitney57
 */
public final class BinUpdateResult {
    /** The {@link Bin} attempted to be updated. */
    private Bin bin;
    /**
     * A boolean for whether or not the update command was successful.
     * This is not to be confused with whether or not the binary was actually updated.
     */
    private boolean success;
    /** The version of the binary before the update attempt. */
    private String oldVersion;
    /** The version of the binary after the update attempt. */
    private String newVersion;
    
    /**
     * Creates a new BinUpdateResult with the passed {@link Bin} and success
     * boolean. This constructor automatically uses <code>null</code> values for
     * both the before and after versions.
     * 
     * @param b       - the {@link Bin} which was attempted to be updated.
     * @param success - a boolean for whether or not the update attempt succeeded.
     * @see #BinUpdateResult(Bin, boolean, String, String)
     */
    public BinUpdateResult(Bin b, boolean success) {
        this(b, success, null, null);
    }
    
    /**
     * Creates a new BinUpdateResult with the passed {@link Bin}, success boolean,
     * and before and after version Strings.
     * 
     * @param b          - the {@link Bin} which was attempted to be updated.
     * @param success    - a boolean for whether or not the update attempt
     *                   succeeded.
     * @param oldVersion - a String with the version before the update attempt.
     * @param newVersion - a String with the version after the update attempt.
     * @see #BinUpdateResult(Bin, boolean)
     */
    public BinUpdateResult(Bin b, boolean success, String oldVersion, String newVersion) {
        this.bin = b;
        this.success = success;
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
    }
    
    /**
     * Gets whether the update attempt succeeded. <b>Not to be confused with whether
     * or not any update actually occurred.</b>
     * 
     * @return <code>true</code> if the attempt was successful; <code>false</code>
     *         otherwise.
     */
    public final boolean success() {
        return this.success;
    }
    
    /**
     * Gets the previous version of the binary before the update attempt, which may
     * ultimately be the same as the {@link #newVersion()} if the binary was already
     * up-to-date.
     * 
     * @return a String with the old version, or <code>null</code> if not specified.
     */
    public final String oldVersion() {
        return this.oldVersion;
    }
    
    /**
     * Gets the new version of the binary before the update attempt, which may
     * ultimately be the same as the {@link #oldVersion()} if the binary was already
     * up-to-date.
     * 
     * @return a String with the new version, or <code>null</code> if not specified.
     */
    public final String newVersion() {
        return this.newVersion;
    }
    
    /**
     * Checks if the update attempt resulted in any change to the before and after
     * versions.
     * 
     * @return <code>true</code> if the {@link #oldVersion()} and
     *         {@link #newVersion()} are non-<code>null</code> and differ;
     *         <code>false</code> otherwise.
     */
    public final boolean updated() {
        return (this.oldVersion != null && this.newVersion != null && !this.oldVersion.equals(this.newVersion));
    }
    
    /**
     * Gets a String representation of this {@link BinUpdateResult}.
     * 
     * @return a String with the representation of this {@link BinUpdateResult}.
     */
    @Override
    public String toString() {
        if (!success) return Objects.toString(bin, "null") + " failed to update.";
        if (success && newVersion != null && newVersion.equals(oldVersion)) return (bin != null ? bin.exeless() : "null") + " is already up-to-date (" + newVersion + ")";
        return String.format("%s updated from %s to %s",
                bin != null ? bin.exeless() : "null",
                Objects.toString(oldVersion, "null"),
                Objects.toString(newVersion, "null"));
    }
}