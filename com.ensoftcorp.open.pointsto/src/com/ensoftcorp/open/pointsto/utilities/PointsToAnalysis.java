package com.ensoftcorp.open.pointsto.utilities;

import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.NodeDirection;
import com.ensoftcorp.atlas.core.db.graph.NodeGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Attr.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.pointsto.common.Constants;

/**
 * Utilities for assisting in the computation of points-to sets.
 * 
 * @author Ben Holland
 * @author Tom Deering - Large credit for developing the conservative data flow graph
 */
public class PointsToAnalysis {

	/**
	 * Gets or creates the points to set for a graph element.
	 * Returns a reference to the points to set so that updates to the 
	 * set will also update the set on the graph element.
	 * @param ge
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public static HashSet<Long> getPointsToSet(GraphElement ge){
		if(ge.hasAttr(Constants.POINTS_TO_SET)){
			return (HashSet<Long>) ge.getAttr(Constants.POINTS_TO_SET);
		} else {
			HashSet<Long> pointsToIds = new HashSet<Long>();
			ge.putAttr(Constants.POINTS_TO_SET, pointsToIds);
			return pointsToIds;
		}
	}
	
	/**
	 * Given an object reference, return the stated type of that reference.
	 * 
	 * @param ge
	 * @return
	 */
	public static GraphElement statedType(GraphElement ge){
		return Common.universe().edgesTaggedWithAny(XCSG.TypeOf).successors(Common.toQ(ge)).eval().nodes().getFirst();
	}
	
	/**
	 * Given an array element type, return the array type for the given dimension
	 * @param arrayElementType
	 * @param dimension
	 * @return
	 */
	public static GraphElement getArrayTypeForDimension(GraphElement arrayElementType, int dimension){
		Q arrayTypes = Common.universe().edgesTaggedWithAny(XCSG.ArrayElementType).predecessors(Common.toQ(arrayElementType));
		return arrayTypes.selectNode(Node.DIMENSION, dimension).eval().nodes().getFirst();
	}
	
	/**
	 * Returns a data flow graph containing all data flow edges minus those
	 * involved in dynamic dispatches with multiple targets resulting in a
	 * conservative data flow graph with only statically resolvable data flow
	 * edges.
	 * 
	 * @param monitor
	 * @return
	 */
	public static Q getConservativeDataFlow(IProgressMonitor monitor) {
		Q dfInvokeThisContext = Common.resolve(monitor, Common.universe().edgesTaggedWithAny(XCSG.IdentityPassedTo));
		Q dfInvokeParamContext = Common.resolve(monitor, Common.universe().edgesTaggedWithAny(XCSG.ParameterPassedTo));
		Q resolvableCallsites = getResolvableCallsites(monitor);
		Q resolvableParams = dfInvokeParamContext.predecessors(resolvableCallsites);
		Q resolvableThis = dfInvokeThisContext.predecessors(resolvableCallsites);
		Q fields = Common.universe().nodesTaggedWithAny(XCSG.Field); 
		Q dfInterprocContext = Common.resolve(monitor, Common.universe().edgesTaggedWithAny(XCSG.InterproceduralDataFlow));
		
		Q conservativeDF = Common.resolve(monitor, Common.universe().edgesTaggedWithAny(XCSG.LocalDataFlow).union(
				dfInterprocContext.forwardStep(fields),
				dfInterprocContext.reverseStep(fields),
				dfInterprocContext.reverseStep(resolvableCallsites),
				dfInterprocContext.forwardStep(resolvableParams),
				dfInterprocContext.forwardStep(resolvableThis)));
		
		return conservativeDF;
	}
	
	/**
	 * Returns a set of callsites that are statically resolvable (don't involve
	 * dynamic dispatch with multiple targets).
	 * 
	 * @param monitor
	 * @return
	 */
	private static Q getResolvableCallsites(IProgressMonitor monitor){
		Q supertypeContext = Common.resolve(monitor, Common.universe().edgesTaggedWithAny(XCSG.Supertype));
		Q overridesContext = Common.resolve(monitor, Common.universe().edgesTaggedWithAny(XCSG.Overrides));
		Q typeToSearchContext = Common.resolve(monitor, Common.universe().edgesTaggedWithAny(XCSG.InvokedType));
		
		Q concreteTypes = Common.resolve(monitor, Common.universe().nodesTaggedWithAny(XCSG.ArrayType, XCSG.Java.Class).difference(Common.universe().nodesTaggedWithAny(XCSG.Java.AbstractClass)));
		Q finalConcreteTypes = Common.resolve(monitor, concreteTypes.nodesTaggedWithAny(Node.IS_FINAL).union(roots(supertypeContext, concreteTypes)));
		
		Q methodSignatureContext = Common.resolve(monitor, Common.universe().edgesTaggedWithAny(XCSG.InvokedFunction, XCSG.InvokedSignature));
		
		Q methods = Common.universe().nodesTaggedWithAny(XCSG.InstanceMethod);
		Q concreteMemberMethods = methods.difference(Common.universe().nodesTaggedWithAny(XCSG.abstractMethod));
		Q finalMemberMethods = Common.resolve(monitor, concreteMemberMethods.nodesTaggedWithAny(Node.IS_FINAL).union(roots(overridesContext, concreteMemberMethods)));
		
		Q resolvableCallsites = Common.resolve(monitor, Common.universe().nodesTaggedWithAny(XCSG.StaticDispatchCallSite).union(
				typeToSearchContext.predecessors(finalConcreteTypes).nodesTaggedWithAny(XCSG.CallSite),
				methodSignatureContext.predecessors(finalMemberMethods).nodesTaggedWithAny(XCSG.CallSite)));
		
		return resolvableCallsites;
	}
	
	/**
	 * Returns the subset of the given nodes that are roots in the given edge
	 * context
	 * 
	 * @param edgeContext
	 * @param nodes
	 * @return
	 */
	public static Q roots(Q edgeContext, Q nodes){
		AtlasSet<GraphElement> nodeSet = nodes.eval().nodes();
		AtlasSet<GraphElement> rootSet = new AtlasHashSet<GraphElement>((int) nodeSet.size());
		
		Graph edgeContextG = edgeContext.eval();
		for(GraphElement node : nodeSet){
			if(edgeContextG.edges(node, NodeDirection.IN).isEmpty()){
				rootSet.add(node);
			}
		}
		
		return Common.toQ(new NodeGraph(rootSet));
	}
	
}
