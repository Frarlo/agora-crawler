package me.ferlo.crawler.parsers;

import me.ferlo.client.HttpClientService;
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
import java.util.ArrayList;
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

                    final String quizHtml = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                            .lines()
                            .collect(Collectors.joining("\n"));

                    return factory.createQuiz(name, href, type, indent, quizHtml);
                }

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        return factory.createQuiz(name, href, type, indent, "");
    }
}
