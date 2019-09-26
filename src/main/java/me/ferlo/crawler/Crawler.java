package me.ferlo.crawler;

import com.google.inject.Inject;
import me.ferlo.client.Authenticated;
import me.ferlo.client.HttpClientService;
import me.ferlo.crawler.activities.Activity;
import me.ferlo.crawler.activities.ActivityFactory;
import me.ferlo.crawler.category.Category;
import me.ferlo.crawler.category.CategoryFactory;
import me.ferlo.crawler.course.Course;
import me.ferlo.crawler.course.CourseFactory;
import me.ferlo.crawler.parsers.AssignmentParser;
import me.ferlo.crawler.parsers.PageParser;
import me.ferlo.crawler.parsers.QuizParser;
import me.ferlo.crawler.parsers.UrlParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Crawler implements CrawlerService {

    private final HttpClientService httpClientService;

    private final CourseFactory courseFactory;
    private final CategoryFactory categoryFactory;
    private final ActivityFactory activityFactory;

    @Inject Crawler(@Authenticated HttpClientService httpClientService,
                    CourseFactory courseFactory,
                    CategoryFactory categoryFactory,
                    ActivityFactory activityFactory) {
        this.httpClientService = httpClientService;
        this.courseFactory = courseFactory;
        this.categoryFactory = categoryFactory;
        this.activityFactory = activityFactory;
    }

    @Override
    public List<Course> fetchCourses() {
        try(CloseableHttpClient client = httpClientService.makeHttpClient()) {

            System.out.println("Fetching courses");
            HttpGet request = new HttpGet("https://agora.ismonnet.it/agora/");
            try(CloseableHttpResponse response = client.execute(request)) {

                final String html = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                        .lines()
                        .collect(Collectors.joining("\n"));

                final Pattern coursePattern = Pattern.compile("" +
                        "<h3(?: +)class=\"coursename\">(?: *)" +
                        "<a(?: +)class=\"\"(?: +)href=\"(?<link>[^\"]*)\">" +
                        "(?<name>[^<]*)" +
                        "</a>" +
                        "(?: *)</h3>");
                final Matcher matcher = coursePattern.matcher(html);

                final List<Course> courses = new ArrayList<>();
                while(matcher.find())
                    courses.add(courseFactory.create(matcher.group("name"), matcher.group("link")));

                return courses;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<Category> fetchCategories(String courseHref) {
        try(CloseableHttpClient client = httpClientService.makeHttpClient()) {

            System.out.println("Fetching course " + courseHref);
            HttpGet request = new HttpGet(courseHref);
            try(CloseableHttpResponse response = client.execute(request)) {

                final String html = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                        .lines()
                        .collect(Collectors.joining("\n"));

                final List<Category> categories = new ArrayList<>();

                final Pattern sectionPattern = Pattern.compile("<h3(?: +)class=\"sectionname\">(?<name>[^<]*)</h3>");
                final Matcher matcher = sectionPattern.matcher(html);


                int startIdx = 0;
                String previousSectionName = "Generale";

                while(matcher.find()) {
                    int endIdx = matcher.start();
                    String previousSectionHtml = html.substring(startIdx, endIdx);
                    categories.add(categoryFactory.create(previousSectionName, fetchActivities(previousSectionHtml)));

                    previousSectionName = matcher.group("name");
                    startIdx = matcher.end();
                }

                String lastHtml = html.substring(startIdx);
                categories.add(categoryFactory.create(previousSectionName, fetchActivities(lastHtml)));

                return categories;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<Activity> fetchActivities(String html) {
        List<Activity> activities = new ArrayList<>();
        Pattern activityPattern = Pattern.compile("" +
                //@formatter:off
                "<li class=\"activity (?<type0>[^\"]*) modtype_(?<type1>[^\"]*) \" id=\"(?<id>[^\"]*)\">" +
                    "<div>" +
                        "<div class=\"mod-indent-outer\">" +
                            "<div class=\"mod-indent(?: mod-indent-(?<indent>\\d+)|)\"></div>" +
                            "<div>" +
                                "<div class=\"activityinstance\">" +
                                    "<a class=\"\" onclick=\"\" href=\"(?<href>[^\"]*)\">" +
                                        "<img src=\"(?<typeIcon>[^\"]*)\" class=\"iconlarge activityicon\" alt=\"\" role=\"presentation\" aria-hidden=\"true\" />" +
                                        "<span class=\"instancename\">" +
                                            "(?<name>[^<]*)" +
                                            "<span class=\"accesshide \" >(?<specificType>[^<]*)</span>" +
                                        "</span>" +
                                    "</a>" +
                                "</div>" +
                            "</div>" +
                        "</div>" +
                    "</div>" +
                "</li>");
                //@formatter:on

        Matcher matcher = activityPattern.matcher(html);
        while(matcher.find()) {

            String type = matcher.group("type0");
            String type1 = matcher.group("type1");
            if(!type.equals(type1))
                throw new AssertionError(String.format("Type mismatch between '%s' and '%s'", type, type1));

            int indent;
            String indentString = matcher.group("indent");
            try {
                if(indentString == null)
                    indent = 0;
                else
                    indent = Integer.parseInt(indentString);
            } catch (NumberFormatException ex) {
                throw new AssertionError("Invalid indent " + indentString);
            }

            Activity activity = fetchActivity(
                    matcher.group("name"),
                    matcher.group("href"),
                    type,
                    indent
            );

            activities.add(activity);
        }

        return activities;
    }

    private Activity fetchActivity(String name,
                                   String href,
                                   String type,
                                   int indent) {

        // This is honestly shit but I can't be bothered to design it properly

        switch (type) {
            case "assign":
            case "page":
            case "quiz":

                try(CloseableHttpClient client = httpClientService.makeHttpClient()) {

                    System.out.println("Fetching activity " + href);
                    HttpGet request = new HttpGet(href);
                    try(CloseableHttpResponse response = client.execute(request)) {

                        final String html = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                                .lines()
                                .collect(Collectors.joining("\n"));

                        switch (type) {
                            case "assign":
                                return AssignmentParser.parse(activityFactory, name, href, type, indent, html);
                            case "page":
                                return PageParser.parse(activityFactory, name, href, type, indent, html);
                            case "quiz":
                                return QuizParser.parse(httpClientService, activityFactory, name, href, type, indent, html);
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            case "url":
                return UrlParser.parse(httpClientService, activityFactory, name, href, type, indent);
            case "resource":
                return activityFactory.createResource(name, href, type, indent);
            default:
                return activityFactory.createUnsupported(name, href, type, indent);
        }
    }

}
