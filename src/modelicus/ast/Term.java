package modelicus.ast;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Collection;

import modelicus.PLResult;
import modelicus.PLTransform;
import modelicus.typing.*;
import modelicus.config.Language;

import missing.Tuple;

public class Term extends ModelicusAST implements Typed,PLTransform {
	public List<Factor> factors;
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if(factors.size() > 1) {
			buf.append("AND[");
			Iterator<Factor> it = factors.iterator();
			while(it.hasNext()) {
				buf.append(it.next().toString());
				if(it.hasNext()) {
					buf.append(" | ");
				}
			}
			buf.append("]");
		} else {
			buf.append(factors.get(0).toString());
		}
		return buf.toString();
	}
	public Tuple<Type,Typer> resolveTypes(Language lang,Type allowed,boolean isNegated,Map<String,Type[]> funcs) throws TypeError {
		if(factors.size() == 1) {
			return factors.get(0).resolveTypes(lang,allowed,isNegated,funcs);
		} else {
			if(!allowed.contains(BasicType.Boolean.instance())) {
				flawed(allowed,BasicType.Boolean.instance());
			}
			Typer res = new Typer();
			Type sub_allowed = Type.construct(BasicType.Boolean.instance());
			for(Factor factor : factors) {
				Tuple<Type,Typer> tmp = factor.resolveTypes(lang,sub_allowed,isNegated,funcs);
				res = new Typer(res,tmp.snd(),!isNegated);
			}
			return new Tuple<Type,Typer>(sub_allowed,res);
		}
	}
    Type inferType(Language lang,Typer env) throws TypeError {
        if(factors.size() == 1) {
            return factors.get(0).inferType(lang,env);
        } else {
            for(Factor f : factors) {
                f.inferType(lang,env);
            }
            return Type.singleton(BasicType.Boolean.instance());
        }
    }
	public static PLResult constructAnd(Collection<? extends PLTransform> elems,Language lang,int badness,Set<String> grounded,boolean isNegated) {
		LinkedList<PLTransform> unsolved = new LinkedList<PLTransform>();
		LinkedList<org.jpl7.Term> globalsStart = new LinkedList<org.jpl7.Term>();
		LinkedList<org.jpl7.Term> globalsEnd = new LinkedList<org.jpl7.Term>();
        Set<org.jpl7.Variable> toBeAnon = new HashSet<org.jpl7.Variable>();
		org.jpl7.Term result = null;
		boolean progress = true;
		unsolved.addAll(elems);
		while(progress) {
			progress = false;
			Iterator<PLTransform> it = unsolved.iterator();
			while(it.hasNext()) {
				PLResult solve_res = it.next().toPL(lang,badness,grounded,isNegated);
				if(solve_res!=null) {
					it.remove();
					if(result == null) {
						result = solve_res.term;
					} else {
						result = new org.jpl7.Compound(",",new org.jpl7.Term[]
							{result
							,solve_res.term});
					}
					globalsStart.addAll(solve_res.globalsStart);
					globalsEnd.addAll(solve_res.globalsEnd);
					grounded = solve_res.grounded;
                    toBeAnon.addAll(solve_res.anons);
					progress = true;
				}
			}
			if(unsolved.isEmpty()) {
				if(elems.size() > 1) {
					for(org.jpl7.Term term : globalsEnd) {
						result = new org.jpl7.Compound(",",new org.jpl7.Term[]
							{result,term});
					}
					for(org.jpl7.Term term : globalsStart) {
						result = new org.jpl7.Compound(",",new org.jpl7.Term[]
							{term,result});
					}
					return new PLResult(result,grounded,toBeAnon);
				} else {
					return new PLResult(result,grounded,globalsStart,globalsEnd,toBeAnon);
				}
			}
		}
		return null;
	}
	public PLResult toPL(Language lang,int badness,Set<String> grounded,boolean isNegated) {
		if(isNegated) {
            if(factors.size() > 1) {
			    PLResult tmp = constructAnd(factors,lang,badness,grounded,false);
                if(tmp==null) return null;
                return new PLResult(new org.jpl7.Compound("\\+",new org.jpl7.Term[]
                    {tmp.term}),tmp.grounded,tmp.globalsStart,tmp.globalsEnd,tmp.anons);
            }
            else {
                return Expression.constructOr(factors,lang,badness,grounded,true);
            }
		} else {
			return constructAnd(factors,lang,badness,grounded,false);
		}
	}
	/**
	 * Tries to get rid of unneccessary AND nestings
	 */
	public void flattenTop() {
		LinkedList<Factor> nfactors = new LinkedList<Factor>();
		for(Factor factor : factors) {
			List<Factor> result = factor.flatten();
			if(result == null) {
				nfactors.addLast(factor);
			} else {
				nfactors.addAll(result);
			}
		}
		factors = nfactors;
	}
	public List<Factor> flatten() {
		return factors;
	}
	public String asVariable() {
		if(factors.size() == 1) {
			return factors.get(0).asVariable();
		} else {
			return null;
		}
	}
    public void typeCheck(Environment env,Language lang,boolean isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError {
        if(factors.size() > 1) {
            env.addExprType(this,Type.singleton(BasicType.Boolean.instance()));
            for(Factor f : factors) {
                env.addExprType(f,Type.singleton(BasicType.Boolean.instance()));
                f.typeCheck(env,lang,isNegated,funcs);
            }
        } else {
            env.addEqConstraint(this,factors.get(0));
            factors.get(0).typeCheck(env,lang,isNegated,funcs);
        }
    }
}
