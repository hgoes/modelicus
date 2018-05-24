package modelicus.typing;

import java.util.Set;
import java.util.Map;
import modelicus.config.Language;
import missing.Tuple;

/**
 * Interface for the type checking mechanism.
 * Every class implementing this can be typechecked.
 */
public interface Typed {
    /**
     * Used to typecheck an object.
     * @param lang the target language.
     * @param allowed the infered type.
     * @param isNegated whether the context is negated.
     * @param funcs all function signatures.
     * @return the resulting binding.
     */
	public Tuple<Type,Typer> resolveTypes(Language lang,Type allowed,boolean isNegated,Map<String,Type[]> funcs) throws TypeError;
}
