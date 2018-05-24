package modelicus;

import org.jpl7.*;
import modelica.ast.*;
import modelica.parser.*;
import modelica.resolver.*;
import modelicus.typing.*;
import modelicus.config.*;

import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.Map;

public class PrologGenerator {
	public PrologGenerator() {
		if(JPL.getActualInitArgs() == null) {
			JPL.init();
		} else {
			throw new IllegalArgumentException("Sadly, we can only have exactly one PrologGenerator at the moment");
		}
	}
	public void assertFact(Term t) {
		Term goal = new Compound("assert",new Term[] { t });
		Query doIt = new Query(goal);
		doIt.hasSolution();
	}
	public void loadFile(String filename) {
		Term compile = new Compound("consult",new Term[] { new Atom(filename) });
		Query compileIt = new Query(compile);
		compileIt.hasSolution();
	}
	public void performGoal(String name) {
		Query action = new Query(new Compound(name,new Term[] { new Atom("Hi") }));
		while(action.hasMoreElements()) {
			action.nextElement();
		}
	}
	public void setAST(String filename) throws FileNotFoundException,antlr.RecognitionException,antlr.TokenStreamException,ResolveException {
		FileReader reader = new FileReader(filename);
		ModelicaLexer lex = new ModelicaLexer(reader);
		ModelicaParser p = new ModelicaParser(lex);
		StoredDef def = p.stored_definition();
		def.resolveAll(new ResolveContext(new StoredDef.Context(def)));
        //System.out.println(def.toString());
		setAST(def);
	}
	public void setAST(StoredDef def) {
		Term ref = JPL.newJRef(def);
		Term ast = new Compound("ast",new Term[] { ref });
		assertFact(ast);
	}
	/*public void query(Language lang,modelicus.ast.Rule rule) throws TypeError {
		Typer tp_check = rule.pointcut.resolveTypes(lang,Type.free(),false);
		rule.pointcut.flattenTop();
		Query qry = new Query(rule.pointcut.toPL(lang));
		//Map<String,TypeInformation> mapping = tp_check.getTypeMapping();
		while(qry.hasMoreSolutions()) {
			System.out.println(rule.advice.evaluate(qry.nextSolution()));
		}
	}*/
}
