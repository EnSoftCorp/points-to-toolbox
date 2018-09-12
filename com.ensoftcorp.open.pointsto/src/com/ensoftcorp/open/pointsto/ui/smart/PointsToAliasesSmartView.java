package com.ensoftcorp.open.pointsto.ui.smart;

import java.awt.Color;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.highlight.Highlighter;
import com.ensoftcorp.atlas.core.markup.MarkupFromH;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.FrontierStyledResult;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.scripts.selections.IExplorableScript;
import com.ensoftcorp.atlas.ui.scripts.selections.IResizableScript;
import com.ensoftcorp.atlas.ui.scripts.util.SimpleScriptUtil;
import com.ensoftcorp.atlas.ui.selection.event.FrontierEdgeExploreEvent;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.pointsto.common.PointsToAnalysis;
import com.ensoftcorp.open.pointsto.log.Log;
import com.ensoftcorp.open.pointsto.preferences.PointsToPreferences;

public class PointsToAliasesSmartView extends FilteringAtlasSmartViewScript implements IResizableScript, IExplorableScript {

	@Override
	protected String[] getSupportedNodeTags() {
		return new String[]{XCSG.DataFlow_Node};
	}

	@Override
	protected String[] getSupportedEdgeTags() {
		return NOTHING;
	}
	
	@Override
	public String getTitle() {
		return "Points-to Aliases";
	}
	
	@Override
	public FrontierStyledResult explore(FrontierEdgeExploreEvent event, FrontierStyledResult oldResult) {
		return SimpleScriptUtil.explore(this, event, oldResult);
	}

	@Override
	public FrontierStyledResult evaluate(IAtlasSelectionEvent event, int reverse, int forward) {
		Q filteredSelection = filter(event.getSelection());

		boolean useInferredDataFlow = PointsToPreferences.isTagInferredDataflowsEnabled();
		
		// for each selected graph element get the corresponding instantiation
		// as determined by the points-to analysis
		AtlasSet<Node> instantiationSet = new AtlasHashSet<Node>();
		AtlasSet<Node> aliasNodes = new AtlasHashSet<Node>();
		for(Node node : filteredSelection.eval().nodes()){
			Q aliases = PointsToAnalysis.getAliases(node);
			if(!useInferredDataFlow) {
				aliasNodes.addAll(aliases.eval().nodes());
			}
			AtlasSet<Node> instantiations = aliases.nodesTaggedWithAny(XCSG.Instantiation, XCSG.ArrayInstantiation).eval().nodes();
			if(!instantiations.isEmpty()){
				instantiationSet.addAll(instantiations);
			} else {
				Log.warning("No known instantation for reference: " + node.address().toAddressString());
			}
		}
		
		Q inferredDF;
		if(useInferredDataFlow) {
			// this is easier to work with
			inferredDF = Common.universe().edges(PointsToAnalysis.INFERRED_DATA_FLOW);
		} else {
			// however if tagging was turned off we could fall back to inducing the edges from regular data flow
			inferredDF = Common.toQ(aliasNodes).induce(Common.universe().edges(XCSG.DataFlow_Edge));
		}
		
		Q instantations = Common.toQ(instantiationSet);
		
		Highlighter h = new Highlighter();
		h.highlight(instantations, Color.RED);

		// return a graph that contains the selected references in blue,
		// and the points-to instantiations highlighted red, with inferred
		// data flow edges between the instantiations and the selections
		// by default the data flow edges are not shown until forward/backward
		// steps are increased. Union instantiations in, for null's since they
		// won't have a between edge
		Q completeResult = instantations.union(inferredDF.between(instantations, filteredSelection));
		
		// compute what to show for current steps
		Q f = instantations.forwardStepOn(completeResult, forward);
		Q r = filteredSelection.reverseStepOn(completeResult, reverse);
		Q result = f.union(r).union(filteredSelection);
		
		// compute what is on the frontier
		Q frontierForward = instantations.forwardStepOn(completeResult, forward+1);
		Q frontierReverse = filteredSelection.reverseStepOn(completeResult, reverse+1);
		frontierForward = frontierForward.retainEdges().differenceEdges(result);
		frontierReverse = frontierReverse.retainEdges().differenceEdges(result);

		return new com.ensoftcorp.atlas.core.script.FrontierStyledResult(result, frontierReverse, frontierForward, new MarkupFromH(h));
	}

	@Override
	public int getDefaultStepBottom() {
		return 0;
	}

	@Override
	public int getDefaultStepTop() {
		return 0;
	}

	@Override
	protected StyledResult selectionChanged(IAtlasSelectionEvent input, Q filteredSelection) {
		return null;
	}

}
