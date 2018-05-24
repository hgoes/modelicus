package modelicus.typing;

import java.util.Set;

public abstract class TypeError extends Exception {
	public static class Mismatch extends TypeError {
		private String typeName;
		private TypeInformation typeInfo1;
		private TypeInformation typeInfo2;
		public Mismatch(String name,TypeInformation tp1,TypeInformation tp2) {
			typeName = name;
			typeInfo1 = tp1;
			typeInfo2 = tp2;
		}
		public String getLocalizedMessage() {
			StringBuffer buf = new StringBuffer();
			buf.append("Type mismatch for ");
			buf.append(typeName);
			buf.append(":");
			buf.append(typeInfo1.toString());
			buf.append(" and ");
			buf.append(typeInfo2.toString());
			return buf.toString();
		}
	}
    public static class RelationFailed extends TypeError {
        private int line;
        private int column;
        private Type lType;
        private Type rType;
        public RelationFailed(int l,int c,Type left,Type right) {
            line = l;
            column = c;
            lType = left;
            rType = right;
        }
        public String getLocalizedMessage() {
            return "Arguments for relation at line "+line+", column "+column+" must be of the same type (Left hand is "+lType.toString()+" and right hand is "+rType.toString()+")";
        }
    }
    public static class ArgumentNotMentioned extends TypeError {
        private String func_name;
        private String var_name;
        private int line;
        private int column;
        public ArgumentNotMentioned(String fn,String vn,int l,int c) {
            func_name = fn;
            var_name = vn;
            line = l;
            column = c;
        }
        public String getLocalizedMessage() {
            return "Argument "+var_name+" of function "+func_name+" is never mentioned at line "+line+", column "+column;
        }
    }
	public static class FlawedArgument extends TypeError {
		private int line;
		private int column;
		private Type infered;
		private Type expected;
		public FlawedArgument(int l, int c,Type inf,Type exp) {
			line = l;
			column = c;
			infered = inf;
			expected = exp;
		}
		public String getLocalizedMessage() {
			StringBuffer buf = new StringBuffer();
			buf.append("Wrong argument at line ");
			buf.append(line);
			buf.append(", column ");
			buf.append(column);
			buf.append(". Expected: ");
			buf.append(expected.toString());
			buf.append(" but got: ");
			buf.append(infered.toString());
			return buf.toString();
		}
	}
	public static class PredicateNotFound extends TypeError {
		private int line;
		private int column;
		private String name;
		public PredicateNotFound(String n,int l,int c) {
			name = n;
			line = l;
			column = c;
		}
		public String getLocalizedMessage() {
			StringBuffer buf = new StringBuffer();
			buf.append("Couldn't find predicate \"");
			buf.append(name);
			buf.append("\" at line: ");
			buf.append(line);
			buf.append(" column: ");
			buf.append(column);
			return buf.toString();
		}
	}
	public static class WrongNumberOfArguments extends TypeError {
		private int expected,got;
		private String name;
		public WrongNumberOfArguments(String n,int ex,int g) {
			name = n;
			expected = ex;
			got = g;
		}
		public String getLocalizedMessage() {
			StringBuffer buf = new StringBuffer();
			buf.append("Wrong number of arguments at line: ??? column: ???. Expected: ");
			buf.append(expected);
			buf.append(" but got: ");
			buf.append(got);
			return buf.toString();
		}
	}
	public static class DifferentNumberOfArguments extends TypeError {
        private Object ast1,ast2;
		public DifferentNumberOfArguments(Object call1,Object call2) {
            ast1=call1;
            ast2=call2;
		}
		public String getLocalizedMessage() {
			StringBuffer buf = new StringBuffer();
			buf.append("Different number of arguments for ");
            buf.append(ast1.toString());
            buf.append(" and ");
            buf.append(ast2.toString());
			return buf.toString();
		}
	}
	public static class AmbigousType extends TypeError {
        private String var;
		public AmbigousType(String a) {
            var=a;
		}
		public String getLocalizedMessage() {
			StringBuffer buf = new StringBuffer();
			buf.append(var);
            buf.append(" has ambigous type");
			return buf.toString();
		}
	}
	public static class CantUnify extends TypeError {
        private Type tp1,tp2;
		public CantUnify(Type t1,Type t2) {
            tp1 = t1;
            tp2 = t2;
		}
		public String getLocalizedMessage() {
			StringBuffer buf = new StringBuffer();
            buf.append("Can't unify type ");
			buf.append(tp1.toString());
            buf.append(" with type ");
            buf.append(tp2.toString());
			return buf.toString();
		}
	}
	public static class ASTNodeFailed extends TypeError {
		private String source;
		private Type infered;
		private int line;
		private int column;
		public ASTNodeFailed(int l,int c,String src,Type inf) {
			source = src;
			infered = inf;
			line = l;
			column = c;
		}
		public String getLocalizedMessage() {
			StringBuffer buf = new StringBuffer();
			buf.append("Parsing of AST node failed: Couldn't parse \"");
			buf.append(source);
			buf.append("\" to match types of: ");
			buf.append(infered.toString());
			buf.append(" at line: ");
			buf.append(line);
			buf.append(", column: ");
			buf.append(column);
			return buf.toString();
		}
	}
	public static class VariableNotBound extends TypeError {
		private String name;
		private int line;
		private int column;
		public VariableNotBound(int l,int c,String n) {
			line = l;
			column = c;
			name = n;
		}
		public String getLocalizedMessage() {
			return "Variable \""+name+"\" is not bound at line: "+line+", column: "+column;
		}
	}
	public static class PropertyNotFound extends TypeError {
		private String name;
		private int line;
		private int column;
		public PropertyNotFound(int l,int c,String n) {
			line = l;
			column = c;
			name = n;
		}
		public String getLocalizedMessage() {
			return "Property \""+name+"\" is not found at line: "+line+", column: "+column;
		}
	}
    public static class UnknownType extends TypeError {
        private String name;
        private int line;
        private int column;
        public UnknownType(int l,int c,String n) {
            line = l;
            column = c;
            name = n;
        }
        public String getLocalizedMessage() {
            return "Type \""+name+"\" at line: "+line+", column: "+column+" is unknown";
        }
    }
}
