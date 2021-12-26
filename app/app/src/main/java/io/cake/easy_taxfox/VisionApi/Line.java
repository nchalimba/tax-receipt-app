package io.cake.easy_taxfox.VisionApi;

import io.cake.easy_taxfox.Config.AppConfig;

/***
 * This class provides methods for linear algebra to extract the google vision api data
 */
public class Line {

    private Coordinate point1;
    private Coordinate point2;
    private double m;
    private double t;

    public Line(Coordinate point1, Coordinate point2) {
        this.point1 = point1;
        this.point2 = point2;
        m = (point2.getY() - point1.getY()) / (point2.getX() - point1.getX());
        t = point1.getY() - m * point1.getX();
    }

    private Line(double m, double t) {
        this.m = m;
        this.t = t;
    }

    /***
     * This method gets a parallel line for a given line, in which parallel line is between point1 to point2 and point1b and point2b
     * @param point1b
     * @param point2b
     * @return
     */
    public Line getParallelLine(Coordinate point1b, Coordinate point2b) {
        double x1 = (point1.getX() + point1b.getX()) / 2;
        double y1 = (point1.getY() + point1b.getY()) / 2;
        int roundedX1 = (int) Math.round(x1);
        int roundedy1 = (int) Math.round(y1);
        Coordinate parallelPoint1 = new Coordinate(roundedX1, roundedy1);

        double x2 = (point2.getX() + point2b.getX()) / 2;
        double y2 = (point2.getY() + point2b.getY()) / 2;
        int roundedX2 = (int) Math.round(x2);
        int roundedY2 = (int) Math.round(y2);
        Coordinate parallelPoint2 = new Coordinate(roundedX2, roundedY2);

        return new Line(parallelPoint1, parallelPoint2);
    }

    /***
     * Gets a second parralel line that has the same distance to the original line as the first parallel line
     * @param line
     * @param parallelLine
     * @return
     */
    public static Line getSecondParallelLine(Line line, Line parallelLine) {
        double deltaT = line.t - parallelLine.t;
        double secondT = line.t + deltaT;

        return new Line(line.m, secondT);
    }

    /***
     * Determines whether a point is between two parallel lines (line1 must be the line above)
     * @param point
     * @param line1
     * @param line2
     * @return
     */
    public static boolean isPointBetweenLines(Coordinate point, Line line1, Line line2) {
        Line lowerLine;
        Line upperLine;
        if (line1.t > line2.t) {
            lowerLine = line1;
            upperLine = line2;
        } else {
            lowerLine = line2;
            upperLine = line1;
        }
        return getPointPosition(point, upperLine) == AppConfig.GEOMETRY_BELOW_LINE && getPointPosition(point, lowerLine) == AppConfig.GEOMETRY_ABOVE_LINE;
    }


    /***
     * Determines whether point is above, below or on line
     * @param point
     * @param line
     * @return
     */
    private static int getPointPosition(Coordinate point, Line line) {
        double y = point.getX() * line.m + line.t;

        if (point.getY() == y) {
            return AppConfig.GEOMETRY_ON_LINE;
        } else if (point.getY() > y) {
            return AppConfig.GEOMETRY_BELOW_LINE;
        } else {
            return AppConfig.GEOMETRY_ABOVE_LINE;
        }
    }

    @Override
    public String toString() {
        return "Line{" +
                "point1=" + point1.toString() +
                ", point2=" + point2.toString() +
                ", m=" + m +
                ", t=" + t +
                '}';
    }
}
