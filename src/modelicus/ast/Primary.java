package modelicus.ast;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import modelicus.PLResult;
import modelicus.typing.*;
import modelicus.config.Language;

import missing.Tuple;
import missing.Zip;

public abstract class Primary extends ModelicusAST implements Typed {
	public abstract PLResult toPL(Language lang,int badness,Set<java.lang.String> grounded,boolean isNegated);
    public abstract void typeCheck(Environment env,Language lang,boolean isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError;
    public abstract Type inferType(Language lang,Typer env) throws TypeError;
	public List<Factor> flatten() {
		return null;
	}
	public java.lang.String asVariable() {
		return null;
	}
	public static class Variable extends Primary {
		public java.lang.String name;
		public java.lang.String toString() {
			return name;
		}
		public Tuple<Type,Typer> resolveTypes(Language lang,Type allowed,boolean isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError {
			Typer res = new Typer();
			res.addTypeBinding(name,new TypeInformation(allowed,line,column));
			return new Tuple<Type,Typer>(Type.free(),res);
		}
		public PLResult toPL(Language lang,int badness,Set<java.lang.String> grounded,boolean isNegated) {
			org.jpl7.Term res = new org.jpl7.Variable(name);
			if(isNegated) {
				res = new org.jpl7.Compound("\\+",new org.jpl7.Term[] {res});
			}
			return new PLResult(res,grounded);
		}
		public java.lang.String asVariable() {
			return name;
		}
        public void typeCheck(Environment env,Language lang,boolean isNegated,Map<java.lang.String,Type[]> funcs) {
            env.addEqConstraint(this,name);
            //env.addExprType(name,Type.free());
        }
        public Type inferType(Language lang,Typer env) {
            return env.getTypeInformation(name).type;
        }
	}
    public static class DontCare extends Primary {
        public java.lang.String toString() {
            return "_";
        }
		public Tuple<Type,Typer> resolveTypes(Language lang,Type allowed
            ,boolean isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError {
			return new Tuple<Type,Typer>(Type.free(),new Typer());
		}
		public PLResult toPL(Language lang,int badness,Set<java.lang.String> grounded,boolean isNegated) {
            HashSet<org.jpl7.Variable> anon = new HashSet<org.jpl7.Variable>();
            org.jpl7.Variable var = new org.jpl7.Variable();
            anon.add(var);
			return new PLResult(var,grounded,anon);
		}
		public java.lang.String asVariable() {
			return "";
		}
        public void typeCheck(Environment env,Language lang,boolean isNegated,Map<java.lang.String,Type[]> funcs) {
        }
        public Type inferType(Language lang,Typer env) {
            return Type.free();
        }
    }
	public static class Number extends Primary {
		public Integer value;
		public Number(Integer v) {
			value = v;
		}
		public Tuple<Type,Typer> resolveTypes(Language lang,Type allowed,boolean isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError {
			if(!allowed.contains(BasicType.Number.instance())) {
				flawed(allowed,BasicType.Number.instance());
			}
			return new Tuple<Type,Typer>(Type.singleton(BasicType.Number.instance()),new Typer());
		}
		public java.lang.String toString() {
			return value.toString();
		}
		public PLResult toPL(Language lang,int badness,Set<java.lang.String> grounded,boolean isNegated) {
			return new PLResult(new org.jpl7.Integer(value.longValue()),grounded);
		}
        public void typeCheck(Environment env,Language lang,boolean isNegated,Map<java.lang.String,Type[]> funcs) {
            env.addExprType(this,Type.singleton(BasicType.Number.instance()));
        }
        public Type inferType(Language lang,Typer env) {
            return Type.singleton(BasicType.Number.instance());
        }
	}
	public static class String extends Primary {
		public java.lang.String value;
		public String(java.lang.String v) {
			value = v;
		}
		public Tuple<Type,Typer> resolveTypes(Language lang,Type allowed,boolean isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError {
			if(!allowed.contains(BasicType.String.instance())) {
				flawed(allowed,BasicType.String.instance());
			}
			return new Tuple<Type,Typer>(Type.singleton(BasicType.String.instance()),new Typer());
		}
		public java.lang.String toString() {
			return "\""+value+"\"";
		}
		public PLResult toPL(Language lang,int badness,Set<java.lang.String> grounded,boolean isNegated) {
			return new PLResult(new org.jpl7.Atom(value),grounded);
		}
        public void typeCheck(Environment env,Language lang,boolean isNegated,Map<java.lang.String,Type[]> funcs) {
            env.addExprType(this,Type.singleton(BasicType.String.instance()));
        }
        public Type inferType(Language lang,Typer env) {
            return Type.singleton(BasicType.String.instance());
        }
	}
	public static class Predicate extends Primary {
		protected java.lang.String name;
		/**
		 * The actual arguments with which the predicate were called.
		 */
		protected List<Expression> arguments;
		public Predicate(java.lang.String n,List<Expression> args) {
			name = n;
			arguments = args;
		}
		public Tuple<Type,Typer> resolveTypes(Language lang,Type allowed,boolean isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError {
            Type[] usedSig;
			Language.PredicateDescription description = lang.getDescription(name);
			if(description == null) {
                usedSig = funcs.get(name);
                if(usedSig==null) {
				    throw new TypeError.PredicateNotFound(name,line,column);
                }
            } else {
				if(isNegated) {
					usedSig = description.signature_not;
				} else {
					usedSig = description.signature;
				}
            }
            Zip<Type,Expression> zip
                = new Zip<Type,Expression>(Arrays.asList(usedSig),arguments);
            Typer binding = new Typer();
            if(!allowed.contains(BasicType.Boolean.instance())) {
                flawed(allowed,BasicType.Boolean.instance());
            }
            if(usedSig.length != arguments.size()) {
                throw new TypeError.WrongNumberOfArguments(name,description.signature.length,arguments.size());
            }
            for(Tuple<Type,Expression> tup : zip) {
                Tuple<Type,Typer> res = tup.snd().resolveTypes(lang,tup.fst(),false,funcs);
                binding = new Typer(binding,res.snd(),true);
            }
            return new Tuple<Type,Typer>(Type.singleton(BasicType.Boolean.instance()),binding);
		}
        public Type inferType(Language lang,Typer env) throws TypeError {
            for(Expression arg : arguments) {
                arg.inferType(lang,env);
            }
            return Type.singleton(BasicType.Boolean.instance());
        }
		public PLResult toPL(Language lang,int badness,Set<java.lang.String> grounded,boolean isNegated) {
			int curBit = 1;
			int groundedBitSet = 0;
			Set<java.lang.String> toBeGrounded = new HashSet<java.lang.String>();
            for(Expression arg : arguments) {
                java.lang.String varName = arg.asVariable();
                if(varName != null) {
                    if(!varName.equals("")) {
                        if(grounded.contains(varName)) {
                            groundedBitSet = (groundedBitSet | curBit);
                        } else {
                            toBeGrounded.add(varName);
                        }
                    }
                } else {
                    groundedBitSet = (groundedBitSet | curBit);
                }
                curBit = curBit << 1;
            }
            LinkedList<org.jpl7.Term> globalsStart = new LinkedList<org.jpl7.Term>();
            LinkedList<org.jpl7.Term> globalsEnd = new LinkedList<org.jpl7.Term>();
            org.jpl7.Term targs[] = new org.jpl7.Term[arguments.size()];
            Set<org.jpl7.Variable> toBeExistentiated = new HashSet<org.jpl7.Variable>();
            int i = 0;
            for(Expression expr : arguments) {
                PLResult curRes = expr.toPL(lang,badness,grounded,false);
                if(curRes == null) {
                    return null;
                }
                targs[i] = curRes.term;
                grounded = curRes.grounded;
                globalsStart.addAll(curRes.globalsStart);
                globalsEnd.addAll(curRes.globalsEnd);
                toBeExistentiated.addAll(curRes.anons);
                i++;
            }
			Language.PredicateDescription description = lang.getDescription(name);
            if(description != null) {
    			java.lang.String realName = description.getImplementation(badness,groundedBitSet,isNegated);
    			if(realName == null) {
    				return null;
    			} else {
    				toBeGrounded.addAll(grounded);
    				return new PLResult(
       					new org.jpl7.Compound(realName,targs)
    					,toBeGrounded,globalsStart,globalsEnd,toBeExistentiated);
                }
			} else {
                java.lang.String infix;
                if(isNegated) infix="_not_";
                else infix = "_";
                return new PLResult(new org.jpl7.Compound(name+infix+groundedBitSet,targs),toBeGrounded,globalsStart,globalsEnd,toBeExistentiated);
            }
		}
		public java.lang.String toString() {
			Iterator<Expression> it = arguments.iterator();
			StringBuffer buf = new StringBuffer();
			buf.append(name);
			buf.append("(");
			while(it.hasNext()) {
				buf.append(it.next().toString());
				if(it.hasNext()) {
					buf.append(",");
				}
			}
			buf.append(")");
			return buf.toString();
		}
        public void typeCheck(Environment env,Language lang,boolean isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError {
            env.addExprType(this,Type.singleton(BasicType.Boolean.instance()));
			Type[] usedSig;
            Language.PredicateDescription description = lang.getDescription(name);
			if(description == null) {
                usedSig = funcs.get(name);
                if(usedSig==null) {
				    throw new TypeError.PredicateNotFound(name,line,column);
                }
            } else {
				if(isNegated) {
					usedSig = description.signature_not;
				} else {
					usedSig = description.signature;
				}
            }
            if(usedSig.length != arguments.size()) {
                throw new TypeError.WrongNumberOfArguments(name,description.signature.length,arguments.size());
            }
            Zip<Type,Expression> zip
                = new Zip<Type,Expression>(Arrays.asList(usedSig),arguments);
            for(Tuple<Type,Expression> tup : zip) {
                env.addExprType(tup.snd(),tup.fst());
                tup.snd().typeCheck(env,lang,false,funcs);
            }
        }
	}
	public static class Count extends Primary {
		protected java.lang.String var;
		protected Expression pattern;
        private TypeInformation inferedVarType;
		public Count(java.lang.String v,Expression e) {
			var = v;
			pattern = e;
		}
		public Tuple<Type,Typer> resolveTypes(Language lang,Type allowed,boolean isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError {
			if(!allowed.contains(BasicType.Number.instance())) {
				flawed(allowed,BasicType.Number.instance());
			}
			Tuple<Type,Typer> res = pattern.resolveTypes(lang,Type.singleton(BasicType.Boolean.instance()),false,funcs);
            inferedVarType = res.snd().getTypeInformation(var);
			res.snd().remove(var);
			return new Tuple<Type,Typer>(Type.singleton(BasicType.Number.instance()),res.snd());
		}
        public Type inferType(Language lang,Typer env) throws TypeError {
            Typer tp = new Typer();
            tp.addTypeBinding(var,inferedVarType);
            pattern.inferType(lang,new Typer(env,tp,true));
            return Type.singleton(BasicType.Number.instance());
        }
		public PLResult toPL(Language lang,int badness,Set<java.lang.String> grounded,boolean isNegated) {
			Set<java.lang.String> ngrounded = new HashSet<java.lang.String>();
			ngrounded.addAll(grounded);
			ngrounded.remove(var);
			PLResult result = pattern.toPL(lang,badness,ngrounded,false);
			if(result == null) {
				return null;
			}
			List<org.jpl7.Term> globalsEnd = result.globalsEnd;
			org.jpl7.Variable tmp = new org.jpl7.Variable();
            org.jpl7.Term countBody = result.term;
            for(org.jpl7.Variable var : result.anons) {
                countBody = new org.jpl7.Compound("^",new org.jpl7.Term[]
                    {var
                    ,countBody
                    });
            }
			globalsEnd.add(new org.jpl7.Compound("count",new org.jpl7.Term[]
				{new org.jpl7.Variable(var)
				,countBody
				,tmp
				}));
			return new PLResult(tmp,grounded,result.globalsStart,globalsEnd);
		}
		public java.lang.String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("[ ");
			buf.append(var);
			buf.append(" | ");
			buf.append(pattern.toString());
			buf.append(" ]");
			return buf.toString();
		}
        public void typeCheck(Environment env,Language lang,boolean isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError {
            env.addExprType(this,Type.singleton(BasicType.Number.instance()));
            env.addExprType(pattern,Type.singleton(BasicType.Boolean.instance()));
            pattern.typeCheck(env,lang,isNegated,funcs);
        }
	}
	public static class NameRef extends Primary {
		public List<java.lang.String> name;
		public NameRef(java.lang.String n) {
			name = Arrays.asList(n.split("\\."));
		}
		public Tuple<Type,Typer> resolveTypes(Language lang,Type allowed,boolean isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError {
			if(!lang.nameRefType().compatible(allowed)) {
				flawed(allowed,lang.nameRefType());
			}
			return new Tuple<Type,Typer>(lang.nameRefType(),new Typer());
		}
		public PLResult toPL(Language lang,int badness,Set<java.lang.String> grounded,boolean isNegated) {
			org.jpl7.Variable tmp = new org.jpl7.Variable();
			org.jpl7.Compound global = new org.jpl7.Compound("resolve",new org.jpl7.Term[]
				{org.jpl7.JPL.newJRef(name)
				,tmp});
			List<org.jpl7.Term> globalsStart = new LinkedList<org.jpl7.Term>();
			List<org.jpl7.Term> globalsEnd = new LinkedList<org.jpl7.Term>();
			globalsStart.add(global);
			return new PLResult(tmp,grounded,globalsStart,globalsEnd);
		}
		public java.lang.String toString() {
			StringBuffer buf = new StringBuffer();
			Iterator<java.lang.String> it = name.iterator();
			buf.append("\'");
			while(it.hasNext()) {
				buf.append(it.next());
				if(it.hasNext()) {
					buf.append(".");
				}
			}
			buf.append("\'");
			return buf.toString();			
		}
        public Type inferType(Language lang,Typer env) {
            return lang.nameRefType();
        }
        public void typeCheck(Environment env,Language lang,boolean isNegated,Map<java.lang.String,Type[]> funcs) {
            //TODO!!
        }
	}
	public static class Forall extends Primary {
		public Expression pattern;
		public Expression condition;
		public Forall(Expression p,Expression c) {
			pattern = p;
			condition = c;
		}
		public Tuple<Type,Typer> resolveTypes(Language lang,Type allowed,boolean isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError {
			if(!allowed.contains(BasicType.Boolean.instance())) {
				flawed(allowed,BasicType.Boolean.instance());
			}
			Typer tp1 = pattern.resolveTypes(lang,Type.singleton(BasicType.Boolean.instance()),false,funcs).snd();
			Typer tp2 = condition.resolveTypes(lang,Type.singleton(BasicType.Boolean.instance()),false,funcs).snd();
			Typer tp = new Typer(tp1,tp2,true);
			return new Tuple<Type,Typer>(Type.singleton(BasicType.Boolean.instance()),tp1);
		}
        public Type inferType(Language lang,Typer env) throws TypeError {
            pattern.inferType(lang,env);
            condition.inferType(lang,env);
            return Type.singleton(BasicType.Boolean.instance());
        }
		public PLResult toPL(Language lang,int badness,Set<java.lang.String> grounded,boolean isNegated) {
			PLResult patSolve = pattern.toPL(lang,badness,grounded,false);
			if(patSolve == null) {
				return null;
			}
			PLResult condSolve = condition.toPL(lang,badness,patSolve.grounded,false);
			if(condSolve == null) {
				return null;
			}
            org.jpl7.Term result = new org.jpl7.Compound("forall",new org.jpl7.Term[]
				{patSolve.term,condSolve.term});
            if(isNegated) {
                result = new org.jpl7.Compound("\\+",new org.jpl7.Term[]
                    {result});
            }
			return new PLResult(result,grounded);
		}
        public void typeCheck(Environment env,Language lang,boolean isNegated,Map<java.lang.String,Type[]> funcs) {
            //TODO!
            env.addExprType(this,Type.singleton(BasicType.Boolean.instance()));
        }
	}
	public static class ASTPrimary extends Primary {
		private java.lang.String source;
		private Object parsedAST;
		public ASTPrimary(java.lang.String str) {
			source = str;
			parsedAST = null;
		}
		public Tuple<Type,Typer> resolveTypes(Language lang,Type allowed,boolean isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError {
			parsedAST = lang.parseAST(source,allowed);
			if(parsedAST == null) {
				astFailed(source,allowed);
			}
			return new Tuple<Type,Typer>(allowed,new Typer());
		}
        public Type inferType(Language lang,Typer env) {
            return Type.free();
        }
		public PLResult toPL(Language lang,int badness,Set<java.lang.String> grounded,boolean isNegated) {
			return new PLResult(org.jpl7.JPL.newJRef(parsedAST),grounded);
		}
		public java.lang.String toString() {
			return "{"+source+"}";
		}
        public void typeCheck(Environment env,Language lang,boolean isNegated,Map<java.lang.String,Type[]> funcs) {
            //TODO!
        }
	}
}
