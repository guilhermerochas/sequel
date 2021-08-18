import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RequireOnly implements UseType {
    private List<String> fields;

    private RequireOnly(List<String> fields) {
        this.fields = fields;
    }

    public static RequireOnly of(String... strings) {
        List<String> fields = Arrays.stream(strings).collect(Collectors.toList());
        return new RequireOnly(fields);
    }

    @Override
    public List<String> getFields() {
        return fields;
    }
}
