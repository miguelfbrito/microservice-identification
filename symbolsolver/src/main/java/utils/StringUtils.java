package utils;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static Set<String> extractCamelCaseLower(String string) {
        Set<String> extracted = new HashSet<>();
        for (String s : string.split("(?<=[a-z])(?=[A-Z])")) {
            extracted.add(s.toLowerCase());
        }
        return extracted;
    }
}
