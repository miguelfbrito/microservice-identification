package utils;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtils {

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
                strings.add(m.group(0).toLowerCase());
            }
        }

        // Set<String> stopTypes = new HashSet<>(Arrays.asList("integer", "int", "long", "string", "void", "object"));

        return strings; //.stream().filter(s -> !stopTypes.contains(s)).collect(Collectors.toList());

    }

    public static Set<String> extractCamelCaseLower(String string) {
        Set<String> extracted = new HashSet<>();
        for (String s : string.split("(?<=[a-z])(?=[A-Z])")) {
            extracted.add(s.toLowerCase());
        }
        return extracted;
    }
}
