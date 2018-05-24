package modelicus.ast;

import modelicus.typing.Type;
import modelicus.typing.TypeError;
import modelicus.config.Language;
import modelicus.Preprocessed;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

/**
 * A document contains rules and new primitive definitions.
 * It is read from a file by the Modelicus parser.
 */
public class Document extends ModelicusAST {
    /**
     * All the rules contained in the document.
     */
    public List<Rule> rules;
    /**
     * All the functions contained in the document.
     */
    public List<Function> functions;
    /**
     * Extract the signatures of all the primitives defined in the document.
     */
    public Map<String,Type[]> getSignatures(Language lang)
        throws TypeError.UnknownType {
        HashMap<String,Type[]> res = new HashMap<String,Type[]>();
        for(Function f : functions) {
            res.put(f.name,f.getSignature(lang));
        }
        return res;
    }
    /**
     * Preprocessing the document means typechecking all rules and primitives and compiling them to prolog.
     */
    public Preprocessed.Document preprocess(Language lang) throws TypeError {
        Map<String,Type[]> funcs = getSignatures(lang);
        Preprocessed.Document doc = new Preprocessed.Document();
        LinkedList<Preprocessed.Rule> _rules
            = new LinkedList<Preprocessed.Rule>();
        LinkedList<Preprocessed.Function> _functions
            = new LinkedList<Preprocessed.Function>();
        doc.rules = _rules;
        doc.functions = _functions;
        for(Rule rule : rules) {
            _rules.addLast(rule.preprocess(lang,funcs));
        }
        for(Function func : functions) {
            _functions.addLast(func.preprocess(lang,funcs));
        }
        return doc;
    }
}
