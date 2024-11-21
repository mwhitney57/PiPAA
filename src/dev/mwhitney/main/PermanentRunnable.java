package dev.mwhitney.main;

/**
 * An extension upon {@link RecurringRunnable} which is meant to exist
 * permanently, so long as the object it is associated with exists. Therefore,
 * be cautious with application and consider using {@link RecurringRunnable}
 * instead.
 * 
 * @author mwhitney57
 */
public interface PermanentRunnable extends RecurringRunnable {}
