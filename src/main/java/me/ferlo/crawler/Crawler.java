package me.ferlo.crawler;

import com.google.inject.Inject;
import me.ferlo.client.Authenticated;
import me.ferlo.client.HttpClientService;
import me.ferlo.crawler.activities.Activity;
import me.ferlo.crawler.activities.BaseActivityData;
import me.ferlo.crawler.category.Category;
import me.ferlo.crawler.category.CategoryFactory;
import me.ferlo.crawler.course.Course;
import me.ferlo.crawler.course.CourseFactory;
import me.ferlo.crawler.parser.ActivityParserService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Crawler implements CrawlerService {

    private final HttpClientService httpClientService;

    private final CourseFactory courseFactory;
    private final CategoryFactory categoryFactory;
    private final ActivityParserService activityParserService;

    @Inject Crawler(@Authenticated HttpClientService httpClientService,
                    CourseFactory courseFactory,
                    CategoryFactory categoryFactory,
                    ActivityParserService activityParserService) {
        this.httpClientService = httpClientService;
        this.courseFactory = courseFactory;
        this.categoryFactory = categoryFactory;
        this.activityParserService = activityParserService;
    }

    @SuppressWarnings("unused")
    public List<Course> fetchCoursesOld() {
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
    @SuppressWarnings("unchecked")
    public List<Course> fetchCourses() {
        try(CloseableHttpClient client = httpClientService.makeHttpClient()) {
            System.out.println("Fetching courses");

            final HttpPost request = new HttpPost(new URIBuilder("https://agora.ismonnet.it/agora/lib/ajax/service.php")
                    .setParameter("sesskey", "")
                    .setParameter("info", "core_course_get_enrolled_courses_by_timeline_classification")
                    .build());
            request.setHeader("accept", "application/json, text/javascript, */*; q=0.01");
            request.setHeader("content-type", "application/json");

            final JSONArray bodyJson = new JSONArray();

            final JSONObject jsonObj = new JSONObject();
            jsonObj.put("index", 0);
            jsonObj.put("methodname", "core_course_get_enrolled_courses_by_timeline_classification");

            final JSONObject argsJson = new JSONObject();
            argsJson.put("offset", 0);
            argsJson.put("limit", 96);
            argsJson.put("classification", "all");
            argsJson.put("sort", "fullname");

            jsonObj.put("args", argsJson);
            bodyJson.add(jsonObj);

            request.setEntity(new StringEntity(bodyJson.toJSONString()));

            try(CloseableHttpResponse response = client.execute(request)) {

                final String json = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                        .lines()
                        .collect(Collectors.joining("\n"));

                try {
                    final JSONArray reply = (JSONArray) new JSONParser().parse(json);
                    final JSONObject obj = (JSONObject) reply.get(0);
                    if(obj == null)
                        throw new IOException("Reply json missing index 0 object (json: " + json + ")");
                    if(obj.get("error") instanceof Boolean && ((Boolean) obj.get("error")))
                        throw new IOException("Reply json has error parameter set to true (json: " + json + ")");

                    final JSONObject data = (JSONObject) obj.get("data");
                    if(data == null)
                        throw new IOException("Reply json missing data object (json: " + json + ")");

                    final JSONArray coursesJson = (JSONArray) data.get("courses");
                    if(coursesJson == null)
                        throw new IOException("Reply json missing courses object (json: " + json + ")");

                    return ((List<Object>) coursesJson).stream()
                            .filter(o -> {
                                if(o instanceof JSONObject)
                                    return true;
                                System.err.println("Invalid course: '" + o + "'");
                                return false;
                            })
                            .map(JSONObject.class::cast)
                            .filter(jsonCourse -> {
                                if(jsonCourse.get("fullname") != null && jsonCourse.get("fullname") instanceof String) {
                                    jsonCourse.put("___myname___", jsonCourse.get("fullname"));
                                    return true;
                                }
                                if(jsonCourse.get("shortname") != null && jsonCourse.get("shortname") instanceof String) {
                                    jsonCourse.put("___myname___", jsonCourse.get("shortname"));
                                    return true;
                                }
                                if(jsonCourse.get("fullnamedisplay") != null && jsonCourse.get("fullnamedisplay") instanceof String) {
                                    jsonCourse.put("___myname___", jsonCourse.get("fullnamedisplay"));
                                    return true;
                                }
                                System.err.println("Course missing name: '" + jsonCourse + "'");
                                return false;
                            })
                            .filter(jsonCourse -> {
                                if(jsonCourse.get("viewurl") != null && jsonCourse.get("viewurl") instanceof String)
                                    return true;
                                System.err.println("Course missing viewurl: '" + jsonCourse + "'");
                                return false;
                            })
                            .map(jsonCourse -> courseFactory.create(
                                    (String) jsonCourse.get("___myname___"),
                                    (String) jsonCourse.get("viewurl")))
                            .collect(Collectors.toList());
                } catch (Exception ex) {
                    throw new RuntimeException("Invalid json (json: '" + json + "', request: '" + request + "')", ex);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
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
                                "(?:<span class=\"actions\">" +
                                    ".*?" +
                                "</span>|)" +
                                "(?:<div class=\"contentafterlink\">" +
                                    "<div class=\"no-overflow\">" +
                                        "<div class=\"no-overflow\">" +
                                            ".*?" +
                                        "</div>" +
                                    "</div>" +
                                "</div>|)" +
                            "</div>" +
                        "</div>" +
                    "</div>" +
                "</li>", Pattern.DOTALL);
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

            Activity activity = activityParserService.parse(new BaseActivityData(
                    matcher.group("name"),
                    matcher.group("href"),
                    type,
                    indent
            ));

            activities.add(activity);
        }

        return activities;
    }
}
