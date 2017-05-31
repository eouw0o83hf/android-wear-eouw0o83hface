package com.eouw0o83hf.eouw0o83hface;

import android.graphics.Color;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class PlaidBackgroundStateManager implements BackgroundShapeManager {

    int sections = 6;
    ArrayList<BackgroundShape> shapes = new ArrayList(sections * sections);

    StateChangeStatus _state = StateChangeStatus.NotInitialized;
    Random random = new Random();

    public Collection<BackgroundShape> getBackgroundShapes() {
        return shapes;
    }

    public void initialize(Rect bounds) {
        if(_state != StateChangeStatus.NotInitialized) {
            return;
        }

        int height = bounds.height();
        int width = bounds.width();

        int heightSection = height / sections;
        int widthSection = width / sections;

        for(int i = 0; i < sections; ++i) {
            for(int j = 0; j < sections; ++j) {
                shapes.add(new BackgroundShape(widthSection * i, heightSection * j, widthSection * (i + 1), heightSection * (j + 1)));
            }
        }

        randomizeCore();
        incrementState(StateChangeStatus.Neutral);
    }

    public void randomize() {
        if(_state == StateChangeStatus.NotInitialized) {
            return;
        }

        randomizeCore();
    }

    private void randomizeCore() {

        float saturation = 0.35f;
        float brightness = 0.76f;

        // Change color
        for (BackgroundShape shape : shapes) {

            float hue = random.nextInt(360);
            float[] hsv = { hue, saturation, brightness };
            int randomColor = Color.HSVToColor(hsv);
            shape.PushColor(randomColor);
        }

        // Shuffle
        for(int i = shapes.size() - 1; i >= 0; --i) {
            int targetIndex = random.nextInt(shapes.size() - 1);
            BackgroundShape holder = shapes.get(targetIndex);
            shapes.set(targetIndex, shapes.get(i));
            shapes.set(i, holder);
        }
    }

    public void incrementState(StateChangeStatus changeStatus) {
        _state = changeStatus;
    }

    public boolean turnOnOrOff() {

        boolean stateRefreshRequired = false;
        boolean activeToggle = _state == StateChangeStatus.TurningOn;
        int switchedCount = 0;
        for(int i = 0 ; i < shapes.size(); ++i) {
            BackgroundShape s = shapes.get(i);
            if(s.GetActive() != activeToggle) {
                s.SetActive(activeToggle);
                ++switchedCount;
                stateRefreshRequired = true;
                if(switchedCount >= sections) {
                    break;
                }
            } else if(i == shapes.size() - 1) {
                incrementState(StateChangeStatus.Neutral);
                break;
            }
        }
        return stateRefreshRequired;
    }
}
