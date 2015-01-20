package com.outmet.data;

public class Node<E> {


	private E element;
	
	/**
	 * Unique key of node. Whitespace not allowed.
	 */
	private String key;
	
	/**
	 * 	Label of node. Whitespace allowed.
	 */
	private String label;
	
	private double weight;

	public Node(){
		
	}
	
	public Node(E element, String key, String label, double weight){
	
		this.element = element;
		this.key = key;
		this.label = label;
		this.weight = weight;
		
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		key.replaceAll(" ", "---");
		this.key = key;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public void setElement(E element){
		this.element = element;
	}
	
	public E getElement(){
		return element;
	}
	
	public String toString(){
		return element.toString();
	}
	
	public String toDotString(){
		return key + "[label = \"" + label + "\"frequency = \"" + weight + "\"];";		
	}

}
