package modelica.ast;

import java.io.PrintStream;

public class TypePrefix extends ModelicaAST {
	public boolean isFlow;
	public enum TypePrefixKind {
		NormalPrefix,
		Discrete,
		Parameter,
		Constant
	}
	public TypePrefixKind kind;
	public enum TypePrefixDirection {
		Default,
		Input,
		Output
	}
	public TypePrefixDirection direction;
	public void prettyPrint(PrintStream stream) {
		if(isFlow) {
			stream.print("flow ");
		}
		switch(kind) {
			case NormalPrefix:
				break;
			case Discrete:
				stream.print("discrete ");
				break;
			case Parameter:
				stream.print("parameter ");
				break;
			case Constant:
				stream.print("constant ");
				break;
		}
		switch(direction) {
			case Default:
				break;
			case Input:
				stream.print("input ");
				break;
			case Output:
				stream.print("output ");
				break;
		}
	}
}
