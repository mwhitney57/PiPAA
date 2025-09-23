package dev.mwhitney.media;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;

import dev.mwhitney.exceptions.MediaModificationException;
import dev.mwhitney.listeners.AttributeUpdateAdapter;
import dev.mwhitney.listeners.AttributeUpdateListener;
import dev.mwhitney.main.Binaries;
import dev.mwhitney.main.Binaries.Bin;
import dev.mwhitney.main.CroppedBufferedImage;
import dev.mwhitney.main.Initializer;
import dev.mwhitney.main.PiPProperty.TRIM_OPTION;
import dev.mwhitney.media.PiPMediaAttributes.TYPE;
import dev.mwhitney.resources.PiPAARes;
import dev.mwhitney.util.PiPAAUtils;

/**
 * The media displayed within a PiPWindow.
 * 
 * @author mwhitney57
 */
public class PiPMedia {
    
    /** The source of the PiPMedia as a String. */
    private String src;
    /** The source of the PiPMedia's cache as a String. */
    private String cacheSrc;
    /** The source of the trimmed PiPMedia's cache as a String. */
    private String trimSrc;
    /** The source of the converted PiPMedia's cache as a string. */
    private String convSrc;
    /** The loading status for the PiPMedia. <code>true</code> if loading; <code>false</code> otherwise. */
    private boolean loading;
    /** A boolean which becomes <code>true</code> if this media is marked for deletion after being closed. */
    private boolean markedForDeletion;
    /** A boolean which becomes permanently <code>true</code> when this media is successfully attributed for the first time. */
    private boolean attributed;
    /**
     * The set of PiPMediaAttributes for the PiPMedia.
     * These attributes can be automatically determined using the {@link PiPMediaAttributor}.
     */
    private PiPMediaAttributes attributes;
    /** The attribute update listener that fires when certain attributes are updated. */
    private AttributeUpdateListener listener;
    
    /**
     * Creates a new PiPMedia with a default, empty source.
     */
    public PiPMedia() {
        this("");
    }
    
    /**
     * Creates a new PiPMedia with the passed source String.
     * 
     * @param src - the source for the PiPMedia as a String.
     */
    public PiPMedia(String src) {
        this.markedForDeletion = false;
        this.attributed = false;
        setLoading(true);
        setSrc(src);
    }
    
    /**
     * Creates a copy of the passed PiPMedia.
     * 
     * @param media - the PiPMedia to copy.
     */
    public PiPMedia(PiPMedia media) {
        this.src               = (media.src        == null ? null : new String(media.src));
        this.cacheSrc          = (media.cacheSrc   == null ? null : new String(media.cacheSrc));
        this.trimSrc           = (media.trimSrc    == null ? null : new String(media.trimSrc));
        this.convSrc           = (media.convSrc    == null ? null : new String(media.convSrc));
        this.loading           =  true;
        this.markedForDeletion =  media.markedForDeletion;
        this.attributed        =  media.attributed;
        this.attributes        = (media.attributes == null ? null : new PiPMediaAttributes(media.attributes));
        this.listener          =  media.listener;
    }
    
    /**
     * Converts media with unsupported formats to a similar, supported format. For
     * example, this method will convert <code>WEBP</code> images to either
     * <code>PNG</code> or <code>GIF</code> formats, depending on if the media is
     * animated. After conversion, the path to the converted media is returned as a
     * String. If no conversion was necessary, this method will simply return the
     * media's unmodified source.
     * <p>
     * <b>WARNING:</b> The passed source must be locally-accessible. If the file is
     * not already stored locally, then conversion will not be possible.
     * <p>
     * <b>Note:</b> This method does not alter any properties of this PiPMedia
     * instance <b>other than setting its converted source</b>, if conversion took place.
     * It is otherwise a convenience method for converting invalid media and
     * obtaining the path to the result.
     * 
     * @param source - a String with a custom source to use.
     * @return a String with the path to the converted media, or the PiPMedia's
     *         source if no conversion was necessary.
     */
    public String convertUnsupported(String source) {
        if (source == null || source.trim().isEmpty() || source.indexOf('.') == -1 || !hasAttributes() || getAttributes().getFileExtension() == null)
            return source;
        
        // Ensure conversion cache folder exists.
        PiPAAUtils.ensureExistence(PiPAARes.APP_CONVERTED_FOLDER);
        
        // Setup then determine converted media format and what to use to convert the media.
        final File sourceFile = new File(source);
        String out = PiPAAUtils.slashFix(PiPAARes.APP_CONVERTED_FOLDER + "/") + FilenameUtils.removeExtension(sourceFile.getName());
        Bin convBin = null;
        final MediaExt ext = getAttributes().getFileExtension();
        switch (ext) {
        case AVIF:
            convBin = Bin.IMGMAGICK;
        case TIFF:
        case BMP:
            out += ".png";
            break;
        case WEBP:
            // If animated, change out path and media's type.
            final boolean animated = checkForAnimatedWEBP(sourceFile);
            if (animated) {
                out += ".gif";
                getAttributes().setType(TYPE.GIF);
            } else {
                out += ".png";
                getAttributes().setType(TYPE.IMAGE);
            }
            convBin = Bin.IMGMAGICK;
            break;
        default:
            return source;
        }
        
        // Don't convert again if conversion result already exists.
        final File outFile = new File(out);
        if (!outFile.exists()) {
            try {
                // Conversion -- Use Java ImageIO or External Binary
                if (convBin == null)
                    ImageIO.write(ImageIO.read(sourceFile), out.substring(out.lastIndexOf('.') + 1), outFile);
                else
                    Binaries.exec(Binaries.bin(convBin), "\"" + source + "\"", "\"" + out + "\"");
            } catch (InterruptedException | IOException e) {
                System.err.println("Unexpected error occurred during unsupported media conversion.");
                return source;
            }
        }
        this.setConvSrc(out);
        return out;
    }
    
    /**
     * Checks if the passed File exists and contains animation headers. If these
     * headers are present, it indicates that the passed <code>WEBP</code> media
     * file is animated.
     * 
     * @param file - the File to check for an animated WEBP.
     * @return <code>true</code> if the File exists and is an animated WEBP;
     *         <code>false</code> otherwise.
     */
    private boolean checkForAnimatedWEBP(File file) {
        if (file == null || !file.exists())
            return false;
        
        // Booleans 
        boolean riff = false;
        boolean webp = false;
        boolean vp8x = false;
        boolean anim = false;
        
        // Reads the starting bytes of the file to detect if the file is an animated or static WEBP.
        try (final InputStream in = new FileInputStream(file)) {
            // Set byte array buffer and read first four bytes.
            byte[] buff = new byte[4];
            in.read(buff);

            // WEBP is based on RIFF (Resource Interchange File Format). Check for RIFF header.
            if (buff[0] == 0x52 && buff[1] == 0x49 && buff[2] == 0x46 && buff[3] == buff[2])
                riff = true;

            // Check for WEBP header following RIFF header.
            in.skip(4);
            in.read(buff);
            if (buff[0] == 0x57 && buff[1] == 0x45 && buff[2] == 0x42 && buff[3] == 0x50)
                webp = true;

            in.read(buff); // (16+)
            if (buff[0] == 0x41 && buff[1] == 0x4e && buff[2] == 0x49 && buff[3] == 0x4d)
                vp8x = true;

            // Check for ANIM header : A (41) N (4e) I (49) M (4d)
            in.skip(14);
            in.read(buff);
            if (buff[0] == 0x41 && buff[1] == 0x4e && buff[2] == 0x49 && buff[3] == 0x4d)
                anim = true;
        } catch (Exception e) { e.printStackTrace(); return false; }
        
        return riff && webp && (vp8x || anim);
    }
    
    /**
     * Checks if this media's source points to a location somewhere within the
     * {@link Initializer#APP_CACHE_FOLDER}. Since this method simply compares the
     * beginning of two paths, the media source can be within subfolders of
     * {@link Initializer#APP_CACHE_FOLDER} and this method will still return
     * <code>true</code>.
     * 
     * @return <code>true</code> if this media's source stems from somewhere in the
     *         cache; <code>false</code> otherwise.
     */
    public boolean isFromCache() {
        return (hasAttributes() && getAttributes().isLocal() && hasSrc()
                && new File(getSrc()).getPath().startsWith(PiPAAUtils.slashFix(PiPAARes.APP_CACHE_FOLDER)));
    }
    
    /**
     * Checks if the passed media file already exists within the
     * {@link Initializer#APP_CLIPBOARD_FOLDER}. Since this method is intended to
     * check for duplicate clipboard media, the passed {@link File} is expected to
     * be located within the clipboard cache folder. <b>However, this method will
     * not match the media with itself.</b> If a duplicate exists, it will return
     * the first duplicate's String path. If no duplicate is found, this method
     * returns <code>null</code>.
     * 
     * @param media - a {@link File} pointing to the clipboard media to check for
     *              duplicates against.
     * @return a String path to the duplicate clipboard media, or <code>null</code>
     *         if no duplicates exist.
     */
    public String existsInClipboardCache(final File media) {
        if (!hasAttributes() || getAttributes().getFileExtension() == null
                || !media.getPath().startsWith(PiPAAUtils.slashFix(PiPAARes.APP_CLIPBOARD_FOLDER)))
            return null;
        
        // Either get the path of the duplicate file, or return null (no duplicates).
        final File file = PiPAAUtils.fileDupeInCache(media);
        return (file != null ? file.getPath() : null);
    }
    
    /**
     * Trims the surrounding transparency from an image or GIF source and returns
     * the source of the new, trimmed media. The trimmed media should be written to
     * the cache folder by default.
     * 
     * @param source - a String with the PiPMedia.
     * @return a String with the trimmed PiPMedia, or the passed String source if no
     *         trimming took place.
     * @throws MediaModificationException if there was an error modifying the media.
     */
    public String trimTransparency(String source, TRIM_OPTION option) throws MediaModificationException {
        if (source == null || source.trim().isEmpty() || source.indexOf('.') == -1 ||
                !hasAttributes() || getAttributes().getFileExtension() == null || getAttributes().getType() == null || getAttributes().isVideo())
            return source;
        
        // Set trim source extension based on current trim option.
        final String trimExt = option.is(TRIM_OPTION.STRICT) ? PiPAARes.MEDIA_TRIM_EXT_STRICT : PiPAARes.MEDIA_TRIM_EXT;
        
        // Return the source if it is already PiPAA cropped media.
        final File   inFile         = new File(source);
        final String inFileBaseName = FilenameUtils.getBaseName(inFile.getPath());
        // Only return trimmed source if version exists for passed trim option.
        if (inFileBaseName.endsWith(trimExt))
            return source;
        
        // Ensure trimmed folder exists in cache then build media's cropped version path.
        final String ext = FilenameUtils.getExtension(inFile.getPath());
        String cropFilePath = PiPAARes.APP_TRIMMED_FOLDER;
        PiPAAUtils.ensureExistence(cropFilePath);
        cropFilePath += ("/" + inFileBaseName + trimExt + (ext.isEmpty() ? "" : "." + ext));
        
        // Return the trimmed output if the file exists.
        final File outFile = new File(cropFilePath);
        if (outFile.exists())
            return outFile.getPath();
        
        // Don't even check for transparent pixels if FORCE option is set.
        boolean FORCE = (option == TRIM_OPTION.FORCE);
        if (!FORCE) {
            // Check for any transparent edges on the image/first frame of image.
            CroppedBufferedImage img = null;
            try {
                img = new CroppedBufferedImage(ImageIO.read(inFile));
                img.determineCrop((option == TRIM_OPTION.STRICT ? 0 : 5));
            } catch (IOException ioe) {
                ioe.printStackTrace();
                final Throwable cause = ioe.getCause();
                if (cause != null && cause.getMessage().contains("no End of Image tag present")) {
                    System.err.println("Recognized, rare image error: Forcing trim as fallback in attempt to fix.");
                    FORCE = true;
                } else
                    throw new MediaModificationException("PiPAA may not have permission to view or edit the necessary file(s) or folder(s).");
            } finally {
                if (img != null) img.flush();
            }
                
            // Return original source if no crop necessary for image/first frame, which means it wouldn't be needed at all.
            if (!FORCE && !img.canCrop()) return source;
        }
        
        // Execute trimming command via binary.
        try {
            if (option == TRIM_OPTION.NORMAL || option == TRIM_OPTION.FORCE)
                Binaries.exec(Binaries.bin(Bin.IMGMAGICK), "-background", "none", "-fuzz", "4%", "\"" + source + "\"",
                        "-trim", "-layers", "TrimBounds", "-coalesce", "\"" + outFile.getPath() + "\"");
            else
                Binaries.exec(Binaries.bin(Bin.IMGMAGICK), "-background", "none", "\"" + source + "\"", "-trim",
                        "-layers", "TrimBounds", "-coalesce", "\"" + outFile.getPath() + "\"");
        } catch (InterruptedException e) {
            throw new MediaModificationException("The trimming process was interrupted.");
        } catch (IOException e) {
            throw new MediaModificationException("PiPAA may not have permission to view or edit the necessary file(s) or folder(s).");
        }
        return cropFilePath;
    }
    
    /**
     * Checks if the passed PiPMedia has the same source as this PiPMedia instance.
     * The only criteria that must be met is that the passed object is not
     * <code>null</code>, and the original sources match (case-sensitive.)
     * 
     * @param media - the PiPMedia to check.
     * @return <code>true</code> if the two PiPMedias contain matching sources;
     *         <code>false</code> otherwise.
     */
    public boolean sameSrcAs(final PiPMedia media) {
        return (media != null && this.getSrc().equals(media.getSrc()));
    }
    
    /**
     * Checks if the PiPMedia has a source.
     * 
     * @return <code>true</code> if the media has a non-<code>null</code> source of
     *         a non-zero length; <code>false</code> otherwise.
     */
    public boolean hasSrc() {
        return (this.src != null && this.src.length() > 0);
    }
    
    /**
     * Gets the source of the PiPMedia.
     * 
     * @return a String with the PiPMedia's source.
     */
    public String getSrc() {
        return this.src;
    }
    
    /**
     * Sets the source of the PiPMedia.
     * 
     * @param src - a String with the new source.
     */
    public void setSrc(String src) {
        this.src = (src != null ? src.trim() : src);
    }
    
    /**
     * Checks if the PiPMedia has a cache source, indicating that it is cached.
     * 
     * @return <code>true</code> if the media is cached; <code>false</code> otherwise.
     */
    public boolean isCached() {
        return (this.cacheSrc != null && this.cacheSrc.length() > 0);
    }
    
    /**
     * Gets the cache source of the PiPMedia.
     * 
     * @return a String with the PiPMedia's cache source.
     */
    public String getCacheSrc() {
        return this.cacheSrc;
    }
    
    /**
     * Sets the cache source of the PiPMedia.
     * 
     * @param src - a String with the new cache source.
     * @return this PiPMedia instance.
     */
    public PiPMedia setCacheSrc(String src) {
        this.cacheSrc = (src != null ? src.trim() : src);
        return this;
    }
    
    /**
     * Checks if the PiPMedia has been trimmed before and exists in the proper cache folder.
     * 
     * @return <code>true</code> if the media is trimmed and in the cache; <code>false</code> otherwise.
     */
    public boolean hasTrimSrc() {
        return this.trimSrc != null && this.trimSrc.length() > 0;
    }
    
    /**
     * Gets the trim source of the PiPMedia.
     * 
     * @return a String with the PiPMedia's trim cache src.
     */
    public String getTrimSrc() {
        return this.trimSrc;
    }
    
    /**
     * Sets the trim source of the PiPMedia.
     * 
     * @param src - a String with the new trim cache src.
     * @return this PiPMedia instance.
     */
    public PiPMedia setTrimSrc(String src) {
        this.trimSrc = (src != null ? src.trim() : src);
        return this;
    }
    
    /**
     * Checks if the PiPMedia has been converted before and exists in the proper cache folder.
     * 
     * @return <code>true</code> if the media is converted and in the cache; <code>false</code> otherwise.
     */
    public boolean hasConvertSrc() {
        return this.convSrc != null && this.convSrc.length() > 0;
    }
    
    /**
     * Gets the converted source of the PiPMedia.
     * 
     * @return a String with the PiPMedia's converted cache src.
     */
    public String getConvertSrc() {
        return this.convSrc;
    }
    
    /**
     * Sets the converted source of the PiPMedia.
     * 
     * @param src - a String with the new converted cache src.
     * @return this PiPMedia instance.
     */
    public PiPMedia setConvSrc(String src) {
        this.convSrc = (src != null ? src.trim() : src);
        return this;
    }
    
    /**
     * Checks if the PiPMedia is loading.
     * 
     * @return <code>true</code> if the PiPMedia is loading; <code>false</code> otherwise.
     */
    public boolean isLoading() {
        return this.loading;
    }
    
    /**
     * Sets the loading state of the PiPMedia.
     * 
     * @param loading - a boolean with the loading state of the PiPMedia.
     */
    public void setLoading(boolean loading) {
        this.loading = loading;
    }
    
    /**
     * Checks if this PiPMedia is marked for deletion when it is closed.
     * 
     * @return <code>true</code> if marked for deletion; <code>false</code>
     *         otherwise.
     */
    public boolean isMarkedForDeletion() {
        return this.markedForDeletion;
    }
    
    /**
     * Marks this PiPMedia for deletion when it is closed.
     * 
     * @return this PiPMedia instance.
     */
    public PiPMedia markForDeletion() {
        this.markedForDeletion = true;
        return this;
    }
    
    /**
     * Checks if this PiPMedia is attributed.
     * 
     * @return <code>true</code> if the media is attributed; <code>false</code>
     *         otherwise.
     */
    public boolean isAttributed() {
        return this.attributed;
    }
    
    /**
     * Sets this PiPMedia as attributed.
     */
    public void setAttributed() {
        this.attributed = true;
    }
    
    /**
     * Checks if this media has attributes set. This method will return
     * <code>false</code> if the attributes are <code>null</code>.
     * 
     * @return <code>true</code> if this media has attributes; <code>false</code>
     *         otherwise.
     */
    public boolean hasAttributes() {
        return this.attributes != null;
    }
    
    /**
     * Gets the attributes of the PiPMedia.
     * 
     * @return the set of PiPMediaAttributes for the PiPMedia.
     */
    public PiPMediaAttributes getAttributes() {
        return this.attributes;
    }
    
    /**
     * Sets the attributes of the PiPMedia.
     * 
     * @param attributes - the set of PiPMediaAttributes.
     * @return this PiPMedia instance.
     */
    public PiPMedia setAttributes(PiPMediaAttributes attributes) {
        this.attributes = attributes;
        if (this.listener != null && this.attributes != null) {
            this.attributes.setAttributeUpdateListener(new AttributeUpdateAdapter() {
                @Override
                public void titleUpdated(String title) { listener.titleUpdated(title); }
            });
            listener.allUpdated();
        }
        return this;
    }

    /**
     * Sets the AttributeUpdateListener which fires when the media attributes are
     * updated.
     * 
     * @param aul - the new AttributeUpdateListener.
     */
    public void setAttributeUpdateListener(AttributeUpdateListener aul) {
        this.listener = aul;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s)%n%14s: %s%n%14s: %s%n%14s: %s%n%14s: %s%n%14s: %s%n%14s: %s%n%14s: %s%n---- %s",
                "Media", (hasAttributes() ? "Has Attributes" : "No Attributes"),
                "Src", Objects.toString(this.src, "NONE"),
                "Cache Src", Objects.toString(this.cacheSrc, "NONE"),
                "Trim Src", Objects.toString(this.trimSrc, "NONE"),
                "Conversion Src", Objects.toString(this.convSrc, "NONE"),
                "Is Loading", this.loading,
                "Del. on Close", this.markedForDeletion,
                "Attributed", this.attributed,
                Objects.toString(getAttributes(), ""));
    }
}
