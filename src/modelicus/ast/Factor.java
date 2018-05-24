package modelicus.ast;

import modelicus.PLResult;
import modelicus.PLTransform;
import modelicus.typing.*;
import modelicus.config.Language;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;

import missing.Tuple;

public class Factor extends ModelicusAST implements Typed,PLTransform {
	public boolean isNegated;
	public Relation relation;
	public String toString() {
		if(!isNegated) {
			return relation.toString();
		} else {
			return "not "+relation.toString();
		}
	}
	public Tuple<Type,Typer> resolveTypes(Language lang,Type allowed,boolean _isNegated,Map<String,Type[]> funcs) throws TypeError {
		boolean rIsNegated;
		if(_isNegated) {
			rIsNegated = !isNegated;
		} else {
			rIsNegated = isNegated;
		}
		if(rIsNegated) {
			if(!allowed.contains(BasicType.Boolean.instance())) {
				flawed(allowed,BasicType.Boolean.instance());
			}
			Type sub_allowed = Type.construct(BasicType.Boolean.instance());
			return relation.resolveTypes(lang,sub_allowed,true,funcs);
		} else {
			return relation.resolveTypes(lang,allowed,false,funcs);
		}
	}
    public Type inferType(Language lang,Typer env) throws TypeError {
        return relation.inferType(lang,env);
    }
	public PLResult toPL(Language lang,int badness,Set<String> grounded,boolean _isNegated) {
		boolean rIsNegated;
		if(_isNegated) {
			rIsNegated = !isNegated;
		} else {
			rIsNegated = isNegated;
		}
		return relation.toPL(lang,badness,grounded,rIsNegated);
	}
	public List<Factor> flatten() {
		if(isNegated) {
			//TODO: find a way to deal with this
			return null;
		} else {
			return relation.flatten();
		}
	}
	public String asVariable() {
		if(isNegated) {
			return null;
		} else {
			return relation.asVariable();
		}
	}
    public void typeCheck(Environment env,Language lang,boolean _isNegated,Map<java.lang.String,Type[]> funcs) throws TypeError {
        boolean rIsNegated = _isNegated ? !isNegated : isNegated;
        if(rIsNegated) {
            env.addExprType(this,Type.singleton(BasicType.Boolean.instance()));
        } else {
            env.addEqConstraint(this,relation);
        }
        relation.typeCheck(env,lang,rIsNegated,funcs);
    }
}
