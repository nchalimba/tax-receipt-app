package io.cake.easy_taxfox.Entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

/**
 * This class is necessary to map the one to many relationship between customer and receitps entity.
 */
public class CustomerWithReceipts {

    @Embedded
    public Customer customer;
    @Relation(
            parentColumn = "customerId",
            entityColumn = "_customerId"
    )
    public List<Receipt> receipts;
}
