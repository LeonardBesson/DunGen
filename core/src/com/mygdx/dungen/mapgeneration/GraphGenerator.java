package com.mygdx.dungen.mapgeneration;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import com.mygdx.dungen.GameCell;
import com.mygdx.dungen.GameMap;
import com.mygdx.dungen.GameRoom;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.graph.Multigraph;

import java.util.HashSet;
import java.util.Set;

/**
 * @author leonard
 *         Date: 22/2/2016
 */
public class GraphGenerator {

    public GraphGenerator() {}

    public Multigraph<GameRoom, NoDuplicateEdge> generateMinimalSpanningTree(Array<GameCell> cells, float remainingEdgesMultiplier) {
        Gdx.app.log("GraphGenerator", "----------------- Generating minimal spanning tree -----------------");

        Array<GameRoom> rooms = GameMap.extractRooms(cells);
        ShortArray trianglesIndices = generateTrianglesIndices(cells);

        Multigraph<GameRoom, NoDuplicateEdge> graph = getGraphFromTriangulation(trianglesIndices, rooms);

        Set<NoDuplicateEdge> minEdges = new KruskalMinimumSpanningTree(graph).getMinimumSpanningTreeEdgeSet();
//        Set<NoDuplicateEdge> minEdges = new PrimMinimumSpanningTree(graph).getMinimumSpanningTreeEdgeSet();

        minimumSpanningTreeFromGraph(graph, minEdges, remainingEdgesMultiplier);

        Gdx.app.log("GraphGenerator", "  --> Graph is now a minimal spanning tree. Edge count: " + graph.edgeSet().size());

        return graph;
    }

    private void minimumSpanningTreeFromGraph(Multigraph<GameRoom, NoDuplicateEdge> graph, Set<NoDuplicateEdge> minEdges, float remainingEdgesMultiplier) {

        Gdx.app.log("GraphGenerator", "  --> Removing useless edges from graph");

        Set<NoDuplicateEdge> allEdges = graph.edgeSet();

        HashSet<NoDuplicateEdge> unusedEdges = new HashSet<>(allEdges);
        unusedEdges.removeAll(minEdges);

        int minEdgeSize = minEdges.size();
        int remainingEdgeCount = Math.round(minEdgeSize * remainingEdgesMultiplier);
        Gdx.app.log("GraphGenerator", "  --> " + remainingEdgeCount + " (" + minEdgeSize + " x " + remainingEdgesMultiplier + ") " + (remainingEdgeCount > 1 ? "edges" : "edge") + " will remain from the triangulation graph");
        remainingEdgeCount = MathUtils.clamp(remainingEdgeCount, 0, unusedEdges.size());
        Gdx.app.log("GraphGenerator", "      --> Clamped to " + remainingEdgeCount);

        while (remainingEdgeCount > 0) {
            int remainingEdgeIndex = new RandomDataGenerator().nextInt(0, unusedEdges.size() - 1);

            NoDuplicateEdge edge = (NoDuplicateEdge) unusedEdges.toArray()[remainingEdgeIndex];

            if (minEdges.contains(edge)) continue;

            unusedEdges.remove(edge);

            remainingEdgeCount--;
        }

        unusedEdges.forEach(graph::removeEdge);
    }

    private Multigraph<GameRoom, NoDuplicateEdge> getGraphFromTriangulation(ShortArray trianglesIndices, Array<GameRoom> rooms) {

        Gdx.app.log("GraphGenerator", "  --> Generating graph from triangulation");

        Multigraph<GameRoom, NoDuplicateEdge> graph = new Multigraph<>(NoDuplicateEdge.class);

        int indexCount = trianglesIndices.size;

        for (int i = 2; i < indexCount; i += 3) {
            short index1 = trianglesIndices.get(i - 2);
            short index2 = trianglesIndices.get(i - 1);
            short index3 = trianglesIndices.get(i);

            GameRoom room1 = rooms.get(index1);
            GameRoom room2 = rooms.get(index2);
            GameRoom room3 = rooms.get(index3);

            graph.addVertex(room1);
            graph.addVertex(room2);
            graph.addVertex(room3);

            if (!graph.containsEdge(room1, room2)) graph.addEdge(room1, room2);
            if (!graph.containsEdge(room2, room3)) graph.addEdge(room2, room3);
            if (!graph.containsEdge(room3, room1)) graph.addEdge(room3, room1);

        }

        return graph;
    }


    private ShortArray generateTrianglesIndices(Array<GameCell> cells) {

        Gdx.app.log("GraphGenerator", "  --> Generating Delaunay Triangulation");

        float[] roomCenterPoints = getRoomCenterPoints(cells);

        return new DelaunayTriangulator().computeTriangles(roomCenterPoints, false);
    }

    private float[] getRoomCenterPoints(Array<GameCell> cells) {

        Gdx.app.log("GraphGenerator", "      --> Getting rooms center points");

        int cellCount = cells.size;
        FloatArray points = new FloatArray(cellCount);
        Vector2 cellCenter = new Vector2();

        for (int i = 0; i < cellCount; i++) {
            GameCell cell = cells.get(i);

            if (!(cell instanceof GameRoom)) continue;

            cell.getCenter(cellCenter);

            points.add(cellCenter.x);
            points.add(cellCenter.y);
        }

        points.shrink();
        return points.items;
    }
}
