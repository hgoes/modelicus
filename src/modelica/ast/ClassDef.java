package modelica.ast;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.io.PrintStream;
import modelica.resolver.Resolveable;
import modelica.resolver.ResolveContext;
import modelica.resolver.ResolveException;

import modelicus.PrologGenerator;

/**
 * Represents a class definition (either class/model/type...) in the source.
 */
public class ClassDef extends ModelicaAST implements Resolveable {
	/**
	 * Whether or not the class is prefixed be "encapsulated".
	 */
	public boolean isEncapsulated;
	/**
	 * Whether this class is partial.
	 */
	public boolean isPartial;
	/**
	 * Whether this class is final.
	 */
	public boolean isFinal;
	private int line,col;
	public ClassDef(int l,int c) {
		line = l;
		col = c;
	}
	public int getLine() {
		return line;
	}
	public int getColumn() {
		return col;
	}
	/**
	 * Represents all kinds of classes.
	 */
	public enum ClassType {
		Class,
		Model,
		Record,
		Block,
		ExpandableConnector,
		Connector,
		Type,
		Package,
		Function,
		UnionType
	}
	/**
	 * The actual type of the class.
	 */
	public ClassType classType;
	public static abstract class ClassSpec extends ModelicaAST {
		public String getName() {
			return null;
		}
		public ResolveContext resolve(String name,ResolveContext ctx,ClassDef upper) {
			return null;
		}
		public void resolveAll(ResolveContext ctx,ClassDef upper) throws ResolveException {
		}
	}
	public static class NormalClassSpec extends ClassSpec {
		public String name;
		public List<String> comment;
		public Composition composition;
		public void prettyPrint(PrintStream stream) {
			stream.println(name);
			composition.prettyPrint(stream);
			stream.print("end ");
			stream.print(name);
			stream.println(";");
		}
		public String getName() {
			return name;
		}
		public ResolveContext resolve(String cname,ResolveContext ctx,ClassDef upper) {
			if(cname.equals(name)) {
				return ctx.goDown(upper);
			} else {
				return null;
			}
		}
		public void resolveAll(ResolveContext ctx,ClassDef upper) throws ResolveException {
			composition.resolveAll(ctx.goDown(upper));
		}
	}
	public static class ShortClassSpec extends ClassSpec {
		public String name;
		public TypePrefix prefix;
		public List<String> base;
		public List<ArraySubscript> subscripts;
		public ClassModification modifications;
		public Comment comment;
		public String getName() {
			return name;
		}
	}
	public static class EnumerationSpec extends ClassSpec {
		public String name;
		public Comment comment;
		public List<Literal> literals;
		public static class Literal extends ModelicaAST {
			public String name;
			public Comment comment;
		}
		public String getName() {
			return name;
		}
	}
	public ClassSpec spec;
	public static class Composition extends ModelicaAST {
		public List<ElementOrAnnotation> defaultElements;
		public List<CompositionContent> sections;
		public Map<String,Flattened> flattened;
        public Set<ClassDef> resolvedExtended;
		public Composition() {
			sections = new LinkedList<CompositionContent>();
		}
		public void prettyPrint(PrintStream stream) {
			for(ElementOrAnnotation el : defaultElements) {
				el.prettyPrint(stream);
				stream.println(";");
			}
			for(CompositionContent cont : sections) {
				cont.prettyPrint(stream);
			}
		}
		public ResolveContext resolve(String name,ResolveContext ctx) throws ResolveException {
            Flattened flat = flattened.get(name);
            if(flat!=null) {
                return ctx.goDown(flat);
            }
            for(ClassDef extend : resolvedExtended) {
                ResolveContext res = extend.resolve(name,ctx);
                if(res != null) {
                    return res;
                }
            }
			/*for(ElementOrAnnotation el : defaultElements) {
				res = el.resolve(name,ctx);
				if(res != null) {
					return res;
				}
			}
			for(CompositionContent section : sections) {
				res = section.resolve(name,ctx);
				if(res != null) {
					return res;
				}
			}*/
			return null;
		}
		public void resolveAll(ResolveContext ctx) throws ResolveException {
            flattened = new HashMap<String,Flattened>();
            resolvedExtended = new HashSet<ClassDef>();
            for(ElementOrAnnotation defEl : defaultElements) {
                processFlattenElement(ctx,defEl,Flattened.AccessSpec.DEFAULT);
            }
            for(CompositionContent cont : sections) {
                if(cont instanceof ClassDef.ProtectedSection) {
                    for(ElementOrAnnotation protEl : ((ClassDef.ProtectedSection)cont).elements) {
                        processFlattenElement(ctx,protEl,Flattened.AccessSpec.PROTECTED);
                    }
                } else if(cont instanceof ClassDef.PublicSection) {
                    for(ElementOrAnnotation protEl : ((ClassDef.PublicSection)cont).elements) {
                        processFlattenElement(ctx,protEl,Flattened.AccessSpec.PUBLIC);
                    }
                }
            }
            for(Flattened flat : flattened.values()) {
                flat.resolveAll(ctx);
            }
			/*for(ElementOrAnnotation el : defaultElements) {
				el.resolveAll(ctx);
			}
			for(CompositionContent section : sections) {
				section.resolveAll(ctx);
			}*/
		}
		private void processFlattenElement(ResolveContext ctx,ElementOrAnnotation defEl,Flattened.AccessSpec acc) throws ResolveException {
			if(defEl instanceof ClassDef.DefinitionClause) {
                ClassDef.DefinitionClause clause = (ClassDef.DefinitionClause)defEl;
				Flattened.Definition def = new Flattened.Definition();
				def.definition = clause.definition;
                def.isFinal = clause.isFinal;
                def.isInner = clause.isInner;
                def.isOuter = clause.isOuter;
                def.isRedeclare = clause.isRedeclare;
                def.isReplaceable = clause.isReplaceable;
				flattened.put(clause.definition.spec.getName(),def);
			} else if(defEl instanceof ClassDef.DeclarationClause) {
				ClassDef.DeclarationClause tmp = (ClassDef.DeclarationClause)defEl;
				Flattened.Declaration decl;
				for(ComponentDeclaration compDecl : tmp.declarations) {
					decl = new Flattened.Declaration();
                    decl.name = compDecl.declaration.name;
					decl.access = acc;
					decl.prefix = tmp.prefix;
					decl.specifier = tmp.specifier;
					decl.comment = compDecl.comment;
					decl.conditional = compDecl.conditional;
					decl.modification = compDecl.declaration.modification;
					decl.subscripts = compDecl.declaration.subscripts;
                    decl.isFinal = tmp.isFinal;
                    decl.isInner = tmp.isInner;
                    decl.isOuter = tmp.isOuter;
                    decl.isRedeclare = tmp.isRedeclare;
                    decl.isReplaceable = tmp.isReplaceable;
					flattened.put(compDecl.declaration.name,decl);
				}
			} else if(defEl instanceof ClassDef.ExtendsClause) {
                ClassDef.ExtendsClause clause = (ClassDef.ExtendsClause)defEl;
                ResolveContext cur = ctx.resolveMany(clause.from);
                if(cur==null) {
                    throw new ResolveException(clause.from,getLine(),getColumn());
                }
                ClassDef result = cur.resolvedClass();
                if(result==null) {
                    throw new ResolveException(clause.from,getLine(),getColumn());
                }
                resolvedExtended.add(result);
            }
		}
	}
	public static abstract class Element extends ModelicaAST implements ElementOrAnnotation {
		/*public ResolveContext resolve(String name,ResolveContext ctx) throws ResolveException {
			return null;
		}
		public void resolveAll(ResolveContext ctx) throws ResolveException {
		}*/
	}
	public static interface ElementOrAnnotation extends PrettyPrint {
		//public ResolveContext resolve(String name,ResolveContext ctx) throws ResolveException;
		//public void resolveAll(ResolveContext ctx) throws ResolveException;
	}
	public static interface EquationOrAnnotation extends PrettyPrint {
		public void resolveAll(ResolveContext ctx) throws ResolveException;
	}
	public static interface AlgorithmOrAnnotation extends PrettyPrint {
	}
	public static abstract class ImportClause extends Element {
		public Comment comment;
	}
	public static class ExplicitImportClause extends ImportClause {
		public String as;
		public List<String> path;
		private ResolveContext resolvedPath;
		public ExplicitImportClause() {
			resolvedPath = null;
		}
		public void prettyPrint(PrintStream stream) {
			stream.print("import ");
			stream.print(as);
			stream.print(" = ");
			prettyPrintName(stream,path);
		}
		public ResolveContext resolve(String name,ResolveContext ctx) throws ResolveException {
			resolveAll(ctx);
			if(name.equals(as)) {
				return resolvedPath;
			} else {
				return null;
			}
		}
		public void resolveAll(ResolveContext ctx) throws ResolveException {
			if(resolvedPath == null) {
				resolvedPath = ctx.resolveMany(path);
				if(resolvedPath == null) {
					throw new ResolveException(path,0,0);
				}
			}
		}
	}
	public static class ImplicitImportClause extends ImportClause {
		public boolean hasStar;
		public List<String> path;
		private ResolveContext resolvedPath;
		public ResolveContext resolve(String name,ResolveContext ctx) throws ResolveException {
			resolveAll(ctx);
			if(hasStar) {
				return resolvedPath.resolved().resolve(name,resolvedPath);
			} else {
				if(name.equals(path.get(path.size()-1))) {
					return resolvedPath;
				} else {
					return null;
				}
			}
		}
		public void resolveAll(ResolveContext ctx) throws ResolveException {
			if(resolvedPath == null) {
				resolvedPath = ctx.resolveMany(path);
				if(resolvedPath == null) {
					throw new ResolveException(path,0,0);
				}
			}
		}
	}
	public static abstract class CompositionContent extends ModelicaAST {
		public ResolveContext resolve(String name,ResolveContext ctx) throws ResolveException {
			return null;
		}
		public void resolveAll(ResolveContext ctx) throws ResolveException {
		}
	}
	public static class PublicSection extends CompositionContent {
		public List<ElementOrAnnotation> elements;
		public PublicSection(List<ElementOrAnnotation> els) {
			elements = els;
		}
		public void prettyPrint(PrintStream stream) {
			stream.println("public");
			for(ElementOrAnnotation el : elements) {
				el.prettyPrint(stream);
				stream.println(";");
			}
		}
		/*public ResolveContext resolve(String name,ResolveContext ctx) throws ResolveException {
			ResolveContext res;
			for(ElementOrAnnotation el : elements) {
				res = el.resolve(name,ctx);
				if(res!=null) return res;
			}
			return null;
		}
		public void resolveAll(ResolveContext ctx) throws ResolveException {
			for(ElementOrAnnotation element : elements) {
				element.resolveAll(ctx);
			}
		}*/
	}
	public static class ProtectedSection extends CompositionContent {
		public List<ElementOrAnnotation> elements;
		public ProtectedSection(List<ElementOrAnnotation> els) {
			elements = els;
		}
		public void prettyPrint(PrintStream stream) {
			stream.println("protected");
			for(ElementOrAnnotation el : elements) {
				el.prettyPrint(stream);
				stream.println(";");
			}
		}
		/*public ResolveContext resolve(String name,ResolveContext ctx) throws ResolveException {
			ResolveContext res;
			for(ElementOrAnnotation el : elements) {
				res = el.resolve(name,ctx);
				if(res!=null) return res;
			}
			return null;
		}
		public void resolveAll(ResolveContext ctx) throws ResolveException {
			for(ElementOrAnnotation element : elements) {
				element.resolveAll(ctx);
			}
		}*/
	}
	public static class ExtendsClause extends Element {
		public List<String> from;
		private ResolveContext resolvedFrom;
		public ClassModification modifications;
		public Annotation annotation;
		public ExtendsClause() {
			resolvedFrom = null;
		}
		public void prettyPrint(PrintStream stream) {
			stream.print("extends ");
			prettyPrintName(stream,from);
			if(modifications!=null) {
				modifications.prettyPrint(stream);
			}
			if(annotation!=null) {
				annotation.prettyPrint(stream);
			}
		}
		/*public ResolveContext resolve(String name,ResolveContext ctx) throws ResolveException {
			resolveAll(ctx);
			return resolvedFrom.resolvedClass().resolve(name,resolvedFrom);
		}
		public void resolveAll(ResolveContext ctx) throws ResolveException {
			if(resolvedFrom == null) {
				resolvedFrom = ctx.resolveMany(from);
				if(resolvedFrom == null) {
					throw new ResolveException(from,0,0);
				}
				if(resolvedFrom.resolvedClass() == null) {
					resolvedFrom = null;
					throw new ResolveException(from,0,0);
				}
			}
		}*/
		public ClassDef getResolvedFrom() {
			if(resolvedFrom == null) {
				throw new Error("Call \"resolveAll\" on the root node before using any getResolved* method");
			}
			return resolvedFrom.resolvedClass();
		}
		/*public ClassDef resolveFrom(StoredDef ast) {
			System.out.println("DEBUG: Resolving "+from.toString());
			return ast.resolveClass(from);
		}*/
	}
	public static abstract class DefinitionOrDeclarationClause extends Element {
		public boolean isRedeclare;
		public boolean isFinal;
		public boolean isInner;
		public boolean isOuter;
		public boolean isReplaceable;
		public ExtendsClause replaceable;
		public Comment replaceable_comment;
		public void prettyPrintHeader(PrintStream stream) {
			if(isRedeclare) {
				stream.print("redeclare ");
			}
			if(isFinal) {
				stream.print("final ");
			}
			if(isInner) {
				stream.print("inner ");
			}
			if(isOuter) {
				stream.print("outer ");
			}
			if(isReplaceable) {
				stream.print("replaceable ");
			}
		}
		public void prettyPrintFooter(PrintStream stream) {
			if(replaceable != null) {
				replaceable.prettyPrint(stream);
				if(replaceable_comment!=null) {
					replaceable_comment.prettyPrint(stream);
				}
			}
		}
	}
	public static class DefinitionClause extends DefinitionOrDeclarationClause {
		public ClassDef definition;
		public void prettyPrint(PrintStream stream) {
			prettyPrintHeader(stream);
			definition.prettyPrint(stream);
			prettyPrintFooter(stream);
		}
		public ResolveContext resolve(String name,ResolveContext ctx) throws ResolveException {
			return definition.resolve(name,ctx);
		}
		public void resolveAll(ResolveContext ctx) throws ResolveException {
			definition.resolveAll(ctx);
		}
	}
	public static class DeclarationClause extends DefinitionOrDeclarationClause {
		public TypePrefix prefix;
		public TypeSpecifier specifier;
		public List<ComponentDeclaration> declarations;
        public ClassDef resolvedType = null;
		public void prettyPrint(PrintStream stream) {
			Iterator<ComponentDeclaration> it = declarations.iterator();
			prettyPrintHeader(stream);
			prefix.prettyPrint(stream);
			specifier.prettyPrint(stream);
			while(it.hasNext()) {
				it.next().prettyPrint(stream);
				if(it.hasNext()) {
					stream.print(", ");
				}
			}
			prettyPrintFooter(stream);
		}
        public void resolveAll(ResolveContext ctx) throws ResolveException {
            ResolveContext nctx = ctx.resolveMany(specifier.name);
            if(nctx==null) {
                throw new ResolveException(specifier.name,getLine(),getColumn());
            }
            resolvedType = nctx.resolvedClass();
        }
	}
	public static class EquationSection extends CompositionContent {
		public boolean isInitial;
		public List<EquationOrAnnotation> equations;
		public void prettyPrint(PrintStream stream) {
			if(isInitial) {
				stream.println("initial ");
			}
			stream.println("equation");
			for(EquationOrAnnotation eq : equations) {
				eq.prettyPrint(stream);
				stream.println(";");
			}
		}
        public void resolveAll(ResolveContext ctx) throws ResolveException {
            for(EquationOrAnnotation eq : equations) {
                eq.resolveAll(ctx);
            }
        }
	}
	public static class AlgorithmSection extends CompositionContent {
		public boolean isInitial;
		public List<AlgorithmOrAnnotation> algorithms;
	}
	public void prettyPrint(PrintStream stream) {
		if(isEncapsulated) {
			stream.print("encapsulated ");
		}
		if(isPartial) {
			stream.print("partial ");
		}
		switch(classType) {
			case Class:
				stream.print("class ");
				break;
			case Model:
				stream.print("model ");
				break;
			case Record:
				stream.print("record ");
				break;
			case Block:
				stream.print("block ");
				break;
			case ExpandableConnector:
				stream.print("expandable connector ");
				break;
			case Connector:
				stream.print("connector ");
				break;
			case Type:
				stream.print("type ");
				break;
		}
		spec.prettyPrint(stream);
	}
	public ResolveContext resolve(String name,ResolveContext ctx) throws ResolveException {
		return spec.resolve(name,ctx,this);
	}
	public ClassDef thisClass() {
		return this;
	}
	public void resolveAll(ResolveContext ctx) throws ResolveException {
		spec.resolveAll(ctx,this);
		qualifiedName = ctx.goDown(this).resolvedName();
	}
	public String thisName() {
		return spec.getName();
	}
	private List<String> qualifiedName;
	/*public boolean isSubTypeOf(ClassDef sup) {
		if(this.qualifiedName.equals(sup.qualifiedName)) {
			return true;
		}
		
	}*/
}
