package com.eouw0o83hf.eouw0o83hface;

import android.graphics.*;

public class BackgroundShape {

    public BackgroundShape(float left, float top, float right, float bottom) {
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

    private int _shapeColor;
    private boolean _isActive;

    public void SetColor(int color) {
        _shapeColor = color;
    }

    public boolean GetActive() {
        return _isActive;
    }

    public void SetActive(boolean isActive) {
        _isActive = isActive;
    }

    public void Render(Canvas canvas, Paint sharedPaint) {
        sharedPaint.setColor(_isActive ? _shapeColor : Color.BLACK);
        canvas.drawRect(_left, _top, _right, _bottom, sharedPaint);
    }
}
