package io.cake.easy_taxfox.VisionApi;


import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Vertex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.cake.easy_taxfox.Config.AppConfig;
import io.cake.easy_taxfox.Config.Log;

/***
 * This class provides the main functionality to process the prediction response of the google vision api
 */
public class VisionApiParser {

    /***
     * This method serves as the entry point of the processing functionality. It calls methods to extract a title and an amount from the response
     * @param batchResponse
     * @return
     */
    public static ReceiptPrediction getReceiptPrediction(BatchAnnotateImagesResponse batchResponse) {
        List<AnnotateImageResponse> responses = batchResponse.getResponses();
        List<EntityAnnotation> flatAnnotations = responses.get(0).getTextAnnotations();
        String title = getTitle(flatAnnotations);

        Log.d(AppConfig.VISION_API_TAG, "Neuer Titel: " + title);
        Double sum = AppConfig.DEFAULT_RECEIPT_PREDICTION_AMOUNT;
        try {
            sum = getSumPrediction(flatAnnotations);
            Log.d(AppConfig.VISION_API_TAG, "Summe Ergebnis " + sum.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ReceiptPrediction(title, sum);
    }

    /***
     * This method uses geometry to extract the title out of the vision api response
     * @param entityAnnotations
     * @return
     */
    private static String getTitle(List<EntityAnnotation> entityAnnotations) {
        //Get lower points of the first row of responses
        Vertex vertex1 = entityAnnotations.get(1).getBoundingPoly().getVertices().get(3);
        Vertex vertex2 = entityAnnotations.get(1).getBoundingPoly().getVertices().get(2);
        //get upper points of the first row of responses
        Vertex vertex1b = entityAnnotations.get(1).getBoundingPoly().getVertices().get(0);
        Vertex vertex2b = entityAnnotations.get(1).getBoundingPoly().getVertices().get(1);

        //Create a line with the lower points
        Coordinate point1 = new Coordinate(vertex1.getX(), vertex1.getY());
        Coordinate point2 = new Coordinate(vertex2.getX(), vertex2.getY());
        Line line1 = new Line(point1, point2);

        //Create a line with the upper points and a parallel line  below the first line
        Coordinate point1b = new Coordinate(vertex1b.getX(), vertex1b.getY());
        Coordinate point2b = new Coordinate(vertex2b.getX(), vertex2b.getY());
        Line parallelLine1 = line1.getParallelLine(point1b, point2b);
        Line parallelLine2 = Line.getSecondParallelLine(line1, parallelLine1);

        StringBuilder titleBuilder = new StringBuilder();
        //Loop through annotations
        for (EntityAnnotation entityAnnotation : entityAnnotations) {
            if (entityAnnotation.getDescription().length() > AppConfig.VISION_MAX_TITLE_LENGTH) {
                continue;
            }
            //get both lower points of the annotation
            Vertex loopVertex1 = entityAnnotation.getBoundingPoly().getVertices().get(3);
            Vertex loopVertex2 = entityAnnotation.getBoundingPoly().getVertices().get(2);
            Coordinate loopPoint1 = new Coordinate(loopVertex1.getX(), loopVertex1.getY());
            Coordinate loopPoint2 = new Coordinate(loopVertex2.getX(), loopVertex2.getY());
            //if these points are between the two parallel lines, append the title
            if (Line.isPointBetweenLines(loopPoint1, parallelLine1, parallelLine2) && Line.isPointBetweenLines(loopPoint2, parallelLine1, parallelLine2)) {
                titleBuilder.append(" ");
                titleBuilder.append(entityAnnotation.getDescription());
            } else {
                break;
            }
        }
        return titleBuilder.toString();
    }


    /***
     * This method uses geometry to extract the sum out of the google vision api response
     * @param entityAnnotations
     * @return
     * @throws IOException
     */
    private static Double getSumPrediction(List<EntityAnnotation> entityAnnotations) throws IOException {
        //Loop through descriptions and find element that is in String array
        EntityAnnotation sumEntityAnnotation = null;
        Log.d(AppConfig.VISION_API_TAG, "Starte Loop");
        int i = 0;

        //Get first instance where the description matches a sum-keyword --> first instance of sum
        for (EntityAnnotation entityAnnotation : entityAnnotations) {
            String description = entityAnnotation.getDescription();
            Log.d(AppConfig.VISION_API_TAG, "Durchlauf " + i);
            i++;
            if (Arrays.asList(AppConfig.VISION_SUM_KEYWORDS).contains(description.toLowerCase())) {
                sumEntityAnnotation = entityAnnotation;
                break;
            }
        }

        if (sumEntityAnnotation != null) {
            //if an instance of sum was found
            Log.d(AppConfig.VISION_API_TAG, "Summe " + sumEntityAnnotation.toPrettyString());

            //create lower points
            Vertex vertex1 = sumEntityAnnotation.getBoundingPoly().getVertices().get(3);
            Vertex vertex2 = sumEntityAnnotation.getBoundingPoly().getVertices().get(2);
            Coordinate point1 = new Coordinate(vertex1.getX(), vertex1.getY());
            Coordinate point2 = new Coordinate(vertex2.getX(), vertex2.getY());

            //create upper points
            Vertex vertex1b = sumEntityAnnotation.getBoundingPoly().getVertices().get(0);
            Vertex vertex2b = sumEntityAnnotation.getBoundingPoly().getVertices().get(1);
            Coordinate point1b = new Coordinate(vertex1b.getX(), vertex1b.getY());
            Coordinate point2b = new Coordinate(vertex2b.getX(), vertex2b.getY());
            //create lines
            Line line1 = new Line(point1, point2);
            Line parallelLine1 = line1.getParallelLine(point1b, point2b);
            Line parallelLine2 = Line.getSecondParallelLine(line1, parallelLine1);



            StringBuilder sumBuilder = new StringBuilder();
            Log.d(AppConfig.VISION_API_TAG, "Start Loop 2");
            i = 0;
            for (EntityAnnotation entityAnnotation : entityAnnotations) {
                Log.d(AppConfig.VISION_API_TAG, "2. Loop: " + i);
                i++;
                if (entityAnnotation.getDescription().length() > AppConfig.VISION_MAX_TITLE_LENGTH) {
                    continue;
                }
                //get both lower points of current annotation
                Vertex loopVertex1 = entityAnnotation.getBoundingPoly().getVertices().get(3);
                Vertex loopVertex2 = entityAnnotation.getBoundingPoly().getVertices().get(2);
                Coordinate loopPoint1 = new Coordinate(loopVertex1.getX(), loopVertex1.getY());
                Coordinate loopPoint2 = new Coordinate(loopVertex2.getX(), loopVertex2.getY());
                //if these lower points are between the 2 parallel lines, append sum
                if (Line.isPointBetweenLines(loopPoint1, parallelLine1, parallelLine2) && Line.isPointBetweenLines(loopPoint2, parallelLine1, parallelLine2)) {
                    sumBuilder.append(" ");
                    sumBuilder.append(entityAnnotation.getDescription());
                }
            }


            String sumString = sumBuilder.toString();

            Log.d(AppConfig.VISION_API_TAG, "Sum unparsed: " + sumString);
            //Parse the sum
            List<String> sumList = Arrays.asList(sumString.trim().split(" "));
            Double sum = AppConfig.DEFAULT_RECEIPT_PREDICTION_AMOUNT;
            for (String sumItem : sumList) {
                sumItem = sumItem.trim().replaceAll(",", ".");
                try {
                    sum = Double.valueOf(sumItem);
                } catch (Exception e) {
                    Log.e(AppConfig.VISION_API_TAG, "Parsing error" + e.toString());
                }
            }
            Log.d(AppConfig.VISION_API_TAG, "Sum" + sum.toString());
            return sum;
        } else{
            return AppConfig.DEFAULT_RECEIPT_PREDICTION_AMOUNT;
        }
    }
}
