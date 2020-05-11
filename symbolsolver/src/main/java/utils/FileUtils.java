package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import graph.entities.MyClass;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtils {

    @Test
    public static <T> void jsonDump(List<T> data) throws IOException {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        FileWriter file = null;
        try {
            file = new FileWriter("./output.json");
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

}
