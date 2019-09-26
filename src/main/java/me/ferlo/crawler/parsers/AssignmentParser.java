package me.ferlo.crawler.parsers;

import me.ferlo.crawler.Resource;
import me.ferlo.crawler.activities.Activity;
import me.ferlo.crawler.activities.ActivityFactory;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AssignmentParser {

    public static Activity parse(ActivityFactory factory,
                                 String name,
                                 String href,
                                 String type,
                                 int indent,
                                 String html) {


        String generalBox = extractGeneralBox(html);
        String desc = extractDesc(generalBox);

        final List<Resource> resources = new ArrayList<>();
        resources.addAll(extractLinks(desc));
        resources.addAll(extractTreeLinks(generalBox));

        final List<Resource> submissions = new ArrayList<>(extractTreeLinks(extractSubmissionBox(html)));

        return factory.createAssignment(
                name, href, type, indent,
                desc, resources, submissions
        );
    }

    private static String extractGeneralBox(String html) {

        final Pattern generalBoxPattern = Pattern.compile("" +
                //@formatter:off
                        "<div id=\"intro\" class=\"box py-3 generalbox boxaligncenter\">" +
                            "(?<generalBox>[\\S\\s]*)" +
                        "</div>" +
                        "<div class=\"submissionstatustable\">");
                        //@formatter:on
        final Matcher generalBoxMatcher = generalBoxPattern.matcher(html);

        if(generalBoxMatcher.find())
            return generalBoxMatcher.group("generalBox");
        return "";
    }

    private static String extractSubmissionBox(String html) {

        final Pattern submissionBoxPattern = Pattern.compile("" +
                //@formatter:off
                "<tr class=\"\">\n" +
                    "<td class=\"cell c0\" style=\"\">" +
                        "Consegna file" +
                    "</td>\n" +
                    "<td class=\"cell c1 lastcol\" style=\"\">" +
                        "(?<submissionBox>[\\S\\s]*)" +
                    "</td>\n" +
                "</tr>\n" +
                "<tr class=\"lastrow\">");
                //@formatter:on
        final Matcher submissionBoxMatcher = submissionBoxPattern.matcher(html);

        if(submissionBoxMatcher.find())
            return submissionBoxMatcher.group("submissionBox");
        return "";
    }

    private static String extractDesc(String generalBox) {

        final Pattern descPattern = Pattern.compile("" +
                //@formatter:off
                "<div class=\"no-overflow\">" +
                    "(?<desc>[\\S\\s]*)" +
                "</div>");
                //@formatter:on
        final Matcher descMatcher = descPattern.matcher(generalBox);

        if(descMatcher.find())
            return descMatcher.group("desc");
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

    private static List<Resource> extractTreeLinks(String html) {

        final Pattern outerTreePattern = Pattern.compile("" +
                //@formatter:off
                "<div id=\"assign_files_tree(?:.*)\">" +
                    "<ul>" +
                        "(?<inside>[\\S\\s]*)" +
                    "</ul>" +
                "</div>");
                //@formatter:on
        final Matcher outerTreeMatcher = outerTreePattern.matcher(html);

        if(outerTreeMatcher.find()) {
            final String inside = outerTreeMatcher.group("inside");

            final Pattern itemPattern = Pattern.compile("" +
                    //@formatter:off
                    "<li yuiConfig='\\{\"type\":\"html\"}'>" +
                        "<div>" +
                            "<div class=\"fileuploadsubmission\">" +
                                "<img class=\"icon icon\" alt=\"(?:[^\"]*)\" title=\"(?:[^\"]*)\" src=\"(?:[^\"]*)\" />" +
                                " " +
                                "<a target=\"_blank\" href=\"(?<link>[^\"]*)\">" +
                                    "(?<name>[^<]*)" +
                                "</a>" +
                                " {3}" +
                            "</div>" +
                            "<div class=\"fileuploadsubmissiontime\">" +
                                "(?<date>[^<]*)" +
                            "</div>" +
                        "</div>" +
                    "</li>");
                    //@formatter:on
            final Matcher itemMatcher = itemPattern.matcher(inside);

            final List<Resource> resources = new ArrayList<>();
            while (itemMatcher.find()) {
                final String name = itemMatcher.group("name");
                final String link = itemMatcher.group("link");

                resources.add(new Resource(name, link));
            }

            return resources;
        }

        return Collections.emptyList();
    }

    private static List<Resource> extractTreeLinksFromFolder(String folderHtml, String currPath) {

        final Pattern folderPattern = Pattern.compile("" +
                //@formatter:off
                    "<div class=\"ygtvitem\" id=\"ygtv(?:\\d*)\">" +
                        "<table id=\"ygtvtableel(?:\\d*)\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"ygtvtable ygtvdepth(?:\\d*) ygtv-expanded ygtv-highlight0\">" +
                            "<tbody id=\"(?:.*)\">" +
                                "<tr class=\"ygtvrow\" id=\"(?:.*)\">" +
                                    "<td class=\"ygtvcell ygtvblankdepthcell\">" +
                                        "<div class=\"ygtvspacer\"></div>" +
                                    "</td>" +
                                    "<td id=\"ygtvt2\" class=\"ygtvcell ygtvtm\">" +
                                        "<a href=\"#\" class=\"ygtvspacer\" id=\"(?:.*)\">&nbsp;</a>" +
                                    "</td>" +
                                    "<td id=\"ygtvcontentel(?:\\d*)\" class=\"ygtvcell ygtvhtml ygtvcontent\">" +
                                        "<div class=\"fp-filename-icon\" id=\"(?:.*)\">" +
                                            "<span class=\"fp-icon\">" +
                                                "<img class=\"icon \" alt=\"(?:.*)\" title=\"(?:.*)\" src=\"(?:.*)\">" +
                                            "</span>" +
                                            "<span class=\"fp-filename\" id=\"(?:.*)\">" +
                                                "(?<name>.*)" +
                                            "</span>" +
                                        "</div>" +
                                    "</td>" +
                                "</tr>" +
                            "</tbody>" +
                        "</table>" +
                        "<div class=\"ygtvchildren\" id=\"ygtvc(?:\\d*)\" style=\"\">" +
                            "(?<children>[\\S\\s]*)" +
                        "</div>" +
                    "</div>");
                    //@formatter:on
        final Matcher folderMatcher = folderPattern.matcher(folderHtml);

        final List<Resource> resources = new ArrayList<>();

        String remaining = folderHtml;
        while(folderMatcher.find()) {
            final String match = folderMatcher.group();
            final String name = folderMatcher.group("name");
            final String children = folderMatcher.group("children");

            resources.addAll(extractTreeLinksFromFolder(name, currPath + File.separator + children));
            remaining = remaining.replaceFirst(Pattern.quote(match), "");
        }

        final Pattern itemPattern = Pattern.compile("" +
                //@formatter:off
                "<div class=\"ygtvitem\" id=\"ygtv(?:\\d*)\">" +
                    "<table id=\"ygtvtableel(?:\\d*)\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"ygtvtable ygtvdepth(?:\\d*) ygtv-expanded ygtv-highlight0\">" +
                        "<tbody>" +
                            "<tr class=\"ygtvrow\">" +
                                "<td id=\"ygtvt(?:\\d*)\" class=\"ygtvcell ygtvtn\">" +
                                    "<a href=\"#\" class=\"ygtvspacer\">&nbsp;</a>" +
                                "</td>" +
                                "<td id=\"ygtvcontentel(?:\\d*)\" class=\"ygtvcell ygtvhtml ygtvcontent\">" +
                                    "<div>" +
                                        "<div class=\"fileuploadsubmission\">" +
                                            "<img class=\"icon icon\" alt=\".*\" title=\".*\" src=\".*\">" +
                                            " " +
                                            "<a target=\"_blank\" href=\"(?<link>.*)\">" +
                                                "(?<name>.*)" +
                                            "</a>" +
                                            " {3}" +
                                        "</div>" +
                                        "<div class=\"fileuploadsubmissiontime\">" +
                                            "(?<date>.*)" +
                                        "</div>" +
                                    "</div>" +
                                "</td>" +
                            "</tr>" +
                       "</tbody>" +
                    "</table>" +
                    "<div class=\"ygtvchildren\" id=\"ygtvc(?:\\d*)\" style=\"display:none;\">" +
                    "</div>" +
                "</div>");
                //@formatter:on
        final Matcher itemMatcher = itemPattern.matcher(remaining);

        while (itemMatcher.find()) {
            final String name = itemMatcher.group("name");
            final String link = itemMatcher.group("link");

            resources.add(new Resource(new File(currPath, name).getPath(), link));
        }

        return resources;
    }
}
