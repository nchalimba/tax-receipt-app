package io.cake.easy_taxfox.Helpers;

import android.app.Activity;
import android.graphics.Bitmap;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.AreaBreakType;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.rendering.PDFRenderer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import io.cake.easy_taxfox.Config.AppConfig;
import io.cake.easy_taxfox.Config.Log;
import io.cake.easy_taxfox.Database.TaxFoxDatabaseHelper;
import io.cake.easy_taxfox.Entities.Export;
import io.cake.easy_taxfox.Entities.Receipt;
import io.cake.easy_taxfox.R;
import io.cake.easy_taxfox.Tasks.CreateExportTask;
import io.cake.easy_taxfox.Tasks.CreatePdfExportSingleTask;
import io.cake.easy_taxfox.Tasks.CreatePdfExportSummaryTask;

/**
* This class provides all the methods for creating PDF-Files.
* */
public class PdfHelper {

    private TaxFoxDatabaseHelper dbHelper;
    private PdfExportResultListener listener;
    private Activity context;
    private ArrayList<Export> exportPDFList;

    public PdfHelper (Activity context, PdfExportResultListener listener) {
        this.context = context;
        this.listener = listener;
        exportPDFList = new ArrayList<>();
        initDbHelper(context);
    }

    /**
     * This method initializes the databasehelper.
     * @param context
     */
    private void initDbHelper(Activity context){
        dbHelper = TaxFoxDatabaseHelper.getInstance(context);
    }

    /**
     * This method triggers an runnable Task for creating one summarizing PDF-File.
     * @param receipts
     */
    public void createExportSummaryTask(List<Receipt> receipts){
        CreatePdfExportSummaryTask task = new CreatePdfExportSummaryTask(receipts, PdfHelper.this, context, listener);
        Executors.newSingleThreadExecutor().submit(task);
    }

    /**
     * This method triggers an runnable Task for creating a PDF-File for each element in the list.
     * @param receipts
     */
    public void createExportSingleTask(List<Receipt> receipts){
        CreatePdfExportSingleTask task = new CreatePdfExportSingleTask(receipts, PdfHelper.this, context, listener);
        Executors.newSingleThreadExecutor().submit(task);
    }

    /**
     * This method clears the exportPDFList. This is necessary for creating multiple Exports.
     */
    public void removeCachedExports(){
        exportPDFList.clear();
    }

    /**
     * This method creates for every receipt in the list one table with the matching receipt data.
     * For every receipt in the receiptslist, one tableline will be created.
     * @throws IOException
     */
    public void createExportSummary(List<Receipt> receipts, CreateExportTask.CreateExportResultListener listener) throws IOException{
        if(!ScannerHelper.initDirectory()){
            UIHelper.showToast(context, context.getResources().getString(R.string.scanNoDirectoryCreated));
        }
        Date timeStamp = new Date();
        String fileName = createFileName(timeStamp);
        File tableWithReceiptInfo = new File(AppConfig.FOLDER_PATH, fileName);
        Document document = initDocument(tableWithReceiptInfo);
        Table table = new Table(AppConfig.EXPORT_TABLE_COLUMN_WIDTH);
        addTableHeader(table);
        for (Receipt element: receipts) {
            addTableReceiptRow(table, element);
        }
        document.add(table);
        for (Receipt element: receipts) {
            addReceiptPage(document, element);
        }
        document.close();
        Export export = new Export(AppConfig.FOLDER_PATH, fileName, timeStamp);
        this.exportPDFList.add(export);
        dbHelper.createExport(export, listener);
    }



    /**
     * This method defines the header of the table
     * @param table
     */
    public void addTableHeader(Table table){
        table.addCell(AppConfig.EXPORT_TABLE_DATE_HEADER);
        table.addCell(AppConfig.EXPORT_TABLE_ANNEX_HEADER);
        table.addCell(AppConfig.EXPORT_TABLE_SECTION_HEADER);
        table.addCell(AppConfig.EXPORT_TABLE_TITLE_HEADER);
        table.addCell(AppConfig.EXPORT_TABLE_AMOUNT_HEADER);
    }

    /**
     * This method creates for each receipt one table row in a table.
     * @param table
     * @param receipt
     */
    public void addTableReceiptRow(Table table, Receipt receipt){
        //table.addCell(receipt.getReceiptDate().toString());
        table.addCell(AppConfig.DATE_FORMAT.format(receipt.getReceiptDate()));
        table.addCell(receipt.getAnnex());
        table.addCell(receipt.getSection());
        table.addCell(receipt.getTitle());
        //table.addCell(receipt.getAmount().toString());
        table.addCell(AppConfig.formatMoney(receipt.getAmount()));
    }

    /**
     * This method converts a bitmap of a receipt into a PDF-File.
     * @param document
     * @param receipt
     * @throws IOException
     */
    public void addReceiptPage(Document document, Receipt receipt) throws IOException {
        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
        Bitmap bitmap = createBitmapFromPDF(receipt.getFilePath()+ receipt.getFileName());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, AppConfig.EXPORT_IMAGE_QUALITY, stream);
        byte[] bitmapData = stream.toByteArray();
        ImageData imageData = ImageDataFactory.create(bitmapData);
        Image image = new Image(imageData);
        image.setHeight(AppConfig.EXPORT_IMAGE_HEIGHT);
        image.setWidth(AppConfig.EXPORT_IMAGE_WIDTH);
        document.add(image);
    }

    /**
     * This method creates the PDF-Docuent.
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    private Document initDocument(File file) throws FileNotFoundException {
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(writer);
        return new Document(pdfDocument);
    }

    /**
     * This method chaches the list of exports.
     * @return
     */
    public ArrayList<Export> getChachedExport (){
        if(exportPDFList != null){
            for(Export export: exportPDFList){
                Log.d(AppConfig.PDF_TAG, "Export file name" + export.getFileName());
            }
        }
        return exportPDFList;
    }

    /**
     * This method creates for every receipt its own table with the matching receipt data.
     * For every receipt in the receiptslist, one-pdf-file is created separately.
     * @throws IOException
     */
    public void createExportSingle(List<Receipt> receipts, CreateExportTask.CreateExportResultListener listener) throws IOException {
        if(!ScannerHelper.initDirectory()){
            UIHelper.showToast(context, context.getResources().getString(R.string.scanNoDirectoryCreated));
        }
        for (Receipt element: receipts){
            Date timestamp = new Date();
            String fileName = createFileName(timestamp);
            File tableWithReceiptInfo = new File(AppConfig.FOLDER_PATH, fileName);
            PdfWriter writer = new PdfWriter(tableWithReceiptInfo);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            Table table = new Table(AppConfig.EXPORT_TABLE_COLUMN_WIDTH);
            addTableHeader(table);
            addTableReceiptRow(table, element);
            document.add(table);
            addReceiptPage(document, element);
            document.close();
            Export export = new Export(AppConfig.FOLDER_PATH, fileName, timestamp);
            this.exportPDFList.add(export);
            dbHelper.createExport(export, listener);
        }
    }

    /**
     * This method creates a Bitmap-object from an PDF-Filepath.
     * @param pdfPath
     * @return
     * @throws IOException
     */
    public Bitmap createBitmapFromPDF(String pdfPath) throws IOException {
        File file = new File(pdfPath);
        PDDocument pd = PDDocument.load(file);
        PDFRenderer pr = new PDFRenderer (pd);
        return pr.renderImageWithDPI(AppConfig.PDF_TO_BITMAP_PAGE_INDEX, AppConfig.PDF_TO_BITMAP_DPI);
    }


    /**
     * This method creates the filename of an export.
     * @return filename
     */
    public String createFileName(Date date) {
        String timeStamp = AppConfig.TIME_STAMP_FORMAT.format(date);
        return timeStamp + AppConfig.EXPORT_FILE_NAME;
    }

    /**
     * This interface is responsible for intercepting the arraylist of exports.
     */
    public interface PdfExportResultListener{
        void onExportResult(ArrayList<Export> exports);
    }

}
