package dev.mwhitney.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.util.Objects;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.plaf.ProgressBarUI;
import javax.swing.plaf.basic.BasicProgressBarUI;

import dev.mwhitney.gui.components.better.BetterPanel;
import dev.mwhitney.gui.interfaces.ThemedComponent;
import dev.mwhitney.listeners.simplified.KeyPressListener;
import dev.mwhitney.properties.PiPProperty.THEME_OPTION;
import dev.mwhitney.properties.PiPProperty.THEME_OPTION.COLOR;
import dev.mwhitney.resources.AppRes;
import dev.mwhitney.util.PiPAAUtils;
import net.miginfocom.swing.MigLayout;

/**
 * A window that displays a progression or loading process over time.
 * <p>
 * This window lays out a framework for easily creating loading pop-ups. It is
 * especially useful during application initialization to keep the user updated
 * on what's happening in the background. There are many other potential
 * applications though, including showing download progress.
 * <p>
 * Both the status label and progress value can be updated by calling
 * {@link #update(String, float)}. To update the status label or progress value
 * alone, call {@link #setMessage(String)} or {@link #setProgress(float)}
 * respectively.
 * <p>
 * A banner image may be provided at construction, but is not required.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public class ProgressWindow extends JFrame implements ThemedComponent {
    /** The randomly-generated serial ID. */
    private static final long serialVersionUID = -5754584826165388728L;
    
    /** The {@link Color} of the progress bar's text. */
    private Color colorPBTxt = Color.WHITE;
    /** The {@link ProgressBarUI} for painting the progress bar. */
    private final ProgressBarUI pbUI = new BasicProgressBarUI() {
        @Override
        protected void paintDeterminate(Graphics g, JComponent c) {
            final JProgressBar bar = (JProgressBar) c;
            final int w = c.getWidth(), h = c.getHeight();
            final int min = bar.getMinimum(), max = bar.getMaximum();
            
            final Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw unfilled track.
            g2d.setColor(bar.getBackground());
            g2d.fillRect(0, 0, w, h);
            
            // Draw filled portion of track.
            final int fillWidth = (int) (w * (bar.getValue() - min) / (double) (max - min));
            g2d.setColor(bar.getForeground());
            g2d.fillRect(0, 0, fillWidth, h);
            
            // Draw the String over the bar.
            if (bar.isStringPainted()) {
                final String text = bar.getString();
                final FontMetrics fontMetrics = g2d.getFontMetrics();
                final int tx = (w - fontMetrics.stringWidth(text)) / 2;
                final int ty = (h + fontMetrics.getAscent() - fontMetrics.getDescent()) / 2;
                
                // Choose text color based on position (Currently not in use).
//                Color textColor = (tx + fm.stringWidth(text) / 2 < fillWidth)
//                    ? colorPBTxtFilled   // Filled
//                    : colorPBTxtTrack;   // Empty Track
//                g2.setColor(textColor);
                
                g2d.setColor(colorPBTxt);
                g2d.drawString(text, tx, ty);
            }
            g2d.dispose();
        }
    };
    
    /** The {@link BetterPanel} content pane of the window.  */
    private final BetterPanel panel;
    /** A {@link JLabel} that holds the banner image and message text. */
    private final JLabel banner;
    /** A {@link JProgressBar} that displays the progress made. */
    private final JProgressBar progressBar;
    
    /** An int with the minimum progress value. */
    private final int min;
    /** An int with the maximum progress value. */
    private final int max;
    /** A boolean for whether or not the window should persist past progress completion. */
    private boolean persist = false;
    /** A float with the current progress value. */
    private float progress = 0;
    /** A {@link Runnable} to execute upon reaching progress completion. */
    private Runnable onCompleteRun = null;

    /**
     * Creates a window that shows a progression or loading process over time. Both
     * the status label and progress value can be updated by calling
     * {@link #update(String, float)}. To update the status label or progress value
     * alone, call {@link #setMessage(String)} or {@link #setProgress(float)}
     * respectively.
     * <p>
     * This constructor creates a window without a banner or initial status message,
     * and it uses the default minimum and maximum progress values.
     */
    public ProgressWindow() {
        this(null, null);
    }

    /**
     * Creates a window that shows a progression or loading process over time. Both
     * the status label and progress value can be updated by calling
     * {@link #update(String, float)}. To update the status label or progress value
     * alone, call {@link #setMessage(String)} or {@link #setProgress(float)}
     * respectively.
     * <p>
     * This constructor creates a window without a banner or initial status message.
     * Specify custom minimum and maximum progress values.
     * 
     * @param min - an int with the minimum acceptable progress value.
     * @param max - an int with the maximum acceptable progress value.
     */
    public ProgressWindow(int min, int max) {
        this(null, null, min, max);
    }

    /**
     * Creates a window that shows a progression or loading process over time. Both
     * the status label and progress value can be updated by calling
     * {@link #update(String, float)}. To update the status label or progress value
     * alone, call {@link #setMessage(String)} or {@link #setProgress(float)}
     * respectively.
     * <p>
     * This constructor creates a window without a banner. Specify an initial status
     * message.
     * 
     * @param msg - a String with the initial status message to display.
     */
    public ProgressWindow(String msg) {
        this(null, msg);
    }
    
    /**
     * Creates a window that shows a progression or loading process over time. Both
     * the status label and progress value can be updated by calling
     * {@link #update(String, float)}. To update the status label or progress value
     * alone, call {@link #setMessage(String)} or {@link #setProgress(float)}
     * respectively.
     * <p>
     * This constructor creates a window without a banner. Specify the initial
     * starting message, as well as the custom minimum and maximum progress values.
     * 
     * @param msg - a String with the initial status message to display.
     * @param min - an int with the minimum acceptable progress value.
     * @param max - an int with the maximum acceptable progress value.
     */
    public ProgressWindow(String msg, int min, int max) {
        this(null, msg, min, max);
    }

    /**
     * Creates a window that shows a progression or loading process over time. Both
     * the status label and progress value can be updated by calling
     * {@link #update(String, float)}. To update the status label or progress value
     * alone, call {@link #setMessage(String)} or {@link #setProgress(float)}
     * respectively.
     * <p>
     * This constructor creates a window without an initial status message. Provide
     * a banner image to display.
     * 
     * @param banner - an {@link ImageIcon} to display as a banner.
     */
    public ProgressWindow(ImageIcon banner) {
        this(banner, null);
    }

    /**
     * Creates a window that shows a progression or loading process over time. Both
     * the status label and progress value can be updated by calling
     * {@link #update(String, float)}. To update the status label or progress value
     * alone, call {@link #setMessage(String)} or {@link #setProgress(float)}
     * respectively.
     * <p>
     * This constructor creates a window that uses the default minimum and maximum
     * progress values of {@code 0} and {@code 100}. Provide a banner image and an
     * initial status message to display.
     * 
     * @param banner - an {@link ImageIcon} to display as a banner.
     * @param msg    - a String with the initial status message to display.
     */
    public ProgressWindow(ImageIcon banner, String msg) {
        this(banner, msg, 0, 100);
    }

    /**
     * Creates a window that shows a progression or loading process over time. Both
     * the status label and progress value can be updated by calling
     * {@link #update(String, float)}. To update the status label or progress value
     * alone, call {@link #setMessage(String)} or {@link #setProgress(float)}
     * respectively.
     * <p>
     * This constructor creates a window without an initial status message. Provide
     * a banner image to display, as well as custom minimum and maximum progress
     * values.
     * 
     * @param banner - an {@link ImageIcon} to display as a banner.
     * @param min    - an int with the minimum acceptable progress value.
     * @param max    - an int with the maximum acceptable progress value.
     */
    public ProgressWindow(ImageIcon banner, int min, int max) {
        this(banner, null, min, max);
    }
    
    /**
     * Creates a window that shows a progression or loading process over time. Both
     * the status label and progress value can be updated by calling
     * {@link #update(String, float)}. To update the status label or progress value
     * alone, call {@link #setMessage(String)} or {@link #setProgress(float)}
     * respectively.
     * <p>
     * This constructor assumes nothing, creating a window using all of the passed
     * parameters. Provide a banner image and initial status message to display, as
     * well as custom minimum and maximum progress values.
     * 
     * @param banner - an {@link ImageIcon} to display as a banner.
     * @param msg    - a String with the initial status message to display.
     * @param min    - an int with the minimum acceptable progress value.
     * @param max    - an int with the maximum acceptable progress value.
     */
    public ProgressWindow(ImageIcon banner, String msg, int min, int max) {
        // Throw exception if minimum is less than maximum for some reason.
        if (min > max) throw new IllegalArgumentException("Minimum value cannot be greater than the max!");
        
        // Setup Swing Components - Starting with Panel
        this.panel = new BetterPanel(new MigLayout("insets 0 2", "[grow]", "[grow]"));
        this.panel.setRoundedArc(0);
        
        // Banner and Label
        this.banner = new JLabel(msg);
        this.banner.setIcon(banner);
        this.banner.setHorizontalAlignment(SwingConstants.CENTER);
        this.banner.setHorizontalTextPosition(SwingConstants.CENTER);
        this.banner.setVerticalTextPosition(SwingConstants.BOTTOM);
        this.banner.setIconTextGap(5);
        this.banner.setFont(AppRes.FONT_POPUP);
        // Progress Bar
        this.progressBar = new JProgressBar(min, max);
        this.progressBar.setFont(AppRes.FONT_POPUP);
        this.progressBar.setBorder(null);
        this.progressBar.setBorderPainted(false);
        this.progressBar.setStringPainted(true);
        this.progressBar.setUI(pbUI);
        
        // Use the application-based theme as the default.
        theme(THEME_OPTION.PIPAA);
        
        // Add Swing components to panel.
        this.panel.add(this.banner, "grow, wrap");
        this.panel.add(this.progressBar, "growx, h 20px!, gapbottom " + (hasBanner() ? "20px" : "15px"));
        
        // Set final member variables.
        this.min = min;
        this.max = max;
        
        // Frame
        this.setUndecorated(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setResizable(false);
        this.setBackground(AppRes.COLOR_TRANSPARENT);
        this.setContentPane(this.panel);
        this.setMinimumSize(hasBanner() ? new Dimension(400, 300) : new Dimension(300, 80));
    }
    
    /**
     * A factory method layer on top of {@link JFrame#setIconImage(Image)}.
     * 
     * @param img - an {@link Image} to use as the icon.
     * @return this {@link ProgressWindow} instance.
     */
    public ProgressWindow useIcon(Image img) {
        this.setIconImage(img);
        return this;
    }
    
    /**
     * A factory method layer on top of {@link JFrame#setTitle(String)}.
     * 
     * @param title - a String to use as the title.
     * @return this {@link ProgressWindow} instance.
     */
    public ProgressWindow useTitle(String title) {
        this.setTitle(title);
        return this;
    }
    
    /**
     * Tells this window to persist past reaching its maximum progress value, only
     * closing when {@link #exit()} is manually called.
     * 
     * @return this {@link ProgressWindow} instance.
     */
    public ProgressWindow persist() {
        this.persist = true;
        return this;
    }
    
    /**
     * Tells this window to always display above other windows.
     * 
     * @return this {@link ProgressWindow} instance.
     */
    public ProgressWindow onTop() {
        this.setAlwaysOnTop(true);
        return this;
    }
    
    /**
     * Tells this window to perform setup that will allow a user to close it
     * manually with keyboard input.
     * 
     * @return this {@link ProgressWindow} instance.
     */
    public ProgressWindow userCanClose() {
        this.addKeyListener((KeyPressListener) e -> {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE -> this.exit();
            }
        });
        return this;
    }
    
    /**
     * Displays this window, preparing its content and making it visible. This
     * method is safe to call, but will do nothing, if the window is not
     * {@code displayable} per {@link #isDisplayable()}.
     * 
     * @return this {@link ProgressWindow} instance.
     */
    public ProgressWindow display() {
        this.pack();  // Makes displayable.
        if (this.isDisplayable()) {
            // Set location here to ensure accuracy after pack.
            this.setLocationRelativeTo(null);
            this.setVisible(true);
        }
        return this;
    }
    
    /**
     * Exits this window, making it invisible and disposing of its native screen
     * resources. This is <b>not like calling {@code setVisible(false)}</b>. The
     * window will be disposed and should not be used further.
     */
    public void exit() {
        this.setVisible(false);
        this.dispose();
    }
    
    /**
     * Exits this window, making it invisible and disposing of its native screen
     * resources, after a set amount of seconds. This is <b>not like calling
     * {@code setVisible(false)}</b>. The window will be disposed and should not be
     * used further.
     * <p>
     * The minimum delay before exiting is one second and passing a smaller number
     * will result in the minimum delay being used. Keep in mind that, if the window
     * was not told to {@link #persist()}, it may close earlier than expected by
     * completing and reaching its maximum progress value.
     * 
     * @param seconds - an int with the delay before exiting measured in seconds.
     */
    public void exitAfter(int seconds) {
        final Timer timer = new Timer(Math.max(1, seconds) * 1000, e -> exit());
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * Checks if the window is using a banner image.
     * 
     * @return {@code true} if using a banner image; {@code false} otherwise.
     */
    public boolean hasBanner() {
        return this.banner.getIcon() != null;
    }
    
    /**
     * Provides a {@link Runnable} to run after the progress has reached the maximum
     * value.
     * <p>
     * This method <b>sets</b> the {@link Runnable}; it does not add it. Therefore,
     * calling this method multiple times will do nothing of value, and only the
     * last call will be respected and used.
     * 
     * @param run - the {@link Runnable} to execute when progress has completed.
     */
    public void whenComplete(Runnable run) {
        this.onCompleteRun = run;
    }
    
    /**
     * Checks if the progress has reached the maximum value.
     * 
     * @return {@code true} if progress is complete; {@code false} otherwise.
     */
    public boolean isComplete() {
        return this.progress >= this.max;
    }
    
    /**
     * Performs a progress completion check for the window. If not complete, nothing
     * will happen. However, if progress has reached the maximum value and is not
     * complete, the method executes up to two tasks. First, if the window is not
     * set to {@link #persist()}, then it will exit. Second, the set
     * {@link Runnable} will be executed, if one was provided via
     * {@link #whenComplete(Runnable)}.
     */
    private void complete() {
        // Check first: If not completed, return and do nothing.
        if (!isComplete()) return;
        
        // Exit, unless window is meant to persist past completion.
        if (!this.persist)              PiPAAUtils.invokeNowOrLater(this::exit);
        // Execute on completion run.
        if (this.onCompleteRun != null) PiPAAUtils.invokeNowOrAsync(onCompleteRun::run);
    }
    
    /**
     * Updates the window, setting both the status message and the progress value.
     * 
     * @param msg      - a String with the new status message.
     * @param progress - a float with the new progress value.
     */
    public void update(String msg, float progress) {
        setMessage(msg);
        setProgress(progress);
    }
    
    /**
     * Updates the window, setting both the status message and the progress value.
     * <p>
     * This method will then wait by sleeping the current thread for the passed
     * number of milliseconds. The intention is to give the user time to see the
     * update, though this should be used sparingly to prevent significant slowdowns
     * to the actual progress being tracked.
     * <p>
     * No waiting will occur if the passed delay is less than one, or if the current
     * thread is the event-dispatch thread (EDT).
     * 
     * @param msg      - a String with the new status message.
     * @param progress - a float with the new progress value.
     * @param delay    - an int with the delay in milliseconds.
     */
    public void updateAndWait(String msg, float progress, int delay) {
        update(msg, progress);
        if (!SwingUtilities.isEventDispatchThread() && delay > 0) {
            try {
                Thread.sleep(Duration.ofMillis(delay));
            } catch (InterruptedException ignore) {}  // Ignore and treat as cancel.
        }
    }
    
    /**
     * Gets the current status message.
     * 
     * @return a String with the status message.
     */
    public String getMessage() {
        return this.banner.getText();
    }
    
    /**
     * Sets the status message.
     * 
     * @param msg - a String with the new status message.
     */
    public void setMessage(String msg) {
        PiPAAUtils.invokeNowOrLater(() -> this.banner.setText(Objects.requireNonNullElse(msg, "").trim()));
    }
    
    /**
     * Gets the current progress value.
     * 
     * @return a float with the progress value.
     */
    public float getProgress() {
        return this.progress;
    }
    
    /**
     * Gets the current progress value as a String. Only the decimals to the
     * hundredth place will be shown, with the rest truncated.
     * 
     * @return a String with the truncated progress value.
     */
    public String getProgressStr() {
        return String.format("%.2f", this.progress) + "%";
    }
    
    /**
     * Sets the progress value.
     * <p>
     * This method clamps the value to lie within the set minimum and maximum
     * values. After the value is set, it calls for the progress bar to be
     * refreshed. If any custom code is provided via an override to
     * {@link #progressUpdated(float)}, then that will execute. Finally, a
     * completion check is run.
     * 
     * @param progress - a float with the new progress value.
     */
    public void setProgress(float progress) {
        this.progress = Math.clamp(progress, this.min, this.max);
        refreshProgressBar();
        progressUpdated(this.progress);
        complete();
    }
    
    /**
     * Refreshes the progress bar, updating its displayed value and String. This
     * method automatically ensures these updates happen on the event-dispatch
     * thread (EDT).
     */
    private void refreshProgressBar() {
        PiPAAUtils.invokeNowOrLater(() -> {
            this.progressBar.setValue((int) getProgress());
            this.progressBar.setString(getProgressStr());
        });
    }
    
    /**
     * Called when the progress amount has been updated. This method does nothing by
     * default, but exists so that it may be overridden with custom logic.
     * <p>
     * This method is typically called by {@link #setProgress(float)} whenever a new
     * progress value is provided, and it will be called before any automatic completion
     * checks and subsequent window exits.
     * 
     * @param progress - a float with the new progress value.
     */
    public void progressUpdated(float progress) {}
    
    @Override
    public ProgressWindow theme(THEME_OPTION theme) {
        this.panel.setBackground(theme.color(COLOR.BG));
        this.panel.setBorderColor(theme.color(COLOR.BG_ACCENT));
        this.banner.setForeground(theme.color(COLOR.TXT));
        this.progressBar.setForeground(theme.color(COLOR.SLIDER));
        this.progressBar.setBackground(theme.color(COLOR.SLIDER_EMPTY));
        this.colorPBTxt = theme.color(COLOR.BTN_TXT);
        this.repaint(); // Repaint to ensure new colors are being displayed.
        return this;
    }
}
 