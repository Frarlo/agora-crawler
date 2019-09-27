package me.ferlo.crawler.parser.impl;

import com.google.inject.Inject;
import me.ferlo.crawler.Domain;
import me.ferlo.crawler.activities.Activity;
import me.ferlo.crawler.activities.ActivityFactory;
import me.ferlo.crawler.activities.BaseActivityData;
import me.ferlo.crawler.download.DownloadService;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageParser extends BaseActivityParser {

    @Inject PageParser(ActivityFactory factory,
                       DownloadService downloadService,
                       @Domain String domain) {
        super(factory, downloadService, domain);
    }

    @Override
    public Activity parse(BaseActivityData baseData) {

        final String html = downloadService.fetch(baseData.getHref());
        final String content = extractGeneralBox(html);

        final Map<Path, String> resources = new HashMap<>();
        extractLinks(content, resources);

        return factory.createPage(baseData, content, resources);
    }

    protected String extractGeneralBox(String html) {

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
}
