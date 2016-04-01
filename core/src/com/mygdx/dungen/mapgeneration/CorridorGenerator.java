package com.mygdx.dungen.mapgeneration;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.dungen.GameCell;
import com.mygdx.dungen.GameMap;
import com.mygdx.dungen.GameRoom;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.jgrapht.graph.Multigraph;

import java.util.Set;

/**
 * @author leonard
 *         Date: 24/2/2016
 */
public class CorridorGenerator {

    private static final int CORRIDOR_SIZE = 3;
    private static final int MAX_TRY = 200;

    public CorridorGenerator() {}

    public Array<Rectangle> generateCorridors(Array<GameCell> cells, Multigraph<GameRoom, NoDuplicateEdge> corridorsGraph) throws MapGenException {

        Gdx.app.log("CorridorGenerator", "------------- Generating corridors -------------");
        Array<Rectangle> corridors = new Array<>();
        Set<NoDuplicateEdge> edges = corridorsGraph.edgeSet();

        Array<GameRoom> rooms = GameMap.extractRooms(cells);

        for (NoDuplicateEdge edge : edges) {
            Gdx.app.log("CorridorGenerator", "  Generating corridor for edge: " + edge);

            GameRoom sourceRoom = corridorsGraph.getEdgeSource(edge);
            GameRoom targetRoom = corridorsGraph.getEdgeTarget(edge);

            corridors = generateCorridor(rooms, corridors, sourceRoom, targetRoom);
        }

        return corridors;
    }

    private Array<Rectangle> generateCorridor(Array<GameRoom> rooms, Array<Rectangle> corridors, GameRoom source, GameRoom target) throws MapGenException {

        Rectangle[] corridor = null;

        int tryCount = 0;
        boolean validCorridor = false;

        while (!validCorridor) {

            if (tryCount > MAX_TRY) {
                throw new MapGenException("Couldn't create a proper corridor that doesn't overlap other rooms and corridors");
            }

            corridor = createCorridor(source, target);

            validCorridor = ensureNoCollisionCorridor(rooms, corridors, corridor);
            if (!validCorridor) {
                source.removeLastEntrance();
                target.removeLastEntrance();
            }

            tryCount++;
        }

        Gdx.app.log("CorridorGenerator", "      --> Corridor created in " + tryCount + " try");

        for (int i = 0; i < corridor.length; i++) {
            Rectangle corridorPart = corridor[i];

            if (corridorPart != null) {
                corridors.add(corridorPart);
            }
        }

        return corridors;
    }

    private Rectangle[] createCorridor(GameRoom source, GameRoom target) {

        /*Gdx.app.log("CorridorGenerator", "Generating corridor for :");
        Gdx.app.log("CorridorGenerator", "Source: " + source);
        Gdx.app.log("CorridorGenerator", "Target: " + target);*/

        int sourceStartX = (int) source.x;
        int sourceEndX = (int) (source.x + source.width);
        int targetStartX = (int) target.x;
        int targetEndX = (int) (target.x + target.width);

        int sourceStartY = (int) source.y;
        int sourceEndY = (int) (source.y + source.height);
        int targetStartY = (int) target.y;
        int targetEndY = (int) (target.y + target.height);

        boolean xIntersectSource = (sourceStartX <= targetStartX && sourceEndX >= targetStartX);
        boolean xIntersectTarget = (targetStartX <= sourceStartX && targetEndX >= sourceStartX);

        boolean yIntersectSource = (sourceStartY <= targetStartY && sourceEndY >= targetStartY);
        boolean yIntersectTarget = (targetStartY <= sourceStartY && targetEndY >= sourceStartY);

        boolean xIntersect = xIntersectSource || xIntersectTarget;
        boolean yIntersect = yIntersectSource || yIntersectTarget;

        if (xIntersect) {
            int spaceAvailableX;

            if (sourceStartX == targetStartX) {
                if (sourceEndX > targetEndX) {
                    spaceAvailableX = (int) target.width;
                } else {
                    spaceAvailableX = (int) source.width;
                }
            } else if (source.x < target.x && source.x + source.width > target.x + target.width) {
                spaceAvailableX = (int) target.width;
            } else if (target.x < source.x && target.x + target.width > source.x + source.width) {
                spaceAvailableX = (int) source.width;
            } else {
                spaceAvailableX = xIntersectSource ? (int) Vector2.dst(sourceEndX, 0, targetStartX, 0) : (int) Vector2.dst(targetEndX, 0, sourceStartX, 0);
            }

            if (spaceAvailableX >= CORRIDOR_SIZE + 2) {
                Rectangle corridor;
                if (xIntersectSource) {
                    corridor = createLineCorridor(true, source, target);
                } else {
                    corridor = createLineCorridor(true, target, source);
                }
                return new Rectangle[]{corridor};
            } else {
                return createLShapeCorridor(source, target);
            }
        } else if (yIntersect) {
            int spaceAvailableY;

            if (sourceStartY == targetStartY) {
                if (sourceEndY > targetEndY) {
                    spaceAvailableY = (int) target.height;
                } else {
                    spaceAvailableY = (int) source.height;
                }
            } else if (source.y < target.y && source.y + source.height > target.y + target.height) {
                spaceAvailableY = (int) target.height;
            } else if (target.y < source.y && target.y + target.height > source.y + source.height) {
                spaceAvailableY = (int) source.height;
            } else {
                spaceAvailableY = yIntersectSource ? (int) Vector2.dst(0, sourceEndY, 0, targetStartY) : (int) Vector2.dst(0, targetEndY, 0, sourceStartY);
            }

            if (spaceAvailableY >= CORRIDOR_SIZE + 2) {
                Rectangle corridor;
                if (yIntersectSource) {
                    corridor = createLineCorridor(false, source, target);
                } else {
                    corridor = createLineCorridor(false, target, source);
                }
                return new Rectangle[]{corridor};
            } else {
                return createLShapeCorridor(source, target);
            }
        } else {
            return createLShapeCorridor(source, target);
        }
    }

    private Rectangle createLineCorridor(boolean onX, GameRoom source, GameRoom target) {

        Gdx.app.log("CorridorGenerator", "  --> Creating straight line corridor");

        int startLower;
        int startUpper;
        int start;

        if (onX) {
            if (source.x <= target.x && source.x + source.width >= target.x + target.width) {
                startLower = (int) (target.x + 1);
                startUpper = (int) (target.x - 1 + target.width - CORRIDOR_SIZE);
            } else if (target.x <= source.x && target.x + target.width >= source.x + source.width) {
                startLower = (int) (source.x + 1);
                startUpper = (int) (source.x - 1 + source.width - CORRIDOR_SIZE);
            } else {
                startLower = (int) (target.x + 1);
                startUpper = (int) (target.x - 1 + Vector2.dst(target.x, 0, source.x + source.width, 0) - CORRIDOR_SIZE);
            }
            start = getRandomInt(startLower, startUpper);

            boolean sourceTop = (source.y >= target.y + target.height);
            if (sourceTop) {
                source.addEntrance(start, source.y, start + CORRIDOR_SIZE, source.y);
                target.addEntrance(start, target.y + target.height, start + CORRIDOR_SIZE, target.y + target.height);
                return new Rectangle(start, target.y + target.height, CORRIDOR_SIZE, Vector2.dst(0, source.y, 0, target.y + target.height));
            } else {
                source.addEntrance(start, source.y + source.height, start + CORRIDOR_SIZE, source.y + source.height);
                target.addEntrance(start, target.y, start + CORRIDOR_SIZE, target.y);
                return new Rectangle(start, source.y + source.height, CORRIDOR_SIZE, Vector2.dst(0, target.y, 0, source.y + source.height));
            }
        } else {
            if (source.y <= target.y && source.y + source.height >= target.y + target.height) {
                startLower = (int) (target.y + 1);
                startUpper = (int) (target.y - 1 + target.height - CORRIDOR_SIZE);
            } else if (target.y <= source.y && target.y + target.height >= source.y + source.height) {
                startLower = (int) (source.y + 1);
                startUpper = (int) (source.y - 1 + source.height - CORRIDOR_SIZE);
            } else {
                startLower = (int) (target.y + 1);
                startUpper = (int) (target.y - 1 + Vector2.dst(0, target.y, 0, source.y + source.height) - CORRIDOR_SIZE);
            }
            start = getRandomInt(startLower, startUpper);

            boolean sourceLeft = (source.x + source.width <= target.x);
            if (sourceLeft) {
                source.addEntrance(source.x + source.width, start, source.x + source.width, start + CORRIDOR_SIZE);
                target.addEntrance(target.x, start, target.x, start + CORRIDOR_SIZE);
                return new Rectangle(source.x + source.width, start, Vector2.dst(source.x + source.width, 0, target.x, 0), CORRIDOR_SIZE);
            } else {
                source.addEntrance(source.x, start, source.x , start + CORRIDOR_SIZE);
                target.addEntrance(target.x + target.width, start, target.x + target.width, start + CORRIDOR_SIZE);
                return new Rectangle(target.x + target.width, start, Vector2.dst(target.x + target.width, 0, source.x, 0), CORRIDOR_SIZE);
            }
        }
    }

    private int getRandomInt(int lower, int upper) {
        RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

        if (upper < lower) {
            upper = lower;
        }

        return randomDataGenerator.nextInt(lower, upper);
    }

    private Rectangle[] createLShapeCorridor(GameRoom source, GameRoom target) {

        Gdx.app.log("CorridorGenerator", "  --> Creating L shape corridor");

        Vector2 sourceCenter = new Vector2();
        Vector2 targetCenter = new Vector2();
        source.getCenter(sourceCenter);
        target.getCenter(targetCenter);

        int targetCenterX = (int) targetCenter.x;
        int targetCenterY = (int) targetCenter.y;
        int sourceCenterX = (int) sourceCenter.x;
        int sourceCenterY = (int) sourceCenter.y;

        boolean canGoRight  = targetCenterX >= sourceCenterX;
        boolean canGoTop    = targetCenterY >= sourceCenterY;
        boolean canGoLeft   = sourceCenterX >= targetCenterX;
        boolean canGoBottom = sourceCenterY >= targetCenterY;

        boolean rightTop    = canGoRight && canGoTop;
        boolean rightBottom = canGoRight && canGoBottom;
        boolean leftTop     = canGoLeft && canGoTop;
        boolean leftBottom  = canGoLeft && canGoBottom;

        int direction = getRandomInt(1, 2);
        Rectangle first = null;
        Rectangle second = null;

        int start;
        int end;

        int startLower;
        int startUpper;
        int endLower;
        int endUpper;

        if (rightTop) {
            switch (direction) {
                case 1:
                    if (target.y < source.y + source.height) {
                        startLower = (int) (source.y + 1);
                        startUpper = (int) (source.y + Vector2.dst(0, source.y, 0, target.y) - CORRIDOR_SIZE);
                    } else {
                        startLower = (int) (source.y + 1);
                        startUpper = (int) (source.y - 1 + source.height - CORRIDOR_SIZE);
                    }
                    start = getRandomInt(startLower, startUpper);

                    if (target.x < source.x + source.width) {
                        endLower = (int) (source.x + source.width);
                        endUpper = (int) (source.x - 1 + source.width + Vector2.dst(source.x + source.width, 0, target.x + target.width, 0) - CORRIDOR_SIZE);
                    } else {
                        endLower = (int) (target.x + 1);
                        endUpper = (int) (target.x - 1 + target.width - CORRIDOR_SIZE);
                    }
                    end = getRandomInt(endLower, endUpper);

                    first = new Rectangle(source.x + source.width, start, Vector2.dst(end, 0, source.x + source.width, 0), CORRIDOR_SIZE);
                    second = new Rectangle(end, start, CORRIDOR_SIZE, Vector2.dst(0, start, 0, target.y));

                    source.addEntrance(source.x + source.width, start, source.x + source.width, start + CORRIDOR_SIZE);
                    target.addEntrance(end, target.y, end + CORRIDOR_SIZE, target.y);
                    break;
                case 2:
                    if (target.x < source.x + source.width) {
                        startLower = (int) (source.x + 1);
                        startUpper = (int) (source.x + Vector2.dst(source.x, 0, target.x, 0) - CORRIDOR_SIZE);
                    } else {
                        startLower = (int) (source.x + 1);
                        startUpper = (int) (source.x - 1 + source.width - CORRIDOR_SIZE);
                    }
                    start = getRandomInt(startLower, startUpper);

                    if (target.y < source.y + source.height) {
                        endLower = (int) (source.y + source.height);
                        endUpper = (int) (source.y + source.height + Vector2.dst(0, source.y + source.height, 0, target.y + target.height) - CORRIDOR_SIZE);
                    } else {
                        endLower = (int) (target.y + 1);
                        endUpper = (int) (target.y - 1 + target.height - CORRIDOR_SIZE);
                    }
                    end = getRandomInt(endLower, endUpper);

                    first = new Rectangle(start, source.y + source.height, CORRIDOR_SIZE, Vector2.dst(0, end, 0, source.y + source.height));
                    second = new Rectangle(start, end, Vector2.dst(start, 0, target.x, 0), CORRIDOR_SIZE);

                    source.addEntrance(start, source.y + source.height, start + CORRIDOR_SIZE, source.y + source.height);
                    target.addEntrance(target.x, end, target.x, end + CORRIDOR_SIZE);
                    break;
            }
        } else if (rightBottom) {
            switch (direction) {
                case 1:
                    if (target.y + target.height > source.y) {
                        startLower = (int) (target.y + target.height);
                        startUpper = (int) (target.y - 1 + target.height + Vector2.dst(0, target.y + target.height, 0, source.y + source.height) - CORRIDOR_SIZE);
                    } else {
                        startLower = (int) (source.y + 1);
                        startUpper = (int) (source.y - 1 + source.height - CORRIDOR_SIZE);
                    }
                    start = getRandomInt(startLower, startUpper);

                    if (target.x < source.x + source.width) {
                        endLower = (int) (source.x + source.width);
                        endUpper = (int) (source.x - 1 + source.width + Vector2.dst(source.x + source.width, 0, target.x + target.width, 0) - CORRIDOR_SIZE);
                    } else {
                        endLower = (int) (target.x + 1);
                        endUpper = (int) (target.x - 1 + target.width - CORRIDOR_SIZE);
                    }
                    end = getRandomInt(endLower, endUpper);

                    first = new Rectangle(source.x + source.width, start, Vector2.dst(end, 0, source.x + source.width, 0), CORRIDOR_SIZE);
                    second = new Rectangle(end, target.y + target.height, CORRIDOR_SIZE, Vector2.dst(0, start + CORRIDOR_SIZE, 0, target.y + target.height));

                    source.addEntrance(source.x + source.width, start, source.x + source.width, start + CORRIDOR_SIZE);
                    target.addEntrance(end, target.y + target.height, end + CORRIDOR_SIZE, target.y + target.height);
                    break;
                case 2:
                    if (target.x < source.x + source.width) {
                        startLower = (int) (source.x + 1);
                        startUpper = (int) (source.x + Vector2.dst(source.x, 0, target.x, 0) - CORRIDOR_SIZE);
                    } else {
                        startLower = (int) (source.x + 1);
                        startUpper = (int) (source.x - 1 + source.width - CORRIDOR_SIZE);
                    }
                    start = getRandomInt(startLower, startUpper);

                    if (target.y + target.height > source.y) {
                        endLower = (int) (target.y + 1);
                        endUpper = (int) (target.y + Vector2.dst(0, target.y, 0, source.y) - CORRIDOR_SIZE);
                    } else {
                        endLower = (int) (target.y + 1);
                        endUpper = (int) (target.y - 1 + target.height - CORRIDOR_SIZE);
                    }
                    end = getRandomInt(endLower, endUpper);

                    first = new Rectangle(start, end + CORRIDOR_SIZE, CORRIDOR_SIZE, Vector2.dst(0, end, 0, source.y));
                    second = new Rectangle(start, end, Vector2.dst(start, 0, target.x, 0), CORRIDOR_SIZE);

                    source.addEntrance(start, source.y, start + CORRIDOR_SIZE, source.y);
                    target.addEntrance(target.x, end, target.x, end + CORRIDOR_SIZE);
                    break;
            }
        } else if (leftTop) {
            switch (direction) {
                case 1:
                    if (target.y < source.y + source.height) {
                        startLower = (int) (source.y + 1);
                        startUpper = (int) (source.y + Vector2.dst(0, source.y, 0, target.y) - CORRIDOR_SIZE);
                    } else {
                        startLower = (int) (source.y + 1);
                        startUpper = (int) (source.y - 1 + source.height - CORRIDOR_SIZE);
                    }
                    start = getRandomInt(startLower, startUpper);

                    if (target.x + target.width > source.x) {
                        endLower = (int) (target.x + 1);
                        endUpper = (int) (target.x + Vector2.dst(target.x, 0, source.x, 0) - CORRIDOR_SIZE);
                    } else {
                        endLower = (int) (target.x + 1);
                        endUpper = (int) (target.x - 1 + target.width - CORRIDOR_SIZE);
                    }
                    end = getRandomInt(endLower, endUpper);

                    first = new Rectangle(end, start, Vector2.dst(source.x, 0, end, 0), CORRIDOR_SIZE);
                    second = new Rectangle(end, start + CORRIDOR_SIZE, CORRIDOR_SIZE, Vector2.dst(0, start, 0, target.y));

                    source.addEntrance(source.x, start, source.x, start + CORRIDOR_SIZE);
                    target.addEntrance(end, target.y, end + CORRIDOR_SIZE, target.y);
                    break;
                case 2:
                    if (target.x + target.width > source.x) {
                        startLower = (int) (target.x + target.width);
                        startUpper = (int) (target.x + target.width + Vector2.dst(target.x + target.width, 0, source.x + source.width, 0) - CORRIDOR_SIZE);
                    } else {
                        startLower = (int) (source.x + 1);
                        startUpper = (int) (source.x - 1 + source.width - CORRIDOR_SIZE);
                    }
                    start = getRandomInt(startLower, startUpper);

                    if (target.y < source.y + source.height) {
                        endLower = (int) (source.y + source.height);
                        endUpper = (int) (source.y + source.height + Vector2.dst(0, source.y + source.height, 0, target.y + target.height) - CORRIDOR_SIZE);
                    } else {
                        endLower = (int) (target.y + 1);
                        endUpper = (int) (target.y - 1 + target.height - CORRIDOR_SIZE);
                    }
                    end = getRandomInt(endLower, endUpper);

                    first = new Rectangle(start, source.y + source.height, CORRIDOR_SIZE, Vector2.dst(0, end, 0, source.y + source.height));
                    second = new Rectangle(target.x + target.width, end, Vector2.dst(start + CORRIDOR_SIZE, 0, target.x + target.width, 0), CORRIDOR_SIZE);

                    source.addEntrance(start, source.y + source.height, start + CORRIDOR_SIZE, source.y + source.height);
                    target.addEntrance(target.x + target.width, end, target.x + target.width, end + CORRIDOR_SIZE);
                    break;
            }
        } else if (leftBottom) {
            switch (direction) {
                case 1:
                    if (target.y + target.height > source.y) {
                        startLower = (int) (target.y + target.height);
                        startUpper = (int) (target.y - 1 + target.height + Vector2.dst(0, target.y + target.height, 0, source.y + source.height) - CORRIDOR_SIZE);
                    } else {
                        startLower = (int) (source.y + 1);
                        startUpper = (int) (source.y - 1 + source.height - CORRIDOR_SIZE);
                    }
                    start = getRandomInt(startLower, startUpper);

                    if (target.x + target.width > source.x) {
                        endLower = (int) (target.x + 1);
                        endUpper = (int) (target.x + Vector2.dst(target.x, 0, source.x, 0) - CORRIDOR_SIZE);
                    } else {
                        endLower = (int) (target.x + 1);
                        endUpper = (int) (target.x - 1 + target.width - CORRIDOR_SIZE);
                    }
                    end = getRandomInt(endLower, endUpper);

                    first = new Rectangle(end, start, Vector2.dst(end, 0, source.x, 0), CORRIDOR_SIZE);
                    second = new Rectangle(end, target.y + target.height, CORRIDOR_SIZE, Vector2.dst(0, start, 0, target.y + target.height));

                    source.addEntrance(source.x, start, source.x, start + CORRIDOR_SIZE);
                    target.addEntrance(end, target.y + target.height, end + CORRIDOR_SIZE, target.y + target.height);
                    break;
                case 2:
                    if (target.x + target.width > source.x) {
                        startLower = (int) (target.x + target.width);
                        startUpper = (int) (target.x + target.width + Vector2.dst(target.x + target.width, 0, source.x + source.width, 0) - CORRIDOR_SIZE);
                    } else {
                        startLower = (int) (source.x + 1);
                        startUpper = (int) (source.x - 1 + source.width - CORRIDOR_SIZE);
                    }
                    start = getRandomInt(startLower, startUpper);

                    if (target.y + target.height > source.y) {
                        endLower = (int) (target.y + 1);
                        endUpper = (int) (target.y + Vector2.dst(0, target.y, 0, source.y) - CORRIDOR_SIZE);
                    } else {
                        endLower = (int) (target.y + 1);
                        endUpper = (int) (target.y - 1 + target.height - CORRIDOR_SIZE);
                    }
                    end = getRandomInt(endLower, endUpper);

                    first = new Rectangle(start, end, CORRIDOR_SIZE, Vector2.dst(0, end, 0, source.y));
                    second = new Rectangle(target.x + target.width, end, Vector2.dst(start, 0, target.x + target.width, 0), CORRIDOR_SIZE);

                    source.addEntrance(start, source.y, start + CORRIDOR_SIZE, source.y);
                    target.addEntrance(target.x + target.width, end, target.x + target.width, end + CORRIDOR_SIZE);
                    break;
            }
        }

        return new Rectangle[]{first, second};
    }

    private boolean ensureNoCollisionCorridor(Array<GameRoom> rooms, Array<Rectangle> corridors, Rectangle[] corridor) {

        int roomCount = rooms.size;
        int otherCorridorCount = corridors.size;
        
        Gdx.app.log("CorridorGenerator", "      --> Checking collision with " + roomCount + " rooms and " + otherCorridorCount + " corridors");

        for (int i = 0; i < corridor.length; i++) {
            Rectangle corridorPart = corridor[i];

            // Checking collision with other rooms
            for (int j = 0; j < roomCount; j++) {
                GameRoom room = rooms.get(j);
                
                if (corridorPart.overlaps(room)) return false;
            }

            // Checking collision with other corridors
            for (int k = 0; k < otherCorridorCount; k++) {
                Rectangle otherCorridorPart = corridors.get(k);

                if (corridorPart.overlaps(otherCorridorPart)) return false;
            }
        }

        return true;
    }

    public Array<GameCell> removeUselessCells(Array<GameCell> cells, Array<Rectangle> corridors) {
        int cellCount = cells.size;
        int corridorCount = corridors.size;

        Array<GameCell> remainingCells = new Array<>(cellCount);

        for (int i = 0; i < corridorCount; i++) {
            Rectangle corridorPart = corridors.get(i);

            for (int j = 0; j < cellCount; j++) {
                GameCell cell = cells.get(j);

                if (cell instanceof GameRoom || corridorPart.overlaps(cell)) {
                    if (!remainingCells.contains(cell, false)) remainingCells.add(cell);
                }
            }
        }

        remainingCells.shrink();
        return remainingCells;
    }
}

