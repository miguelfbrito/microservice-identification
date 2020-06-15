package extraction;

import graph.entities.MyClass;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExtractIdentifiedClasses {

    public List<String> extractFilterBased(List<String> classes, List<String> filterPatterns) {
        List<String> matchingClasses = new ArrayList<>();
        List<Pattern> patternList = filterPatterns.stream().map(Pattern::compile).collect(Collectors.toList());

        for (String className : classes) {
            if (doesMatch(className, patternList)) {
                matchingClasses.add(className);
            }
        }

        return matchingClasses;
    }

    private boolean doesMatch(String className, List<Pattern> filterPatterns) {
        for (Pattern p : filterPatterns) {
            Matcher m = p.matcher(className);
            if (m.find()) {
                return true;
            }
        }
        return false;
    }
}
