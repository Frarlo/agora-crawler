package me.ferlo.crawler.parser.impl;

import com.google.inject.Inject;
import me.ferlo.crawler.Domain;
import me.ferlo.crawler.activities.Activity;
import me.ferlo.crawler.activities.ActivityFactory;
import me.ferlo.crawler.activities.BaseActivityData;
import me.ferlo.crawler.download.DownloadService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FolderParser extends BaseActivityParser {

    @Inject FolderParser(ActivityFactory factory,
                         DownloadService downloadService,
                         @Domain String domain) {
        super(factory, downloadService, domain);
    }

    @Override
    public Activity parse(BaseActivityData baseData) {

        final String html = downloadService.fetch(baseData.getHref());
        final String generalBox = extractGeneralBox(html);

        final Map<Path, String> docs = new HashMap<>();
        extractDocs(generalBox, docs);

        return factory.createFolder(baseData, docs);

    }

    protected String extractGeneralBox(String html) {

        final Pattern generalBoxPattern = Pattern.compile("" +
                //@formatter:off
                "<div class=\"box py-3 generalbox foldertree\">" +
                "(?<generalBox>[\\S\\s]*)" +
                "</div>" +
                "<div class=\"box py-3 generalbox folderbuttons\">");
        //@formatter:on
        final Matcher generalBoxMatcher = generalBoxPattern.matcher(html);

        if(generalBoxMatcher.find())
            return generalBoxMatcher.group("generalBox");
        return "";
    }


    protected void extractDocs(String html, Map<Path, String> docs) {
        //Per qualche strano motivo l'html è diverso da quello di un browser
//       final Pattern docsBoxPattern = Pattern.compile("" +
//                //@formatter:off
//                "<tr class=\"ygtvrow\">\n" +
//                "<td class=\"ygtvcell ygtvblankdepthcell\" style=\"\">" +
//                "<div class=\"ygtvspacer\"></div>" +
//                "</td>\n" +
//                "<td id=\"ygtvt2\" class=\"ygtvcell ygtvtn\">"+
//                "<a href=\"#\" class=\"ygtvspacer\">&nbsp;</a>"+
//                "</td>\n"+
//                "<td id=\"ygtvcontentel[0-9]*\" class=\"ygtvcell ygtvhtml ygtvcontent\">" +
//                "<span class=\"fp-filename-icon\"><a href=\"(?<link>[^\"]*)\"><span class=\"fp-icon\"><img class=\"icon \" alt=\"(?:[^\"]*)\" title=\"(?:[^\"]*)\" src=\"(?:[^\"]*)\"></span><span class=\"fp-filename\">(?<name>[^<]*)</span></a></span>"+
//                "</td>\n" +
//                "</tr>\n");
//                //@formatter:on

        final Pattern docsBoxPattern = Pattern.compile("" +
                //@formatter:off
                "<li><span class=\"fp-filename-icon\">"+
                "<a href=\"(?<link>[^\"]*)\">"+
                "<span class=\"fp-icon\">"+
                "<img class=\"icon \" alt=\"(?:[^\"]*)\" title=\"(?:[^\"]*)\" src=\"(?:[^\"]*)\" />"+
                "</span>"+
                "<span class=\"fp-filename\">(?<name>[^<]*)</span>"+
                "</a></span></li>");
                //@formatter:on
        final Matcher docMatcher = docsBoxPattern.matcher(html);

        while(docMatcher.find()) {
            final String name = docMatcher.group("name");
            final String link = docMatcher.group("link");

            docs.put(Paths.get(name), link);
        }
    }
}
