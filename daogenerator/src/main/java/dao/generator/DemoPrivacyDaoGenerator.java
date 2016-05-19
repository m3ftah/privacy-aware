package dao.generator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class DemoPrivacyDaoGenerator {
    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(1, "fr.rsommerard.privacyaware.dao");
        schema.enableKeepSectionsByDefault();

        createDataTable(schema);

        new DaoGenerator().generateAll(schema, "app/src/main/java");
    }

    private static void createDataTable(Schema schema) {
        Entity data = schema.addEntity("Data");

        data.implementsSerializable();

        data.addIdProperty();
        data.addStringProperty("identifier").index().unique().notNull();
        data.addStringProperty("content").notNull();
        data.addIntProperty("color");
    }
}
