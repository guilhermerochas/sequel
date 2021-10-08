import java.util.Date;

public class ExampleModel {
    @PrimaryKey
    private int id;
    private boolean myBytes;
    @Sized(15)
    private String stringValue;
    @Sized(2)
    private String test;

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

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
