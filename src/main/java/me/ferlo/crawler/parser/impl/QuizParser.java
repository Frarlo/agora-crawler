package me.ferlo.crawler.parser.impl;

import com.google.inject.Inject;
import me.ferlo.crawler.Domain;
import me.ferlo.crawler.activities.Activity;
import me.ferlo.crawler.activities.ActivityFactory;
import me.ferlo.crawler.activities.BaseActivityData;
import me.ferlo.crawler.download.DownloadService;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuizParser extends BaseActivityParser {

    @Inject QuizParser(ActivityFactory factory,
                       DownloadService downloadService,
                       @Domain String domain) {
        super(factory, downloadService, domain);
    }

    @Override
    public Activity parse(BaseActivityData baseData) {

        final String html = downloadService.fetch(baseData.getHref());
        final Pattern formPattern = Pattern.compile("" +
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

        final Matcher formMatcher = formPattern.matcher(html);
        if(formMatcher.find()) {

            final String attempt = formMatcher.group("attempt");
            final String cmid = formMatcher.group("cmid");
            final String sesskey = formMatcher.group("sesskey");

            final List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("attempt", attempt));
            params.add(new BasicNameValuePair("cmid", cmid));
            params.add(new BasicNameValuePair("sesskey", sesskey));

            HttpPost request = new HttpPost("https://agora.ismonnet.it/agora/mod/quiz/review.php");
            try {
                request.setEntity(new UrlEncodedFormEntity(params));
            } catch (UnsupportedEncodingException e) {
                throw new UncheckedIOException(e);
            }

            String quizHtml = downloadService.fetch(request);

            final Map<Path, String> resources = new HashMap<>();
            quizHtml = removeSecureWindow(quizHtml);
            // Changed idea, just fucking nuke any css, it still looks decent
//          extractJavascriptLinks(quizHtml, resources);
            quizHtml = removeJavascript(quizHtml);
            // Kill any form too, cause why not
            quizHtml = removeFormActions(quizHtml);
            extractCssLinks(quizHtml, resources);

            return factory.createQuiz(baseData, quizHtml, resources);
        }

        return factory.createQuiz(baseData, "", Collections.emptyMap());
    }

    protected String removeSecureWindow(String html) {
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
}
