package com.ensoftcorp.open.pointsto.common;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.pointsto.utilities.PointsToAnalysis;
import com.ensoftcorp.open.toolbox.commons.FormattedSourceCorrespondence;

/**
 * Provides client access to the results of points-to analysis and some
 * statistics utilities
 * 
 * @author Ben Holland
 */
public class PointsToResults {

	@SuppressWarnings("unchecked")
	private PointsToResults() {
		// TODO: remove super nasty hack workaround for class loader issues...
		GraphElement pointsToResults = Common.universe().nodesTaggedWithAny("points-to-results").eval().nodes().getFirst();
		addressToInstantiation = (Map<Long, GraphElement>) pointsToResults.getAttr("addressToInstantiation");
		addressToType = (Map<Long, GraphElement>) pointsToResults.getAttr("addressToType");
		arrayMemoryModel = (Map<Long, HashSet<Long>>) pointsToResults.getAttr("arrayMemoryModel");
		inferredDataFlowGraph = (Q) pointsToResults.getAttr("inferredDataFlowGraph");
	}

	private static PointsToResults instance = null;

	public static PointsToResults getInstance() {
		// TODO: fix
		// intentionally violating singleton pattern because of classloader issues
//		if (instance == null) {
			instance = new PointsToResults();
//		}
		return instance;
	}
	
	public Map<Long, GraphElement> addressToInstantiation;
	public Map<Long, GraphElement> addressToType;
	public Map<Long, HashSet<Long>> arrayMemoryModel;
	public Q inferredDataFlowGraph;
	
	/**
	 * Returns the points-to address set for a given data flow node
	 * @param ge
	 * @return
	 */
	public static Set<Long> getPointsToSet(GraphElement ge){
		if(ge.hasAttr(Constants.POINTS_TO_SET)){
			HashSet<Long> result = new HashSet<Long>();
			for(Long address : PointsToAnalysis.getPointsToSet(ge)){
				result.add(address);
			}
			return result;
		} else {
			return new HashSet<Long>();
		}
	}
	
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
