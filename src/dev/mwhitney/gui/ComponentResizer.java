package dev.mwhitney.gui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import dev.mwhitney.listeners.StartEndListener;
import dev.mwhitney.util.ScalingDimension;

/**
 *  The ComponentResizer allows you to resize a component by dragging a border
 *  of the component.
 *  
 *  @author Rob Camick
 *  @author mwhitney57 (ONLY Modifications for PiPAA)
 *  @see https://tips4java.wordpress.com/2009/09/13/resizing-components/
 *  @see https://github.com/tips4java/tips4java/blob/main/source/ComponentResizer.java
 */
public class ComponentResizer extends MouseAdapter
{
    private final static Dimension MINIMUM_SIZE = new Dimension(10, 10);
    private final static Dimension MAXIMUM_SIZE =
        new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

    private static Map<Integer, Integer> cursors = new HashMap<Integer, Integer>();
    {
        cursors.put(1, Cursor.N_RESIZE_CURSOR);
        cursors.put(2, Cursor.W_RESIZE_CURSOR);
        cursors.put(4, Cursor.S_RESIZE_CURSOR);
        cursors.put(8, Cursor.E_RESIZE_CURSOR);
        cursors.put(3, Cursor.NW_RESIZE_CURSOR);
        cursors.put(9, Cursor.NE_RESIZE_CURSOR);
        cursors.put(6, Cursor.SW_RESIZE_CURSOR);
        cursors.put(12, Cursor.SE_RESIZE_CURSOR);
    }

    private Insets dragInsets;
    private Dimension snapSize;

    private int direction;
    protected static final int NORTH = 1;
    protected static final int WEST = 2;
    protected static final int SOUTH = 4;
    protected static final int EAST = 8;

    private Cursor sourceCursor;
    private boolean resizing;
    private Rectangle bounds;
    private Point pressed;
    private boolean autoscrolls;
    private boolean autoLayout;
    /**
     * A Dimension with a width and height (for an aspect ratio).
     * @author mwhitney57
     */
    private Dimension aspectRatio;
    /**
     * A Supplier that is registered to check for size locks and returns a boolean.
     * @author mwhitney57
     */
    private Supplier<Boolean> lockChecker;
    /**
     * An {@link ArrayList} of listeners which receive updates on resize events.
     * @author mwhitney57
     * @since 0.9.5
     */
    private ArrayList<StartEndListener> resizeListeners = new ArrayList<>();
    /**
     * A {@link Dimension} that holds the minimum size for any content to be
     * displayed within the component, excluding its borders. This differs from the
     * {@link #getMinimumSize()} value, as that includes the borders.
     * @author mwhitney57
     * @since 0.9.5
     */
    private Dimension minContentSize = new Dimension(1, 1);

    private Dimension minimumSize = MINIMUM_SIZE;
    private Dimension maximumSize = MAXIMUM_SIZE;

    /**
     *  Convenience contructor. All borders are resizable in increments of
     *  a single pixel. Components must be registered separately.
     */
    public ComponentResizer()
    {
        this(new Insets(5, 5, 5, 5), new Dimension(1, 1));
    }

    /**
     *  Convenience contructor. All borders are resizable in increments of
     *  a single pixel. Components can be registered when the class is created
     *  or they can be registered separately afterwards.
     *
     *  @param components components to be automatically registered
     */
    public ComponentResizer(Component... components)
    {
        this(new Insets(5, 5, 5, 5), new Dimension(1, 1), components);
    }

    /**
     *  Convenience contructor. Eligible borders are resisable in increments of
     *  a single pixel. Components can be registered when the class is created
     *  or they can be registered separately afterwards.
     *
     *  @param dragInsets Insets specifying which borders are eligible to be
     *                    resized.
     *  @param components components to be automatically registered
     */
    public ComponentResizer(Insets dragInsets, Component... components)
    {
        this(dragInsets, new Dimension(1, 1), components);
    }

    /**
     *  Create a ComponentResizer.
     *
     *  @param dragInsets Insets specifying which borders are eligible to be
     *                    resized.
     *  @param snapSize Specify the dimension to which the border will snap to
     *                  when being dragged. Snapping occurs at the halfway mark.
     *  @param components components to be automatically registered
     */
    public ComponentResizer(Insets dragInsets, Dimension snapSize, Component... components)
    {
        setDragInsets( dragInsets );
        setSnapSize( snapSize );
        registerComponent( components );
    }

    /**
     *  Get the auto layout property
     *
     *  @return  the auto layout property
     */
    public boolean isAutoLayout()
    {
        return autoLayout;
    }

    /**
     *  Set the auto layout property
     *
     *  @param  autoLayout when true layout will be invoked on the parent container
     */
    public void setAutoLayout(boolean autoLayout)
    {
        this.autoLayout = autoLayout;
    }

    /**
     *  Get the drag insets
     *
     *  @return  the drag insets
     */
    public Insets getDragInsets()
    {
        return dragInsets;
    }

    /**
     *  Set the drag dragInsets. The insets specify an area where mouseDragged
     *  events are recognized from the edge of the border inwards. A value of
     *  0 for any size will imply that the border is not resizable. Otherwise
     *  the appropriate drag cursor will appear when the mouse is inside the
     *  resizable border area.
     *
     *  @param  dragInsets Insets to control which borders are resizeable.
     */
    public void setDragInsets(Insets dragInsets)
    {
        validateMinimumAndInsets(minimumSize, dragInsets);

        this.dragInsets = dragInsets;
    }

    /**
     *  Get the components maximum size.
     *
     *  @return the maximum size
     */
    public Dimension getMaximumSize()
    {
        return maximumSize;
    }

    /**
     *  Specify the maximum size for the component. The component will still
     *  be constrained by the size of its parent.
     *
     *  @param maximumSize the maximum size for a component.
     */
    public void setMaximumSize(Dimension maximumSize)
    {
        /* Modified by author @mwhitney57 to equate null values to no max size preference (MAXIMUM_SIZE). */
        if (maximumSize == null) this.maximumSize = MAXIMUM_SIZE;
        else this.maximumSize = maximumSize;
    }

    /**
     *  Get the components minimum size.
     *
     *  @return the minimum size
     */
    public Dimension getMinimumSize()
    {
        return minimumSize;
    }

    /**
     *  Specify the minimum size for the component. The minimum size is
     *  constrained by the drag insets.
     *
     *  @param minimumSize the minimum size for a component.
     */
    public void setMinimumSize(Dimension minimumSize)
    {
        validateMinimumAndInsets(minimumSize, dragInsets);

        this.minimumSize = minimumSize;
    }

    /**
     *  Remove listeners from the specified component
     *
     *  @param component  the component the listeners are removed from
     */
    public void deregisterComponent(Component... components)
    {
        for (Component component : components)
        {
            component.removeMouseListener( this );
            component.removeMouseMotionListener( this );
        }
    }

    /**
     *  Add the required listeners to the specified component
     *
     *  @param component  the component the listeners are added to
     */
    public void registerComponent(Component... components)
    {
        for (Component component : components)
        {
            component.addMouseListener( this );
            component.addMouseMotionListener( this );
        }
    }

    /**
     *  Get the snap size.
     *
     *  @return the snap size.
     */
    public Dimension getSnapSize()
    {
        return snapSize;
    }

    /**
     *  Control how many pixels a border must be dragged before the size of
     *  the component is changed. The border will snap to the size once
     *  dragging has passed the halfway mark.
     *
     *  @param snapSize Dimension object allows you to separately spcify a
     *                  horizontal and vertical snap size.
     */
    public void setSnapSize(Dimension snapSize)
    {
        this.snapSize = snapSize;
    }
    
    /**
     * Gets the aspect ratio being used during resizing operations, if any.
     * 
     * @return a Dimension with a width and height figure to calculate aspect ratio.
     * @author mwhitney57
     */
    public Dimension getAspectRatio()
    {
        return this.aspectRatio;
    }
    
    /**
     * Sets the aspect ratio to be used during resizing operations, if any.
     * 
     * @param aspectRatio - a Dimension with a width and height figure to calculate
     *                    aspect ratio.
     * @author mwhitney57
     */
    public void setAspectRatio(Dimension aspectRatio)
    {
        this.aspectRatio = aspectRatio;
    }
    
    /**
     * Checks if the aspect ratio is to be used/maintained during resizing. If the
     * aspect ratio has not been set, then default resizing behavior will be
     * utilized.
     * 
     * @return {@code true} if the aspect ratio is to be maintained while resizing;
     *         {@code false} otherwise.
     * @author mwhitney57
     */
    public boolean isUsingRatio()
    {
        return getAspectRatio() != null;
    }
    
    /**
     * Sets the lock checker. The lock checker is responsible for ensuring the size
     * is not locked, which would prevent this class from adjusting it or indicating
     * to the user that it is capable of doing so.
     * 
     * @param checker - a {@link Supplier} that returns a boolean.
     * @author mwhitney57
     * @since 0.9.4
     */
    public void setLockChecker(final Supplier<Boolean> checker) {
        this.lockChecker = checker;
    }
    
    /**
     * Checks if the size is locked using the internal lock checker.
     * 
     * @return {@code true} if the size is locked and no adjustments should be made
     *         to it; {@code false} otherwise.
     * @see {@link #setLockChecker(Supplier)} to set the internal lock checker.
     * @author mwhitney57
     * @since 0.9.4
     */
    public boolean isSizeLocked() {
        return this.lockChecker.get();
    }
    
    /**
     * Adds a resize listener. These listeners receive updates when the component
     * begins the process of being resized, and when that process ends.
     * 
     * @param listener - the {@link StartEndListener} to add.
     * @author mwhitney57
     * @since 0.9.5
     */
    public void addResizeListener(StartEndListener listener) {
        if (listener != null) this.resizeListeners.add(listener);
    }
    
    /**
     * Checks if the component is currently being resized.
     * 
     * @return <code>true</code> if being resized; <code>false</code> otherwise.
     * @author mwhitney57
     * @since 0.9.5
     */
    public boolean isResizing() {
        return this.resizing;
    }
    
    /**
     * Sets the internal resizing boolean, then fires appropriate listeners.
     * 
     * @param r - the boolean for if the component is being resized.
     * @author mwhitney57
     * @since 0.9.5
     */
    private void setResizing(boolean r) {
        this.resizing = r;
        
        if (r) resizeListeners.forEach(StartEndListener::started);
        else   resizeListeners.forEach(StartEndListener::ended);
    }
    
    /**
     * Sets the minimum size for content shown within this component, excluding the
     * borders.
     * 
     * @param d - a {@link Dimension} with the minimum content size.
     * @author mwhitney57
     * @since 0.9.5
     */
    public void setMinimumContentSize(Dimension d) {
        this.minContentSize = Objects.requireNonNullElseGet(d, () -> new Dimension(1, 1));
    }
    
    /**
     *  When the components minimum size is less than the drag insets then
     *  we can't determine which border should be resized so we need to
     *  prevent this from happening.
     */
    private void validateMinimumAndInsets(Dimension minimum, Insets drag)
    {
        int minimumWidth = drag.left + drag.right;
        int minimumHeight = drag.top + drag.bottom;

        if (minimum.width  < minimumWidth
        ||  minimum.height < minimumHeight)
        {
            String message = "Minimum size cannot be less than drag insets";
            throw new IllegalArgumentException( message );
        }
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        // Cancel if size is locked -- @mwhitney57
        if (isSizeLocked()) return;
        
        Component source = e.getComponent();
        Point location = e.getPoint();
        direction = 0;

        if (location.x < dragInsets.left)
            direction += WEST;

        if (location.x > source.getWidth() - dragInsets.right - 1)
            direction += EAST;

        if (location.y < dragInsets.top)
            direction += NORTH;

        if (location.y > source.getHeight() - dragInsets.bottom - 1)
            direction += SOUTH;

        //  Mouse is no longer over a resizable border

        if (direction == 0)
        {
            source.setCursor( sourceCursor );
        }
        else  // use the appropriate resizable cursor
        {
            int cursorType = cursors.get( direction );
            Cursor cursor = Cursor.getPredefinedCursor( cursorType );
            source.setCursor( cursor );
        }
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        // Cancel if size is locked -- @mwhitney57
        if (isSizeLocked()) return;
        
        if (! resizing)
        {
            Component source = e.getComponent();
            sourceCursor = source.getCursor();
        }
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        if (! resizing)
        {
            Component source = e.getComponent();
//            source.setCursor( sourceCursor );
            source.setCursor( Cursor.getDefaultCursor() ); // Changed by author @mwhitney57 to solve rare bug where cursor would set to a resize cursor.
        }
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        // Cancel if size is locked -- @mwhitney57
        if (isSizeLocked()) return;
        
        //  The mouseMoved event continually updates this variable

        if (direction == 0) return;

        //  Setup for resizing. All future dragging calculations are done based
        //  on the original bounds of the component and mouse pressed location.

        setResizing(true);    // Changed by @mwhitney57 to use method instead.

        Component source = e.getComponent();
        pressed = e.getPoint();
        SwingUtilities.convertPointToScreen(pressed, source);
        bounds = source.getBounds();

        //  Making sure autoscrolls is false will allow for smoother resizing
        //  of components

        if (source instanceof JComponent)
        {
            JComponent jc = (JComponent)source;
            autoscrolls = jc.getAutoscrolls();
            jc.setAutoscrolls( false );
        }
    }

    /**
     *  Restore the original state of the Component
     */
    @Override
    public void mouseReleased(MouseEvent e)
    {
        setResizing(false);   // Changed by @mwhitney57 to use method instead.

        Component source = e.getComponent();
        source.setCursor( sourceCursor );

        if (source instanceof JComponent)
        {
            ((JComponent)source).setAutoscrolls( autoscrolls );
        }

        if (autoLayout)
        {
            Component parent = source.getParent();

            if (parent != null)
            {
                if (parent instanceof JComponent)
                {
                    ((JComponent)parent).revalidate();
                }
                else
                {
                    parent.validate();
                }
            }
        }
    }

    /**
     *  Resize the component ensuring location and size is within the bounds
     *  of the parent container and that the size is within the minimum and
     *  maximum constraints.
     *
     *  All calculations are done using the bounds of the component when the
     *  resizing started.
     */
    @Override
    public void mouseDragged(MouseEvent e)
    {
        // Cancel if size is locked -- @mwhitney57
        if (isSizeLocked()) return;
        
        if (resizing == false) return;

        Component source = e.getComponent();
        Point dragged = e.getPoint();
        SwingUtilities.convertPointToScreen(dragged, source);

        changeBounds(source, direction, bounds, pressed, dragged);
    }

    protected void changeBounds(Component source, int direction, Rectangle bounds, Point pressed, Point current)
    {
        //  Start with original locaton and size

        int x = bounds.x;
        int y = bounds.y;
        int width = bounds.width;
        int height = bounds.height;
        // N & W drag amounts for aspect ratio reference -- @mwhitney57
        int dragN = 0, dragW = 0;
        /* Begin aspect ratio-preserving resize changes -- @mwhitney57 */
        // Declare adapted minimums. Use set minimums by default.
        int adaptedMinimumWidth  = minimumSize.width;
        int adaptedMinimumHeight = minimumSize.height;
        // Find aspect ratio-respecting minimums. These will vary by media and must adapt on the fly here.
        if (isUsingRatio()) {
            // Use media's aspect ratio and minimum size without borders for accuracy in initial calculations.
            final ScalingDimension sd = ScalingDimension.from(getAspectRatio())
                    .setMinimumSize(minContentSize)
                    .scaleToMinimum();
            // Set ratio-preserving minimums, which consist of the scaled values with the borders added back.
            adaptedMinimumWidth  = sd.width  + (PiPWindow.BORDER_SIZE*2);
            adaptedMinimumHeight = sd.height + (PiPWindow.BORDER_SIZE*2);
        }
        /* End changes by @mwhitney57 */

        //  Resizing the West or North border affects the size and location

        if (WEST == (direction & WEST))
        {
            int drag = getDragDistance(pressed.x, current.x, snapSize.width);
//            int maximum = Math.min(width + x, maximumSize.width);
            int maximum = Math.min(width + x - 10, maximumSize.width);
            drag = getDragBounded(drag, snapSize.width, width, adaptedMinimumWidth, maximum);
            // Save W drag amount for aspect ratio reference -- @mwhitney57
            dragW = drag;

            x -= drag;
            width += drag;
        }

        if (NORTH == (direction & NORTH))
        {
            int drag = getDragDistance(pressed.y, current.y, snapSize.height);
//            int maximum = Math.min(height + y, maximumSize.height);
            int maximum = Math.min(height + y - 10, maximumSize.height);
            drag = getDragBounded(drag, snapSize.height, height, adaptedMinimumHeight, maximum);
            // Save N drag amount for aspect ratio reference -- @mwhitney57
            dragN = drag;
            
            y -= drag;
            height += drag;
        }

        //  Resizing the East or South border only affects the size

        if (EAST == (direction & EAST))
        {
            int drag = getDragDistance(current.x, pressed.x, snapSize.width);
            Dimension boundingSize = getBoundingSize( source );
            int maximum = Math.min(boundingSize.width - x, maximumSize.width);
            drag = getDragBounded(drag, snapSize.width, width, adaptedMinimumWidth, maximum);
            width += drag;
        }

        if (SOUTH == (direction & SOUTH))
        {
            int drag = getDragDistance(current.y, pressed.y, snapSize.height);
            Dimension boundingSize = getBoundingSize( source );
            int maximum = Math.min(boundingSize.height - y, maximumSize.height);
            drag = getDragBounded(drag, snapSize.height, height, adaptedMinimumHeight, maximum);
            height += drag;
        }

        // Begin modifications by @mwhitney57
        if(isUsingRatio()) {
            // Aspect ratio should be respected...
            final float ratio = (float) (getAspectRatio().width) / (float) (getAspectRatio().height);
            
            // Scale based on aspect ratio, depending on resizing direction(s).
            if (NORTH == (direction & NORTH)) {
                /*
                 * If drag direction is NW, cancel out x-direction drag.
                 * Then ensure resizing in proper direction while maintaining aspect ratio.
                 * Subtract the North drag amount multiplied by the aspect ratio.
                 */
                if(WEST == (direction & WEST)) {
                    x += dragW;
                    x -= dragN * ratio;
                }
                // Subtract from height in preparation to account for border
                height -= PiPWindow.BORDER_SIZE*2;
                // Ensure final calculated values remain above minimums. Precision can be off by a pixel or so. This remedies that.
                final int calcW = Math.max(adaptedMinimumWidth,  (int) (height * ratio) + (PiPWindow.BORDER_SIZE*2));
                final int calcH = Math.max(adaptedMinimumHeight, (int) (height + (PiPWindow.BORDER_SIZE*2)));
                // Set bounds of component, adding the width of the borders x2 to both the W and H.
                source.setBounds(x, y, calcW, calcH);
            }
            else if (WEST == (direction & WEST) || (EAST == (direction & EAST))) {
                // Subtract from width in preparation to account for border
                width -= PiPWindow.BORDER_SIZE*2;
                // Ensure final calculated values remain above minimums. Precision can be off by a pixel or so. This remedies that.
                final int calcW = Math.max(adaptedMinimumWidth,  width + (PiPWindow.BORDER_SIZE*2));
                final int calcH = Math.max(adaptedMinimumHeight, (int) (width / ratio) + (PiPWindow.BORDER_SIZE*2));
                // Set bounds of component, adding the width of the borders x2 to both the W and H.
                source.setBounds(x, y, calcW, calcH);
            }
            else if (SOUTH == (direction & SOUTH)) {
                // Subtract from height in preparation to account for border
                height -= PiPWindow.BORDER_SIZE*2;
                // Ensure final calculated values remain above minimums. Precision can be off by a pixel or so. This remedies that.
                final int calcW = Math.max(adaptedMinimumWidth,  (int) (height * ratio) + (PiPWindow.BORDER_SIZE*2));
                final int calcH = Math.max(adaptedMinimumHeight, height + (PiPWindow.BORDER_SIZE*2));
                // Set bounds of component, adding the width of the borders x2 to both the W and H.
                source.setBounds(x, y, calcW, calcH);
            }
        } else {
            source.setBounds(x, y, width, height);
        }
        // End modifications by @mwhitney57
        source.validate();
    }

    /*
     *  Determine how far the mouse has moved from where dragging started
     */
    private int getDragDistance(int larger, int smaller, int snapSize)
    {
        int halfway = snapSize / 2;
        int drag = larger - smaller;
        drag += (drag < 0) ? -halfway : halfway;
        drag = (drag / snapSize) * snapSize;

        return drag;
    }

    /*
     *  Adjust the drag value to be within the minimum and maximum range.
     */
    private int getDragBounded(int drag, int snapSize, int dimension, int minimum, int maximum)
    {
        while (dimension + drag < minimum)
            drag += snapSize;

        while (dimension + drag > maximum)
            drag -= snapSize;


        return drag;
    }

    /*
     *  Keep the size of the component within the bounds of its parent.
     */
    private Dimension getBoundingSize(Component source)
    {
        if (source instanceof Window)
        {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle bounds = env.getMaximumWindowBounds();
            return new Dimension(bounds.width, bounds.height);
        }
        else
        {
//            return source.getParent().getSize();
            Dimension d = source.getParent().getSize();
            d.width += -10;
            d.height += -10;
            return d;
        }
    }
}