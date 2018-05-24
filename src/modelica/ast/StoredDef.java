package modelica.ast;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.PrintStream;
import modelica.resolver.Resolveable;
import modelica.resolver.ResolveContext;
import modelica.resolver.ResolveException;

import modelicus.PrologGenerator;

/**
 * Represents a whole .mo file.
 */
public class StoredDef extends ModelicaAST {
	/**
	 * The package name this file is in
	 */
	public List<String> within;
	/**
	 * The classes that are defined in this file
	 */
	public List<ClassDef> classes;

	public StoredDef() {
		classes = new LinkedList<ClassDef>();
	}
	public void prettyPrint(PrintStream stream) {
		if(within!=null) {
			stream.print("within ");
			prettyPrintName(stream,within);
			stream.println(";");
		}
		for(ClassDef def : classes) {
			def.prettyPrint(stream);
		}
	}
	/**
	 * Recursivly resolves all names that are defined in this file.
	 */
	public void resolveAll(ResolveContext ctx) throws ResolveException {
		for(ClassDef def : classes) {
			def.resolveAll(ctx);
		}
	}
	/**
	 * Resolves a top-level name to a ClassDef.
	 * Note that this function must be called <u>after</u> resolveAll.
	 */
	public ClassDef resolveClass(List<String> name) throws ResolveException {
		ResolveContext ctx = new ResolveContext(new Context(this));
		return ctx.resolveMany(name).resolvedClass();
	}
	private static org.jpl7.Term stringListToPL(Iterator<String> it) {
		if(it.hasNext()) {
			return new org.jpl7.Compound(".",new org.jpl7.Term[]
			    {new org.jpl7.Atom(it.next())
			     ,stringListToPL(it)});
		} else {
			return new org.jpl7.Atom("[]");
		}
	}
	/*public void generateFacts(PrologGenerator gen) {
		jpl.Term pkg;
		if(within == null) {
			pkg = new jpl.Atom("[]");
		} else {
			pkg = stringListToPL(within.iterator());
		}
		for(ClassDef cls : classes) {
			cls.generateFacts(gen,pkg);
		}
	}*/
	public static class Context implements Resolveable {
		private StoredDef def;
		private int level;
		public Context(StoredDef d) {
			def = d;
			level = 0;
		}
		public Context(Context old) {
			def = old.def;
			level = old.level+1;
		}
		public ClassDef thisClass() {
			return null;
		}
		public ResolveContext resolve(String name,ResolveContext ctx)
			throws ResolveException {
			if(def.within!=null && level < def.within.size()) {
				if(def.within.get(level).equals(name)) {
					return ctx.goDown(new Context(this));
				} else {
					return null;
				}
			} else {
				ResolveContext res;
				for(ClassDef cl : def.classes) {
					res = cl.resolve(name,ctx);
					if(res != null) return res;
				}
				return null;
			}
		}
		public String thisName() {
			if(level == 0) {
				return ".";
			} else {
				return def.within.get(level-1);
			}
		}
	}
}
