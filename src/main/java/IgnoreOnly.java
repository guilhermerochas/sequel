import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IgnoreOnly implements UseType {
    private List<String> fields;

    private IgnoreOnly(List<String> fields) {
        this.fields = fields;
    }

    public static IgnoreOnly of(String... strings) {
        List<String> fields = Arrays.stream(strings).collect(Collectors.toList());
        return new IgnoreOnly(fields);
    }

    @Override
    public List<String> getFields() {
        return fields;
    }
}
