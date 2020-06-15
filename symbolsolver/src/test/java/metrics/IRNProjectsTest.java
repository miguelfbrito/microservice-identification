package metrics;

import com.github.javaparser.ast.CompilationUnit;
import graph.MyGraph;
import graph.creation.ByMethodCallInvocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import parser.Parse;
import parser.ParseResultServices;
import parser.Parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;


@Disabled
public class IRNProjectsTest {

    private static String PROJECTS_ROOT;

    @BeforeAll
    public static void checkEnv() {

        if (System.getenv("CI") == null) {
            PROJECTS_ROOT = "/home/mbrito/git/thesis-web-applications/monoliths";
        } else {
            PROJECTS_ROOT = System.getenv("GITHUB_WORKSPACE") + "/thesis-web-applications/monoliths";
        }
    }

    public double IRNProjectTest(String clusters, String path) throws IOException {
        List<CompilationUnit> compilationUnits = new Parser().parseProject(Path.of(path));
        Parse parse = new Parse();
        ParseResultServices parseResultServices = parse.completeParseClusters(compilationUnits, clusters);

        MyGraph graphReference = new ByMethodCallInvocation(parseResultServices);
        Metric IRN = new IRN(graphReference, parseResultServices);
        double irn = IRN.calculateService();
        System.out.println("IRN Project: " + irn);
        return irn;
    }

    @Test
    public void IRNSpringPetClinic() throws IOException {
        String clusters = "[['FailedToLoadPackageName.MavenWrapperDownloader'], ['org.springframework.samples.petclinic.vet.Specialty', 'org.springframework.samples.petclinic.vet.Vets', 'org.springframework.samples.petclinic.vet.Vet', 'org.springframework.samples.petclinic.vet.VetRepository', 'org.springframework.samples.petclinic.vet.VetController', 'org.springframework.samples.petclinic.PetclinicIntegrationTests', 'org.springframework.samples.petclinic.vet.VetTests', 'org.springframework.samples.petclinic.vet.VetControllerTests'], ['org.springframework.samples.petclinic.model.NamedEntity', 'org.springframework.samples.petclinic.model.Person', 'org.springframework.samples.petclinic.model.BaseEntity', 'org.springframework.samples.petclinic.service.EntityUtils', 'org.springframework.samples.petclinic.model.ValidatorTests'], ['org.springframework.samples.petclinic.system.CrashController', 'org.springframework.samples.petclinic.system.CrashControllerTests'], ['org.springframework.samples.petclinic.system.WelcomeController'], ['org.springframework.samples.petclinic.system.CacheConfiguration'], ['org.springframework.samples.petclinic.visit.VisitRepository', 'org.springframework.samples.petclinic.visit.Visit', 'org.springframework.samples.petclinic.owner.VisitController', 'org.springframework.samples.petclinic.service.ClinicServiceTests', 'org.springframework.samples.petclinic.owner.VisitControllerTests'], ['org.springframework.samples.petclinic.owner.OwnerRepository', 'org.springframework.samples.petclinic.owner.Owner', 'org.springframework.samples.petclinic.owner.OwnerController', 'org.springframework.samples.petclinic.owner.OwnerControllerTests'], ['org.springframework.samples.petclinic.owner.PetRepository', 'org.springframework.samples.petclinic.owner.PetType', 'org.springframework.samples.petclinic.owner.Pet', 'org.springframework.samples.petclinic.owner.PetTypeFormatter', 'org.springframework.samples.petclinic.owner.PetValidator', 'org.springframework.samples.petclinic.owner.PetController', 'org.springframework.samples.petclinic.owner.PetControllerTests', 'org.springframework.samples.petclinic.owner.PetTypeFormatterTests']]";
        String path = PROJECTS_ROOT + "/spring-petclinic";

        double irn = IRNProjectTest(clusters, path);
        assertEquals(20, irn);
    }

    @Test
    public void IRNSpringBlog() throws IOException {

        String clusters = "[['com.raysmond.blog.JpaConfig', 'com.raysmond.blog.Application'], ['com.raysmond.blog.constants.Constants'], ['com.raysmond.blog.WebConfig', 'com.raysmond.blog.support.web.ViewHelper', 'com.raysmond.blog.services.AppSetting', 'com.raysmond.blog.models.support.WebError', 'com.raysmond.blog.repositories.SettingRepository', 'com.raysmond.blog.models.Setting', 'com.raysmond.blog.notificators.telegram.TelegramBot', 'com.raysmond.blog.services.TelegramBotSettings', 'com.raysmond.blog.forms.LikeForm', 'com.raysmond.blog.forms.SettingsForm', 'com.raysmond.blog.controllers.SympathyRequestData', 'com.raysmond.blog.services.RequestProcessorService', 'com.raysmond.blog.services.CacheSettingService', 'com.raysmond.blog.services.SettingService', 'com.raysmond.blog.admin.controllers.AdminController'], ['com.raysmond.blog.SecurityConfig', 'com.raysmond.blog.services.UserService', 'com.raysmond.blog.repositories.UserRepository', 'com.raysmond.blog.models.User', 'com.raysmond.blog.repositories.LikeRepository', 'com.raysmond.blog.models.Like', 'com.raysmond.blog.forms.UserForm', 'com.raysmond.blog.models.BaseModel', 'com.raysmond.blog.services.LikeService', 'com.raysmond.blog.admin.controllers.UserController'], ['com.raysmond.blog.utils.PaginatorUtil'], ['com.raysmond.blog.utils.DTOUtil'], ['com.raysmond.blog.utils.CommonHelper'], ['com.raysmond.blog.support.web.SyntaxHighlightService', 'com.raysmond.blog.models.Post', 'com.raysmond.blog.support.web.FlexmarkMarkdownService', 'com.raysmond.blog.support.web.MarkdownService', 'com.raysmond.blog.repositories.TagRepository', 'com.raysmond.blog.models.Tag', 'com.raysmond.blog.repositories.SeoPostDataRepository', 'com.raysmond.blog.models.SeoPostData', 'com.raysmond.blog.repositories.PostRepository', 'com.raysmond.blog.notificators.Notificator', 'com.raysmond.blog.seo.controllers.SitemapController', 'com.raysmond.blog.services.PostService', 'com.raysmond.blog.services.SeoService', 'com.raysmond.blog.forms.PostPreviewForm', 'com.raysmond.blog.forms.PostForm', 'com.raysmond.blog.models.dto.PostAnnouncementDTO', 'com.raysmond.blog.models.dto.AjaxAnswerDTO', 'com.raysmond.blog.models.dto.PostIdTitleDTO', 'com.raysmond.blog.models.dto.PostPreviewDTO', 'com.raysmond.blog.admin.controllers.PostController', 'com.raysmond.blog.controllers.TagController', 'com.raysmond.blog.services.TagService', 'com.raysmond.blog.admin.controllers.NotificatorController'], ['com.raysmond.blog.support.web.HttpContentTypeSerializer'], ['com.raysmond.blog.support.web.Message', 'com.raysmond.blog.support.web.MessageHelper'], ['com.raysmond.blog.support.web.extensions.YouTubeLinkExtension'], ['com.raysmond.blog.repositories.SeoRobotAgentRepository', 'com.raysmond.blog.models.SeoRobotAgent', 'com.raysmond.blog.repositories.VisitRepository', 'com.raysmond.blog.models.Visit', 'com.raysmond.blog.forms.SeoRobotAgentForm', 'com.raysmond.blog.services.VisitService', 'com.raysmond.blog.admin.controllers.SeoRobotAgentController'], ['com.raysmond.blog.repositories.StoredFileRepository', 'com.raysmond.blog.models.StoredFile', 'com.raysmond.blog.forms.StoredFileForm', 'com.raysmond.blog.error.NotFoundException', 'com.raysmond.blog.controllers.HomeController', 'com.raysmond.blog.admin.controllers.StoredFileController', 'com.raysmond.blog.services.FileStorageService', 'com.raysmond.blog.error.ExceptionHandlerController'], ['com.raysmond.blog.models.support.FailedToLoadClassName'], ['com.raysmond.blog.models.dto.VisitStatDTO'], ['com.raysmond.blog.models.dto.PostsIdListDTO', 'com.raysmond.blog.models.dto.Series', 'com.raysmond.blog.services.StatisticsService', 'com.raysmond.blog.admin.controllers.StatisticsController'], ['com.raysmond.blog.services.SeoRobotAgentService']]";
        String path = PROJECTS_ROOT + "/spring-blog";

        double irn = IRNProjectTest(clusters, path);
        assertEquals(45, irn);
    }


    @Test
    public void IRNSpringBlogAsSingleCluster() throws IOException {

        String clusters = "[['com.raysmond.blog.JpaConfig', 'com.raysmond.blog.Application', 'com.raysmond.blog.constants.Constants', 'com.raysmond.blog.WebConfig', 'com.raysmond.blog.support.web.ViewHelper', 'com.raysmond.blog.services.AppSetting', 'com.raysmond.blog.models.support.WebError', 'com.raysmond.blog.repositories.SettingRepository', 'com.raysmond.blog.models.Setting', 'com.raysmond.blog.notificators.telegram.TelegramBot', 'com.raysmond.blog.services.TelegramBotSettings', 'com.raysmond.blog.forms.LikeForm', 'com.raysmond.blog.forms.SettingsForm', 'com.raysmond.blog.controllers.SympathyRequestData', 'com.raysmond.blog.services.RequestProcessorService', 'com.raysmond.blog.services.CacheSettingService', 'com.raysmond.blog.services.SettingService', 'com.raysmond.blog.admin.controllers.AdminController', 'com.raysmond.blog.SecurityConfig', 'com.raysmond.blog.services.UserService', 'com.raysmond.blog.repositories.UserRepository', 'com.raysmond.blog.models.User', 'com.raysmond.blog.repositories.LikeRepository', 'com.raysmond.blog.models.Like', 'com.raysmond.blog.forms.UserForm', 'com.raysmond.blog.models.BaseModel', 'com.raysmond.blog.services.LikeService', 'com.raysmond.blog.admin.controllers.UserController', 'com.raysmond.blog.utils.PaginatorUtil', 'com.raysmond.blog.utils.DTOUtil', 'com.raysmond.blog.utils.CommonHelper', 'com.raysmond.blog.support.web.SyntaxHighlightService', 'com.raysmond.blog.models.Post', 'com.raysmond.blog.support.web.FlexmarkMarkdownService', 'com.raysmond.blog.support.web.MarkdownService', 'com.raysmond.blog.repositories.TagRepository', 'com.raysmond.blog.models.Tag', 'com.raysmond.blog.repositories.SeoPostDataRepository', 'com.raysmond.blog.models.SeoPostData', 'com.raysmond.blog.repositories.PostRepository', 'com.raysmond.blog.notificators.Notificator', 'com.raysmond.blog.seo.controllers.SitemapController', 'com.raysmond.blog.services.PostService', 'com.raysmond.blog.services.SeoService', 'com.raysmond.blog.forms.PostPreviewForm', 'com.raysmond.blog.forms.PostForm', 'com.raysmond.blog.models.dto.PostAnnouncementDTO', 'com.raysmond.blog.models.dto.AjaxAnswerDTO', 'com.raysmond.blog.models.dto.PostIdTitleDTO', 'com.raysmond.blog.models.dto.PostPreviewDTO', 'com.raysmond.blog.admin.controllers.PostController', 'com.raysmond.blog.controllers.TagController', 'com.raysmond.blog.services.TagService', 'com.raysmond.blog.admin.controllers.NotificatorController', 'com.raysmond.blog.support.web.HttpContentTypeSerializer', 'com.raysmond.blog.support.web.Message', 'com.raysmond.blog.support.web.MessageHelper', 'com.raysmond.blog.support.web.extensions.YouTubeLinkExtension', 'com.raysmond.blog.repositories.SeoRobotAgentRepository', 'com.raysmond.blog.models.SeoRobotAgent', 'com.raysmond.blog.repositories.VisitRepository', 'com.raysmond.blog.models.Visit', 'com.raysmond.blog.forms.SeoRobotAgentForm', 'com.raysmond.blog.services.VisitService', 'com.raysmond.blog.admin.controllers.SeoRobotAgentController', 'com.raysmond.blog.repositories.StoredFileRepository', 'com.raysmond.blog.models.StoredFile', 'com.raysmond.blog.forms.StoredFileForm', 'com.raysmond.blog.error.NotFoundException', 'com.raysmond.blog.controllers.HomeController', 'com.raysmond.blog.admin.controllers.StoredFileController', 'com.raysmond.blog.services.FileStorageService', 'com.raysmond.blog.error.ExceptionHandlerController', 'com.raysmond.blog.models.support.FailedToLoadClassName', 'com.raysmond.blog.models.dto.VisitStatDTO', 'com.raysmond.blog.models.dto.PostsIdListDTO', 'com.raysmond.blog.models.dto.Series', 'com.raysmond.blog.services.StatisticsService', 'com.raysmond.blog.admin.controllers.StatisticsController', 'com.raysmond.blog.services.SeoRobotAgentService']]";
        String path = PROJECTS_ROOT + "/spring-blog";

        double irn = IRNProjectTest(clusters, path);
        assertEquals(0, irn);
    }

    @Test
    public void IRNSpringPetClinicAsSingleCluster() throws IOException {
        String clusters = "[['FailedToLoadPackageName.MavenWrapperDownloader', 'org.springframework.samples.petclinic.vet.Specialty', 'org.springframework.samples.petclinic.vet.Vets', 'org.springframework.samples.petclinic.vet.Vet', 'org.springframework.samples.petclinic.vet.VetRepository', 'org.springframework.samples.petclinic.vet.VetController', 'org.springframework.samples.petclinic.PetclinicIntegrationTests', 'org.springframework.samples.petclinic.vet.VetTests', 'org.springframework.samples.petclinic.vet.VetControllerTests', 'org.springframework.samples.petclinic.model.NamedEntity', 'org.springframework.samples.petclinic.model.Person', 'org.springframework.samples.petclinic.model.BaseEntity', 'org.springframework.samples.petclinic.service.EntityUtils', 'org.springframework.samples.petclinic.model.ValidatorTests', 'org.springframework.samples.petclinic.system.CrashController', 'org.springframework.samples.petclinic.system.CrashControllerTests', 'org.springframework.samples.petclinic.system.WelcomeController', 'org.springframework.samples.petclinic.system.CacheConfiguration', 'org.springframework.samples.petclinic.visit.VisitRepository', 'org.springframework.samples.petclinic.visit.Visit', 'org.springframework.samples.petclinic.owner.VisitController', 'org.springframework.samples.petclinic.service.ClinicServiceTests', 'org.springframework.samples.petclinic.owner.VisitControllerTests', 'org.springframework.samples.petclinic.owner.OwnerRepository', 'org.springframework.samples.petclinic.owner.Owner', 'org.springframework.samples.petclinic.owner.OwnerController', 'org.springframework.samples.petclinic.owner.OwnerControllerTests', 'org.springframework.samples.petclinic.owner.PetRepository', 'org.springframework.samples.petclinic.owner.PetType', 'org.springframework.samples.petclinic.owner.Pet', 'org.springframework.samples.petclinic.owner.PetTypeFormatter', 'org.springframework.samples.petclinic.owner.PetValidator', 'org.springframework.samples.petclinic.owner.PetController', 'org.springframework.samples.petclinic.owner.PetControllerTests', 'org.springframework.samples.petclinic.owner.PetTypeFormatterTests']]";
        String path = PROJECTS_ROOT + "/spring-petclinic";

        double irn = IRNProjectTest(clusters, path);
        assertEquals(0, irn);
    }

    @Test
    public void IRNMonoMusic() throws IOException {

        String clusters = "[['de.infonautika.monomusiccorp.app.ApplicationSecurity', 'de.infonautika.monomusiccorp.app.controller.utils.links.curi.RelationMethodRegistry', 'de.infonautika.monomusiccorp.app.controller.utils.links.curi.MethodCuriProvider', 'de.infonautika.monomusiccorp.app.security.ModifiableUserDetailsManager', 'de.infonautika.monomusiccorp.app.security.ModifiableUserDetailsManagerImpl', 'de.infonautika.monomusiccorp.app.controller.ResourceNotFoundException', 'de.infonautika.monomusiccorp.app.controller.utils.links.curi.CuriInfo', 'de.infonautika.monomusiccorp.app.controller.utils.links.curi.RelationMethod', 'de.infonautika.monomusiccorp.app.security.DefaultUsers', 'de.infonautika.monomusiccorp.app.security.MutableUserDetails', 'de.infonautika.monomusiccorp.app.security.SecurityServiceImpl', 'de.infonautika.monomusiccorp.app.business.errors.ConflictException'], ['de.infonautika.monomusiccorp.app.business.ApplicationState', 'de.infonautika.monomusiccorp.app.controller.CatalogController', 'de.infonautika.monomusiccorp.app.repository.ProductLookup', 'de.infonautika.monomusiccorp.app.controller.resources.ProductResource', 'de.infonautika.monomusiccorp.app.controller.resources.ProductResourceAssembler', 'de.infonautika.monomusiccorp.app.domain.Product', 'de.infonautika.monomusiccorp.app.controller.StockController', 'de.infonautika.monomusiccorp.app.repository.StockItemRepository', 'de.infonautika.monomusiccorp.app.controller.resources.StockItemResource', 'de.infonautika.monomusiccorp.app.domain.StockItem', 'de.infonautika.monomusiccorp.app.controller.resources.StockItemResourceAssembler', 'de.infonautika.monomusiccorp.app.repository.ProductRepository', 'de.infonautika.monomusiccorp.app.repository.ProductLookupImpl', 'de.infonautika.monomusiccorp.app.controller.StockControllerTest'], ['de.infonautika.monomusiccorp.app.util.VoidOptional'], ['de.infonautika.monomusiccorp.app.controller.utils.AuthorizedInvocationFilter', 'de.infonautika.monomusiccorp.app.controller.utils.links.Invocation', 'de.infonautika.monomusiccorp.app.controller.InfoController', 'de.infonautika.monomusiccorp.app.controller.resources.MessageResource', 'de.infonautika.monomusiccorp.app.controller.UserController', 'de.infonautika.monomusiccorp.app.security.AuthenticationFacade', 'de.infonautika.monomusiccorp.app.controller.utils.LinkSupport', 'de.infonautika.monomusiccorp.app.controller.utils.links.LinkCreator', 'de.infonautika.monomusiccorp.app.controller.utils.links.LinkFacade', 'de.infonautika.monomusiccorp.app.controller.utils.links.InvocationInterceptor', 'de.infonautika.monomusiccorp.app.security.AuthenticationFacadeImpl', 'de.infonautika.monomusiccorp.app.controller.CatalogControllerTest', 'de.infonautika.monomusiccorp.app.controller.UserControllerTest', 'de.infonautika.monomusiccorp.app.controller.utils.DummyController', 'de.infonautika.monomusiccorp.app.controller.utils.links.MyController'], ['de.infonautika.monomusiccorp.app.controller.ShoppingController', 'de.infonautika.monomusiccorp.app.business.BusinessProcess', 'de.infonautika.monomusiccorp.app.intermediate.CurrentCustomerProvider', 'de.infonautika.monomusiccorp.app.controller.resources.PositionResource', 'de.infonautika.monomusiccorp.app.controller.resources.PositionResourceAssembler', 'de.infonautika.monomusiccorp.app.domain.Customer', 'de.infonautika.monomusiccorp.app.repository.CustomerLookup', 'de.infonautika.monomusiccorp.app.controller.CustomerController', 'de.infonautika.monomusiccorp.app.controller.resources.CustomerResource', 'de.infonautika.monomusiccorp.app.controller.resources.CustomerResourceAssembler', 'de.infonautika.monomusiccorp.app.business.CustomerInfo', 'de.infonautika.monomusiccorp.app.domain.Address', 'de.infonautika.monomusiccorp.app.repository.CustomerRepository', 'de.infonautika.monomusiccorp.app.intermediate.CurrentCustomerProviderImpl', 'de.infonautika.monomusiccorp.app.repository.CustomerLookupImpl', 'de.infonautika.monomusiccorp.app.controller.CustomerControllerTest', 'de.infonautika.monomusiccorp.app.controller.ShoppingControllerTest', 'de.infonautika.monomusiccorp.app.intermediate.CurrentCustomerProviderImplTest'], ['de.infonautika.monomusiccorp.app.domain.Position', 'de.infonautika.monomusiccorp.app.domain.PricedPosition', 'de.infonautika.monomusiccorp.app.domain.Money', 'de.infonautika.monomusiccorp.app.security.SecurityService', 'de.infonautika.monomusiccorp.app.business.InvoiceDelivery', 'de.infonautika.monomusiccorp.app.domain.Invoice', 'de.infonautika.monomusiccorp.app.business.DummyInvoiceDeliveryImpl', 'de.infonautika.monomusiccorp.app.business.BusinessProcessImpl', 'de.infonautika.monomusiccorp.app.repository.OrderRepository', 'de.infonautika.monomusiccorp.app.repository.ShoppingBasketRepository', 'de.infonautika.monomusiccorp.app.repository.InvoiceRepository', 'de.infonautika.monomusiccorp.app.domain.ShoppingBasket', 'de.infonautika.monomusiccorp.app.domain.Order', 'de.infonautika.monomusiccorp.app.domain.HasPositions', 'de.infonautika.monomusiccorp.app.domain.HasPricedPositions', 'de.infonautika.monomusiccorp.app.business.StateSetup', 'de.infonautika.monomusiccorp.app.domain.ShoppingBasketTest'], ['de.infonautika.monomusiccorp.app.business.errors.ForbiddenException', 'de.infonautika.monomusiccorp.app.controller.OrdersController', 'de.infonautika.monomusiccorp.app.repository.PickingOrderRepository', 'de.infonautika.monomusiccorp.app.controller.resources.OrderStatusResource', 'de.infonautika.monomusiccorp.app.business.errors.DoesNotExistException', 'de.infonautika.monomusiccorp.app.domain.PickingOrder', 'de.infonautika.monomusiccorp.app.controller.resources.OrderStatusResourceAssembler', 'de.infonautika.monomusiccorp.app.controller.resources.PricedPositionResource', 'de.infonautika.monomusiccorp.app.controller.resources.PricedPositionResourceAssembler', 'de.infonautika.monomusiccorp.app.business.StockNotification', 'de.infonautika.monomusiccorp.app.business.DummyStockNotificationImpl', 'de.infonautika.monomusiccorp.app.business.errors.BusinessError', 'de.infonautika.monomusiccorp.app.controller.OrdersControllerTest'], ['de.infonautika.monomusiccorp.app.controller.utils.Results'], ['de.infonautika.monomusiccorp.app.controller.utils.links.FailedToLoadClassName'], ['de.infonautika.monomusiccorp.app.security.UserRole'], ['de.infonautika.monomusiccorp.app.persist.LocalDateTimeConverter'], ['de.infonautika.monomusiccorp.app.domain.Currencies'], ['de.infonautika.monomusiccorp.app.JsonTesterRule'], ['de.infonautika.monomusiccorp.app.DescribingMatcherBuilder', 'de.infonautika.monomusiccorp.app.BiDescribingMatcher', 'de.infonautika.monomusiccorp.app.BiDescribingMatcherBuilder'], ['de.infonautika.monomusiccorp.app.controller.ControllerConstants'], ['de.infonautika.monomusiccorp.app.controller.MatcherDebug']]";
        String path = PROJECTS_ROOT + "/monomusiccorp";
        double irn = IRNProjectTest(clusters, path);
        assertEquals(82, irn);
    }


    @Test
    @Disabled
    public void IRNTestProject() throws IOException {

        String clusters = "[['com.test.Main'], ['com.test.OtherClass']]";
        String path = PROJECTS_ROOT + "/test";

        double irn = IRNProjectTest(clusters, path);
        assertEquals(6, irn);
    }

    @Test
    public void IRNJpetStore() throws IOException {

        String clusters = "[['FailedToLoadPackageName.MavenWrapperDownloader'], ['org.mybatis.jpetstore.web.actions.CartActionBean', 'org.mybatis.jpetstore.domain.Cart', 'org.mybatis.jpetstore.domain.Item', 'org.mybatis.jpetstore.domain.CartItem', 'org.mybatis.jpetstore.domain.CartTest', 'org.mybatis.jpetstore.domain.OrderTest'], ['org.mybatis.jpetstore.web.actions.AbstractActionBean', 'org.mybatis.jpetstore.web.actions.AccountActionBean', 'org.mybatis.jpetstore.service.AccountService', 'org.mybatis.jpetstore.domain.Account', 'org.mybatis.jpetstore.web.actions.OrderActionBean', 'org.mybatis.jpetstore.mapper.AccountMapper', 'org.mybatis.jpetstore.web.actions.AccountActionBeanTest', 'org.mybatis.jpetstore.web.actions.OrderActionBeanTest', 'org.mybatis.jpetstore.mapper.AccountMapperTest', 'org.mybatis.jpetstore.service.AccountServiceTest'], ['org.mybatis.jpetstore.service.CatalogService', 'org.mybatis.jpetstore.domain.Product', 'org.mybatis.jpetstore.web.actions.CatalogActionBean', 'org.mybatis.jpetstore.domain.Category', 'org.mybatis.jpetstore.mapper.CategoryMapper', 'org.mybatis.jpetstore.mapper.ProductMapper', 'org.mybatis.jpetstore.web.actions.CatalogActionBeanTest', 'org.mybatis.jpetstore.mapper.ProductMapperTest', 'org.mybatis.jpetstore.mapper.CategoryMapperTest', 'org.mybatis.jpetstore.service.CatalogServiceTest'], ['org.mybatis.jpetstore.service.OrderService', 'org.mybatis.jpetstore.domain.Order', 'org.mybatis.jpetstore.mapper.OrderMapper', 'org.mybatis.jpetstore.mapper.ItemMapper', 'org.mybatis.jpetstore.mapper.SequenceMapper', 'org.mybatis.jpetstore.domain.Sequence', 'org.mybatis.jpetstore.mapper.LineItemMapper', 'org.mybatis.jpetstore.domain.LineItem', 'org.mybatis.jpetstore.mapper.OrderMapperTest', 'org.mybatis.jpetstore.mapper.MapperTestContext', 'org.mybatis.jpetstore.mapper.ItemMapperTest', 'org.mybatis.jpetstore.mapper.LineItemMapperTest', 'org.mybatis.jpetstore.mapper.SequenceMapperTest', 'org.mybatis.jpetstore.service.OrderServiceTest'], ['org.mybatis.jpetstore.ScreenTransitionIT']]";
        String path = PROJECTS_ROOT + "/jpetstore";

        double irn = IRNProjectTest(clusters, path);
        System.out.println("Irn:  " + irn);
        assertEquals(46, irn);
    }

}
