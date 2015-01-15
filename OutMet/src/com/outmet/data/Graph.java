package com.outmet.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph<E> {

	private List<Node<E>> nodes;
	private List<Edge<E>> edges;
	private Map<String, String> tags;
	private String key;
	//List
	/**
	 * @param args
	 */
	
	public Graph(){
		nodes = new ArrayList<Node<E>>();
		edges = new ArrayList<Edge<E>>();
		tags = new HashMap<String, String>();
	}
	
	public void addNode(Node<E> node){
		// check if already contains node
		nodes.add(node);
	}
	
	public Node<E> getFirstNode(){
		return nodes.get(0);
	}
	
	public Node<E> getLastNode(){
		return nodes.get(nodes.size()-1);
	}
	
	public List<Node<E>> getNodes(){
		return nodes;
	}
	
	public void addEdge(Edge<E> edge){
		edges.add(edge);
	}
	
	public List<Edge<E>> getEdges(){
		return edges;
	}
	
	public void addTag(String key, String value){
		tags.put(key, value);
	}
	
	public Map<String, String> getTags() {
		return tags;
	}

	public void setTags(Map<String, String> tags) {
		this.tags = tags;
	}

	public void setKey(String key){
		this.key = key;
	}
	
	public String getKey(){
		return this.key;
	}
	
	public String toString(){
		return "\nKey: " + key + "\n"+
				"Nodes \n" + nodes.toString() + "\n" +
				"Edges \n" + edges.toString()+"\n";
	}
	
	
	public String toDotString(){
		String graph = "digraph 1{ ";
		
		// Aggregate Nodes
		Map<String, Node<E>> groupedNodes = new HashMap<String, Node<E>>();
		
		for(Node<E> node: nodes){
			if (groupedNodes.containsKey(node.getLabel())){
				Node<E> n = groupedNodes.get(node.getLabel());
				groupedNodes.get(node.getLabel()).setWeight(n.getWeight()+node.getWeight());
			}else{
				Node<E> dotNode = new Node<E>();
				dotNode.setKey(String.valueOf(groupedNodes.size()));
				dotNode.setLabel(node.getLabel());
				groupedNodes.put(node.getLabel(), dotNode);					
			}
		}
		
		for (Node<E> node : groupedNodes.values()){
			graph += node.toDotString();
		}
		
		// Aggregate Nodes
		Map<String, Edge<E>> dotEdges = new HashMap<String, Edge<E>>();
		for(Edge<E> edge: edges){
			if (dotEdges.containsKey(edge.getLabel())){
				Edge<E> e = dotEdges.get(edge.getLabel());
				dotEdges.get(edge.getLabel()).setWeight(e.getWeight()+edge.getWeight());
			}else{
				Edge<E> dotEdge = new Edge<E>();
				dotEdge.setKey(groupedNodes.get(edge.getSource().getLabel()).getKey() + "->" + groupedNodes.get(edge.getTarget().getLabel()).getKey());
				dotEdge.setLabel(edge.getLabel());
				dotEdges.put(edge.getLabel(), dotEdge);					
			}
		}
		
		for (Edge<E> edge : dotEdges.values()){
			graph += edge.toDotString();
		}
		
		graph += "}";
		
		return graph;
	}

}
