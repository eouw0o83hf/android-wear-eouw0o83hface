package com.eouw0o83hf.eouw0o83hface;

import android.graphics.*;

public interface BackgroundShape {
    void Render(Canvas canvas, Paint sharedPaint);
    boolean GetActive();
}

