package com.eouw0o83hf.eouw0o83hface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.eouw0o83hf.eouw0o83hface.polygon.PolygonsBackgroundShapeManager;

import static com.eouw0o83hf.eouw0o83hface.DeterministicStateManager.VisualState.Ambient;
import static com.eouw0o83hf.eouw0o83hface.DeterministicStateManager.VisualState.Interactive;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 * <p>
 * Important Note: Because watch face apps do not have a default Activity in
 * their project, you will need to set your Configurations to
 * "Do not launch Activity" for both the Wear and/or Application modules. If you
 * are unsure how to do this, please review the "Run Starter project" section
 * in the Google Watch Face Code Lab:
 * https://codelabs.developers.google.com/codelabs/watchface/index.html#0
 */
public class eouw0o83hface extends CanvasWatchFaceService {
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. Defaults to one second
     * because the watch face needs to update seconds in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    BackgroundShapeManager shapeManager = new PolygonsBackgroundShapeManager();

    @Override
    public Engine onCreateEngine() {
        return new Engine();
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

    private class Engine extends CanvasWatchFaceService.Engine {

        private final Handler mUpdateTimeHandler = new EngineHandler(this);
        private Calendar mCalendar;
        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };

        final DeterministicStateManager mStateManager = new DeterministicStateManager(shapeManager) {
            @Override
            protected void ChangeModeTo(VisualState state) {
                VisualState from = GetState();
                super.ChangeModeTo(state);
                HandleStateChange(from, GetState());
            }
        };

        private boolean mRegisteredTimeZoneReceiver = false;
        Time mTime;
        private float mXOffset;
        private float mYOffset;
        private Paint mBackgroundPaint;
        private Paint mTextPaint;
        private Paint mDatePaint;
        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;
        private boolean mAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(eouw0o83hface.this)
                    .setAcceptsTapEvents(true)
                    .build());

            mCalendar = Calendar.getInstance();

            Resources resources = eouw0o83hface.this.getResources();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);

            // Initializes background.
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(
                    ContextCompat.getColor(getApplicationContext(), R.color.background));

            mTextPaint = new Paint();
            mTextPaint = createTextPaint(resources.getColor(R.color.digital_text));
            mTextPaint.setTypeface(NORMAL_TYPEFACE);
            mTextPaint.setAntiAlias(true);
            mTextPaint.setTextAlign(Paint.Align.CENTER);

            mDatePaint = new Paint();
            mDatePaint = createTextPaint(resources.getColor(R.color.digital_text));
            mDatePaint.setTypeface(NORMAL_TYPEFACE);
            mDatePaint.setAntiAlias(true);
            mDatePaint.setTextAlign(Paint.Align.CENTER);

            mTime = new Time();
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
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
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
//
//            mAmbient = inAmbientMode;
//            if (mLowBitAmbient) {
//                mTextPaint.setAntiAlias(!inAmbientMode);
//            }
//
//            // Whether the timer should be running depends on whether we're visible (as well as
//            // whether we're in ambient mode), so we may need to start or stop the timer.
//            updateTimer();
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
                    shapeManager.randomize();
                    break;

                case LeavingInteractive:
                    shapeManager.incrementState(BackgroundShapeManager.StateChangeStatus.TurningOff);
                    break;

                case EnteringInteractive:
                    shapeManager.incrementState(BackgroundShapeManager.StateChangeStatus.TurningOn);
                    break;

                default:
                    break;
            }


            invalidate();
            updateTimer();
        }

        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    // TODO: Add code to handle the tap gesture.
                    Toast.makeText(getApplicationContext(), R.string.message, Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {



            shapeManager.initialize(bounds);
            boolean stateRefreshRequired = false;

            switch (mStateManager.GetState()) {
                case LeavingInteractive:
                case EnteringInteractive:
                    stateRefreshRequired = shapeManager.turnOnOrOff();
                    break;

                default:
                    break;
            }

            // Draw the background.
            if(mStateManager.IsInAmbientMode()) {
//                mBackgroundPaint.setShader(null);
                Log.d("state", "ambient");
                mBackgroundPaint.setColor(Color.BLACK);
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            } else {
                Log.d("state", "active");

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


                for (BackgroundShape shape : shapeManager.getBackgroundShapes()) {
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
//            mTextPaint.setColor(mStateManager.IsInAmbientMode() ? Color.WHITE : Color.LTGRAY);
//            mDatePaint.setColor(mStateManager.IsInAmbientMode() ? Color.WHITE : Color.LTGRAY);

            mTextPaint.setColor(Color.WHITE);
            mDatePaint.setColor(Color.WHITE);

            mTextPaint.setFakeBoldText(mStateManager.GetState() == Interactive);
            mDatePaint.setFakeBoldText(mStateManager.GetState() == Interactive);

            canvas.drawText(text, mXOffset, mYOffset, mTextPaint);
            canvas.drawText(dayText, mXOffset, mYOffset / 2, mDatePaint);








//
//
//            // Draw the background.
//            if (isInAmbientMode()) {
//                canvas.drawColor(Color.BLACK);
//            } else {
//                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
//            }
//
//            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
//            long now = System.currentTimeMillis();
//            mCalendar.setTimeInMillis(now);
//
//            String text = mAmbient
//                    ? String.format("%d:%02d", mCalendar.get(Calendar.HOUR),
//                    mCalendar.get(Calendar.MINUTE))
//                    : String.format("%d:%02d:%02d", mCalendar.get(Calendar.HOUR),
//                    mCalendar.get(Calendar.MINUTE), mCalendar.get(Calendar.SECOND));
//            canvas.drawText(text, mXOffset, mYOffset, mTextPaint);
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
}
