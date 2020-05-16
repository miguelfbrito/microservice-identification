package graph.entities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Constants {

    public static final Set<String> STOP_WORDS_DATA_TYPES = new HashSet<>(
            Arrays.asList("int", "integer", "void", "long", "double", "float", "string", "char", "character"));

    public static final Set<String> STOP_WORDS_METHODS_NAMES = new HashSet<>(
            Arrays.asList("set", "get", "add", "create", "remove"));

}
