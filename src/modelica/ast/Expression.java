package modelica.ast;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.Iterator;
import java.math.BigInteger;
import java.io.PrintStream;

import modelica.resolver.*;

public abstract class Expression extends ModelicaAST {
	public abstract void putVariables(Set<ComponentReference> vars);
    public abstract void resolveAll(ResolveContext ctx) throws ResolveException;
	public static class Simple extends Expression {
		public Logical.Expression expr1;
		public void prettyPrint(PrintStream stream) {
			expr1.prettyPrint(stream);
		}
		public void putVariables(Set<ComponentReference> vars) {
			expr1.putVariables(vars);
		}
		public boolean equals(Object obj) {
			if(!(obj instanceof Simple)) {
				return false;
			}
			Simple sim = (Simple)obj;
			if(!expr1.equals(sim.expr1)) {
				return false;
			}
			return true;
		}
        public void resolveAll(ResolveContext ctx) throws ResolveException {
            expr1.resolveAll(ctx);
        }
	}
	public static class Range2 extends Simple {
		public Logical.Expression expr2;
		public void prettyPrint(PrintStream stream) {
			super.prettyPrint(stream);
			stream.print(": ");
			expr2.prettyPrint(stream);
		}
		public boolean equals(Object obj) {
			if(!(obj instanceof Range2)) {
				return false;
			}
			Range2 range = (Range2)obj;
			if(!expr1.equals(range.expr1)) {
				return false;
			}
			if(!expr2.equals(range.expr2)) {
				return false;
			}
			return true;
		}
        public void resolveAll(ResolveContext ctx) throws ResolveException {
            super.resolveAll(ctx);
            expr2.resolveAll(ctx);
        }
	}
	public static class Range3 extends Range2 {
		public Logical.Expression expr3;
		public void prettyPrint(PrintStream stream) {
			super.prettyPrint(stream);
			stream.print(": ");
			expr3.prettyPrint(stream);
		}
		public boolean equals(Object obj) {
			if(!(obj instanceof Range3)) {
				return false;
			}
			Range3 range = (Range3)obj;
			if(!expr1.equals(range.expr1)) {
				return false;
			}
			if(!expr2.equals(range.expr2)) {
				return false;
			}
			if(!expr3.equals(range.expr3)) {
				return false;
			}
			return true;
		}
        public void resolveAll(ResolveContext ctx) throws ResolveException {
            super.resolveAll(ctx);
            expr3.resolveAll(ctx);
        }
	}
	public static class If extends Expression {
		public static class IfNode extends ModelicaAST {
			public Expression ifExpr;
			public Expression thenExpr;
			public boolean equals(Object obj) {
				if(!(obj instanceof IfNode)) {
					return false;
				}
				IfNode ifNode = (IfNode)obj;
				if(!ifExpr.equals(ifNode.ifExpr)) {
					return false;
				}
				if(!thenExpr.equals(ifNode.thenExpr)) {
					return false;
				}
				return true;
			}
            public void resolveAll(ResolveContext ctx) throws ResolveException {
                ifExpr.resolveAll(ctx);
                thenExpr.resolveAll(ctx);
            }
		}
		public List<IfNode> nodes;
		public Expression elseExpr;
		public If() {
			nodes = new LinkedList<IfNode>();
		}
		public void addNode(Expression ifE,Expression thenE) {
			IfNode toAdd = new IfNode();
			toAdd.ifExpr = ifE;
			toAdd.thenExpr = thenE;
			nodes.add(toAdd);
		}
		public void prettyPrint(PrintStream stream) {
			Iterator<IfNode> it = nodes.iterator();
			IfNode ak;
			ak = it.next();
			stream.print("if ");
			ak.ifExpr.prettyPrint(stream);
			stream.print("then ");
			ak.thenExpr.prettyPrint(stream);
			while(it.hasNext()) {
				ak = it.next();
				stream.print("elseif ");
				ak.ifExpr.prettyPrint(stream);
				stream.print("then ");
				ak.thenExpr.prettyPrint(stream);
			}
		}
		public void putVariables(Set<ComponentReference> vars) {
			//TODO implement this
		}
		public boolean equals(Object obj) {
			if(!(obj instanceof If)) {
				return false;
			}
			If i = (If)obj;
			if(!nodes.equals(i.nodes)) {
				return false;
			}
			if(elseExpr==null) {
				if(i.elseExpr!=null) {
					return false;
				}
			} else {
				if(i.elseExpr==null) {
					return false;
				}
			}
			if(elseExpr.equals(i.elseExpr)) {
				return false;
			}
			return true;
		}
        public void resolveAll(ResolveContext ctx) throws ResolveException {
            for(IfNode node : nodes) {
                node.resolveAll(ctx);
            }
            if(elseExpr!=null) {
                elseExpr.resolveAll(ctx);
            }
        }
	}
	public static class Arithmetic extends ModelicaAST {
		public enum AddOp {
			Plus,
			Minus
		}
		public enum MulOp {
			Mul,
			Div
		}
		public boolean startsWithMinus;
		public static class Term extends ModelicaAST {
			public Factor start;
			public List<FactorElement> factors;
			public void prettyPrint(PrintStream stream) {
				start.prettyPrint(stream);
				for(FactorElement factor : factors) {
					factor.prettyPrint(stream);
				}
			}
			public void putVariables(Set<ComponentReference> vars) {
				start.putVariables(vars);
				for(FactorElement factor : factors) {
					factor.putVariables(vars);
				}
			}
			public boolean equals(Object obj) {
				if(!(obj instanceof Term)) {
					return false;
				}
				Term term = (Term)obj;
				if(!start.equals(term.start)) {
					return false;
				}
				if(!factors.equals(term.factors)) {
					return false;
				}
				return true;
			}
            public void resolveAll(ResolveContext ctx) throws ResolveException {
                start.resolveAll(ctx);
                for(FactorElement factor : factors) {
                    factor.resolveAll(ctx);
                }
            }
		}
		public Term start;
		public static class TermElement extends ModelicaAST {
			public AddOp op;
			public Term term;
			public void prettyPrint(PrintStream stream) {
				switch(op) {
				case Plus:
					stream.print("+ ");
					break;
				case Minus:
					stream.print("- ");
					break;
				}
				term.prettyPrint(stream);
			}
			public void putVariables(Set<ComponentReference> vars) {
				term.putVariables(vars);
			}
			public boolean equals(Object obj) {
				if(!(obj instanceof TermElement)) {
					return false;
				}
				TermElement te = (TermElement)obj;
				if(op!=te.op) {
					return false;
				}
				if(!term.equals(te.term)) {
					return false;
				}
				return true;
			}
            public void resolveAll(ResolveContext ctx) throws ResolveException {
                term.resolveAll(ctx);
            }
		}
		public static class FactorElement extends ModelicaAST {
			public MulOp op;
			public Factor factor;
			public void prettyPrint(PrintStream stream) {
				switch(op) {
				case Mul:
					stream.print("* ");
					break;
				case Div:
					stream.print("/ ");
					break;
				}
				factor.prettyPrint(stream);
			}
			public void putVariables(Set<ComponentReference> vars) {
				factor.putVariables(vars);
			}
			public boolean equals(Object obj) {
				if(!(obj instanceof FactorElement)) {
					return false;
				}
				FactorElement fe = (FactorElement)obj;
				if(op!=fe.op) {
					return false;
				}
				if(!factor.equals(fe.factor)) {
					return false;
				}
				return true;
			}
            public void resolveAll(ResolveContext ctx) throws ResolveException {
                factor.resolveAll(ctx);
            }
		}
		public List<TermElement> terms;
		public static class Factor extends ModelicaAST {
			public Primary value;
			public Primary exponent;
			public void prettyPrint(PrintStream stream) {
				value.prettyPrint(stream);
				if(exponent!=null) {
					stream.print("^");
					exponent.prettyPrint(stream);
				}
			}
			public void putVariables(Set<ComponentReference> vars) {
				value.putVariables(vars);
				if(exponent!=null) {
					exponent.putVariables(vars);
				}
			}
			public boolean equals(Object obj) {
				if(!(obj instanceof Factor)) {
					return false;
				}
				Factor fac = (Factor)obj;
				if(!value.equals(fac.value)) {
					return false;
				}
				if(exponent==null) {
					if(fac.exponent!=null) {
						return false;
					}
				} else {
					if(fac.exponent==null) {
						return false;
					}
					if(!exponent.equals(fac.exponent)) {
						return false;
					}
				}
				return true;
			}
            public void resolveAll(ResolveContext ctx) throws ResolveException {
                value.resolveAll(ctx);
                if(exponent!=null) {
                    exponent.resolveAll(ctx);
                }
            }
		}
		public static abstract class Primary extends ModelicaAST {
			public void putVariables(Set<ComponentReference> vars) {}
            public void resolveAll(ResolveContext ctx) throws ResolveException {}
		}
		public static class UnsignedNum extends Primary {
			public BigInteger content;
			public void prettyPrint(PrintStream stream) {
				stream.print(content.toString());
				stream.print(' ');
			}
			public boolean equals(Object obj) {
				if(!(obj instanceof UnsignedNum)) {
					return false;
				}
				UnsignedNum unum = (UnsignedNum)obj;
				if(!content.equals(unum.content)) {
					return false;
				}
				return true;
			}
		}
		public static class String extends Primary {
			public java.lang.String content;
			public  void prettyPrint(PrintStream stream) {
				stream.print('"');
				stream.print(content);
				stream.print("\" ");
			}
			public boolean equals(Object obj) {
				if(!(obj instanceof String)) {
					return false;
				}
				String str = (String)obj;
				if(!content.equals(str.content)) {
					return false;
				}
				return true;
			}
		}
		public static class Boolean extends Primary {
			public boolean content;
			public void prettyPrint(PrintStream stream) {
				if(content) {
					stream.print("true");
				} else {
					stream.print("false");
				}
			}
			public boolean equals(Object obj) {
				if(!(obj instanceof Boolean)) {
					return false;
				}
				Boolean bo = (Boolean)obj;
				return content==bo.content;
			}
		}
		public void prettyPrint(PrintStream stream) {
			if(startsWithMinus) {
				stream.print("-");
			}
			start.prettyPrint(stream);
			for(TermElement term : terms) {
				term.prettyPrint(stream);
			}
		}
		public void putVariables(Set<ComponentReference> vars) {
			start.putVariables(vars);
			for(TermElement term : terms) {
				term.putVariables(vars);
			}
		}
		public boolean equals(Object obj) {
			if(!(obj instanceof Arithmetic)) {
				return false;
			}
			Arithmetic arith = (Arithmetic)obj;
			if(!start.equals(arith.start)) {
				return false;
			}
			if(startsWithMinus!=arith.startsWithMinus) {
				return false;
			}
			if(!terms.equals(arith.terms)) {
				return false;
			}
			return true;
		}
        public void resolveAll(ResolveContext ctx) throws ResolveException {
            start.resolveAll(ctx);
            for(TermElement term : terms) {
                term.resolveAll(ctx);
            }
        }
	}
}
