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

    private class MyBackgroundSomething implements BackgroundShape {

        private class Polygon {

            public Polygon(ArrayList<Foint> bounds) {
                Bounds = bounds;
            }

            public ArrayList<Foint> Bounds;
        }


        private ArrayList<Polygon> polygons = new ArrayList<>(1);


        private class Foint {
            public Foint(float x, float y) {
                X = x;
                Y = y;
            }

            public float X;
            public float Y;
        }
        private Random random = new Random();

        public void Initialize() {

            polygons = new ArrayList<>(1);
            polygons.add(new Polygon(new ArrayList<>(Arrays.asList(
                    new Foint(_bounds.left, _bounds.top),
                    new Foint(_bounds.left, _bounds.bottom),
                    new Foint(_bounds.right, _bounds.bottom),
                    new Foint(_bounds.right, _bounds.top)))));

            for(int targetIndex = 0; targetIndex < 3; ++targetIndex) {

                Polygon target = polygons.get(targetIndex);
                int edge1 = random.nextInt(target.Bounds.size());
                int edge2 = random.nextInt(target.Bounds.size());
                if (edge2 == edge1) {
                    edge2 = (edge2 + 1) % target.Bounds.size();
                }

                Foint edge1Point1 = target.Bounds.get(edge1);
                Foint edge1Point2 = target.Bounds.get((edge1 + 1) % target.Bounds.size());
                Foint edge2Point1 = target.Bounds.get(edge2);
                Foint edge2Point2 = target.Bounds.get((edge2 + 1) % target.Bounds.size());

                float percent1 = random.nextFloat();

                // This needs to use a line formula instead of this shitty thing
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
                for (int i = (edge1 + 1) % target.Bounds.size(); true; i = (i + 1) % target.Bounds.size()) {
                    polygon1Foints.add(target.Bounds.get(i));
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
                for (int i = (edge2 + 1) % target.Bounds.size(); true; i = (i + 1) % target.Bounds.size()) {
                    polygon2Foints.add(target.Bounds.get(i));
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

                polygons.remove(targetIndex);
                polygons.add(newPolygon1);
                polygons.add(newPolygon2);
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

            int[] colors = new int[] { Color.CYAN, Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA };

            for(int j = 0; j < polygons.size(); ++j) {
                Polygon polygon = polygons.get(j);
                Path myPath = new Path();

                myPath.moveTo(polygon.Bounds.get(0).X, polygon.Bounds.get(0).Y);
                for(int i = 1; i < polygon.Bounds.size(); ++i) {
                    myPath.lineTo(polygon.Bounds.get(i).X, polygon.Bounds.get(i).Y);
                }
                myPath.lineTo(polygon.Bounds.get(0).X, polygon.Bounds.get(0).Y);

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

        public boolean IsActive = true;

        @Override
        public boolean GetActive() {
            return IsActive;
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

        switch (_state) {
            case TurningOff:
                for (MyBackgroundSomething myBackgroundSomething : shapeList) {
                    myBackgroundSomething.IsActive = false;
                }
                break;

            case TurningOn:
                for (MyBackgroundSomething myBackgroundSomething : shapeList) {
                    myBackgroundSomething.IsActive = true;
                }
                break;

            default:
                break;
        }

        return true;
    }

    StateChangeStatus _state = StateChangeStatus.NotInitialized;

    @Override
    public void incrementState(StateChangeStatus changeStatus) {
        _state = changeStatus;
    }
}
