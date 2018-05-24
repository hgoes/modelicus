package modelica.ast;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.io.PrintStream;

import modelica.resolver.*;

public abstract class Logical {
	public static class Expression extends ModelicaAST {
		public List<Term> terms;
		public Expression() {
			terms = new LinkedList<Term>();
		}
		public void prettyPrint(PrintStream stream) {
			Iterator<Term> it = terms.iterator();
			while(it.hasNext()) {
				it.next().prettyPrint(stream);
				if(it.hasNext()) {
					stream.print("or ");
				}
			}
		}
		public void putVariables(Set<ComponentReference> vars) {
			for(Term trm : terms) {
				trm.putVariables(vars);
			}
		}
		public boolean equals(Object obj) {
			if(!(obj instanceof Expression)) {
				return false;
			}
			Expression expr = (Expression)obj;
			if(!terms.equals(expr.terms)) {
				return false;
			}
			return true;
		}
        public void resolveAll(ResolveContext ctx) throws ResolveException {
            for(Term t : terms) {
                t.resolveAll(ctx);
            }
        }
	}
	public static class Term extends ModelicaAST {
		public List<Factor> factors;
		public Term() {
			factors = new LinkedList<Factor>();
		}
		public void prettyPrint(PrintStream stream) {
			Iterator<Factor> it = factors.iterator();
			while(it.hasNext()) {
				it.next().prettyPrint(stream);
				if(it.hasNext()) {
					stream.print("and ");
				}
			}
		}
		public void putVariables(Set<ComponentReference> vars) {
			for(Factor f : factors) {
				f.putVariables(vars);
			}
		}
		public boolean equals(Object obj) {
			if(!(obj instanceof Term)) {
				return false;
			}
			Term term = (Term)obj;
			if(!factors.equals(term.factors)) {
				return false;
			}
			return true;
		}
        public void resolveAll(ResolveContext ctx) throws ResolveException {
            for(Factor f : factors) {
                f.resolveAll(ctx);
            }
        }
	}
	public static class Factor extends ModelicaAST {
		public boolean isNegated;
		public Relation relation;
		public void prettyPrint(PrintStream stream) {
			if(isNegated) {
				stream.print("not ");
			}
			relation.prettyPrint(stream);
		}
		public void putVariables(Set<ComponentReference> vars) {
			relation.putVariables(vars);
		}
		public boolean equals(Object obj) {
			if(!(obj instanceof Factor)) {
				return false;
			}
			Factor fac = (Factor)obj;
			if(isNegated!=fac.isNegated) {
				return false;
			}
			if(!relation.equals(fac.relation)) {
				return false;
			}
			return true;
		}
        public void resolveAll(ResolveContext ctx) throws ResolveException {
            relation.resolveAll(ctx);
        }
	}
	public static class Relation extends ModelicaAST {
		public static enum Kind {
			UnaryRelation,
			Less,
			LessEq,
			Greater,
			GreaterEq,
			Eq,
			LessGt
		}
		public Kind kind;
		public modelica.ast.Expression.Arithmetic leftHand;
		public modelica.ast.Expression.Arithmetic rightHand;
		public void prettyPrint(PrintStream stream) {
			leftHand.prettyPrint(stream);
			switch(kind) {
			case Less:      stream.print("< ");  break;
			case LessEq:    stream.print("<= "); break;
			case Greater:   stream.print("> ");  break;
			case GreaterEq: stream.print(">= "); break;
			case Eq:        stream.print("== ");  break;
			case LessGt:    stream.print("!= "); break;
			}
			switch(kind) {
			case UnaryRelation: break;
			default:
				rightHand.prettyPrint(stream);
				break;
			}
		}
		public void putVariables(Set<ComponentReference> vars) {
			leftHand.putVariables(vars);
			if(rightHand!=null) {
				rightHand.putVariables(vars);
			}
		}
		public boolean equals(Object obj) {
			if(!(obj instanceof Relation)) {
				return false;
			}
			Relation rel = (Relation)obj;
			if(kind!=rel.kind) {
				return false;
			}
			if(!leftHand.equals(rel.leftHand)) {
				return false;
			}
			if(rightHand==null) {
				if(rel.rightHand!=null) {
					return false;
				}
			} else {
				if(rel.rightHand==null) {
					return false;
				}
				if(!rightHand.equals(rel.rightHand)) {
					return false;
				}
			}
			return true;
		}
        public void resolveAll(ResolveContext ctx) throws ResolveException {
            leftHand.resolveAll(ctx);
            if(rightHand!=null) {
                rightHand.resolveAll(ctx);
            }
        }
	}
}
