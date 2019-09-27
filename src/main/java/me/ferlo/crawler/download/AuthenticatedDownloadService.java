package me.ferlo.crawler.download;

import com.google.inject.Inject;
import me.ferlo.client.Authenticated;
import me.ferlo.client.HttpClientService;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

public class AuthenticatedDownloadService implements DownloadService {

    private final HttpClientService httpClientService;

    @Inject AuthenticatedDownloadService(@Authenticated HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

    @Override
    public String getFileName(String url) {
        String actualUrl = followRedirects(url);
        try {
            return Paths.get(new URI(actualUrl).getPath()).getFileName().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String followRedirects(String url) {

        final HttpClientBuilder clientBuilder = httpClientService.makeHttpClientBuilder();
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

            String currentUrl = url;
            do {
                System.out.println("Following redirections for " + currentUrl);

                HttpGet request = new HttpGet(currentUrl);
                try(CloseableHttpResponse response = client.execute(request)) {

                    if(response.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_PERMANENTLY &&
                            response.getStatusLine().getStatusCode() != HttpStatus.SC_MOVED_TEMPORARILY &&
                            response.getStatusLine().getStatusCode() != HttpStatus.SC_SEE_OTHER &&
                            response.getStatusLine().getStatusCode() != HttpStatus.SC_TEMPORARY_REDIRECT) {
                        return currentUrl;
                    }

                    currentUrl = response.getFirstHeader("Location").getValue();
                }
            } while (true);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void download(String url, Path destination) {
        System.out.println("Downloading " + url);

        try (CloseableHttpClient client = httpClientService.makeHttpClient()) {

            HttpGet request = new HttpGet(url);
            try(CloseableHttpResponse response = client.execute(request)) {
                if(response.getStatusLine().getStatusCode() != 200)
                    throw new IOException(response.getStatusLine().toString());

                Files.copy(response.getEntity().getContent(), destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String fetch(String url) {
        return fetch(new HttpGet(url));
    }

    @Override
    public String fetch(HttpUriRequest request) {
        System.out.println("Fetching " + request);

        try(CloseableHttpClient client = httpClientService.makeHttpClient()) {
            try(CloseableHttpResponse response = client.execute(request)) {
                return new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                        .lines()
                        .collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
