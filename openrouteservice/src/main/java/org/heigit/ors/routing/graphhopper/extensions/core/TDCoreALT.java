/*  This file is part of Openrouteservice.
 *
 *  Openrouteservice is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.

 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details.

 *  You should have received a copy of the GNU Lesser General Public License along with this library;
 *  if not, see <https://www.gnu.org/licenses/>.
 */
package org.heigit.ors.routing.graphhopper.extensions.core;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.AStar;
import com.graphhopper.routing.EdgeIteratorStateHelper;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.BeelineWeightApproximator;
import com.graphhopper.routing.weighting.ConsistentWeightApproximator;
import com.graphhopper.routing.weighting.WeightApproximator;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.*;

import java.util.PriorityQueue;


/**
 * Calculates best path using CH routing outside core and ALT inside core.
 *
 * This code is based on that from GraphHopper GmbH.
 *
 * @author Peter Karich
 * @author jansoe
 * @author Andrzej Oles
 */

public class TDCoreALT extends CoreALT {
    Weighting coreWeighting;
    private boolean reverse;
    private long at;

    private PriorityQueue<AStarEntry> sourcePriorityQueueCore;
    private PriorityQueue<AStarEntry> targetPriorityQueueCore;

    public TDCoreALT(Graph graph, Weighting weighting, Weighting coreWeighting, TraversalMode tMode) {
        super(graph, weighting, tMode);
        this.coreWeighting = weighting;
    }

    @Override
    public Path calcPath(int from, int to, long at) {
        this.at = at;
        return super.calcPath(from, to);
    }

    @Override
    protected void recalculateWeights(PriorityQueue<AStarEntry> queue, boolean backward) {
        AStarEntry[] entries = queue.toArray(new AStarEntry[queue.size()]);
        queue.clear();
        for (AStarEntry value : entries) {
            value.weight = value.weightOfVisitedPath + weightApprox.approximate(value.adjNode, backward);
            if (backward==reverse)
                value.time = at + (reverse ? -1 : 1) * value.time;
            queue.add(value);
        }
    }

    void fillEdges(AStarEntry currEdge, PriorityQueue<AStarEntry> prioQueue, IntObjectMap<AStarEntry> bestWeightMap,
                   EdgeExplorer explorer, boolean reverse) {
        EdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);
        while (iter.next()) {
            if (!accept(iter, currEdge.edge))
                continue;
            int traversalId = traversalMode.createTraversalId(iter, reverse);
            // Modification by Maxim Rylov: use originalEdge as the previousEdgeId
            double tmpWeight = weighting.calcWeight(iter, reverse, currEdge.originalEdge) + currEdge.weight;
            if (Double.isInfinite(tmpWeight))
                continue;
            AStarEntry aStarEntry = bestWeightMap.get(traversalId);
            if (aStarEntry == null) {
                aStarEntry = new AStarEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight, tmpWeight);
                // Modification by Maxim Rylov: Assign the original edge id.
                aStarEntry.originalEdge = EdgeIteratorStateHelper.getOriginalEdge(iter);
                bestWeightMap.put(traversalId, aStarEntry);
            } else if (aStarEntry.weight > tmpWeight) {
                prioQueue.remove(aStarEntry);
                aStarEntry.edge = iter.getEdge();
                aStarEntry.weight = tmpWeight;
                aStarEntry.weightOfVisitedPath = tmpWeight;
            } else
                continue;

            aStarEntry.parent = currEdge;
            aStarEntry.time = weighting.calcMillis(iter, reverse, currEdge.edge, currEdge.time) + currEdge.time;
            prioQueue.add(aStarEntry);

            if (doUpdateBestPath)
                updateBestPath(iter, aStarEntry, traversalId);
        }
    }

    @Override
    boolean fillEdgesFromALT() {
        if (reverse)
            return true;
        if (fromPriorityQueueCore.isEmpty())
            return false;

        currFrom = fromPriorityQueueCore.poll();
        bestWeightMapOther = bestWeightMapTo;
        fillEdgesALT(currFrom, fromPriorityQueueCore, bestWeightMapFrom, ignoreExplorationFrom, outEdgeExplorer, false);
        visitedCountFrom2++;
        return true;
    }

    @Override
    boolean fillEdgesToALT() {
        if (!reverse)
            return true;
        if (toPriorityQueueCore.isEmpty())
            return false;

        currTo = toPriorityQueueCore.poll();
        bestWeightMapOther = bestWeightMapFrom;
        fillEdgesALT(currTo, toPriorityQueueCore, bestWeightMapTo, ignoreExplorationTo, inEdgeExplorer, true);
        visitedCountTo2++;
        return true;
    }

    @Override
    void fillEdgesALT(AStarEntry currEdge, PriorityQueue<AStarEntry> prioQueueOpenSet,
                      IntObjectMap<AStarEntry> bestWeightMap, IntHashSet ignoreExploration, EdgeExplorer explorer,
                      boolean reverse) {

        int currNode = currEdge.adjNode;
        EdgeIterator iter = explorer.setBaseNode(currNode);
        while (iter.next()) {
            if (!accept(iter, currEdge.edge))
                continue;
            visitedEdgesALTCount++;

            int neighborNode = iter.getAdjNode();
            int traversalId = traversalMode.createTraversalId(iter, reverse);
            if (ignoreExploration.contains(traversalId))
                continue;

            double alreadyVisitedWeight = coreWeighting.calcWeight(iter, reverse, currEdge.originalEdge, currEdge.time)
                    + currEdge.getWeightOfVisitedPath();
            if (Double.isInfinite(alreadyVisitedWeight))
                continue;

            AStarEntry aStarEntry = bestWeightMap.get(traversalId);
            if (aStarEntry == null || aStarEntry.getWeightOfVisitedPath() > alreadyVisitedWeight) {
                double currWeightToGoal = weightApprox.approximate(neighborNode, reverse);
                double estimationFullWeight = alreadyVisitedWeight + currWeightToGoal;
                if (aStarEntry == null) {
                    aStarEntry = new AStarEntry(iter.getEdge(), neighborNode, estimationFullWeight, alreadyVisitedWeight);
                    // Modification by Maxim Rylov: assign originalEdge
                    aStarEntry.originalEdge = EdgeIteratorStateHelper.getOriginalEdge(iter);
                    bestWeightMap.put(traversalId, aStarEntry);
                } else {
                    prioQueueOpenSet.remove(aStarEntry);
                    aStarEntry.edge = iter.getEdge();
                    aStarEntry.weight = estimationFullWeight;
                    aStarEntry.weightOfVisitedPath = alreadyVisitedWeight;
                }
                aStarEntry.time = currEdge.time + (reverse ? -1 : 1) * coreWeighting.calcMillis(iter, reverse, currEdge.edge, currEdge.time);
                aStarEntry.parent = currEdge;
                prioQueueOpenSet.add(aStarEntry);

                if (doUpdateBestPath)
                    updateBestPath(iter, aStarEntry, traversalId);
            }
        }
    }

    public void reverse() {
        reverse = !reverse;
    }

    @Override
    public String getName() {
        return "td_calt" + "|" + weightApprox;
    }

}
