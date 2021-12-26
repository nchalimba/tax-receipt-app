package io.cake.easy_taxfox.Others;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import io.cake.easy_taxfox.Config.AppConfig;
import io.cake.easy_taxfox.Entities.Receipt;
import io.cake.easy_taxfox.R;

/***
 * This class provides the adapter for the listview of receipts in the overview activity
 */
public class ReceiptListAdapter extends ArrayAdapter <Receipt> {

    private Context context;
    private ArrayList<Receipt> receiptArrayList;


    public ReceiptListAdapter(Context context, ArrayList <Receipt> receiptArrayList) {
        super(context, R.layout.activity_overview_list_item, receiptArrayList);
        this.context = context;
        this.receiptArrayList = receiptArrayList;
    }

    /**
     * This method prepares the adapter for the overview activity
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.activity_overview_list_item, null);
        }

        Receipt receipt = receiptArrayList.get(position);
        TextView tvReceiptTitle = v.findViewById(R.id.overviewReceiptTitle);
        TextView tvReceiptDate = v.findViewById(R.id.overviewReceiptDate);
        TextView tvReceiptAmount = v.findViewById(R.id.overviewReceiptAmount);
        if (receipt != null && receipt.getTitle() != null && receipt.getReceiptDate() != null && receipt.getAmount() != null) {
            tvReceiptTitle.setText(receipt.getTitle());
            tvReceiptDate.setText(AppConfig.DATE_FORMAT.format(receipt.getReceiptDate()));
            tvReceiptAmount.setText(AppConfig.formatMoney(receipt.getAmount()));
        }else{
            tvReceiptTitle.setText(context.getResources().getString(R.string.overviewEmptyReceiptTitle));
            tvReceiptDate.setText(context.getResources().getString(R.string.overviewEmptyReceiptDate));
            tvReceiptAmount.setText(context.getResources().getString(R.string.overviewEmptyReceiptAmount));
        }
        return v;
    }

}
