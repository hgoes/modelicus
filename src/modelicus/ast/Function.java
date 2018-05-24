package modelicus.ast;

import modelicus.typing.Type;
import modelicus.typing.Typer;
import modelicus.typing.BasicType;
import modelicus.typing.TypeInformation;
import modelicus.typing.TypeError;
import modelicus.config.Language;
import modelicus.Preprocessed;
import modelicus.PLResult;

import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Iterator;

/**
 * Represents a new primitive definition read from a rule file.
 */
public class Function extends ModelicusAST {
    /**
     * An argument description to a defined primitive.
     */
    public static class Argument {
        /**
         * The name of the argument.
         */
        public String name;
        /**
         * The type of the argument, encoded as a list of strings.
         * This list has to be resolved into instances of {@link modelicus.typing.BasicType} later on.
         */
        public List<String> type_names;
        /**
         * Constructs a new Argument.
         * @param n the name of the argument.
         * @param tp the type of the argument.
         */
        public Argument(String n,List<String> tp) {
            name = n;
            type_names = tp;
        }
    }
    /**
     * The name of the primitive.
     */
	public String name;
    /**
     * The arguments to the primitive.
     */
	public List<Argument> arguments;
    /**
     * The body of the primitive, which must bind all the arguments declared.
     */
	public Expression body;
    /**
     * @return this instance.
     */
	public Function asFunction() {
			return this;
	}
    /**
     * @return null, because this is not a rule.
     */
	public Rule asRule() {
			return null;
	}
    /**
     * Extract the signature of this function.
     * @param lang the target language. Needed because the types need to be looked up.
     * @return the type signature of the function.
     */
    public Type[] getSignature(Language lang) throws TypeError.UnknownType {
        Type[] res = new Type[arguments.size()];
        int c = 0;
        for(Argument arg : arguments) {
            BasicType[] basics = new BasicType[arg.type_names.size()];
            int d = 0;
            for(String basic : arg.type_names) {
                BasicType tp = lang.typeFromString(basic);
                if(tp==null) {
                    throw new TypeError.UnknownType(line,column,basic);
                }
                basics[d] = tp;
                d++;
            }
            res[c] = Type.construct(basics);
            c++;
        }
        return res;
    }
    /**
     * Typecheck and compile the primitive.
     * @param lang the target language.
     * @param funcs the signatures of all the functions. This is needed because primitives can call other new defined functions or even themselves.
     * @return the preprocessed function.
     */
	public Preprocessed.Function preprocess(Language lang,Map<String,Type[]> funcs) throws TypeError {
		Typer tp = body.resolveTypes(lang,
			Type.singleton(BasicType.Boolean.instance()),false,funcs).snd();
		Typer tp_not = body.resolveTypes(lang,
			Type.singleton(BasicType.Boolean.instance()),true,funcs).snd();
		int arity = arguments.size();
        Type[] sig = funcs.get(name);
        org.jpl7.Term[] _arguments = new org.jpl7.Term[arity];
		int c = 0;
		for(Argument var : arguments) {
			TypeInformation info = tp.getTypeInformation(var.name);
            if(info==null) {
                throw new TypeError.ArgumentNotMentioned(name,var.name,line,column);
            }
            if(!sig[c].subtypeOf(info.type)) {
                throw new TypeError.Mismatch(var.name,info,new TypeInformation(sig[c],line,column));
            }
            _arguments[c] = new org.jpl7.Variable(var.name);
			c++;
		}

		org.jpl7.Term[] bodies = new org.jpl7.Term[(1 << arity)];
		org.jpl7.Term[] bodies_not = new org.jpl7.Term[(1 << arity)];
		for(c=0 ; c < (1 << arity) ; c++) {
            org.jpl7.Term header = new org.jpl7.Compound(name+"_"+c,_arguments);
            org.jpl7.Term header_not = new org.jpl7.Compound(name+"_not_"+c,_arguments);
			HashSet<String> grounded = new HashSet<String>();
			for(int p = 0; p < arity; p++) {
				if((c | (1 << p)) == c) {
					grounded.add(arguments.get(p).name);
				}
			}
			PLResult res = null;
			for(int badness = 0 ; badness < 10 ; badness++) {
				res = body.toPL(lang,badness,grounded,false);
				if(res!=null) break;
			}
			PLResult res_not = null;
			for(int badness = 0 ; badness < 10 ; badness++) {
				res_not = body.toPL(lang,badness,grounded,true);
				if(res_not!=null) break;
			}
            if(res!=null) {
			    bodies[c] = new org.jpl7.Compound(":-",new org.jpl7.Term[] { header, res.toTerm() });
            } else {
                bodies[c] = null;
            }
            if(res_not!=null) {
    			bodies_not[c] = new org.jpl7.Compound(":-",new org.jpl7.Term[] { header_not, res_not.toTerm() });
            } else {
                bodies_not[c] = null;
            }
		}
		return new Preprocessed.Function(name,bodies,bodies_not);
	}
}
