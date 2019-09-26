package me.ferlo.crawler.activities;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.io.*;

public class QuizActivity extends Activity {

    private final String quizHtml;

    @Inject QuizActivity(@Assisted("name") String name,
                         @Assisted("href") String href,
                         @Assisted("type") String type,
                         @Assisted("indent") int indent,
                         @Assisted("quizHtml") String quizHtml) {
        super(name, href, type, indent);

        this.quizHtml = quizHtml;
    }

    @Override
    public void writeInFolder(File folder) {
        if(quizHtml.equals(""))
            return;

        File file = new File(folder, "revisione.html");

        try (PrintStream out = new PrintStream(new FileOutputStream(file))) {
            out.print(quizHtml);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
