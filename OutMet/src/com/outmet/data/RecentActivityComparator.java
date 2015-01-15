package com.outmet.data;

import java.util.Comparator;
/**
 * Sorts a set of graphs in ascending order using the last alerts timestamp.
 * i.e. from the earliest inactive graph to the latest.
 * 
 * @author riyanat
 *
 */
public class RecentActivityComparator implements Comparator<Graph<Alert>> {

	@Override
	public int compare(Graph<Alert> graphA, Graph<Alert> graphB) {
		return graphA.getLastNode().getElement().getEndTime()
				.compareTo(graphB.getLastNode().getElement().getEndTime());
	}

}
