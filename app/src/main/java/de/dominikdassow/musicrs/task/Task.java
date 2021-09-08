package de.dominikdassow.musicrs.task;

import de.dominikdassow.musicrs.AppConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

@Slf4j
public abstract class Task {

    public enum Type {
        DOWNLOAD_SPOTIFY_DATA,
        IMPORT_DATA,
        GENERATE_SIMILAR_TRACKS_LISTS,
        MAKE_RECOMMENDATIONS,
        EVALUATE_SAMPLES,
        CONDUCT_STUDY,
    }

    private final String name;

    protected Task(String name) {
        this.name = name;
    }

    public void run() {
        sendWebhook("Started: " + name);
        log("INIT");
        long start = System.currentTimeMillis();

        try {
            init();
        } catch (Exception e) {
            log.error(e.getMessage(), e);

            return;
        }

        try {
            execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            finish();
        }

        log("FINISH - " + (System.currentTimeMillis() - start) + "ms");
        sendWebhook("Finished: " + name);
    }

    protected void init() throws Exception {}

    abstract protected void execute() throws Exception;

    protected void finish() {}

    private void log(String message) {
        log.trace(name + " :: " + message);
    }

    private void sendWebhook(String text) {
        if (!AppConfiguration.get().json.hasNonNull("webhookUrl")) {
            log.trace("No webhook url found.");

            return;
        }

        try {
            URI uri = new URIBuilder(AppConfiguration.get().json.get("webhookUrl").asText())
                .addParameter("title", "Music RS")
                .addParameter("text", text)
                .build();

            HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Accept", "application/json")
                .build();

            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, (chain, authType) -> true)
                .build();

            HttpClient client = HttpClient.newBuilder()
                .sslContext(sslContext)
                .build();

            client
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> log.trace("Sent Webhook :: " + response))
                .get();
        } catch (URISyntaxException |
            InterruptedException |
            ExecutionException |
            NoSuchAlgorithmException |
            KeyStoreException |
            KeyManagementException e
        ) {
            e.printStackTrace();
        }
    }
}
