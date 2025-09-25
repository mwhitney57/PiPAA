/**
 * @(#)StretchIcon.java	1.0 03/27/12
 */
package darrylbu.icon;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.net.URL;
import java.util.Objects;

import javax.swing.ImageIcon;
import javax.swing.Timer;

import dev.mwhitney.gui.viewer.SubImageObserver;
import dev.mwhitney.gui.viewer.ZoomPanSnapshot;
import dev.mwhitney.listeners.PaintRequester;
import dev.mwhitney.listeners.PropertyListener;
import dev.mwhitney.main.PiPProperty;
import dev.mwhitney.main.PiPProperty.PropDefault;
import dev.mwhitney.main.PiPProperty.SCALING_OPTION;

/**
 * An <CODE>Icon</CODE> that scales its image to fill the component area,
 * excluding any border or insets, optionally maintaining the image's aspect
 * ratio by padding and centering the scaled image horizontally or vertically.
 * <P>
 * The class is a drop-in replacement for <CODE>ImageIcon</CODE>, except that
 * the no-argument constructor is not supported.
 * <P>
 * As the size of the Icon is determined by the size of the component in which
 * it is displayed, <CODE>StretchIcon</CODE> must only be used in conjunction
 * with a component and layout that does not depend on the size of the
 * component's Icon.
 * 
 * @version 1.0 03/27/12
 * @author Darryl
 */
public class StretchIcon extends ImageIcon implements PaintRequester, PropertyListener {

  /**
   * A generated, unique serial ID for StretchIcons.
   * Modified by mwhitney57
   */
  private static final long serialVersionUID = -7332916991429549436L;
  
  /**
   * Determines whether the aspect ratio of the image is maintained.
   * Set to <code>false</code> to allow the image to distort to fill the component.
   */
  protected boolean proportionate = true;

  /**
   * Creates a <CODE>StretchIcon</CODE> from an array of bytes.
   *
   * @param  imageData an array of pixels in an image format supported by
   *             the AWT Toolkit, such as GIF, JPEG, or (as of 1.3) PNG
   *
   * @see ImageIcon#ImageIcon(byte[])
   */
  public StretchIcon(byte[] imageData) {
    super(imageData);
  }

  /**
   * Creates a <CODE>StretchIcon</CODE> from an array of bytes with the specified behavior.
   *
   * @param  imageData an array of pixels in an image format supported by
   *             the AWT Toolkit, such as GIF, JPEG, or (as of 1.3) PNG
   * @param proportionate <code>true</code> to retain the image's aspect ratio,
   *        <code>false</code> to allow distortion of the image to fill the
   *        component.
   *
   * @see ImageIcon#ImageIcon(byte[])
   */
  public StretchIcon(byte[] imageData, boolean proportionate) {
    super(imageData);
    this.proportionate = proportionate;
  }

  /**
   * Creates a <CODE>StretchIcon</CODE> from an array of bytes.
   *
   * @param  imageData an array of pixels in an image format supported by
   *             the AWT Toolkit, such as GIF, JPEG, or (as of 1.3) PNG
   * @param  description a brief textual description of the image
   *
   * @see ImageIcon#ImageIcon(byte[], java.lang.String)
   */
  public StretchIcon(byte[] imageData, String description) {
    super(imageData, description);
  }

  /**
   * Creates a <CODE>StretchIcon</CODE> from an array of bytes with the specified behavior.
   *
   * @see ImageIcon#ImageIcon(byte[])
   * @param  imageData an array of pixels in an image format supported by
   *             the AWT Toolkit, such as GIF, JPEG, or (as of 1.3) PNG
   * @param  description a brief textual description of the image
   * @param proportionate <code>true</code> to retain the image's aspect ratio,
   *        <code>false</code> to allow distortion of the image to fill the
   *        component.
   *
   * @see ImageIcon#ImageIcon(byte[], java.lang.String)
   */
  public StretchIcon(byte[] imageData, String description, boolean proportionate) {
    super(imageData, description);
    this.proportionate = proportionate;
  }

  /**
   * Creates a <CODE>StretchIcon</CODE> from the image.
   *
   * @param image the image
   *
   * @see ImageIcon#ImageIcon(java.awt.Image)
   */
  public StretchIcon(Image image) {
    super(image);
  }

  /**
   * Creates a <CODE>StretchIcon</CODE> from the image with the specified behavior.
   * 
   * @param image the image
   * @param proportionate <code>true</code> to retain the image's aspect ratio,
   *        <code>false</code> to allow distortion of the image to fill the
   *        component.
   * 
   * @see ImageIcon#ImageIcon(java.awt.Image)
   */
  public StretchIcon(Image image, boolean proportionate) {
    super(image);
    this.proportionate = proportionate;
  }

  /**
   * Creates a <CODE>StretchIcon</CODE> from the image.
   * 
   * @param image the image
   * @param  description a brief textual description of the image
   * 
   * @see ImageIcon#ImageIcon(java.awt.Image, java.lang.String) 
   */
  public StretchIcon(Image image, String description) {
    super(image, description);
  }

  /**
   * Creates a <CODE>StretchIcon</CODE> from the image with the specified behavior.
   *
   * @param image the image
   * @param  description a brief textual description of the image
   * @param proportionate <code>true</code> to retain the image's aspect ratio,
   *        <code>false</code> to allow distortion of the image to fill the
   *        component.
   *
   * @see ImageIcon#ImageIcon(java.awt.Image, java.lang.String)
   */
  public StretchIcon(Image image, String description, boolean proportionate) {
    super(image, description);
    this.proportionate = proportionate;
  }

  /**
   * Creates a <CODE>StretchIcon</CODE> from the specified file.
   *
   * @param filename a String specifying a filename or path
   *
   * @see ImageIcon#ImageIcon(java.lang.String)
   */
  public StretchIcon(String filename) {
    super(filename);
  }

  /**
   * Creates a <CODE>StretchIcon</CODE> from the specified file with the specified behavior.
   * 
   * @param filename a String specifying a filename or path
   * @param proportionate <code>true</code> to retain the image's aspect ratio,
   *        <code>false</code> to allow distortion of the image to fill the
   *        component.
   *
   * @see ImageIcon#ImageIcon(java.lang.String)
   */
  public StretchIcon(String filename, boolean proportionate) {
    super(filename);
    this.proportionate = proportionate;
  }

  /**
   * Creates a <CODE>StretchIcon</CODE> from the specified file.
   *
   * @param filename a String specifying a filename or path
   * @param  description a brief textual description of the image
   *
   * @see ImageIcon#ImageIcon(java.lang.String, java.lang.String)
   */
  public StretchIcon(String filename, String description) {
    super(filename, description);
  }

  /**
   * Creates a <CODE>StretchIcon</CODE> from the specified file with the specified behavior.
   * 
   * @param filename a String specifying a filename or path
   * @param  description a brief textual description of the image
   * @param proportionate <code>true</code> to retain the image's aspect ratio,
   *        <code>false</code> to allow distortion of the image to fill the
   *        component.
   *
   * @see ImageIcon#ImageIcon(java.awt.Image, java.lang.String)
   */
  public StretchIcon(String filename, String description, boolean proportionate) {
    super(filename, description);
    this.proportionate = proportionate;
  }

  /**
   * Creates a <CODE>StretchIcon</CODE> from the specified URL.
   *
   * @param location the URL for the image
   *
   * @see ImageIcon#ImageIcon(java.net.URL)
   */
  public StretchIcon(URL location) {
    super(location);
  }

  /**
   * Creates a <CODE>StretchIcon</CODE> from the specified URL with the specified behavior.
   * 
   * @param location the URL for the image
   * @param proportionate <code>true</code> to retain the image's aspect ratio,
   *        <code>false</code> to allow distortion of the image to fill the
   *        component.
   *
   * @see ImageIcon#ImageIcon(java.net.URL)
   */
  public StretchIcon(URL location, boolean proportionate) {
    super(location);
    this.proportionate = proportionate;
  }

  /**
   * Creates a <CODE>StretchIcon</CODE> from the specified URL.
   *
   * @param location the URL for the image
   * @param  description a brief textual description of the image
   *
   * @see ImageIcon#ImageIcon(java.net.URL, java.lang.String)
   */
  public StretchIcon(URL location, String description) {
    super(location, description);
  }

  /**
   * Creates a <CODE>StretchIcon</CODE> from the specified URL with the specified behavior.
   *
   * @param location the URL for the image
   * @param  description a brief textual description of the image
   * @param proportionate <code>true</code> to retain the image's aspect ratio,
   *        <code>false</code> to allow distortion of the image to fill the
   *        component.
   *
   * @see ImageIcon#ImageIcon(java.net.URL, java.lang.String)
   */
  public StretchIcon(URL location, String description, boolean proportionate) {
    super(location, description);
    this.proportionate = proportionate;
  }

  /**
   * Paints the icon.  The image is reduced or magnified to fit the component to which
   * it is painted.
   * <P>
   * If the proportion has not been specified, or has been specified as <code>true</code>,
   * the aspect ratio of the image will be preserved by padding and centering the image
   * horizontally or vertically.  Otherwise the image may be distorted to fill the
   * component it is painted to.
   * <P>
   * If this icon has no image observer,this method uses the <code>c</code> component
   * as the observer.
   *
   * @param c the component to which the Icon is painted.  This is used as the
   *          observer if this icon has no image observer
   * @param g the graphics context
   * @param x not used.
   * @param y not used.
   *
   * @see ImageIcon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
   */
  @Override
  public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
    Image image = getImage();
    if (image == null) {
      return;
    }
    // Update component size cache.
    c.getSize(compSize);
    
    // Additional clause for when a rescale should happen -- when the window size has changed. @mwhitney57
    if (parentSizeAtLastScale == null || !parentSizeAtLastScale.equals(c.getSize())) pendingImgRescale = true;
    
    Insets insets = ((Container) c).getInsets();
    x = insets.left;
    y = insets.top;

    int w = c.getWidth() - x - insets.right;
    int h = c.getHeight() - y - insets.bottom;

    if (proportionate) {
      int iw = image.getWidth(c);
      int ih = image.getHeight(c);

      if (iw * h < ih * w) {
        iw = (h * iw) / ih;
        x += (w - iw) / 2;
        w = iw;
      } else {
        ih = (w * ih) / iw;
        y += (h - ih) / 2;
        h = ih;
      }
    }
    
    /* Modifications onward (below) in this method are authored by @mwhitney57. */
    
    // Observers are crucial for animated images, like GIFs.
    // Use the set image observer, or the component itself if null.
    final ImageObserver io = getImageObserver() != null ? getImageObserver() : c;
    // Ensure custom observer implementation is tied to parent observer.
    rescaleObserver.setParentObserver(io);
    
    final Graphics2D g2d = (Graphics2D) g;
    
    // Zooming Functionality
    if (zoom > 1) {
        initPoints();
        
        // Update the Zoomed Draw Size and its difference compared to the normal draw size.
        zoomSize.setSize(w * zoom, h * zoom);
        zoomDiff.setSize(
            // Formula: Subtract component size from scaled image size.
            // If positive, bound to zero. If negative, use absolute value as zoom difference.
            Math.abs(Math.min((compSize.width  - zoomSize.width), 0)),  
            Math.abs(Math.min((compSize.height - zoomSize.height), 0))
        );
        
        // Offset the x and y to zoom centrally using the size difference from zoom scaling.
        x -= (zoomSize.width  - w) / 2;
        y -= (zoomSize.height - h) / 2;
        
        // Adjust the view towards the last zoom point.
        pendingZoomPan();
        
        // Lastly, update the width and height to reflect the zoomed size.
        w = zoomSize.width;
        h = zoomSize.height;

        /*
         * Define the x and y bounds for upcoming coordinates adjustment. The bounds
         * help keep an image positioned in the center of the component, especially if
         * the component's aspect-ratio does not match the image's (e.g. fullscreen mode).
         */
        // Must be at least 0, but can be greater when the component's dimension is larger than the zoomed image's.
        final int boundX    = Math.max(0, (compSize.width  - w) / 2);
        final int boundY    = Math.max(0, (compSize.height - h) / 2);
        // Same as upper bound, unless negative, which happens when the zoomed size's dimension is greater than the component's.
        final int boundMinX = Math.min(compSize.width  - w, boundX);
        final int boundMinY = Math.min(compSize.height - h, boundY);
        
        // Panning Functionality
        int offX = (int) (panOffsetPercentX * zoomDiff.width);
        int offY = (int) (panOffsetPercentY * zoomDiff.height);
        x = boundedAdd(x, boundedAdd(offX, this.panBuffer.width,  -zoomDiff.width/2,  zoomDiff.width/2),  boundMinX, boundX);
        y = boundedAdd(y, boundedAdd(offY, this.panBuffer.height, -zoomDiff.height/2, zoomDiff.height/2), boundMinY, boundY);
//        System.out.println(this.toString("(x: " + x + ", y:" + y + ", w: " + w + ", h: " + h + ")"));
    }
    
    // Get current image scaling option configuration.
    final SCALING_OPTION scaling = currentScalingOption();
    
    // Only re-scale the image if pending (zoom or image changed).
    if (scaling.not(SCALING_OPTION.FAST) && pendingImgRescale && !zoomChanging && (scaling.is(SCALING_OPTION.QUALITY) || !parentResizing)) {
//        if (this.scaledImage != null) this.scaledImage.flush();   // Basic testing indicates not necessary. No visible side effects using it though.
        this.scaledImage = toBufferedImage(image).getScaledInstance(w, h, Image.SCALE_SMOOTH);
        this.parentSizeAtLastScale = c.getSize();
        this.pendingImgRescale = false;
    }
    
    // Draw depending on image scaling configuration.
    switch (scaling) {
    case QUALITY:
    case SMART:
        // QUALITY forces itself into this block. Smart only allowed when not zooming or resizing.
        if (scaling.is(SCALING_OPTION.QUALITY) || (!zoomChanging && !parentResizing)) {
            // Triggers the observer, ensuring subsequent frames get paint calls when needed. Prevents freezing for GIFs when drawn in "quality" mode.
            c.prepareImage(image, this.rescaleObserver);
            g2d.drawImage(scaledImage, x, y, this.rescaleObserver);
            break;
        }
        // SMART, if conditions not met, reaches here and continues to use FAST approach.
    case FAST:
        // FAST always reaches here, but SMART can as well.
        g2d.drawImage(image, x, y, w, h, this.rescaleObserver);
        break;
    }
    g2d.dispose();
  }

  /**
   * Overridden to return 0.  The size of this Icon is determined by
   * the size of the component.
   * 
   * @return 0
   */
  @Override
  public int getIconWidth() {
    return 0;
  }

  /**
   * Overridden to return 0.  The size of this Icon is determined by
   * the size of the component.
   *
   * @return 0
   */
  @Override
  public int getIconHeight() {
    return 0;
  }
  
  /* Modifications onward (below) are authored by @mwhitney57. */
  
  /**
   * Gets the width of the image icon.
   * 
   * @return an int with the width.
   */
  public int getImgWidth() {
      return super.getIconWidth();
  }
  /**
   * Gets the height of the image icon.
   * 
   * @return an int with the height.
   */
  public int getImgHeight() {
      return super.getIconHeight();
  }
  
  /**
   * The last known size of the parent component hosting this image icon. Updated
   * whenever there's a paint call and the image is valid.
   * 
   * @author mwhitney57
   * @since 0.9.5
   */
  private final Dimension compSize = new Dimension(0, 0);
  /**
   * The {@link Timer} that tracks recent zoom changes and fires when the icon is
   * free to render in higher quality again.
   * <p>
   * Adjust the timer delay to control how long it takes until the quality version
   * is painted. After a zoom action. The lower the number, the quicker it will
   * update. However, too low a number won't provide enough time between zoom
   * steps, resulting in a less smooth experience as quality versions render in
   * intermittently.
   * 
   * @author mwhitney57
   * @since 0.9.5
   */
  private final Timer zoomRescaleTimer = new Timer(500, e -> {
      zoomChanging = false;
      requestPaint();   // Requests another paint to trigger higher quality rescale.
  });
  /**
   * A boolean for whether or not the zoom is "changing." This has intentionally
   * loose accuracy, extending beyond to include the delay of
   * {@link #zoomRescaleTimer}. This variable's core purpose is aiding the
   * painting process in knowing when zoom operations are happening, or when they
   * have happened very recently. This allows it to shift between quality and fast
   * scaling.
   * 
   * @author mwhitney57
   * @since 0.9.5
   */
  private boolean zoomChanging;
  /** A boolean for whether or not a zoom pan is pending, which is the effect of panning towards a point when zooming. */
  private boolean pendingZoomPan;
  /** A float with the zoom ratio, which is always at least <code>1.0f</code>. */
  private float zoom;
  /** A {@link Dimension} with the x (width) and y (height) zoom size. The zoom size is calculated by multiplying the regular size by the zoom ratio. */
  private Dimension zoomSize;
  /** A {@link Dimension} with the x (width) and y (height) difference in a zoomed icon's size against its displaying component. */
  private Dimension zoomDiff;
  /** A {@link Point} with x and y coordinates for where the last zoom action took place. */
  private Point zoomPoint;
  /** A {@link Dimension} with the x (width) and y (height) offsets for an active pan. There can only be a pan if the zoom is greater than <code>1.0f</code>. */
  private Dimension panOffset;
  /** The pan offset, represented as a percentage of the zoom difference. */
  private double panOffsetPercentX, panOffsetPercentY;
  /** A {@link Dimension} with the x (width) and y (height) offset buffers for an active pan. These numbers are applied to the pan offset when the pan action stops. */
  private Dimension panBuffer;
  /**
   * The most recent scaled version of the {@link ImageIcon}. The latest scale is
   * cached to prevent having to re-scale the image on every call of
   * {@link #paintIcon(Component, Graphics, int, int)}.
   * 
   * @author mwhitney57
   * @since 0.9.5
   */
  private Image scaledImage;
  /**
   * A {@link Dimension} with the size of the parent when the image was last
   * scaled. This is tracked to help prevent unnecessary scaling operations.
   * Functionally different from the tracking done with
   * {@link #setParentResizing(boolean)}. Removal of this may lead to
   * inconsistencies, especially if the parent size changes instantly instead of
   * gradually.
   * 
   * @author mwhitney57
   * @since 0.9.5
   */
  private Dimension parentSizeAtLastScale = null;
  /**
   * A boolean for whether or not the cached scaled image needs to be rescaled on
   * next repaint.
   * 
   * @author mwhitney57
   * @since 0.9.5
   */
  private boolean pendingImgRescale = true;
  /**
   * A custom {@link ImageObserver} that monitors for frame changes and updates
   * that require a new image rescale, while maintaining the functionality of the
   * observer its stepping in for.
   * 
   * @author mwhitney57
   * @since 0.9.5
   */
  private final SubImageObserver rescaleObserver = new SubImageObserver() {
      @Override
      public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
          // Check for frame change, which means we need to rescale and cache the image on next paint.
          if ((infoflags & (FRAMEBITS | ALLBITS)) != 0) {
              pendingImgRescale = true;
          }
          // Not checking hasParentObserver intentionally. Should always have parent when in use. If not, other logic is at fault.
          return getParentObserver().imageUpdate(img, infoflags, x, y, width, height);
      }
  };
  /**
   * A boolean for whether or not the parent of this icon is being resized. If it
   * is, this icon may display itself using a lower quality scaling algorithm
   * until resizing is complete.
   * 
   * @author mwhitney57
   * @since 0.9.5
   */
  private boolean parentResizing;
  /**
   * Sets whether or not the parent of this icon is being resized.
   * 
   * @param r - the resize status.
   * @author mwhitney57
   * @since 0.9.5
   */
  public void setParentResizing(boolean r) {
      this.parentResizing = r;
  }
  
  /**
   * Converts the passed {@link Image} to a {@link BufferedImage} by utilizing
   * {@link Graphics2D} and drawing the image.
   * 
   * @param img - the {@link Image} to convert.
   * @return the converted {@link BufferedImage}.
   * @author mwhitney57
   * @since 0.9.5
   */
  public static BufferedImage toBufferedImage(Image img) {
      if (img instanceof BufferedImage buffImg) return buffImg;

      // Create a BufferedImage and respect transparency.
      final BufferedImage buffImg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

      // Draw the Image onto the BufferedImage.
      final Graphics2D g2d = buffImg.createGraphics();
      g2d.drawImage(img, 0, 0, null);
      g2d.dispose();

      // Return the BufferedImage.
      return buffImg;
  }
  
  /**
   * Gets the current {@link SCALING_OPTION} configured to the
   * {@link PiPProperty#IMG_SCALING_QUALITY} option. This method returns the
   * default as a fallback.
   * 
   * @return the current {@link SCALING_OPTION}.
   */
  private SCALING_OPTION currentScalingOption() {
      return PropDefault.SCALING.matchAny(propertyState(PiPProperty.IMG_SCALING_QUALITY, String.class));
  }
  /**
   * Restarts the internal zoom rescale {@link Timer}. This timer is responsible
   * for ensuring the paint process is aware of any ongoing zoom operations, and
   * provides a delay to ensure quality rescale operations don't happen during the
   * zoom for better performance. Once executed, the timer will trigger a repaint
   * that can do a quality rescale.
   * 
   * @author mwhitney57
   * @since 0.9.5
   */
  private void restartZoomRescaleTimer() {
      // This logic only pertains to the SMART scaling option. Return otherwise.
      if(currentScalingOption().not(SCALING_OPTION.SMART)) return;
      
      zoomChanging = true;
      zoomRescaleTimer.setRepeats(false);
      zoomRescaleTimer.restart();
  }
  
  /**
   * Sets the zoom factor and zoom point for the icon. The zoom factor cannot be
   * less than <code>1.00f</code>, and any passed value of <code>z</code> below it
   * will round up to <code>1.00f</code>.
   * 
   * @param z - a float with the zoom factor (1.00f for no zoom).
   * @param p - a Point with the x and y coordinates of the zoom action.
   */
  public void setZoom(float z, Point p) {
      initPoints();
      this.zoom = Math.max(1.00f, z);
      zoomPoint.setLocation(p);
      this.pendingZoomPan = true;
      this.pendingImgRescale = true;  // Indicate the image should be rescaled since the zoom changed.
      restartZoomRescaleTimer();
      
      // Reset Values if Zoom is Now Normal (1.0) (Not Zoomed)
      if (this.zoom == 1.0f) {
          /*
           * These values help provide a gradual zoom towards a point over multiple steps.
           * However, if not reset between zoom chains, they skew the next zoom towards
           * the old values. Reset to make each zoom chain feel accurate.
           */
          this.panOffsetPercentX = 0.0f;
          this.panOffsetPercentY = 0.0f;
      }
  }

  /**
   * Acts upon a pending zoom pan. The "zoom pan" effect is when a user attempts
   * to zoom in on a specific area of the icon, as opposed to just zooming into
   * the center. This effect feels more responsive and allows for finer control of
   * the viewport.
   */
  private void pendingZoomPan() {
      if (pendingZoomPan) {
          /**
           * Multiplied with the % screen location of the zoom point. The greater the
           * number, the greater the movement towards the point after each zoom step. The
           * factor is scaled based on the window size, since its panning impact is much
           * more dramatic in smaller windows if left constant.
           */
          //                  Constant ↓    Scale with greatest window size dimension     ↓ Arbitrary Constant
          final int panFactor = (int) (20 * (Math.max(compSize.width, compSize.height) / 500.0f));
          /**
           * Calculate where the zoom point lies in the bounds of the component displaying
           * this image, resulting in a percentage for both axes. The very center of the
           * component would be (0.0,0.0).
           */
          float xPercOfScreen = zoomPoint.x / (compSize.width  / 2.0f); // X: -1.0 ← 0 → 1.0
          float yPercOfScreen = zoomPoint.y / (compSize.height / 2.0f); // Y: -1.0 ↑ 0 ↓ 1.0
          /**
           * The point's percentage across the screen is then multiplied against the pan
           * factor to determine exactly how much the pan should move in each direction.
           * <code> Example — Factor: 20 -> (0.75, 0.75) -> (15, 15) →↓ </code>
           */
          // Linear Function — Just basic multiplication. Feels accurate. Chose to scale factor with window size rather than scale the function on position.
          float xScaledFactor = panFactor * xPercOfScreen;  // X: -Factor ← 0 → Factor
          float yScaledFactor = panFactor * yPercOfScreen;  // Y: -Factor ↑ 0 ↓ Factor
          // Power Function Alternative — Smaller results at smaller percentages.  Concave-up curve. Math.signum call to respect negatives. Can feel inaccurate.
//          float xScaledFactor = panFactor * Math.signum(xPercOfScreen) * (float) Math.pow(xPercOfScreen, 2);  // X: -Factor ← 0 → Factor
//          float yScaledFactor = panFactor * Math.signum(yPercOfScreen) * (float) Math.pow(yPercOfScreen, 2);  // Y: -Factor ↑ 0 ↓ Factor
          
          pendingZoomPan = false;
          // Pan — invert values to push image *away*, which gives effect of zooming into the point.
          this.pan((int) -xScaledFactor, (int) -yScaledFactor);
          this.stoppedPan();
      }
  }
  
  /**
   * Increases the zoom factor and sets the zoom point for the icon.
   * 
   * @param i - a float with the additive zoom factor amount.
   * @param p - a Point with the x and y coordinates of the zoom action.
   */
  public void incZoom(float i, Point p) {
      setZoom(this.zoom += i, p);
  }

  /**
   * Initializes Point objects used for zooming and panning. This method will do
   * nothing if all objects are already initialized.
   */
  private void initPoints() {
      if (this.zoomSize == null)
          this.zoomSize = new Dimension();
      if (this.zoomPoint == null)
          this.zoomPoint = new Point(0, 0);
      if (this.zoomDiff == null)
          this.zoomDiff = new Dimension();
      if (this.panBuffer == null)
          this.panBuffer = new Dimension();
      if (this.panOffset == null)
          this.panOffset = new Dimension();
  }

  /**
   * Adds the passed <code>int a</code> to <code>int x</code> while respecting the
   * bounds: <code>int l --> int u</code>. The integers <code>l</code> and
   * <code>u</code> are the lower and upper bounds respectively. The bounds are
   * checked at the end of the equation, so the returned <code>int</code> will
   * always lie between them.
   * 
   * @param x - the number to be added to.
   * @param a - the number to add to <code>x</code>.
   * @param l - the lower bound for the addition equation.
   * @param u - the upper bound for the addition equation.
   * @return an <code>int</code> result from the addition operation that lies
   *         between the passed lower and upper bounds.
   */
  private int boundedAdd(int x, int a, int l, int u) {
      return Math.max(l, Math.min(x + a, u));
  }
  
  /**
   * Performs a sync and recalculation of the pan offset using the percentage
   * offset and the current zoom difference. This method is useful in ensuring
   * that the pan offset amounts in raw pixels are still accurate and within
   * bounds, as this may cease to be true if the window size changes.
   * <p>
   * <b>Note:</b> As of writing this documentation, this method is not called
   * constantly, such as within the <code>paintIcon(...)</code> method, as it
   * seemed to interfere with the accuracy of itself and the buffer.
   */
  private void syncPan() {
      this.panOffset.setSize((int) (panOffsetPercentX * zoomDiff.getWidth()), (int) (panOffsetPercentY * zoomDiff.getHeight()));
  }
  
  /**
   * Pans the image icon by the passed x and y amounts.
   * If the image icon is not zoomed at all, nothing happens.
   * 
   * @param x - an int with the x amount to pan.
   * @param y - an int with the y amount to pan.
   */
  public void pan(int x, int y) {
      if (zoom <= 1)
          return;
      
      initPoints();
      
//      System.out.println("Pan CMD Received: (" + x + ", " + y + ")");
//      System.out.println("PAN OFFSET: " + panOffset);
      this.panBuffer.setSize(x, y);
//      System.out.println("PAN OFFSET AFTER: " + panOffset);
  }
  
  /**
   * Called when an active pan operation is stopped. This method will add the pan
   * buffer amounts to the pan offset values, but it will keep the final value
   * within the bounds of the image icon.
   */
  public void stoppedPan() {
//      System.out.println("Pan stopped.");
      // Ensure pan offset amounts are still accurate before performing calculations.
      syncPan();
      
      this.panOffset.setSize(
              boundedAdd(this.panOffset.width,  this.panBuffer.width,  -zoomDiff.width/2,  zoomDiff.width/2),
              boundedAdd(this.panOffset.height, this.panBuffer.height, -zoomDiff.height/2, zoomDiff.height/2));
      this.panBuffer.setSize(0, 0);
      
      // Recalculate percentage offsets using new numbers.
      this.panOffsetPercentX = (panOffset.getWidth()  / zoomDiff.getWidth());
      this.panOffsetPercentY = (panOffset.getHeight() / zoomDiff.getHeight());
  }
  
  /**
   * Takes a snapshot of the current zoom and pan data. Useful for capturing
   * exactly what the image looked like in its window at any given time.
   * 
   * @return a {@link ZoomPanSnapshot} containing the zoom and pan data.
   * @author mwhitney57
   * @since 0.9.5
   * @see {@link #applySnapshot(ZoomPanSnapshot)} to apply a snapshot.
   */
  public ZoomPanSnapshot snapshot() {
      return new ZoomPanSnapshot(this.zoom, this.panOffset, this.panOffsetPercentX, this.panOffsetPercentY);
  }
  
  /**
   * Apply data from a {@link ZoomPanSnapshot}, returning to the look from when
   * the snapshot was taken.
   * 
   * @param snapshot - the {@link ZoomPanSnapshot} to apply the data from.
   * @author mwhitney57
   * @since 0.9.5
   * @see {@link #snapshot()} to capture a snapshot.
   */
  public void applySnapshot(ZoomPanSnapshot snapshot) {
      this.zoom = snapshot.zoom();
      this.panOffset = snapshot.pan();
      this.panOffsetPercentX = snapshot.panX();
      this.panOffsetPercentY = snapshot.panY();
  }
  
  /**
   * A helper method for cleanly printing a {@link Point}'s x and y values.
   * 
   * @param p - the {@link Point} to print.
   * @return a String with the neatly formatted x and y values, or an empty String
   *         if the point was <code>null</code>.
   */
  private String asStr(Point p) {
      if (p == null) return "";
      
      return "(" + p.x + "," + p.y + ")";
  }
  
  /**
   * A helper method for cleanly printing a {@link Dimension}'s width and height
   * values.
   * 
   * @param d - the {@link Dimension} to print.
   * @return a String with the neatly formatted width and height values, or an
   *         empty String if the point was <code>null</code>.
   */
  private String asStr(Dimension d) {
      if (d == null) return "";
      
      return "(" + d.width + "," + d.height + ")";
  }
  
  /**
   * Gets a String representation of this StretchIcon, while including some String
   * filler. The String filler can be <code>null</code>, in which case it will
   * just ultimately be appended as an empty String.
   * 
   * @param fill - a String with content to fill within the standard
   *             <code>toString()</code> return.
   * @return a String representation of this StretchIcon, including any passed
   *         filler.
   */
  public String toString(String fill) {
      return String.format("""
              StretchIcon (Size: %s,%s) %s
              > Zoom: %s | Size: %s | Diff: %s | Point: %s
              >  Pan: Offset: %s at Percent: (%.4f,%.4f) | Buffer: %s | Pending: %s
              """,
              this.getImgWidth(), this.getImgHeight(), Objects.toString(fill, ""),
              this.zoom, asStr(this.zoomSize), asStr(this.zoomDiff), asStr(this.zoomPoint),
              asStr(this.panOffset), this.panOffsetPercentX, this.panOffsetPercentY, asStr(this.panBuffer), this.pendingZoomPan);
  }
  
  @Override
  public String toString() {
      return this.toString(null);
  }
  // To Be Overridden.
  @Override
  public void requestPaint() {}
  @Override
  public void propertyChanged(PiPProperty prop, String value) {}
  @Override
  public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return null; }
}
