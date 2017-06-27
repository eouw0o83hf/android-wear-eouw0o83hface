package com.eouw0o83hf.eouw0o83hface.backgrounds.polygon;

import android.graphics.Rect;

import com.eouw0o83hf.eouw0o83hface.BackgroundShape;
import com.eouw0o83hf.eouw0o83hface.BackgroundShapeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

public class PolygonsBackgroundShapeManager implements BackgroundShapeManager {

    public void Initialize(boolean initialLoad) {

        ArrayList<Foint> verteces = new ArrayList<>(Arrays.asList(
                new Foint(_bounds.left, _bounds.top),
                new Foint(_bounds.left, _bounds.bottom),
                new Foint(_bounds.right, _bounds.bottom),
                new Foint(_bounds.right, _bounds.top)));

        // We need to do a reference swap for the refresh so that we don't
        // encounter a write/read race condition between rendering and refreshing
        ArrayList<BackgroundPolygon> updatedPolygons = new ArrayList<>(1);

        // Start with a single polygon which encompasses the entirety of the canvas
        updatedPolygons.add(new BackgroundPolygon(verteces, initialLoad));

        int nextShapeCount = nextNumberOfShapes();

        // Find the largest polygon by area and
        // split it into two smaller polygons. Replace the original one in the list
        // with the two sub-shapes instead. Do this until we've filled the list.
        while (updatedPolygons.size() < nextShapeCount - 1) {

            // It'd be really nice if Java had some sort of linq syntax...
            // This just finds the
            int targetIndex = 0;
            float maxArea = 0;
            for (int i = 0; i < updatedPolygons.size(); ++i) {
                float currentArea = updatedPolygons.get(i).Area();
                if (currentArea > maxArea) {
                    targetIndex = i;
                    maxArea = currentArea;
                }
            }

            // Remove the original entry, split into two and re-add
            BackgroundPolygon targetPolygon = updatedPolygons.get(targetIndex);
            updatedPolygons.remove(targetIndex);
            updatedPolygons.addAll(targetPolygon.Split());
        }

        int[] colors = new int[]{
                0xFF2196F3,
                0xFF8BC34A,
                0xFFFF5722,
                0xFF9C27B0,
                0xFF673AB7,
                0xFF3F51B5,
                0xFFFF9800,
                0xFF607D8B
        };

        // Set colors now that the population is solidified
        for (int i = 0; i < updatedPolygons.size(); ++i) {
            updatedPolygons.get(i).SetColor(colors[i]);
        }
        backgroundPolygons = updatedPolygons;
    }

    private ArrayList<BackgroundPolygon> backgroundPolygons = new ArrayList<>(1);

    // Need to get this from the running activity. THere may be some way to
    // pull it in with the constructor, but it always gets passed in with a
    // render() call. So, just set it the first time we know what it is, and
    // ignore some stuff if it hasn't been set yet.
    Rect _bounds = null;

    private final int MaxNumberOfShapes = 10;
    private final int MinNumberOfShapes = 4;

    // Gets a random number of next shapes to generate
    private int nextNumberOfShapes() {
        return random.nextInt(MaxNumberOfShapes - MinNumberOfShapes) + MinNumberOfShapes;
    }

    private Random random = new Random();

    private float nextBiasedRandom() {
        // Quadratic, points at (0,0) (0.5,0.5) (1,0)
        float floatValue = random.nextFloat();

        float quadraticOutput = (-2 * (floatValue * floatValue)) + (2 * floatValue);
        if (floatValue > 0.5f) {
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
        if (_state == StateChangeStatus.NotInitialized) {
            _bounds = bounds;
            Initialize(true);
            _state = StateChangeStatus.Neutral;
        }
    }

    @Override
    public void randomize() {
        Initialize(false);
    }

    @Override
    public boolean turnOnOrOff() {

        boolean stateRefreshRequired = false;
        boolean activeToggle = _state == StateChangeStatus.TurningOn;

        // Make a local in case the underlying collection changes during the
        // evaluation. This is unlikely but conceivable since we're working
        // in an event-based paradigm, and the collection is updated in a
        // different event chain.
        ArrayList<BackgroundPolygon> localPolygons = new ArrayList<>(backgroundPolygons);

        for (int i = 0; i < localPolygons.size(); ++i) {
            BackgroundPolygon s = localPolygons.get(i);
            if (s.GetActive() != activeToggle) {
                s.SetActive(activeToggle);
                stateRefreshRequired = true;
                break;
            } else if (i == localPolygons.size() - 1) {
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
