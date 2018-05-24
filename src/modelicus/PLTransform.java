package modelicus;

import modelicus.config.Language;
import java.util.Set;

public interface PLTransform {
	PLResult toPL(Language lang,int badness,Set<String> grounded,boolean isNegated);
}
