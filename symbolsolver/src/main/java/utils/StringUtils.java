package utils;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.nodeTypes.*;
import com.google.common.base.Strings;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import graph.entities.MyClass;
import graph.entities.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtils {


    private static StanfordCoreNLP pipeline;

    static {
        Properties props = new Properties();
        //props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(props);
    }

    public static List<String> filterAndCleanText(String text, Set<String> stopWords) {
        return StringUtils.filterStopWords(StringUtils.lemmatize(String.join(" ", StringUtils.extractCamelCaseLower(text))), stopWords);
    }

    public static List<String> lemmatize(String text) {
        CoreDocument cd = new CoreDocument(String.join(" ", text));
        pipeline.annotate(cd);

        return cd.tokens().stream()
                .map(CoreLabel::lemma)
                .collect(Collectors.toList());
    }

    public static boolean isMethodCallGetterOrSetter(String name, int parametersSize) {
        Matcher matchGet = Pattern.compile("^get").matcher(name);
        Matcher matchSet = Pattern.compile("^set").matcher(name);
        Matcher matchBoolean = Pattern.compile("^is").matcher(name);
        return (matchGet.find() && parametersSize == 0) || (matchSet.find() && parametersSize == 1) || (matchBoolean.find() && parametersSize == 0);
    }

    public static List<String> filterStopWords(List<String> words, Set<String> stopWords){
        return words.stream().filter(word -> !stopWords.contains(word)).collect(Collectors.toList());
    }

    public static Map<String, Integer> readClustersFromString(String string) {
        String pattern = "'([\\w\\.]*)'";
        String[] stringSplit = string.split("\\]");

        int group = 0;
        Map<String, Integer> clusters = new HashMap<>();
        Pattern p = Pattern.compile(pattern);

        for (String s : stringSplit) {
            Matcher m = p.matcher(s);
            while (m.find()) {
                clusters.put(m.group(1), group);
            }
            group++;
        }
        return clusters;
    }


    public static Map<Integer, Service> extractClustersToServices(String string) {

        Map<String, Integer> clustersFromString = readClustersFromString(string);
        Map<Integer, Service> clusters = new HashMap<>();

        for (Map.Entry<String, Integer> entry : clustersFromString.entrySet()) {

            // TODO : clean this up
            if (clusters.containsKey(entry.getValue())) {
                clusters.get(entry.getValue()).getClasses().put(entry.getKey(),
                        new MyClass(entry.getKey(), clusters.get(entry.getValue())));
            } else {
                Service service = new Service(entry.getValue());
                service.getClasses().put(entry.getKey(), new MyClass(entry.getKey(), service));
                clusters.put(service.getId(), service);
            }

        }

        return clusters;
    }

    public static Set<String> extractCamelCase(String string) {
        return new HashSet<>(Arrays.asList(string.split("(?<=[a-z])(?=[A-Z])")));
    }

    public static List<String> extractVariableType(String string) {

        List<String> strings = new ArrayList<>();
        String pattern = "(\\b\\w*\\b)(?!\\<)";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(string);

        while (m.find()) {
            if (!m.group(0).equals("")) {
                // TODO : REVIEW, previously used as toLowerCase() for metrics but it interfers currently
                // we need the camel case version to export the parsed information
                strings.add(m.group(0));
            }
        }

        // Set<String> stopTypes = new HashSet<>(Arrays.asList("integer", "int", "long", "string", "void", "object"));

        return strings; //.stream().filter(s -> !stopTypes.contains(s)).collect(Collectors.toList());

    }

    public static List<String> extractCamelCaseLower(String string) {
        List<String> extracted = new ArrayList<>();
        for (String s : string.split("(?<=[a-z])(?=[A-Z])")) {
            extracted.add(s.toLowerCase());
        }
        return extracted;
    }
}
