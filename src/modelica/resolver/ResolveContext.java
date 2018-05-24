package modelica.resolver;

import modelica.ast.ClassDef;
import java.util.List;
import java.util.LinkedList;



/**
 * Builds up a tree structure in which names can be resolved.
 * @author Henning Günther
 *
 */
public class ResolveContext {
	private ResolveContext up;
	private Resolveable content;
	public ResolveContext(Resolveable cont) {
		up = null;
		content = cont;
	}
	public ResolveContext(Resolveable cont,ResolveContext parent) {
		up = parent;
		content = cont;
	}
	/**
	 * Creates a context that is a child of the current one.
	 * @param next The current node for which a name can be resolved
	 * @return A new context that is the child of next
	 */
	public ResolveContext goDown(Resolveable next) {
		return new ResolveContext(next,this);
	}
	public ResolveContext goUp() {
		return up;
	}
    public ResolveContext resolve(String name) throws ResolveException {
        ResolveContext cur = content.resolve(name,this);
        if(cur!=null) {
            return cur;
        }
		if(up != null) {
			return up.resolve(name);
		} else {
			return null;
		}
    }
	public ResolveContext resolveMany(List<String> names) throws ResolveException {
		ResolveContext cur = this;
		//First, search down the hierarchy
		for(String name : names) {
			cur = content.resolve(name,cur);
			if(cur == null) break;
		}
		//If there's something
		if(cur != null) {
			return cur;
		}
		//Otherwise, search up the hierarchy
		if(up != null) {
			return up.resolveMany(names);
		} else {
			return null;
		}
	}
	public ClassDef resolvedClass() {
		return content.thisClass();
	}
	public Resolveable resolved() {
		return content;
	}
	public List<String> resolvedName() {
		LinkedList<String> res = new LinkedList<String>();
		for(ResolveContext cur = this; cur!=null; cur = cur.goUp()) {
			res.addLast(cur.content.thisName());
		}
		return res;
	}
}
