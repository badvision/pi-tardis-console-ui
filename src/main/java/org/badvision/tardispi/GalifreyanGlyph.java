package org.badvision.tardispi;

import javafx.scene.Group;
import static java.lang.Math.*;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

/**
 * Generate a circular glyph
 */
public class GalifreyanGlyph extends Group {

    public static class LineSegment {

        double x, y, arcBegin, arcEnd, lineRad;

        public LineSegment(double x, double y, double arcBegin, double arcEnd, double lineRad) {
            this.x = x;
            this.y = y;
            this.arcBegin = arcBegin;
            this.arcEnd = arcEnd;
            this.lineRad = lineRad;
        }
    }

    int maxPartSize;
    int overallRadius;
    Paint foregroundColor;
    Paint backgroundColor;
    double TWO_PI = 2 * PI;

    public GalifreyanGlyph(String message, int radius, Paint foreground, Paint background) throws Exception {
        super();
        overallRadius = radius;
        foregroundColor = foreground;
        backgroundColor = background;
//        Circle base = new Circle();
//        base.setRadius(radius);
//        base.getStyleClass().add("basic-shape");
//        getChildren().add(base);
//        Circle clip = new Circle();
//        clip.setRadius(radius);
//        setClip(clip);
        writeSentence(message);
    }

    public String performSymbolicSubstitutions(String message) {
        message = message.toLowerCase();
        message = message.replaceAll("\\s*-\\s*", "-");
        message = message.replaceAll("ch", "#");
        message = message.replaceAll("sh", "$");
        message = message.replaceAll("th", "%");
        message = message.replaceAll("ng", "&");
        return message.replaceAll("qu", "q");
    }

    public SentenceType determineSentenceType(String message) throws Exception {
        boolean hasSpaces = message.contains(" ");
        String allButLast = message.substring(0, message.length() - 1);
        boolean multipleSentences = allButLast.contains("!") || allButLast.contains(".") || allButLast.contains("?");
        if (!hasSpaces) {
            return SentenceType.single_word;
        } else if (!multipleSentences) {
            return SentenceType.full_sentence;
        } else {
            throw new Exception("ERROR: Multiple sentences are not yet supported.");
        }
    }

    Paint strokeColor = Color.BLACK;
    Paint fillColor = Color.color(0, 0, 0, 0);
    Double strokeWeight = 1.0;
    StrokeLineCap strokeCap = StrokeLineCap.SQUARE;

    private void stroke(Paint paint) {
        strokeColor = paint;
    }

    private void strokeWeight(double w) {
        strokeWeight = w;
    }

    private void strokeCap(StrokeLineCap strokeLineCap) {
        strokeCap = strokeLineCap;
    }

    private void fill(Paint paint) {
        fillColor = paint;
    }

    private void noStroke() {
        strokeColor = Color.rgb(0, 0, 0, 0);
    }

    private void noFill() {
        fillColor = Color.rgb(0, 0, 0, 0);
    }

    private void line(double x1, double y1, double x2, double y2) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(strokeColor);
        line.setStrokeWidth(strokeWeight);
        line.setStrokeLineCap(strokeCap);
        getChildren().add(line);
    }

    private void arc(double x, double y, double width, double height, double start, double stop) {
        Arc arc = new Arc(x, y, width, height, start, stop - start);
        arc.setStroke(strokeColor);
        arc.setStrokeWidth(strokeWeight);
        arc.setStrokeLineCap(strokeCap);
        getChildren().add(arc);
    }

    private void ellipse(double x, double y, double width, double height) {
        Ellipse ellipse = new Ellipse(x, y, width, height);
        ellipse.setStroke(strokeColor);
        ellipse.setStrokeWidth(strokeWeight);
        ellipse.setFill(fillColor);
        getChildren().add(ellipse);
    }

    public static enum SentenceType {
        single_word, full_sentence
    };

    public void appendLastString(List<String> s, String v) {
        s.set(s.size() - 1, s.get(s.size() - 1) + v);
    }

    public void writeSentence(String message) throws Exception {
        SentenceType type = determineSentenceType(message);
        List<Double> wordRadius = new ArrayList<>();
        List<LineSegment> lineSegments = new ArrayList<>();
        double1 = 0;
        double2 = 0;
        double charCount = 0;
        String[] words = performSymbolicSubstitutions(message).trim().split("\\s");
        List<List<String>> sentence = new ArrayList<>();
        char[] punctuation = new char[words.length];
        boolean[][] apostrophes = new boolean[words.length][100];
        for (int j = 0; j < words.length; j++) {
            List<String> wordParts = new ArrayList<>();
            words[j] = words[j].replaceAll("\\s", "");
            boolean noRoomForVowel = true;
            for (int i = 0; i < words[j].length(); i++) {
                char currentChar = words[j].charAt(i);
                String currentCharStr = String.valueOf(words[j].charAt(i));

                if (i != 0) {
                    if (currentChar == words[j].charAt(i - 1)) {
                        appendLastString(wordParts, "@");
                        continue;
                    }
                }
                if (isVowel(currentChar)) {
                    if (noRoomForVowel) {
                        wordParts.add(String.valueOf(currentChar));
                    } else {
                        appendLastString(wordParts, currentCharStr);
                    }
                    noRoomForVowel = true;
                } else if (isPunctuation(currentChar)) {
                    if (currentChar == '\'') {
                        apostrophes[j][i] = true;
                    } else {
                        punctuation[j] = currentChar;
                    }
                } else {
                    wordParts.add(String.valueOf(currentChar));
                    noRoomForVowel = isInThirdSet(currentChar);
                }
            }
            sentence.add(wordParts);
            charCount += wordParts.size();
        }
        stroke(foregroundColor);
        if (type == SentenceType.single_word) {
            strokeWeight(3);
            ellipse(0, 0, overallRadius, overallRadius);
        }
        strokeWeight(4);
        ellipse(0, 0, overallRadius + 20, overallRadius + 20);
        double pos = PI / 2;
        double maxRadius = 0;
        for (int i = 0; i < sentence.size(); i++) {
            double boundedValue = min(max(overallRadius * sentence.get(i).size() / charCount * 1.2f, 0), overallRadius / 2);
            wordRadius.add(boundedValue);
            maxRadius = max(boundedValue, maxRadius);
        }
        double scaleFactor = overallRadius / (maxRadius + (overallRadius / 2));
        double distance = scaleFactor * overallRadius / 2;
        for (int i = 0; i < wordRadius.size(); i++) {
            wordRadius.set(i, wordRadius.get(i) * scaleFactor);
        }
        double[] x = new double[sentence.size()];
        double[] y = new double[sentence.size()];
        stroke(foregroundColor);
        for (int i = 0; i < sentence.size(); i++) {
            x[i] = distance * cos(pos);
            y[i] = distance * sin(pos);
            int nextIndex = 0;
            if (i != sentence.size() - 1) {
                nextIndex = i + 1;
            }
            int currentWordSize = sentence.get(i).size();
            int nextWordSize = sentence.get(nextIndex).size();

            pos -= (currentWordSize + nextWordSize) / (2 * charCount) * TWO_PI;
            double pX = cos(pos + currentWordSize + nextWordSize / (2 * charCount) * PI) * overallRadius;
            double pY = sin(pos + currentWordSize + nextWordSize / (2 * charCount) * PI) * overallRadius;
            switch (punctuation[i]) {
                case '.':
                    ellipse(pX, pY, 20, 20);
                    break;
                case '?':
                    makeDots(0, 0, overallRadius * 1.4f, 2, -1.2f, 0.1f);
                    break;
                case '!':
                    makeDots(0, 0, overallRadius * 1.4f, 3, -1.2f, 0.1f);
                    break;
                case '"':
                    line(cos(pos + currentWordSize + nextWordSize / (2 * charCount) * PI) * overallRadius, sin(pos + currentWordSize + nextWordSize / (2 * charCount) * PI) * overallRadius, cos(pos + currentWordSize + nextWordSize / (2 * charCount) * PI) * (overallRadius + 20), sin(pos + currentWordSize + nextWordSize / (2 * charCount) * PI) * (overallRadius + 20));
                    break;
                case '-':
                    line(cos(pos + currentWordSize + nextWordSize / (2 * charCount) * PI) * overallRadius, sin(pos + currentWordSize + nextWordSize / (2 * charCount) * PI) * overallRadius, cos(pos + currentWordSize + nextWordSize / (2 * charCount) * PI) * (overallRadius + 20), sin(pos + currentWordSize + nextWordSize / (2 * charCount) * PI) * (overallRadius + 20));
                    line(cos(pos + (currentWordSize + nextWordSize + 0.3f) / (2 * charCount) * PI) * overallRadius, sin(pos + (currentWordSize + nextWordSize + 0.2f) / (2 * charCount) * PI) * overallRadius, cos(pos + (currentWordSize + nextWordSize + 0.2f) / (2 * charCount) * PI) * (overallRadius + 20), sin(pos + (currentWordSize + nextWordSize + 0.3f) / (2 * charCount) * PI) * (overallRadius + 20));
                    line(cos(pos + (currentWordSize + nextWordSize - 0.3f) / (2 * charCount) * PI) * overallRadius, sin(pos + (currentWordSize + nextWordSize - 0.2f) / (2 * charCount) * PI) * overallRadius, cos(pos + (currentWordSize + nextWordSize - 0.2f) / (2 * charCount) * PI) * (overallRadius + 20), sin(pos + (currentWordSize + nextWordSize - 0.3f) / (2 * charCount) * PI) * (overallRadius + 20));
                    break;
                case ',':
                    fill(foregroundColor);
                    ellipse(pX, pY, 20, 20);
                    noFill();
                    break;
                case ';':
                    fill(foregroundColor);
                    ellipse(cos(pos + currentWordSize + nextWordSize / (2 * charCount) * PI) * overallRadius - 10, sin(pos + currentWordSize + nextWordSize / (2 * charCount) * PI) * overallRadius - 10, 10, 10);
                    noFill();
                    break;
                case ':':
                    ellipse(pX, pY, 25, 25);
                    strokeWeight(2);
                    ellipse(pX, pY, 15, 15);
                    strokeWeight(4);
                    break;
                default:
                    break;
            }
        }
        int otherIndex = 0;
        boolean[][] nested = new boolean[sentence.size()][100];
        for (int i = 0; i < sentence.size(); i++) {
            List<String> currentWord = sentence.get(i);
            int currentWordSize = currentWord.size();

            double angle1 = 0;//angle facing onwards
            if (i == sentence.size() - 1) {
                otherIndex = 0;
            } else {
                otherIndex = i + 1;
            }
            angle1 = atan((y[i] - y[otherIndex]) / (x[i] - x[otherIndex]));
            if (hypot(x[i] + (cos(angle1) * 20) - x[otherIndex], y[otherIndex] - y[i] + (sin(angle1) * 20)) > hypot(x[i] - x[otherIndex], y[i] - y[otherIndex])) {
                angle1 -= PI;
            }
            if (angle1 < 0) {
                angle1 += TWO_PI;
            }
            if (angle1 < 0) {
                angle1 += TWO_PI;
            }
            angle1 -= PI / 2;
            if (angle1 < 0) {
                angle1 += TWO_PI;
            }
            angle1 = TWO_PI - angle1;
            int index = (int) round(map(angle1, 0, TWO_PI, 0, currentWordSize)) % currentWordSize;
            char tempChar = sentence.get(i).get(index).charAt(0);
            if (isInThirdSet(tempChar) && type != SentenceType.single_word) {
                nested[i][index] = true;
                wordRadius.set(i, min(max(wordRadius.get(i) * 1.2, 0), maxRadius * scaleFactor));
                while (hypot(x[i] - x[otherIndex], y[i] - y[otherIndex]) > wordRadius.get(i) + wordRadius.get(otherIndex)) {
                    x[i] = lerp(x[i], x[otherIndex], 0.05);
                    y[i] = lerp(y[i], y[otherIndex], 0.05);
                }
            }
        }
        strokeWeight(2);
        if (type == SentenceType.single_word) {
            wordRadius.set(0, overallRadius * 0.9);
            x[0] = 0;
            y[0] = 0;
        }
        for (int i = 0; i < sentence.size(); i++) {
            List<String> currentWord = sentence.get(i);
            int currentWordSize = currentWord.size();
            pos = PI / 2;
            double letterRadius = wordRadius.get(i) / (currentWordSize + 1) * 1.5f;
            for (int j = 0; j < currentWordSize; j++) {
                if (apostrophes[i][j]) {
                    double a = pos + PI / currentWordSize - 0.1f;
                    double d = 0;
                    double tempX = x[i];
                    double tempY = y[i];
                    while (pow(tempX - 0, 2) + pow(tempY - 0, 2) < pow(overallRadius + 20, 2)) {
                        tempX = x[i] + cos(a) * d;
                        tempY = y[i] + sin(a) * d;
                        d += 1;
                    }
                    line(x[i] + cos(a) * wordRadius.get(i), y[i] + sin(a) * wordRadius.get(i), tempX, tempY);
                    a = pos + PI / currentWordSize + 0.1f;
                    d = 0;
                    tempX = x[i];
                    tempY = y[i];
                    while (pow(tempX - 0, 2) + pow(tempY - 0, 2) < pow(overallRadius + 20, 2)) {
                        tempX = x[i] + cos(a) * d;
                        tempY = y[i] + sin(a) * d;
                        d += 1;
                    }
                    line(x[i] + cos(a) * wordRadius.get(i), y[i] + sin(a) * wordRadius.get(i), tempX, tempY);
                }
                boolean vowel = true;
                double tempX = 0;
                double tempY = 0;

                String currentPart = currentWord.get(j);
                char firstLetter = currentPart.charAt(0);
                //single vowels
                switch (firstLetter) {
                    case 'a':
                        tempX = x[i] + cos(pos) * (wordRadius.get(i) + letterRadius / 2);
                        tempY = y[i] + sin(pos) * (wordRadius.get(i) + letterRadius / 2);
                        ellipse(tempX, tempY, letterRadius, letterRadius);
                        break;

                    case 'e':
                        tempX = x[i] + cos(pos) * (wordRadius.get(i));
                        tempY = y[i] + sin(pos) * (wordRadius.get(i));
                        ellipse(tempX, tempY, letterRadius, letterRadius);
                        break;
                    case 'i':
                        tempX = x[i] + cos(pos) * (wordRadius.get(i));
                        tempY = y[i] + sin(pos) * (wordRadius.get(i));
                        ellipse(tempX, tempY, letterRadius, letterRadius);
                        lineSegments.add(new LineSegment(tempX, tempY, pos + PI / 2, pos + 3 * PI / 2, letterRadius));
                        break;
                    case 'o':
                        tempX = x[i] + cos(pos) * (wordRadius.get(i) - letterRadius / 1.6f);
                        tempY = y[i] + sin(pos) * (wordRadius.get(i) - letterRadius / 1.6f);
                        ellipse(tempX, tempY, letterRadius, letterRadius);
                        break;
                    case 'u':
                        tempX = x[i] + cos(pos) * (wordRadius.get(i));
                        tempY = y[i] + sin(pos) * (wordRadius.get(i));
                        ellipse(tempX, tempY, letterRadius, letterRadius);
                        lineSegments.add(new LineSegment(tempX, tempY, pos - PI / 2, pos + PI / 2, letterRadius));
                        break;

                    default:
                        vowel = false;
                }

                if (vowel) {
                    arc(x[i], y[i], wordRadius.get(i) * 2, wordRadius.get(i) * 2, pos - PI / currentWordSize, pos + PI / currentWordSize);
                    if (currentPart.length() > 1) {
                        //double vowels
                        if (currentPart.charAt(1) == '@') {
                            ellipse(tempX, tempY, letterRadius * 1.3f, letterRadius * 1.3f);
                        }
                    }
                } else {

                    // consonants
                    if (isInFirstSet(firstLetter)) {
                        tempX = x[i] + cos(pos) * (wordRadius.get(i) - letterRadius * 0.95f);
                        tempY = y[i] + sin(pos) * (wordRadius.get(i) - letterRadius * 0.95f);
                        makeArcs(tempX, tempY, x[i], y[i], wordRadius.get(i), letterRadius, pos - PI / currentWordSize, pos + PI / currentWordSize);
                        int lines = 0;

                        switch (firstLetter) {
                            case '#':
                                makeDots(tempX, tempY, letterRadius, 2, pos, 1);
                                break;
                            case 'd':
                                makeDots(tempX, tempY, letterRadius, 3, pos, 1);
                                break;
                            case 'f':
                                lines = 3;
                                break;
                            case 'g':
                                lines = 1;
                                break;
                            case 'h':
                                lines = 2;
                        }
                        for (int k = 0; k < lines; k++) {
                            lineSegments.add(new LineSegment(tempX, tempY, pos + 0.5f, pos + TWO_PI - 0.5, letterRadius * 2));
                        }
                        if (currentPart.length() > 1) {
                            int vowelIndex = 1;
                            if (currentPart.charAt(1) == '@') {
                                makeArcs(tempX, tempY, x[i], y[i], wordRadius.get(i), letterRadius * 1.3f, pos + TWO_PI, pos - TWO_PI);
                                vowelIndex = 2;
                            }
                            if (currentPart.length() == vowelIndex) {
                                pos -= TWO_PI / currentWordSize;
                                continue;
                            }
                            switch (currentPart.charAt(vowelIndex)) {
                                case 'a':
                                    tempX = x[i] + cos(pos) * (wordRadius.get(i) + letterRadius / 2);
                                    tempY = y[i] + sin(pos) * (wordRadius.get(i) + letterRadius / 2);
                                    ellipse(tempX, tempY, letterRadius, letterRadius);
                                    break;
                                case 'e':
                                    ellipse(tempX, tempY, letterRadius, letterRadius);
                                    break;
                                case 'i':
                                    ellipse(tempX, tempY, letterRadius, letterRadius);
                                    lineSegments.add(new LineSegment(tempX, tempY, pos + PI / 2, pos + 3 * PI / 2, letterRadius));
                                    break;
                                case 'o':
                                    tempX = x[i] + cos(pos) * (wordRadius.get(i) - letterRadius * 2);
                                    tempY = y[i] + sin(pos) * (wordRadius.get(i) - letterRadius * 2);
                                    ellipse(tempX, tempY, letterRadius, letterRadius);
                                    break;
                                case 'u':
                                    ellipse(tempX, tempY, letterRadius, letterRadius);
                                    lineSegments.add(new LineSegment(tempX, tempY, pos - PI / 2, pos + PI / 2, letterRadius));
                                    break;
                                default:
                                    break;
                            }
                            if (currentPart.length() == (vowelIndex + 2)) {
                                if (currentPart.charAt(vowelIndex + 1) == '@') {
                                    ellipse(tempX, tempY, letterRadius * 1.3f, letterRadius * 1.3f);
                                }
                            }
                        }
                    }
                    if (isInSecondSet(firstLetter)) {
                        tempX = x[i] + cos(pos) * (wordRadius.get(i) - letterRadius);
                        tempY = y[i] + sin(pos) * (wordRadius.get(i) - letterRadius);
                        ellipse(tempX, tempY, letterRadius * 1.9f, letterRadius * 1.9f);
                        arc(x[i], y[i], wordRadius.get(i) * 2, wordRadius.get(i) * 2, pos - PI / currentWordSize, pos + PI / currentWordSize);
                        int lines = 0;
                        switch (currentPart.charAt(0)) {
                            case 'k':
                                makeDots(tempX, tempY, letterRadius, 2, pos, 1);
                                break;
                            case 'l':
                                makeDots(tempX, tempY, letterRadius, 3, pos, 1);
                                break;
                            case 'm':
                                lines = 3;
                                break;
                            case 'n':
                                lines = 1;
                                break;
                            case 'p':
                                lines = 2;
                                break;
                            default:
                                break;
                        }
                        for (int k = 0; k < lines; k++) {
                            lineSegments.add(new LineSegment(tempX, tempY, 0, TWO_PI, letterRadius * 1.9));
                        }
                        if (currentPart.length() > 1) {
                            int vowelIndex = 1;
                            if (currentPart.charAt(1) == '@') {
                                ellipse(tempX, tempY, letterRadius * 2.3f, letterRadius * 2.3f);
                                vowelIndex = 2;
                            }
                            if (currentPart.length() == vowelIndex) {
                                pos -= TWO_PI / currentWordSize;
                                continue;
                            }
                            switch (currentPart.charAt(vowelIndex)) {
                                case 'a':
                                    tempX = x[i] + cos(pos) * (wordRadius.get(i) + letterRadius / 2);
                                    tempY = y[i] + sin(pos) * (wordRadius.get(i) + letterRadius / 2);
                                    ellipse(tempX, tempY, letterRadius, letterRadius);
                                    break;
                                case 'e':
                                    ellipse(tempX, tempY, letterRadius, letterRadius);
                                    break;
                                case 'i':
                                    ellipse(tempX, tempY, letterRadius, letterRadius);
                                    lineSegments.add(new LineSegment(tempX, tempY, pos + PI / 2, pos + 3 * PI / 2, letterRadius));
                                    break;
                                case 'o':
                                    tempX = x[i] + cos(pos) * (wordRadius.get(i) - letterRadius * 2);
                                    tempY = y[i] + sin(pos) * (wordRadius.get(i) - letterRadius * 2);
                                    ellipse(tempX, tempY, letterRadius, letterRadius);
                                    break;
                                case 'u':
                                    ellipse(tempX, tempY, letterRadius, letterRadius);
                                    lineSegments.add(new LineSegment(tempX, tempY, pos - PI / 2, pos + PI / 2, letterRadius));
                                    break;
                                default:
                                    break;
                            }
                            if (currentPart.length() == (vowelIndex + 2)) {
                                if (currentPart.charAt(vowelIndex + 1) == '@') {
                                    ellipse(tempX, tempY, letterRadius * 1.3f, letterRadius * 1.3f);
                                }
                            }
                        }
                    }
                    if (isInThirdSet(firstLetter)) {
                        tempX = x[i] + cos(pos) * (wordRadius.get(i));
                        tempY = y[i] + sin(pos) * (wordRadius.get(i));
                        int nextIndex;
                        if (i == sentence.size() - 1) {
                            nextIndex = 0;
                        } else {
                            nextIndex = i + 1;
                        }
                        double angle1 = atan((y[i] - y[nextIndex]) / (x[i] - x[nextIndex]));
                        if (hypot(x[i] + (cos(angle1) * 20) - x[nextIndex], y[i] + (sin(angle1) * 20) - y[nextIndex]) > hypot(x[i] - x[nextIndex], y[i] - y[nextIndex])) {
                            angle1 -= PI;
                        }
                        if (angle1 < 0) {
                            angle1 += TWO_PI;
                        }
                        if (angle1 < 0) {
                            angle1 += TWO_PI;
                        }
                        if (nested[i][j]) {
                            makeArcs(x[nextIndex], y[nextIndex], x[i], y[i], wordRadius.get(i), wordRadius.get(nextIndex) + 20, pos - PI / currentWordSize, pos + PI / currentWordSize);
                        } else {
                            makeArcs(tempX, tempY, x[i], y[i], wordRadius.get(i), letterRadius * 1.5, pos - PI / currentWordSize, pos + PI / currentWordSize);
                        }
                        int lines = 0;
                        switch (currentPart.charAt(0)) {
                            case '$':
                                if (nested[i][j]) {
                                    makeDots(x[nextIndex], y[nextIndex], (wordRadius.get(nextIndex) * 1.4) + 14, 2, angle1, wordRadius.get(nextIndex) / 500);
                                } else {
                                    makeDots(tempX, tempY, letterRadius * 2.6, 2, pos, 0.5);
                                }
                                break;
                            case 'r':
                                if (nested[i][j]) {
                                    makeDots(x[nextIndex], y[nextIndex], (wordRadius.get(nextIndex) * 1.4) + 14, 3, angle1, wordRadius.get(nextIndex) / 500);
                                } else {
                                    makeDots(tempX, tempY, letterRadius * 2.6, 3, pos, 0.5);
                                }
                                break;
                            case 's':
                                lines = 3;
                                break;
                            case 'v':
                                lines = 1;
                                break;
                            case 'w':
                                lines = 2;
                                break;
                            default:
                                break;
                        }
                        if (nested[i][j]) {
                            for (int k = 0; k < lines; k++) {
                                lineSegments.add(new LineSegment(x[nextIndex], y[nextIndex], double1, double2, wordRadius.get(nextIndex) * 2 + 40));
                            }
                        } else {
                            for (int k = 0; k < lines; k++) {
                                lineSegments.add(new LineSegment(tempX, tempY, double1, double2, letterRadius * 3));
                            }
                        }
                        if (currentPart.length() > 1) {
                            if (currentPart.charAt(1) == '@') {
                                if (nested[i][j]) {
                                    makeArcs(x[nextIndex], y[nextIndex], x[i], y[i], wordRadius.get(i), (wordRadius.get(nextIndex) + 20) * 1.2f, pos + TWO_PI, pos - TWO_PI);
                                } else {
                                    makeArcs(tempX, tempY, x[i], y[i], wordRadius.get(i), letterRadius * 1.8f, pos + TWO_PI, pos - TWO_PI);
                                }
                            }
                        }
                    }
                    if (isInFourthSet(firstLetter)) {
                        tempX = x[i] + cos(pos) * (wordRadius.get(i));
                        tempY = y[i] + sin(pos) * (wordRadius.get(i));
                        ellipse(tempX, tempY, letterRadius * 2, letterRadius * 2);
                        arc(x[i], y[i], wordRadius.get(i) * 2, wordRadius.get(i) * 2, pos - PI / currentWordSize, pos + PI / currentWordSize);
                        int lines = 0;
                        switch (currentPart.charAt(0)) {
                            case 'y':
                                makeDots(tempX, tempY, letterRadius, 2, pos, 1);
                                break;
                            case 'z':
                                makeDots(tempX, tempY, letterRadius, 3, pos, 1);
                                break;
                            case '&':
                                lines = 3;
                                break;
                            case 'q':
                                lines = 1;
                                break;
                            case 'x':
                                lines = 2;
                                break;
                            default:
                                break;
                        }
                        for (int k = 0; k < lines; k++) {
                            lineSegments.add(new LineSegment(tempX, tempY, 0, TWO_PI, letterRadius * 2));
                        }
                        if (currentPart.length() > 1) {
                            int vowelIndex = 1;
                            if (currentPart.charAt(1) == '@') {
                                ellipse(tempX, tempY, letterRadius * 2.3f, letterRadius * 2.3f);
                                vowelIndex = 2;
                            }
                            if (currentPart.length() == vowelIndex) {
                                pos -= TWO_PI / currentWordSize;
                                continue;
                            }
                            switch (currentPart.charAt(vowelIndex)) {
                                case 'a':
                                    tempX = x[i] + cos(pos) * (wordRadius.get(i) + letterRadius / 2);
                                    tempY = y[i] + sin(pos) * (wordRadius.get(i) + letterRadius / 2);
                                    ellipse(tempX, tempY, letterRadius, letterRadius);
                                    break;
                                case 'e':
                                    ellipse(tempX, tempY, letterRadius, letterRadius);
                                    break;
                                case 'i':
                                    ellipse(tempX, tempY, letterRadius, letterRadius);
                                    lineSegments.add(new LineSegment(tempX, tempY, pos + PI / 2, pos + 3 * PI / 2, letterRadius));
                                    break;
                                case 'o':
                                    tempX = x[i] + cos(pos) * (wordRadius.get(i) - letterRadius);
                                    tempY = y[i] + sin(pos) * (wordRadius.get(i) - letterRadius);
                                    ellipse(tempX, tempY, letterRadius, letterRadius);
                                    break;
                                case 'u':
                                    ellipse(tempX, tempY, letterRadius, letterRadius);
                                    lineSegments.add(new LineSegment(tempX, tempY, pos - PI / 2, pos + PI / 2, letterRadius));
                                    break;
                                default:
                                    break;
                            }
                            if (currentPart.length() == (vowelIndex + 2)) {
                                if (currentPart.charAt(vowelIndex + 1) == '@') {
                                    ellipse(tempX, tempY, letterRadius * 1.8f, letterRadius * 1.8f);
                                }
                            }
                        }
                    }
                }
                pos -= TWO_PI / currentWordSize;
            }
        }
        strokeWeight(2);
        List<Double> lineLengths = new ArrayList<>();
        stroke(foregroundColor);
        for (int i = 0; i < lineSegments.size(); i++) {
            LineSegment lineI = lineSegments.get(i);
            List<Integer> indexes = new ArrayList<>();
            List<Double> angles = new ArrayList<>();
            for (int j = 0; j < lineSegments.size(); j++) {
                LineSegment lineJ = lineSegments.get(j);
                if (round(lineI.y) == round(lineJ.y) && round(lineI.x) == round(lineJ.x)) {
                    continue;
                }
                boolean b = false;
                for (int k = 0; k < lineLengths.size(); k++) {
                    if (lineLengths.get(k) == hypot(lineI.x - lineJ.x, lineI.y - lineJ.y + lineI.x + lineI.y + lineJ.x + lineJ.y)) {
                        b = true;
                        break;
                    }
                }
                if (b) {
                    continue;
                }
                double angle1 = atan((lineI.y - lineJ.y) / (lineI.x - lineJ.x));
                if (hypot(lineI.x + (cos(angle1) * 20) - lineJ.x, lineI.y + (sin(angle1) * 20) - lineJ.y) > hypot(lineI.x - lineJ.y, lineI.y - lineJ.y)) {
                    angle1 -= PI;
                }
                if (angle1 < 0) {
                    angle1 += TWO_PI;
                }
                if (angle1 < 0) {
                    angle1 += TWO_PI;
                }
                if (angle1 < lineI.arcEnd && angle1 > lineI.arcBegin) {
                    angle1 -= PI;
                    if (angle1 < 0) {
                        angle1 += TWO_PI;
                    }
                    if (angle1 < lineJ.arcEnd && angle1 > lineJ.arcBegin) {
                        indexes.add(j);
                        angles.add(angle1);
                    }
                }
            }
            if (indexes.size() == 0) {
                double a = lerp(lineI.arcBegin, lineI.arcEnd, random());
                double d = 0;
                double tempX = lineI.x + cos(a) * d;
                double tempY = lineI.y + sin(a) * d;
                while (pow(tempX - 0, 2) + pow(tempY - 0, 2) < pow(overallRadius + 20, 2)) {
                    tempX = lineI.x + cos(a) * d;
                    tempY = lineI.y + sin(a) * d;
                    d += 1;
                }
                line(lineI.x + cos(a) * lineI.lineRad / 2, lineI.y + sin(a) * lineI.lineRad / 2, tempX, tempY);
            } else {
                int r = (int) floor(random() * indexes.size());
                int j = indexes.get(r);
                LineSegment lineJ = lineSegments.get(j);
                double a = angles.get(r) + PI;
                line(lineI.x + cos(a) * lineI.lineRad / 2, lineI.y + sin(a) * lineI.lineRad / 2, lineJ.x + cos(a + PI) * lineJ.lineRad / 2, lineJ.y + sin(a + PI) * lineJ.lineRad / 2);
                lineLengths.add(hypot(lineI.x - lineJ.x, lineI.y - lineJ.y + lineI.x + lineI.y + lineJ.x + lineJ.y));
                List<LineSegment> keepLineSegments = new ArrayList<>();
                for (int k = 0; k < lineSegments.size(); k++) {
                    if (k != j && k != i) {
                        keepLineSegments.add(lineSegments.get(k));
                    }
                }
                lineSegments.retainAll(keepLineSegments);
                i -= 1;
            }
        }
    }

    public void makeDots(double mX, double mY, double r, int amnt, double pos, double scaleFactor) {
        noStroke();
        fill(foregroundColor);
        if (amnt == 3) {
            ellipse(mX + cos(pos + PI) * r / 1.4f, mY + sin(pos + PI) * r / 1.4f, r / 3 * scaleFactor, r / 3 * scaleFactor);
        }
        ellipse(mX + cos(pos + PI + scaleFactor) * r / 1.4f, mY + sin(pos + PI + scaleFactor) * r / 1.4f, r / 3 * scaleFactor, r / 3 * scaleFactor);
        ellipse(mX + cos(pos + PI - scaleFactor) * r / 1.4f, mY + sin(pos + PI - scaleFactor) * r / 1.4f, r / 3 * scaleFactor, r / 3 * scaleFactor);
        noFill();
        stroke(foregroundColor);
    }

    double double1 = 0;
    double double2 = 0;

    public void makeArcs(double mX, double mY, double nX, double nY, double r1, double r2, double begin, double end) {
        double theta;
        double omega = 0;
        double d = hypot(mX - nX, mY - nY);
        theta = acos((pow(r1, 2) - pow(r2, 2) + pow(d, 2)) / (2 * d * r1));
        if (nX - mX < 0) {
            omega = atan((mY - nY) / (mX - nX));
        } else if (nX - mX > 0) {
            omega = PI + atan((mY - nY) / (mX - nX));
        } else if (nX - mX == 0) {
            if (nY > mY) {
                omega = 3 * PI / 2;
            } else {
                omega = PI / 2;
            }
        }
        if (omega + theta - end > 0) {
            arc(nX, nY, r1 * 2, r1 * 2, omega + theta, end + TWO_PI);
            arc(nX, nY, r1 * 2, r1 * 2, begin + TWO_PI, omega - theta);
        } else {
            arc(nX, nY, r1 * 2, r1 * 2, omega + theta, end);
            arc(nX, nY, r1 * 2, r1 * 2, begin + TWO_PI, omega - theta + TWO_PI);
        }
        if (omega + theta < end || omega - theta > begin) {
            strokeCap(StrokeLineCap.SQUARE);
            stroke(backgroundColor);
            strokeWeight(4);
            // arc(nX, nY, r1*2, r1*2, omega-theta,omega+theta);
            strokeWeight(2);
            stroke(foregroundColor);
            strokeCap(StrokeLineCap.ROUND);
        }
        theta = PI - acos((pow(r2, 2) - pow(r1, 2) + pow(d, 2)) / (2 * d * r2));
        if (nX - mX < 0) {
            omega = atan((mY - nY) / (mX - nX));
        } else if (nX - mX > 0) {
            omega = PI + atan((mY - nY) / (mX - nX));
        } else if (nX - mX == 0) {
            if (nY > mY) {
                omega = 3 * PI / 2;
            } else {
                omega = PI / 2;
            }
        }
        arc(mX, mY, r2 * 2, r2 * 2, omega + theta, omega - theta + TWO_PI);
        double1 = omega + theta;
        double2 = omega - theta + TWO_PI;
    }

    private double map(double value, double rangeStart, double rangeEnd, double targetRangeStart, double targetRangeEnd) {
        double range1 = rangeEnd - rangeStart;
        double range2 = targetRangeEnd - targetRangeStart;
        double valueInRange = (value - rangeStart) / range1;
        return targetRangeStart + (valueInRange * range2);
    }

    private double lerp(double start, double stop, double amt) {
        return start + ((stop - start) * amt);
    }

    private boolean isInFirstSet(char ch) {
        return "b#dfgh".indexOf(ch) >= 0;
    }

    private boolean isInSecondSet(char ch) {
        return "jklmnp".indexOf(ch) >= 0;
    }

    private boolean isInThirdSet(char ch) {
        return "t$rsvw".indexOf(ch) >= 0;
    }

    private boolean isInFourthSet(char ch) {
        return "&yz&qx".indexOf(ch) >= 0;
    }

    private boolean isVowel(char ch) {
        return "aeiou".indexOf(ch) >= 0;
    }

    private boolean isPunctuation(char ch) {
        return ".?!\"'-,;:".indexOf(ch) >= 0;
    }
}
