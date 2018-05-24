package modelicus;

import java.lang.System;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.File;

import modelicus.ast.*;
import modelicus.parser.*;
import modelicus.typing.*;
import modelicus.config.*;

import missing.Tuple;

public class Main {
    public static void main(String args[]) {
	try {
	    ModelicusLexer lex = new ModelicusLexer(new FileInputStream(args[0]));
	    ModelicusParser p = new ModelicusParser(lex);
	    PrologGenerator gen = new PrologGenerator();
	    gen.loadFile("lib/base.pl");
	    //Language lang = new Modelica();
            Language lang = new ParsedLanguage(new File("lib/Modelica.lang"));
	    gen.setAST(args[1]);
            Document doc = p.document();
            Preprocessed.Document pdoc = doc.preprocess(lang);
            pdoc.execute();
	} catch(Exception e) {
	    e.printStackTrace();
	}
    }
}
