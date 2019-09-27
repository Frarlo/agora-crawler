package me.ferlo.crawler.activities;

import com.google.inject.assistedinject.Assisted;
import me.ferlo.crawler.Resource;

import javax.inject.Named;
import java.util.List;

public interface ActivityFactory {

    @Named("unsupported") Activity createUnsupported(@Assisted("name") String name,
                                                     @Assisted("href") String href,
                                                     @Assisted("type") String type,
                                                     @Assisted("indent") int indent);

    @Named("assignment") Activity createAssignment(@Assisted("name") String name,
                                                   @Assisted("href") String href,
                                                   @Assisted("type") String type,
                                                   @Assisted("indent") int indent,
                                                   @Assisted("desc") String desc,
                                                   @Assisted("resources") List<Resource> resources,
                                                   @Assisted("submissions") List<Resource> submissions);

    @Named("resource") Activity createResource(@Assisted("name") String name,
                                               @Assisted("href") String href,
                                               @Assisted("type") String type,
                                               @Assisted("indent") int indent);

    @Named("page") Activity createPage(@Assisted("name") String name,
                                       @Assisted("href") String href,
                                       @Assisted("type") String type,
                                       @Assisted("indent") int indent,
                                       @Assisted("content") String content,
                                       @Assisted("resources") List<Resource> resources);

    @Named("url") Activity createUrl(@Assisted("name") String name,
                                     @Assisted("href") String href,
                                     @Assisted("type") String type,
                                     @Assisted("indent") int indent,
                                     @Assisted("content") String content,
                                     @Assisted("resources") List<Resource> resources);

    @Named("quiz") Activity createQuiz(@Assisted("name") String name,
                                       @Assisted("href") String href,
                                       @Assisted("type") String type,
                                       @Assisted("indent") int indent,
                                       @Assisted("quizHtml") String quizHtml,
                                       @Assisted("resources") List<Resource> resources);
}
