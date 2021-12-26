package io.cake.easy_taxfox.Entities;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

/**
 * This class is necessary to map the many to many relationship between export and receitps entity.
 */
public class ExportWithReceipts {
    @Embedded
    public Export export;
    @Relation(
            parentColumn = "exportId",
            entityColumn = "receiptId",
            associateBy = @Junction(ReceiptExportCrossRef.class)
    )
    public List<Receipt> receipts;
}
