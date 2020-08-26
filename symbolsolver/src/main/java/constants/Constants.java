package constants;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Constants {
    public static final String DIRECTORY = getRootPath();
    // public static final String MONOLITHS_DIRECTORY =  "/home/mbrito/git/thesis-web-applications/monoliths";

    public static final Set<String> STOP_WORDS_DATA_TYPES = new HashSet<>(Arrays.asList()); // "int", "integer", "void", "long", "double", "float", "string", "char", "character"

/*
    public static final Set<String> STOP_WORDS_METHODS = new HashSet<>(
            Arrays.asList("set", "add", "get", "index", "archive", "update", "remove", "edit",
                    "delete", "show", "save", "create", "view", "list", "new", "clear", "list", "insert", "form", "signon", "signoff", "from", "to"));
*/

    public static final Set<String> STOP_WORDS = readStopWords();

    public static String getRootPath() {
        List<String> split = new ArrayList<>(Arrays.asList(System.getProperty("user.dir").toString().split("/")));
        split.remove(0);
        split.remove(split.size() - 1);

        return split.stream().reduce("", (acc, string) -> acc + "/" + string).replaceAll("/symbolsolver", "");
    }

    public static Set<String> readStopWords() {
        String stopWordsPath = System.getProperty("stop_words");

        if (stopWordsPath == null) {
            stopWordsPath = DIRECTORY + "/stop_words.txt";
        }

        String lines = "";
        try (BufferedReader br = new BufferedReader(new FileReader(stopWordsPath))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            lines = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        lines = lines.replaceAll("\n", " ");
        Set<String> uniqueStopWords = new HashSet<>(Arrays.asList(lines.split(" ")));
        uniqueStopWords.remove("");

        return uniqueStopWords;
    }


}
