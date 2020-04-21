import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import utils.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StringUtilsTest {

    @Test
    public void extractFromParameterNormalVariable() {
        List<String> extracted = StringUtils.extractVariableType("Visit");
        assertEquals(Collections.singletonList("Visit"), extracted);
    }

    @Test
    public void extractGenerics() {
        List<String> extracted = StringUtils.extractVariableType("Collection<T>");
        assertEquals(Collections.singletonList("T"), extracted);
    }

    @Test
    public void extractFromCollectionSingleElement() {
        List<String> extracted = StringUtils.extractVariableType("Collection<Visit>");
        assertEquals(Collections.singletonList("Visit"), extracted);
    }

    @Test
    public void extractArrayTypeFromString() {
        List<String> extracted = StringUtils.extractVariableType("Integer[][]");
        assertEquals(Collections.singletonList("Integer"), extracted);
    }

    @Test
    public void extractFromCollectionMultipleElements() {
        List<String> extracted = StringUtils.extractVariableType("Collection<String, Visit>");
        assertEquals(Arrays.asList("String", "Visit"), extracted);
    }

    @Test
    public void extractFromCollectionNestedElements() {
        List<String> extracted = StringUtils.extractVariableType("Map<String, Collection<Visit>>");
        assertEquals(Arrays.asList("String", "Visit"), extracted);
    }

    @Test
    public void extractFromCollectionMultipleNestedElements() {
        List<String> extracted = StringUtils.extractVariableType("Map<String, Map<ClassA, Map<ClassB, Collection<Collection<Visit>>");
        assertEquals(Arrays.asList("String", "ClassA", "ClassB", "Visit"), extracted);
    }
}