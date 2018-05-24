package modelica.ast;

import java.io.PrintStream;

import java.util.Set;
import java.util.HashSet;

import modelica.resolver.*;

public abstract class Equation extends ModelicaAST implements ClassDef.EquationOrAnnotation {
	public Comment comment;
	public static class NormalEquation extends Equation {
		public Expression leftHand;
		public Expression rightHand;
		public void prettyPrint(PrintStream stream) {
			leftHand.prettyPrint(stream);
			stream.print("= ");
			rightHand.prettyPrint(stream);
		}
		public void putVariables(Set<ComponentReference> vars) {
			leftHand.putVariables(vars);
			rightHand.putVariables(vars);
		}
		public Set<String> variables() {
			Set<ComponentReference> comps = new HashSet<ComponentReference>();
			Set<String> res = new HashSet<String>();
			putVariables(comps);
			for(ComponentReference ref : comps) {
				res.add(ref.toString());
			}
			return res;
		}
		public boolean equals(Object obj) {
			if(!(obj instanceof NormalEquation)) {
				return false;
			}
			NormalEquation normEq = (NormalEquation)obj;
			if(comment==null) {
				if(normEq.comment!=null) {
					return false;
				}
			} else {
				if(normEq.comment==null) {
					return false;
				}
				if(!comment.equals(normEq.comment)) {
					return false;
				}
			}
			if(!leftHand.equals(normEq.leftHand)) {
				return false;
			}
			if(!rightHand.equals(normEq.rightHand)) {
				return false;
			}
			return true;
		}
        public void resolveAll(ResolveContext ctx) throws ResolveException {
            leftHand.resolveAll(ctx);
            rightHand.resolveAll(ctx);
        }
	}
}
