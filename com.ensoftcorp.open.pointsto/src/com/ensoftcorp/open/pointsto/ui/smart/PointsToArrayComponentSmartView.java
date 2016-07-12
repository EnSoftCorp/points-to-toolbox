package com.ensoftcorp.open.pointsto.ui.smart;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.FrontierStyledResult;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.scripts.selections.IResizableScript;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;

public class PointsToArrayComponentSmartView extends FilteringAtlasSmartViewScript implements IResizableScript {

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
		return "Points-to Array Component";
	}

	@Override
	public FrontierStyledResult evaluate(IAtlasSelectionEvent event, int reverse, int forward) {
		Q filteredSelection = filter(event.getSelection());

//		PointsToResults pointsToResults = PointsToResults.getInstance();
//
//		AtlasSet<GraphElement> arrayComponentsSet = new AtlasHashSet<GraphElement>();
//		for(GraphElement ge : filteredSelection.eval().nodes()){
//			for(Long address : PointsToResults.getPointsToSet(ge)){
//				// if the array memory model has a key for the address then this element 
//				// could be an array (even if it doesn't look like it currently)
//				if(pointsToResults.arrayMemoryModel.containsKey(address)){
//					// get the array component with this address
//					GraphElement arrayComponent = Common.universe().nodesTaggedWithAny(XCSG.ArrayComponents).selectNode(Constants.POINTS_TO_ARRAY_ADDRESS, address).eval().nodes().getFirst();
//					arrayComponentsSet.add(arrayComponent);
//				}	
//			}
//		}
//
//		Q arrayComponents = Common.toQ(arrayComponentsSet);
//		
//		// compute what to show for current steps
//		Q f = arrayComponents.forwardStepOn(pointsToResults.inferredDataFlowGraph, forward);
//		Q r = arrayComponents.reverseStepOn(pointsToResults.inferredDataFlowGraph, reverse);
//		Q result = f.union(r);
//		
//		// compute what is on the frontier
//		Q frontierForward = arrayComponents.forwardStepOn(pointsToResults.inferredDataFlowGraph, forward+1);
//		Q frontierReverse = arrayComponents.reverseStepOn(pointsToResults.inferredDataFlowGraph, reverse+1);
//		frontierForward = frontierForward.retainEdges().differenceEdges(result);
//		frontierReverse = frontierReverse.retainEdges().differenceEdges(result);
//
//		Highlighter h = new Highlighter();
//		h.highlight(arrayComponents, Color.CYAN);
//		
//		return new com.ensoftcorp.atlas.core.script.FrontierStyledResult(result, frontierReverse, frontierForward, new MarkupFromH(h));
		
		return null;
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
