package com.eouw0o83hf.eouw0o83hface;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

public class RandomPolygonBackgroundShapeManager implements BackgroundShapeManager {

    Rect _bounds = null;

    // A floating-point representation of a Point
    private class Foint {
        public Foint(float x, float y) {
            X = x;
            Y = y;
        }

        public float X;
        public float Y;
    }

    // The actual Shape implementation
    private class BackgroundPolygon implements BackgroundShape {

        private ArrayList<Foint> _verteces;
        private int _color;
        private boolean _isActive = true;

        public BackgroundPolygon(ArrayList<Foint> verteces) {
            _verteces = verteces;
        }

        public void SetColor(int color) {
            _color = color;
        }

        public ArrayList<Foint> GetVerteces() {
            return _verteces;
        }

        public void SetVerteces(ArrayList<Foint> verteces) {
            _verteces = verteces;
        }

        @Override
        public void Render(Canvas canvas, Paint sharedPaint) {
            if(_bounds == null) {
                return;
            }

            Path perimiter = new Path();

            perimiter.moveTo(_verteces.get(0).X, _verteces.get(0).Y);
            for(int i = 1; i < _verteces.size(); ++i) {
                perimiter.lineTo(_verteces.get(i).X, _verteces.get(i).Y);
            }
            perimiter.lineTo(_verteces.get(0).X, _verteces.get(0).Y);

            sharedPaint.setColor(_isActive ? _color : Color.BLACK);
            canvas.drawPath(perimiter, sharedPaint);
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

        public Foint GetRandomPointOnEdge(int anchorVertexIndex) {

            Foint anchorPoint = _verteces.get(anchorVertexIndex);
            Foint nextPoint = _verteces.get((anchorVertexIndex + 1) % _verteces.size());
            float percent1 = nextBiasedRandom();
            return new Foint(
                    anchorPoint.X + ((nextPoint.X - anchorPoint.X) * percent1),
                    anchorPoint.Y + ((nextPoint.Y - anchorPoint.Y) * percent1)
            );
        }

//        // Splits this polygon into two along the line denoted by
//        // the given points
//        public ArrayList<Foint> Split(Foint point1, Foint point2) {
//
//        }
    }

    public void Initialize() {

        ArrayList<Foint> verteces = new ArrayList<>(Arrays.asList(
                new Foint(_bounds.left, _bounds.top),
                new Foint(_bounds.left, _bounds.bottom),
                new Foint(_bounds.right, _bounds.bottom),
                new Foint(_bounds.right, _bounds.top)));

        // We need to do a reference swap for the refresh so that we don't
        // encounter a write/read race condition between rendering and refreshing
        ArrayList<BackgroundPolygon> updatedPolygons = new ArrayList<>(1);

        // Start with a single polygon which encompasses the entirety of the canvas
        updatedPolygons.add(new BackgroundPolygon(verteces));

        // Find the largest polygon by area and
        // split it into two smaller polygons. Replace the original one in the list
        // with the two sub-shapes instead. Do this until we've filled the list.
        while(updatedPolygons.size() < NumberOfShapes - 1) {

            Log.i("Updating polygon", "#" + updatedPolygons.size());

            // It'd be really nice if Java had some sort of linq syntax...
            // This just finds the
            int targetIndex = 0;
            float maxArea = 0;
            for(int i = 0; i < updatedPolygons.size(); ++i) {
                float currentArea = updatedPolygons.get(i).Area();
                if(currentArea > maxArea) {
                    targetIndex = i;
                    maxArea = currentArea;
                }
            }

            BackgroundPolygon targetPolygon = updatedPolygons.get(targetIndex);

            // Randomly select two sides of the target polygon to run
            // a split through. This first section just determines anchor
            // verteces to use
            ArrayList<Foint> targetVerteces = targetPolygon.GetVerteces();
            int edge1 = random.nextInt(targetVerteces.size());
            int edge2 = random.nextInt(targetVerteces.size());
            // Can't allow both sides to be the same side. Rather than
            // while()ing until they're different, just handle the one-
            // off situation. Yes, this makes the randomness effect
            // a little non-homogeneous, but it shouldn't be noticeable.
            if (edge2 == edge1) {
                edge2 = (edge2 + 1) % targetVerteces.size();
            }

            Foint newPoint1 = targetPolygon.GetRandomPointOnEdge(edge1);
            Foint newPoint2 = targetPolygon.GetRandomPointOnEdge(edge2);

            ArrayList<Foint> polygon1Foints = new ArrayList<>(1);
            polygon1Foints.add(newPoint1);
            for (int i = (edge1 + 1) % targetVerteces.size(); true; i = (i + 1) % targetVerteces.size()) {
                polygon1Foints.add(targetVerteces.get(i));
                if (i == edge2) {
                    break;
                }
            }
            polygon1Foints.add(newPoint2);

            ArrayList<Foint> polygon2Foints = new ArrayList<>(1);
            polygon2Foints.add(newPoint2);
            for (int i = (edge2 + 1) % targetVerteces.size(); true; i = (i + 1) % targetVerteces.size()) {
                polygon2Foints.add(targetVerteces.get(i));
                if (i == edge1) {
                    break;
                }
            }
            polygon2Foints.add(newPoint1);

            updatedPolygons.remove(targetIndex);
            updatedPolygons.add(new BackgroundPolygon(polygon1Foints));
            updatedPolygons.add(new BackgroundPolygon(polygon2Foints));
        }

        int[] colors = new int[] {
                0xFF2196F3,
                0xFF8BC34A,
                0xFFFF5722,
                0xFF9C27B0,
                0xFF673AB7,
                0xFF3F51B5,
                0xFFFF9800,
                0xFF607D8B
        };

        for(int i = 0; i < updatedPolygons.size(); ++i) {
            // Reverse them so rendering goes small-to-large
            BackgroundPolygon source = updatedPolygons.get(updatedPolygons.size() - i - 1);

            if(backgroundPolygons.size() <= i) {
                backgroundPolygons.add(new BackgroundPolygon(source.GetVerteces()));
            } else {
                backgroundPolygons.get(i).SetVerteces(source.GetVerteces());
            }

            backgroundPolygons.get(i).SetColor(colors[i]);
        }
    }

    private ArrayList<BackgroundPolygon> backgroundPolygons = new ArrayList<>(1);
    private final int NumberOfShapes = 8;

    private Random random = new Random();
    private float nextBiasedRandom() {
        // Quadratic, points at (0,0) (0.5,0.5) (1,0)
        float floatValue = random.nextFloat();

        float quadraticOutput =  (-2 * (floatValue * floatValue)) + (2 * floatValue);
        if(floatValue > 0.5f) {
            // Flip the right half of the curve so it makes it all the way up to (1,1)
            quadraticOutput = 1f - quadraticOutput;
        }

        return quadraticOutput;
    }

    @Override
    public Collection<? extends BackgroundShape> getBackgroundShapes() {
        return backgroundPolygons;
    }

    @Override
    public void initialize(Rect bounds) {
        if(_state == StateChangeStatus.NotInitialized) {
            _bounds = bounds;
            Initialize();
            _state = StateChangeStatus.Neutral;
        }
    }

    @Override
    public void randomize() {
        Initialize();
    }

    @Override
    public boolean turnOnOrOff() {

        boolean stateRefreshRequired = false;
        boolean activeToggle = _state == StateChangeStatus.TurningOn;

        for(int i = 0; i < backgroundPolygons.size(); ++i) {
            BackgroundPolygon s = backgroundPolygons.get(i);
            if(s.GetActive() != activeToggle) {
                s.SetActive(activeToggle);
                stateRefreshRequired = true;
                break;
            } else if(i == backgroundPolygons.size() - 1) {
                incrementState(StateChangeStatus.Neutral);
                break;
            }
        }

        return stateRefreshRequired;
    }

    StateChangeStatus _state = StateChangeStatus.NotInitialized;

    @Override
    public void incrementState(StateChangeStatus changeStatus) {
        _state = changeStatus;
    }
}
