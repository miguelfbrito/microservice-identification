import com.github.javaparser.ast.CompilationUnit;
import extraction.ExtractOperations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import parser.Parse;
import parser.ParseResultServices;
import parser.Parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;


public class ParseTests {

    private static String PROJECTS_ROOT;

    @BeforeAll
    public static void checkEnv() {
        if (System.getenv("CI") == null) {
            PROJECTS_ROOT = "/home/mbrito/git/thesis-web-applications/monoliths";
        } else {
            PROJECTS_ROOT = System.getenv("GITHUB_WORKSPACE") + "/thesis-web-applications/monoliths";
        }
    }

    public ParseResultServices shouldParseProjectWithClusters(String clusters, String path) throws IOException {
        List<CompilationUnit> compilationUnits = new Parser().parseProject(Path.of(path));
        Parse parse = new Parse();
        return parse.completeParseClusters(compilationUnits, clusters);
    }


    @Test
    public void parseSpringBlog() throws IOException {
        String clusters = "[['com.raysmond.blog.JpaConfig', 'com.raysmond.blog.Application'], ['com.raysmond.blog.constants.Constants'], ['com.raysmond.blog.WebConfig', 'com.raysmond.blog.support.web.ViewHelper', 'com.raysmond.blog.services.AppSetting', 'com.raysmond.blog.models.support.WebError', 'com.raysmond.blog.repositories.SettingRepository', 'com.raysmond.blog.models.Setting', 'com.raysmond.blog.notificators.telegram.TelegramBot', 'com.raysmond.blog.services.TelegramBotSettings', 'com.raysmond.blog.forms.LikeForm', 'com.raysmond.blog.forms.SettingsForm', 'com.raysmond.blog.controllers.SympathyRequestData', 'com.raysmond.blog.services.RequestProcessorService', 'com.raysmond.blog.services.CacheSettingService', 'com.raysmond.blog.services.SettingService', 'com.raysmond.blog.admin.controllers.AdminController'], ['com.raysmond.blog.SecurityConfig', 'com.raysmond.blog.services.UserService', 'com.raysmond.blog.repositories.UserRepository', 'com.raysmond.blog.models.User', 'com.raysmond.blog.repositories.LikeRepository', 'com.raysmond.blog.models.Like', 'com.raysmond.blog.forms.UserForm', 'com.raysmond.blog.models.BaseModel', 'com.raysmond.blog.services.LikeService', 'com.raysmond.blog.admin.controllers.UserController'], ['com.raysmond.blog.utils.PaginatorUtil'], ['com.raysmond.blog.utils.DTOUtil'], ['com.raysmond.blog.utils.CommonHelper'], ['com.raysmond.blog.support.web.SyntaxHighlightService', 'com.raysmond.blog.models.Post', 'com.raysmond.blog.support.web.FlexmarkMarkdownService', 'com.raysmond.blog.support.web.MarkdownService', 'com.raysmond.blog.repositories.TagRepository', 'com.raysmond.blog.models.Tag', 'com.raysmond.blog.repositories.SeoPostDataRepository', 'com.raysmond.blog.models.SeoPostData', 'com.raysmond.blog.repositories.PostRepository', 'com.raysmond.blog.notificators.Notificator', 'com.raysmond.blog.seo.controllers.SitemapController', 'com.raysmond.blog.services.PostService', 'com.raysmond.blog.services.SeoService', 'com.raysmond.blog.forms.PostPreviewForm', 'com.raysmond.blog.forms.PostForm', 'com.raysmond.blog.models.dto.PostAnnouncementDTO', 'com.raysmond.blog.models.dto.AjaxAnswerDTO', 'com.raysmond.blog.models.dto.PostIdTitleDTO', 'com.raysmond.blog.models.dto.PostPreviewDTO', 'com.raysmond.blog.admin.controllers.PostController', 'com.raysmond.blog.controllers.TagController', 'com.raysmond.blog.services.TagService', 'com.raysmond.blog.admin.controllers.NotificatorController'], ['com.raysmond.blog.support.web.HttpContentTypeSerializer'], ['com.raysmond.blog.support.web.Message', 'com.raysmond.blog.support.web.MessageHelper'], ['com.raysmond.blog.support.web.extensions.YouTubeLinkExtension'], ['com.raysmond.blog.repositories.SeoRobotAgentRepository', 'com.raysmond.blog.models.SeoRobotAgent', 'com.raysmond.blog.repositories.VisitRepository', 'com.raysmond.blog.models.Visit', 'com.raysmond.blog.forms.SeoRobotAgentForm', 'com.raysmond.blog.services.VisitService', 'com.raysmond.blog.admin.controllers.SeoRobotAgentController'], ['com.raysmond.blog.repositories.StoredFileRepository', 'com.raysmond.blog.models.StoredFile', 'com.raysmond.blog.forms.StoredFileForm', 'com.raysmond.blog.error.NotFoundException', 'com.raysmond.blog.controllers.HomeController', 'com.raysmond.blog.admin.controllers.StoredFileController', 'com.raysmond.blog.services.FileStorageService', 'com.raysmond.blog.error.ExceptionHandlerController'], ['com.raysmond.blog.models.support.FailedToLoadClassName'], ['com.raysmond.blog.models.dto.VisitStatDTO'], ['com.raysmond.blog.models.dto.PostsIdListDTO', 'com.raysmond.blog.models.dto.Series', 'com.raysmond.blog.services.StatisticsService', 'com.raysmond.blog.admin.controllers.StatisticsController'], ['com.raysmond.blog.services.SeoRobotAgentService']]";
        String path = PROJECTS_ROOT + "/spring-blog";

        ParseResultServices parseResultServices = shouldParseProjectWithClusters(clusters, path);
        ExtractOperations.extractAtServiceLevel(parseResultServices);

        assertEquals(15, parseResultServices.getServices().size());
    }
}
