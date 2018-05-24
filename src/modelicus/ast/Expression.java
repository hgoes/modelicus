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

public class Expression extends Primary {
	public List<Term> terms;
	public java.lang.String toString() {
		StringBuffer buf = new StringBuffer();
		if(terms.size() > 1) {
			buf.append("OR[");
			Iterator<Term> it = terms.iterator();
			while(it.hasNext()) {
				buf.append(it.next().toString());
				if(it.hasNext()) {
					buf.append(" | ");
				}
			}
			buf.append("]");
		} else {
			buf.append(terms.get(0).toString());
		}
		return buf.toString();
	}
	public Tuple<Type,Typer> resolveTypes(Language lang,Type allowed,boolean isNegated,Map<java.lang.String,Type[]> funcs)
        throws TypeError {
		if(terms.size() == 1) {
            return terms.get(0).resolveTypes(lang,allowed,isNegated,funcs);
		} else {
			if(!allowed.contains(BasicType.Boolean.instance())) {
				flawed(allowed,BasicType.Boolean.instance());
			}
			Typer res = new Typer();
            //Tuple<Type,Typer> tup;
			Type sub_allowed = Type.construct(BasicType.Boolean.instance());
			for(Term term : terms) {
				Tuple<Type,Typer> tmp = term.resolveTypes(lang,sub_allowed,isNegated,funcs);
				res = new Typer(res,tmp.snd(),isNegated);
			}
			return new Tuple<Type,Typer>(sub_allowed,res);
		}
	}
    public Type inferType(Language lang,Typer env) throws TypeError {
        if(terms.size() == 1) {
            return terms.get(0).inferType(lang,env);
        } else {
            for(Term t : terms) {
                t.inferType(lang,env);
            }
            return Type.singleton(BasicType.Boolean.instance());
        }
    }
	public org.jpl7.Term toPL(Language lang) {
		Set<java.lang.String> grounded = new HashSet<java.lang.String>();
		for(int badness = 0 ; badness<=10 ; badness++) {
			PLResult solve = toPL(lang,badness,grounded,false);
			if(solve!=null) {
				return solve.toTerm();
			}
		}
		return null;
	}
	public static PLResult constructOr(Collection<? extends PLTransform> elems,Language lang,int badness,Set<java.lang.String> grounded,boolean isNegated) {
		org.jpl7.Term result = null;
		Set<java.lang.String> ngrounded = new HashSet<java.lang.String>();
		LinkedList<org.jpl7.Term> globalsStart = new LinkedList<org.jpl7.Term>();
		LinkedList<org.jpl7.Term> globalsEnd = new LinkedList<org.jpl7.Term>();
        Set<org.jpl7.Variable> toBeAnon = new HashSet<org.jpl7.Variable>();
		for(PLTransform t : elems) {
			PLResult solve = t.toPL(lang,badness,grounded,isNegated);
			if(solve==null) {
				return null;
			}
			if(result == null) {
				result = solve.term;
				ngrounded = solve.grounded;
			} else {
				result = new org.jpl7.Compound(";",new org.jpl7.Term[]
					{result,solve.term});
				ngrounded.retainAll(solve.grounded);
			}
			globalsStart.addAll(solve.globalsStart);
			globalsEnd.addAll(solve.globalsEnd);
            toBeAnon.addAll(solve.anons);
		}
		ngrounded.addAll(grounded);
		if(elems.size() > 1) {
			for(org.jpl7.Term term : globalsEnd) {
				result = new org.jpl7.Compound(",",new org.jpl7.Term[]
					{result,term});
			}
			for(org.jpl7.Term term : globalsStart) {
				result = new org.jpl7.Compound(",",new org.jpl7.Term[]
					{term,result});
			}
			return new PLResult(result,ngrounded,toBeAnon);
		} else {
			return new PLResult(result,ngrounded,globalsStart,globalsEnd,toBeAnon);
		}

	}
	public PLResult toPL(Language lang,int badness,Set<java.lang.String> grounded,boolean isNegated) {
		if(isNegated) {
			return Term.constructAnd(terms,lang,badness,grounded,isNegated);
		} else {
			return constructOr(terms,lang,badness,grounded,isNegated);
		}
	}
	public void flattenTop() {
		for(Term term : terms) {
			term.flattenTop();
		}
	}
	public List<Factor> flatten() {
		if(terms.size() == 1) {
			return terms.get(0).flatten();
		} else {
			return null;
		}
	}
	public java.lang.String asVariable() {
		if(terms.size() == 1) {
			return terms.get(0).asVariable();
		} else {
			return null;
		}
	}
    public void typeCheck(Environment env,Language lang,boolean isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError {
        if(terms.size() > 1) {
            env.addExprType(this,Type.singleton(BasicType.Boolean.instance()));
            for(Term t : terms) {
                env.addExprType(t,Type.singleton(BasicType.Boolean.instance()));
                t.typeCheck(env,lang,isNegated,funcs);
            }
        } else {
            env.addEqConstraint(this,terms.get(0));
            terms.get(0).typeCheck(env,lang,isNegated,funcs);
        }
    }
}
