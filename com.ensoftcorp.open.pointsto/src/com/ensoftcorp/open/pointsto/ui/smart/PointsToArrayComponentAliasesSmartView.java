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

public class PointsToArrayComponentAliasesSmartView extends FilteringAtlasSmartViewScript implements IResizableScript {

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
		return "Points-to Array Component Aliases";
	}

	@Override
	public FrontierStyledResult evaluate(IAtlasSelectionEvent event, int reverse, int forward) {
		Q filteredSelection = filter(event.getSelection());

		AtlasSet<Node> arrayComponentsSet = new AtlasHashSet<Node>();
		AtlasSet<Node> arrayComponentInstantiationSet = new AtlasHashSet<Node>();
		for(Node node : filteredSelection.eval().nodes()){
			Q aliases = Common.toQ(PointsToAnalysis.getAliases(node));
			Q arrayComponentAliases = aliases.nodesTaggedWithAny(XCSG.ArrayComponents);
			for(Node arrayComponentAlias : arrayComponentAliases.eval().nodes()){
				arrayComponentsSet.add(arrayComponentAlias);
				AtlasSet<Node> arrayInstantiations = PointsToAnalysis.getAliases(arrayComponentAlias);
				arrayComponentInstantiationSet.addAll(arrayInstantiations);
			}
		}

		Q arrayComponents = Common.toQ(arrayComponentsSet);
		Q arrayComponentInstantiations = Common.toQ(arrayComponentInstantiationSet);

		Highlighter h = new Highlighter();
		h.highlight(arrayComponents, Color.CYAN);
		h.highlight(arrayComponentInstantiations, Color.RED);

		// return a graph that contains the selected references in blue,
		// and the points-to instantiations highlighted red, with inferred
		// data flow edges between the instantiations and the selections
		// by default the data flow edges are not shown until forward/backward
		// steps are increased. Union instantiations in, for null's since they
		// won't have a between edge
		Q inferredDF = Common.universe().edgesTaggedWithAny(PointsToAnalysis.INFERRED);
		Q completeResult = arrayComponentInstantiations.union(inferredDF.forward(arrayComponentInstantiations).intersection(inferredDF.reverse(arrayComponents)));
		
		// compute what to show for current steps
		Q f = arrayComponentInstantiations.forwardStepOn(completeResult, forward);
		Q r = arrayComponents.reverseStepOn(completeResult, reverse);
		Q result = f.union(r);
		
		// compute what is on the frontier
		Q frontierForward = arrayComponentInstantiations.forwardStepOn(completeResult, forward+1);
		Q frontierReverse = arrayComponents.reverseStepOn(completeResult, reverse+1);
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
