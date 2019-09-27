package me.ferlo.crawler.activities;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import me.ferlo.client.Authenticated;
import me.ferlo.client.HttpClientService;
import me.ferlo.crawler.Resource;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class QuizActivity extends Activity {

    private final HttpClientService httpClientService;

    private final String quizHtml;
    private final List<Resource> resources;

    @Inject QuizActivity(@Authenticated HttpClientService httpClientService,
                         @Assisted("name") String name,
                         @Assisted("href") String href,
                         @Assisted("type") String type,
                         @Assisted("indent") int indent,
                         @Assisted("quizHtml") String quizHtml,
                         @Assisted("resources") List<Resource> resources) {
        super(name, href, type, indent);

        this.httpClientService = httpClientService;

        this.quizHtml = quizHtml;
        this.resources = resources;
    }

    public String getQuizHtml() {
        return quizHtml;
    }

    public List<Resource> getResources() {
        return resources;
    }

    @Override
    public void writeInFolder(File folder) {

        resources.forEach(toSave -> {

            String relativePath = toSave.getPath().trim();
            File dest = new File(folder, relativePath);

            if(!dest.getParentFile().exists())
                System.out.println("Creating dir " + dest.getParentFile());
            if(!dest.getParentFile().exists() && !dest.getParentFile().mkdirs())
                throw new AssertionError("Couldn't create parent directories " + dest.getParentFile() + " for " + dest);

            System.out.println("Downloading file " + toSave.getHref());
            try (CloseableHttpClient client = httpClientService.makeHttpClient()) {

                HttpGet request = new HttpGet(toSave.getHref());
                try(CloseableHttpResponse response = client.execute(request)) {
                    if(response.getStatusLine().getStatusCode() != 200)
                        throw new IOException(response.getStatusLine().toString());

                    Files.copy(response.getEntity().getContent(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

            } catch (IOException e) {
                // Don't rethrow cause it tries to download an unknown host at one point (http://map.norsecorp.com)
                System.err.println("Couldn't download file " + toSave.getHref());
                e.printStackTrace();
            }
        });

        if(!quizHtml.equals("")) {
            File file = new File(folder, "revisione.html");

            try (PrintStream out = new PrintStream(new FileOutputStream(file))) {
                out.print(quizHtml);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    @Override
    public String toString() {
        return "QuizActivity{" +
                "quizHtml='" + quizHtml + '\'' +
                ", resources=" + resources +
                "} " + super.toString();
    }
}
