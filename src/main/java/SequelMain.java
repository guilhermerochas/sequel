public class SequelMain {
    public static void main(String[] args) throws Exception {
        Sequelizer<ExampleModel> sequelizer = new Sequelizer<>(ExampleModel.class);
        ExampleModel model = new ExampleModel();
        model.setMyBytes(true);
        model.setId(12);
        model.setStringValue("hahah");
        model.setTest("ha");

        //sequelizer.create();
        //sequelizer.insert(model);

        /*sequelizer.delete(model);
        sequelizer.delete(model, IgnoreOnly.of("id", "test"));
        sequelizer.delete(model, UseOnly.of("id", "test"));*/

        //sequelizer.update(model);
        //sequelizer.update(model, UseOnly.of("test"));

        sequelizer.serializeToJson(model);
        ExampleModel modeloJson = sequelizer.fromJson(" { id: \"12\", myBytes: \"true\", \"stringValue\": \"hahah\", \"test\": \"ha\" } ");
        System.out.println(modeloJson.getId());
        System.out.println(modeloJson.getMyBytes());

        System.out.println("\n");

        sequelizer.serializeToXml(model);
        ExampleModel modeloXml = sequelizer.fromXml("<ExampleModel><id>12<id/><myBytes>true<myBytes/><stringValue>hahah<stringValue/><test>ha<test/><ExampleModel/>");
        System.out.println(modeloXml.getId());
        System.out.println(modeloXml.getStringValue());
    }
}
