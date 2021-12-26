package io.cake.easy_taxfox.Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import io.cake.easy_taxfox.Config.AppConfig;
import io.cake.easy_taxfox.Dao.CustomerDao;
import io.cake.easy_taxfox.Dao.ExportDao;
import io.cake.easy_taxfox.Dao.ReceiptDao;
import io.cake.easy_taxfox.Entities.Converters;
import io.cake.easy_taxfox.Entities.Customer;
import io.cake.easy_taxfox.Entities.Export;
import io.cake.easy_taxfox.Entities.Receipt;

/**
 * This class contains the database-setup with the entity-classes and their matching daos.
 */

@Database(entities = {Customer.class, Export.class,
        Receipt.class}, version = AppConfig.DATABASE_VERSION, exportSchema = AppConfig.DATABASE_EXPORT_SCHEMA)
@TypeConverters({Converters.class})
public abstract class TaxFoxDatabase extends RoomDatabase {
    public abstract CustomerDao customerDao();
    public abstract ExportDao exportDao();
    public abstract ReceiptDao receiptDao();
}
