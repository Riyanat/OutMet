package com.outmet.data;

public class Node<E> {

	/*
	 * key cannot have spaces
	 * label can have spaces
	 * 
	 */
	private E element;
	private String key; 
	private String label;
	private double weight;
	/**
	 * @param args
	 */
	
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
		// Check if key has spaces, if so delete//
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
