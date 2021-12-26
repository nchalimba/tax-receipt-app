package io.cake.easy_taxfox.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import io.cake.easy_taxfox.Entities.Export;

/**
 * This interface contains all database-query for the export entity.
 */
@Dao
public interface ExportDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public long createExport(Export export);
}
