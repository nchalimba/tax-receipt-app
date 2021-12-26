package io.cake.easy_taxfox.VisionApi;

import android.os.Parcel;
import android.os.Parcelable;

/***
 * This class is intended to hold the predictions of the receipt made by the google vision api
 */
public class ReceiptPrediction implements Parcelable {
    private String title;
    private Double sum;

    public ReceiptPrediction(String title, Double sum) {
        this.title = title;
        this.sum = sum;
    }

    protected ReceiptPrediction(Parcel in) {
        title = in.readString();
        if (in.readByte() == 0) {
            sum = null;
        } else {
            sum = in.readDouble();
        }
    }

    public static final Creator<ReceiptPrediction> CREATOR = new Creator<ReceiptPrediction>() {
        @Override
        public ReceiptPrediction createFromParcel(Parcel in) {
            return new ReceiptPrediction(in);
        }

        @Override
        public ReceiptPrediction[] newArray(int size) {
            return new ReceiptPrediction[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public Double getSum() {
        return sum;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        if (sum == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(sum);
        }
    }
}
