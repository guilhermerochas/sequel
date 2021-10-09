import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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

                if (option instanceof UseOnly) {
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

    public void update(T entity) {
        this.update(entity, null);
    }

    public void update(T entity, UseType option) {
        try {
            StringBuilder builder = new StringBuilder("UPDATE ").append(tableType.getName()).append(" SET ");

            if (entity == null) {
                throw new Exception("Objeto não pode ser nulo");
            }

            if (option == null) {
                boolean hasPK = fieldDataInfo.stream().anyMatch(FieldData::getPrimaryKey);
                if (!hasPK) {
                    throw new Exception("O objeto passado não possui uma chave primaria, considere usar `UseOnly` e especifique o campo");
                }
            } else {
                if (!(option instanceof UseOnly)) {
                    throw new Exception("Considere usar `UseOnly` quando passar os valores para a instancia");
                }
            }

            StringBuilder whereBuilder = new StringBuilder(" WHERE ");
            boolean firstOcurrance = false;

            for (FieldData data : fieldDataInfo) {
                StringBuilder localBuilder = new StringBuilder(data.getFieldName()).append(" = ");
                boolean contained;

                if (option == null) {
                    contained = data.getPrimaryKey();
                } else {
                    contained = option.getFields().contains(data.getFieldName());
                }

                String value;
                if (data.isPublic()) {
                    value = (String) tableType.getField(data.getFieldName()).get(entity);
                } else {
                    String captalizedField = data.getFieldName().substring(0, 1).toUpperCase() + data.getFieldName().substring(1);
                    Method method = tableType.getMethod("get" + captalizedField);
                    value = method.invoke(entity).toString();
                }

                localBuilder.append(setStringWithQuotes(data, value));

                if (!contained) {
                    builder.append(localBuilder.toString().trim()).append(" ");
                } else {
                    localBuilder.append(" AND ");
                    whereBuilder.append(localBuilder.toString().trim());
                }
            }

            builder.setLength(builder.length() - 2);
            builder.append(" ").append(whereBuilder.toString().trim());
            builder.setLength(builder.length() - 6);
            System.out.println(builder.toString());
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

    public void serializeToJson(T model) throws Exception {
        StringBuilder builder = new StringBuilder(" { ");

        for (FieldData data : fieldDataInfo) {
            if(data.getReturnType().equals("java.lang.String")) {
                builder.append("\"").append(data.getFieldName()).append("\"").append(":");
            } else {
                builder.append(data.getFieldName()).append(":");
            }

            String value;
            if (data.isPublic()) {
                value = (String) tableType.getField(data.getFieldName()).get(model);
            } else {
                String captalizedField = data.getFieldName().substring(0, 1).toUpperCase() + data.getFieldName().substring(1);
                Method method = tableType.getMethod("get" + captalizedField);
                value = method.invoke(model).toString();
            }

            builder.append(" \"").append(value).append("\"").append(", ");
        }

        builder.setLength(builder.length() - 2);

        builder.append(" } ");

        System.out.println(builder);
    }

    public void serializeToXml(T model) throws Exception {
        StringBuilder builder = new StringBuilder("<");
        builder.append(model.getClass().getName()).append(">");

        for (FieldData data : fieldDataInfo) {
            builder.append("<").append(data.getFieldName()).append(">");

            String value;
            if (data.isPublic()) {
                value = (String) tableType.getField(data.getFieldName()).get(model);
            } else {
                String captalizedField = data.getFieldName().substring(0, 1).toUpperCase() + data.getFieldName().substring(1);
                Method method = tableType.getMethod("get" + captalizedField);
                value = method.invoke(model).toString();
            }

            builder.append(value);

            builder.append("<").append(data.getFieldName()).append("/>");
        }

        builder.append("<").append(model.getClass().getName()).append("/>");

        System.out.println(builder);
    }

    public T fromJson(String fileContent) throws Exception {
        Constructor<T> constructor = tableType.getConstructor();
        T instance = constructor.newInstance();

        fileContent = fileContent.replace("{", "");
        fileContent = fileContent.replace("}", "");
        String[] keyValueSet = fileContent.split(",");

        Map<String, String> hashSet = new HashMap<>();

        for (String keyValue : keyValueSet) {
            String[] resuls = keyValue.split(":");
            hashSet.put(resuls[0].replace("\"", "").trim(), resuls[1].replace("\"", "").trim());
        }

        for (Method method : Arrays.stream(instance.getClass().getDeclaredMethods()).filter(meth ->
                meth.getName().startsWith("set")).collect(Collectors.toList())) {
            String methodName = method.getName().replace("set", "");
            methodName = String.valueOf(methodName.charAt(0)).toLowerCase() + methodName.substring(1);
            String value = hashSet.get(methodName);

            Class returnType = tableType.getMethod(method.getName().replace("set", "get")).getReturnType();

            switch (returnType.getSimpleName().toLowerCase()) {
                case "string":
                    method.invoke(instance, value);
                    break;
                case "int":
                case "integer":
                    int valueInt = Integer.parseInt(value);
                    method.invoke(instance, valueInt);
                    break;
                case "boolean":
                    Boolean valueBool = Boolean.parseBoolean(value);
                    method.invoke(instance, valueBool);
                    break;
                default:
                    System.out.println("value not able to invoke");
                    break;
            }
        }

        return instance;
    }

    public T fromXml(String fileContent) throws Exception {
        Constructor<T> constructor = tableType.getConstructor();
        T instance = constructor.newInstance();

        fileContent = fileContent.replace("<" + instance.getClass().getSimpleName() + ">", "");
        fileContent = fileContent.replace("<" + instance.getClass().getSimpleName() + "/>", "");

        fileContent = fileContent.replace("<", " ");
        fileContent = fileContent.replace("/>", ",");
        fileContent = fileContent.replace(">", ":");

        Map<String, String> hashSet = new HashMap<>();
        String[] keyValueSet = fileContent.split(",");

        for (String keyValue : keyValueSet) {
            String[] resuls = keyValue.split(":");
            hashSet.put(resuls[0].trim(), resuls[1].split(" ")[0].trim());
        }

        for (Method method : Arrays.stream(instance.getClass().getDeclaredMethods()).filter(meth ->
                meth.getName().startsWith("set")).collect(Collectors.toList())) {
            String methodName = method.getName().replace("set", "");
            methodName = String.valueOf(methodName.charAt(0)).toLowerCase() + methodName.substring(1);
            String value = hashSet.get(methodName);

            Class returnType = tableType.getMethod(method.getName().replace("set", "get")).getReturnType();

            switch (returnType.getSimpleName().toLowerCase()) {
                case "string":
                    method.invoke(instance, value);
                    break;
                case "int":
                case "integer":
                    int valueInt = Integer.parseInt(value);
                    method.invoke(instance, valueInt);
                    break;
                case "boolean":
                    Boolean valueBool = Boolean.parseBoolean(value);
                    method.invoke(instance, valueBool);
                    break;
                default:
                    System.out.println("value not able to invoke");
                    break;
            }
        }

        return instance;
    }
}
