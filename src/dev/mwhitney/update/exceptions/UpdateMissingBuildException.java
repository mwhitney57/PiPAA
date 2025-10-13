package dev.mwhitney.update.exceptions;

/**
 * An extension upon {@link PiPUpdateException} that is thrown when an available
 * release is missing a {@link Build}, or when one couldn't be derived from the
 * API data.
 * <p>
 * Unless there are significant changes to the update process code, this error
 * should indicate an API or application code issue.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public class UpdateMissingBuildException extends PiPUpdateException {
    /** The randomly-generated serial ID. */
    private static final long serialVersionUID = 3694892993673116280L;

    /**
     * Creates a new {@link UpdateMissingBuildException}.
     */
    public UpdateMissingBuildException() {
        super("Unexpected error while fetching latest update. No Build received in payload.");
    }
    
    @Override
    public String getUserFriendlyErrorMessage() {
        return "Couldn't get update build! Contact if this persists.";
    }
}
