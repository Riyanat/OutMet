package com.outmet.analysis;

import ged.editpath.CostLimitExceededException;
import ged.graph.DotParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.outmet.data.Alert;
import com.outmet.data.Graph;
import com.outmet.data.Node;

/**
 * ---------------------------------------------------------------------------
 * Prioritisation Component: Given a set of meta-alerts, a prioritisation value
 * is assigned to each meta-alert based on the degree to which it differs to
 * other meta-alerts.
 * ---------------------------------------------------------------------------
 * 
 * Prioritisation is based on LOF algorithm described in: Breunig et al (2000).
 * "LOF: Identifying Density-based Local Outliers". Proceedings of the 2000 ACM
 * SIGMOD International Conference on Management of Data.
 * 
 * @author riyanat
 * 
 */
public class Prioritiser {
	private static final Logger log = Logger.getLogger(Prioritiser.class
			.getName());
	
	/**
	 * An n-by-n distance matrix where n is the number of meta-alerts.
	 */
	protected double[][] distance;

	/**
	 * The local outlier factor values of each meta-alert where lof[i] belongs
	 * to meta-alert[i].
	 */
	protected double[] lofs;

	/**
	 * The maximum local outlier factor in {lofs}
	 */
	protected double maxLof;

	/**
	 * The number of neighbors required to measure a meta-alerts lof.
	 */
	protected int k;

	/**
	 * The list of meta-alerts to prioritise.
	 */
	protected List<Graph<Alert>> graphs;

	public Prioritiser() {
		this.k = 0;
		this.graphs = new ArrayList<Graph<Alert>>();
	}

	public Prioritiser(int k, List<Graph<Alert>> graphs) {
		this.k = k;
		this.graphs = graphs;
	}

	public void run() {
		calculateDistanceMatrix();
		calculateOutMet();
		updateAlerts();
		printStatistics();
	}

	/**
	 * Computes the distance matrix.
	 */
	protected void calculateDistanceMatrix() {
		int n = graphs.size();
		distance = new double[n][n];

		for (int i = 0; i < n; i++) {
			distance[i] = new double[n];
		}

		for (int i = 0; i < n; i++) {
			for (int j = i; j < n; j++) {
				if (i == j)
					continue;
				try {
					double dist = Distance.calculateGED(graphs.get(i),
							graphs.get(j));
					distance[i][j] = distance[j][i] = Math.max(0.1, dist);
				} catch (DotParseException e) {
					distance[i][j] = distance[j][i] = 1;
				} catch (CostLimitExceededException c) {
					distance[i][j] = distance[j][i] = 1;
				}
			}

		}
	}

	/**
	 * 
	 */
	protected void calculateOutMet() {
		if (k <= 0) {
			return;
		}

		int n = distance.length;
		lofs = new double[n];
		maxLof = 0.0;

		double[][] reachabilityMatrix = new double[n][n];
		double[] lrDensities = new double[n];
		double[] kDistances = new double[n];

		Map<Integer, Map<Integer, Double>> neighborReachabilityMatrix = new HashMap<Integer, Map<Integer, Double>>();

		// Step 1: Derive the k-distances.
		for (int i = 0; i < n; i++) {
			double[] distances = distance[i];
			double[] orderedDistances = Arrays.copyOf(distances, n);

			Arrays.sort(orderedDistances);
			double kDistance = orderedDistances[k - 1];
			kDistances[i] = kDistance;
		}

		// Step 2: Derive the reachability distances
		for (int i = 0; i < n; i++) {

			double[] reachDistances = new double[n];
			double kDistance = kDistances[i];
			Map<Integer, Double> neighborReachabilityDistance = new HashMap<Integer, Double>();

			for (int j = 0; j < n; j++) {
				double d = distance[i][j];
				double max = Math.max(kDistances[j], d);
				reachDistances[j] = max;
				if (d <= kDistance) {
					neighborReachabilityDistance.put(j, max);
				}
			}

			neighborReachabilityMatrix.put(i, neighborReachabilityDistance);
			reachabilityMatrix[i] = reachDistances;

		}

		// Step 3: Calculate local reachability densities.
		double lrd = 0.0;
		for (int i = 0; i < n; i++) {
			double sum = 0;

			for (double d : neighborReachabilityMatrix.get(i).values()) {
				sum += d;
			}

			lrd = 1 / (sum / (neighborReachabilityMatrix.get(i).size()));
			lrDensities[i] = lrd;
		}

		// Step 4: Compute Local Outlier Factor.
		for (int i = 0; i < lrDensities.length; i++) {
			double lrdP = lrDensities[i];
			double sum = 0;

			// get lrd of NNs
			List<Integer> NNs = new ArrayList<Integer>(
					neighborReachabilityMatrix.get(i).keySet());

			for (Integer j : NNs) {

				double lrdo = lrDensities[j];

				sum += lrdo / lrdP;
			}

			lofs[i] = sum / NNs.size();
			if (lofs[i] > maxLof) {
				maxLof = lofs[i];
			}
		}

	}

	/**
	 * Updates alerts with new priorities. Each alert is assigned the priority
	 * of its parent meta-alert.
	 */
	private void updateAlerts() {
		int i = 0;
		for (Graph<Alert> graph : graphs) {
			for (Node<Alert> alert : graph.getNodes()) {
				double l = lofs[i] / maxLof;
				int priority;

				// Maps the scaled lof to discrete priority value where 1 ==
				// high priority, 4 == low priority
				if (l >= 0.75) {
					priority = 4;
				} else if (l >= 0.5) {
					priority = 3;
				} else if (l >= 0.25) {
					priority = 2;
				} else {
					priority = 1;
				}
				alert.getElement().setOutMetPriority(priority);
			}
			i++;
		}
	}

	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

	public double[] getLofs() {
		return lofs;
	}

	public double getMaxLof() {
		return maxLof;
	}

	public void setDistanceMatrix(double[][] distanceMatrix) {
		this.distance = distanceMatrix;
	}

	public double[][] getDistanceMatrix() {
		return distance;
	}

	public void setGraphs(List<Graph<Alert>> graphs) {
		this.graphs = graphs;
	}

	public void getGraphs(List<Graph<Alert>> graphs) {
		this.graphs = graphs;
	}

	private void printStatistics() {
		log.log(Level.INFO, "Prioritised " + graphs.size() + " meta-alerts");
	}

}
