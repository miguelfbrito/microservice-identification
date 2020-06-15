package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import constants.Constants;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class FileUtils {

    @Test
    public static <T> void jsonDump(Object data) throws IOException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        FileWriter file = null;
        try {
            file = new FileWriter(Constants.DIRECTORY + "/data/output.json");
            gson.toJson(data, file);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                file.flush();
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeToFile(List<String> lines, String path, boolean append) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path, append));
        for (String line : lines) {
            writer.write(line);
            writer.newLine();
        }
        writer.close();

    }
}
