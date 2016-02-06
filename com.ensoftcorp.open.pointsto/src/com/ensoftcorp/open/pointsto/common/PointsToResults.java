package com.ensoftcorp.open.pointsto.common;

import java.io.IOException;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.pointsto.utilities.PointsToAnalysis;
import com.ensoftcorp.open.toolbox.commons.FormattedSourceCorrespondence;

public class PointsToResults {

	/**
	 * Computes the average size of each points to set
	 * @return
	 */
	public static double getAverageSizeOfPointsToSets(){
		AtlasSet<GraphElement> allNodesWithPointsToSets = Common.universe().selectNode(Constants.POINTS_TO_SET).eval().nodes();
		double sum = 0;
		for(GraphElement nodeWithPointsToSet : allNodesWithPointsToSets){
			sum += PointsToAnalysis.getPointsToSet(nodeWithPointsToSet).size();
		}
		return sum/allNodesWithPointsToSets.size();
	}
	
	/**
	 * Returns an entry in the points to table as a string
	 * @param ge
	 * @return
	 * @throws IOException 
	 */
	public static String getPointsToTableEntry(GraphElement ge) throws IOException {
		FormattedSourceCorrespondence fsc = FormattedSourceCorrespondence.getSourceCorrespondent(ge);
		String file = fsc.getRelativeFile();
		String line = fsc.getLineNumbers();
		String variable = ge.getAttr(XCSG.name).toString();
		String pointsTo = PointsToAnalysis.getPointsToSet(ge).toString();
		return file + "," + line + "," + variable + "," + pointsTo;
	}
	
}
