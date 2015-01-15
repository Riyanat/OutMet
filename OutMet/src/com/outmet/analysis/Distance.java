package com.outmet.analysis;

import ged.editpath.CostLimitExceededException;
import ged.editpath.EditPath;
import ged.editpath.EditPathFinder;
import ged.graph.DecoratedGraph;
import ged.graph.DotParseException;
import ged.graph.GraphConverter;
import ged.processor.CostContainer;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.BitSet;
import java.util.Date;

import com.outmet.data.Alert;
import com.outmet.data.Graph;

/**
 * Provides a range of distance metrics for computing distances/similarities
 * between objects used in outmet. Many of these methods are based on
 * third-party libraries or source code.
 * 
 * @author riyanat
 * 
 */
public class Distance {

	public static final int ACCEPTANCE_LIMIT_COST = 100;

	public static double calculateGED(Graph<Alert> graph_i, Graph<Alert> graph_j)
			throws CostLimitExceededException, DotParseException {

		CostContainer costContainer = new CostContainer();
		costContainer.setAcceptanceLimitCost(new BigDecimal(
				ACCEPTANCE_LIMIT_COST));

		DecoratedGraph first = GraphConverter.parse(graph_i.toDotString());
		DecoratedGraph second = GraphConverter.parse(graph_j.toDotString());

		double cost = 1;

		try {
			EditPath editPath = EditPathFinder.find(first, second,
					costContainer);
			cost = editPath.getCost().doubleValue() / ACCEPTANCE_LIMIT_COST;
		} catch (CostLimitExceededException e) {
			cost = costContainer.getAcceptanceLimitCost().doubleValue()
					/ ACCEPTANCE_LIMIT_COST;
		}
		return cost;
	}


	public static double calculatePortSimilarity(String i, String k) {
		if (i.equals(k)) {
			return 1.0;
		} else {
			return 0.0;
		}
	}

	public static double calculateIpSimilarity(String x, String y) {
		String strThisIP = "";
		String strOtherIP = "";

		BitSet[] bitThisIP;
		BitSet[] bitOtherIP;

		try {
			InetAddress thisIP = InetAddress.getByName(x);
			InetAddress otherIP = InetAddress.getByName(y);

			byte[] thisByte = thisIP.getAddress();
			byte[] otherByte = otherIP.getAddress();

			bitThisIP = new BitSet[thisByte.length];
			bitOtherIP = new BitSet[otherByte.length];

			int i = 0;
			for (byte xB : thisByte) {
				bitThisIP[i] = toBitSet(xB);
				strThisIP += toString(bitThisIP[i]);
				i++;
			}

			int j = 0;
			for (byte yB : otherByte) {
				bitOtherIP[j] = toBitSet(yB);
				strOtherIP += toString(bitOtherIP[j]);
				j++;

			}

			double countVal = 0.0d;
			for (int n = 0; n < strThisIP.length(); n++) {
				if (strThisIP.charAt(n) == strOtherIP.charAt(n)) {
					countVal++;
				} else {
					break;
				}
			}

			double val = countVal / 32.00;
			return val;

		} catch (UnknownHostException e) {
			if (x.toLowerCase().equals(y.toLowerCase()))
				return 1;
			else
				return 0;
		}

	}

	public static String toString(BitSet bits) {
		String out = "";
		for (int n = 0; n < 8; n++) {
			out += bits.get(n) ? "1" : "0";
		}

		return out;
	}

	public static BitSet toBitSet(byte b) {
		BitSet bits = new BitSet(8);
		for (int i = 0; i < 8; i++) {
			bits.set(i, ((b & (1 << i)) != 0));

		}
		return bits;
	}
}
