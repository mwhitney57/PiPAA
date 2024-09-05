package dev.mwhitney.update.api;

import dev.mwhitney.main.PiPProperty.TYPE_OPTION;

/**
 * A specific build or update of the application, containing a {@link Version}
 * and {@link TYPE_OPTION}.
 * 
 * @author mwhitney57
 */
public record Build(
        Version version,
        TYPE_OPTION type
) {
    /**
     * Checks if this Build is considered more stable than the passed instance.
     * To be considered more stable, this method first checks each Build's
     * {@link TYPE_OPTION} and uses the comparison method {@link TYPE_OPTION#stablerThan(TYPE_OPTION)}.
     * This method will immediately return <code>true</code> if that comparison is <code>true</code>.
     * <p>
     * Otherwise, the versions will be compared to determine which is the newest, as newer builds of the same type are likely to be more stable.
     * 
     * @param other - the other Build instance to compare against.
     * @return <code>true</code> if this Build is more stable than the passed instance; <code>false</code> otherwise.
     */
    public boolean moreStableThan(Build other) {
        // Return true if this build is stabler, even if the version is older.
        if (this.type.stablerThan(other.type())) return true;
        else if (this.type == other.type()) return this.version.newerThan(other.version());
        else return false;
    }
    
    /**
     * Checks if this Build is considered less stable than the passed instance. This
     * method is shorthand for inversely calling {@link #moreStableThan(Build)}. See
     * that method for more documentation.
     * 
     * @param other - the other Build instance to compare against.
     * @return <code>true</code> if this Build is less stable than the passed
     *         instance; <code>false</code> otherwise.
     */
    public boolean lessStableThan(Build other) {
        return (other == null ? false : other.moreStableThan(this));
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other instanceof Build otherBuild) {
            if (this.version.equals(otherBuild.version())
            &&  this.type == otherBuild.type())
                return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return this.version.toString() + "-" + this.type.toString();
    }
}
