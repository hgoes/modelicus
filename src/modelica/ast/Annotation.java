package modelica.ast;

import modelica.resolver.ResolveContext;

public class Annotation extends ModelicaAST
	implements ClassDef.ElementOrAnnotation,
		   ClassDef.EquationOrAnnotation,
		   ClassDef.AlgorithmOrAnnotation {
	public ClassModification modification;
	public ResolveContext resolve(String name,ResolveContext ctx) {
		return null;
	}
	public ClassDef thisClass() {
		return null;
	}
	public void resolveAll(ResolveContext ctx) {
	}
}
