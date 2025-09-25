package dev.mwhitney.gui.viewer;

import java.awt.Dimension;

/**
 * A snapshot of zoom and pan data. Intended to be used with players/viewers to
 * create a save state which can be loaded later.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public record ZoomPanSnapshot(
    /** A float with the zoom amount. Typically cannot be less than <code>1.0f</code>. */
    float zoom,
    /** A {@link Dimension} with the pan offset. */
    Dimension pan,
    /** A double with the percentage pan offset in the X direction. */
    double panX,
    /** A double with the percentage pan offset in the Y direction. */
    double panY
) {
    /** Simply a {@link Point} with coordinates of <code>0,0</code>.  */
    public static final Dimension ZOOM_ZERO = new Dimension();
    /** The default snapshot instance, with all of the default data values. */
    public static final ZoomPanSnapshot DEFAULT = new ZoomPanSnapshot(1.0f, ZOOM_ZERO, 0.0, 0.0);
}
