package com.eouw0o83hf.eouw0o83hface;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class BackgroundSquare implements BackgroundShape {

    public BackgroundSquare(float left, float top, float right, float bottom) {
        _left = left;
        _top = top;
        _right = right;
        _bottom = bottom;

        SetActive(true);
    }

    private float _left;
    private float _top;
    private float _right;
    private float _bottom;

    private int _shapeColor = 0;
    private int _nextColor = 0;
    private boolean _isActive;

    public void PushColor(int color) {
        if(_shapeColor == 0) {
            _shapeColor = color;
        } else {
            _nextColor = color;
        }
    }

    public boolean GetActive() {
        return _isActive;
    }

    public void SetActive(boolean isActive) {
        _isActive = isActive;
        if(!_isActive && _nextColor != 0) {
            _shapeColor = _nextColor;
        }
    }

    public void Render(Canvas canvas, Paint sharedPaint) {
        sharedPaint.setColor(_isActive ? _shapeColor : Color.BLACK);
        canvas.drawRect(_left, _top, _right, _bottom, sharedPaint);
    }
}
