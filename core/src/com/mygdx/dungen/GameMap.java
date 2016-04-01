package com.mygdx.dungen;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.mygdx.dungen.mapgeneration.NoDuplicateEdge;
import org.jgrapht.graph.Multigraph;

public class GameMap {

    public Array<GameCell> cells;
    public Array<GameCell> onlyCells;
    public Array<GameRoom> onlyRooms;
    public Rectangle boundingBox;
    public Multigraph<GameRoom, NoDuplicateEdge> minimalSpanningTree;
    public Array<Rectangle> corridors;

    public GameMap(Array<GameCell> cells, Rectangle boundingBox, Multigraph<GameRoom, NoDuplicateEdge> minimalSpanningTree, Array<Rectangle> corridors) {
        this.cells = cells;
        this.boundingBox = boundingBox;
        this.minimalSpanningTree = minimalSpanningTree;
        this.corridors = corridors;
        extractCellsAndRooms();
    }

    public void extractCellsAndRooms() {
        int cellCount = cells.size;
        onlyCells = new Array<>(cellCount);
        onlyRooms = new Array<>(cellCount);

        Gdx.app.log("GameMap", "Extracting rooms and cells from " + cellCount + " cells");

        for (int i = 0; i < cellCount; i++) {
            GameCell cell = cells.get(i);

            if (cell instanceof GameRoom) {
                if (!onlyRooms.contains((GameRoom) cell, false)) onlyRooms.add((GameRoom) cell);
            } else {
                if (!onlyCells.contains(cell, false)) onlyCells.add(cell);
            }
        }

        onlyCells.shrink();
        onlyRooms.shrink();
    }

    public Array<GameCell> getCells() {
        return cells;
    }

    public static Array<GameRoom> extractRooms(Array<GameCell> cells) {
        int cellCount = cells.size;
        Array<GameRoom> rooms = new Array<>(cellCount);

        for (int i = 0; i < cellCount; i++) {
            GameCell cell = cells.get(i);

            if (cell instanceof GameRoom) {
                rooms.add((GameRoom) cell);
            }
        }

        rooms.shrink();
        return rooms;
    }
}
