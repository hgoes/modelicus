package modelicus.ast;

import modelicus.typing.Environment;
import modelicus.typing.Type;
import modelicus.typing.Typer;
import modelicus.typing.TypeError;
import modelicus.config.Language;
import modelicus.Preprocessed;

import java.util.Map;

/**
 * Represents a parsed rule from a rule file.
 * It is neither type checked nor compiled.
 */
public class Rule extends ModelicusAST {
    /**
     * The pointcut of the rule that matches certain elements in the source tree.
     */
	public Expression pointcut;
    /**
     * The advice part is then evaluated upon the elements matched by the pointcut.
     */
	public Advice advice;
    /**
     * Construct a new Rule from an Expression and an Advice.
     * @param p the Expression to be the pointcut of the rule.
     * @param a the Advice of the new rule.
     */
	public Rule(Expression p,Advice a) {
		pointcut = p;
		advice = a;
	}
    /**
     * @return this instance.
     */
	public Rule asRule() {
		return this;
	}
    /**
     * @return null, because this is not a function
     */
	public Function asFunction() {
		return null;
	}
    /**
     * Typecheck and compile the rule to Prolog, but do not evaluate it.
     */
	public Preprocessed.Rule preprocess(Language lang,Map<String,Type[]> funcs)
        throws TypeError {
            Typer tp_check = pointcut.resolveTypes(lang,Type.free(),false,funcs).snd();
            pointcut.inferType(lang,tp_check);
            advice.typeCheck(lang,tp_check);
            pointcut.flattenTop();
            return new Preprocessed.Rule(pointcut.toPL(lang),advice);
	}
}
