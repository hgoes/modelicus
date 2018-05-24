package modelica.ast;

import java.util.List;
import java.util.Iterator;
import java.io.PrintStream;


public class TypeSpecifier extends ModelicaAST {
	public List<String> name;
	public List<TypeSpecifier> specifiers;
	public List<ArraySubscript> subscripts;
	public void prettyPrint(PrintStream stream) {
		prettyPrintName(stream,name);
		if(specifiers != null) {
			Iterator<TypeSpecifier> it = specifiers.iterator();
			stream.print("<");
			while(it.hasNext()) {
				it.next().prettyPrint(stream);
				if(it.hasNext()) {
					stream.print(", ");
				}
			}
			stream.print("> ");
		}
		prettyPrintSubscripts(stream,subscripts);
	}
}
