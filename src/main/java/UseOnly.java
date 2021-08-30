import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UseOnly implements UseType {
    private List<String> fields;

    private UseOnly(List<String> fields) {
        this.fields = fields;
    }

    public static UseOnly of(String... strings) {
        List<String> fields = Arrays.stream(strings).collect(Collectors.toList());
        return new UseOnly(fields);
    }

    @Override
    public List<String> getFields() {
        return fields;
    }
}
