package com.ensoftcorp.open.pointsto.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.utils.FormattedSourceCorrespondence;
import com.ensoftcorp.open.pointsto.utilities.PointsToAnalysis;

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
		AtlasSet<Node> allNodesWithPointsToSets = Common.universe().selectNode(Constants.POINTS_TO_SET).eval().nodes();
		double sum = 0;
		for(GraphElement nodeWithPointsToSet : allNodesWithPointsToSets){
			sum += PointsToAnalysis.getPointsToSet(nodeWithPointsToSet).size();
		}
		return sum/allNodesWithPointsToSets.size();
	}
	
	/**
	 * Dumps the entire points-to set to two tables
	 * @param instatiationsFile
	 * @param aliasesFile
	 * @throws IOException
	 */
	public void dumpPointsToTables(File instatiationsFile, File aliasesFile) throws IOException {
		// write out instantiations table
		FileWriter instantiations = new FileWriter(instatiationsFile);
		AtlasSet<GraphElement> allInstantiations = new AtlasHashSet<GraphElement>();
		instantiations.write("Allocation Identifier,File Name,Line Number,Type\n");
		Long numAddresses = (long) addressToInstantiation.keySet().size();
		for(Long address=0L; address<numAddresses; address++){
			GraphElement instantiation = addressToInstantiation.get(address);
			allInstantiations.add(instantiation);
			FormattedSourceCorrespondence fsc = FormattedSourceCorrespondence.getSourceCorrespondent(instantiation);
			String file;
			String line;
			try {
				file = fsc.getRelativeFile();
				line = fsc.getLineNumbers();
			} catch (Exception e){
				line = "N/A";
				try {
					file = fsc.getFile().getName();
				} catch (Exception e2){
					file = "N/A";
				}
			}
			String type = addressToType.get(address).getAttr(XCSG.name).toString();
			instantiations.write(address + "," + file + "," + line + "," + type + "\n");
		}
		instantiations.close();
		
		// write out the alias points-to table
		FileWriter aliases = new FileWriter(aliasesFile);
		aliases.write("File Name,Line Number,Variable,Points-to-set\n");
		AtlasSet<Node> aliasNodes = Common.universe().selectNode(Constants.POINTS_TO_SET)
				.difference(Common.toQ(allInstantiations), Common.universe().nodesTaggedWithAny(XCSG.ArrayComponents))
				.eval().nodes();
		for(GraphElement alias : aliasNodes){
			FormattedSourceCorrespondence fsc = FormattedSourceCorrespondence.getSourceCorrespondent(alias);
			String file;
			String line;
			try {
				file = fsc.getRelativeFile();
				line = fsc.getLineNumbers();
			} catch (Exception e){
				line = "N/A";
				try {
					file = fsc.getFile().getName();
				} catch (Exception e2){
					file = "N/A";
				}
			}
			String name = alias.getAttr(XCSG.name).toString();
			String pointsTo = PointsToResults.getPointsToSet(alias).toString();
			aliases.write(file + "," + line + "," + name + ",\"" + pointsTo + "\"\n");
		}
		aliases.close();
	}
	
}
