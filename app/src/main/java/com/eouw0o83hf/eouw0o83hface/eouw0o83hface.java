/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eouw0o83hf.eouw0o83hface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.lang.ref.WeakReference;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static com.eouw0o83hf.eouw0o83hface.DeterministicStateManager.VisualState.Ambient;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class eouw0o83hface extends CanvasWatchFaceService {
    private static final Typeface NORMAL_TYPEFACE = Typeface.create("sans-serif-thin", Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.MILLISECONDS.toMillis(80);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;


    Random random = new Random();
    float grayControl = 0.5f;

    int sections = 6;
    ArrayList<BackgroundShape> shapes = new ArrayList(sections * sections);
    BackgroundShapeStatus shapeStatus = BackgroundShapeStatus.NotInitialized;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    public enum BackgroundShapeStatus {
        NotInitialized,
        Neutral,
        TurningOn,
        TurningOff
    }

    @SuppressWarnings("deprecation")
    private class Engine extends CanvasWatchFaceService.Engine {
        final Handler mUpdateTimeHandler = new EngineHandler(this);

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };

        //final VisualStateManager mStateManager = new VisualStateManager() {
        final DeterministicStateManager mStateManager = new DeterministicStateManager(shapes) {
            @Override
            protected void ChangeModeTo(VisualState state) {
                VisualState from = GetState();
                super.ChangeModeTo(state);
                HandleStateChange(from, GetState());
            }
        };

        boolean mRegisteredTimeZoneReceiver = false;

        Paint mBackgroundPaint;
        Paint mTextPaint;
        Paint mDatePaint;

        int mGradientTopColor;
        int mGradientBottomColor;

        Time mTime;

        float mXOffset;
        float mYOffset;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(eouw0o83hface.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());
            Resources resources = eouw0o83hface.this.getResources();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(resources.getColor(R.color.digital_background));

            mTextPaint = new Paint();
            mTextPaint = createTextPaint(resources.getColor(R.color.digital_text));
            mTextPaint.setTextAlign(Paint.Align.CENTER);

            mDatePaint = new Paint();
            mDatePaint = createTextPaint(resources.getColor(R.color.digital_text));
            mDatePaint.setTextAlign(Paint.Align.CENTER);

            mGradientTopColor = resources.getColor(R.color.interactive_background_top);
            mGradientBottomColor = resources.getColor(R.color.interactive_background_bottom);

            mTime = new Time();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            eouw0o83hface.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            eouw0o83hface.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = eouw0o83hface.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(R.dimen.digital_text_size);

            mTextPaint.setTextSize(textSize);
            mDatePaint.setTextSize(resources.getDimension(R.dimen.digital_date_size));
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            mStateManager.AmbientModeChanged(inAmbientMode);
        }

        private void HandleStateChange(DeterministicStateManager.VisualState from, DeterministicStateManager.VisualState to) {
            if(from == to) {
                return;
            }

            if(mLowBitAmbient) {
                mTextPaint.setAntiAlias(to != DeterministicStateManager.VisualState.Ambient);
                mDatePaint.setAntiAlias(to != DeterministicStateManager.VisualState.Ambient);
            }

            switch (to) {
                case Ambient:
                    if(shapeStatus != BackgroundShapeStatus.NotInitialized) {
                        RandomizeBackground();
                    }
                    break;

                case LeavingInteractive:
                    shapeStatus = BackgroundShapeStatus.TurningOff;
                    break;

                case EnteringInteractive:
                    shapeStatus = BackgroundShapeStatus.TurningOn;
                    break;

                default:
                    break;
            }


            invalidate();
            updateTimer();
        }


        private void RandomizeBackground() {
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

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {

            boolean stateRefreshRequired = false;

            switch (shapeStatus) {
                case NotInitialized:
                    int height = bounds.height();
                    int width = bounds.width();

                    int heightSection = height / sections;
                    int widthSection = width / sections;

                    for(int i = 0; i < sections; ++i) {
                        for(int j = 0; j < sections; ++j) {
                            shapes.add(new BackgroundShape(widthSection * i, heightSection * j, widthSection * (i + 1), heightSection * (j + 1)));
                        }
                    }

                    RandomizeBackground();

                    shapeStatus = BackgroundShapeStatus.Neutral;
                    break;

                case TurningOn:
                case TurningOff:

                    boolean activeToggle = shapeStatus == BackgroundShapeStatus.TurningOn;
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
                            shapeStatus = BackgroundShapeStatus.Neutral;
                            break;
                        }
                    }
                    break;

                default:
                    break;
            }

            // Draw the background.
            if(mStateManager.IsInAmbientMode()) {
//                mBackgroundPaint.setShader(null);
                mBackgroundPaint.setColor(Color.BLACK);
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            } else {

                //# Original single rolling gradient
//                float[] hsv = new float[3];
//                Color.colorToHSV(mGradientTopColor, hsv);
//                hsv[0] = (hsv[0] + 0.5f) % 360;
//                mGradientTopColor = Color.HSVToColor(hsv);
//
//                mBackgroundPaint.setShader(new LinearGradient(0, 0, 0, bounds.height(), mGradientTopColor, mGradientBottomColor, Shader.TileMode.MIRROR));
//                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);


//                //# Gradient squares
//                int sections = 5;
//                int heightSection = bounds.height() / sections;
//                int widthSection = bounds.width() / sections;
//                for(int i = 0; i < sections; ++i) {
//                    for(int j = 0; j < sections; ++j) {
//                        float[] hsv = new float[3];
//                        Color.colorToHSV(mGradientTopColor, hsv);
//                        hsv[0] = (hsv[0] + 0.5f) % 360;
//                        mGradientTopColor = Color.HSVToColor(hsv);
//
//
//                        mBackgroundPaint.setShader(new LinearGradient(widthSection * i, heightSection * j, widthSection * (i + 1), heightSection * (j + 1), mGradientTopColor, mGradientBottomColor, Shader.TileMode.MIRROR));
//                        canvas.drawRect(widthSection * i, heightSection * j, widthSection * (i + 1), heightSection * (j + 1), mBackgroundPaint);
//                    }
//                }


                for (BackgroundShape shape : shapes) {
                    shape.Render(canvas, mBackgroundPaint);
                }
            }

            if(stateRefreshRequired) {
                mStateManager.RefreshStateFromBackground();
            }

            mTime.setToNow();

            // Goofy modulo math to use AM/PM times and get 0:00 to render as 12:00
            String text = String.format("%d:%02d", ((mTime.hour + 11) % 12) + 1, mTime.minute);
            String dayText = String.format("%s %02d", new DateFormatSymbols().getMonths()[mTime.month].substring(0, 3), mTime.monthDay);

            mXOffset = bounds.width() / 2;
            //mYOffset = bounds.height() / 2;
//            mTextPaint.setColor(mStateManager.IsInAmbientMode() ? Color.WHITE : Color.LTGRAY);
//            mDatePaint.setColor(mStateManager.IsInAmbientMode() ? Color.WHITE : Color.LTGRAY);

            mTextPaint.setColor(Color.WHITE);
            mDatePaint.setColor(Color.WHITE);

//            mTextPaint.setFakeBoldText(!mStateManager.IsInAmbientMode());
//            mDatePaint.setFakeBoldText(!mStateManager.IsInAmbientMode());

            canvas.drawText(text, mXOffset, mYOffset, mTextPaint);
            canvas.drawText(dayText, mXOffset, mYOffset / 2, mDatePaint);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !mStateManager.IsInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<eouw0o83hface.Engine> mWeakReference;

        public EngineHandler(eouw0o83hface.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            eouw0o83hface.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }
}
