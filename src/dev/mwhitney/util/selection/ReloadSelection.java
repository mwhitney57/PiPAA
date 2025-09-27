package dev.mwhitney.util.selection;

import org.apache.commons.lang3.ArrayUtils;

import dev.mwhitney.gui.PiPWindowSnapshot.SnapshotData;
import dev.mwhitney.util.interfaces.PiPEnum;

/**
 * A selection which dictates how a reload should operate.
 * 
 * This class acts as a storage and management layer for two types of data:
 * <li>The {@link ReloadSelections} pick for how the reload should operate in
 * general.</li>
 * <li>The {@link SnapshotData} pertaining to that selection, which could be a
 * custom set of data if {@link ReloadSelections#CUSTOM} is selected.</li>
 * <p>
 * To use this class, simply call a constructor and pass one of the
 * {@link ReloadSelections}. If you intend to use
 * {@link ReloadSelections#CUSTOM}, then use the
 * {@link #ReloadSelection(SnapshotData...)} constructor and pass the data to
 * maintain after reload.
 * <p>
 * The technical details of how this class works are simple. The
 * {@link ReloadSelections} pick will dictate the {@link SnapshotData} that
 * should be kept after the reload. Instead, a custom set of
 * {@link SnapshotData} can be provided which will automatically infer the
 * {@link ReloadSelections#CUSTOM} option.
 * 
 * @author mwhitney57
 * @since 0.9.4
 */
public class ReloadSelection {
    /**
     * An enum of {@link Selections} available for use during a reload
     * operation.
     * 
     * @author mwhitney57
     * @since 0.9.4
     */
    public enum ReloadSelections implements PiPEnum<ReloadSelections>, Selections<ReloadSelections> {
        /** A regular reload which does not preserve any data. */
        REGULAR,
        /** A quick reload which preserves a lot of data for the sake of speed. */
        QUICK,
        /** A custom reload which preserves specific data provided alongside it. */
        CUSTOM;

        @Override
        public ReloadSelections[] getSelections() {
            return ReloadSelections.values();
        }
    };
    
    /** The type of reload to perform. There are a few presets, or {@link #CUSTOM} selections can be made. */
    private ReloadSelections selection = ReloadSelections.REGULAR;
    /** The specific {@link SnapshotData} to keep after the reload operation. */
    private SnapshotData[] data;
    
    /**
     * Creates a ReloadSelection with the default of {@link #REGULAR}.
     */
    public ReloadSelection() {
        this(ReloadSelections.REGULAR);  // Default to REGULAR.
    }
    
    /**
     * Creates a ReloadSelection using the passed {@link RegularSelections}.
     * <p>
     * <b>Note:</b> {@link #CUSTOM} cannot be used with this constructor and would
     * default to {@link #REGULAR}, as no data is specified. Call
     * {@link #ReloadSelections(SnapshotData...)} instead.
     */
    public ReloadSelection(ReloadSelections selection) {
        // Tie out-of-bounds values to REGULAR. Otherwise use valid selection value.
        if (selection == null || selection == ReloadSelections.CUSTOM) this.selection = ReloadSelections.REGULAR;
        else this.selection = selection;
        // Set the data based on the type.
        setData(
            switch (selection) {                     // No Case for Custom -- Uses Other Constructor
            case QUICK  -> SnapshotData.ALL;         // Quick
            default -> SnapshotData.MEDIA_SOURCES;   // Regular/Default
            }
        );
    }
    
    /**
     * Create a ReloadSelection with custom {@link SnapshotData}. This constructor
     * is used with the {@link #CUSTOM} type, and it is automatically inferred
     * during construction.
     * 
     * @param data - one or more selections of {@link SnapshotData}.
     */
    public ReloadSelection(SnapshotData... data) {
        this.selection = ReloadSelections.CUSTOM;
        setData(data);
    }
    
    /**
     * Sets the {@link SnapshotData}.
     * 
     * @param data - one or more selections of {@link SnapshotData}.
     */
    private void setData(SnapshotData... data) {
        this.data = data;
    }
    
    /**
     * Checks if there is any data.
     * This method exists for safety, but it should almost always return <code>true</code>.
     * 
     * @return <code>true</code> if internal data is stored; <code>false</code> otherwise.
     */
    public boolean hasData() {
        return this.data != null && this.data.length > 0;
    }
    
    /**
     * Gets the raw internal {@link SnapshotData}.
     * 
     * @return the raw {@link SnapshotData} array.
     */
    public SnapshotData[] rawData() {
        return this.data;
    }
    
    /**
     * Gets the internal {@link SnapshotData} after passing it through a filter. The
     * filter removes particular data in cases where the passed boolean is
     * <code>false</code>.
     * 
     * @param usingVLC - a boolean for whether or not the VLC player is being used.
     * @return the {@link SnapshotData} array.
     */
    public SnapshotData[] filteredData(boolean usingVLC)  {
        // QUICK: Not using VLC, so don't include them in the returned data.
        if (this.selection == ReloadSelections.QUICK && !usingVLC) return ArrayUtils.removeElements(SnapshotData.values(), SnapshotData.ALL, SnapshotData.PLAYER);
        // Everything else: Return the raw data.
        return this.data;
    }

    /**
     * Indicates whether media information should be applied based on the type of reload.
     * 
     * @return <code>true</code> if it should be applied; <code>false</code> otherwise.
     */
    public boolean shouldApplyMedia() {
        return (selection == ReloadSelections.QUICK || (selection == ReloadSelections.CUSTOM && hasData()
                && (SnapshotData.MEDIA_SOURCES.in(this.data) || SnapshotData.MEDIA_ATTRIBUTES.in(this.data))));
    }
    
    /**
     * Indicates whether the hook should be applied based on the type of reload.
     * 
     * @return <code>true</code> if it should be applied; <code>false</code> otherwise.
     */
    public boolean shouldApplyHook() {
        return (selection == ReloadSelections.QUICK || (selection == ReloadSelections.CUSTOM && hasData()
                && (SnapshotData.WINDOW.in(this.data) || SnapshotData.PLAYER.in(this.data))));
    }
}
