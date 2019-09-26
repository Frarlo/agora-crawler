package me.ferlo.crawler.parsers;

import me.ferlo.crawler.Resource;
import me.ferlo.crawler.activities.Activity;
import me.ferlo.crawler.activities.ActivityFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageParser {

    public static Activity parse(ActivityFactory factory,
                                 String name,
                                 String href,
                                 String type,
                                 int indent,
                                 String html) {


        String generalBox = extractGeneralBox(html);
        final List<Resource> resources = new ArrayList<>(extractLinks(generalBox));

        return factory.createPage(
                name, href, type, indent,
                generalBox, resources
        );
    }

    private static String extractGeneralBox(String html) {

        final Pattern generalBoxPattern = Pattern.compile("" +
                //@formatter:off
                "<div class=\"box py-3 generalbox center clearfix\">" +
                    "<div class=\"no-overflow\">" +
                        "(?<generalBox>[\\S\\s]*)" +
                    "</div>" +
                "</div>" +
                "<div class=\"modified\">");
                //@formatter:on
        final Matcher generalBoxMatcher = generalBoxPattern.matcher(html);

        if(generalBoxMatcher.find())
            return generalBoxMatcher.group("generalBox");
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
