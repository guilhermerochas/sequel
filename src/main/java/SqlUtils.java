import java.util.HashMap;

public class SqlUtils {
    private static final HashMap<String, String> columnFieldHashMap;

    static {
        columnFieldHashMap = new HashMap<String, String>() {{
            put("java.lang.String", "VARCHAR");
            put("int", "INT");
            put("java.lang.Integer", "INT");
            put("java.util.Date", "DATETIME");
            put("boolean", "BIT");
            put("Boolean", "BIT");
        }};
    }

    public static String getField(String propertyType) throws Exception {
        String field = columnFieldHashMap.get(propertyType);
        if (field == null) {
            throw new Exception("not found field with name: " + propertyType);
        }

        return field;
    }
}
