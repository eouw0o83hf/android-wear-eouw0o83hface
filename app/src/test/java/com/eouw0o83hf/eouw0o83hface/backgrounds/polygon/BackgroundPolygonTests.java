package com.eouw0o83hf.eouw0o83hface.backgrounds.polygon;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class BackgroundPolygonTests extends TestCase {

    protected void setUp() {
    }

    protected void tearDown() {
    }

    public void test_Area_SimpleConvexShape() {
        // A 1x1 square
        ArrayList<Foint> verteces = new ArrayList<>(Arrays.asList(
                new Foint(0, 0),
                new Foint(0, 1),
                new Foint(1, 1),
                new Foint(1, 0)
        ));

        BackgroundPolygon sut = new BackgroundPolygon(verteces, true);
        assertEquals(1f, sut.Area());
    }

    public void test_Area_ConcaveShape() {
        // A 1x1 square with a 0.25-area triangle cut out of it
        ArrayList<Foint> verteces = new ArrayList<>(Arrays.asList(
                new Foint(0, 0),
                new Foint(0, 1),
                new Foint(1, 1),
                new Foint(0.5f, 0.5f),
                new Foint(1, 0)
        ));

        BackgroundPolygon sut = new BackgroundPolygon(verteces, true);
        assertEquals(0.15f, sut.Area());
    }

    public void test_Split_DoesNotDestroyTheWorld() {
        // A 1x1 square
        ArrayList<Foint> verteces = new ArrayList<>(Arrays.asList(
                new Foint(0, 0),
                new Foint(0, 1),
                new Foint(1, 1),
                new Foint(1, 0)
        ));

        BackgroundPolygon sut = new BackgroundPolygon(verteces, true);
        Collection<BackgroundPolygon> splits = sut.Split();

        ArrayList<Foint> otherFoints = new ArrayList<>(0);
        for (BackgroundPolygon split : splits) {
            split.GetVerteces();
        }
    }
}
