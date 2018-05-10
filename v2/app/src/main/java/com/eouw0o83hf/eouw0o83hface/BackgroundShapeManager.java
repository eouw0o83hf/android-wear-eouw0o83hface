package com.eouw0o83hf.eouw0o83hface;

import android.graphics.Color;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

// This should both manage the StateChangeStatus and the state of each individual
// BackgroundShape. It might be better served as an abstract class in order to provider
// more helpers
public interface BackgroundShapeManager {
    Collection<? extends BackgroundShape> getBackgroundShapes();

    void initialize(Rect bounds);
    void randomize();

    boolean turnOnOrOff();
    void incrementState(StateChangeStatus changeStatus);


    public enum StateChangeStatus {
        NotInitialized,
        Neutral,
        TurningOn,
        TurningOff
    }
}

