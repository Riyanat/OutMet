package com.outmet.data;

import java.util.Date;

public class Alert {

	private String key;
	private String name;
	private String category;
	private String destPort;
	private String sourcePort;
	private String destIP;
	private String sourceIP;

	private Date startTime;
	private Date endTime;

	private int priority;
	private int outMet;
	private int count;

	private boolean flag;

	public Alert() {
		this.name = "";
		this.category = "";
		this.destPort = "";
		this.sourcePort = "";
		count = 1;
	}

	public Alert(Date startTime, Date endTime, String key, String name,
			String category, String sourceIP,  String sourcePort, String destIP,
			String destPort) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.key = key;
		this.name = name;
		this.category = category;
		this.sourceIP = sourceIP;
		this.sourcePort = sourcePort;
		this.destIP = destIP;
		this.destPort = destPort;
		
		

	}

	public Alert clone() {
		// TODO Auto-generated method stub
		Alert a = new Alert();
		a.setName(name);
		a.setKey(key);
		a.setCategory(category);
		a.setStartTime(startTime);
		a.setEndTime(endTime);
		a.setDestIP(destIP);
		a.setSourceIP(sourceIP);
		a.setDestPort(destPort);
		a.setSourcePort(sourcePort);
		a.setPriority(priority);
		a.setCount(count);
		return a;
	}

	// Setters and Getters
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return this.key;
	}

	public void setCategory(String category) {
		this.category = ("".equals(category) || category == null) ? this.name
				: category;
	}

	public String getCategory() {
		return this.category;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getStartTime() {
		return this.startTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getEndTime() {
		return this.endTime;
	}

	public void setDestIP(String destIP) {
		this.destIP = destIP;
		setFlag(destIP);
	}

	public String getDestIP() {
		return this.destIP;
	}

	public void setSourceIP(String sourceIP) {
		this.sourceIP = sourceIP;
		setFlag(sourceIP);
	}

	public String getSourceIP() {
		return this.sourceIP;
	}

	public void setSourcePort(String sourcePort) {
		try {
			int test = Integer.parseInt(sourcePort);
			if (test >= 1024 && test <= 49151)
				this.sourcePort = "private";

			else if (test >= 49152 && test <= 65535)
				this.sourcePort = "registered";

			else
				this.sourcePort = sourcePort;

		} catch (NumberFormatException e) {
			this.sourcePort = sourcePort;
		}
	}

	public String getSourcePort() {
		return this.sourcePort;
	}

	// TODO : Create Method GetPortRange
	public void setDestPort(String destPort) {
		try {
			int test = Integer.parseInt(destPort);
			if (test >= 1024 && test <= 65535) {
				this.destPort = "private";
			}

			else
				this.destPort = destPort;
		} catch (NumberFormatException e) {
			this.destPort = destPort;
		}
	}

	public String getDestPort() {
		return this.destPort;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return this.priority;
	}

	public void setOutMetPriority(int outMet) {
		this.outMet = outMet;
	}

	public int getOutMetPriority() {
		return outMet;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getCount() {
		return this.count;
	}

	public void setFlag(String s) {
		if (s.contains("66.66")) {
			flag = true;
		}
	}

	public boolean getFlag() {
		return flag;
	}

	@Override
	public boolean equals(Object entity) {
		if (this.toString().equals(entity.toString()))
			return true;

		else
			return false;
	}

	public String toDotString() {
		// creates a node for each attribute
		String tempKey = key.replace("-", "");
		String str = "";
		str += tempKey + "[label = \"type: " + name + "\"frequency = \"" + 1
				+ "\"];";
		str += "srcIP_" + destIP.hashCode()
				+ "[shape = \"rectangle\" label = \"src IP: " + sourceIP
				+ "\"frequency = \"" + 1 + "\"];";
		str += "destIP_" + destIP.hashCode() + "[label = \"dest IP:  " + destIP
				+ "\"frequency = \"" + 1 + "\"];";

		str += "srcIP_" + destIP.hashCode() + "->" + tempKey + "[weight=\"" + 1
				+ "\"];";
		str += "destIP_" + destIP.hashCode() + "->" + tempKey + "[weight=\""
				+ 1 + "\"];";
		return str;
	}

	public String toString() {

		return "startTime: " + startTime + " endTime: " + endTime + " name: "
				+ name + " destIP: " + destIP + " sourceIP: " + sourceIP
				+ " dest Port: " + destPort + "\n";

	}

}
