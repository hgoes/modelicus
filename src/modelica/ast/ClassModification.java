package modelica.ast;

import java.util.List;
import java.util.Iterator;
import java.io.PrintStream;

public class ClassModification extends ModelicaAST {
	public List<Argument> arguments;
	public static abstract class Argument extends ModelicaAST {
		public boolean isEach;
		public boolean isFinal;
		public boolean equals(Object obj) {
			if(!(obj instanceof Argument)) {
				return false;
			}
			Argument arg = (Argument)obj;
			if(isEach!=arg.isEach) {
				return false;
			}
			if(isFinal!=arg.isFinal) {
				return false;
			}
			return true;
		}
        public void prettyPrint(PrintStream stream) {
            if(isEach) {
                stream.print("each ");
            }
            if(isFinal) {
                stream.print("final ");
            }
        }
	}
	public static class ElementModification extends Argument {
		public ComponentReference element;
		public Modification modification;
		public List<String> comment;
		public boolean equals(Object obj) {
			if(!super.equals(obj)) {
				return false;
			}
			if(!(obj instanceof ElementModification)) {
				return false;
			}
			ElementModification mod = (ElementModification)obj;
			if(!element.equals(mod.element)) {
				return false;
			}
			if(modification==null) {
				if(mod.modification!=null) {
					return false;
				}
			} else {
				if(mod.modification==null) {
					return false;
				}
				if(!modification.equals(mod.modification)) {
					return false;
				}
			}
			if(comment==null) {
				if(mod.comment!=null) {
					return false;
				}
			} else {
				if(mod.comment==null) {
					return false;
				}
				if(!comment.equals(mod.comment)) {
					return false;
				}
			}
			return true;
		}
        public void prettyPrint(PrintStream stream) {
            super.prettyPrint(stream);
            element.prettyPrint(stream);
            modification.prettyPrint(stream);
        }
	}
	public static abstract class ElementReplaceable extends Argument {
		public ClassDef.ExtendsClause extend;
		public Comment comment;
		public boolean equals(Object obj) {
			if(!super.equals(obj)) {
				return false;
			}
			if(!(obj instanceof ElementReplaceable)) {
				return false;
			}
			ElementReplaceable mod = (ElementReplaceable)obj;
			if(extend==null) {
				if(mod.extend!=null) {
					return false;
				}
			} else {
				if(mod.extend==null) {
					return false;
				}
				if(!extend.equals(mod.extend)) {
					return false;
				}
			}
			if(comment==null) {
				if(mod.comment!=null) {
					return false;
				}
			} else {
				if(mod.comment==null) {
					return false;
				}
				if(comment.equals(mod.comment)) {
					return false;
				}
			}
			return true;
		}
	}
	public static class ElementReplaceableClass extends ElementReplaceable {
		public ClassDef classDefinition;
		public boolean equals(Object obj) {
			if(!super.equals(obj)) {
				return false;
			}
			if(!(obj instanceof ElementReplaceableClass)) {
				return false;
			}
			ElementReplaceableClass mod = (ElementReplaceableClass)obj;
			if(!classDefinition.equals(mod.classDefinition)) {
				return false;
			}
			return true;
		}
	}
	public static class ElementReplaceableComponent extends ElementReplaceable {
		public TypePrefix prefix;
		public TypeSpecifier specifier;
		public Declaration declaration;
		public Comment comment;
		public boolean equals(Object obj) {
			if(!super.equals(obj)) {
				return false;
			}
			if(!(obj instanceof ElementReplaceableComponent)) {
				return false;
			}
			ElementReplaceableComponent mod = (ElementReplaceableComponent)obj;
			if(!prefix.equals(mod.prefix)) {
				return false;
			}
			if(!specifier.equals(mod.specifier)) {
				return false;
			}
			if(!declaration.equals(mod.declaration)) {
				return false;
			}
			if(comment==null) {
				if(mod.comment!=null) {
					return false;
				}
			} else {
				if(mod.comment==null) {
					return false;
				}
				if(!comment.equals(mod.comment)) {
					return false;
				}
			}
			return true;
		}
	}
	public static abstract class ElementRedeclaration extends Argument {
		public static class NewClass extends ElementRedeclaration {
			public ClassDef content;
			public boolean equals(Object obj) {
				if(!super.equals(obj)) {
					return false;
				}
				if(!(obj instanceof NewClass)) {
					return false;
				}
				NewClass mod = (NewClass)obj;
				if(!content.equals(mod.content)) {
					return false;
				}
				return true;
			}
		}
		public static class NewDeclaration extends ElementRedeclaration {
			public TypePrefix prefix;
			public TypeSpecifier specifier;
			public Declaration declaration;
			public Comment comment;
			public boolean equals(Object obj) {
				if(!super.equals(obj)) {
					return false;
				}
				if(obj instanceof NewDeclaration) {
					return false;
				}
				NewDeclaration mod = (NewDeclaration)obj;
				if(!prefix.equals(mod.prefix)) {
					return false;
				}
				if(!specifier.equals(mod.specifier)) {
					return false;
				}
				if(!declaration.equals(mod.declaration)) {
					return false;
				}
				if(comment==null) {
					if(mod.comment!=null) {
						return false;
					}
				} else {
					if(mod.comment==null) {
						return false;
					}
					if(!comment.equals(mod.comment)) {
						return false;
					}
				}
				return true;
			}
		}
		public boolean equals(Object obj) {
			if(!(obj instanceof ElementRedeclaration)) {
				return false;
			}
			return super.equals(obj);
		}
	}
	public static abstract class Modification extends ModelicaAST {
	}
	public static class DeepModification extends Modification {
		public ClassModification modification;
		public Expression assigned;
		public boolean equals(Object obj) {
			if(!(obj instanceof DeepModification)) {
				return false;
			}
			DeepModification mod = (DeepModification)obj;
			if(!modification.equals(mod.modification)) {
				return false;
			}
			if(assigned==null) {
				if(mod.assigned!=null) {
					return false;
				}
			} else {
				if(mod.assigned==null) {
					return false;
				}
				if(!assigned.equals(mod.assigned)) {
					return false;
				}
			}
			return true;
		}
        public void prettyPrint(PrintStream stream) {
            modification.prettyPrint(stream);
            if(assigned!=null) {
                stream.print("=");
                assigned.prettyPrint(stream);
            }
        }
	}
	public static class EqualsModification extends Modification {
		public Expression assigned;
		public boolean equals(Object obj) {
			if(!(obj instanceof EqualsModification)) {
				return false;
			}
			EqualsModification mod = (EqualsModification)obj;
			if(!assigned.equals(mod.assigned)) {
				return false;
			}
			return true;
		}
        public void prettyPrint(PrintStream stream) {
            stream.print("=");
            assigned.prettyPrint(stream);
        }
	}
	public static class AssignModification extends Modification {
		public Expression assigned;
		public boolean equals(Object obj) {
			if(!(obj instanceof EqualsModification)) {
				return false;
			}
			EqualsModification mod = (EqualsModification)obj;
			if(!assigned.equals(mod.assigned)) {
				return false;
			}
			return true;
		}
	}
	public boolean equals(Object obj) {
		if(!(obj instanceof ClassModification)) {
			return false;
		}
		ClassModification mod = (ClassModification)obj;
		if(!arguments.equals(mod.arguments)) {
			return false;
		}
		return true;
	}
    public void prettyPrint(PrintStream stream) {
        stream.print("(");
        Iterator<Argument> it = arguments.iterator();
        while(it.hasNext()) {
            it.next().prettyPrint(stream);
            if(it.hasNext()) {
                stream.print(",");
            }
        }
        stream.print(")");
    }
}
