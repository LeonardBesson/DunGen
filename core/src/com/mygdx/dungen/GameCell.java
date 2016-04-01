package com.mygdx.dungen;


import com.badlogic.gdx.math.Rectangle;

/**
 * @author leonard
 *         Date: 11/2/2016
 */
public class GameCell extends Rectangle {

    private CellOrientation orientation;

    public GameCell(float x, float y, float width, float height) {
        super(x, y, width, height);
        this.orientation = CellOrientation.ORIGINAL;
    }

    @Override
    public String toString() {
        return "Cell: " + super.toString();
    }

    public CellOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(CellOrientation orientation) throws GameCellException {
        ensureCorrectOrientation(orientation);
        this.orientation = orientation;
    }

    private void ensureCorrectOrientation(CellOrientation orientation) throws GameCellException {
        switch (orientation) {
            default:
            case ORIGINAL:
                break;
            case LANDSCAPE:
                if (!(width > height)) {
                    throw new GameCellException("Width must be greater than height for LANDSCAPE orientation");
                }
                break;
            case PORTRAIT:
                if (!(height > width)) {
                    throw new GameCellException("Height must be greater than width for PORTRAIT orientation");
                }
                break;
        }
    }
}
