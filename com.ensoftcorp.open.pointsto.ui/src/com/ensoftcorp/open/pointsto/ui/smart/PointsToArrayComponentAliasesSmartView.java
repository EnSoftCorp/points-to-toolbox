package com.ensoftcorp.open.pointsto.ui.smart;

import java.awt.Color;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.highlight.Highlighter;
import com.ensoftcorp.atlas.core.markup.MarkupFromH;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
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
import com.ensoftcorp.open.pointsto.preferences.PointsToPreferences;

public class PointsToArrayComponentAliasesSmartView extends FilteringAtlasSmartViewScript implements IResizableScript, IExplorableScript {

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
	public FrontierStyledResult explore(FrontierEdgeExploreEvent event, FrontierStyledResult oldResult) {
		return SimpleScriptUtil.explore(this, event, oldResult);
	}
	
	@Override
	public FrontierStyledResult evaluate(IAtlasSelectionEvent event, int reverse, int forward) {
		Q filteredSelection = filter(event.getSelection());
		
		boolean useInferredDataFlow = PointsToPreferences.isTagInferredDataflowsEnabled();
		
		AtlasSet<GraphElement> arrayComponentsSet = new AtlasHashSet<GraphElement>();
		AtlasSet<GraphElement> arrayComponentsInstantiationSet = new AtlasHashSet<GraphElement>();
		AtlasSet<Node> aliasNodes = new AtlasHashSet<Node>();
		for(Node node : filteredSelection.eval().nodes()){
			Q aliases = PointsToAnalysis.getAliases(node);
			if(!useInferredDataFlow) {
				aliasNodes.addAll(aliases.eval().nodes());
			}
			arrayComponentsSet.addAll(aliases.nodes(XCSG.ArrayComponents).eval().nodes());
			Q aliasedArrayInstantiations = aliases.nodes(XCSG.ArrayInstantiation);
			for(Node aliasedArrayInstantiation : aliasedArrayInstantiations.eval().nodes()){
				Q arrayComponentAliases = Common.toQ(PointsToAnalysis.getArrayMemoryModelAliases(aliasedArrayInstantiation));
				if(!useInferredDataFlow) {
					aliasNodes.addAll(arrayComponentAliases.eval().nodes());
				}
				Q arrayComponentInstantiations = arrayComponentAliases.nodes(XCSG.Instantiation, XCSG.ArrayInstantiation);
				arrayComponentsInstantiationSet.addAll(arrayComponentInstantiations.eval().nodes());
			}
		}
		Q arrayComponents = Common.toQ(arrayComponentsSet);
		Q arrayComponentInstantiations = Common.toQ(arrayComponentsInstantiationSet);

		Q inferredDF;
		if(useInferredDataFlow) {
			// this is easier to work with
			inferredDF = Query.universe().edges(PointsToAnalysis.INFERRED_DATA_FLOW);
		} else {
			// however if tagging was turned off we could fall back to inducing the edges from regular data flow
			inferredDF = Common.toQ(aliasNodes).induce(Query.universe().edges(XCSG.DataFlow_Edge));
		}
		
		Highlighter h = new Highlighter();
		h.highlight(arrayComponents, Color.YELLOW);
		h.highlight(arrayComponentInstantiations, Color.RED);

		// return a graph that contains the selected references in blue,
		// and the points-to instantiations highlighted red, with inferred
		// data flow edges between the instantiations and the selections
		// by default the data flow edges are not shown until forward/backward
		// steps are increased. Union instantiations in, for null's since they
		// won't have a between edge
		Q between = inferredDF.between(arrayComponentInstantiations, arrayComponents);
		Q completeResult = arrayComponents.union(arrayComponentInstantiations).union(between);
		
		// compute what to show for current steps
		Q f = arrayComponentInstantiations.forwardStepOn(completeResult, forward);
		Q r = arrayComponents.reverseStepOn(completeResult, reverse);
		Q result = f.union(r).union(filteredSelection);;
		
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
