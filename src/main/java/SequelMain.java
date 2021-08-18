public class SequelMain {
    public static void main(String[] args) {
        Sequelizer<ExampleModel> sequelizer = new Sequelizer<>(ExampleModel.class);
        ExampleModel model = new ExampleModel();
        model.setMyBytes(true);
        model.setId(12);
        model.setStringValue("hahah");
        model.test = "ha";

        //sequelizer.create();
        //sequelizer.insert(model);

        sequelizer.delete(model);
        sequelizer.delete(model, IgnoreOnly.of("id", "test"));
        sequelizer.delete(model, RequireOnly.of("id", "test"));
    }
}
