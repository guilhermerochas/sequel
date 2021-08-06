public class SequelMain {
    public static void main(String[] args) {
        Sequelizer<ExampleModel> sequelizer = new Sequelizer<>(ExampleModel.class);
        sequelizer.createTable();
    }
}
