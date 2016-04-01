package com.mygdx.dungen;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * @author leonard
 *         Date: 11/2/2016
 */
public class GameRoom extends GameCell {

    private Array<Entrance> entrances;

    public GameRoom(float x, float y, float width, float height) {
        super(x, y, width, height);
        entrances = new Array<>();
    }

    public void addEntrance(Entrance entrance) {
        if (entrance != null) entrances.add(entrance);
    }

    public void addEntrance(Vector2 start, Vector2 end) {
        if (start != null && end != null) entrances.add(new Entrance(start, end));
    }

    public void addEntrance(float x1, float y1, float x2, float y2) {
        Entrance entrance = new Entrance(x1, y1, x2, y2);
        entrances.add(entrance);
    }

    public void removeLastEntrance() {
        entrances.removeIndex(entrances.size - 1);
        entrances.shrink();
    }

    public Array<Entrance> getEntrances() {
        return entrances;
    }

    @Override
    public String toString() {
        return "Room: " + super.toString() + " Entrances: " + entrances.size;
    }

    public Array<Entrance> getBottomEntrances() {
        Array<Entrance> bottomEntrances = new Array<>();

        for (int i = 0; i < entrances.size; i++) {
            Entrance entrance = entrances.get(i);

            if (entrance.getStart().y == this.y && entrance.getEnd().y == this.y) {
                bottomEntrances.add(entrance);
            }
        }

        return bottomEntrances;
    }

    public Array<Entrance> getTopEntrances() {
        Array<Entrance> topEntrances = new Array<>();

        float topY = this.y + this.height;

        for (int i = 0; i < entrances.size; i++) {
            Entrance entrance = entrances.get(i);

            if (entrance.getStart().y == topY && entrance.getEnd().y == topY) {
                topEntrances.add(entrance);
            }
        }

        return topEntrances;
    }

    public Array<Entrance> getLeftEntrances() {
        Array<Entrance> leftEntrances = new Array<>();

        for (int i = 0; i < entrances.size; i++) {
            Entrance entrance = entrances.get(i);

            if (entrance.getStart().x == this.x && entrance.getEnd().x == this.x) {
                leftEntrances.add(entrance);
            }
        }

        return leftEntrances;
    }

    public Array<Entrance> getRightEntrances() {
        Array<Entrance> rightEntrances = new Array<>();

        float rightX = this.x + this.width;

        for (int i = 0; i < entrances.size; i++) {
            Entrance entrance = entrances.get(i);

            if (entrance.getStart().x == rightX && entrance.getEnd().x == rightX) {
                rightEntrances.add(entrance);
            }
        }

        return rightEntrances;
    }
}
