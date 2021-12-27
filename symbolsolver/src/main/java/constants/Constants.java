package constants;

import java.io.*;
import java.util.*;

public class Constants {

    public static final String DIRECTORY;
    public static final String PROJECT_PATH;
    public static final String PROJECT_NAME;

    static {
        DIRECTORY = getRootPath();
        PROJECT_PATH = getProjectPath();
        PROJECT_NAME = getProjectName();
    }

    private static String getProjectName() {
        String[] split = PROJECT_PATH.split("/");
        System.out.println("Project name: " + split[split.length - 1]);
        return split[split.length - 1];
    }

    public static final Set<String> STOP_WORDS_DATA_TYPES = new HashSet<>(Arrays.asList()); // "int", "integer", "void", "long", "double", "float", "string", "char", "character"

    public static final Set<String> STOP_WORDS = readStopWords();

    public static String getProjectPath() {
        String projectPath = System.getProperty("project");
        if (projectPath != null) {
            return projectPath;
        }

        return "";
        // throw new IllegalArgumentException("Project path not found.");
    }

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
