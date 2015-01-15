package com.outmet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.outmet.analysis.Correlator;
import com.outmet.analysis.Prioritiser;
import com.outmet.data.Alert;

/***
 * 
 * 
 * OutMet is a metric for prioritising intrusion alerts using correlation
 * and outlier analysis.
 * 
 * Given a set of intrusion alerts, OutMet assigns an outlier value to each
 * alert which can be used to indicate how high or low an alert should be
 * prioritised. First, alerts are grouped into meta-alerts using the correlation
 * component. Next, the prioritisation component computes the outlier degree of
 * each meta-alert.
 * 
 * More information: Shittu et al OutMet: A new metric for prioritising
 * intrusion alerts using correlation and outlier analysis, 2014 IEEE 39th
 * Conference on Local Computer Networks
 * 
 * @author riyanat
 * 
 */


/**
 * Demos how outmet works. (Uses default settings)
 * This demo assumes all input alerts in the csv are sorted by start time
 */
public class Demo {
	private static final Logger log = Logger.getLogger(Demo.class
			.getName());
	
	private static List<Alert> readAlertsFromCSV(String filename,
			String delimiter) {
		List<Alert> alerts = new ArrayList<Alert>();
		BufferedReader br = null;
		String line = "";

		try {
			br = new BufferedReader(new FileReader(filename));
			line = br.readLine();
			while (line != null) {
				// Each alert is represented on a line.
				String[] lineSplit = line.split(delimiter);

				// Each line should have 9 features for each alert.
				if (lineSplit.length == 9) {
					Date startDate = new Date(Long.parseLong(lineSplit[0]));
					Date endDate = new Date(Long.parseLong(lineSplit[1]));
					Alert alert = new Alert(startDate, endDate, lineSplit[2],
							lineSplit[3], lineSplit[4], lineSplit[5],
							lineSplit[6], lineSplit[7], lineSplit[8]);
					alerts.add(alert);
				}
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		log.log(Level.INFO, "Read " + alerts.size() + " alerts from: " + filename);
		return alerts;
	}

	public static void main(String[] args) {
		Correlator correlator = new Correlator();
		correlator.run(readAlertsFromCSV("data/sample_alerts.csv", ","));

		Prioritiser prioritiser = new Prioritiser(2, 5, correlator.getGraphs());
		prioritiser.run();
	}
}
