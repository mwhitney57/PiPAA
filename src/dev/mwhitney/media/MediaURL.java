package dev.mwhitney.media;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;

import de.malkusch.whoisServerList.publicSuffixList.PublicSuffixListFactory;
import dev.mwhitney.exceptions.InvalidMediaExtensionException;
import dev.mwhitney.media.PiPMediaAttributes.SRC_PLATFORM;

/**
 * A wrapper for the {@link URL} class specifically designed for links to media.
 * 
 * @author mwhitney57
 */
public class MediaURL {
    
//    final static private String[] DIRECT_FILE_TYPES = { "image", "video", "audio", "application" };
            
    /** A String[] with a possible <b>name</b> query keys. */
    final static private String[] QUERIES_NAME = { "name", "filename", "file" };
    /** A String[] with a possible <b>ID</b> query keys. */
    final static private String[] QUERIES_ID   = { "id", "cid", "content_id" };
    /** A String[] with a possible <b>format</b> or <b>extension</b> query keys. */
    final static private String[] QUERIES_FORMAT  = { "ext", "extension", "format", "form" };
    
    /**
     * The URL class that MediaURLs wrap around. This object determines the validity
     * of the passed sources and is used for retrieving potential media information.
     */
    private URL url;
    
    /** A boolean for whether or not this MediaURL is valid. */
    private boolean valid;
    /** A String with the URL's full domain, including subdomains. */
    private String fullDomain;
    /** A String with the URL's registrable domain, which excludes subdomains. */
    private String domain;
    /** A String with the URL's full path. */
    private String fullPath;
    /** A String with the last directory of the URL's path. */
    private String path;
    /** A HashMap<String, String> with the URL's queries, represented as K/V pairs. */
    private HashMap<String, String> queries;
    /** A String with the media's content type extension. */
    private String contentTypeExt;

    /**
     * Creates a MediaURL with the passed String source. MediaURLs act as wrappers
     * for the {@link URL} class, allowing for quick and easy retrieval of potential
     * media at the source.
     * <p>
     * Normally, passing a String URL without HTTP/S headers will throw an
     * exception, but this class will attempt to automatically convert such sources
     * by appending <code>https://</code>. This may still ultimately fail, but it is
     * another layer of protection.
     * 
     * @param src - a String with the potential media source.
     * @throws MalformedURLException if the passed source is not a valid URL.
     */
    public MediaURL(String src) throws MalformedURLException {
        this.url = new URL(httpify(src));
        
        // Will only turn true if the above does not throw an exception.
        this.valid = true;
        setDomain();
        setPath();
        setQueries();
        
        // Use Library to Consistently Convert a Full Domain to Exclude the Subdomains.
        try {
            final PublicSuffixListFactory factory = new PublicSuffixListFactory();
            final Properties properties = factory.getDefaults();
            properties.setProperty(PublicSuffixListFactory.PROPERTY_LIST_FILE, "/dev/mwhitney/resources/effective_tld_names.dat");
            this.domain = factory.build(properties).getRegistrableDomain(fullDomain());
            System.out.println("Public Suffix thing worked: " + domain() + " from " + fullDomain());
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    /**
     * Attempts to retrieve and set the full domain from the URL. If the MediaURL is
     * invalid, this method will do nothing.
     */
    private void setDomain() {
        if (notValid())
            return;
        
        this.fullDomain = url.getHost();
    }
    
    /**
     * Attempts to retrieve and set the full path from the URL. If a full path
     * exists, a shorter, final path destination is saved. If a path does not exist
     * at all or the MediaURL is invalid, this method will do nothing.
     */
    private void setPath() {
        // Don't set path if the URL isn't valid or path is not present.
        if (notValid() || url.getPath() == null || url.getPath().length() <= 1)
            return;
        
        this.fullPath = url.getPath().trim();
        if (this.fullPath.endsWith("/"))
            this.fullPath = this.fullPath.substring(0, this.fullPath.length() - 1);
        this.path = this.fullPath.substring(this.fullPath.lastIndexOf('/'));
    }
    
    /**
     * Attempts to retrieve and store any and all queries from the URL. If no
     * queries exist or the MediaURL is invalid, this method will just set queries
     * to be <code>null</code>.
     */
    private void setQueries() {
        // Don't set path if the URL isn't valid or no queries.
        if (notValid() || url.getQuery() == null) {
            this.queries = null;
            return;
        }
        
        final String[] queryArr = url.getQuery().split("&");
        final HashMap<String, String> querySet = new HashMap<String, String>();
        for (final String query : queryArr) {
            final String[] querySplit = query.split("=", 2);
            if (querySplit.length >= 2)
                querySet.put(querySplit[0].toLowerCase(), querySplit[1]);
        }
        
        this.queries = querySet;
    }

    /**
     * Ensures that a passed source starts with the http(s) URL header. The
     * {@link URL} class does not accept sources that are missing <code>http</code>
     * or <code>https</code>. If the source already has one of these two at the
     * beginning of the String, then the method will just return the same String.
     * 
     * @param src - the media source String.
     * @return the media source String, guaranteed to have <code>http</code> or
     *         <code>https</code> at the beginning of it.
     */
    private String httpify(String src) {
        if (!src.toLowerCase().startsWith("http"))
            src = "https://" + src;
        return src;
    }
    
    /**
     * Checks if the MediaURL has a redirect query, which is present with some media
     * links (i.e. Google Images). If a query is found, its value will be returned.
     * Otherwise, this method returns <code>null</code>.
     * 
     * @return the redirect destination URL, or null if no redirect query was found
     *         in the MediaURL.
     */
    public String redirects() {
        // Do not attempt if the MediaURL is invalid.
        if (notValid())
            return null;
        
        final String redir = findQueryIn(null, new String[]{ "url", "redirect" });
        if (redir != null && redir.length() > 0)
            return URLDecoder.decode(redir, StandardCharsets.UTF_8);
        return null;
    }
    
    /**
     * Checks the destination of the MediaURL, seeing what type of content it points
     * to. The result of this check is stored in the MediaURL as the
     * <code>contentTypeExt</code>. If this process fails or the content is not
     * valid (regardless of if accepted by PiPAA), then the content type will be set
     * to <code>null</code> and this method will return <code>false</code>.
     * 
     * @return <code>true</code> if the destination check succeeded;
     *         <code>false</code> otherwise.
     */
    public boolean checkDestination() {
        // Do not attempt if the MediaURL is invalid.
        if (notValid())
            return false;
        System.out.println("about to check destination.");
        // Attempt to open a connection to the source.
        String cont = null;
        try {
            final URLConnection c = url.openConnection();
            cont = c.getContentType();
        } catch (IOException e) { e.printStackTrace(); }
        System.out.println("passed first check destination.");
        
        // Connection failed, do not proceed.
        if (cont == null)
            return false;
        
        System.out.println("about to second check destination.");
        cont = (cont.indexOf(';') != -1 ? cont.substring(0, cont.indexOf(';')).trim() : cont.trim());
        System.out.println(cont);
        final String[] contentType = cont.split("/");
        if (contentType.length < 2 || contentType[0] == null && contentType[1] == null)
            return false;
        System.out.println("passed second check destination.");
            
//        this.contentTypeHeader = contentType[0];
        this.contentTypeExt    = contentType[1];
        return true;
    }
    
    /**
     * Checks if this MediaURL points to a file. It checks the content type of the
     * URL, and if the extension of the content matches a {@link MediaExt} that
     * PiPAA accepts, this method will return <code>true</code>.
     * 
     * @return <code>true</code> if the MediaURL points to a valid media file;
     *         <code>false</code> otherwise.
     */
    public boolean pointsToFile() {
        System.err.println("Checking if MediaURL points to a file...");
        if (this.contentTypeExt == null && !checkDestination())
            return false;
        System.err.println("Done checking destination");
        try {
            MediaExt.parse(contentTypeExt);
            this.contentTypeExt = this.contentTypeExt.toUpperCase();
        } catch (InvalidMediaExtensionException imee) { return false; }
        
        System.err.println("MediaURL points to: " + this.contentTypeExt);
        return true;
    }
    
    /**
     * Gets the content type extension, which will be <code>null</code> if the
     * destination has not been checked via <code>checkDestination()</code> or
     * <code>pointsToFile()</code>. The extension may also be <code>null</code> in
     * others cases. If there was an extension in the content type, but it was not
     * one accepted by PiPAA, this value will be <code>null</code>. It may also
     * occur if no extension was detected.
     * 
     * @return a String with the extension, or <code>null</code> if it has not been
     *         checked or is not valid.
     */
    public String contentExt() {
        return this.contentTypeExt;
    }

    /**
     * Gets the format or extension by first attempting to find the format in the
     * queries. If that is unsuccessful, then the content type extension will be
     * used. However, that may also turn out to be <code>null</code>.
     * 
     * @param plat - the {@link SRC_PLATFORM} which the media is located on.
     * @return a String with the format from either the URL queries or the content
     *         type.
     */
    public String format(SRC_PLATFORM plat) {
        final String queryResult = findFormatQuery(plat);
        return (queryResult != null ? queryResult : (pointsToFile() ? contentExt() : null));
    }
    
    /**
     * Checks if the MediaURL turned out to <b>valid</b>. The only condition for a
     * MediaURL to be considered valid is its URL being valid and accepted.
     * 
     * @return <code>true</code> if <b>valid</b>; <code>false</code> otherwise.
     */
    public boolean isValid() {
        return this.valid;
    }
    
    /**
     * Checks if the MediaURL turned out to <b>invalid</b>. The only condition for a
     * MediaURL to be considered valid is its URL being valid and accepted.
     * 
     * @return <code>true</code> if <b>invalid</b>; <code>false</code> otherwise.
     */
    public boolean notValid() {
        return (!this.valid);
    }
    
    /**
     * Checks if the MediaURL has any queries.
     * 
     * @return <code>true</code> if the URL has queries; <code>false</code>
     *         otherwise.
     */
    public boolean hasQueries() {
        return this.queries != null && !this.queries.isEmpty();
    }
    
    /**
     * Checks if any of the queries in the passed String array match with a query
     * within the MediaURL. This method returns the query key which was found in
     * the passed array.
     * 
     * @param plat     - the {@link SRC_PLATFORM} which the media is located on.
     * @param queryArr - the String[] with the queries to check for.
     * @return a String with the key of the first query that matched, or
     *         <code>null</code> if there were no matches.
     */
    private String hasQueryIn(SRC_PLATFORM plat, String[] queryArr) {
        if (!hasQueries())
            return null;

        for (final String query : queryArr) {
            if (queries().get(query) != null)
                return query;
        }
        return null;
    }
    
    /**
     * Finds if any of the queries in the passed String array match with a query
     * within the MediaURL. This method returns the query value which was found in
     * the passed array.
     * 
     * @param plat     - the {@link SRC_PLATFORM} which the media is located on.
     * @param queryArr - the String[] with the queries to check for.
     * @return a String with the value of the first query that matched, or
     *         <code>null</code> if there were no matches.
     */
    private String findQueryIn(SRC_PLATFORM plat, String[] queryArr) {
        if (!hasQueries())
            return null;
        
        for (final String query : queryArr) {
            if (queries().get(query) != null)
                return queries().get(query);
        }
        return null;
    }
    
    /**
     * Checks if the MediaURL queries contain a name query for the media.
     * 
     * @param plat - the {@link SRC_PLATFORM} which the media is located on.
     * @return a String with the first query key that matched, or <code>null</code>
     *         if there was no match.
     */
    public String hasNameQuery(SRC_PLATFORM plat) {
        return hasQueryIn(plat, QUERIES_NAME);
    }
    
    /**
     * Checks if the MediaURL queries contain an ID query for the media.
     * 
     * @param plat - the {@link SRC_PLATFORM} which the media is located on.
     * @return a String with the first query key that matched, or <code>null</code>
     *         if there was no match.
     */
    public String hasIDQuery(SRC_PLATFORM plat) {
        return hasQueryIn(plat, QUERIES_ID);
    }
    
    /**
     * Checks if the MediaURL queries contain a format query for the media.
     * 
     * @param plat - the {@link SRC_PLATFORM} which the media is located on.
     * @return a String with the first query key that matched, or <code>null</code>
     *         if there was no match.
     */
    public String hasFormatQuery(SRC_PLATFORM plat) {
        return hasQueryIn(plat, QUERIES_FORMAT);
    }
    
    /**
     * Finds a name query value in the the MediaURL's queries, if one exists.
     * 
     * @param plat - the {@link SRC_PLATFORM} which the media is located on.
     * @return a String with the first query value that matched, or
     *         <code>null</code> if there was no match.
     */
    public String findNameQuery(SRC_PLATFORM plat) {
        return findQueryIn(plat, QUERIES_NAME);
    }
    
    /**
     * Finds an ID query value in the the MediaURL's queries, if one exists.
     * 
     * @param plat - the {@link SRC_PLATFORM} which the media is located on.
     * @return a String with the first query value that matched, or
     *         <code>null</code> if there was no match.
     */
    public String findIDQuery(SRC_PLATFORM plat) {
        return findQueryIn(plat, QUERIES_ID);
    }
    
    /**
     * Finds a format query value in the the MediaURL's queries, if one exists.
     * 
     * @param plat - the {@link SRC_PLATFORM} which the media is located on.
     * @return a String with the first query value that matched, or
     *         <code>null</code> if there was no match.
     */
    public String findFormatQuery(SRC_PLATFORM plat) {
        return findQueryIn(plat, QUERIES_FORMAT);
    }
    
    /**
     * Gets the MediaURL's map of key/value queries.
     * 
     * @return a HashMap<String, String> with the URL queries as K/V pairs.
     */
    public HashMap<String, String> queries() {
        return this.queries;
    }
    
    /**
     * Checks if this MediaURL has a valid full domain value.
     * 
     * @return <code>true</code> if the full domain value is valid (non-null and at
     *         least 1 char long); <code>false</code> otherwise.
     */
    public boolean hasFullDomain() {
        return this.fullDomain != null && this.fullDomain.length() > 0;
    }
    
    /**
     * Gets the MediaURL's full domain String value.
     * 
     * @return a String with the full URL domain value.
     */
    public String fullDomain() {
        return this.fullDomain;
    }
    
    /**
     * Checks if this MediaURL has a valid domain value.
     * 
     * @return <code>true</code> if the domain value is valid (non-null and at least
     *         1 char long); <code>false</code> otherwise.
     */
    public boolean hasDomain() {
        return this.domain != null && this.domain.length() > 0;
    }
    
    /**
     * Gets the MediaURL's domain String value.
     * 
     * @return a String with the URL domain value.
     */
    public String domain() {
        return this.domain;
    }
    
    /**
     * Checks if the MediaURL has a path. Though an empty path ("/") technically is
     * non-null and valid, it is treated as not having a path at all for the
     * purposes of this application.
     * 
     * @return <code>true</code> if the path is not <code>null</code> and the length
     *         is greater than <code>1</code>.
     */
    public boolean hasPath() {
        return this.path != null && this.path.length() > 1;
    }

    /**
     * Gets the name of the file, retrieving the exact filename if the path points
     * directly to a file. Otherwise, this method will make a guess and retrieve the
     * end of the path as the name. If there is no valid path value for this
     * MediaURL, this method will return <code>null</code>.
     * 
     * @return a String with the name, or <code>null</code> if there is no path.
     */
    public String getNameFromPath() {
        if (isPathToFile()) {
            return path().substring(1, path().lastIndexOf('.'));
        } else if (isFullPathToFile()) {
            final int periodIndex  = fullPath.lastIndexOf('.');
            final int leadingSlash = fullPath.lastIndexOf('/', periodIndex);
            return fullPath.substring(leadingSlash + 1, periodIndex);
        } else if (hasPath()) {
            return path().substring(1);
        }
        return null;
    }
    
    /**
     * Checks if MediaURL's full path points directly to a file, if a path exists at
     * all.
     * 
     * @return <code>true</code> if the full path exists, and it points directly to
     *         a file; <code>false</code> otherwise.
     */
    public boolean isFullPathToFile() {
        if (!hasPath())
            return false;
        
        final int periodIndex  = fullPath.lastIndexOf('.');
        final int trailingSlash = (periodIndex != -1 ? fullPath.indexOf('/', periodIndex) : -1);
        return (periodIndex != -1 && (periodIndex <= (fullPath.length() - 3) || periodIndex <= (trailingSlash - 3)));
    }
    
    /**
     * Checks if MediaURL's path points directly to a file, if a path exists at all.
     * 
     * @return <code>true</code> if a path exists, and it points directly to a file;
     *         <code>false</code> otherwise.
     */
    public boolean isPathToFile() {
        if (!hasPath())
            return false;
        
        final int periodIndex = path().lastIndexOf('.');
        return (periodIndex != -1 && periodIndex <= (path().length() - 3));
    }
    
    /**
     * Gets the MediaURL's path String value.
     * 
     * @return a String with the URL path value.
     */
    public String path() {
        return this.path;
    }
    
    /**
     * Returns a toString() representation of this MediaURL, providing extra
     * information if the passed debug boolean is <code>true</code>.
     * 
     * @param debug - a boolean which controls whether or not to include debug
     *              information in the returned String.
     * @return a String with the MediaURL information.
     */
    public String toString(boolean debug) {
        final StringBuilder builder = new StringBuilder();
        
        builder.append("    Valid: " + this.valid).append("\n");
        builder.append("   Domain: " + Objects.toString(this.fullDomain, "<none>")).append("\n");
        builder.append("Full Path: " + Objects.toString(this.fullPath, "<none>")).append("\n");
        builder.append("     Path: " + Objects.toString(this.path, "<none>")).append("\n");
        builder.append("  Queries: " + Objects.toString(this.queries, "<none>"));
        
        if (!debug)
            return builder.toString();
        if (notValid()) {
            System.err.println("Cannot print debug portion of MediaURL printout: URL not valid.");
            return builder.toString();
        }
        
        builder.append("\n# DEBUG\n");
        builder.append(Objects.toString(url.getAuthority(), "<none>")).append("\n");
        builder.append(Objects.toString(url.getFile(), "<none>")).append("\n");
        builder.append(Objects.toString(url.getHost(), "<none>")).append("\n");
        builder.append(Objects.toString(url.getPath(), "<none>")).append("\n");
        builder.append(Objects.toString(url.getProtocol(), "<none>")).append("\n");
        builder.append(Objects.toString(url.getQuery(), "<none>")).append("\n");
        builder.append(Objects.toString(url.getRef(), "<none>")).append("\n");
        builder.append(Objects.toString(url.getUserInfo(), "<none>")).append("\n");
        builder.append(Objects.toString(url.getDefaultPort(), "<none>")).append("\n");
        builder.append(Objects.toString(url.getPort(), "<none>"));
        return builder.toString();
    }
    
    @Override
    public String toString() {
        return this.toString(false);
    }
}
