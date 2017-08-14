package org.badvision.tardispi;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

/**
 * Abstraction of a Group which has convenience methods for drawing shapes
 */
public class ShapeGroup extends Group {
    Paint strokeColor = Color.BLACK;
    Paint fillColor = Color.TRANSPARENT;
    Double strokeWeight = 1.0;
    StrokeLineCap strokeCap = StrokeLineCap.SQUARE;

    public void stroke(Paint paint) {
        strokeColor = paint;
    }

    public void strokeWeight(double w) {
        strokeWeight = w;
    }

    public void strokeCap(StrokeLineCap strokeLineCap) {
        strokeCap = strokeLineCap;
    }

    public void fill(Paint paint) {
        fillColor = paint;
    }

    public void noStroke() {
        strokeColor = Color.TRANSPARENT;
    }

    public void noFill() {
        fillColor = Color.TRANSPARENT;
    }

    public void line(double x1, double y1, double x2, double y2) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(strokeColor);
        line.setStrokeWidth(strokeWeight);
        line.setStrokeLineCap(strokeCap);
        getChildren().add(line);
    }

    public double rad2deg(double rads) {
        return rads * 180.0 / Math.PI;
    }

    public void arc(double x, double y, double width, double height, double start, double stop) {
        Arc arc = new Arc(x, y, width, height, rad2deg(start), rad2deg(stop - start));
        arc.setType(ArcType.OPEN);
        arc.setStroke(strokeColor);
        arc.setFill(Color.TRANSPARENT);
        arc.setStrokeWidth(strokeWeight);
        arc.setStrokeLineCap(strokeCap);
        getChildren().add(arc);
    }

    public void ellipse(double x, double y, double width, double height) {
        Ellipse ellipse = new Ellipse(x, y, width, height);
        ellipse.setStroke(strokeColor);
        ellipse.setStrokeWidth(strokeWeight);
        ellipse.setFill(fillColor);
        getChildren().add(ellipse);
    }
    
    public double map(double value, double rangeStart, double rangeEnd, double targetRangeStart, double targetRangeEnd) {
        double range1 = rangeEnd - rangeStart;
        double amt = (value - rangeStart) / range1;
        return lerp(targetRangeStart, targetRangeEnd, amt);
    }

    public double lerp(double start, double stop, double amt) {
        return start + ((stop - start) * amt);
    }    
}