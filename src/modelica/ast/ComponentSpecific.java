package modelica.ast;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.Iterator;
import java.io.PrintStream;

import modelica.resolver.*;

public class ComponentSpecific extends ComponentReference {
	public String name;
	public List<ArraySubscript> subscripts;
	public ComponentReference next;
    public Declaration resolvedDecl;
	public void prettyPrint(PrintStream stream) {
		stream.print(name);
		if(subscripts!=null) {
			stream.print("[");
			Iterator<ArraySubscript> it = subscripts.iterator();
			while(it.hasNext()) {
				it.next().prettyPrint(stream);
				if(it.hasNext()) {
					stream.print(",");
				}
			}
			stream.print("]");
		}
		if(next!=null) {
			stream.print(".");
			next.prettyPrint(stream);
		}
	}
	public void putVariables(Set<ComponentReference> vars) {
		vars.add(this);
	}
	public boolean equals(Object obj) {
		if(!(obj instanceof ComponentSpecific)) {
			return false;
		}
		ComponentSpecific comp = (ComponentSpecific)obj;
		if(!name.equals(comp.name)) {
			return false;
		}
		if(subscripts==null) {
			if(comp.subscripts!=null) {
				return false;
			}
		} else {
			if(comp.subscripts==null) {
				return false;
			}
			if(!subscripts.equals(comp.subscripts)) {
				return false;
			}
		}
		if(next==null) {
			if(comp.next!=null) {
				return false;
			}
		} else {
			if(comp.next==null) {
				return false;
			}
			if(!next.equals(comp.next)) {
				return false;
			}
		}
		return true;
	}
    public void resolveAll(ResolveContext ctx) throws ResolveException {
        ResolveContext cur = ctx.resolve(name);
        if(cur==null) {
            List<String> names = new LinkedList<String>();
            names.add(name);
            throw new ResolveException(names,getLine(),getColumn());
        }
        resolvedDecl = (Declaration)cur.resolved();
        if(next!=null) {
            next.resolveAll(cur);
        }
    }
}
