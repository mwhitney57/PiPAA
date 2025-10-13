package dev.mwhitney.update.exceptions;

import dev.mwhitney.update.api.Artifact;

/**
 * An extension upon {@link PiPUpdateException} that is thrown when the current,
 * running instance has an unknown extension. The application expects a value
 * from {@link Artifact}.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public class UpdateUnknownArtifactException extends PiPUpdateException {
    /** The randomly-generated serial ID. */
    private static final long serialVersionUID = 8323666731937683034L;

    /**
     * Creates a new {@link UpdateUnknownArtifactException}.
     */
    public UpdateUnknownArtifactException() {
        super("Current PiPAA instance is running via an unknown artifact.");
    }
    
    @Override
    public String getUserFriendlyErrorMessage() {
        return "Can't update! Unrecognized PiPAA build extension.";
    }
}
