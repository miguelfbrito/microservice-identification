package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static Map<String, Integer> readClustersFromString(String string){
        String pattern = "'([\\w\\.]*)'";
        String[] stringSplit = string.split("\\]");

        int group = 0;
        Map<String, Integer> clusters = new HashMap<>();
        Pattern  p = Pattern.compile(pattern);

        for(String s : stringSplit){
            Matcher m = p.matcher(s);
            while(m.find()){
                clusters.put(m.group(1), group);
            }
            group++;
        }
        return clusters;
    }
}
