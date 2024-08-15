/**
 * @(#)StretchIcon.java	1.0 03/27/12
 */
package darrylbu.icon;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.image.ImageObserver;
import java.net.URL;
import java.util.Objects;

import javax.swing.ImageIcon;

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
public class StretchIcon extends ImageIcon {

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
    
    final ImageObserver io = getImageObserver();
    final Graphics2D g2d = (Graphics2D) g;
    
    // Zooming Functionality
    if (zoom > 1) {
        initPoints();
        
        // Update the Zoomed Draw Size and its difference compared to the normal draw size.
        zoomSize.setLocation(w * zoom, h * zoom);
        zoomDiff.setLocation(zoomSize.x - w, zoomSize.y - h);
        
        // Offset the x and y to zoom centrally
        x -= zoomDiff.x/2;
        y -= zoomDiff.y/2;
        
        // Adjust the view towards the last zoom point.
        pendingZoomPan();
        
        // Lastly, update the width and height to reflect to zoomed size.
        w = zoomSize.x;
        h = zoomSize.y;
        
        // Panning Functionality
        int offX = (int) (panOffsetPercentX * zoomDiff.x);
        int offY = (int) (panOffsetPercentY * zoomDiff.y);
        x = boundedAdd(x, boundedAdd(offX, this.panBuffer.x, -zoomDiff.x/2, zoomDiff.x/2), -zoomDiff.x, 0);
        y = boundedAdd(y, boundedAdd(offY, this.panBuffer.y, -zoomDiff.y/2, zoomDiff.y/2), -zoomDiff.y, 0);
//        System.out.println(this.toString("(x: " + x + ", y:" + y + ", w: " + w + ", h: " + h + ")"));
    }
    
    g2d.drawImage(image, x, y, w, h, io == null ? c : io);
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
  
  /** A boolean for whether or not a zoom pan is pending, which is the effect of panning towards a point when zooming. */
  private boolean pendingZoomPan;
  /** A float with the zoom ratio, which is always at least <code>1.0f</code>. */
  private float zoom;
  /** A Point with the x (width) and y (height) zoom size. The zoom size is calculated by multiplying the regular size by the zoom ratio. */
  private Point zoomSize;
  /** A Point with the x (width) and y (height) difference in icon size after zooming. */
  private Point zoomDiff;
  /** A Point with x and y coordinates for where the last zoom action took place. */
  private Point zoomPoint;
  /** A Point with the x and y coordinate offsets for an active pan. There can only be a pan if the zoom is greater than <code>1.0f</code>. */
  private Point panOffset;
  /** The pan offset, represented as a percentage of the zoom difference. */
  private double panOffsetPercentX, panOffsetPercentY;
  /** A Point with the x and y coordinate offset buffers for an active pan. These numbers are applied to the pan offset when the pan action is stopped. */
  private Point panBuffer;
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
  }

  /**
   * Acts upon a pending zoom pan, with a maximum amount of <code>+-20</code>. The
   * "zoom pan" effect is when a user attempts to zoom in on a specific area of
   * the icon, as opposed to just zooming into the center. This effect feels more
   * responsive and allows for finer control of the viewport.
   */
  private void pendingZoomPan() {
      if (pendingZoomPan) {
          pendingZoomPan = false;
          this.pan(Math.max(-20, Math.min(-zoomPoint.x, 20)), Math.max(-20, Math.min(-zoomPoint.y, 20)));
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
          this.zoomSize = new Point(0, 0);
      if (this.zoomPoint == null)
          this.zoomPoint = new Point(0, 0);
      if (this.zoomDiff == null)
          this.zoomDiff = new Point(0, 0);
      if (this.panBuffer == null)
          this.panBuffer = new Point(0, 0);
      if (this.panOffset == null)
          this.panOffset = new Point(0, 0);
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
      this.panOffset.setLocation((int) (panOffsetPercentX * zoomDiff.getX()), (int) (panOffsetPercentY * zoomDiff.getY()));
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
      this.panBuffer.setLocation(x, y);
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
      
      this.panOffset.setLocation(
              boundedAdd(this.panOffset.x, this.panBuffer.x, -zoomDiff.x/2, zoomDiff.x/2),
              boundedAdd(this.panOffset.y, this.panBuffer.y, -zoomDiff.y/2, zoomDiff.y/2));
      this.panBuffer.setLocation(0, 0);
      
      // Recalculate percentage offsets using new numbers.
      this.panOffsetPercentX = (panOffset.getX() / zoomDiff.getX());
      this.panOffsetPercentY = (panOffset.getY() / zoomDiff.getY());
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
      return String.format("%s (%s: %s,%s) %s%n> %4s: %s | %s | %s | %s%n> %4s: %s (%s,%s) | %s | %s%n",
          "StretchIcon", "Size", this.getImgWidth(), this.getImgHeight(), Objects.toString(fill, ""),
          "Zoom", this.zoom, this.zoomSize, this.zoomDiff, this.zoomPoint,
          "Pan", this.panOffset, this.panOffsetPercentX, this.panOffsetPercentY, this.panBuffer, this.pendingZoomPan);
  }
  
  @Override
  public String toString() {
      return this.toString(null);
  }
}
