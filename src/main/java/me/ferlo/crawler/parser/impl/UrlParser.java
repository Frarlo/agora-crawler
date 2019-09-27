package me.ferlo.crawler.parser.impl;

import com.google.inject.Inject;
import me.ferlo.crawler.Domain;
import me.ferlo.crawler.activities.Activity;
import me.ferlo.crawler.activities.ActivityFactory;
import me.ferlo.crawler.activities.BaseActivityData;
import me.ferlo.crawler.download.DownloadService;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlParser extends BaseActivityParser {

    @Inject UrlParser(ActivityFactory factory,
                      DownloadService downloadService,
                      @Domain String domain) {
        super(factory, downloadService, domain);
    }

    @Override
    public Activity parse(BaseActivityData baseData) {

        final String redirectedUrl = downloadService.followRedirects(baseData.getHref());

        if(redirectedUrl.equals(baseData.getHref())) {

            final String html = downloadService.fetch(redirectedUrl);
            final String urlWorkaround = extractUrlWorkaround(html);

            final Map<Path, String> resources = new HashMap<>();
            extractLinks(urlWorkaround, resources);

            return factory.createUrl(baseData, urlWorkaround, resources);
        }

        return factory.createUrl(
                baseData,
                "<a href=\"" + redirectedUrl + "\">" + redirectedUrl + "</a>",
                Collections.emptyMap()
        );
    }

    protected String extractUrlWorkaround(String html) {

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
}
