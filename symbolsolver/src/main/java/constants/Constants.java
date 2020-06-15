package constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Constants {

    public static final String DIRECTORY = "/home/mbrito/git/thesis";
    public static final String MONOLITHS_DIRECTORY =  "/home/mbrito/git/thesis-web-applications/monoliths";

    public static final Set<String> STOP_WORDS_DATA_TYPES = new HashSet<>(
            Arrays.asList()); // "int", "integer", "void", "long", "double", "float", "string", "char", "character"

/*
    public static final Set<String> STOP_WORDS_METHODS = new HashSet<>(
            Arrays.asList("set", "add", "get", "index", "archive", "update", "remove", "edit",
                    "delete", "show", "save", "create", "view", "list", "new", "clear", "list", "insert", "form", "signon", "signoff", "from", "to"));
*/

    public static final Set<String> STOP_WORDS = new HashSet<>(
            Arrays.asList("jpetstore", "jforum", "xwiki", "roller", "agilefant", "blog", "raysmond",
                    "b3log", "solo", "fi", "hut", "soberit", "agilefant", "servlets", "javax",
                    "java", "net", "org", "util", "lang", "apache", "roller", "weblogger", "int",
                    "math", "string", "int", "void", "date", "object", "list",
                    "get", "set", "is", "be", "decimal", "boolean", "action", "service", "bean",
                    "service", "repository", "controller", "data", "dto", "util", "id", "processor",
                    "solo", "service", "repository", "process", "controller", "data", "date", "dto",
                    "util", "id", "processor", "impl", "cache", "mgmt", "query", "console",
                    "service", "hsqldb", "type", "dao", "acces", "default", "generic", "common", "action",
                    "repository", "process", "control", "controller", "data", "date", "dto", "util", "id",
                    "processor", "impl", "cache", "mgmt", "query", "console", "comparator",
                    "exception", "provider", "impl", "bean", "edit", "action", "interceptor", "factory",
                    "util", "data", "servlet", "view", "base", "management", "request", "cache", "manage",
                    "manager", "manag", "pager", "pag", "model", "service", "wrapper", "wrapp", "weblog",
                    "comparator", "accessor", "task", "jpa", "abstract", "action", "container", "interceptor", "business",
                    "impl", "history", "load", "filter", "hierarchy", "dao", "hibernate", "entry", "generator",
                    "agilefant", "to", "type", "data", "node", "metric", "handle", "manager", "manage",
                    "default", "service", "config", "filter", "filt", "listen", "render", "abstract", "typ", "string",
                    "request", "resource", "response", "object", "factory", "access", "model", "action", "abstract",
                    "customiz", "generator", "load", "build", "listen", "descriptor", "script", "repository", "action",
                    "cache", "type", "resolve", "convert", "and", "provid", "of", "in", "list", "from", "impl", "check",
                    "serializer", "serialize", "xwiki", "wiki", "context", "reference", "translation", "configuration",
                    "annotation", "bridge", "new", "clear", "my", "signon", "signoff", "clear")
    );
}
