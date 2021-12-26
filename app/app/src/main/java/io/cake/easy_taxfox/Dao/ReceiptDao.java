package io.cake.easy_taxfox.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.cake.easy_taxfox.Entities.Receipt;

/**
 * This interface contains all database-query for the export entity.
 */
@Dao
public interface ReceiptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public long createReceipt(Receipt receipt);

    @Query("SELECT * FROM receipt WHERE receiptId = :id")
    public Receipt getReceiptById(int id);

    @Query("SELECT * FROM receipt WHERE businessYear = :businessYear")
    public Receipt[] getReceiptsByBusinessYear(int businessYear);

    @Update
    public void updateReceipt(Receipt receipt);

    @Delete
    public void deleteReceipt(Receipt receipt);
}
