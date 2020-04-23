import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import utils.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StringUtilsTest {

    @Test
    public void shouldExtractSingleVariable() {
        List<String> extracted = StringUtils.extractVariableType("Visit");
        assertEquals(Collections.singletonList("visit"), extracted);
    }

    @Test
    public void shouldExtractGeneric() {
        List<String> extracted = StringUtils.extractVariableType("Collection<T>");
        assertEquals(Collections.singletonList("t"), extracted);
    }

    @Test
    public void shouldExtractSingleVariableFromCollection() {
        List<String> extracted = StringUtils.extractVariableType("Collection<Visit>");
        assertEquals(Collections.singletonList("visit"), extracted);
    }

    @Test
    public void shouldExtractArrayType() {
        List<String> extracted = StringUtils.extractVariableType("Integer[][]");
        assertEquals(Collections.singletonList("integer"), extracted);
    }

    @Test
    public void shouldExtractMultipleVariablesFromCollection() {
        List<String> extracted = StringUtils.extractVariableType("Map<String, Visit>");
        assertEquals(Arrays.asList("string", "visit"), extracted);
    }

    @Test
    public void shouldExtractMultipleVariablesFromNestedCollections() {
        List<String> extracted = StringUtils.extractVariableType("Map<String, Collection<Visit>>");
        assertEquals(Arrays.asList("string", "visit"), extracted);
    }

    @Test
    public void shouldExtractMultipleVariablesFromMultipleLevelsOfNestedCollections() {
        List<String> extracted = StringUtils.extractVariableType("Map<String, Map<ClassA, Map<ClassB, Collection<Collection<Visit>>");
        assertEquals(Arrays.asList("string", "classa", "classb", "visit"), extracted);
    }
}
