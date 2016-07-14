package com.ensoftcorp.open.pointsto.ui.smart;

import java.awt.Color;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
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

		AtlasSet<GraphElement> arrayComponentsSet = new AtlasHashSet<GraphElement>();
		for(Node node : filteredSelection.eval().nodes()){
			for(String aliasTag : PointsToAnalysis.getAliasTags(node)){
				// if the array memory model has an alias then this element 
				// could be an array (even if it doesn't look like it currently)
				if(PointsToAnalysis.isArrayMemoryModelAlias(aliasTag)){
					// get the array component with this alias
					Q arrayComponents = Common.universe().nodesTaggedWithAny(XCSG.ArrayComponents);
					for(Node arrayComponent : arrayComponents.nodesTaggedWithAny(aliasTag).eval().nodes()){
						arrayComponentsSet.add(arrayComponent);
					}
				}
			}
		}
		Q arrayComponents = Common.toQ(arrayComponentsSet);
		
		// compute what to show for current steps
		Q inferredDataFlow = Common.universe().edgesTaggedWithAny(PointsToAnalysis.INFERRED);
		Q f = arrayComponents.forwardStepOn(inferredDataFlow, forward);
		Q r = arrayComponents.reverseStepOn(inferredDataFlow, reverse);
		Q result = f.union(r).union(filteredSelection);
		
		// compute what is on the frontier
		Q frontierForward = arrayComponents.forwardStepOn(inferredDataFlow, forward+1);
		Q frontierReverse = arrayComponents.reverseStepOn(inferredDataFlow, reverse+1);
		frontierForward = frontierForward.retainEdges().differenceEdges(result);
		frontierReverse = frontierReverse.retainEdges().differenceEdges(result);

		Highlighter h = new Highlighter();
		h.highlight(arrayComponents, Color.CYAN);
		
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
