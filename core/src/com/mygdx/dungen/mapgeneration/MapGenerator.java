package com.mygdx.dungen.mapgeneration;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.dungen.*;
import com.mygdx.dungen.screen.MapGenDebugScreen;
import org.jgrapht.graph.Multigraph;

public class MapGenerator implements Runnable {

    private static final int                MAX_GEN_TRY                     = 25;

    // Threshold used to get the 90th percentile of values we get from normal distribution (0,1)
    private static final float              NORMAL_DISTRIBUTION_THRESHOLD   = 1.65f;
    private static final double             NORMAL_DISTRIBUTION_MEAN        = 0.00;
    private static final double             NORMAL_DISTRIBUTION_SD          = 1.00;

    private static final int                DESIRED_CELL_COUNT              = 400;

    private static final float              MIN_ROOM_COUNT_MULTIPLIER       = 0.030f;
    private static final float              MAX_ROOM_COUNT_MULTIPLIER       = 0.040f;

    private static final int                MIN_CELL_WIDTH                  = 3;
    private static final int                MIN_CELL_HEIGHT                 = 3;
    private static final int                MAX_CELL_WIDTH                  = 5;
    private static final int                MAX_CELL_HEIGHT                 = 5;

    private static final int                MIN_ROOM_WIDTH                  = 6;
    private static final int                MIN_ROOM_HEIGHT                 = 6;
    private static final float              MIN_CELL_RATIO                  = 1.10f;
    private static final float              MAX_CELL_RATIO                  = 1.75f;

    private static final CellOrientation    CELL_ORIENTATION                = CellOrientation.ORIGINAL;

    // Since we work with small numbers in normal distribution,
    // this is used to scale normal distribution to game units
    private static final float              RADIUS_MULTIPLIER               = 8.00f;
    // The radius offset added to the base radius of rooms (diagonal)
    public static final float               ROOM_SPREAD_RADIUS_OFFSET       = 3.00f;
    private static final float              REMAINING_EDGES_MULTIPLIER      = 0.15f;

    private RadiusGenerator radiusGenerator;
    private CellGenerator cellGenerator;
    private OverlappingRectanglesSeparator rectanglesSeparator;
    private GraphGenerator graphGenerator;
    private CorridorGenerator corridorGenerator;

    public MapGenerator() {
        radiusGenerator = new RadiusGenerator();
        cellGenerator = new CellGenerator();
        rectanglesSeparator = new OverlappingRectanglesSeparator();
        graphGenerator = new GraphGenerator();
        corridorGenerator = new CorridorGenerator();
    }

    public GameMap generateMap() throws MapGenException, GameCellException {

        // Generating radiuses
        Array<Radius> radiuses = radiusGenerator.generateRadiuses(DESIRED_CELL_COUNT, MIN_ROOM_COUNT_MULTIPLIER, MAX_ROOM_COUNT_MULTIPLIER, RADIUS_MULTIPLIER, NORMAL_DISTRIBUTION_MEAN, NORMAL_DISTRIBUTION_SD, NORMAL_DISTRIBUTION_THRESHOLD);
        // Generating cells and rooms from radiuses
        Array<GameCell> cells = cellGenerator.generateCellsFromRadiuses(radiuses, MIN_CELL_WIDTH, MIN_CELL_HEIGHT, MAX_CELL_WIDTH, MAX_CELL_HEIGHT, MIN_ROOM_WIDTH, MIN_ROOM_HEIGHT, MIN_CELL_RATIO, MAX_CELL_RATIO, CELL_ORIENTATION);

        // Spreading rooms
        rectanglesSeparator.spreadRooms(cells, ROOM_SPREAD_RADIUS_OFFSET);

        // Moving cells to the center of the spread rooms so they can separate homogeneously
        Vector2 roomsBoundingBoxCenter = new Vector2();
        Rectangle roomsBoundingBox = rectanglesSeparator.findBoundingBox(GameMap.extractRooms(cells));
        roomsBoundingBox.getCenter(roomsBoundingBoxCenter);
        rectanglesSeparator.moveCellsWithOffset(cells, ((int) roomsBoundingBoxCenter.x), ((int) roomsBoundingBoxCenter.y), 1, 1);

        // Separating cells with rooms fixed
        rectanglesSeparator.separateOverlappingRectangles(cells, true);

        /*
        Array<GameCell> cellsX = new Array<>(cells);
        Array<GameCell> cellsY = new Array<>(cells);

        cellsX.sort(Comparator.comparing(Rectangle::getX));
        cellsY.sort(Comparator.comparing(Rectangle::getY));
        cells.sort(Comparator.comparing(Rectangle::getX).thenComparing(Rectangle::getY));

        System.out.println(cellsX);
        System.out.println(cellsY);
        System.out.println(cells);
        */

        // Generating MST + keeping (MSTEdges x REMAINING_EDGES_MULTIPLIER) edges from the triangulation graph
        Multigraph<GameRoom, NoDuplicateEdge> corridorsGraph = graphGenerator.generateMinimalSpanningTree(cells, REMAINING_EDGES_MULTIPLIER);

        // Generating corridors
        Array<Rectangle> corridors = corridorGenerator.generateCorridors(cells, corridorsGraph);

        // Removing cells that don't overlap with corridors
        cells = corridorGenerator.removeUselessCells(cells, corridors);

        return new GameMap(cells, rectanglesSeparator.findBoundingBox(cells), corridorsGraph, corridors);
    }

    @Override
    public void run() {
        int tryCount = 1;

        Gdx.app.log("MapGenerator", "Thread started, generating Game Map");
        GameMap map;

        for (;;) {
            try {
                map = generateMap();
                break;
            } catch (MapGenException | GameCellException exception) {
                if (tryCount > MAX_GEN_TRY) {
                    throw new RuntimeException("Could not create a proper map in " + MAX_GEN_TRY + " tries");
                }

                Gdx.app.log("MapGenerator", "Exception caught (" + exception + "), creating another Map");

                tryCount++;
            }
        }

        Gdx.app.log("MapGenerator", "Map generated in " + tryCount + (tryCount > 1 ? " tries" : " try"));

        setDebugScreen(map);
    }

    private void setDebugScreen(GameMap map) {
        Gdx.app.postRunnable(() -> ((Game) Gdx.app.getApplicationListener()).setScreen(new MapGenDebugScreen(map)));
    }
}
