import java.util.Date;

public class ExampleModel {
    @PrimaryKey
    private int Id;
    private boolean bytes;
    @Sized(15)
    private String stringValue;
    private Integer intValue;
    public Date date;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    public boolean getBytes() {
        return bytes;
    }

    public void setBytes(boolean bytes) {
        this.bytes = bytes;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }
}
