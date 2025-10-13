package dev.mwhitney.update.exceptions;

import java.util.Objects;

import dev.mwhitney.update.api.Artifact;

/**
 * An extension upon {@link PiPUpdateException} that is thrown when an available
 * release is missing a necessary artifact for the update process.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public class UpdateMissingArtifactException extends PiPUpdateException {
    /** The randomly-generated serial ID. */
    private static final long serialVersionUID = -8115955984335193677L;
    
    /** The required {@link Artifact} which was missing from the desired update release. */
    private final Artifact artifact;
    
    /**
     * Creates a new {@link UpdateMissingArtifactException}.
     * 
     * @param artifact - the {@link Artifact} that was missing from the update.
     */
    public UpdateMissingArtifactException(Artifact artifact) {
        super("Update contains no download link with current file extension.");
        
        this.artifact = artifact;
    }
    
    @Override
    public String getUserFriendlyErrorMessage() {
        return "Update is missing the required PiPAA " + Objects.requireNonNullElse(this.artifact, "artifact") + "!";
    }
}
