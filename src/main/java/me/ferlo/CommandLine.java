package me.ferlo;

import com.google.inject.Inject;
import me.ferlo.crawler.CrawlerService;
import me.ferlo.crawler.category.Category;
import me.ferlo.crawler.course.Course;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        final Path root = Paths.get("C:", "AgoraCrawler");
        try {
            if(!Files.exists(root)) {
                System.out.println("Creating dir " + root.toAbsolutePath());
                Files.createDirectories(root);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        final AtomicInteger n = new AtomicInteger(0);
        IntStream.range(0, crawledCourses.size())
                .filter(selected::contains)
                .mapToObj(i -> crawledCourses.get(i - 1))
                .forEach(course -> {
                    String courseName = n.incrementAndGet() + ". " + course.getTitle().replaceAll("[\\\\/:*?\"<>|]", "").trim();
                    Path courseDir = root.resolve(courseName);

                    try {
                        if(!Files.exists(courseDir)) {
                            System.out.println("Creating dir " + courseDir.toAbsolutePath());
                            Files.createDirectories(courseDir);
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }

                    final AtomicInteger categoriesN = new AtomicInteger(0);
                    List<Category> categories = crawler.fetchCategories(course.getHref());
                    categories.forEach(category -> {

                        String categoryName = categoriesN.incrementAndGet() + ". " + category.getName().replaceAll("[\\\\/:*?\"<>|]", "").trim();
                        Path categoryDir = courseDir.resolve(categoryName);

                        try {
                            if(!Files.exists(categoryDir)) {
                                System.out.println("Creating dir " + categoryDir.toAbsolutePath());
                                Files.createDirectories(categoryDir);
                            }
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }

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

                            String activityName = folderNumber.toString() + activity.getName().replaceAll("[\\\\/:*?\"<>|]", "").trim();
                            Path activityDir = categoryDir.resolve(activityName);

                            try {
                                if(!Files.exists(activityDir)) {
                                    System.out.println("Creating dir " + activityDir.toAbsolutePath());
                                    Files.createDirectories(activityDir);
                                }
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }

                            activity.writeInFolder(activityDir);
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
