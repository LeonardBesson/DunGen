package com.mygdx.dungen.mapgeneration;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.mygdx.dungen.CellOrientation;
import com.mygdx.dungen.GameCell;
import com.mygdx.dungen.GameRoom;
import com.mygdx.dungen.GameCellException;

public class CellGenerator {

    public static final int MAX_TRY = 400;

    public CellGenerator() {}

    /**
     * Generate cells from an array of radiuses and rooms if radiuses are above threshold
     * @param radiuses The array of radiuses
     * @param minCellWidth Minimum width of cell
     * @param minCellHeight Minimum height of cell
     * @param maxCellWidth Maximum width of cell
     * @param maxCellHeight Maximum height of cell
     * @param minRoomWidth Minimum width of room
     * @param minRoomHeight Minimum height of room
     * @param minRoomRatio Minimum ratio of room
     * @param maxRoomRatio Maximum ratio of room
     * @return An array containing the cells and rooms generated
     */
    public Array<GameCell> generateCellsFromRadiuses(Array<Radius> radiuses, int minCellWidth, int minCellHeight, int maxCellWidth, int maxCellHeight, int minRoomWidth, int minRoomHeight, float minRoomRatio, float maxRoomRatio, CellOrientation orientation) throws GameCellException {

        int cellCount = radiuses.size;

        Gdx.app.log("CellGenerator", "---------- Now generating cells ----------");
        Gdx.app.log("CellRatio", "minRatio: " + minRoomRatio + ", maxRatio: " + maxRoomRatio);

        Array<GameCell> cells = new Array<GameCell>(cellCount);

        for (int i = 0; i < cellCount; i++) {
            Radius radius = radiuses.get(i);
            float radiusValue = radius.getValue();

            float angle = (float) (Math.random() * Math.PI * 2);

            int x = Math.round((float) (Math.cos(angle) * radiusValue));
            int y = Math.round((float) (Math.sin(angle) * radiusValue));

            boolean isRadiusAboveThreshold = radius.isAboveThreshold();

            int minCellXSize = isRadiusAboveThreshold ? minRoomWidth : minCellWidth;
            int minCellYSize = isRadiusAboveThreshold ? minRoomHeight : minCellHeight;

            int width;
            int height;

            if (isRadiusAboveThreshold) {
                width = Math.max(Math.abs(x) * 2, minCellXSize);
                height = Math.max(Math.abs(y) * 2, minCellYSize);
            } else {
                width = MathUtils.clamp(Math.abs(x) * 2, minCellXSize, maxCellWidth);
                height = MathUtils.clamp(Math.abs(y) * 2, minCellYSize, maxCellHeight);
            }

            GameCell cell;

            if (isRadiusAboveThreshold) {
                cell = new GameRoom(x, y, width, height);
                ensureRoomRatio(cell, radiusValue, minRoomRatio, maxRoomRatio, minCellXSize, minCellYSize, orientation);
                cells.add(cell);
            } else {
                cell = new GameCell(x, y, width, height);
                cells.add(cell);
            }

            String cellOrRoom = isRadiusAboveThreshold ? "ROOM" : "CELL";
            Gdx.app.log("CellGenerator", "  --> new " + cellOrRoom + ": (" + cell.x + ", " + cell.y + ") width: " + cell.width + ", height: " + cell.height);
        }

        return cells;
    }

    // TODO might want to add maxWidth and maxHeight (not necessary for now)
    /**
     * Ensure that the ratio of a cell
     * @param cell The cell
     * @param radiusValue Radius used to generate x and y
     * @param minRatio Minimum ratio
     * @param maxRatio Maximum ratio
     * @param minCellWidth Minimum width of cell
     * @param minCellHeight Minimum height of cell
     * @param orientation the orientation of the cell
     */
    public void ensureRoomRatio(GameCell cell, float radiusValue, float minRatio, float maxRatio, int minCellWidth, int minCellHeight, CellOrientation orientation) throws GameCellException {

        Gdx.app.log("CellGenerator", "  --> checking cell ratio");

        float ratio = getRatio(orientation, cell);
        int iterationCount = 0;
        int tryCount = 0;

        while ((ratio < minRatio || ratio > maxRatio)) {
            if (tryCount > MAX_TRY) throw new GameCellException("CellGenerator: Ratio max try exceeded -> RADIUS_MULTIPLIER must be too low");

            float angle = (float) (Math.random() * Math.PI * 2);

            cell.x = Math.round((float) (Math.cos(angle) * radiusValue));
            cell.y = Math.round((float) (Math.sin(angle) * radiusValue));

            cell.width = Math.max(Math.abs(cell.x) * 2, minCellWidth);
            cell.height = Math.max(Math.abs(cell.y) * 2, minCellHeight);

            ratio = getRatio(orientation, cell);

            iterationCount++;
            tryCount++;
        }

        cell.setOrientation(orientation);

        Gdx.app.log("CellGenerator", "      --> Final Cell: " + ": (" + cell.x + ", " + cell.y + ") width: " + cell.width + ", height: " + cell.height + ", RATIO: " + ratio + ", iterations: " + iterationCount);
    }

    private float getRatio(CellOrientation orientation, GameCell cell) {
        float ratio;
        switch (orientation) {
            default:
            case ORIGINAL:
                ratio = cell.width > cell.height ? (cell.width / cell.height) : (cell.height / cell.width);
                break;
            case LANDSCAPE:
                ratio = cell.getAspectRatio();
                break;
            case PORTRAIT:
                ratio = cell.height / cell.width;
                break;
        }

        return ratio;
    }
}
