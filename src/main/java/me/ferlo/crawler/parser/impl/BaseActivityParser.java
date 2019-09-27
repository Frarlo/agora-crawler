package me.ferlo.crawler.parser.impl;

import com.google.inject.Inject;
import me.ferlo.crawler.Domain;
import me.ferlo.crawler.activities.ActivityFactory;
import me.ferlo.crawler.download.DownloadService;
import me.ferlo.crawler.parser.ActivityParserService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseActivityParser implements ActivityParserService {

    protected final ActivityFactory factory;
    protected final DownloadService downloadService;
    protected final String domain;

    @Inject BaseActivityParser(ActivityFactory factory,
                               DownloadService downloadService,
                               @Domain String domain) {
        this.factory = factory;
        this.downloadService = downloadService;
        this.domain = domain;
    }

    protected void extractLinks(String html, Map<Path, String> links) {
        final Pattern resourcePattern = Pattern.compile("" +
                "<a" +
                "\\s" +
                "[^>]*" +
                "href=\"(?<link>[^\"]*)\"" +
                "[^>]*" +
                ">");

        final Matcher resourceMatcher = resourcePattern.matcher(html);
        while(resourceMatcher.find()) {
            String link = resourceMatcher.group("link");
            if(!link.contains(domain))
                continue;

            links.put(
                    Paths.get(downloadService.getFileName(link)),
                    resourceMatcher.group("link"));
        }
    }

    protected void extractCssLinks(String html, Map<Path, String> links) {
        final Pattern linkPattern = Pattern.compile("" +
                "<link" +
                "\\s" +
                "[^>]*" +
                "href=\"(?<href>[^\"]*)\"" +
                "[^>]*" +
                ">");

        final Matcher linkMatcher = linkPattern.matcher(html);
        while(linkMatcher.find()) {
            final String href = linkMatcher.group("href");
            if(!href.contains(domain))
                continue;

            String name = downloadService.getFileName(href) + ".css";
            links.put(Paths.get(name), href);
        }
    }

    protected void extractJavascriptLinks(String html, Map<Path, String> links) {
        final Pattern scriptPattern = Pattern.compile("" +
                "<script" +
                "\\s" +
                "[^>]*" +
                "src=\"(?<link>[^\"]*)\"" +
                "[^>]*" +
                ">");

        final Matcher scriptMatcher = scriptPattern.matcher(html);
        while(scriptMatcher.find()) {
            String link = scriptMatcher.group("link");
            if(link.contains(domain))
                continue;

            String name = downloadService.getFileName(link) + ".js";
            links.put(Paths.get(name), link);
        }
    }

    protected String removeJavascript(String html) {
        Pattern scriptPattern = Pattern.compile("" +
                "[^x]" +
                "(?<script>" +
                "<script[^>]*>" +
                "(?:(?!</script>)[\\s\\S])*" +
                "</script>" +
                ")");

        String remaining = html;
        Matcher scriptMatcher = scriptPattern.matcher(remaining);

        while(scriptMatcher.find()) {
            int matchStart = scriptMatcher.start();
            int matchEnd = scriptMatcher.end();

            String script = scriptMatcher.group("script");

            remaining = remaining.substring(0, matchStart)
                    + "<!--x"
                    + script
                    + "-->"
                    + remaining.substring(matchEnd);
            scriptMatcher = scriptPattern.matcher(remaining);
        }

        return remaining;
    }

    protected String removeFormActions(String html) {
        Pattern formPattern = Pattern.compile("" +
                "<form" +
                "[^>]*" +
                "[^x](?<action>action=\"[^\"]*\")" +
                "[^>]*" +
                ">");

        String remaining = html;
        Matcher formMatcher = formPattern.matcher(remaining);

        while(formMatcher.find()) {
            int matchStart = formMatcher.start();
            int matchEnd = formMatcher.end();

            String action = formMatcher.group("action");

            remaining = remaining.substring(0, matchStart)
                    + formMatcher.group().replaceAll(Pattern.quote(action), Matcher.quoteReplacement("x" + action))
                    + remaining.substring(matchEnd);
            formMatcher = formPattern.matcher(remaining);
        }

        return remaining;
    }
}
