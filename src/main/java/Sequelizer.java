import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

    public void create() {
        try {
            StringBuilder builder = new StringBuilder("CREATE TABLE ")
                    .append(tableType.getName()).append("(");
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
            System.err.println("Erro: " + e.getMessage());
        }
    }

    public void insert(T entity) {
        try {
            StringBuilder builder = new StringBuilder("INSERT INTO ")
                    .append(tableType.getName()).append("(");

            for (FieldData data : fieldDataInfo) {
                builder.append(data.getFieldName()).append(", ");
            }

            builder.setLength(builder.length() - 2);
            builder.append(") VALUES (");

            for (FieldData data : fieldDataInfo) {
                String value;
                if (data.isPublic()) {
                    value = (String) tableType.getField(data.getFieldName()).get(entity);
                } else {
                    String capitalizedField = data.getFieldName().substring(0, 1).toUpperCase() + data.getFieldName().substring(1);
                    Method method = tableType.getMethod("get" + capitalizedField);
                    value = method.invoke(entity).toString();
                }
                builder.append(setStringWithQuotes(data, value));
            }

            builder.setLength(builder.length() - 2);
            builder.append(");");

            System.out.println(builder.toString());
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }

    public void delete(T entity) {
        this.delete(entity, null);
    }

    public void delete(T entity, UseType option) {
        try {
            ArrayList<FieldData> toBeParsed = new ArrayList<>(fieldDataInfo);
            if (option != null) {
                Iterator<FieldData> iterableData = fieldDataInfo.iterator();
                List<String> fields = option.getFields();
                AtomicReference<Boolean> isRequired = new AtomicReference<>(false);

                if (option instanceof RequireOnly) {
                    isRequired.set(true);
                } else {
                    if (!(option instanceof IgnoreOnly)) {
                        throw new Exception("");
                    }
                }

                toBeParsed.removeIf(field -> {
                    boolean contained = fields.contains(field.getFieldName());
                    return contained != isRequired.get();
                });
            }

            StringBuilder builder = new StringBuilder("DELETE FROM ")
                    .append(tableType.getName()).append(" WHERE ");

            for (FieldData data : toBeParsed) {
                builder.append(data.getFieldName()).append(" = ");

                String value;
                if (data.isPublic()) {
                    value = (String) tableType.getField(data.getFieldName()).get(entity);
                } else {
                    String captalizedField = data.getFieldName().substring(0, 1).toUpperCase() + data.getFieldName().substring(1);
                    Method method = tableType.getMethod("get" + captalizedField);
                    value = method.invoke(entity).toString();
                }

                builder.append(setStringWithQuotes(data, value));
                builder.setLength(builder.length() - 2);
                builder.append(" AND ");
            }

            builder.setLength(builder.length() - 5);
            System.out.println(builder);
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }

    private StringBuilder setStringWithQuotes(FieldData data, String value) throws Exception {
        StringBuilder builder = new StringBuilder();
        String sqlType = SqlUtils.getField(data.getReturnType());

        if (sqlType.equals("VARCHAR") || sqlType.equals("CHAR")) {
            builder.append("'").append(value).append("'").append(", ");
        } else {
            builder.append(value).append(", ");
        }

        return builder;
    }
}
