package dev.mwhitney.update.api;

import dev.mwhitney.properties.PiPProperty.TYPE_OPTION;

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
