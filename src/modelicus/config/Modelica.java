package modelicus.config;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.io.StringReader;

import modelicus.typing.BasicType;
import modelicus.typing.Type;
import modelica.parser.ModelicaParser;
import modelica.parser.ModelicaLexer;
import modelica.ast.*;

public class Modelica extends Language {
	public static class Class extends BasicType {
		private static Class inst = null;
		public static Class instance() {
			if(inst == null) {
				inst = new Class();
			}
			return inst;
		}
		public java.lang.String toString() {
			return "Class";
		}
	}
	public static class Model extends BasicType {
		private static Model inst = null;
		public static Model instance() {
			if(inst == null) {
				inst = new Model();
			}
			return inst;
		}
		public java.lang.String toString() {
			return "Model";
		}
	}
	public static class Connector extends BasicType {
		private static Connector inst = null;
		public static Connector instance() {
			if(inst == null) {
				inst = new Connector();
			}
			return inst;
		}
		public java.lang.String toString() {
			return "Connector";
		}
	}
	public static class Function extends BasicType {
		private static Function inst = null;
		public static Function instance() {
			if(inst == null) {
				inst = new Function();
			}
			return inst;
		}
		public java.lang.String toString() {
			return "Function";
		}
	}
	public static class Block extends BasicType {
		private static Block inst = null;
		public static Block instance() {
			if(inst == null) {
				inst = new Block();
			}
			return inst;
		}
		public java.lang.String toString() {
			return "Block";
		}
	}
	public static class Package extends BasicType {
		private static Package inst = null;
		public static Package instance() {
			if(inst == null) {
				inst = new Package();
			}
			return inst;
		}
		public java.lang.String toString() {
			return "Package";
		}
	}
	public static class Record extends BasicType {
		private static Record inst = null;
		public static Record instance() {
			if(inst == null) {
				inst = new Record();
			}
			return inst;
		}
		public java.lang.String toString() {
			return "Record";
		}
	}
	public static class Declaration extends BasicType {
		private static Declaration inst = null;
		public static Declaration instance() {
			if(inst==null) {
				inst = new Declaration();
			}
			return inst;
		}
		public java.lang.String toString() {
			return "Declaration";
		}
	}
	public static class Definition extends BasicType {
		private static Definition inst = null;
		public static Definition instance() {
			if(inst==null) {
				inst = new Definition();
			}
			return inst;
		}
		public java.lang.String toString() {
			return "Definition";
		}
	}
	public static class Equation extends BasicType {
		private static Equation inst = null;
		public static Equation instance() {
			if(inst==null) {
				inst = new Equation();
			}
			return inst;
		}
		public java.lang.String toString() {
			return "Equation";
		}
	}
	public static class Modification extends BasicType {
		private static Modification inst = null;
		public static Modification instance() {
			if(inst==null) {
				inst = new Modification();
			}
			return inst;
		}
		public java.lang.String toString() {
			return "Modification";
		}
	}
	public static class Expression extends BasicType {
		private static Expression inst = null;
		public static Expression instance() {
			if(inst==null) {
				inst = new Expression();
			}
			return inst;
		}
		public java.lang.String toString() {
			return "Expression";
		}
	}
	private HashMap<String,PredicateDescription> predMap;
	private HashMap<String,PropertyDescription> propMap;
	private Type allClass;
	public Modelica() {
		allClass = Type.construct
			(Class.instance()
			,Model.instance()
			,Connector.instance()
			,Function.instance()
			,Block.instance()
			,Package.instance()
            ,Record.instance());
		predMap = new HashMap<String,PredicateDescription>();
		for(BasicType tp : new BasicType[]
			{Class.instance(),Model.instance()
			,Connector.instance(),Function.instance()
			,Block.instance(),Package.instance()
            ,Record.instance()}) {

			String low = tp.toString().toLowerCase();
			predMap.put(low,new PredicateDescription
				(new Type[] {Type.singleton(tp)}
				,new Type[] {allClass.without(tp)}
				,new VariantDescription[]
					{new VariantDescription(0,1,low+"_check")
					,new VariantDescription(2,0,low+"_find")
					}
				,new VariantDescription[]
					{new VariantDescription(0,1,low+"_not_check")
					,new VariantDescription(2,0,low+"_not_find")
					}
				));
		}
		predMap.put("extends",new PredicateDescription
			(new Type[] {allClass,allClass}
			,new Type[] {allClass,allClass}
			,new VariantDescription[]
				{new VariantDescription(0,3,"extends_check")
				,new VariantDescription(1,1,"extends_by_sub")
				,new VariantDescription(3,0,"extends_find")
				}
			,new VariantDescription[]
				{new VariantDescription(0,3,"extends_not_check")
				,new VariantDescription(1,1,"extends_not_by_sub")
				,new VariantDescription(3,0,"extends_not_find")
				}
			));
		predMap.put("member",new PredicateDescription
			(new Type[] {allClass,Type.singleton(BasicType.String.instance())
					,Type.construct(Declaration.instance(),Definition.instance())}
			,new Type[] {allClass,Type.singleton(BasicType.String.instance())
					,Type.construct(Declaration.instance(),Definition.instance())}
			,new VariantDescription[]
				{new VariantDescription(0,3,"member_by_class_key")
				,new VariantDescription(1,1,"member_by_class")
				,new VariantDescription(2,2,"member_by_key")
				,new VariantDescription(3,0,"member_find")
				}
			,new VariantDescription[]
				{new VariantDescription(0,3,"member_not_by_class_key")
				,new VariantDescription(2,1,"member_not_by_class")
				,new VariantDescription(2,2,"member_not_by_key")
				,new VariantDescription(3,0,"member_not_find")
				}
			));
		predMap.put("equation",new PredicateDescription
			(new Type[] {allClass,Type.singleton(Equation.instance())}
			,new Type[] {allClass,Type.singleton(Equation.instance())}
			,new VariantDescription[]
				{new VariantDescription(0,3,"equation_check")
				,new VariantDescription(2,2,"equation_by_equation")
				,new VariantDescription(2,1,"equation_by_class")
				,new VariantDescription(3,0,"equation_find")
				}
			,new VariantDescription[]
                {new VariantDescription(0,3,"equation_not_check")
				,new VariantDescription(2,2,"equation_not_by_equation")
				,new VariantDescription(3,1,"equation_not_by_class")
				,new VariantDescription(4,0,"equation_not_find")
                }
			));
		for(java.lang.String name : new java.lang.String[] {"flow","input","output","constant","parameter"}) {
			predMap.put(name,new PredicateDescription
				(new Type[] {Type.singleton(Declaration.instance())}
				,new Type[] {Type.singleton(Declaration.instance())}
				,new VariantDescription[]
					{new VariantDescription(0,1,name+"_check")
					,new VariantDescription(3,0,name+"_find")
					}
				,new VariantDescription[]
					{new VariantDescription(0,1,name+"_not_check")
					,new VariantDescription(3,0,name+"_not_find")
					}
				));
		}
		predMap.put("variable",new PredicateDescription
			(new Type[] {Type.singleton(Equation.instance()),Type.singleton(BasicType.String.instance())}
			,new Type[] {Type.singleton(Equation.instance()),Type.singleton(BasicType.String.instance())}
			,new VariantDescription[]
				{new VariantDescription(0,1,"variable_by_eq")
				,new VariantDescription(3,0,"variable_find")
				}
			,new VariantDescription[]
				{new VariantDescription(0,3,"variable_not_check")
				,new VariantDescription(3,2,"variable_not_by_var")
				,new VariantDescription(4,1,"variable_not_by_eq")
				,new VariantDescription(5,0,"variable_not_by_eq")
				}
			));
		predMap.put("modification",new PredicateDescription
			(new Type[] {Type.singleton(Declaration.instance()),Type.singleton(Modification.instance())}
			,new Type[] {Type.singleton(Declaration.instance()),Type.singleton(Modification.instance())}
			,new VariantDescription[]
				{new VariantDescription(0,3,"modification_check")
				,new VariantDescription(1,1,"modification_by_decl")
				,new VariantDescription(2,2,"modification_by_modification")
				,new VariantDescription(3,0,"modification_find")
				}
			,new VariantDescription[]
				{new VariantDescription(0,3,"modification_not_check")
                ,new VariantDescription(2,2,"modification_not_by_modification")
				}
			));
		predMap.put("assignModification",new PredicateDescription
			(new Type[] {Type.singleton(Modification.instance()),Type.singleton(Expression.instance())}
			,new Type[] {Type.singleton(Modification.instance()),Type.singleton(Expression.instance())}
			,new VariantDescription[]
				{new VariantDescription(0,3,"assign_modification_check")
				,new VariantDescription(1,1,"assign_modification_by_mod")
				,new VariantDescription(2,2,"assign_modification_by_expr")
				,new VariantDescription(3,0,"assign_modification_find")
				}
			,new VariantDescription[]
				{new VariantDescription(0,3,"assign_modification_not_check")
				,new VariantDescription(2,2,"assign_modification_not_by_expr")
				}
			));
        predMap.put("typeOf",new PredicateDescription
            (new Type[] {Type.singleton(Declaration.instance()),allClass}
            ,new Type[] {Type.singleton(Declaration.instance()),allClass}
            ,new VariantDescription[]
                {new VariantDescription(0,1,"typeOf_by_decl")
                ,new VariantDescription(3,0,"typeOf_find")
                }
            ,new VariantDescription[]
                {new VariantDescription(0,3,"typeOf_not_check")
                ,new VariantDescription(2,1,"typeOf_not_by_decl")
                ,new VariantDescription(4,0,"typeOf_not_find")
                }
            ));
        predMap.put("name",new PredicateDescription
            (new Type[] {allClass,Type.singleton(BasicType.String.instance())}
            ,new Type[] {allClass,Type.singleton(BasicType.String.instance())}
            ,new VariantDescription[]
                {new VariantDescription(0,1,"name_check")
                ,new VariantDescription(3,2,"name_by_name")
                ,new VariantDescription(3,0,"name_find")
                }
            ,new VariantDescription[]
                {new VariantDescription(0,1,"name_not_check")
                ,new VariantDescription(3,2,"name_not_by_name")
                }
            ));
        predMap.put("definition",new PredicateDescription
            (new Type[] {Type.singleton(Definition.instance()),allClass}
            ,new Type[] {Type.singleton(Definition.instance()),allClass}
            ,new VariantDescription[]
                {new VariantDescription(0,1,"definition_by_def")
                ,new VariantDescription(3,0,"definition_find")
                }
            ,new VariantDescription[]
                {new VariantDescription(0,1,"definition_not_check")
                ,new VariantDescription(3,2,"definition_not_by_class")
                }
            ));
        for(String name : new String[] {"inner","outer"}) {
            predMap.put(name,new PredicateDescription
                (new Type[] {Type.construct(Definition.instance(),Declaration.instance())}
                ,new Type[] {Type.construct(Definition.instance(),Declaration.instance())}
                ,new VariantDescription[]
                    {new VariantDescription(0,1,name+"_check")
                    ,new VariantDescription(3,0,name+"_find")
                    }
                ,new VariantDescription[]
                    {new VariantDescription(0,1,name+"_not_check")
                    ,new VariantDescription(3,0,name+"_not_find")
                    }
                ));
        }
		propMap = new HashMap<String,PropertyDescription>();
		propMap.put("name",new PropertyDescription() {
			public Type getSignature() { return allClass; }
			public String apply(Object obj) {
				ClassDef def = (ClassDef)obj;
				return def.thisName();
			}
		});
	}
	public PredicateDescription getDescription(String name) {
        return predMap.get(name);
    }
    public PropertyDescription getProperty(String name) {
        return propMap.get(name);
    }
	public Object parseAST(String source,Type allowed) {
		ModelicaLexer lex = new ModelicaLexer(new StringReader(source));
		ModelicaParser p = new ModelicaParser(lex);
		if(allowed.contains(Equation.instance())) {
			try {
				return p.startEquation();
			} catch(Exception exp) {
				System.out.println(exp.toString());
			}
		}
		if(allowed.contains(Modification.instance())) {
			try {
				return p.startModification();
			} catch(Exception exp) {
				System.out.println(exp.toString());
			}
		}
		return null;
	}
	public static Object parseAST2(Language lang,String source,Type allowed) {
		ModelicaLexer lex = new ModelicaLexer(new StringReader(source));
		ModelicaParser p = new ModelicaParser(lex);
		if(allowed.contains(lang.typeFromString("Equation"))) {
			try {
				return p.startEquation();
			} catch(Exception exp) {
				System.out.println(exp.toString());
			}
		}
		if(allowed.contains(lang.typeFromString("Modification"))) {
			try {
				return p.startModification();
			} catch(Exception exp) {
				System.out.println(exp.toString());
			}
		}
		return null;
	}
	public Type nameRefType() {
		return allClass;
	}
    public BasicType typeFromString(String name) {
        BasicType res = super.typeFromString(name);
        if(res != null) {
            return res;
        }
        if("Class".equals(name)) {
            return Class.instance();
        }
        if("Model".equals(name)) {
            return Model.instance();
        }
        if("Connector".equals(name)) {
            return Connector.instance();
        }
        if("Function".equals(name)) {
            return Function.instance();
        }
        if("Block".equals(name)) {
            return Block.instance();
        }
        if("Package".equals(name)) {
            return Package.instance();
        }
        if("Declaration".equals(name)) {
            return Declaration.instance();
        }
        if("Equation".equals(name)) {
            return Equation.instance();
        }
        if("Modification".equals(name)) {
            return Modification.instance();
        }
        if("Expression".equals(name)) {
            return Expression.instance();
        }
        return null;
    }
}
