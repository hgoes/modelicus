package modelica.resolver;

import modelica.ast.ClassDef;

public interface Resolveable {
	public ClassDef thisClass();
	public ResolveContext resolve(String name,ResolveContext ctx) throws ResolveException;
	public String thisName();
}
