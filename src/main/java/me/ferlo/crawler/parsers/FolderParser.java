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

public class FolderParser {
    public static Activity parse(ActivityFactory factory,
                                 String name,
                                 String href,
                                 String type,
                                 int indent,
                                 String html) {

        String generalBox=extractGeneralBox(html);
        List<Resource> docsBox=extractDocs(generalBox);

        return factory.createFolder(name,href,type,indent,docsBox);

    }
    private static String extractGeneralBox(String html) {

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


    private static List<Resource> extractDocs(String html) {
        //Per qualche strano motivo l'html è diverso da quello di un browser
       /* final Pattern docsBoxPattern = Pattern.compile("" +

                //@formatter:off
                "<tr class=\"ygtvrow\">\n" +
                "<td class=\"ygtvcell ygtvblankdepthcell\" style=\"\">" +
                "<div class=\"ygtvspacer\"></div>" +
                "</td>\n" +
                "<td id=\"ygtvt2\" class=\"ygtvcell ygtvtn\">"+
                "<a href=\"#\" class=\"ygtvspacer\">&nbsp;</a>"+
                "</td>\n"+
                "<td id=\"ygtvcontentel[0-9]*\" class=\"ygtvcell ygtvhtml ygtvcontent\">" +
                "<span class=\"fp-filename-icon\"><a href=\"(?<link>[^\"]*)\"><span class=\"fp-icon\"><img class=\"icon \" alt=\"(?:[^\"]*)\" title=\"(?:[^\"]*)\" src=\"(?:[^\"]*)\"></span><span class=\"fp-filename\">(?<name>[^<]*)</span></a></span>"+
                "</td>\n" +
                "</tr>\n");
        //@formatter:on
*/
        final Pattern docsBoxPattern = Pattern.compile("" +

                //@formatter:off
                "<li><span class=\"fp-filename-icon\">"+
                "<a href=\"(?<link>[^\"]*)\">"+
                "<span class=\"fp-icon\">"+
                "<img class=\"icon \" alt=\"(?:[^\"]*)\" title=\"(?:[^\"]*)\" src=\"(?:[^\"]*)\" />"+
                "</span>"+
                "<span class=\"fp-filename\">(?<name>[^<]*)</span>"+
                "</a></span></li>");

        final Matcher docMatcher = docsBoxPattern.matcher(html);

        final List<Resource> resources = new ArrayList<>();
        while(docMatcher.find()) {
            final String name = docMatcher.group("name");
            final String link = docMatcher.group("link");
            resources.add(new Resource(name, link));
        }

        return resources;
    }


}
