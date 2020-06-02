import com.github.javaparser.ast.CompilationUnit;
import extraction.ExtractIdentifiedClasses;
import graph.entities.MyClass;
import org.junit.jupiter.api.Test;
import parser.Parse;
import parser.Parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


import static org.junit.jupiter.api.Assertions.*;

public class ExtractIdentifiedClassesTest {

    @Test
    public void parseAndExtract() throws IOException {

        /*
            jpetstore - action
            spring-blog - controllers
            jforum - > action, view?
            agilefant -> action
            monomusiccorp -> controller
            flight-booking-system -> manager
            greenhouse -> controller
            jhipster-sample-app -> web.rest
            library-application -> controller
            library-management-spring -> controller
            online-banking -> controller
            opencms-core -> controller
            shopping -> servlet
            realworld -> api
         */
        String projectName = "tntconcept";
        String path = "/home/mbrito/git/thesis-web-applications/monoliths/" + projectName;

        Parser parser = new Parser();
        List<CompilationUnit> compilationUnits = parser.parseProject(Path.of(path));

        Parse parse = new Parse();
        Map<String, MyClass> parseResult = parse.extractClasses(compilationUnits);


        System.out.println(parseResult.keySet().toString());

        List<String> filters = Arrays.asList("(?i)manager");
        List<String> classes = ExtractIdentifiedClasses.extractFilterBased(new ArrayList<>(parseResult.keySet()), filters);
        classes.forEach(System.out::println);
    }

    @Test
    public void shouldExtractListOfControllers() {
        List<String> classes = Arrays.asList("com.controllers.Teste", "com.controllers.something.else.ClassA",
                "org.another.one.ClassControllers", "com.something.else", "com", "org.teste.Teste",
                "com.controllers.testeController", "com.Controllers.Class");
        List<String> patterns = Arrays.asList("(?i)controllers?");
        List<String> matchingClasses = ExtractIdentifiedClasses.extractFilterBased(classes, patterns);
        List<String> expectedMatches = Arrays.asList("com.controllers.Teste", "com.controllers.something.else.ClassA",
                "org.another.one.ClassControllers", "com.controllers.testeController", "com.Controllers.Class");
        assertEquals(matchingClasses, expectedMatches);
    }

}
