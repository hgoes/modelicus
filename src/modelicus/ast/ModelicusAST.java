package modelicus.ast;

import java.util.Set;
import modelicus.typing.TypeError;
import modelicus.typing.Type;
import modelicus.typing.BasicType;

/**
 * The base class for all nodes in the Modelicus AST.
 */
public abstract class ModelicusAST {
    /**
     * The line in which the element was declared.
     */
	public int line;
    /**
     * The column in which the element was declared.
     */
	public int column;
    /**
     * Helper function to throw the flawed argument exception.
     * @param inf the infered type.
     * @param exp the expected type.
     */
	protected void flawed(Type inf,Type exp) throws TypeError.FlawedArgument {
		throw new TypeError.FlawedArgument(line,column,inf,exp);
	}
    /**
     * Helper function to throw the flawed argument exception with a BasicType.
     * @param inf the infered type.
     * @param exp the expected basic type.
     */
	protected void flawed(Type inf,BasicType exp) throws TypeError.FlawedArgument {
		flawed(inf,Type.singleton(exp));
	}
    /**
     * Helper function to throw the ASTNodeFailed exception.
     * @param source the source code of the AST node.
     * @param inf the infered type of the AST node.
     */
	protected void astFailed(String source,Type inf) throws TypeError.ASTNodeFailed {
		throw new TypeError.ASTNodeFailed(line,column,source,inf);
	}
}
