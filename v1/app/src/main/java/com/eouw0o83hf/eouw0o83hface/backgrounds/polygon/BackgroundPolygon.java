package com.eouw0o83hf.eouw0o83hface.backgrounds.polygon;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.eouw0o83hf.eouw0o83hface.BackgroundShape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

public class BackgroundPolygon implements BackgroundShape {

    private ArrayList<Foint> _verteces;
    private int _color;
    private boolean _isActive = true;

    private final Random random = new Random();

    public BackgroundPolygon(ArrayList<Foint> verteces, boolean isActive) {
        _verteces = verteces;
        _isActive = isActive;
    }

    public void SetColor(int color) {
        _color = color;
    }

    public ArrayList<Foint> GetVerteces() {
        return _verteces;
    }

    @Override
    public void Render(Canvas canvas, Paint sharedPaint) {
        Path perimeter = new Path();

        perimeter.moveTo(_verteces.get(0).X, _verteces.get(0).Y);
        for(int i = 1; i < _verteces.size(); ++i) {
            perimeter.lineTo(_verteces.get(i).X, _verteces.get(i).Y);
        }
        perimeter.lineTo(_verteces.get(0).X, _verteces.get(0).Y);

        sharedPaint.setColor(_isActive ? _color : Color.BLACK);
        canvas.drawPath(perimeter, sharedPaint);
    }

    public void SetActive(boolean isActive) {
        _isActive = isActive;
    }

    public boolean GetActive() {
        return _isActive;
    }

    // Pulled from http://www.mathopenref.com/coordpolygonarea2.html
    public float Area() {
        float area = 0f;
        int previous = _verteces.size() - 1;

        for(int i = 0; i < _verteces.size(); ++i) {
            area += (_verteces.get(previous).X + _verteces.get(i).X) *
                    (_verteces.get(previous).Y - _verteces.get(i).Y);
            previous = i;
        }

        return area / 2;
    }

    private Foint GetRandomPointOnEdge(int anchorVertexIndex) {

        Foint anchorPoint = _verteces.get(anchorVertexIndex);
        Foint nextPoint = _verteces.get((anchorVertexIndex + 1) % _verteces.size());
        float percent1 = NextBiasedRandom();
        return new Foint(
                anchorPoint.X + ((nextPoint.X - anchorPoint.X) * percent1),
                anchorPoint.Y + ((nextPoint.Y - anchorPoint.Y) * percent1)
        );
    }

    // Splits this polygon into two using a random stright-line slice
    public Collection<BackgroundPolygon> Split() {
        // Randomly select two sides of the polygon to run
        // a split through. This first section just determines anchor
        // verteces to use
        int edge1 = random.nextInt(_verteces.size());
        int edge2 = random.nextInt(_verteces.size());

        // Can't allow both sides to be the same side. Rather than
        // while()ing until they're different, just handle the one-
        // off situation. Yes, this makes the randomness effect
        // a little non-homogeneous, but it shouldn't be noticeable.
        if (edge2 == edge1) {
            edge2 = (edge2 + 1) % _verteces.size();
        }

        Foint newPoint1 = GetRandomPointOnEdge(edge1);
        Foint newPoint2 = GetRandomPointOnEdge(edge2);

        return Arrays.asList(
                GetSubPolygon(edge1, edge2, newPoint1, newPoint2),
                GetSubPolygon(edge2, edge1, newPoint2, newPoint1)
        );
    }

    // Generates the sub-polygon of "this" one on one side of the
    // line which transverses edges defined by anchor points edge1Index and
    // edge2Index, and intercepts them at points newPoint1 and newPoint2,
    // respectively
    private BackgroundPolygon GetSubPolygon(int edge1Index, int edge2Index,
                                            Foint newPoint1, Foint newPoint2) {

        ArrayList<Foint> polygon1Foints = new ArrayList<>(1);
        polygon1Foints.add(newPoint1);
        for (int i = (edge1Index + 1) % _verteces.size(); true; i = (i + 1) % _verteces.size()) {
            polygon1Foints.add(_verteces.get(i));
            if (i == edge2Index) {
                break;
            }
        }
        polygon1Foints.add(newPoint2);
        return new BackgroundPolygon(polygon1Foints, _isActive);
    }

    // Generates a random number from 0 to 1 biased toward the middle of the range
    private float NextBiasedRandom() {
        // Quadratic, points at (0,0) (0.5,0.5) (1,0)
        float floatValue = random.nextFloat();

        float quadraticOutput =  (-2 * (floatValue * floatValue)) + (2 * floatValue);
        if(floatValue > 0.5f) {
            // Flip the right half of the curve so it makes it all the way up to (1,1)
            quadraticOutput = 1f - quadraticOutput;
        }

        return quadraticOutput;
    }
}