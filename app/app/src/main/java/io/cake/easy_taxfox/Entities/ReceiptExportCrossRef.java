package io.cake.easy_taxfox.Entities;

import androidx.room.Entity;

/**
 * This class is necessary to map the many to many relationship between exports and receitps entity.
 */
@Entity(primaryKeys = {"receiptId", "exportId"})
public class ReceiptExportCrossRef {

    public long receiptId;
    public long exportId;



}
