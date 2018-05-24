package modelicus.ast;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import modelicus.PLResult;
import modelicus.typing.*;
import modelicus.config.Language;

import missing.Tuple;

public class Sum extends Relation implements Typed {
	public List<SumElement> elements;
	public String toString() {
		StringBuffer buf = new StringBuffer();
		for(SumElement element : elements) {
			buf.append(element.toString());
		}
		return buf.toString();
	}
	public static class SumElement implements Typed {
		public Product product;
		public SumSign sign;
		public String toString() {
			return opToString() + product.toString();
		}
		private String opToString() {
			switch(sign) {
			case PLUS: return "+";
			case MINUS: return "-";
			default: return "???";
			}
		}
		public Tuple<Type,Typer> resolveTypes(Language lang,Type allowed,boolean isNegated,Map<String,Type[]> funcs) throws TypeError {
			return product.resolveTypes(lang,allowed,isNegated,funcs);
		}
		public List<Factor> flatten() {
			switch(sign) {
			case PLUS: return product.flatten();
			default: return null;
			}
		}
	}
	public enum SumSign {
		PLUS,
		MINUS
	}
	public Tuple<Type,Typer> resolveTypes(Language lang,Type allowed,boolean isNegated,Map<String,Type[]> funcs) throws TypeError {
		if(elements.size() == 1) {
			return elements.get(0).resolveTypes(lang,allowed,isNegated,funcs);
		} else {
			Typer res = new Typer();
			Type sub_allowed = Type.construct(BasicType.Number.instance());
			for(SumElement element : elements) {
				Tuple<Type,Typer> tmp = element.resolveTypes(lang,sub_allowed,isNegated,funcs);
				res = new Typer(res,tmp.snd(),true);
			}
			return new Tuple<Type,Typer>(sub_allowed,res);
		}
	}
    public Type inferType(Language lang,Typer env) throws TypeError {
        if(elements.size() == 1) {
            return elements.get(0).product.inferType(lang,env);
        } else {
            for(SumElement el : elements) {
                el.product.inferType(lang,env);
            }
            return Type.singleton(BasicType.Number.instance());
        }
    }
	public PLResult toPL(Language lang,int badness,Set<String> grounded,boolean isNegated) {
		LinkedList<SumElement> unsolved = new LinkedList<SumElement>();
		LinkedList<org.jpl7.Term> globalsStart = new LinkedList<org.jpl7.Term>();
		LinkedList<org.jpl7.Term> globalsEnd = new LinkedList<org.jpl7.Term>();
        Set<org.jpl7.Variable> toBeAnon = new HashSet<org.jpl7.Variable>();
		org.jpl7.Term result = null;
		unsolved.addAll(elements);
		boolean progress = true;
		while(progress) {
			Iterator<SumElement> it = unsolved.iterator();
			progress = false;
			while(it.hasNext()) {
				SumElement cur = it.next();
				PLResult solve_res = cur.product.toPL(lang,badness,grounded,isNegated);
				if(solve_res!=null) {
					it.remove();
					progress = true;
					if(result==null) {
						if(cur.sign == SumSign.MINUS) {
							result = new org.jpl7.Compound("-"
								,new org.jpl7.Term[] {solve_res.term});
						} else {
							result = solve_res.term;
						}
					} else {
						result = new org.jpl7.Compound(cur.opToString(),new org.jpl7.Term[]
							{result,solve_res.term});
					}
					grounded = solve_res.grounded;
					globalsStart.addAll(solve_res.globalsStart);
					globalsEnd.addAll(solve_res.globalsEnd);
                    toBeAnon.addAll(solve_res.anons);
				}
			}
			if(unsolved.isEmpty()) {
				return new PLResult(result,grounded,globalsStart,globalsEnd,toBeAnon);
			}
		}
		return null;
	}
	public List<Factor> flatten() {
		if(elements.size() == 1) {
			return elements.get(0).flatten();
		} else {
			//Real sums can't be flattened
			return null;
		}
	}
	public String asVariable() {
		if(elements.size() == 1) {
			SumElement only = elements.get(0);
			if(only.sign==SumSign.MINUS) {
				return null;
			} else {
				return only.product.asVariable();
			}
		} else {
			return null;
		}
	}
    public void typeCheck(Environment env,Language lang,boolean isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError {
        if(elements.size()>1) {
            env.addExprType(this,Type.singleton(BasicType.Number.instance()));
            for(SumElement el : elements) {
                env.addExprType(el.product,Type.singleton(BasicType.Number.instance()));
                el.product.typeCheck(env,lang,isNegated,funcs);
            }
        } else {
            SumElement el = elements.get(0);
            switch(el.sign) {
            case PLUS:
                env.addEqConstraint(this,el.product);
                break;
            case MINUS:
                env.addExprType(this,Type.singleton(BasicType.Number.instance()));
                env.addExprType(el.product,Type.singleton(BasicType.Number.instance()));
                break;
            }
            el.product.typeCheck(env,lang,isNegated,funcs);
        }
    }
}
