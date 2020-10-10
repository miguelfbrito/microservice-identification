package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import constants.Constants;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class FileUtils {

    public static <T> void jsonDump(Object data, String filePath) throws IOException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        FileWriter fileWriter = null;
        File file = new File(filePath);
        System.out.println("File: " + filePath);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fileWriter = new FileWriter(file);
            gson.toJson(data, fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
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
