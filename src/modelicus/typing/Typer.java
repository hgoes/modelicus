package modelicus.typing;

import modelicus.Preprocessed;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

/**
 * Represents a state in the process of type checking.
 * Maps variable names to type bindings and merges bindings to produce the final variable types or report a type error.
 */
public class Typer {
	private Map<String,TypeInformation> typeMapping;
    /**
     * Creates an empty Typer
     */
	public Typer() {
		typeMapping = new HashMap<String,TypeInformation>();
	}
    /**
     * Builds a new Typer by merging two others.
     * @param t1 the first Typer
     * @param t2 the second Typer
     * @param isAnd whether the type bindings should be merged using the "and" semantics.
     */
	public Typer(Typer t1,Typer t2,boolean isAnd) throws TypeError {
		typeMapping = mergeType(t1.typeMapping,t2.typeMapping,isAnd);
	}
    /**
     * Adds a new type binding to a Typer.
     * If the mapping already exists, it is checked whether the existing binding is compatible to the new one.
     */
	public void addTypeBinding(String name,TypeInformation info)
		throws TypeError {
		TypeInformation orig = typeMapping.get(name);
		if(orig==null) {
			typeMapping.put(name,info);
		} else if(!orig.type.equals(info.type)) {
			throw new TypeError.Mismatch(name,orig,info);
		}
	}
    /**
     * Lookup the TypeInformation for a specific variable.
     * @param name the name of the variable.
     * @return the information about the type, or null if the variable isn't yet bound.
     */
	public TypeInformation getTypeInformation(String name) {
		return typeMapping.get(name);
	}
    /**
     * Removes the associated type information for a variable from the Typer.
     * @param name the name of the variable to be removed.
     */
	public void remove(String name) {
		typeMapping.remove(name);
	}
	private static Map<String,TypeInformation> mergeType
			(Map<String,TypeInformation> mp1
			,Map<String,TypeInformation> mp2
			,boolean isAnd)
			throws TypeError {
		if(mp1 == null) {
			return mp2;
		}
		if(mp2 == null) {
			return mp1;
		}
		Map<String,TypeInformation> result
			= new HashMap<String,TypeInformation>();
		for(Map.Entry<String,TypeInformation> entry1 : mp1.entrySet()) {
			TypeInformation ti2 = mp2.get(entry1.getKey());
    		TypeInformation res;
            if(ti2 != null) {
	    		if(isAnd) {
		    		res = entry1.getValue().unifyAnd(entry1.getKey(),ti2);
			    } else {
				    res = entry1.getValue().unifyOr(ti2);
			    }
            } else {
                res = entry1.getValue();
            }
			result.put(entry1.getKey(),res);
		}
		for(Map.Entry<String,TypeInformation> entry2 : mp2.entrySet()) {
			if(!mp1.containsKey(entry2.getKey())) {
				result.put(entry2.getKey(),entry2.getValue());
			}
		}
		return result;
	}
	public String toString() {
		StringBuilder buf = new StringBuilder("Typer (");
		for(Map.Entry<String,TypeInformation> entr : typeMapping.entrySet()) {
			buf.append(entr.getKey());
			buf.append(":");
			buf.append(entr.getValue().type.toString());
			buf.append(" ");
		}
		buf.append(")");
		return buf.toString();
	}
}
