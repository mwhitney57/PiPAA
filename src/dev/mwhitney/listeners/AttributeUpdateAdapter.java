package dev.mwhitney.listeners;

/**
 * An adapter for the {@link AttributeUpdateListener}.
 * 
 * @author mwhitney57
 */
public abstract class AttributeUpdateAdapter implements AttributeUpdateListener {
    @Override
    public void allUpdated() {}
    @Override
    public void titleUpdated(String title) {}
}
