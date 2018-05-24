package modelicus.ast;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import modelicus.PLResult;
import modelicus.typing.*;
import modelicus.config.Language;

import missing.Tuple;

public class Product extends ModelicusAST implements Typed {
	public Primary first;
	public List<ProductElement> elements;
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(first.toString());
		for(ProductElement element : elements) {
			buf.append(element.toString());
		}
		return buf.toString();
	}
	public static abstract class ProductElement implements Typed {
		public Primary value;
		public String toString() {
			return opToString() + value.toString();
		}
		public abstract String opToString();
		public Tuple<Type,Typer> resolveTypes(Language lang,Type allowed,boolean isNegated,Map<String,Type[]> funcs) throws TypeError {
			return value.resolveTypes(lang,allowed,isNegated,funcs);
		}
	}
	public static class Mult extends ProductElement {
		public String opToString() {
			return "*";
		}
	}
	public static class Div extends ProductElement {
		public String opToString() {
			return "/";
		}
	}
	public Tuple<Type,Typer> resolveTypes(Language lang,Type allowed,boolean isNegated,Map<String,Type[]> funcs) throws TypeError {
		if(elements.size() > 0) {
			Type sub_allowed = Type.singleton(BasicType.Number.instance());
			Typer tp = first.resolveTypes(lang,sub_allowed,isNegated,funcs).snd();
			for(ProductElement element : elements) {
				Tuple<Type,Typer> tmp = element.resolveTypes(lang,sub_allowed,isNegated,funcs);
				tp = new Typer(tp,tmp.snd(),true);
			}
			return new Tuple<Type,Typer>(Type.singleton(BasicType.Number.instance()),tp);
		} else {
			return first.resolveTypes(lang,allowed,isNegated,funcs);
		}
	}
    public Type inferType(Language lang,Typer env) throws TypeError {
        if(elements.size() == 0) {
            return first.inferType(lang,env);
        } else {
            first.inferType(lang,env);
            for(ProductElement el : elements) {
                el.value.inferType(lang,env);
            }
            return Type.singleton(BasicType.Number.instance());
        }
    }
	public PLResult toPL(Language lang,int badness,Set<String> grounded,boolean isNegated) {
		LinkedList<org.jpl7.Term> globalsStart = new LinkedList<org.jpl7.Term>();
		LinkedList<org.jpl7.Term> globalsEnd = new LinkedList<org.jpl7.Term>();
        Set<org.jpl7.Variable> toBeAnon = new HashSet<org.jpl7.Variable>();
		PLResult result = first.toPL(lang,badness,grounded,isNegated);
		if(result == null) {
			return null;
		}
		org.jpl7.Term term = result.term;
		grounded = result.grounded;
		globalsStart.addAll(result.globalsStart);
		globalsEnd.addAll(result.globalsEnd);
        toBeAnon.addAll(result.anons);
		for(ProductElement elem : elements) {
			result = elem.value.toPL(lang,badness,grounded,isNegated);
			if(result == null) {
				return null;
			}
			term = new org.jpl7.Compound(elem.opToString(),new org.jpl7.Term[]
				{term
				,result.term});
			grounded = result.grounded;
			globalsStart.addAll(result.globalsStart);
			globalsEnd.addAll(result.globalsEnd);
            toBeAnon.addAll(result.anons);
		}
		return new PLResult(term,grounded,globalsStart,globalsEnd,toBeAnon);
	}
	public List<Factor> flatten() {
		if(elements.isEmpty()) {
			return first.flatten();
		} else {
			return null;
		}
	}
	public String asVariable() {
		if(elements.isEmpty()) {
			return first.asVariable();
		} else {
			return null;
		}
	}
    public void typeCheck(Environment env,Language lang,boolean isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError {
        if(elements.isEmpty()) {
            env.addEqConstraint(this,first);
            first.typeCheck(env,lang,isNegated,funcs);
        } else {
            env.addExprType(this,Type.singleton(BasicType.Number.instance()));
            env.addExprType(first,Type.singleton(BasicType.Number.instance()));
            first.typeCheck(env,lang,isNegated,funcs);
            for(ProductElement el : elements) {
                env.addExprType(el.value,Type.singleton(BasicType.Number.instance()));
                el.value.typeCheck(env,lang,isNegated,funcs);
            }
        }
    }
}
