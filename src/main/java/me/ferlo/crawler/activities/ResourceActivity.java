package me.ferlo.crawler.activities;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import me.ferlo.client.Authenticated;
import me.ferlo.client.HttpClientService;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ResourceActivity extends Activity {

    private final HttpClientService httpClientService;

    @Inject ResourceActivity(@Authenticated HttpClientService httpClientService,
                             @Assisted("name") String name,
                             @Assisted("href") String href,
                             @Assisted("type") String type,
                             @Assisted("indent") int indent) {
        super(name, href, type, indent);
        this.httpClientService = httpClientService;
    }

    @Override
    public void writeInFolder(File folder) {

        HttpClientBuilder clientBuilder = httpClientService.makeHttpClientBuilder();
        clientBuilder.setRedirectStrategy(new RedirectStrategy() {
            @Override
            public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context) {
                return false;
            }

            @Override
            public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context) {
                return null;
            }
        });

        try (CloseableHttpClient client = clientBuilder.build()) {

            HttpGet request = new HttpGet(getHref());
            System.out.println("Downloading file " + getHref());
            try(CloseableHttpResponse response = client.execute(request)) {

                if(response.getStatusLine().getStatusCode() != 303)
                    throw new IOException("Expected 303, received " + response.getStatusLine().toString());

                String location = response.getFirstHeader("Location").getValue();
                String name;
                try {
                    name = Paths.get(new URI(location).getPath()).getFileName().toString();
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }

                File dest = new File(folder, name.trim());

                try (CloseableHttpClient client0 = httpClientService.makeHttpClient()) {
                    HttpGet request0 = new HttpGet(getHref());

                    try(CloseableHttpResponse response0 = client0.execute(request0)) {
                        Files.copy(response0.getEntity().getContent(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String toString() {
        return "ResourceActivity{} " + super.toString();
    }
}
