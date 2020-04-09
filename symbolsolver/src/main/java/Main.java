import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import graph.MyClass;
import graph.MyGraph;
import metrics.IRNMetric;
import metrics.Metric;
import parser.Parser;
import utils.StringUtils;
import visitors.ClassOrInterfaceDeclarationVisitor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

    public static void main(String[] args) {

        String projectName = "monomusiccorp";
        // String projectName = "test";
        String path = "/home/mbrito/git/thesis-web-applications/monoliths/" + projectName;

        Set<String> qualifiedNames = new HashSet<>();
        Parser parser = new Parser();
        List<CompilationUnit> compilationUnits = null;
        try {
            compilationUnits = parser.parseProject(Path.of(path));
            for (CompilationUnit cu : compilationUnits) {
                cu.accept(new ClassOrInterfaceDeclarationVisitor(), qualifiedNames);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        MyGraph graph = new MyGraph();
        graph.create(compilationUnits);

        System.out.println("\n\n\nCalculating metrics");
        Metric metric = new IRNMetric(graph);
        metric.setup();
        double irn = metric.calculate();
        System.out.println("IRN: " + irn);

        // TODO: Read from file or stdout
        String clusters = "[['de.infonautika.monomusiccorp.app.ApplicationSecurity', 'de.infonautika.monomusiccorp.app.controller.utils.links.curi.RelationMethodRegistry', 'de.infonautika.monomusiccorp.app.controller.utils.links.curi.MethodCuriProvider', 'de.infonautika.monomusiccorp.app.security.ModifiableUserDetailsManager', 'de.infonautika.monomusiccorp.app.security.ModifiableUserDetailsManagerImpl', 'de.infonautika.monomusiccorp.app.controller.ResourceNotFoundException', 'de.infonautika.monomusiccorp.app.controller.utils.links.curi.CuriInfo', 'de.infonautika.monomusiccorp.app.controller.utils.links.curi.RelationMethod', 'de.infonautika.monomusiccorp.app.security.DefaultUsers', 'de.infonautika.monomusiccorp.app.security.MutableUserDetails', 'de.infonautika.monomusiccorp.app.security.SecurityServiceImpl', 'de.infonautika.monomusiccorp.app.business.errors.ConflictException'], ['de.infonautika.monomusiccorp.app.business.ApplicationState', 'de.infonautika.monomusiccorp.app.controller.CatalogController', 'de.infonautika.monomusiccorp.app.repository.ProductLookup', 'de.infonautika.monomusiccorp.app.controller.resources.ProductResource', 'de.infonautika.monomusiccorp.app.controller.resources.ProductResourceAssembler', 'de.infonautika.monomusiccorp.app.domain.Product', 'de.infonautika.monomusiccorp.app.controller.StockController', 'de.infonautika.monomusiccorp.app.repository.StockItemRepository', 'de.infonautika.monomusiccorp.app.controller.resources.StockItemResource', 'de.infonautika.monomusiccorp.app.domain.StockItem', 'de.infonautika.monomusiccorp.app.controller.resources.StockItemResourceAssembler', 'de.infonautika.monomusiccorp.app.repository.ProductRepository', 'de.infonautika.monomusiccorp.app.repository.ProductLookupImpl', 'de.infonautika.monomusiccorp.app.controller.StockControllerTest'], ['de.infonautika.monomusiccorp.app.util.VoidOptional'], ['de.infonautika.monomusiccorp.app.controller.utils.AuthorizedInvocationFilter', 'de.infonautika.monomusiccorp.app.controller.utils.links.Invocation', 'de.infonautika.monomusiccorp.app.controller.InfoController', 'de.infonautika.monomusiccorp.app.controller.resources.MessageResource', 'de.infonautika.monomusiccorp.app.controller.UserController', 'de.infonautika.monomusiccorp.app.security.AuthenticationFacade', 'de.infonautika.monomusiccorp.app.controller.utils.LinkSupport', 'de.infonautika.monomusiccorp.app.controller.utils.links.LinkCreator', 'de.infonautika.monomusiccorp.app.controller.utils.links.LinkFacade', 'de.infonautika.monomusiccorp.app.controller.utils.links.InvocationInterceptor', 'de.infonautika.monomusiccorp.app.security.AuthenticationFacadeImpl', 'de.infonautika.monomusiccorp.app.controller.CatalogControllerTest', 'de.infonautika.monomusiccorp.app.controller.UserControllerTest', 'de.infonautika.monomusiccorp.app.controller.utils.DummyController', 'de.infonautika.monomusiccorp.app.controller.utils.links.MyController'], ['de.infonautika.monomusiccorp.app.controller.ShoppingController', 'de.infonautika.monomusiccorp.app.business.BusinessProcess', 'de.infonautika.monomusiccorp.app.intermediate.CurrentCustomerProvider', 'de.infonautika.monomusiccorp.app.controller.resources.PositionResource', 'de.infonautika.monomusiccorp.app.controller.resources.PositionResourceAssembler', 'de.infonautika.monomusiccorp.app.domain.Customer', 'de.infonautika.monomusiccorp.app.repository.CustomerLookup', 'de.infonautika.monomusiccorp.app.controller.CustomerController', 'de.infonautika.monomusiccorp.app.controller.resources.CustomerResource', 'de.infonautika.monomusiccorp.app.controller.resources.CustomerResourceAssembler', 'de.infonautika.monomusiccorp.app.business.CustomerInfo', 'de.infonautika.monomusiccorp.app.domain.Address', 'de.infonautika.monomusiccorp.app.repository.CustomerRepository', 'de.infonautika.monomusiccorp.app.intermediate.CurrentCustomerProviderImpl', 'de.infonautika.monomusiccorp.app.repository.CustomerLookupImpl', 'de.infonautika.monomusiccorp.app.controller.CustomerControllerTest', 'de.infonautika.monomusiccorp.app.controller.ShoppingControllerTest', 'de.infonautika.monomusiccorp.app.intermediate.CurrentCustomerProviderImplTest'], ['de.infonautika.monomusiccorp.app.domain.Position', 'de.infonautika.monomusiccorp.app.domain.PricedPosition', 'de.infonautika.monomusiccorp.app.domain.Money', 'de.infonautika.monomusiccorp.app.security.SecurityService', 'de.infonautika.monomusiccorp.app.business.InvoiceDelivery', 'de.infonautika.monomusiccorp.app.domain.Invoice', 'de.infonautika.monomusiccorp.app.business.DummyInvoiceDeliveryImpl', 'de.infonautika.monomusiccorp.app.business.BusinessProcessImpl', 'de.infonautika.monomusiccorp.app.repository.OrderRepository', 'de.infonautika.monomusiccorp.app.repository.ShoppingBasketRepository', 'de.infonautika.monomusiccorp.app.repository.InvoiceRepository', 'de.infonautika.monomusiccorp.app.domain.ShoppingBasket', 'de.infonautika.monomusiccorp.app.domain.Order', 'de.infonautika.monomusiccorp.app.domain.HasPositions', 'de.infonautika.monomusiccorp.app.domain.HasPricedPositions', 'de.infonautika.monomusiccorp.app.business.StateSetup', 'de.infonautika.monomusiccorp.app.domain.ShoppingBasketTest'], ['de.infonautika.monomusiccorp.app.business.errors.ForbiddenException', 'de.infonautika.monomusiccorp.app.controller.OrdersController', 'de.infonautika.monomusiccorp.app.repository.PickingOrderRepository', 'de.infonautika.monomusiccorp.app.controller.resources.OrderStatusResource', 'de.infonautika.monomusiccorp.app.business.errors.DoesNotExistException', 'de.infonautika.monomusiccorp.app.domain.PickingOrder', 'de.infonautika.monomusiccorp.app.controller.resources.OrderStatusResourceAssembler', 'de.infonautika.monomusiccorp.app.controller.resources.PricedPositionResource', 'de.infonautika.monomusiccorp.app.controller.resources.PricedPositionResourceAssembler', 'de.infonautika.monomusiccorp.app.business.StockNotification', 'de.infonautika.monomusiccorp.app.business.DummyStockNotificationImpl', 'de.infonautika.monomusiccorp.app.business.errors.BusinessError', 'de.infonautika.monomusiccorp.app.controller.OrdersControllerTest'], ['de.infonautika.monomusiccorp.app.controller.utils.Results'], ['de.infonautika.monomusiccorp.app.controller.utils.links.FailedToLoadClassName'], ['de.infonautika.monomusiccorp.app.security.UserRole'], ['de.infonautika.monomusiccorp.app.persist.LocalDateTimeConverter'], ['de.infonautika.monomusiccorp.app.domain.Currencies'], ['de.infonautika.monomusiccorp.app.JsonTesterRule'], ['de.infonautika.monomusiccorp.app.DescribingMatcherBuilder', 'de.infonautika.monomusiccorp.app.BiDescribingMatcher', 'de.infonautika.monomusiccorp.app.BiDescribingMatcherBuilder'], ['de.infonautika.monomusiccorp.app.controller.ControllerConstants'], ['de.infonautika.monomusiccorp.app.controller.MatcherDebug']]";
        Map<String, Integer> stringIntegerMap = StringUtils.readClustersFromString(clusters);

        double irnClusters = metric.calculateCluster(stringIntegerMap);
        System.out.println("Total IRN Clusters: " + irnClusters);

    }
}


