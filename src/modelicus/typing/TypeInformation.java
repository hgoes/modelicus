package modelicus.typing;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * This is just a record of a type and a location in the source where that type
 * was infered.
 **/
public class TypeInformation {
    /**
     * The infered type.
     */
	public Type type;
    /**
     * The line in which the type was infered.
     */
	public int line;
    /**
     * The column in which the type was infered.
     */
	public int column;
    /**
     * Construct a TypeInformation using a basic type for the type.
     */
	public TypeInformation(BasicType tp) {
		type = Type.construct(tp);
		line = 0;
		column = 0;
	}
	public TypeInformation(Type tp) {
		type = tp;
		line = 0;
		column = 0;
	}
	public TypeInformation(Type tp,int l,int c) {
		type = tp;
		line = l;
		column = c;
	}
    /**
     * Unifies a type information with another one using the "and" semantics.
     * Throws a mismatch error when the types conflict.
     */
	public TypeInformation unifyAnd(String name,TypeInformation ti) throws TypeError.Mismatch {
		if(ti==null) {
			return this;
		} else {
			Type res = type.unifyAnd(ti.type);
			if(res.isTypeError()) {
				throw new TypeError.Mismatch(name,this,ti);
			}
			return new TypeInformation(res);
		}
	}
    /**
     * Unifies a type information with another one using the "or" semantics.
     */
	public TypeInformation unifyOr(TypeInformation ti) {
		return new TypeInformation(type.unifyOr(ti.type));
	}
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(type.toString());
		buf.append(" at line ");
		buf.append(line);
		buf.append(", column ");
		buf.append(column);
		return buf.toString();
	}
}
