package com.ensoftcorp.open.pointsto.ui.smart;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.highlight.Highlighter;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.atlas.ui.scripts.selections.AtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.scripts.selections.FilteringAtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;
import com.ensoftcorp.open.pointsto.common.PointsToResults;

public class PointsToInstantiationsSmartView extends FilteringAtlasSmartViewScript implements AtlasSmartViewScript {

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
		return "Points-to Instantiations";
	}

	@Override
	protected StyledResult selectionChanged(IAtlasSelectionEvent event, Q arg1) {
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
		return new StyledResult(instantations, h);
	}

}
