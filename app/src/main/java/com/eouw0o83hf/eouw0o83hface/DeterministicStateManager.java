package com.eouw0o83hf.eouw0o83hface;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

public class DeterministicStateManager {
    public enum VisualState {
        Ambient,
        EnteringInteractive,
        Interactive,
        LeavingInteractive
    }

    private final Collection<? extends BackgroundShape> _background;

    public DeterministicStateManager(Collection<? extends BackgroundShape> background) {
        _background = background;
        _state = VisualState.Interactive;
    }

    private VisualState _state;
    public VisualState GetState() {
        return _state;
    }

    protected void ChangeModeTo(VisualState state) {
        _state = state;
    }

    public void AmbientModeChanged(Boolean inAmbientMode) {
        ChangeModeTo(inAmbientMode ? VisualState.LeavingInteractive : VisualState.EnteringInteractive);
    }

    public Boolean IsInAmbientMode() {
        return GetState() == VisualState.Ambient;
    }

    public void RefreshStateFromBackground() {

        switch (_state) {
            // Static states
            case Interactive:
            case Ambient:
                return;

            // Transitional states
            default:
                break;
        }

        // Boolean because null means something here
        Boolean referenceValue = null;
        boolean allEqual = true;
        for (BackgroundShape backgroundShape : _background) {
            if(referenceValue == null) {
                referenceValue = backgroundShape.GetActive();
            } else if(backgroundShape.GetActive() != referenceValue.booleanValue()) {
                allEqual = false;
                break;
            }
        }

        // If the initialization takes longer than a frame to spin up
        if(referenceValue == null) {
            referenceValue = false;
        }

        if(!allEqual) {
            return;
        } else if(referenceValue) {
            ChangeModeTo(VisualState.Interactive);
        } else {
            ChangeModeTo(VisualState.Ambient);
        }
    }
}
