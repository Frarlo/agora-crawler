package me.ferlo.crawler.activities;

import com.google.inject.assistedinject.Assisted;

import javax.inject.Named;
import java.nio.file.Path;
import java.util.Map;

public interface ActivityFactory {

    @Named("unsupported_activity") Activity createUnsupported(@Assisted("baseData") BaseActivityData baseData);

    @Named("resource_activity") Activity createResource(@Assisted("baseData") BaseActivityData baseData);

    @Named("page_activity") Activity createPage(@Assisted("baseData") BaseActivityData baseData,
                                                @Assisted("content") String content,
                                                @Assisted("resources") Map<Path, String> resources);

    @Named("url_activity") Activity createUrl(@Assisted("baseData") BaseActivityData baseData,
                                              @Assisted("content") String content,
                                              @Assisted("resources") Map<Path, String> resources);

    @Named("quiz_activity") Activity createQuiz(@Assisted("baseData") BaseActivityData baseData,
                                                @Assisted("quizHtml") String quizHtml,
                                                @Assisted("resources") Map<Path, String> resources);

    @Named("assignment_activity") Activity createAssignment(@Assisted("baseData") BaseActivityData baseData,
                                                            @Assisted("desc") String desc,
                                                            @Assisted("resources") Map<Path, String> resources,
                                                            @Assisted("submissions") Map<Path, String> submissions);

    @Named("folder_activity") Activity createFolder(@Assisted("baseData") BaseActivityData baseData,
                                                    @Assisted("docs") Map<Path, String> docs);
}
