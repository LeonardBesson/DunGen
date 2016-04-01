package com.mygdx.dungen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.dungen.Entrance;
import com.mygdx.dungen.GameCell;
import com.mygdx.dungen.GameMap;
import com.mygdx.dungen.GameRoom;
import com.mygdx.dungen.mapgeneration.MapGenerator;
import com.mygdx.dungen.mapgeneration.NoDuplicateEdge;
import org.jgrapht.graph.Multigraph;

import java.util.Set;

/**
 * @author leonard
 *         Date: 13/2/2016
 */
public class MapGenDebugRenderer {

    private static final float CAMERA_OFFSET            = 5f;

    private static final int   CIRCLE_SEGMENT_COUNT     = 100;
    private static final Color GRID_COLOR               = new Color(205f, 201f, 201f, .10f);
    private static final Color BOUNDING_BOX_COLOR       = new Color(Color.BROWN);
    private static final Color CELL_GRID_COLOR          = new Color(Color.ROYAL);
    private static final Color ROOM_GRID_COLOR          = new Color(Color.FIREBRICK);
    private static final Color CELL_BORDER_COLOR        = new Color(Color.CLEAR);
    private static final Color ROOM_BORDER_COLOR        = new Color(Color.CLEAR);
    private static final Color ROOM_CIRCLE_COLOR        = new Color(Color.YELLOW);
    private static final Color MIN_SPANNING_TREE_COLOR  = new Color(Color.PINK);
    private static final Color CORRIDOR_GRID_COLOR      = new Color(Color.LIME);
    private static final Color CORRIDOR_BORDER_COLOR    = new Color(Color.CLEAR);
    private static final Color ROOM_ENTRANCE_COLOR      = new Color(Color.WHITE);

    private GameMap map;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private ShapeRenderer shapeRenderer;
    private Rectangle boundingBox;

    public MapGenDebugRenderer(GameMap map) {
        this.map = map;

        boundingBox = map.boundingBox;
        Vector2 boundingBoxCenter = new Vector2();
        boundingBox.getCenter(boundingBoxCenter);

        int boundingBoxWidth = ((int) boundingBox.width);
        int boundingBoxHeight = ((int) boundingBox.height);

        camera = new OrthographicCamera(boundingBoxWidth + CAMERA_OFFSET, boundingBoxHeight + CAMERA_OFFSET);
        camera.position.x = boundingBoxCenter.x;
        camera.position.y = boundingBoxCenter.y;
        camera.update();

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);

        viewport = new FitViewport(boundingBoxWidth, boundingBoxHeight, camera);
    }

    public void render(float delta) {
        camera.update();

        // Enabling blending for ALPHA on grid
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        drawGrid(shapeRenderer);
        drawCells(shapeRenderer);
        drawMinimalSpanningTree(shapeRenderer);
        drawCorridors(shapeRenderer);
        drawEntrances(shapeRenderer);

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawEntrances(ShapeRenderer shapeRenderer) {
        Array<GameRoom> rooms = map.onlyRooms;
        shapeRenderer.setColor(ROOM_ENTRANCE_COLOR);

        for (int i = 0; i < rooms.size; i++) {
            GameRoom room = rooms.get(i);

            Array<Entrance> entrances = room.getEntrances();

            for (int j = 0; j < entrances.size; j++) {
                Entrance entrance = entrances.get(j);
                shapeRenderer.line(entrance.getStart(), entrance.getEnd());
            }
        }
    }

    private void drawGrid(ShapeRenderer shapeRenderer) {
        // Bounding box to int
        int boundingBoxX = ((int) boundingBox.x);
        int boundingBoxY = ((int) boundingBox.y);
        int boundingBoxWidth = ((int) boundingBox.width);
        int boundingBoxHeight = ((int) boundingBox.height);

        shapeRenderer.setColor(GRID_COLOR);

        // Drawing x lines
        for (int i = boundingBoxX; i <= (boundingBoxX + boundingBoxWidth) ; i++) {
            shapeRenderer.line(i, boundingBoxY, i, boundingBoxY + boundingBoxHeight);
        }
        // Drawing y lines
        for (int j = boundingBoxY; j <= (boundingBoxY + boundingBoxHeight) ; j++) {
            shapeRenderer.line(boundingBoxX, j, boundingBoxX + boundingBoxWidth, j);
        }
        // Drawing bounding box
        drawLineRectangle(shapeRenderer, BOUNDING_BOX_COLOR, boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
    }

    private void drawCells(ShapeRenderer shapeRenderer) {

        for (int i = map.getCells().size -1; i >= 0; i--) {
            GameCell cell = map.getCells().get(i);

            if (cell instanceof GameRoom) {
                drawCellGrid(shapeRenderer, ROOM_GRID_COLOR, cell);
                drawLineRectangle(shapeRenderer, ROOM_BORDER_COLOR, cell.x, cell.y, cell.width, cell.height);
                drawRoomRadius(shapeRenderer, ROOM_CIRCLE_COLOR, cell, MapGenerator.ROOM_SPREAD_RADIUS_OFFSET);
            } else {
                drawCellGrid(shapeRenderer, CELL_GRID_COLOR, cell);
                drawLineRectangle(shapeRenderer, CELL_BORDER_COLOR, cell.x, cell.y, cell.width, cell.height);
            }
        }
    }

    private void drawMinimalSpanningTree(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(MIN_SPANNING_TREE_COLOR);

        Multigraph<GameRoom, NoDuplicateEdge> minSpanningTree = map.minimalSpanningTree;

        Set<NoDuplicateEdge> edges = minSpanningTree.edgeSet();

        for (NoDuplicateEdge edge : edges) {

            Vector2 sourceRoomCenter = new Vector2();
            Vector2 targetRoomCenter = new Vector2();

            minSpanningTree.getEdgeSource(edge).getCenter(sourceRoomCenter);
            minSpanningTree.getEdgeTarget(edge).getCenter(targetRoomCenter);

            shapeRenderer.line(sourceRoomCenter.x, sourceRoomCenter.y, targetRoomCenter.x, targetRoomCenter.y);
        }
    }

    private void drawLineRectangle(ShapeRenderer shapeRenderer, Color color, float x, float y, float width, float height) {
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, width, height);
    }

    private void drawFilledRectangle(ShapeRenderer shapeRenderer, Color color, float x, float y, float width, float height) {
        shapeRenderer.setAutoShapeType(true);
        shapeRenderer.setColor(color);
        shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
    }

    private void drawRoomRadius(ShapeRenderer shapeRenderer, Color color, Rectangle room, float radius) {
        shapeRenderer.setColor(color);

        Vector2 roomCenter = new Vector2();
        room.getCenter(roomCenter);
        float roomRadius = new Vector2(roomCenter.x - room.x, roomCenter.y - room.y).len();
        shapeRenderer.circle(roomCenter.x, roomCenter.y, roomRadius + radius, CIRCLE_SEGMENT_COUNT);
    }

    private void drawCorridors(ShapeRenderer shapeRenderer) {
        Array<Rectangle> corridors = map.corridors;

        for (Rectangle corridor : corridors) {
            drawCellGrid(shapeRenderer, CORRIDOR_GRID_COLOR, corridor);
            drawLineRectangle(shapeRenderer, CORRIDOR_BORDER_COLOR, corridor.x, corridor.y, corridor.width, corridor.height);
        }
    }

    private void drawCellGrid(ShapeRenderer shapeRenderer, Color color, Rectangle cell) {

        int cellX = (int) cell.x;
        int cellY = (int) cell.y;
        int cellWidth = (int) cell.width;
        int cellHeight = (int) cell.height;

        shapeRenderer.setColor(color);
        // Drawing x lines
        for (int i = cellX; i <= (cellX + cellWidth); i++) {
            shapeRenderer.line(i, cellY, i, cellY + cellHeight);
        }
        // Drawing y lines
        for (int j = cellY; j <= (cellY + cellHeight); j++) {
            shapeRenderer.line(cellX, j, cellX + cellWidth, j);
        }
    }

    public void dispose() {
        shapeRenderer.dispose();
    }

    public void resize(int width, int height) {
        viewport.update(width, height);
    }
}
