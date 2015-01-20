package com.outmet.analysis;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.outmet.data.Alert;
import com.outmet.data.Edge;
import com.outmet.data.Graph;
import com.outmet.data.Node;
import com.outmet.data.RecentActivityComparator;

/**
 * ----------------------------------------------------------------------------
 * 
 * Correlation Component: A correlation approach for finding relationships
 * between a set of IDS alerts and grouping alerts into graph-like structures
 * called meta-alerts.
 * 
 * ---------------------------------------------------------------------------
 * 
 * Summary: Receives a List of sorted (by time) Alert Objects A = {a_1, a_2,
 * ..., a_n}. Compares each Alert a_i, with a set of Alert objects a_j \in A
 * where a_i meets a set of minimum candidate requirements. If a_i and a_j have
 * a correlation metric greater than threshold t, both are considered
 * correlated, transformed into nodes where an edge is drawn between them.
 * 
 * 
 * 
 * Complexity 0(n^2)
 * 
 * @author riyanat
 * 
 */
public class Correlator {

	private static final Logger log = Logger.getLogger(Correlator.class
			.getName());

	/**
	 * The output of graphs created from the input alerts.
	 */
	private List<Graph<Alert>> graphs;

	/**
	 * A Priority queue containing all graphs with activity in the last
	 * {timeThreshold} seconds.
	 */
	protected PriorityQueue<Graph<Alert>> queue;

	/**
	 * The minimum threshold allowed to correlate two alerts.
	 */
	private double correlationThreshold;

	/**
	 * The minimum time threshold allowed to correlate two alerts in minutes.
	 * 
	 */
	private int timeThreshold;

	/**
	 * The weight of the time feature.
	 */
	private double timeProximityWeight;

	/**
	 * The weight of the source ip feature.
	 */
	private double sourceIpWeight;

	/**
	 * The weight of the destination ip feature.
	 */
	private double destIpWeight;

	/**
	 * The weight of the destination port feature.
	 */
	private double destPortWeight;

	/**
	 * The total number of graphs discovered.
	 */

	private int total;

	/**
	 * Default constructor with default settings.
	 */
	public Correlator() {
		graphs = new ArrayList<Graph<Alert>>();
		queue = new PriorityQueue<Graph<Alert>>(100,
				new RecentActivityComparator());
		sourceIpWeight = 1;
		destIpWeight = 1;
		destPortWeight = 1;
		timeProximityWeight = 1;
		timeThreshold = 30;
		correlationThreshold = 0.8;
		total = 0;

	}

	/**
	 * Runs the correlation component and logs meta-alert summaries.
	 * 
	 * @param alertStream
	 */
	public void run(List<Alert> alertStream) {
		for (Alert alert : alertStream) {
			if (graphs.isEmpty() || queue.isEmpty()) {
				createGraph(alert);
			} else {
				correlate(alert);
			}
		}
		printStatistics();
	}

	/**
	 * Creates a single-node graph where 'alert' is the only node in the graph.
	 */
	private void createGraph(Alert alert) {
		Node<Alert> node = new Node<Alert>();
		node.setElement(alert);
		node.setKey(alert.getKey());
		node.setLabel(alert.getName());
		node.setWeight(alert.getCount());

		Graph<Alert> graph = new Graph<Alert>();
		graph.setKey(String.valueOf(total));
		graph.addNode(node);
		graphs.add(graph);
		updateQueue(graph);

		total++;
	}

	/**
	 * Updates the queue of alerts used to find candidate correlations for newer
	 * alerts. The most recent graph is added and in active graphs are removed.
	 * 
	 * @param graph
	 */
	protected void updateQueue(Graph<Alert> graph) {
		// Add the current graph if not already in queue.
		if (!queue.contains(graph)) {
			queue.add(graph);
		}

		// Remove all outdated graphs.
		Date endTime = graph.getLastNode().getElement().getEndTime();

		Calendar cal = Calendar.getInstance();
		cal.setTime(endTime);
		cal.add(Calendar.MINUTE, -(timeThreshold));

		Graph<Alert> earliestGraph = queue.poll();
		Date earliestQueueDateAllowed = cal.getTime();
		Date earliestQueueDate = earliestGraph.getLastNode().getElement()
				.getEndTime();

		while (!queue.isEmpty()
				&& earliestQueueDate.before(earliestQueueDateAllowed)) {
			earliestGraph = queue.poll();
			earliestQueueDate = earliestGraph.getLastNode().getElement()
					.getEndTime();

		}

		// Place earliest node back into queue.
		queue.add(earliestGraph);
	}

	/**
	 * Compares alert_k (a newer alert) with older (yet active) alerts. If
	 * alert_k cannot be correlated to any older alert, a new meta-alert is
	 * formed.
	 * 
	 * @param alert_k
	 */
	private void correlate(Alert alert_k) {
		// check the queue
		Graph<Alert> closestGraph = null;
		Alert closestAlert = null;
		Node<Alert> closestNode = null;

		double maxThreshold = correlationThreshold;

		Calendar cal = Calendar.getInstance();
		cal.setTime(alert_k.getStartTime());
		cal.add(Calendar.MINUTE, -timeThreshold);

		Date earliestDateAllowed = cal.getTime();

		PriorityQueue<Graph<Alert>> newestQueue = new PriorityQueue<Graph<Alert>>(
				100, new RecentActivityComparator());
		
		while (!queue.isEmpty()) {
			Graph<Alert> graph = queue.poll();
			
			// Uses the time threshold to determine candidate correlations.
			Date endDate = graph.getLastNode().getElement().getStartTime();
			if (endDate.after(earliestDateAllowed)) {
				Node<Alert> node_i = graph.getLastNode();
				Alert alert_i = node_i.getElement();

				double corr = getCorr(alert_i, alert_k);

				// Finds graph with maximum and closest correlation
				if (corr >= maxThreshold) {
					closestGraph = graph;
					closestAlert = alert_i;
					closestNode = node_i;
					maxThreshold = corr;
				}
				
				// Dequeue and queue update.
				newestQueue.add(graph);
			}

		}
		queue = newestQueue;

		// This alert cannot be correlated with any existing alerts.
		if (closestGraph == null || closestAlert == null) {
			createGraph(alert_k);

		} else {
			// This alert can be correlated with {closestGraph}.
			Node<Alert> newNode = new Node<Alert>();
			newNode.setElement(alert_k);
			newNode.setKey(alert_k.getKey());
			newNode.setLabel(alert_k.getName());
			newNode.setWeight(alert_k.getCount());
			closestGraph.addNode(newNode);

			// create an edge between new node and closestAlert
			Edge<Alert> newEdge = new Edge<Alert>();
			newEdge.setKey(newNode.getKey() + "->" + closestNode.getKey());
			newEdge.setLabel(newNode.getLabel() + "->" + closestNode.getLabel());
			newEdge.setSource(closestNode);
			newEdge.setTarget(newNode);
			newEdge.setWeight(maxThreshold);
			closestGraph.addEdge(newEdge);

		}
	}

	protected double getCorr(Alert alert_i, Alert alert_k) {
		double corr = 0;

		// Similarity when source_ip(alert_i) is compared to source_ip(alert_k)
		// and dest_ip(alert_i) is compared to dest_ip(alert_k)
		double dist1 = Distance.calculateIpSimilarity(alert_i.getSourceIP(),
				alert_k.getSourceIP())
				* sourceIpWeight
				+ Distance.calculateIpSimilarity(alert_i.getSourceIP(),
						alert_k.getSourceIP()) * destIpWeight;

		// Similarity when dest_ip(alert_i) is compared to source_ip(alert_k)
		// and dest_ip(alert_i) is compared to source_ip(alert_k)
		double dist2 = Distance.calculateIpSimilarity(alert_i.getDestIP(),
				alert_k.getSourceIP())
				* destIpWeight
				+ Distance.calculateIpSimilarity(alert_i.getSourceIP(),
						alert_k.getDestIP()) * sourceIpWeight;

		corr += Math.max(dist1, dist2);

		double t = (Math.abs(alert_k.getStartTime().getTime()
				- alert_i.getStartTime().getTime()) / timeThreshold);
		corr += 1 / Math.pow(Math.E, t);

		corr += Distance.calculatePortSimilarity(alert_i.getDestPort(),
				alert_k.getDestIP())
				+ destPortWeight;

		double weightSum = sourceIpWeight + destIpWeight + destPortWeight
				+ timeProximityWeight;

		return corr / weightSum;
	}

	// Setters and getters.
	public int getTimeThreshold() {
		return timeThreshold;
	}

	public void setTimeThreshold(int timeThreshold) {
		this.timeThreshold = timeThreshold;
	}

	public double getCorrelationThreshold() {
		return correlationThreshold;
	}

	public void setCorrelationThreshold(double similarityThreshold) {
		this.correlationThreshold = similarityThreshold;
	}

	public double getDestIpWeight() {
		return destIpWeight;
	}

	public double getDestPortWeight() {
		return destPortWeight;
	}

	public double getSourceIpWeight() {
		return sourceIpWeight;
	}

	public double getTimeProximityWeight() {
		return timeProximityWeight;
	}

	public List<Graph<Alert>> getGraphs() {
		return graphs;
	}

	public void printStatistics() {
		log.log(Level.INFO, "Derived " + graphs.size() + " meta-alerts");
	}

}