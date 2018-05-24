package modelicus.config;

import java.util.Map;
import java.util.Set;

import modelicus.typing.Type;
import modelicus.typing.BasicType;

/**
 * The set of functions that every language specific backend to the rule language will have to provide.
 */
public abstract class Language {
    /**
     * Parse a piece of source code into an AST node.
     * @param source the source code of the AST node.
     * @param allowed allowed types infered by the environment.
     * @return the parsed AST node, or null if the source doesn't parse.
     */
	public abstract Object parseAST(String source,Type allowed);
    /**
     * Get the type of name references in the rule language.
     */
	public abstract Type nameRefType();
    /**
     * Construct a type from a name.
     * If you want more than just the basic types, you have to override this function.
     */
    public BasicType typeFromString(String name) {
        if("String".equals(name)) {
            return BasicType.String.instance();
        }
        if("Boolean".equals(name)) {
            return BasicType.Boolean.instance();
        }
        if("Number".equals(name)) {
            return BasicType.Boolean.instance();
        }
        return null;
    }
    /**
     * Get the predicate description for a predicate name.
     * @param name the name of the predicate.
     * @return the predicate or null if it couldn't be found.
     */
	public abstract PredicateDescription getDescription(String name);
    /**
     * Get the property description for a property name.
     * @param name the name of the property.
     * @return the property or null if it couldn't be found.
     */
	public abstract PropertyDescription getProperty(String name);
    /**
     * The complete description of a predicate.
     */
	public static class PredicateDescription {
        /**
         * The signature of the predicate in a normal context.
         */
		public Type[] signature;
        /**
         * The signature of the predicate in a negated context.
         */
		public Type[] signature_not;
		private VariantDescription[] variants;
		private VariantDescription[] variants_not;
		/**
		 * Constructs a new PredicateDescription.
		 * Note that vars must be sorted ascending by badness.
         * @param sig the signature of the predicate.
         * @param sig_not the signature of the predicate in a negated context.
         * @param vars the variants of this predicate.
         * @param vars_not the variants for negated contexts.
		 */
		public PredicateDescription(Type[] sig
			,Type[] sig_not
			,VariantDescription[] vars
			,VariantDescription[] vars_not) {
			signature = sig;
			signature_not = sig_not;
			variants = vars;
			variants_not = vars_not;
		}
        /**
         * Determine the name of the best implementation of the predicate to use in a given compilation state.
         * @param max_badness the maximal badness a variant may have.
         * @param grounded the instantiation state of the arguments encoded binary as a natural number.
         * @param not whether the context is negated.
         * @return the name of the variant or null if no variant is suitable at the moment.
         */
		public java.lang.String getImplementation(int max_badness,int grounded,boolean not) {
			VariantDescription[] base;
			if(not) {
				base = variants_not;
			} else {
				base = variants;
			}
			for(VariantDescription var : base) {
				if(var.badness > max_badness) {
					return null;
				}
				if((grounded & var.groundness) == var.groundness) {
					return var.name;
				}
			}
			return null;
		}
	}
    /**
     * The complete description of a variant of a predicate.
     */
	protected static class VariantDescription {
        /**
         * A natural number encoding the algorithmic complexity of executing it.
         */
		public int badness;
        /**
         * A binary number encoding which arguments have to be instantiated before this variant may be used.
         */
		public int groundness;
        /**
         * The name of the variant.
         * Note that the name must be existent in the Prolog source of the language.
         */
		public java.lang.String name;
        /**
         * Constructs a new description.
         * @param bad the badness of the variant.
         * @param grnd the instantiation requirement of the variant.
         * @param n the name of the variant.
         */
		public VariantDescription(int bad,int grnd,java.lang.String n) {
			badness = bad;
			groundness = grnd;
			name = n;
		}
	}
    /**
     * The abstract base class for properties
     */
	public static abstract class PropertyDescription {
        /**
         * Get the signature from the property.
         */
		public abstract Type getSignature();
        /**
         * Execute the property.
         * @param arg the argument to the property.
         * @return the result of the property.
         */
		public abstract java.lang.String apply(Object arg);
	}
}
