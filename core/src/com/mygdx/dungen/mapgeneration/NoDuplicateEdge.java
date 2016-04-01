package com.mygdx.dungen.mapgeneration;

import org.jgrapht.graph.DefaultEdge;

/**
 * @author leonard
 *         Date: 6/3/2016
 */
public class NoDuplicateEdge extends DefaultEdge {

    public NoDuplicateEdge() {
        super();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) return false;

        NoDuplicateEdge otherEdge = (NoDuplicateEdge) obj;

        Object otherEdgeSource = otherEdge.getSource();
        Object otherEdgeTarget = otherEdge.getTarget();

        Object thisEdgeSource = this.getSource();
        Object thisEdgeTarget = this.getTarget();

        return (thisEdgeSource.equals(otherEdgeSource) && thisEdgeTarget.equals(otherEdgeTarget)) || (thisEdgeSource.equals(otherEdgeTarget) && thisEdgeTarget.equals(otherEdgeSource));
    }
}
