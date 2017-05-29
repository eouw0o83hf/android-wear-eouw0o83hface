//package com.eouw0o83hf.eouw0o83hface;
//
//import android.text.format.Time;
//import android.util.Log;
//
//import java.util.Timer;
//import java.util.TimerTask;
//
///**
// * Created by nathanlandis on 10/25/15.
// */
//@SuppressWarnings("deprecation")
//public class VisualStateManager {
//    public enum VisualState {
//        Ambient,
//        EnteringInteractive,
//        Interactive,
//        LeavingInteractive
//    }
//
//    private long _changeTime = System.currentTimeMillis();
//    private static final float _stateChangeMillis = 500;
//    private Timer _currentTimer;
//
//    private VisualState _state = VisualState.Interactive;
//    public VisualState GetState() {
//        return _state;
//    }
//
//    public float PercentageOfTimeInCurrentState() {
//        return (System.currentTimeMillis() - _changeTime) / _stateChangeMillis;
//    }
//
//    protected void ChangeModeTo(VisualState state) {
//        Log.d("State changing", _state.name() + " to " + state.name());
//        _state = state;
//        _changeTime = System.currentTimeMillis();
//    }
//
//    public void AmbientModeChanged(Boolean inAmbientMode) {
//        if(_currentTimer != null) {
//            _currentTimer.cancel();
//            _currentTimer.purge();
//        }
//
//        ChangeModeTo(inAmbientMode ? VisualState.LeavingInteractive : VisualState.EnteringInteractive);
//
//        _currentTimer = new Timer();
//        _currentTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                switch (_state) {
//                    case LeavingInteractive:
//                        ChangeModeTo(VisualState.Ambient);
//                        return;
//                    case EnteringInteractive:
//                        ChangeModeTo(VisualState.Interactive);
//                }
//            }
//        }, (int) _stateChangeMillis);
//    }
//
//    public Boolean IsInAmbientMode() {
//        return GetState() == VisualState.Ambient;
//    }
//}
//
//
