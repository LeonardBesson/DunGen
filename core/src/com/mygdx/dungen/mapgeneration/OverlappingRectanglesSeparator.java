package com.mygdx.dungen.mapgeneration;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.dungen.GameCell;
import com.mygdx.dungen.GameRoom;
import org.apache.commons.math3.random.RandomDataGenerator;

/**
 * @author leonard
 * Date: 15/2/2016
 */
public class OverlappingRectanglesSeparator {

    private static final float REPEL_DECAY_COEFFICIENT = 1.0f;

    public OverlappingRectanglesSeparator() {}

    public Rectangle findBoundingBox(Array<? extends Rectangle> rectangles) {

        int rectangleCount = rectangles.size;
        Gdx.app.log("RectanglesSeparator", "Finding bouncing box of " + rectangleCount + " rectangles");

        float maxX = 0;
        float maxY = 0;
        float minX = 0;
        float minY = 0;

        for (int i = 0; i < rectangleCount; i++) {

            Rectangle rect = rectangles.get(i);
            float x = rect.x;
            float y = rect.y;
            float x2 = rect.x + rect.width;
            float y2 = rect.y + rect.height;

            if (i == 0) {
                minX = x;
                minY = y;
                maxX = x2;
                maxY = y2;
            } else {
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x2);
                maxY = Math.max(maxY, y2);
            }
        }

        Rectangle rectangle = new Rectangle(minX, minY, maxX - minX, maxY - minY);

        Gdx.app.log("RectanglesSeparator", "Bounding box: " + rectangle.toString());

        return rectangle;
    }

    public void separateOverlappingRectangles(Array<? extends Rectangle> rectangles, boolean roomsAreFixed) {
        int rectangleCount = rectangles.size;
        boolean separated = false;

        Gdx.app.log("RectanglesSeparator", "Separating " + rectangleCount + " rectangles");

        while (!separated) {
            separated = true;

            for (int i = 0; i < rectangleCount; i++) {
                Rectangle rect = rectangles.get(i);

                if (roomsAreFixed && rect instanceof GameRoom) continue;

                Vector2 velocity = new Vector2();
                Vector2 center = new Vector2();
                rect.getCenter(center);

                for (int j = 0; j < rectangleCount; j++) {
                    Rectangle otherRect = rectangles.get(j);

                    if (rect == otherRect) continue;
                    if (!rect.overlaps(otherRect)) continue;

                    Vector2 otherCenter = new Vector2();
                    otherRect.getCenter(otherCenter);
                    Vector2 diff = new Vector2(center.x - otherCenter.x, center.y - otherCenter.y);

                    applyDiffToVelocity(diff, velocity);
                }

                if (velocity.len2() > 0f) {
                    separated = false;

                    setCellPositionFromVelocity((GameCell) rect, velocity);
                }
            }
        }
    }

    public void spreadRooms (Array<? extends Rectangle> rectangles, float radius) {
        int rectangleCount = rectangles.size;
        boolean spread = false;

        Gdx.app.log("RectanglesSeparator", "Spreading rooms within " + rectangleCount + " cells");

        while (!spread) {
            spread = true;

            for (int i = 0; i < rectangleCount; i++) {
                Rectangle cell = rectangles.get(i);

                if (!(cell instanceof GameRoom)) continue;

                Vector2 velocity = new Vector2();
                Vector2 center = new Vector2();
                cell.getCenter(center);

                float cellRadius = new Vector2(center.x - cell.x, center.y - cell.y).len();
                Circle cellCircle = new Circle(center.x, center.y, cellRadius + radius);

                for (int j = 0; j < rectangleCount; j++) {
                    Rectangle otherCell = rectangles.get(j);

                    if (cell == otherCell) continue;
                    if (!(otherCell instanceof GameRoom)) continue;

                    Vector2 otherCellCenter = new Vector2();
                    otherCell.getCenter(otherCellCenter);

                    float otherCellRadius = new Vector2(otherCellCenter.x - otherCell.x, otherCellCenter.y - otherCell.y).len();
                    Circle otherCellCircle = new Circle(otherCellCenter.x, otherCellCenter.y, otherCellRadius + radius);

                    if (!cellCircle.overlaps(otherCellCircle)) continue;

                    Vector2 diff = new Vector2(center.x - otherCellCenter.x, center.y - otherCellCenter.y);

                    applyDiffToVelocity(diff, velocity);
                }

                if (velocity.len2() > 0f) {
                    spread = false;

                    setCellPositionFromVelocity((GameRoom) cell, velocity);
                }
            }
        }
    }

    private void setCellPositionFromVelocity(GameCell cell, Vector2 velocity) {
        RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

        velocity.nor();

        Vector2 cellPosition = new Vector2();
        cell.getPosition(cellPosition);
        cellPosition.add(velocity);

        int cellX = Math.round(cellPosition.x);
        int cellY = Math.round(cellPosition.y);
        int x = randomDataGenerator.nextInt(cellX - 1, cellX + 1);
        int y = randomDataGenerator.nextInt(cellY - 1, cellY + 1);

        cell.setPosition(x, y);
    }

    private void applyDiffToVelocity(Vector2 diff, Vector2 velocity) {
        float diffLen = diff.len();

        if (diffLen > 0f) {
            float scale = REPEL_DECAY_COEFFICIENT / diffLen;
            diff.nor();
            diff.scl(scale);

            velocity.add(diff);
        }
    }

    public int[] getRoomsGridWidthHeight(int roomCount) {
        int width;
        int height;

        int nearestSquareRoot = (int) Math.ceil(Math.sqrt(roomCount));
        Gdx.app.log("RectanglesSeparator", "Nearest upper square root is " + nearestSquareRoot);

        width = nearestSquareRoot;
        height = (nearestSquareRoot * (nearestSquareRoot - 1) >= roomCount ? nearestSquareRoot - 1 : nearestSquareRoot);

        Gdx.app.log("RectanglesSeparator", "Nearest (W x H) Grid is (" + width + " x " + height + ")");
        return new int[] {width, height};
    }

    public void moveRooms(Array<? extends Rectangle> rectangles, int x, int y) {
        Gdx.app.log("RectanglesSeparator", "Moving Rooms to (" + x + ", " + y + ")");

        for (Rectangle cell : rectangles) {
            if (cell instanceof GameRoom) {
                cell.setCenter(new Vector2(x, y));
            }
        }
    }

    public void moveCellsWithOffset(Array<? extends Rectangle> cells, int x, int y, int offsetX, int offsetY) {
        Gdx.app.log("RectanglesSeparator", "Moving cells to (" + x + ", " + y + ")");

        RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

        for (Rectangle cell : cells) {
            if (!(cell instanceof GameRoom)) {
                // X/Y +- 1 so they are not stacked at the exact same spot and can't separate
                int newX = randomDataGenerator.nextInt(x - offsetX, x + offsetX);
                int newY = randomDataGenerator.nextInt(y - offsetY, y + offsetY);
                cell.setCenter(new Vector2(newX, newY));
            }
        }
    }

    public int[] computeAverageStats(Array<GameCell> rectangles) {
        int averageWidth = 0;
        int averageHeight = 0;

        for (int i = 0; i < rectangles.size; i++) {

            GameCell cell = rectangles.get(i);

            if (cell instanceof GameRoom) {
                averageWidth = (int) (averageWidth + cell.width) / 2;
                averageHeight = (int) (averageHeight + cell.height) / 2;
            }
        }

        return new int[] {averageWidth, averageHeight};
    }

    public void stackSeparatedRectangles(Array<GameCell> rectangles) {
        int[] averages = computeAverageStats(rectangles);

        System.out.println("Average width: " + averages[0] + ", height: " + averages[1]);
        Rectangle boundingBox = findBoundingBox(rectangles);
        Vector2 boundingBoxCenter = new Vector2();
        boundingBox.getCenter(boundingBoxCenter);

        for (int i = 0; i < rectangles.size; i++) {
            GameCell cell = rectangles.get(i);
            if (cell instanceof GameRoom) {
                cell.setCenter(new Vector2(boundingBoxCenter.x, boundingBoxCenter.y));
                System.out.println("Room placed at (" + boundingBoxCenter.x + ", " + boundingBoxCenter.y + ")");
                break;
            }
        }

        separateOverlappingRectangles(rectangles, false);
    }

    public Array<GameCell> findOverlappingRectangles(Array<GameCell> rectangles, GameCell rectangle) {
        int rectangleCount = rectangles.size;

        Gdx.app.log("RectanglesSeparator", "Finding overlapping rectangles. COUNT: " + rectangleCount);

        Array<GameCell> overlappingRectangles = new Array<GameCell>(rectangleCount);

        for (int i = 0; i < rectangleCount; i++) {
            GameCell rect = rectangles.get(i);

            if (rect.overlaps(rectangle)){
                overlappingRectangles.add(rect);
            }
        }

        overlappingRectangles.shrink();
        Gdx.app.log("RectanglesSeparator", overlappingRectangles.size + " were overlapping the given rectangle");

        return overlappingRectangles;
    }
}
