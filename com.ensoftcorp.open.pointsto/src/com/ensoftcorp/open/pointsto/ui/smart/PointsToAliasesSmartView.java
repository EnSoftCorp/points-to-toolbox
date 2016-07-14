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
import com.ensoftcorp.atlas.ui.scripts.selections.IResizableScript;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.pointsto.common.PointsToAnalysis;
import com.ensoftcorp.open.pointsto.log.Log;

public class PointsToAliasesSmartView extends FilteringAtlasSmartViewScript implements IResizableScript {

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
	public FrontierStyledResult evaluate(IAtlasSelectionEvent event, int reverse, int forward) {
		Q filteredSelection = filter(event.getSelection());

		// for each selected graph element get the corresponding instantiation
		// as determined by the points-to analysis
		AtlasSet<Node> instantiationSet = new AtlasHashSet<Node>();
		for(Node node : filteredSelection.eval().nodes()){
			Q aliases = Common.toQ(PointsToAnalysis.getAliases(node));
			AtlasSet<Node> instantiations = aliases.nodesTaggedWithAny(XCSG.Instantiation, XCSG.ArrayInstantiation).eval().nodes();
			if(!instantiations.isEmpty()){
				instantiationSet.addAll(instantiations);
			} else {
				Log.warning("No known instantation for reference: " + node.address().toAddressString());
			}
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
		Q inferredDF = Common.universe().edgesTaggedWithAny(PointsToAnalysis.INFERRED_DATA_FLOW);
		Q completeResult = instantations.union(inferredDF.forward(instantations).intersection(inferredDF.reverse(filteredSelection)));
		
		// compute what to show for current steps
		Q f = instantations.forwardStepOn(completeResult, forward);
		Q r = filteredSelection.reverseStepOn(completeResult, reverse);
		Q result = f.union(r).union(filteredSelection);;
		
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
