package me.ferlo.crawler.parsers;

import me.ferlo.client.HttpClientService;
import me.ferlo.crawler.Resource;
import me.ferlo.crawler.activities.Activity;
import me.ferlo.crawler.activities.ActivityFactory;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UrlParser {

    public static Activity parse(HttpClientService httpClientService,
                                 ActivityFactory factory,
                                 String name,
                                 String href,
                                 String type,
                                 int indent) {

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

        try(CloseableHttpClient client = clientBuilder.build()) {

            System.out.println("Fetching activity " + href);
            HttpGet request = new HttpGet(href);
            try(CloseableHttpResponse response = client.execute(request)) {

                if(response.getStatusLine().getStatusCode() == 303) {
                    String location = response.getFirstHeader("Location").getValue();
                    return factory.createUrl(
                            name, href, type, indent,
                            "<a href=\"" + location + "\">" + location + "</a>", Collections.emptyList()
                    );
                }

                if(response.getStatusLine().getStatusCode() != 200)
                    throw new IOException(response.getStatusLine().toString());

                final String html = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                        .lines()
                        .collect(Collectors.joining("\n"));
                return parseWorkaround(factory, name, href, type, indent, html);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    private static Activity parseWorkaround(ActivityFactory factory,
                                            String name,
                                            String href,
                                            String type,
                                            int indent,
                                            String html) {


        String urlWorkaround = extractUrlWorkaround(html);
        final List<Resource> resources = new ArrayList<>(extractLinks(urlWorkaround));

        return factory.createUrl(
                name, href, type, indent,
                urlWorkaround, resources
        );
    }

    private static String extractUrlWorkaround(String html) {

        final Pattern urlWorkaroundPattern = Pattern.compile("" +
                //@formatter:off
                "<div class=\"urlworkaround\">" +
                    "(?<urlWorkaround>[\\S\\s]*)" +
                "</div>\\s*" +
                "<div class=\"mt-5 mb-1 activity-navigation\">");
                //@formatter:on
        final Matcher urlWorkaroundMatcher = urlWorkaroundPattern.matcher(html);

        if(urlWorkaroundMatcher.find())
            return urlWorkaroundMatcher.group("urlWorkaround");
        return "";
    }

    private static List<Resource> extractLinks(String html) {
        final Pattern resourcePattern = Pattern.compile("<a href=\"(?<link>[^\"]*)\"(?: target=\"_blank\")?>");
        final Matcher resourceMatcher = resourcePattern.matcher(html);

        List<Resource> links = new ArrayList<>();
        while(resourceMatcher.find()) {
            String link = resourceMatcher.group("link");
            String name;
            try {
                name = Paths.get(new URI(link).getPath()).getFileName().toString();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            links.add(new Resource(name, resourceMatcher.group("link")));
        }
        return links;
    }
}
