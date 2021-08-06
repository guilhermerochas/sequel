public class FieldData {
    private String fieldName;
    private boolean isPublic = true;
    private boolean isPrimaryKey = false;
    private String returnType;
    private int sizedValue = -1;

    public int getSizedValue() {
        return sizedValue;
    }

    public void setSizedValue(int sizedValue) {
        this.sizedValue = sizedValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public boolean getPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean key) {
        isPrimaryKey = key;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }


}
