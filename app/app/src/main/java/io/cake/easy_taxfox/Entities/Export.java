package io.cake.easy_taxfox.Entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * This class is the representation of the export entity.
 */
@Entity
public class Export {

    @PrimaryKey(autoGenerate = true)
    private int exportId;
    private int _customerId;
    private String filePath;
    private String fileName;
    private Date timeStamp;

    public Export(){

    }

    @Ignore
    public Export(String filePath, String fileName, Date timeStamp) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.timeStamp = timeStamp;
    }

    public int getExportId() {
        return exportId;
    }

    public void setExportId(int exportId) {
        this.exportId = exportId;
    }

    public int get_customerId() {
        return _customerId;
    }

    public void set_customerId(int _customerId) {
        this._customerId = _customerId;
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

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}
