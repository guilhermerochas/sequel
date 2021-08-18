import java.util.Date;

public class ExampleModel {
    @PrimaryKey
    private int id;
    private boolean myBytes;
    @Sized(15)
    private String stringValue;
    @Sized(2)
    public String test;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean getMyBytes() {
        return myBytes;
    }

    public void setMyBytes(boolean myBytes) {
        this.myBytes = myBytes;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }
}
