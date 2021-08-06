import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class Sequelizer<T> {
    private Class<T> tableType;
    private ArrayList<FieldData> fieldDataInfo;

    public Sequelizer(Class<T> tableType) {
        if (tableType != null) {
            this.tableType = tableType;
            this.fieldDataInfo = new ArrayList<>();
            this.validateClass();
            return;
        }
        System.err.println("tableType was null...");
        System.exit(1);
    }

    private void validateClass() {
        try {
            int fieldModifier;
            FieldData fieldData;

            for (Field field : tableType.getDeclaredFields()) {
                fieldData = new FieldData();
                fieldData.setFieldName(field.getName());
                fieldData.setReturnType(field.getType().getTypeName());

                fieldModifier = field.getModifiers();
                String propertyName = field.getName().toLowerCase();

                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    fieldData.setPrimaryKey(true);
                }

                if (field.getType().getName().equals("java.lang.String")) {
                    if (field.isAnnotationPresent(Sized.class)) {
                        Sized sized = field.getAnnotation(Sized.class);
                        fieldData.setSizedValue(sized.value());
                    }
                }

                if (Modifier.isProtected(fieldModifier) || Modifier.isPrivate(fieldModifier)) {
                    fieldData.setPublic(false);

                    List<Method> methods = Arrays.stream(tableType.getMethods()).collect(Collectors.toList());

                    Optional<Method> hasGetter = methods.stream().filter(meth ->
                            meth.getName().toLowerCase().equals("get" + propertyName)).findFirst();
                    Optional<Method> hasSetter = methods.stream().filter(meth ->
                            meth.getName().toLowerCase().equals("set" + propertyName)).findFirst();

                    if (!hasGetter.isPresent() || !hasSetter.isPresent()) {
                        throw new Exception("Property " + propertyName + " doesn't have getter and setter associated to it!");
                    }

                    if (hasGetter.get().getReturnType() != field.getType()) {
                        throw new Exception("Getter associated with " + propertyName + " doesn't have a matching return type");
                    }
                }

                fieldDataInfo.add(fieldData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void createTable() {
        try {
            StringBuilder builder = new StringBuilder("CREATE TABLE ");
            builder.append(tableType.getName()).append("(");
            for (FieldData data : fieldDataInfo) {
                String sqlType = SqlUtils.getField(data.getReturnType());
                if (sqlType.equals("VARCHAR")) {
                    sqlType += data.getSizedValue() == -1 ? "(MAX)" : "(" + data.getSizedValue() + ")";
                }

                builder.append(data.getFieldName()).append(" ").append(sqlType)
                        .append(data.getPrimaryKey() ? " PRIMARY KEY" : "");
                builder.append(", ");
            }
            builder.setLength(builder.length() - 2);
            builder.append(");");

            System.out.println(builder.toString());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
