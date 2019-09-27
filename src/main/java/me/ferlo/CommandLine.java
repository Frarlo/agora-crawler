package me.ferlo;

import com.google.inject.Inject;
import me.ferlo.crawler.CrawlerService;
import me.ferlo.crawler.category.Category;
import me.ferlo.crawler.course.Course;

import java.io.File;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class CommandLine {

    private final Scanner scanner;
    private final PrintStream out;

    private final CrawlerService crawler;

    @Inject CommandLine(Scanner scanner,
                        PrintStream out,
                        CrawlerService crawler) {
        this.scanner = scanner;
        this.out = out;
        this.crawler = crawler;
    }

    public void start() {

        final List<Course> crawledCourses = crawler.fetchCourses();

        out.println("Select the courses to download [0 to stop]: ");

        int[] id = { 1 };
        crawledCourses.forEach(e -> {
            final String course = e.getTitle();
            final String link = e.getHref();

            out.printf("%d. %s => %s\n", id[0]++, course, link);
        });

        final Set<Integer> selected = new HashSet<>(crawledCourses.size());

        while(true) {
            int n = readInt();
            while (n < 0 || n > crawledCourses.size()) {
                out.printf("Invalid id (min: %d, max: %d)\n", 0, crawledCourses.size());
                n = readInt();
            }

            if(n == 0)
                break;

            out.println("Added " + n);
            selected.add(n);
        }

        File dest = new File("C:\\AgoraCrawler");
        if(!dest.exists() && !dest.mkdirs())
            throw new AssertionError("Couldn't create dirs " + dest);

        final AtomicInteger n = new AtomicInteger(0);
        IntStream.range(0, crawledCourses.size())
                .filter(selected::contains)
                .mapToObj(i -> crawledCourses.get(i - 1))
                .forEach(course -> {
                    File courseDir = new File(dest, n.incrementAndGet() + ". " + course.getTitle().replaceAll("[\\\\/:*?\"<>|]", "").trim());

                    if(!courseDir.exists())
                        System.out.println("Creating dir " + courseDir.getAbsolutePath());
                    if(!courseDir.exists() && !courseDir.mkdirs())
                        throw new AssertionError("Couldn't create dirs " + courseDir);

                    final AtomicInteger categoriesN = new AtomicInteger(0);

                    List<Category> categories = crawler.fetchCategories(course.getHref());
                    categories.forEach(category -> {

                        File categoryDir = new File(courseDir, categoriesN.incrementAndGet() + ". " + category.getName().replaceAll("[\\\\/:*?\"<>|]", "").trim());

                        if(!categoryDir.exists())
                            System.out.println("Creating dir " + categoryDir.getAbsolutePath());
                        if(!categoryDir.exists() && !categoryDir.mkdirs())
                            throw new AssertionError("Couldn't create dirs " + categoryDir);

                        LinkedList<Integer> currentNumbers = new LinkedList<>();
                        final AtomicInteger lastIndent = new AtomicInteger(-1);

                        category.getActivities().forEach(activity -> {

                            if(activity.getIndent() > lastIndent.get())
                                currentNumbers.add(0);
                            if(activity.getIndent() < lastIndent.get())
                                currentNumbers.removeLast();
                            lastIndent.set(activity.getIndent());

                            currentNumbers.set(currentNumbers.size() - 1, currentNumbers.getLast() + 1);

                            StringBuilder folderNumber = new StringBuilder();
                            for(int i : currentNumbers)
                                folderNumber.append(i).append(".");
                            folderNumber.append(" ");

                            File activityDir = new File(categoryDir, folderNumber.toString() + activity.getName().replaceAll("[\\\\/:*?\"<>|]", "").trim());

                            if(!activityDir.exists())
                                System.out.println("Creating dir " + activityDir.getAbsolutePath());
                            if(!activityDir.exists() && !activityDir.mkdirs())
                                throw new AssertionError("Couldn't create dirs " + activityDir);
                            activity.writeInFolder(activityDir.toPath());
                        });
                    });
                });
    }

    private int readInt() {
        while (true) {
            try {
                String s = scanner.nextLine();
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
                out.println("Not a valid number");
            }
        }
    }
}
