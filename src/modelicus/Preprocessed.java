package modelicus;

import modelicus.ast.Advice;
import modelicus.typing.Type;

import java.util.Iterator;
import java.util.List;

/**
 * Just an empty class to allow for a nicer namespace.
 */
public abstract class Preprocessed {
    /**
     * A preprocessed document is a typechecked and compiled {@link modelicus.ast.Document}.
     */
    public static class Document {
        /**
         * All the rules contained in the document.
         */
        public List<Rule> rules;
        /**
         * All the functions contained in the document.
         */
        public List<Function> functions;
        /**
         * Execute the whole document.
         * This means handing all the prolog implementations into the prolog engine and evaluating each pointcut of each rule and executing their adivce part on the returnings.
         */
        public void execute() {
            for(Function func : functions) {
                for(org.jpl7.Term trm : func.bodies) {
                    if(trm!=null) {
                        //System.out.println(trm);
                        org.jpl7.Query ass = new org.jpl7.Query
                            (new org.jpl7.Compound("assert"
                                ,new org.jpl7.Term[] {trm}));
                        ass.hasSolution();
                    }
                }
                for(org.jpl7.Term trm : func.bodies_not) {
                    if(trm!=null) {
                        //System.out.println(trm);
                        org.jpl7.Query ass = new org.jpl7.Query
                            (new org.jpl7.Compound("assert"
                                ,new org.jpl7.Term[] {trm}));
                        ass.hasSolution();
                    }
                }
            }
            for(Rule rule : rules) {
                //System.out.println("DEBUG: "+rule.pointcut);
                org.jpl7.Query qry = new org.jpl7.Query(rule.pointcut);
                while(qry.hasMoreSolutions()) {
                    System.out.println(rule.advice.evaluate(qry.nextSolution()));
                }
            }
        }
    }
    /**
     * A preprocessed rule is a typechecked and compiled regular {@link modelicus.ast.Rule}.
     */
	public static class Rule implements Iterable<String> {
		private org.jpl7.Term pointcut;
		private Advice advice;
        /**
         * Construct a new rule from a Prolog term and an advice part.
         */
		public Rule(org.jpl7.Term pc,Advice adv) {
			pointcut = pc;
			advice = adv;
		}
        /**
         * This iterator will evaluate the rule and return the results of the advice part.
         */
		public Iterator<String> iterator() {
			return new Iterator<String>() {
				private org.jpl7.Query qry = new org.jpl7.Query(pointcut);
				public boolean hasNext() {
					return qry.hasMoreSolutions();
				}
				public String next() {
					return advice.evaluate(qry.nextSolution());
				}
				public void remove() {
					throw new UnsupportedOperationException("remove");
				}
			};
		}
	}
    /**
     * A preprocessed function is a typechecked and compiled regular {@link modelicus.ast.Function}.
     */
	public static class Function {
		private String name;
		private org.jpl7.Term[] bodies;
		private org.jpl7.Term[] bodies_not;
		public Function(String n
			,org.jpl7.Term[] bd,org.jpl7.Term[] bd_not) {
			name = n;
			bodies = bd;
			bodies_not = bd_not;
		}
		public String toString() {
			return "Function " + name + " -> "
                + java.util.Arrays.toString(bodies)
				+ "(" + java.util.Arrays.toString(bodies_not) + ");";
		}
	}
}
