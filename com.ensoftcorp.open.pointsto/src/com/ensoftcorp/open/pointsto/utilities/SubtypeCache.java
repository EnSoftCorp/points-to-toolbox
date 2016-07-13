package com.ensoftcorp.open.pointsto.utilities;

import static com.ensoftcorp.atlas.core.query.Query.universe;

import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.db.graph.Edge;
import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.core.db.graph.GraphElement.NodeDirection;
import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Attr;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.pointsto.log.Log;

import net.ontopia.utils.CompactHashMap;

/**
 * A cache for efficiently answering subtyping relationships
 * 
 * @author Jon Mathews
 */
public class SubtypeCache {
	/**
	 * Cached supertype relations for concrete types
	 */
	private CompactHashMap<Node, AtlasSet<Node>> supertypes;
	
	public SubtypeCache(IProgressMonitor monitor) {
		cacheSubtypeRelations(monitor);
	}
	
	/**
	 * 
	 * @param type1
	 * @param type2
	 * @return true if type1 is a subtype of type2
	 */
	public boolean isSubtypeOf(Node type1, Node type2) {
		if (type1.equals(type2))
			return true;
		
		AtlasSet<Node> st = supertypes.get(type1);
		if (st == null) {
			// this only occurs if type1 is not in the index - it's missing, but a few relations can still be inferred

			// assert: already checked if type1.equals(types2)
			
			if (type1.taggedWith(XCSG.Primitive) ^ type2.taggedWith(XCSG.Primitive)) {
				// missing types cannot be related to primitives
				return false;
			}
			
			if ("java.lang.Object".equals(type2.getAttr(Attr.Node.BINARY_NAME))) {
				// can still assert that it's a subtype of Object
				return true;
			}
			
			Exception e = new IllegalArgumentException("No cached supertypes for " + type1 + "\n\n cannot determine if it's a subtype of \n\n" + type2);
			Log.warning("Type inference refusing to propagate a missing type", e);
			return false;			
		}
		return st.contains(type2);
	}

	private void cacheSubtypeRelations(IProgressMonitor m) {
		// for (at least) all concrete types, cache the supertypes
		CompactHashMap<Node, AtlasSet<Node>> supertypeClosure = new CompactHashMap<Node, AtlasSet<Node>>();

		Q supersetOfConcreteTypesQ = universe().nodesTaggedWithAny(XCSG.Java.Class, XCSG.ArrayType);
		AtlasSet<Node> supersetOfConcreteTypes = Common.resolve(m, supersetOfConcreteTypesQ).eval().nodes();

		Graph st = universe().edgesTaggedWithAny(XCSG.Supertype).eval();
		Iterator<Node> itr = supersetOfConcreteTypes.iterator();
		while (itr.hasNext()) {
			Node t = itr.next();
			getClosure(supertypeClosure, st, t);
		}

		// add NullType as a subtype of every other type, to enable assignment compatibility
		Node nullType = universe().nodesTaggedWithAny(XCSG.Java.NullType).eval().nodes().getFirst();
		supertypeClosure.put(nullType, supersetOfConcreteTypes);
		
		this.supertypes = supertypeClosure;
	}

	private AtlasSet<Node> getClosure(CompactHashMap<Node, AtlasSet<Node>> supertypeClosure, Graph SupertypeGraph, Node t) {
		AtlasSet<Node> closure = new AtlasHashSet<Node>();
		closure.add(t);
		AtlasSet<Edge> edges = SupertypeGraph.edges(t, NodeDirection.OUT);
		for (Edge edgeSupertype : edges) {
			Node supertype = edgeSupertype.getNode(EdgeDirection.TO);
			AtlasSet<Node> superClosure = supertypeClosure.get(supertype);
			if (superClosure == null) {
				superClosure = getClosure(supertypeClosure, SupertypeGraph, supertype);
			}
			closure.addAll(superClosure);
		}
		supertypeClosure.put(t, closure);
		return closure;
	}

}