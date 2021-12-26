package io.cake.easy_taxfox.Entities;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

/**
 * This class is necessary to map the many to many relationship between receipt and export entity.
 */
public class ReceiptWithExports {

    @Embedded
    public Receipt receipt;
    @Relation(
            parentColumn = "receiptId",
            entityColumn = "exportId",
            associateBy = @Junction(ReceiptExportCrossRef.class)
    )
    public List<Export> exports;
}
