package modelicus.ast;

import modelicus.PLResult;
import modelicus.typing.*;
import modelicus.config.Language;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Iterator;

import missing.Tuple;

public abstract class Relation extends ModelicusAST implements Typed {
	public abstract PLResult toPL(Language lang,int badness,Set<String> grounded,boolean isNegated);
	public abstract List<Factor> flatten();
	public abstract String asVariable();
    public abstract void typeCheck(Environment env,Language lang,boolean isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError;
    public abstract Type inferType(Language lang,Typer env) throws TypeError;
	public static class Binary extends Relation {
		public Sum leftHand;
		public Sum rightHand;
		public RelOp operator;
        private boolean isNumerical;
		private String opToString() {
			switch(operator) {
			case LESS: return "#<";
			case GREATER: return "#>";
			case EQUAL:
                if(isNumerical) return "#=";
                else return "=";
			case NEQUAL:
                if(isNumerical) return "#\\=";
                else return "\\=";
			default: return "???";
			}
		}
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append(leftHand.toString());
			buf.append(opToString());
			buf.append(rightHand.toString());
			return buf.toString();
		}
		public Tuple<Type,Typer> resolveTypes(Language lang,Type allowed,boolean isNegated,Map<String,Type[]> funcs) throws TypeError {
			if(!allowed.contains(BasicType.Boolean.instance())) {
				flawed(allowed,BasicType.Boolean.instance());
			}
            Tuple<Type,Typer> lres = leftHand.resolveTypes(lang,Type.free(),false,funcs);
	        Tuple<Type,Typer> rres = rightHand.resolveTypes(lang,lres.fst(),false,funcs);
            lres = leftHand.resolveTypes(lang,rres.fst(),false,funcs);
            isNumerical = rres.fst().contains(BasicType.Number.instance());
			return new Tuple<Type,Typer>(Type.singleton(BasicType.Boolean.instance()),new Typer(lres.snd(),rres.snd(),true));
		}
        public Type inferType(Language lang,Typer env) throws TypeError {
            Type lType = leftHand.inferType(lang,env);
            Type rType = rightHand.inferType(lang,env);
            BasicType lBasic = lType.asSingleton();
            BasicType rBasic = rType.asSingleton();

            if(lBasic==null) {
                throw new TypeError.RelationFailed(leftHand.line,leftHand.column,lType,rType);
            }
            if(rBasic==null) {
                throw new TypeError.RelationFailed(rightHand.line,rightHand.column,lType,rType);
            }
            if(lBasic!=rBasic) {
                throw new TypeError.RelationFailed(line,column,lType,rType);
            }
            isNumerical = lBasic==BasicType.Number.instance();
            return Type.singleton(BasicType.Boolean.instance());
        }
		public PLResult toPL(Language lang,int badness,Set<String> grounded,boolean isNegated) {
            if(!isNumerical) {
                String leftVar = leftHand.asVariable();
                String rightVar = rightHand.asVariable();
                switch(operator) {
                case EQUAL:
                    if(leftVar != null && rightHand != null) {
                        // Two variables are being compared, one of them _must_ be grounded.
                        if(!grounded.contains(leftVar) && !grounded.contains(rightVar)) {
                            return null;
                        }
                    }
                    break;
                case NEQUAL:
                    // Both variables _must_ be grounded
                    if(leftVar != null) {
                        if(!grounded.contains(leftVar)) {
                            return null;
                        }
                    }
                    if(rightVar != null) {
                        if(!grounded.contains(rightVar)) {
                            return null;
                        }
                    }
                    break;
                }
            }
			PLResult lres = leftHand.toPL(lang,badness,grounded,false);
			PLResult rres = rightHand.toPL(lang,badness,grounded,false);
			if(lres == null || rres == null) {
				return null;
			}
			LinkedList<org.jpl7.Term> globalsStart = new LinkedList<org.jpl7.Term>();
			LinkedList<org.jpl7.Term> globalsEnd = new LinkedList<org.jpl7.Term>();
            Set<org.jpl7.Variable> toBeAnon = new HashSet<org.jpl7.Variable>();
			globalsStart.addAll(lres.globalsStart);
			globalsStart.addAll(rres.globalsStart);
			globalsEnd.addAll(lres.globalsEnd);
			globalsEnd.addAll(rres.globalsEnd);
            toBeAnon.addAll(lres.anons);
            toBeAnon.addAll(rres.anons);

			Set<String> res = new HashSet<String>();
			res.addAll(lres.grounded);
			res.addAll(rres.grounded);
			org.jpl7.Term resTerm = new org.jpl7.Compound(opToString(),new org.jpl7.Term[]
				{lres.term
				,rres.term});
			if(isNegated) {
				resTerm = new org.jpl7.Compound("\\+",new org.jpl7.Term[] {resTerm});
			}
			return new PLResult(resTerm,res,globalsStart,globalsEnd,toBeAnon);
		}
		public List<Factor> flatten() {
			//Binary relations can never be flattened
			return null;
		}
		public String asVariable() {
			return null;
		}
        public void typeCheck(Environment env,Language lang,boolean isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError {
            env.addExprType(this,Type.singleton(BasicType.Boolean.instance()));
            switch(operator) {
            case EQUAL:
            case NEQUAL:
                env.addEqConstraint(leftHand,rightHand);
                break;
            default:
                env.addExprType(leftHand,Type.singleton(BasicType.Number.instance()));
                break;
            }
            leftHand.typeCheck(env,lang,false,funcs);
            rightHand.typeCheck(env,lang,false,funcs);
        }
	}
	public enum RelOp {
		LESS,
		GREATER,
		EQUAL,
		NEQUAL
	}
}
