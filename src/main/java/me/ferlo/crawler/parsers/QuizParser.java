package me.ferlo.crawler.parsers;

import me.ferlo.client.HttpClientService;
import me.ferlo.crawler.Resource;
import me.ferlo.crawler.activities.Activity;
import me.ferlo.crawler.activities.ActivityFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

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

public class QuizParser {

    public static Activity parse(HttpClientService httpClientService,
                                 ActivityFactory factory,
                                 String name,
                                 String href,
                                 String type,
                                 int indent,
                                 String html) {

        Pattern formPattern = Pattern.compile("" +
                //@formatter:off
                "<form method=\"post\" action=\"https://agora\\.ismonnet\\.it/agora/mod/quiz/review\\.php\"(?: id=\"(?:.*)\")?\\s*>\\s*" +
                    "<input type=\"hidden\" name=\"attempt\" value=\"(?<attempt>.*)\">\\s*" +
                    "<input type=\"hidden\" name=\"cmid\" value=\"(?<cmid>.*)\">\\s*" +
                    "<input type=\"hidden\" name=\"sesskey\" value=\"(?<sesskey>.*)\">\\s*" +
                    "<button type=\"submit\"(?:\\s*)" +
                        "class=\"btn btn-secondary\"(?:\\s*)" +
                        "id=\"(?:.*)\"(?:\\s*)" +
                        "title=\"\"(?:\\s*)>" +
                            "Revisione" +
                    "</button>\\s*" +
                "</form>");
                //@formatter:on
        Matcher formMatcher = formPattern.matcher(html);

        if(formMatcher.find()) {

            final String attempt = formMatcher.group("attempt");
            final String cmid = formMatcher.group("cmid");
            final String sesskey = formMatcher.group("sesskey");

            try(CloseableHttpClient client = httpClientService.makeHttpClient()) {

                final List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("attempt", attempt));
                params.add(new BasicNameValuePair("cmid", cmid));
                params.add(new BasicNameValuePair("sesskey", sesskey));

                HttpPost request = new HttpPost("https://agora.ismonnet.it/agora/mod/quiz/review.php");
                request.setEntity(new UrlEncodedFormEntity(params));

                try(CloseableHttpResponse response = client.execute(request)) {

                    String quizHtml = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                            .lines()
                            .collect(Collectors.joining("\n"));

                    final List<Resource> resources = new ArrayList<>();
                    quizHtml = removeSecureWindow(quizHtml);
                    // Changed idea, just fucking nuke any css, it still looks decent
//                    quizHtml = extractJavascriptLinks(quizHtml, resources);
                    quizHtml = removeJavascript(quizHtml);
                    // Kill any form too, cause why not
                    quizHtml = removeFormActions(quizHtml);
                    quizHtml = extractCssLinks(quizHtml, resources);

                    return factory.createQuiz(name, href, type, indent, quizHtml, resources);
                }

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        return factory.createQuiz(name, href, type, indent, "", Collections.emptyList());
    }

    private static String removeSecureWindow(String html) {
        Pattern secureWindowPattern = Pattern.compile("" +
                "<script[^>]*>" +
                "(?:(?!</script>)[\\s\\S])*" +
                "M\\.mod_quiz\\.secure_window\\.init" +
                "(?:(?!</script>)[\\s\\S])*" +
                "</script>");
        Matcher secureWindowMatcher = secureWindowPattern.matcher(html);

        if(secureWindowMatcher.find()) {
            int matchStart = secureWindowMatcher.start();
            int matchEnd = secureWindowMatcher.end();

            return html.substring(0, matchStart)
                    + "<!--"
                    + secureWindowMatcher.group()
                    + "-->"
                    + html.substring(matchEnd);
        }

        return html;
    }

    private static String extractCssLinks(String html, List<Resource> extractedLinks) {
        Pattern linkPattern = Pattern.compile("" +
                "<link" +
                "[^>]*" +
                "href=\"(?<href>[^\"]*)\"" +
                "[^>]*" +
                ">");

        String remaining = html;
        Matcher linkMatcher = linkPattern.matcher(html);

        while(linkMatcher.find()) {

            int matchStart = linkMatcher.start();
            int matchEnd = linkMatcher.end();

            String href = linkMatcher.group("href");

            if(href.contains("agora.ismonnet")) {

                String name = "res/" + getResourceName(href) + ".css";
                extractedLinks.add(new Resource(name, href));

                remaining = remaining.substring(0, matchStart)
                        + linkMatcher.group().replaceAll(Pattern.quote(href), name)
                        + remaining.substring(matchEnd);
                linkMatcher = linkPattern.matcher(remaining);
            }
        }

        return remaining;
    }

    private static String extractJavascriptLinks(String html, List<Resource> extractedLinks) {
        Pattern scriptPattern = Pattern.compile("" +
                "<script" +
                "[^>]*" +
                "src=\"(?<link>[^\"]*)\"" +
                "[^>]*" +
                ">");

        String remaining = html;
        Matcher scriptMatcher = scriptPattern.matcher(html);

        while(scriptMatcher.find()) {

            int matchStart = scriptMatcher.start();
            int matchEnd = scriptMatcher.end();

            String link = scriptMatcher.group("link");

            if(link.contains("agora.ismonnet")) {

                String name = "res/" + getResourceName(link) + ".js";
                extractedLinks.add(new Resource(name, link));

                remaining = remaining.substring(0, matchStart)
                        + scriptMatcher.group().replaceAll(Pattern.quote(link), name)
                        + remaining.substring(matchEnd);
                scriptMatcher = scriptPattern.matcher(remaining);
            }
        }

        return remaining;
    }

    private static String getResourceName(String href) {
        try {
            return Paths.get(new URI(href).getPath()).getFileName().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static String removeJavascript(String html) {
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

    private static String removeFormActions(String html) {
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
                    + formMatcher.group().replaceAll(Pattern.quote(action), "x" + action)
                    + remaining.substring(matchEnd);
            formMatcher = formPattern.matcher(remaining);
        }

        return remaining;
    }
}
