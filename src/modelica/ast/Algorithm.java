package modelica.ast;

import java.util.List;

public abstract class Algorithm
	extends ModelicaAST
	implements ClassDef.AlgorithmOrAnnotation {

	public static class AssignClause extends Algorithm {
		public Expression.Simple leftHand;
		public Expression rightHand;
	}
	public static class ForClause extends Algorithm {
		public List<ForIndex> indices;
		public List<Algorithm> content;
	}
	public static class WhileClause extends Algorithm {
		public Expression whileExpr;
		public List<Algorithm> content;
	}
	public static class ConditionalEquation extends Algorithm {
		public List<ConditionalEquationNode> ifNodes;
		public List<Algorithm> elseNode;
	}
	public static class ConditionalEquationNode {
		public Expression ifExpr;
		public List<Algorithm> content;
	}
	public static class FunctionCall extends Algorithm {
		public ComponentReference component;
		public List<NamedArgument> namedArguments;
	}
	public static class FunctionCallFor extends FunctionCall {
		public List<ForIndex> indices;
	}
	public static class FunctionCallExpr extends FunctionCall {
		public List<Expression> arguments;
	}
	public static class WhenClause extends ConditionalEquationNode {
		
	}
	public static class Break extends Algorithm { }
	public static class Return extends Algorithm { }
	public static class NamedArgument {
		public String name;
		public Expression content;
	}
}
