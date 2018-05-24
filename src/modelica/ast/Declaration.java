package modelica.ast;

import java.util.List;
import java.io.PrintStream;

public class Declaration extends ModelicaAST {
	public String name;
	public List<ArraySubscript> subscripts;
	public ClassModification.Modification modification;
	public void prettyPrint(PrintStream stream) {
		stream.print(name);
		stream.print(" ");
		prettyPrintSubscripts(stream,subscripts);
		if(modification != null) {
			modification.prettyPrint(stream);
		}
	}
}
