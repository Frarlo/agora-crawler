package me.ferlo.crawler.activities;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.io.*;

public class UnsupportedActivity extends Activity {

    @Inject UnsupportedActivity(@Assisted("name") String name,
                                @Assisted("href") String href,
                                @Assisted("type") String type,
                                @Assisted("indent") int indent) {
        super(name, href, type, indent);
    }

    @Override
    public void writeInFolder(File folder) {
        File file = new File(folder, "unsupported.html");

        try (PrintStream out = new PrintStream(new FileOutputStream(file))) {
            out.print("'" + getType() + "' type is not supported");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public String toString() {
        return "UnsupportedActivity{} " + super.toString();
    }
}
