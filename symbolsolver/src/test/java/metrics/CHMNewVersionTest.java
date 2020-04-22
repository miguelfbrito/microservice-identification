package metrics;

import com.github.javaparser.ast.CompilationUnit;
import graph.MyGraph;
import graph.creation.ByMethodCallInvocationClusters;
import graph.entities.Service;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import parser.Parser;
import utils.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CHMNewVersionTest {


    private static String PROJECTS_ROOT;

    @BeforeAll
    public static void checkEnv() {
        if (System.getenv("CI") == null) {
            PROJECTS_ROOT = "/home/mbrito/git/thesis-web-applications/monoliths";
        } else {
            PROJECTS_ROOT = System.getenv("GITHUB_WORKSPACE") + "/thesis-web-applications/monoliths";
        }
    }

    public double CHMProjectTest(String clusters, String path) throws IOException {
        List<CompilationUnit> compilationUnits = new Parser().parseProject(Path.of(path));
        Map<Integer, Service> mapClusters = StringUtils.extractClustersToServices(clusters);

        System.out.println("Path: " + path);
        System.out.println("Clusters <class, clusterId>:" + mapClusters);
        System.out.println("Number of classes: " + mapClusters.size());

        MyGraph graphReference = new ByMethodCallInvocationClusters(compilationUnits, mapClusters);
        Metric CHM = new CHM(graphReference);
        //double chm = CHM.calculateCluster(mapClusters);
        //System.out.println("CHM Project: " + chm);
        return 0.0;
    }


    @Test
    public void CHMTest() throws IOException {

        String clusters = "[['com.test.Main', 'com.test.OtherClass']]";
        String path = PROJECTS_ROOT + "/test";

        double chm = CHMProjectTest(clusters, path);
        assertEquals(1, chm, 0.01);
    }

    @Test
    public void CHMSpringPetClinic() throws IOException {
        String clusters = "[['FailedToLoadPackageName.MavenWrapperDownloader'], ['org.springframework.samples.petclinic.vet.Specialty', 'org.springframework.samples.petclinic.vet.Vets', 'org.springframework.samples.petclinic.vet.Vet', 'org.springframework.samples.petclinic.vet.VetRepository', 'org.springframework.samples.petclinic.vet.VetController', 'org.springframework.samples.petclinic.PetclinicIntegrationTests', 'org.springframework.samples.petclinic.vet.VetTests', 'org.springframework.samples.petclinic.vet.VetControllerTests'], ['org.springframework.samples.petclinic.model.NamedEntity', 'org.springframework.samples.petclinic.model.Person', 'org.springframework.samples.petclinic.model.BaseEntity', 'org.springframework.samples.petclinic.service.EntityUtils', 'org.springframework.samples.petclinic.model.ValidatorTests'], ['org.springframework.samples.petclinic.system.CrashController', 'org.springframework.samples.petclinic.system.CrashControllerTests'], ['org.springframework.samples.petclinic.system.WelcomeController'], ['org.springframework.samples.petclinic.system.CacheConfiguration'], ['org.springframework.samples.petclinic.visit.VisitRepository', 'org.springframework.samples.petclinic.visit.Visit', 'org.springframework.samples.petclinic.owner.VisitController', 'org.springframework.samples.petclinic.service.ClinicServiceTests', 'org.springframework.samples.petclinic.owner.VisitControllerTests'], ['org.springframework.samples.petclinic.owner.OwnerRepository', 'org.springframework.samples.petclinic.owner.Owner', 'org.springframework.samples.petclinic.owner.OwnerController', 'org.springframework.samples.petclinic.owner.OwnerControllerTests'], ['org.springframework.samples.petclinic.owner.PetRepository', 'org.springframework.samples.petclinic.owner.PetType', 'org.springframework.samples.petclinic.owner.Pet', 'org.springframework.samples.petclinic.owner.PetTypeFormatter', 'org.springframework.samples.petclinic.owner.PetValidator', 'org.springframework.samples.petclinic.owner.PetController', 'org.springframework.samples.petclinic.owner.PetControllerTests', 'org.springframework.samples.petclinic.owner.PetTypeFormatterTests']]";
        String path = PROJECTS_ROOT + "/spring-petclinic";

        double chm = CHMProjectTest(clusters, path);
        assertEquals(0.876, chm, 0.01);
    }


    @Test
    public void CHMSpringBlog() throws IOException {

        String clusters = "[['com.raysmond.blog.JpaConfig', 'com.raysmond.blog.Application'], ['com.raysmond.blog.Constants'], ['com.raysmond.blog.WebConfig', 'com.raysmond.blog.support.web.ViewHelper', 'com.raysmond.blog.services.AppSetting', 'com.raysmond.blog.models.support.WebError', 'com.raysmond.blog.repositories.SettingRepository', 'com.raysmond.blog.models.Setting', 'com.raysmond.blog.notificators.telegram.TelegramBot', 'com.raysmond.blog.services.TelegramBotSettings', 'com.raysmond.blog.forms.LikeForm', 'com.raysmond.blog.forms.SettingsForm', 'com.raysmond.blog.controllers.SympathyRequestData', 'com.raysmond.blog.services.RequestProcessorService', 'com.raysmond.blog.services.CacheSettingService', 'com.raysmond.blog.services.SettingService', 'com.raysmond.blog.admin.controllers.AdminController'], ['com.raysmond.blog.SecurityConfig', 'com.raysmond.blog.services.UserService', 'com.raysmond.blog.repositories.UserRepository', 'com.raysmond.blog.models.User', 'com.raysmond.blog.repositories.LikeRepository', 'com.raysmond.blog.models.Like', 'com.raysmond.blog.forms.UserForm', 'com.raysmond.blog.models.BaseModel', 'com.raysmond.blog.services.LikeService', 'com.raysmond.blog.admin.controllers.UserController'], ['com.raysmond.blog.utils.PaginatorUtil'], ['com.raysmond.blog.utils.DTOUtil'], ['com.raysmond.blog.utils.CommonHelper'], ['com.raysmond.blog.support.web.SyntaxHighlightService', 'com.raysmond.blog.models.Post', 'com.raysmond.blog.support.web.FlexmarkMarkdownService', 'com.raysmond.blog.support.web.MarkdownService', 'com.raysmond.blog.repositories.TagRepository', 'com.raysmond.blog.models.Tag', 'com.raysmond.blog.repositories.SeoPostDataRepository', 'com.raysmond.blog.models.SeoPostData', 'com.raysmond.blog.repositories.PostRepository', 'com.raysmond.blog.notificators.Notificator', 'com.raysmond.blog.seo.controllers.SitemapController', 'com.raysmond.blog.services.PostService', 'com.raysmond.blog.services.SeoService', 'com.raysmond.blog.forms.PostPreviewForm', 'com.raysmond.blog.forms.PostForm', 'com.raysmond.blog.models.dto.PostAnnouncementDTO', 'com.raysmond.blog.models.dto.AjaxAnswerDTO', 'com.raysmond.blog.models.dto.PostIdTitleDTO', 'com.raysmond.blog.models.dto.PostPreviewDTO', 'com.raysmond.blog.admin.controllers.PostController', 'com.raysmond.blog.controllers.TagController', 'com.raysmond.blog.services.TagService', 'com.raysmond.blog.admin.controllers.NotificatorController'], ['com.raysmond.blog.support.web.HttpContentTypeSerializer'], ['com.raysmond.blog.support.web.Message', 'com.raysmond.blog.support.web.MessageHelper'], ['com.raysmond.blog.support.web.extensions.YouTubeLinkExtension'], ['com.raysmond.blog.repositories.SeoRobotAgentRepository', 'com.raysmond.blog.models.SeoRobotAgent', 'com.raysmond.blog.repositories.VisitRepository', 'com.raysmond.blog.models.Visit', 'com.raysmond.blog.forms.SeoRobotAgentForm', 'com.raysmond.blog.services.VisitService', 'com.raysmond.blog.admin.controllers.SeoRobotAgentController'], ['com.raysmond.blog.repositories.StoredFileRepository', 'com.raysmond.blog.models.StoredFile', 'com.raysmond.blog.forms.StoredFileForm', 'com.raysmond.blog.error.NotFoundException', 'com.raysmond.blog.controllers.HomeController', 'com.raysmond.blog.admin.controllers.StoredFileController', 'com.raysmond.blog.services.FileStorageService', 'com.raysmond.blog.error.ExceptionHandlerController'], ['com.raysmond.blog.models.support.FailedToLoadClassName'], ['com.raysmond.blog.models.dto.VisitStatDTO'], ['com.raysmond.blog.models.dto.PostsIdListDTO', 'com.raysmond.blog.models.dto.Series', 'com.raysmond.blog.services.StatisticsService', 'com.raysmond.blog.admin.controllers.StatisticsController'], ['com.raysmond.blog.services.SeoRobotAgentService']]";
        String path = PROJECTS_ROOT + "/spring-blog";

        double chm = CHMProjectTest(clusters, path);
        assertEquals(0.889, chm, 0.001);
    }

       @Test
    public void IRNSpringBlogClustersBiggerThan2Classes() throws IOException {
        String clusters = "[['com.raysmond.blog.WebConfig', 'com.raysmond.blog.support.web.ViewHelper', 'com.raysmond.blog.models.support.WebError', 'com.raysmond.blog.forms.LikeForm', 'com.raysmond.blog.controllers.SympathyRequestData', 'com.raysmond.blog.services.RequestProcessorService'], ['com.raysmond.blog.SecurityConfig', 'com.raysmond.blog.services.UserService', 'com.raysmond.blog.repositories.StoredFileRepository', 'com.raysmond.blog.models.StoredFile', 'com.raysmond.blog.repositories.UserRepository', 'com.raysmond.blog.forms.StoredFileForm', 'com.raysmond.blog.forms.UserForm', 'com.raysmond.blog.admin.controllers.StoredFileController', 'com.raysmond.blog.services.FileStorageService', 'com.raysmond.blog.admin.controllers.UserController'], ['com.raysmond.blog.support.web.SyntaxHighlightService', 'com.raysmond.blog.support.web.FlexmarkMarkdownService', 'com.raysmond.blog.support.web.MarkdownService', 'com.raysmond.blog.forms.PostPreviewForm', 'com.raysmond.blog.forms.PostForm', 'com.raysmond.blog.models.dto.PostPreviewDTO', 'com.raysmond.blog.admin.controllers.PostController'], ['com.raysmond.blog.services.AppSetting', 'com.raysmond.blog.repositories.SettingRepository', 'com.raysmond.blog.models.Setting', 'com.raysmond.blog.notificators.Notificator', 'com.raysmond.blog.notificators.telegram.TelegramBot', 'com.raysmond.blog.services.TelegramBotSettings', 'com.raysmond.blog.seo.controllers.SitemapController', 'com.raysmond.blog.services.SeoService', 'com.raysmond.blog.forms.SettingsForm', 'com.raysmond.blog.models.dto.PostAnnouncementDTO', 'com.raysmond.blog.models.dto.AjaxAnswerDTO', 'com.raysmond.blog.services.CacheSettingService', 'com.raysmond.blog.services.SettingService', 'com.raysmond.blog.admin.controllers.AdminController', 'com.raysmond.blog.admin.controllers.NotificatorController'], ['com.raysmond.blog.models.Post', 'com.raysmond.blog.repositories.SeoRobotAgentRepository', 'com.raysmond.blog.models.SeoRobotAgent', 'com.raysmond.blog.repositories.VisitRepository', 'com.raysmond.blog.models.Visit', 'com.raysmond.blog.models.User', 'com.raysmond.blog.repositories.LikeRepository', 'com.raysmond.blog.models.Like', 'com.raysmond.blog.forms.SeoRobotAgentForm', 'com.raysmond.blog.models.BaseModel', 'com.raysmond.blog.services.LikeService', 'com.raysmond.blog.services.VisitService', 'com.raysmond.blog.admin.controllers.SeoRobotAgentController'], ['com.raysmond.blog.repositories.TagRepository', 'com.raysmond.blog.models.Tag', 'com.raysmond.blog.repositories.SeoPostDataRepository', 'com.raysmond.blog.models.SeoPostData', 'com.raysmond.blog.repositories.PostRepository', 'com.raysmond.blog.services.PostService', 'com.raysmond.blog.models.dto.PostIdTitleDTO', 'com.raysmond.blog.controllers.TagController', 'com.raysmond.blog.services.TagService', 'com.raysmond.blog.error.NotFoundException', 'com.raysmond.blog.controllers.HomeController', 'com.raysmond.blog.error.ExceptionHandlerController'], ['com.raysmond.blog.models.dto.PostsIdListDTO', 'com.raysmond.blog.models.dto.Series', 'com.raysmond.blog.services.StatisticsService', 'com.raysmond.blog.admin.controllers.StatisticsController']]";
        String path = PROJECTS_ROOT + "/spring-blog";

        double chm = CHMProjectTest(clusters, path);
        assertEquals(117, chm);
    }


}
