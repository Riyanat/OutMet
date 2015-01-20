package com.outmet.data;

public class Edge<E> {
	private String key;
	private String label;
	private double weight;
	private Node<E> source;
	private Node<E> target;

	public Edge() {

	}

	public Edge(String key, String label, Node<E> source, Node<E> target,
			double weight) {
		this.key = key;
		this.label = label;
		this.target = target;
		this.source = source;
		this.weight = weight;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setSource(Node<E> source) {
		this.source = source;
	}

	public Node<E> getSource() {
		return source;
	}

	public void setTarget(Node<E> target) {
		this.target = target;
	}

	public Node<E> getTarget() {
		return target;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public double getWeight() {
		return weight;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public String toString() {
		return "Key : " + key + " Weight : " + weight;

	}

	public String toDotString() {
		return key + "[weight=\"" + weight + "\"];";
	}

}
