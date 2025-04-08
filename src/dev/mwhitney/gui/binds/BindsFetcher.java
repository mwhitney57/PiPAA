package dev.mwhitney.gui.binds;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * A simple interface for fetching the current keyboard and mouse shortcut binds.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public interface BindsFetcher {
    /**
     * Gets a map with the current keyboard binds.
     * 
     * @return a {@link ConcurrentHashMap} with the keyboard binds.
     */
    public ConcurrentHashMap<KeyInput, ConcurrentSkipListMap<Integer, BindDetails<KeyInput>>> getKeyBinds();
    /**
     * Gets a map with the current mouse binds.
     * 
     * @return a {@link ConcurrentHashMap} with the mouse binds.
     */
    public ConcurrentHashMap<MouseInput, ConcurrentSkipListMap<Integer, BindDetails<MouseInput>>> getMouseBinds();
}
