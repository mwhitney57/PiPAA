package dev.mwhitney.media.attribution;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.regex.Pattern;

import dev.mwhitney.listeners.PiPSupplier;
import dev.mwhitney.main.Binaries;
import dev.mwhitney.main.Binaries.Bin;
import dev.mwhitney.main.CFExec;
import dev.mwhitney.media.MediaExt;
import dev.mwhitney.media.MediaURL;
import dev.mwhitney.media.PiPMedia;
import dev.mwhitney.media.PiPMediaAttributes;
import dev.mwhitney.media.WebMediaFormat;
import dev.mwhitney.media.PiPMediaAttributes.SRC_PLATFORM;
import dev.mwhitney.media.PiPMediaAttributes.SRC_TYPE;
import dev.mwhitney.media.PiPMediaAttributes.TYPE;
import dev.mwhitney.media.WebMediaFormat.FORMAT;
import dev.mwhitney.media.exceptions.InvalidMediaException;
import dev.mwhitney.properties.PiPProperty;
import dev.mwhitney.properties.PropertyListener;
import dev.mwhitney.resources.AppRes;
import dev.mwhitney.util.interfaces.PiPEnum;

/**
 * Attributes PiPMedia sources and returns the attribution results.
 * 
 * @author mwhitney57
 */
public class PiPMediaAttributor implements PropertyListener {
    /**
     * A flag for attribution which determines how the attributor behaves.
     * 
     * @author mwhitney57
     * @since 0.9.4
     */
    public enum Flag implements PiPEnum<Flag> {
        // Specific attribution parts. Commented out -- not used for now.
//        EXTENSION,
//        TITLE,
//        ID,
//        TYPE,
//        SRC_TYPE,
//        SRC_PLATFORM,
        
        /**
         * A flag representing a typical, full attribution. A full attribution is the
         * norm, attributing everything it needs or may need so that the application can
         * use the resulting media to the best of its ability.
         */
        FULL,
        /**
         * A flag representing a quicker, less-detailed attribution. Since quick
         * attributions skip over a few time-consuming processes, they save time, but
         * they may not save important properties of the media. Use this type of
         * attribution sparingly, when you just need the simple things.
         * <p>
         * <b>Note:</b> Quick attributions will likely fail if the source is
         * {@link SRC_TYPE#WEB_INDIRECT}, since core attributes cannot be determined
         * quickly.
         */
        QUICK,
        /**
         * A flag which tells the attributor to attribute the raw media without
         * converting the source, ignoring the user configuration entirely.
         * <p>
         * Therefore, using this flag will prevent
         * {@link PiPProperty#CONVERT_WEB_INDIRECT} from having any effect, as this
         * logic will be skipped during attribution.
         */
        RAW_ATTRIBUTION;
    }
    
    // REGEX PATTERNS -- Saved as member variables and pre-compiled during construction for performance.
    /** The RegEx Pattern for fixing spacing issues. */
    private final Pattern rgxSpacer;
    /** The RegEx Pattern for finding an extension in URL queries. */
    private final Pattern rgxSrcQueryExt;
    /** The RegEx Pattern for validating a title and fixing invalid characters. */
    private final Pattern rgxTextValidator;
    /** The RegEx Pattern for attributing a local media title. */
    private final Pattern rgxTitleLocal;
    /** The RegEx Pattern for attributing a media title ID. */
    private final Pattern rgxTitleID;
    /** The RegEx Pattern for attributing a local media's file extension. */
    private final Pattern rgxLocalExt;
    /** The RegEx Pattern for determining if the media is WEB_DIRECT. */
    private final Pattern rgxSrcWebDirect;
    /** The RegEx Pattern for determining if the media is WEB_INDIRECT. */
    private final Pattern rgxSrcWebIndirect;
    
    /**
     * Creates a new PiPMediaAttributor for attributing media sources.
     */
    public PiPMediaAttributor() {
        this.rgxSpacer         = Pattern.compile(" +");
        this.rgxSrcQueryExt    = Pattern.compile(".*\\?.*(" + MediaExt.values('|') + ").*", Pattern.CASE_INSENSITIVE);
        this.rgxTextValidator  = Pattern.compile("[^\\w\\s\\.!@#$^+=-]");
        this.rgxTitleLocal     = Pattern.compile(".*\\\\(.*)\\..*");
        this.rgxTitleID        = Pattern.compile(".*?(?:id|name|title)=(.+?)(?:(?=\\&)|$).*", Pattern.CASE_INSENSITIVE);
        this.rgxLocalExt       = Pattern.compile(".*\\.([^?]{3,5})\\??.*");
        this.rgxSrcWebDirect   = Pattern.compile("(?:https?://)?(?:[a-zA-Z0-9-]+\\.)?(?:[a-zA-Z0-9-]+\\.){1}(?:[a-zA-Z0-9-]+)\\/[-a-zA-Z0-9@:%_\\+.,~#?&//=]+?([-a-zA-Z0-9@:%_+.,~#?&=]+)\\.([a-zA-Z0-9]{2,4})(?:$|[?/])\\??(?:[-a-zA-Z0-9@:%_\\+.~#?&//=]*)?", Pattern.CASE_INSENSITIVE);
        this.rgxSrcWebIndirect = Pattern.compile("(?:https?://)?(?:[a-zA-Z0-9-]+\\.)?(?:[a-zA-Z0-9-]+\\.){1}[a-zA-Z0-9-]*\\/?(?:[-a-zA-Z0-9@:%_\\+.,~#?&//=]*)*", Pattern.CASE_INSENSITIVE);
    }
    
    /**
     * Determines the attributes for the passed {@link PiPMedia}.
     * 
     * @param media - the {@link PiPMedia} to determine attributes for.
     * @param flags - any {@link Flag} values for the attribution process.
     * @return a set of {@link PiPMediaAttributes} for the passed media.
     * @throws InvalidMediaException if there was an error with the passed media or
     *                               during attribution of it.
     */
    public PiPMediaAttributes determineAttributes(PiPMedia media, Flag... flags) throws InvalidMediaException {
        // Set flags default to be a full attribution.
        if (flags == null || flags.length == 0) flags = new Flag[] { Flag.FULL };
        
        String mediaSrc = media.getSrc();
        MediaURL murl = null;
        final PiPMediaAttributes attributes = new PiPMediaAttributes();
        
        // Attribute SRC_TYPE
        attributes.setSrcType(attributeSrcType(mediaSrc));
        
        // Redirect Check for Web Media & Simple Web Direct Extension Attribution
        if (attributes.getSrcType() != SRC_TYPE.LOCAL) {
            murl = genMediaURL(mediaSrc);
            
            // Perform a (basic/soft) check to see if MediaURL redirects to another URL.
            final String redirSrc = murl.redirects();
            if (redirSrc != null) {
                media.setSrc(redirSrc);
                mediaSrc = redirSrc;
                murl = genMediaURL(redirSrc);
            }
            attributes.setWebSrcDomain(murl.domain());
            
            // If the MediaURL points directly to a media file, ensure WEB_DIRECT and file extension are set.
            if (murl.pointsToFile()) {
                System.out.println("Media URL points to a file...");
                attributes.setSrcType(SRC_TYPE.WEB_DIRECT);
                attributes.setFileExtension(MediaExt.parse(murl.contentExt()));
            }
        }
        // Attribute SRC_PLATFORM
        attributes.setSrcPlatform(attributeSrcPlatform(murl, attributes.getSrcType()));
        
        // If WEB_INDIRECT source type OR recognized platform, determine attributes differently. Not run with quick attributions.
        if(Flag.QUICK.notIn(flags) && (attributes.isWebIndirect() || (attributes.isWebDirect() && !attributes.isGenericPlatform()))) {
            final MediaURL murl2 = murl;
            final WebMediaFormat mediatorWMF = new WebMediaFormat();
            
            // Attribute web media from a known (non-Generic) platform.
            final CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
                System.out.println("Running ASYNC Process of Attribution");
                try {
                    final WebMediaFormat wmf = attributeWebMedia(media.getSrc(), attributes.getSrcPlatform());
                    System.out.println("Done web media attribution, here's WMF: \n" + wmf);
                    attributes.setTitle(wmf.title())
                    .setFileExtension(wmf.extension() != null ? wmf.extension() : MediaExt.parse(murl2.format(attributes.getSrcPlatform())))
                    .setWMF(wmf);
                } catch (InvalidMediaException ime) { throw new CompletionException(ime); }
            });
            
            // Web Indirect to Direct Conversion
            // Do not convert if using raw attribution or the user configuration disallows it.
            final Boolean convert = (Flag.RAW_ATTRIBUTION.notIn(flags) && propertyState(PiPProperty.CONVERT_WEB_INDIRECT, Boolean.class));
            if (convert && attributes.isWebIndirect()) {
                System.err.println("Converting Link to Direct: -- " + convert + " and " + attributes.isWebIndirect());
                attributes.setSrcPlatform(attributeSrcPlatform(murl, attributes.getSrcType()));
                final String conversion = convertIndirectSrc(mediaSrc, attributes.getSrcPlatform());
                murl.pointsToFile();
                if (! mediaSrc.equals(conversion)) {
                    // Try to set source type now. If it throws an error, default to the original media source.
                    murl = genMediaURL(conversion);
                    attributes.setSrcType(attributeSrcType(conversion));
                    if (! attributes.isWebDirect() && rgxSrcQueryExt.matcher(conversion).matches()) {
                        attributes.setSrcType(SRC_TYPE.WEB_DIRECT);
                        if (attributes.getFileExtension() == null) {
                            attributes.setFileExtension(MediaExt.parse(rgxSrcQueryExt.matcher(conversion).replaceAll("$1")));
                        }
                    }
                    
                    mediaSrc = conversion;
                    mediatorWMF.setTitle(murl.findNameQuery(attributes.getSrcPlatform()));
                    mediatorWMF.setSrc(mediaSrc);
                }
                System.err.println("AFTER CONVERTING INDIRECT: OG: " + media.getSrc() + " || New: " + mediaSrc);
            }
            
            // Join the two processes running asynchronously.
            try {
                cf.join();
            } catch(CompletionException ce) { throw (InvalidMediaException) ce.getCause(); }
            // If converting indirect to direct found a new source, set it.
            if (mediatorWMF.src() != null)
                attributes.getWMF().setSrc(mediatorWMF.src());
            // If still missing a title, attribute it normally.
            if (!attributes.getWMF().hasTitle()) {
                final String title = attributeTitle(mediaSrc, false, murl);
                attributes.getWMF().setTitle(title);
                attributes.setTitle(title);
            }
            System.out.println("End of indirect to recognized direct attribution reached.");
        } else {
            // Perform standard attribution, typical for local files.
            attributes.setTitle(attributeTitle(mediaSrc, attributes.isLocal(), murl));
            if (attributes.getSrcType() != SRC_TYPE.LOCAL) {
                attributes.getWMF().setID(attributeID(mediaSrc, murl));
            }
            if (attributes.getFileExtension() == null)
                attributes.setFileExtension(attributeExtension(mediaSrc, attributes.getSrcType()));
        }
        attributes.setType(attributeType(attributes.getFileExtension()));
        
        System.out.println("Attribution Results ---------->\n" + attributes);
        return attributes;
    }
    
    /**
     * Attributes indirect web media sources, using specific arguments depending on
     * the source platform. This method is only intended to be called with
     * <code>SRC_TYPE.WEB_INDIRECT</code> media. However, it may also work with
     * direct web media sources.
     * 
     * @param src      - the String media source.
     * @param platform - a {@link SRC_PLATFORM} that matches the media source.
     * @return a {@link WebMediaFormat} with all of the web media attributes.
     * @throws InvalidMediaException if there was an error attributing the web
     *                               media.
     */
    private WebMediaFormat attributeWebMedia(String src, SRC_PLATFORM platform) throws InvalidMediaException {
        final WebMediaFormat format = new WebMediaFormat();
        String platUser = null, platID = null, platDesc = null;
        
        // Pre-attribution checks. Platform-specific and Audio-only.
        final PiPSupplier<String> platSupplier = switch (platform) {
        case X  -> () -> Binaries.execAndFetchSafe(false, Binaries.bin(Bin.GALLERY_DL), "--cookies", AppRes.COOKIES_PATH_ARG, "-K", "\"" + src + "\"");
        default -> null;
        };
        
        System.out.println("Attempting web attribution gets...");
        // Execute multiple binary commands asynchronously and concurrently, including audio-only test, platform-specific and regular attribution.
        final ArrayList<String> cmdOuts = CFExec.runAndGet(
                () -> Binaries.execAndFetchSafe(false, Binaries.bin(Bin.YT_DLP), "\"" + src + "\"", "-I", "1", "--print", "\"%(ext)s\""),
                platSupplier,
                // Not Audio-Only, Cookies Attempts -- Cookies attempts go first. If both cases work, we will likely get more media info.
                () -> Binaries.execAndFetchSafe(false, getWebAttributionArgs(src, platform, Bin.YT_DLP,     false, true ).toArray(new String[0])),
                () -> Binaries.execAndFetchSafe(false, getWebAttributionArgs(src, platform, Bin.GALLERY_DL, false, true ).toArray(new String[0])),
                // Not Audio-Only, No Cookies Attempts -- No cookies attempts are the only ones to work on some sites.
                () -> Binaries.execAndFetchSafe(false, getWebAttributionArgs(src, platform, Bin.YT_DLP,     false, false).toArray(new String[0])),
                () -> Binaries.execAndFetchSafe(false, getWebAttributionArgs(src, platform, Bin.GALLERY_DL, false, false).toArray(new String[0])),
                // Audio-Only, Cookies Attempts -- Cookies attempts go first. If both cases work, we will likely get more media info.
                () -> Binaries.execAndFetchSafe(false, getWebAttributionArgs(src, platform, Bin.YT_DLP,     true,  true ).toArray(new String[0])),
                () -> Binaries.execAndFetchSafe(false, getWebAttributionArgs(src, platform, Bin.GALLERY_DL, true,  true ).toArray(new String[0])),
                // Audio-Only, No Cookies Attempts -- No cookies attempts are the only ones to work on some sites.
                () -> Binaries.execAndFetchSafe(false, getWebAttributionArgs(src, platform, Bin.YT_DLP,     true,  false).toArray(new String[0])),
                () -> Binaries.execAndFetchSafe(false, getWebAttributionArgs(src, platform, Bin.GALLERY_DL, true,  false).toArray(new String[0])))
                .excepts((i, e) -> System.err.println("Exception caught from binary (#" + i + ") in web attribution: " + e))
                .results();
            
        // Check if media is audio first.
        final String ext = cmdOuts.get(0);
        final MediaExt mediaExt = MediaExt.parseSafe(ext);
        final boolean audioMedia = (mediaExt != null && attributeType(mediaExt) == TYPE.AUDIO);
        
        // Platform-specific web media attribution.
        final String platCMDOutput = cmdOuts.get(1);
        if (platCMDOutput != null) {
            switch (platform) {
            case X -> {
                final String[] lines = platCMDOutput.split("\n");
                if (lines.length <= 2 && lines[0].contains("error"))
                    break;
                
                for (int i = 2; i < lines.length; i++) {
                    final String line = lines[i];
                    if (line.startsWith("  ")) continue;
                    
                    final String nextLine = (i + 1 < lines.length ? lines[i + 1].trim() : "");
                    if (line.trim().equals("author['name']"))  platUser = nextLine;
                    if (line.trim().equals("conversation_id")) platID   = nextLine;
                    if (line.trim().equals("content"))         platDesc = nextLine;
                    
                    if (platUser != null && platID != null && platDesc != null) break;
                }
            }
            default -> {}
            }
//                System.out.println("Plat. Pre-Run WMF Results [user/id/desc]: " + platUser + "/" + platID + "/" + platDesc);    //Debug
        }
        
        // Adjust list to only contain the desired outputs.
        cmdOuts.remove(0);  // Removes extension output used for audio-only test.
        cmdOuts.remove(0);  // Removes platform-specific attribution output.
        if (audioMedia) {   // Removes regular, non-audio-only outputs.
            cmdOuts.remove(0);  // Used Cookies
            cmdOuts.remove(0);  // Used Cookies
            cmdOuts.remove(0);
            cmdOuts.remove(0);
        } else {            // Removes audio-only outputs.
            cmdOuts.remove(cmdOuts.size() - 1);
            cmdOuts.remove(cmdOuts.size() - 1);
            cmdOuts.remove(cmdOuts.size() - 1); // Used Cookies (Since removing from end of list here.)
            cmdOuts.remove(cmdOuts.size() - 1); // Used Cookies (Since removing from end of list here.)
        }
        // List should now be of size() 4.
        
        // Execute command and retrieve output, splitting by lines.
        for (int cmd = 0; cmd < cmdOuts.size(); cmd++) {
            final boolean usedYTDLP = (cmd == 0);
            final String cmdOutput = cmdOuts.get(cmd);
            final String[] lines = cmdOutput.split("\n");
            final int mediaNum = (usedYTDLP ? lines.length / 5 : lines.length);
            
            // Check for command execution failure or error, in which case try another binary.
            if (mediaNum < 1 || (lines.length == 0 || lines[0].trim().length() == 0) || lines[lines.length - 1].startsWith("ERROR"))
                continue;
            
            // Selecting the proper media depending on the platform and URL.
            int mediaIndex = 0, mi = 0;
            if (mediaNum > 1 && platform == SRC_PLATFORM.X) {
                for (int i = 1; i <= mediaNum; i++) {
                    if (src.endsWith(String.valueOf(i))) {
                        mediaIndex = i;
                        mi = (i - 1) * 5;
                        break;
                    }
                }
                format.setItem(Math.max(1, mediaIndex));
            }
            // Command completed successfully and used yt-dlp.
            if (usedYTDLP) {
                // Media Start Line Marker || [0] = Title || [1] = ID || [2] = Resolution || [3] = Protocol || [4] = Extension
                // Ensure title contains valid characters. If it is empty, set the format's title to null.
                final String title = spaceFix(rgxTextValidator.matcher(lines[mi + 0]).replaceAll("").trim());
                format.setTitle((title.length() > 1 ? title.substring(1).trim() : "Unknown"));
                
                String id = rgxTextValidator.matcher(lines[mi + 1]).replaceAll("_").trim();
                if (rgxTitleID.matcher(src).matches()) {
                    id = rgxTitleID.matcher(src).replaceAll("$1").trim();
                    rgxTextValidator.matcher(id).replaceAll("_").trim();
                }
                format.setID(lines[1].equalsIgnoreCase("NA") ? null : (id + (mediaIndex >= 1 ? " #" + mediaIndex : "")));
                
                final boolean audioOnly = lines[mi + 2].toLowerCase().contains("audio only");
                format.setAudioOnly(audioOnly);
                final String[] resXY = lines[mi + 2].split("x");
                try {
                    if (!audioOnly && resXY.length >= 2)
                        format.setResolution(Integer.valueOf(resXY[0]), Integer.valueOf(resXY[1]));
                } catch (NumberFormatException nfe) {}
                
                format.setFormat((lines[mi + 3].toLowerCase().startsWith("http")) ? FORMAT.HTTP : (lines[mi + 3].toLowerCase().startsWith("m3u8") ? FORMAT.HLS : null));
                format.setExtension(lines[mi + 4].trim());
            }
            // Command completed successfully and used gallery-dl.
            else {
                if (mediaIndex != 0) mediaIndex -= 1;
                
                format.setFormat(FORMAT.GALLERY_DL);
                String fileName = lines[mediaIndex].substring(lines[mediaIndex].lastIndexOf("\\") + 1);
                String id = "";
                format.setExtension(fileName.substring(fileName.lastIndexOf('.') + 1));
                fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                if (platUser != null && platID != null) {
                    id = platID;
                    fileName = platUser + (platDesc != null && !rgxTextValidator.matcher(platDesc).replaceAll("").trim().isEmpty() ? " - " + platDesc : "");
                } else if(fileName.indexOf(' ') != -1) {
                    id = fileName.substring(0, fileName.indexOf(' '));
                    fileName = fileName.substring(id.length() + 1);
                }
                format.setTitle(spaceFix(rgxTextValidator.matcher(fileName).replaceAll("").trim()));
                format.setID(rgxTextValidator.matcher(id).replaceAll("_").trim());
            }
            // Indicate if the working attempt used cookies. Improves success rate across the web, making more sites work.
            if (cmd > 1) format.setUsedCookies(true);
            break;
        }
        return format;
    }
    
    /**
     * Gets the proper arguments for setting the remote media based on the passed
     * objects, including the {@link SRC_PLATFORM} of the PiPMedia.
     * <p>
     * A {@link Bin} override may also be specified for use instead of the
     * automatically selecting one. Passing a <code>null</code> value for this will
     * simply equate to no override.
     * 
     * @param src         - a String with the media source.
     * @param media       - the PiPMedida object to get remote arguments for.
     * @param binOverride - a {@link Bin} with an override to use a specific binary.
     *                    A <code>null</code> or invalid value will not override.
     * @param audioOnly   - a boolean for whether or not the arguments should treat
     *                    the media as audio only.
     * @param cookies     - a boolean for whether or not to include the cookies in
     *                    the arguments.
     * @return a List<String> of arguments for setting the remote media.
     */
    private ArrayList<String> getWebAttributionArgs(final String src, SRC_PLATFORM platform, final Bin binOverride, final boolean audioOnly, final boolean useCookies) {
        // Cannot proceed without proper, non-null media.
        Objects.requireNonNull(src, "The String source must be non-null to retrieve web attribution arguments.");
        
        /*
         * Create ArrayList ahead of time with a greater initial capacity, default is 10 after adding an element.
         * Increase as arguments are added in development. Amount must meet/exceed maximum number of possible arguments.
         * Otherwise, the list will grow in size as added elements reach the capacity, which is costly by comparison.
         */
        final ArrayList<String> webArgs = new ArrayList<String>(25);
        
        // Determine which binary to use, defaulting to the calculated value if no override was provided.
        boolean useYTDLP = false;   // Current default is false.
        useYTDLP = (binOverride == Bin.YT_DLP ? true : (binOverride == Bin.GALLERY_DL ? false : useYTDLP));
        
        // Change Commands Depending on Remote Source Platform (if any)
        if (platform == null) platform = SRC_PLATFORM.GENERIC;
        switch (platform) {
        case YOUTUBE:
            useYTDLP = (binOverride != null ? useYTDLP : true);
        case X:
        case REDDIT:
        default:
            // Args = [0] Binary [1-2] Cookies
            webArgs.add(useYTDLP ? Binaries.bin(Bin.YT_DLP) : Binaries.bin(Bin.GALLERY_DL));
            if (useCookies) {
                webArgs.add("--cookies");
                webArgs.add(AppRes.COOKIES_PATH_ARG);
            }
            
            // Platform-Specific Intermediate Arguments
            switch(platform) {
            case YOUTUBE:
            case REDDIT:
                break;
            case X:
            default:
                if (!useYTDLP) {
                    webArgs.add("--range");
                    webArgs.add("1");
                }
                break;
            }
            
            // Arguments for All Platforms, Including Generic
            if (!useYTDLP) {
                webArgs.add("--no-download");
                webArgs.add("\"" + src + "\"");
            } else {
                if (audioOnly) {
                    webArgs.add("-S");
                    webArgs.add("aext");
                }
                webArgs.add("--ffmpeg-location");       // yt-dlp can use to help select the best format.
                webArgs.add(AppRes.FFMPEG_LOC_ARG);
                webArgs.add("--no-playlist");
                webArgs.add("-I");
                webArgs.add("1");
                webArgs.add("--print");
                webArgs.add("\"T %(title)s\"");
                webArgs.add("--print");
                webArgs.add("\"%(id)s\"");
                webArgs.add("--print");
                webArgs.add("\"%(resolution)s\"");
                webArgs.add("--print");
                webArgs.add("\"%(protocol)s\"");
                webArgs.add("--print");
                webArgs.add("\"%(ext)s\"");
                webArgs.add("\"" + src + "\"");
            }
            break;
        }
        
//        System.out.println("Media Attribution Web Args:\n---> " + String.join(" ", webArgs) + "\n");    // Debug
        return webArgs;
    }
    
    /**
     * Performs extension attribution given a media source and its source type.
     * 
     * @param src  - the String media source.
     * @param type - a {@link SRC_TYPE} which matches the media source.
     * @return a {@link MediaExt} which matches the media source, or
     *         <code>null</code> if the extension found in the source does not match
     *         accepted values.
     * @throws InvalidMediaException if there was an error attributing the media or
     *                               <code>SRC_TYPE.WEB_INDIRECT</code> is passed.
     */
    private MediaExt attributeExtension(String src, PiPMediaAttributes.SRC_TYPE type) throws InvalidMediaException {
        switch(type) {
        case LOCAL:
            return MediaExt.parse(rgxLocalExt.matcher(src).replaceAll("$1"));
        case WEB_DIRECT:
            return MediaExt.parse(rgxSrcWebDirect.matcher(src).replaceAll("$2"));
        // No case for WEB_INDIRECT -- Determined elsewhere, do not call this method with WEB_INDIRECT.
        default:
            throw new InvalidMediaException("Error attributing media: Cannot determine media file extension here from an indirect source.");
        }
    }
    
    /**
     * Performs title attribution given a media's source, whether or not it is
     * local, and a generated MediaURL.
     * <p>
     * This method will immediately get and return the title using a regular
     * expression if the passed local boolean is <code>true</code>. Otherwise, it
     * will begin by checking for any valid name queries in the passed MediaURL. If
     * this fails, the method will attempt to grab the name from the MediaURL's
     * path. If this also fails, another regular expression will be used.
     * <p>
     * If none of these attempts succeeded in retrieving a valid title String, the
     * method will return a placeholder value of <code>Unknown</code>.
     * 
     * @param src   - the String media source.
     * @param local - a boolean for whether or not the media is local.
     * @param murl  - a MediaURL which contains possible title/name queries to help
     *              with title attribution.
     * @return a String with the media's title, which may be a rough guess for some
     *         non-local media.
     */
    private String attributeTitle(String src, boolean local, MediaURL murl) {
        // If the source is local, simply return the value from the regex.
        if (local)
            return rgxTitleLocal.matcher(src).replaceAll("$1");
        
        String title = null;
        
        // Prefer MediaURL Query
        if (murl != null && murl.hasNameQuery(null) != null)
            title = murl.findNameQuery(null);
        // Query Not Found, Attempt Normal Attribution
        if (murl != null && title == null || title.length() < 1)
            title = murl.getNameFromPath();
        // No Name in Path, Attempt RGX Attribution
        if (title == null || title.length() < 1)
            title = rgxSrcWebDirect.matcher(src).replaceAll("$1");
        
        // All Attributions Returned an Invalid or Empty Result, Use Fallback
        if (title == null || title.length() < 1)
            return "Unknown";
        // Attribution Succeeded -- Return Title.
        else
            return urlFix(title).trim();
    }
    
    /**
     * Performs ID attribution given a media's source and a generated MediaURL. This
     * method will begin by checking for any valid ID queries in the passed
     * MediaURL. If this fails, a regular expression will be used. If neither of
     * these attempts succeeded in retrieving a valid title String,
     * <code>null</code> will be returned.
     * 
     * @param src  - the String media source.
     * @param murl - a MediaURL which contains possible ID queries to help with
     *             attribution.
     * @return a String with the media's ID, or <code>null</code> if one could not
     *         be found.
     */
    private String attributeID(String src, MediaURL murl) {
        // Prefer MediaURL Query
        if (murl != null && murl.hasIDQuery(null) != null) {
            return murl.findIDQuery(null);
        }
        
        // Query Not Found, Attempt RGX Attribution
        return rgxTitleID.matcher(src).matches() ? rgxTitleID.matcher(src).replaceAll("$1").trim() : null;
    }
    
    /**
     * Performs TYPE attribution based on the extension.
     * 
     * @param ext - the MediaExt used for determining the type.
     * @return a {@link PiPMediaAttributes.TYPE} for the type of media based on the extension.
     * @throws InvalidMediaException if the extension is <code>null</code> or not an
     *                               acceptable value.
     */
    private PiPMediaAttributes.TYPE attributeType(MediaExt extension) throws InvalidMediaException {
        if (extension == null)
            throw new InvalidMediaException("Error attributing media: Cannot determine media type from source: null extension.");
        
        switch(extension) {
        case M3U8:
        case MPD:
            return PiPMediaAttributes.TYPE.PLAYLIST;
        case MP4:
        case MKV:
        case WEBM:
        case M4V:
        case MOV:
        case AVI:
        case TS:
            return PiPMediaAttributes.TYPE.VIDEO;
        case PNG:
        case JPG:
        case JPEG:
        case TIFF:
        case BMP:
        case AVIF:
            return PiPMediaAttributes.TYPE.IMAGE;
        case GIF:
        case WEBP:
            return PiPMediaAttributes.TYPE.GIF;
        case MP3:
        case M4A:
        case WAV:
        case WMA:
        case OGG:
        case FLAC:
        case OPUS:
            return PiPMediaAttributes.TYPE.AUDIO;
        }
        throw new InvalidMediaException("Error attributing media: Cannot determine media type from source: invalid extension.");
    }
    
    /**
     * Performs source type attribution given a media source.
     * Source type examples includes: <code>LOCAL</code> and <code>WEB_DIRECT</code>.
     * 
     * @param src - the String media source.
     * @return a {@link SRC_TYPE} which matches the media source.
     * @throws InvalidMediaException if the media's source type could not be determined and/or the media does not exist.
     */
    private PiPMediaAttributes.SRC_TYPE attributeSrcType(String src) throws InvalidMediaException {
        if(rgxSrcWebDirect.matcher(src).matches()) {
            // Ensure that the direct regex did not mistake certain web pages with direct media links.
            final String ext = rgxSrcWebDirect.matcher(src).replaceAll("$2");
            System.out.println("Attributing src type found ext: " + ext);
            if (MediaExt.matchesAny(ext))
                return PiPMediaAttributes.SRC_TYPE.WEB_DIRECT;
            else
                return PiPMediaAttributes.SRC_TYPE.WEB_INDIRECT;
        } else if(rgxSrcWebIndirect.matcher(src).matches()) {
            return PiPMediaAttributes.SRC_TYPE.WEB_INDIRECT;
        }
        final File media = new File(src);
        if (media.exists()) {
            return PiPMediaAttributes.SRC_TYPE.LOCAL;
        }
        throw new InvalidMediaException("Error attributing media: Cannot determine media source type. Does the media exist?");
    }
    
    /**
     * Performs source platform attribution given a media source and its source type.
     * Source platform examples include: <code> X, YOUTUBE, REDDIT</code>.
     * 
     * @param src - the String media source.
     * @param type - a {@link SRC_TYPE} type, such as <code>LOCAL</code> or <code>WEB_DIRECT</code>.
     * @return the {@link SRC_PLATFORM} which the media is located on, or <code>null</code> if the media is local.
     * @throws InvalidMediaException
     */
    private PiPMediaAttributes.SRC_PLATFORM attributeSrcPlatform(MediaURL murl, PiPMediaAttributes.SRC_TYPE type) throws InvalidMediaException {
        if (murl == null)
            return null;
        
        switch(type) {
        // If the source is local, then there is no external source platform/host attribute.
        case LOCAL:
            return null;
        default:
            // See if source matches a specific platform or should just be considered generic.
            final SRC_PLATFORM plat = SRC_PLATFORM.hostMatchesAny(murl.domain());
            if (plat != null)
                return plat;
            return SRC_PLATFORM.GENERIC;
        }
    }
    
    /**
     * Attempts to convert a <code>WEB_INDIRECT</code> media source to a <code>WEB_DIRECT</code> one.
     * This method is not guaranteed to work. It is possible that no direct link will be present,
     * in which case, this method will just return the original source String.
     * 
     * @param src - the String indirect web media source.
     * @return the converted source String, or the passed String if conversion was not possible.
     */
    private String convertIndirectSrc(String src, SRC_PLATFORM platform) {
        // Change arguments depending upon the platform.
        String[] args = null;
        boolean useYTDLP   = true;
        switch(platform) {
        case X:
            useYTDLP = false;
            break;
        default:
            break;
        }
        int attempts = 1;
        while(attempts <= 2) {
            System.out.println("Attempting web indirect src conversion...");
            attempts++;
            
            // Determine command arguments based on binary selection.
            if (useYTDLP)
                args = new String[] { Binaries.bin(Bin.YT_DLP), "--cookies", AppRes.COOKIES_PATH_ARG, "\"" + src + "\"", "--get-url", "-I", "1", "-f", "b" };
            else
                args = new String[] { Binaries.bin(Bin.GALLERY_DL), "--cookies", AppRes.COOKIES_PATH_ARG, "\"" + src + "\"", "--get-url" };
            
            // Check for command execution failure or error, in which case try another binary.
            final String cmdOutput = Binaries.execAndFetchSafe(false, args);
//            System.err.println(cmdOutput);
            String[] lines = cmdOutput.split("\n");
            if ((lines.length == 0 || lines[0].trim().length() == 0)|| lines[lines.length - 1].startsWith("ERROR")) {
                useYTDLP = !useYTDLP;
                continue;
            }
            
            // Command succeeded. If it gave a good URL, return that.
            if (!lines[lines.length - 1].trim().equalsIgnoreCase("NA")) {
                // Remove all non-original quality media lines.
                lines = Arrays.stream(lines).filter(l -> l.trim().startsWith("http")).toArray(String[]::new);
                
                // Selecting the proper media depending on the original URL, defaulting to 0 (1st, and maybe only, media.)
                int mediaIndex = 0;
                if (lines.length > 1) {
                    for (int i = 1; i <= lines.length; i++) {
                        if (src.endsWith("/" + String.valueOf(i))) {
                            mediaIndex = i - 1;
                            break;
                        }
                    }
                }
                System.err.println(lines[mediaIndex]);
                return lines[mediaIndex].trim();
            }
        }
        
        return src;
    }
    
    /**
     * Generate and return a {@link MediaURL} using the passed source String. The
     * returned {@link MediaURL} can be <code>null</code> if the passed String was
     * not valid or accepted.
     * 
     * @param src - a String with the source.
     * @return a {@link MediaURL} for the passed String source, or <code>null</code>
     *         if the source was not acceptable.
     */
    private MediaURL genMediaURL(String src) {
        MediaURL murl = null;
        try {
            murl = new MediaURL(src);
        } catch (MalformedURLException | URISyntaxException ex) { ex.printStackTrace(); }
        return murl;
    }
    
    /**
     * Fixes space issues with the passed String by removing leading and trailing
     * spaces, as well as any occurrences of 2+ space, replacing them with just one.
     * 
     * @param str - the String to space fix.
     */
    private String spaceFix(String str) {
        return rgxSpacer.matcher(str.trim()).replaceAll(" ").trim();
    }
    
    /**
     * Fixes the passed String by decoding any URL-encoded String.
     * 
     * @param str - the URL-encoded String to decode.
     * @return the passed String, but URL-decoded.
     */
    private String urlFix(String str) {
        return URLDecoder.decode(str, StandardCharsets.UTF_8);
    }
    
    // Do Nothing Unless Overridden
    @Override
    public void propertyChanged(PiPProperty prop, String value) {}
    @Override
    public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return null; }
}
