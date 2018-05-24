package modelicus.typing;

import java.util.Set;
import java.util.HashSet;

/**
 * A Type represents a set of {@link BasicType}s.
 * It can be constructed using {@link Type#free}, {@link Type#singleton} or {@link Type#construct}.
 */
public abstract class Type {
    /**
     * @return true, if tp is contained in this type.
     */
	public abstract boolean contains(BasicType tp);
    /**
     * Unifies the type with another type by using the semantics of an "and" operator.
     * @return a new Type, constructed from this Type and the argument.
     */
	public abstract Type unifyAnd(Type other);
    /**
     * Unifies the type with another type by using the semantics of an "or" operator.
     * @return a new Type, constructed from this Type and the argument.
     */
	public abstract Type unifyOr(Type other);
    /**
     * Whether or not a type is compatible to another one.
     * Two types are compatible if they share at least one contained {@link BasicType}.
     * @return true, if the types are compatible.
     */
	public abstract boolean compatible(Type other);
    /**
     * Finds out if this type is a subtype of another type.
     * To be a subtype, this type must contain all the {@link BasicType}s that the other type does.
     * @return true if it is a subtype of other.
     */
	public abstract boolean subtypeOf(Type other);
    /**
     * A type error is a type that contains no {@link BasicType}s.
     * This can happen if the type is constructed using the {@link Type#unifyAnd} method and the types don't share at least one BasicType.
     * @return true if the type is empty.
     */
	public abstract boolean isTypeError();
    /**
     * Construct a new type from this one that does not contain a specific BasicType.
     * @param tp the BasicType that shouldn't be in the new Type.
     * @return a new Type that contains exactly the same BasicTypes as this one with the exception of tp.
     */
	public abstract Type without(BasicType tp);
    /**
     * Construct a new Type from this one that contains a specific Type.
     * @param tp the BasicType that should be in the new Type.
     * @return a new Type that contains the same BasicTypes as this one and tp.
     */
	public abstract Type with(BasicType tp);
    /**
     * If the type is composed from only one basic type, this basic type is returned.
     * @return the basic type from which the type is constructed, null if the type is built from more than one basic type.
     */
    public abstract BasicType asSingleton();
    private Type() {}
	private static class Free extends Type {
		private static Free instance = null;
		public boolean contains(BasicType tp) {
			return true;
		}
		public static Free instance() {
			if(instance==null) {
				instance = new Free();
			}
			return instance;
		}
		public boolean equals(Object obj) {
			if(!(obj instanceof Free)) {
				return false;
			}
			return true;
		}
		public Type unifyAnd(Type other) {
			return other;
		}
		public Type unifyOr(Type other) {
			return Free.instance();
		}
		public boolean compatible(Type other) {
			if(other instanceof Free) {
				return true;
			} else if(other instanceof Compound) {
				return !((Compound)other).compounds.isEmpty();
			} else {
				return false;
			}
		}
		public boolean isTypeError() {
			return false;
		}
		public String toString() {
			return "Free";
		}
		public boolean subtypeOf(Type other) {
			return (other instanceof Free);
		}
		public Type without(BasicType tp) {
			throw new UnsupportedOperationException("Free.without()");
		}
		public Type with(BasicType tp) {
			return new Free();
		}
        public BasicType asSingleton() {
            return null;
        }
	}
	private static class Compound extends Type {
		private HashSet<BasicType> compounds;
		private Compound() {
			compounds = new HashSet<BasicType>();
		}
		public Compound(BasicType tp) {
			compounds = new HashSet<BasicType>();
			compounds.add(tp);
		}
		public Compound(BasicType... types) {
			compounds = new HashSet<BasicType>();
			for(BasicType tp : types) {
				compounds.add(tp);
			}
		}
		public boolean contains(BasicType tp) {
			return compounds.contains(tp);
		}
		public boolean equals(Object obj) {
			if(!(obj instanceof Compound)) {
				return false;
			}
			Compound comp = (Compound)obj;
			if(!compounds.equals(comp.compounds)) {
				return false;
			}
			return true;
		}
		public Type unifyAnd(Type other) {
			if(other instanceof Free) {
				return this;
			} else if(other instanceof Compound) {
				Compound otherC = (Compound)other;
				Compound res = new Compound();
				res.compounds.addAll(compounds);
				res.compounds.addAll(otherC.compounds);
				res.compounds.retainAll(compounds);
				res.compounds.retainAll(otherC.compounds);
				return res;
			} else {
				return null;
			}
		}
		public Type unifyOr(Type other) {
			if(other instanceof Free) {
				return Free.instance();
			} else if(other instanceof Compound) {
				Compound otherC = (Compound)other;
				Compound res = new Compound();
				res.compounds.addAll(compounds);
				res.compounds.addAll(otherC.compounds);
				return res;
			} else {
				return null;
			}
		}
		public boolean compatible(Type other) {
			if(other instanceof Free) {
				return !compounds.isEmpty();
			} else if(other instanceof Compound) {
				for(BasicType tp : ((Compound)other).compounds) {
					if(compounds.contains(tp)) {
						return true;
					}
				}
				return false;
			} else {
				return false;
			}
		}
		public boolean subtypeOf(Type other) {
			if(other instanceof Free) {
				return false;
			} else if (other instanceof Compound) {
				if(((Compound)other).compounds.containsAll(compounds)) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		public boolean isTypeError() {
			return compounds.isEmpty();
		}
		public String toString() {
			return compounds.toString();
		}
		public Type without(BasicType tp) {
			Compound res = new Compound();
			res.compounds = (HashSet<BasicType>)compounds.clone();
			res.compounds.remove(tp);
			return res;
		}
		public Type with(BasicType tp) {
			Compound res = new Compound();
			res.compounds = (HashSet<BasicType>)compounds.clone();
			res.compounds.add(tp);
			return res;
		}
        public BasicType asSingleton() {
            for(BasicType tp : compounds) {
                return tp;
            }
            return null;
        }
	}
    /**
     * Construct a new type that contains only one BasicType.
     * @param tp the BasicType contained in the new Type.
     * @return a new Type containing tp.
     */
	public static Type singleton(BasicType tp) {
		return new Compound(tp);
	}
    /**
     * Construct a new type containing many BasicTypes.
     * @param types all the BasicTypes that the new Type should contain.
     * @return a new type composed from the types list.
     */
	public static Type construct(BasicType... types) {
		return new Compound(types);
	}
    /**
     * Construct a new type containing all the possible BasicTypes.
     */
	public static Type free() {
		return Free.instance();
	}
}
