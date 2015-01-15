package com.outmet.analysis;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import com.outmet.data.Alert;
import com.outmet.data.Graph;
import com.outmet.data.Node;

public class CorrelatorTest {

	private Graph<Alert> createGraph() {

		Alert alert = new Alert(new Date(), new Date(), "1", "2", "3", "4",
				"5", "6", "7");
		Node<Alert> node = new Node<Alert>();
		node.setElement(alert);
		node.setKey(alert.getKey());
		node.setLabel(alert.getName());
		node.setWeight(alert.getCount());

		Graph<Alert> graph = new Graph<Alert>();
		graph.setKey("0001");
		graph.addNode(node);
		return graph;
	}

	// Test that updateQueue maintains only graphs with activity in the last
	// {threshold} minutes.
	@Test
	public void testUpdateQueue() {

		Correlator correlator = new Correlator();
		correlator.setTimeThreshold(30);

		// Calendar for date manipulation.
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(0);
		cal.set(2012, 12, 8, 0, 0, 0);
		Date date = cal.getTime();

		// Creates a new graph to add to the queue.
		Graph<Alert> firstGraph = createGraph();
		firstGraph.getLastNode().getElement().setEndTime(date);
		firstGraph.getLastNode().getElement().setStartTime(date);

		// Updates the queue.
		correlator.updateQueue(firstGraph);

		// Verify that the graph is added to the queue;
		Graph<Alert> topGraph = correlator.queue.peek();
		assertEquals(firstGraph, topGraph);

		// Add another graph.
		cal.add(Calendar.MINUTE, 20);
		date = cal.getTime();
		Graph<Alert> secondGraph = createGraph();
		secondGraph.getLastNode().getElement().setEndTime(date);
		secondGraph.getLastNode().getElement().setStartTime(date);

		// Updates the queue.
		correlator.updateQueue(secondGraph);

		// Verify that the second graph is added to the queue and that
		// the queue still contains the first graph.
		assertTrue(correlator.queue.contains(secondGraph));
		assertEquals(firstGraph, correlator.queue.peek());

		// Add another graph.
		cal.add(Calendar.MINUTE, 20);
		date = cal.getTime();
		Graph<Alert> thirdGraph = createGraph();
		thirdGraph.getLastNode().getElement().setEndTime(date);
		thirdGraph.getLastNode().getElement().setStartTime(date);

		// Updates the queue.
		correlator.updateQueue(thirdGraph);

		// The third graph outdates the first Graph.
		// Verify that the queue removes the first Graph, keeps the second
		// graph, and adds the third.
		assertNotEquals(firstGraph, correlator.queue.peek());
		assertFalse(correlator.queue.contains(firstGraph));
		assertTrue(correlator.queue.contains(secondGraph));
		assertTrue(correlator.queue.contains(thirdGraph));
	}
}
