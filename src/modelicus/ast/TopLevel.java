package modelicus.ast;

public abstract class TopLevel extends ModelicusAST {
	public abstract Rule asRule();
	public abstract Function asFunction();
}
