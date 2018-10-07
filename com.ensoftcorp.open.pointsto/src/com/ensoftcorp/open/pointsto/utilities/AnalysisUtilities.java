package com.ensoftcorp.open.pointsto.utilities;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.NodeDirection;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.graph.NodeGraph;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Attr;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.query.Query;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;

/**
 * Utilities for assisting in the computation of points-to sets.
 * 
 * @author Ben Holland
 * @author Tom Deering - Large credit for developing the conservative data flow
 *                       graph and utilities for resolving dynamic dispatches.
 */
public class AnalysisUtilities {

	/**
	 * Given an object reference, return the stated type of that reference.
	 * 
	 * @param ge
	 * @return
	 */
	public static Node statedType(GraphElement ge){
		return Query.universe().edges(XCSG.TypeOf).successors(Common.toQ(ge)).eval().nodes().one();
	}
	
	/**
	 * Given an array reference, returns a set of corresponding array accesses
	 * @param arrayReference
	 * @return
	 */
	public static AtlasSet<Node> getArrayReadAccessesForArrayReference(GraphElement arrayReference) {
		Q arrayIdentityFor = Query.universe().edges(XCSG.ArrayIdentityFor);
		return arrayIdentityFor.successors(Common.toQ(arrayReference)).nodes(XCSG.ArrayRead).eval().nodes();
	}
	
	/**
	 * Given an array reference, returns a set of corresponding array accesses
	 * @param arrayReference
	 * @return
	 */
	public static AtlasSet<Node> getArrayWriteAccessesForArrayReference(GraphElement arrayReference) {
		Q arrayIdentityFor = Query.universe().edges(XCSG.ArrayIdentityFor);
		return arrayIdentityFor.successors(Common.toQ(arrayReference)).nodes(XCSG.ArrayWrite).eval().nodes();
	}
	
	/**
	 * Given an array access, returns a set of corresponding array references
	 * @param arrayAccess
	 * @return
	 */
	public static AtlasSet<Node> getArrayReferencesForArrayAccess(GraphElement arrayAccess){
		Q arrayIdentityFor = Query.universe().edges(XCSG.ArrayIdentityFor);
		return arrayIdentityFor.predecessors(Common.toQ(arrayAccess)).eval().nodes();
	}
	
	/**
	 * Given an array element type, return the array type for the given dimension
	 * @param arrayElementType
	 * @param dimension
	 * @return
	 */
	public static Node getArrayTypeForDimension(GraphElement arrayElementType, int dimension){
		Q arrayTypes = Query.universe().edges(XCSG.ArrayElementType).predecessors(Common.toQ(arrayElementType));
		return arrayTypes.selectNode(Attr.Node.DIMENSION, dimension).eval().nodes().one();
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
		Q dfInvokeThisContext = Common.resolve(monitor, Query.universe().edges(XCSG.IdentityPassedTo));
		Q dfInvokeParamContext = Common.resolve(monitor, Query.universe().edges(XCSG.ParameterPassedTo));
		Q resolvableCallsites = getResolvableCallsites(monitor);
		Q resolvableParams = dfInvokeParamContext.predecessors(resolvableCallsites);
		Q resolvableThis = dfInvokeThisContext.predecessors(resolvableCallsites);
		Q fields = Query.universe().nodes(XCSG.Field); 
		Q dfInterprocContext = Common.resolve(monitor, Query.universe().edges(XCSG.InterproceduralDataFlow));
		
		Q conservativeDF = Common.resolve(monitor, Query.universe().edges(XCSG.LocalDataFlow).union(
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
		Q supertypeContext = Common.resolve(monitor, Query.universe().edges(XCSG.Supertype));
		Q overridesContext = Common.resolve(monitor, Query.universe().edges(XCSG.Overrides));
		Q typeToSearchContext = Common.resolve(monitor, Query.universe().edges(XCSG.InvokedType));
		
		Q concreteTypes = Common.resolve(monitor, Query.universe().nodes(XCSG.ArrayType, XCSG.Java.Class).difference(Query.universe().nodes(XCSG.Java.AbstractClass)));
		Q finalConcreteTypes = Common.resolve(monitor, concreteTypes.nodes(XCSG.Java.finalClass).union(roots(supertypeContext, concreteTypes)));
		
		Q methodSignatureContext = Common.resolve(monitor, Query.universe().edges(XCSG.InvokedFunction, XCSG.InvokedSignature));
		
		Q methods = Query.universe().nodes(XCSG.InstanceMethod);
		Q concreteMemberMethods = methods.difference(Query.universe().nodes(XCSG.abstractMethod));
		Q finalMemberMethods = Common.resolve(monitor, concreteMemberMethods.nodes(XCSG.Java.finalMethod).union(roots(overridesContext, concreteMemberMethods)));
		
		Q resolvableCallsites = Common.resolve(monitor, Query.universe().nodes(XCSG.StaticDispatchCallSite).union(
				typeToSearchContext.predecessors(finalConcreteTypes).nodes(XCSG.CallSite),
				methodSignatureContext.predecessors(finalMemberMethods).nodes(XCSG.CallSite)));
		
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
		AtlasSet<Node> nodeSet = nodes.eval().nodes();
		AtlasSet<Node> rootSet = new AtlasHashSet<Node>();
		
		Graph edgeContextG = edgeContext.eval();
		for(GraphElement node : nodeSet){
			if(edgeContextG.edges(node, NodeDirection.IN).isEmpty()){
				rootSet.add(node);
			}
		}
		
		return Common.toQ(new NodeGraph(rootSet));
	}
	
	/**
	 * Returns a set of callsite this nodes involved in dynamic dispatches
	 * 
	 * @param m
	 * @return
	 */
	public static AtlasHashSet<Node> getDynamicCallsiteThisSet(IProgressMonitor monitor) {
		Q resolvableCallsites = getResolvableCallsites(monitor);
		Q unresolvableCallsites = Query.universe().nodes(XCSG.DynamicDispatchCallSite).difference(resolvableCallsites);
		Q dfInvokeThisContext = Common.resolve(monitor, Query.universe().edges(XCSG.IdentityPassedTo));
		AtlasHashSet<Node> dynamicCallsiteThis = new AtlasHashSet<Node>(dfInvokeThisContext.predecessors(unresolvableCallsites).eval().nodes());
		return dynamicCallsiteThis;
	}
	
	/**
	 * Given a set of methods, returns the given set with their method signature
	 * elements (param, return, this).
	 * 
	 * @param methods
	 * @param signatureSet
	 */
	public static AtlasSet<Node> getSignatureSet(AtlasSet<Node> methods){
		Graph declaresGraph = Query.universe().edges(XCSG.Contains).retainEdges().eval();
		AtlasSet<Node> signatureSet = new AtlasHashSet<Node>();
		for(GraphElement method : methods){
			AtlasSet<Node> cached = new AtlasHashSet<Node>();
			for(GraphElement outDeclares : declaresGraph.edges(method, NodeDirection.OUT)){
				Node declares = outDeclares.getNode(EdgeDirection.TO);
				if(declares.taggedWith(XCSG.Parameter) || declares.taggedWith(XCSG.Identity) || declares.taggedWith(XCSG.ReturnValue)){
					cached.add(declares);
				}
			}
			signatureSet.addAll(cached);
		}
		return signatureSet;
	}
	
}
