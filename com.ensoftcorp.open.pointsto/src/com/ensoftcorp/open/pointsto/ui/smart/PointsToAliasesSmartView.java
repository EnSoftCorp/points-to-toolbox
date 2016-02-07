package com.ensoftcorp.open.pointsto.ui.smart;

import java.awt.Color;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.highlight.Highlighter;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.markup.MarkupFromH;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.FrontierStyledResult;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.scripts.selections.IResizableScript;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.pointsto.common.PointsToResults;

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

		PointsToResults pointsToResults = new PointsToResults();

		// for each selected graph element get the corresponding instantiation
		// as determined by the points-to analysis
		AtlasSet<GraphElement> instantiationSet = new AtlasHashSet<GraphElement>();
		for(GraphElement ge : filteredSelection.eval().nodes()){
			for(Long address : PointsToResults.getPointsToSet(ge)){
				GraphElement instantiation = pointsToResults.addressToInstantiation.get(address);
				if(instantiation != null){
					instantiationSet.add(instantiation);
				} else {
					Log.warning("No instantation for address: " + address);
				}
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
		Q completeResult = instantations.union(pointsToResults.inferredDataFlowGraph.forward(instantations).intersection(pointsToResults.inferredDataFlowGraph.reverse(filteredSelection)));
		
		// compute what to show for current steps
		Q f = instantations.forwardStepOn(completeResult, forward);
		Q r = filteredSelection.reverseStepOn(completeResult, reverse);
		Q result = f.union(r);
		
		// compute what is on the frontier
		Q frontierForward = instantations.forwardStepOn(completeResult, forward+1);
		Q frontierReverse = filteredSelection.reverseStepOn(completeResult, reverse+1);

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
