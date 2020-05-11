package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class FileUtils {

    @Test
    public static <T> void jsonDump(Object data) throws IOException {
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
