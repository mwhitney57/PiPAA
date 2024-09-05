package dev.mwhitney.update.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import dev.mwhitney.main.PiPProperty.TYPE_OPTION;

/**
 * Communicates with the PiPAA API.
 * 
 * @author mwhitney57
 */
public class APICommunicator {
    /** The base URL for API communicator queries. */
    private static final String BASE_API_URL = "https://pipaa.mwhitney.dev/api";
    /**
     * An API request, which contains a URL endpoint.
     * 
     * @author mwhitney57
     */
    public enum Request {
        /** Grabs the latest RELEASE version. */
        LATEST_RELEASE (BASE_API_URL + "/update/latest/release"),
        /** Grabs the latest version covered by the BETA setting (RELEASE, BETA). */
        LATEST_BETA    (BASE_API_URL + "/update/latest/beta"),
        /** Grabs the latest version covered by the SNAPSHOT setting (RELEASE, BETA, SNAPSHOT). */
        LATEST_SNAPSHOT(BASE_API_URL + "/update/latest/snapshot");
        
        /** A String with the URL endpoint for this request. */
        private String url;
        
        /**
         * Creates a request with the String URL endpoint parameter.
         */
        private Request(String reqURL) {
            this.url = reqURL;
        }
        /**
         * Gets the API request's URL endpoint.
         * 
         * @return the String URL endpoint.
         */
        public String url() {
            return this.url;
        }
    }

    /**
     * The payload returned from API requests which contains an application update.
     * The payload consists of a {@link Build} and a String link where said
     * {@link Build} can be accessed.
     * 
     * @author mwhitney57
     */
    public record UpdatePayload(
        Build build,
        String link
    ) {
        /**
         * Checks if the payload has a non-<code>null</code> {@link Build}.
         * 
         * @return <code>true</code> if the payload has a valid {@link Build};
         *         <code>false</code> otherwise.
         */
        public boolean hasBuild() {
            return (this.build != null);
        }
        /**
         * Checks if the payload has a non-<code>null</code> and non-blank String
         * download link.
         * 
         * @return <code>true</code> if the payload has a valid String link;
         *         <code>false</code> otherwise.
         */
        public boolean hasLink() {
            return (this.link != null && !this.link.isBlank());
        }
    }
    
    /**
     * Performs an API {@link Request}.
     * <p>
     * The request returns an {@link UpdatePayload} containing a {@link Build} and a
     * String link where it can be accessed.
     * 
     * @param r - the {@link Request} to perform.
     * @return an {@link UpdatePayload} if one was retrieved from the request
     *         without error; <code>null</code> otherwise.
     * @throws IOException          if there is an input/output error during the
     *                              request.
     * @throws InterruptedException if the request communication is interrupted.
     */
    public static UpdatePayload request(@NotNull Request r) throws IOException, InterruptedException {
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(r.url()))
                    .timeout(Duration.ofSeconds(10))
                    .header("nothing", "nothing")
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.body().isBlank()) return null;
        final JSONObject json = new JSONObject(response.body());
        final JSONObject version = json.getJSONObject("version");
        final Version v = new Version(version.getInt("majorVersion"), version.getInt("middleVersion"), version.getInt("minorVersion"));
        final TYPE_OPTION type = TYPE_OPTION.parseSafe(json.getString("releaseType").toUpperCase());
        if (v == null || type == null) return null;
        final Build build = new Build(v, type);
        
        final JSONArray jsonLinks = json.getJSONArray("links");
        final String currFileName = new java.io.File(APICommunicator.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
        if (currFileName.equals("classes")) return null;
        final String currFileExt  = currFileName.substring(currFileName.lastIndexOf('.'));
        final StringBuilder link = new StringBuilder();
        jsonLinks.forEach(l -> {
            if (((String) l).endsWith(currFileExt)) link.append(((String) l));
        });
        return new UpdatePayload(build, link.toString());
    }
    
    /**
     * Performs an API {@link Request}.
     * <p>
     * The request returns an {@link UpdatePayload} containing a {@link Build} and a
     * String link where it can be accessed.
     * 
     * @param updateType - the {@link TYPE_OPTION} matching the {@link Request} to
     *                   perform.
     * @return an {@link UpdatePayload} if one was retrieved from the request
     *         without error; <code>null</code> otherwise.
     * @throws IOException          if there is an input/output error during the
     *                              request.
     * @throws InterruptedException if the request communication is interrupted.
     */
    public static UpdatePayload request(@NotNull TYPE_OPTION updateType) throws IOException, InterruptedException {
        return request(switch(updateType) {
        case RELEASE  -> Request.LATEST_RELEASE;
        case BETA     -> Request.LATEST_BETA;
        case SNAPSHOT -> Request.LATEST_SNAPSHOT;
        });
    }
}
