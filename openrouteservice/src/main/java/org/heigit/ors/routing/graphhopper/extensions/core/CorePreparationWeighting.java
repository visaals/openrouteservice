package org.heigit.ors.routing.graphhopper.extensions.core;

import com.graphhopper.routing.ch.PreparationWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.CHEdgeIteratorState;
import com.graphhopper.util.EdgeIteratorState;

class CorePreparationWeighting extends PreparationWeighting {
    public CorePreparationWeighting(Weighting userWeighting) {
        super(userWeighting);
    }

    @Override
    public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
        if (edgeState instanceof CHEdgeIteratorState) {
            CHEdgeIteratorState tmp = (CHEdgeIteratorState) edgeState;
            if (tmp.isShortcut())
                // if a shortcut is in both directions the weight is identical => no need for 'reverse'
                return tmp.getTime();
        }

        return super.calcMillis(edgeState, reverse, prevOrNextEdgeId);
    }

}