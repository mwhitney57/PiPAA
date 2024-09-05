package dev.mwhitney.update.api;

import java.io.Serializable;

import org.jetbrains.annotations.NotNull;

/**
 * A simple wrapper for a multi-dot version number, such as <code>1.4.3</code>.
 * Each integer is stored individually, ordered by <code>major</code>,
 * <code>middle</code>, and <code>minor</code>. Using {@link Version#toString()}
 * will perform as expected and return a Version in a simple, readable String
 * format.
 * 
 * @author mwhitney57
 */
public record Version(
    @NotNull
    Integer majorVersion,
    @NotNull
    Integer middleVersion,
    @NotNull
    Integer minorVersion
) implements Serializable {
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other instanceof Version otherVersion) {
            if (this.majorVersion() == otherVersion.majorVersion()
            && this.middleVersion() == otherVersion.middleVersion()
            && this.minorVersion()  == otherVersion.minorVersion())
                return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return this.majorVersion + "." + this.middleVersion + "." + this.minorVersion;
    }
    
    /**
     * Checks if this Version is newer than the passed Version.
     * 
     * @param other - the Version to check against.
     * @return <code>true</code> if <b>this</b> Version is newer than the passed
     *         Version; <code>false</code> otherwise.
     */
    public boolean newerThan(@NotNull Version other) {
        boolean newer = false;
        
        // Ex: 2.2.3 > 1.2.3
        if (this.majorVersion > other.majorVersion())        newer = true;
        // Ex: 1.4.3 > 1.2.3
        else if (this.majorVersion.equals(other.majorVersion()) &&
                 this.middleVersion > other.middleVersion()) newer = true;
        // Ex: 1.2.5 > 1.2.3
        else if (this.middleVersion.equals(other.middleVersion()) &&
                 this.minorVersion > other.minorVersion())   newer = true;
        return newer;
    }
    
    /**
     * Checks if this Version is older than the passed Version. This method is
     * shorthand for inversely calling {@link #newerThan(Version)}.
     * 
     * @param other - the Version to check against.
     * @return <code>true</code> if <b>this</b> Version is older than the passed
     *         Version; <code>false</code> otherwise.
     */
    public boolean olderThan(@NotNull Version other) {
        return other.newerThan(this);
    }
    
    /**
     * Forms a new Version instance using the passed Strings. Each String is
     * intended to be solely comprised of an integer. Conversion is done
     * automatically by this method using {@link Integer#valueOf(String)}.
     * 
     * @param majV - the major version. Ex: <b>1</b>.4.3
     * @param midV - the middle version. Ex: 1.<b>4</b>.3
     * @param minV - the minor version. Ex: 1.4.<b>3</b>
     * @return the new Version instance.
     */
    public static Version form(@NotNull String majV, @NotNull String midV, @NotNull String minV) {
        return new Version(Integer.valueOf(majV), Integer.valueOf(midV), Integer.valueOf(minV));
    }

    /**
     * Forms a new Version instance using the passed String. The String is intended
     * to be in the format: <code>1.4.3</code>
     * <p>
     * The String is split and each individual String is converted to an integer
     * before creating the Version instance.
     * 
     * @param ver - the total version String . Ex: 1.4.3
     * @return the new Version instance.
     */
    public static Version form(@NotNull String ver) {
        final String[] parts = ver.trim().split("\\.");
        return form(parts[0], parts[1], parts[2]);
    }
}
