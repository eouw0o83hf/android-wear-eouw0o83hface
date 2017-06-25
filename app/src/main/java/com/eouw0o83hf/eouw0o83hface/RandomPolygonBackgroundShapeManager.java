package com.eouw0o83hf.eouw0o83hface;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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

    // An abstraction of a polygon, as defined by the collection
    // of points which mark its verteces
    private class Polygon {
        public Polygon(ArrayList<Foint> bounds) {
            Bounds = bounds;
        }
        public ArrayList<Foint> Bounds;
    }

    // TODO rename because "Abstract" is great as the artistic word
    // but a bad name for this class
    // The actual Shape implementation.
    private class AbstractBackgroundPolygon implements BackgroundShape {

        private ArrayList<Foint> _verteces;
        private int _color;
        private boolean _isActive = true;

        public AbstractBackgroundPolygon(ArrayList<Foint> verteces) {
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
    }

    public void Initialize() {

        ArrayList<Foint> verteces = new ArrayList<>(Arrays.asList(
                new Foint(_bounds.left, _bounds.top),
                new Foint(_bounds.left, _bounds.bottom),
                new Foint(_bounds.right, _bounds.bottom),
                new Foint(_bounds.right, _bounds.top)));

        ArrayList<AbstractBackgroundPolygon> updatedPolygonThings = new ArrayList<>(1);
        updatedPolygonThings.add(new AbstractBackgroundPolygon(verteces));

        for(int targetIndex = 0; targetIndex < NumberOfShapes - 1; ++targetIndex) {

            int realTargetIndex = random.nextInt(updatedPolygonThings.size());

            AbstractBackgroundPolygon target = updatedPolygonThings.get(realTargetIndex);

            ArrayList<Foint> targetVerteces = target.GetVerteces();
            int edge1 = random.nextInt(targetVerteces.size());
            int edge2 = random.nextInt(targetVerteces.size());
            if (edge2 == edge1) {
                edge2 = (edge2 + 1) % targetVerteces.size();
            }

            Foint edge1Point1 = targetVerteces.get(edge1);
            Foint edge1Point2 = targetVerteces.get((edge1 + 1) % targetVerteces.size());
            Foint edge2Point1 = targetVerteces.get(edge2);
            Foint edge2Point2 = targetVerteces.get((edge2 + 1) % targetVerteces.size());

            // TODO At least one of these needs to be biased toward the middle of the
            // line (middle of the bounds?) , otherwise randomness winds up working
            // against the design and clumping all of the shapes off of the watchface

            float percent1 = random.nextFloat();
            Foint newPoint1 = new Foint(
                    edge1Point1.X + ((edge1Point2.X - edge1Point1.X) * percent1),
                    edge1Point1.Y + ((edge1Point2.Y - edge1Point1.Y) * percent1)
            );

            float percent2 = random.nextFloat();
            Foint newPoint2 = new Foint(
                    edge2Point1.X + ((edge2Point2.X - edge2Point1.X) * percent2),
                    edge2Point1.Y + ((edge2Point2.Y - edge2Point1.Y) * percent2)
            );

            ArrayList<Foint> polygon1Foints = new ArrayList<>(1);
            polygon1Foints.add(newPoint1);
            for (int i = (edge1 + 1) % targetVerteces.size(); true; i = (i + 1) % targetVerteces.size()) {
                polygon1Foints.add(targetVerteces.get(i));
                if (i == edge2) {
                    break;
                }
            }
            polygon1Foints.add(newPoint2);
            Polygon newPolygon1 = new Polygon(polygon1Foints);

            ArrayList<Foint> polygon2Foints = new ArrayList<>(1);
            polygon2Foints.add(newPoint2);
            for (int i = (edge2 + 1) % targetVerteces.size(); true; i = (i + 1) % targetVerteces.size()) {
                polygon2Foints.add(targetVerteces.get(i));
                if (i == edge1) {
                    break;
                }
            }
            polygon2Foints.add(newPoint1);
            Polygon newPolygon2 = new Polygon(polygon2Foints);


            updatedPolygonThings.remove(realTargetIndex);
            updatedPolygonThings.add(new AbstractBackgroundPolygon(newPolygon1.Bounds));
            updatedPolygonThings.add(new AbstractBackgroundPolygon(newPolygon2.Bounds));
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
        for(int i = 0; i < updatedPolygonThings.size(); ++i) {
            // Reverse them so rendering goes small-to-large
            AbstractBackgroundPolygon source = updatedPolygonThings.get(updatedPolygonThings.size() - i - 1);

            if(polygonThings.size() <= i) {
                polygonThings.add(new AbstractBackgroundPolygon(source.GetVerteces()));
            } else {
                polygonThings.get(i).SetVerteces(source.GetVerteces());
            }

            polygonThings.get(i).SetColor(colors[i]);
        }
    }

    private ArrayList<AbstractBackgroundPolygon> polygonThings = new ArrayList<>(1);
    private final int NumberOfShapes = 6;
    private Random random = new Random();

    @Override
    public Collection<? extends BackgroundShape> getBackgroundShapes() {
        return polygonThings;
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

        for(int i = 0 ; i < polygonThings.size(); ++i) {
            AbstractBackgroundPolygon s = polygonThings.get(i);
            if(s.GetActive() != activeToggle) {
                s.SetActive(activeToggle);
                stateRefreshRequired = true;
                break;
            } else if(i == polygonThings.size() - 1) {
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
