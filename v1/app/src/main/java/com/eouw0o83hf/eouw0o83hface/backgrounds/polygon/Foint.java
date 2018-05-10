package com.eouw0o83hf.eouw0o83hface.backgrounds.polygon;

// A floating-point representation of a Point
public class Foint {
    public Foint(float x, float y) {
        X = x;
        Y = y;
    }

    public float X;
    public float Y;

    @Override
    public boolean equals(Object o) {
        // If you passed in something that's not a Foint, you're gonna have a bad time

        Foint f = (Foint)o;
        return f.X == X && f.Y == Y;
    }
}
