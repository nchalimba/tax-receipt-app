package io.cake.easy_taxfox.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.cake.easy_taxfox.Entities.Customer;

/**
 * This interface contains all database-query for the customer entity.
 */
@Dao
public interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public void createCustomer(Customer customer);

    @Update
    public void updateCustomer(Customer customer);

    @Query("SELECT * FROM customer WHERE customerid = :id")
    public Customer getCustomerById(int id);

}
