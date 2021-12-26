package io.cake.easy_taxfox.VisionApi;

import android.app.Activity;
import android.graphics.Bitmap;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.cake.easy_taxfox.Config.AppConfig;
import io.cake.easy_taxfox.Config.Log;

/***
 * This class sends the bitmap to the vision api and processes the response
 */
public class VisionApiTask implements Runnable {

    private String accessToken;
    private Bitmap bitmap;
    private Activity activityContext;
    private VisionApiResultListener listener;

    public VisionApiTask(String accessToken, Bitmap bitmap, Activity activityContext, VisionApiResultListener listener) {
        this.accessToken = accessToken;
        this.bitmap = bitmap;
        this.activityContext = activityContext;
        this.listener = listener;
    }

    /***
     * This method configures and sends the request to the vision api
     * @return
     */
    private BatchAnnotateImagesResponse callVisionApi(){
        try {
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
            HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
            Vision.Builder builder = new Vision.Builder
                    (httpTransport, jsonFactory, credential);
            Vision vision = builder.build();
            List<Feature> featureList = new ArrayList<>();
            Feature labelDetection = new Feature();
            labelDetection.setType(AppConfig.VISION_API_REQUEST_TYPE_1);
            labelDetection.setMaxResults(AppConfig.VISION_MAX_RESULTS);
            featureList.add(labelDetection);
            Feature textDetection = new Feature();
            textDetection.setType(AppConfig.VISION_API_REQUEST_TYPE_2);
            textDetection.setMaxResults(AppConfig.VISION_MAX_RESULTS);
            featureList.add(textDetection);
            List<AnnotateImageRequest> imageList = new ArrayList<>();
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
            Image base64EncodedImage = getBase64EncodedJpeg(bitmap);
            annotateImageRequest.setImage(base64EncodedImage);
            annotateImageRequest.setFeatures(featureList);
            imageList.add(annotateImageRequest);
            BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                    new BatchAnnotateImagesRequest();
            batchAnnotateImagesRequest.setRequests(imageList);
            Vision.Images.Annotate annotateRequest =
                    vision.images().annotate(batchAnnotateImagesRequest);
            annotateRequest.setDisableGZipContent(true);
            Log.d(AppConfig.VISION_API_TAG, "Sending request to Google Cloud");
            BatchAnnotateImagesResponse response = annotateRequest.execute();
            return response;
        } catch (GoogleJsonResponseException e) {
            Log.e(AppConfig.VISION_API_TAG, "Request error: " + e.getContent());
        } catch (IOException e) {
            Log.d(AppConfig.VISION_API_TAG, "Request error: " + e.getMessage());
        }
        return null;
    }

    /***
     * This method calls the vision api and the processing methods. After that, the result is passed to the listener
     */
    @Override
    public void run() {
        ReceiptPrediction receiptPrediction;
        BatchAnnotateImagesResponse response = callVisionApi();
        if (response == null){
            receiptPrediction = new ReceiptPrediction(AppConfig.DEFAULT_RECEIPT_PREDICTION_TITLE, AppConfig.DEFAULT_RECEIPT_PREDICTION_AMOUNT);
        }else{
            receiptPrediction = VisionApiParser.getReceiptPrediction(response);
        }
        activityContext.runOnUiThread(() -> listener.onResult(receiptPrediction));

    }

    /***
     * This method transforms a given bitmap to a base 64 encoded jpeg image
     * @param bitmap
     * @return
     */
    private Image getBase64EncodedJpeg(Bitmap bitmap) {
        Image image = new Image();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, AppConfig.VISION_JPEG_COMPRESS_QUALITY, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        image.encodeContent(imageBytes);
        return image;
    }

    /***
     * This interface provides a callback method to send the processed receipt prediction
     */
    public interface VisionApiResultListener{
        void onResult(ReceiptPrediction receiptPrediction);
    }
}
