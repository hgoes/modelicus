package modelica.ast;

import java.util.List;
import java.io.PrintStream;

import modelica.resolver.*;

public abstract class Flattened extends ClassDef.DefinitionOrDeclarationClause implements Resolveable {
	public AccessSpec access;
    public abstract void resolveAll(ResolveContext ctx) throws ResolveException;
	public static class Declaration extends Flattened {
        public String name;
		public TypePrefix prefix;
		public TypeSpecifier specifier;
        public ClassDef resolvedType;
		public Comment comment;
		public Expression conditional;
		public ClassModification.Modification modification;
		public List<ArraySubscript> subscripts;
		public void prettyPrint(PrintStream stream) {
			prefix.prettyPrint(stream);
			specifier.prettyPrint(stream);
			if(modification!=null) {
				modification.prettyPrint(stream);
			}
			prettyPrintSubscripts(stream,subscripts);
			if(conditional!=null) {
				stream.print(" if ");
				conditional.prettyPrint(stream);
			}
		}
        public ResolveContext resolve(String name,ResolveContext ctx) throws ResolveException {
            return resolvedType.resolve(name,ctx);
        }
        public ClassDef thisClass() {
            return null;
        }
        public String thisName() {
            return name;
        }
        public void resolveAll(ResolveContext ctx) throws ResolveException {
            ResolveContext nctx = ctx.resolveMany(specifier.name);
            if(nctx == null) {
                throw new ResolveException(specifier.name,getLine(),getColumn());
            }
            resolvedType = nctx.resolvedClass();
        }
	}
	public static class Definition extends Flattened {
		public ClassDef definition;
        public ClassDef thisClass() {
            return definition;
        }
        public ResolveContext resolve(String name,ResolveContext ctx) throws ResolveException {
            return definition.resolve(name,ctx);
        }
        public String thisName() {
            return definition.thisName();
        }
        public void resolveAll(ResolveContext ctx) throws ResolveException {
            definition.resolveAll(ctx);
        }
	}
	public static enum AccessSpec {
		PUBLIC,
		PROTECTED,
		DEFAULT
	}
}
