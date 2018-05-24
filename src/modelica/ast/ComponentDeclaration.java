package modelica.ast;

import java.io.PrintStream;

public class ComponentDeclaration extends ModelicaAST {
	public Declaration declaration;
	public Expression conditional;
	public Comment comment;
	public void prettyPrint(PrintStream stream) {
		declaration.prettyPrint(stream);
		if(conditional != null) {
			stream.print("if ");
			conditional.prettyPrint(stream);
		}
		if(comment != null) {
			comment.prettyPrint(stream);
		}
	}
}
