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

    private class Foint {
        public Foint(float x, float y) {
            X = x;
            Y = y;
        }

        public float X;
        public float Y;
    }

    private class AbstractBackgroundPolygon implements BackgroundShape {

        private final ArrayList<Foint> _verteces;
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

            sharedPaint.setColor(_color);
            canvas.drawPath(perimiter, sharedPaint);
        }

        public void SetActive(boolean isActive) {
            _isActive = isActive;
        }

        public boolean GetActive() {
            return _isActive;
        }
    }

    private class MyBackgroundSomething implements BackgroundShape {

        private class Polygon {

            public Polygon(ArrayList<Foint> bounds) {
                Bounds = bounds;
            }

            public ArrayList<Foint> Bounds;
        }


        private ArrayList<AbstractBackgroundPolygon> polygonThings = new ArrayList<>(1);
        //private ArrayList<Polygon> polygons = new ArrayList<>(1);

        private final int NumberOfShapes = 6;

        private Random random = new Random();

        public void Initialize() {

            ArrayList<Foint> verteces = new ArrayList<>(Arrays.asList(
                    new Foint(_bounds.left, _bounds.top),
                    new Foint(_bounds.left, _bounds.bottom),
                    new Foint(_bounds.right, _bounds.bottom),
                    new Foint(_bounds.right, _bounds.top)));


//            polygons = new ArrayList<>(1);
//            polygons.add(new Polygon(verteces)));

            polygonThings = new ArrayList<>(1);
            polygonThings.add(new AbstractBackgroundPolygon(verteces));

            for(int targetIndex = 0; targetIndex < NumberOfShapes - 1; ++targetIndex) {

                int realTargetIndex = random.nextInt(polygonThings.size());

                AbstractBackgroundPolygon target = polygonThings.get(realTargetIndex);
//                Polygon target = polygons.get(realTargetIndex);

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

                Log.i("Polygon", "1");
                for (Foint bound : newPolygon1.Bounds) {
                    Log.d("Polygon1 Bounds", "(" + bound.X + ", " + bound.Y + ")");
                }

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

                Log.i("Polygon", "2");
                for (Foint bound : newPolygon2.Bounds) {
                    Log.d("Polygon2 Bounds", "(" + bound.X + ", " + bound.Y + ")");
                }

                polygonThings.remove(realTargetIndex);
                polygonThings.add(new AbstractBackgroundPolygon(newPolygon1.Bounds));
                polygonThings.add(new AbstractBackgroundPolygon(newPolygon2.Bounds));
//                polygons.remove(realTargetIndex);
//                polygons.add(newPolygon1);
//                polygons.add(newPolygon2);
            }
        }

        @Override
        public void Render(Canvas canvas, Paint sharedPaint) {
            if(_bounds == null) {
                return;
            }

            sharedPaint.setColor(Color.BLACK);
            canvas.drawRect(_bounds, sharedPaint);

            Paint.Style oldStyle = sharedPaint.getStyle();
            sharedPaint.setStyle(Paint.Style.FILL);

            //int[] colors = new int[] { Color.CYAN, Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.GRAY };
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

            for(int j = 0; j < polygonThings.size(); ++j) {
                AbstractBackgroundPolygon shape = polygonThings.get(j);
                ArrayList<Foint> verteces = shape.GetVerteces();
                Path myPath = new Path();

                myPath.moveTo(verteces.get(0).X, verteces.get(0).Y);
                for(int i = 1; i < verteces.size(); ++i) {
                    myPath.lineTo(verteces.get(i).X, verteces.get(i).Y);
                }
                myPath.lineTo(verteces.get(0).X, verteces.get(0).Y);

                sharedPaint.setColor(colors[j]);
                canvas.drawPath(myPath, sharedPaint);
            }

//            Path myPath = new Path();
//            myPath.moveTo(_bounds.left, _bounds.top);
//            myPath.lineTo(_bounds.right, _bounds.bottom);
//            myPath.lineTo(_bounds.right, _bounds.top);
//            myPath.lineTo(_bounds.left, _bounds.top);
//
//            sharedPaint.setColor(Color.CYAN);
//            sharedPaint.setStyle(Paint.Style.FILL);
//            canvas.drawPath(myPath, sharedPaint);
//            sharedPaint.setStyle(oldStyle);
//
//            myPath.reset();
//            myPath.moveTo(_bounds.left, _bounds.top);
//            myPath.lineTo(_bounds.right, _bounds.bottom);
//            myPath.lineTo(_bounds.left, _bounds.bottom);
//            myPath.lineTo(_bounds.left, _bounds.top);
//
//            sharedPaint.setColor(Color.RED);
//            canvas.drawPath(myPath, shared  Paint);
            sharedPaint.setStyle(oldStyle);


//            sharedPaint.setColor(Color.YELLOW);
//            float originalWidth = sharedPaint.getStrokeWidth();
//            sharedPaint.setStrokeWidth(4);
//            canvas.drawLine(_bounds.left, _bounds.top, _bounds.right, _bounds.bottom, sharedPaint);
//            sharedPaint.setStrokeWidth(originalWidth);
        }

        private boolean IsActive = true;

        @Override
        public boolean GetActive() {
            return IsActive;
        }

        public void SetActive(boolean isActive) {
            IsActive = isActive;
        }
    }

    private MyBackgroundSomething shape = new MyBackgroundSomething();
    private ArrayList<MyBackgroundSomething> shapeList = new ArrayList<>(Arrays.asList(shape));

    @Override
    public Collection<? extends BackgroundShape> getBackgroundShapes() {
        return new ArrayList(shapeList);
    }

    @Override
    public void initialize(Rect bounds) {
        if(_state == StateChangeStatus.NotInitialized) {
            _bounds = bounds;
            shape.Initialize();
            _state = StateChangeStatus.Neutral;
        }
    }

    @Override
    public void randomize() {
        shape.Initialize();
    }

    @Override
    public boolean turnOnOrOff() {


        boolean stateRefreshRequired = false;
        boolean activeToggle = _state == StateChangeStatus.TurningOn;
        for(int i = 0 ; i < shapeList.size(); ++i) {
            MyBackgroundSomething s = shapeList.get(i);
            if(s.GetActive() != activeToggle) {
                s.SetActive(activeToggle);
                stateRefreshRequired = true;
                break;
            } else if(i == shapeList.size() - 1) {
                incrementState(StateChangeStatus.Neutral);
                break;
            }
        }
        return stateRefreshRequired;



//        switch (_state) {
//            case TurningOff:
//                for (MyBackgroundSomething myBackgroundSomething : shapeList) {
//                    myBackgroundSomething.IsActive = false;
//                }
//                break;
//
//            case TurningOn:
//                for (MyBackgroundSomething myBackgroundSomething : shapeList) {
//                    myBackgroundSomething.IsActive = true;
//                }
//                break;
//
//            default:
//                break;
//        }
//
//        return true;
    }

    StateChangeStatus _state = StateChangeStatus.NotInitialized;

    @Override
    public void incrementState(StateChangeStatus changeStatus) {
        _state = changeStatus;
    }
}
