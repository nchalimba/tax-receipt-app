package io.cake.easy_taxfox.Entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * This class is the representation of the receipt entity.
 */
@Entity
public class Receipt {

    @PrimaryKey(autoGenerate = true)
    private int receiptId;
    private int _customerId;
    private String filePath;
    private String fileName;
    private Date receiptDate;
    private Date timeStamp;
    private String annex;
    private String section;
    private int businessYear;
    private String title;
    private Double amount;



    public Receipt() {
    }

    @Ignore
    public Receipt(String filePath, String fileName, Date receiptDate, Date timeStamp, String annex, String section, int businessYear) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.receiptDate = receiptDate;
        this.timeStamp = timeStamp;
        this.annex = annex;
        this.section = section;
        this.businessYear = businessYear;
    }

    public int get_customerId() {
        return _customerId;
    }

    public void set_customerId(int _customerId) {
        this._customerId = _customerId;
    }

    public int getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(int receiptId) {
        this.receiptId = receiptId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(Date receiptDate) {
        this.receiptDate = receiptDate;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getAnnex() {
        return annex;
    }

    public void setAnnex(String annex) {
        this.annex = annex;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public int getBusinessYear() {
        return businessYear;
    }

    public void setBusinessYear(int businessYear) {
        this.businessYear = businessYear;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

}
