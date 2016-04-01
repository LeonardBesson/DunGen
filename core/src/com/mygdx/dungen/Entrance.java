package com.mygdx.dungen;

import com.badlogic.gdx.math.Vector2;

/**
 * @author leonard
 *         Date: 16/3/2016
 */
public class Entrance {

    private Vector2 start;
    private Vector2 end;

    public Entrance(Vector2 start, Vector2 end) {
        this.start = start;
        this.end = end;
    }

    public Entrance(float x1, float y1, float x2, float y2) {
        this.start = new Vector2(x1, y1);
        this.end = new Vector2(x2, y2);
    }

    public void set(Vector2 start, Vector2 end) {
        this.start = start;
        this.end = end;
    }

    public void set(float x1, float y1, float x2, float y2) {
        this.start = new Vector2(x1, y1);
        this.end = new Vector2(x2, y2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entrance entrance = (Entrance) o;

        return ((start.equals(entrance.start) && end.equals(entrance.end)) || (start.equals(entrance.end) && end.equals(entrance.start)));
    }

    public Vector2 getStart() {
        return start;
    }

    public void setStart(Vector2 start) {
        this.start = start;
    }

    public void setStart(float x, float y) {
        this.start = new Vector2(x, y);
    }

    public Vector2 getEnd() {
        return end;
    }

    public void setEnd(Vector2 end) {
        this.end = end;
    }

    public void setEnd(float x, float y) {
        this.end = new Vector2(x, y);
    }
}
