package modelicus.ast;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

import modelicus.typing.TypeInformation;
import modelicus.typing.Typer;
import modelicus.typing.Type;
import modelicus.typing.BasicType;
import modelicus.typing.TypeError;
import modelicus.config.Language;
import modelicus.config.Language;

public abstract class Advice extends ModelicusAST {
	public abstract void typeCheck(Language lang,Typer tp) throws TypeError;
	public abstract java.lang.String evaluate(Map<java.lang.String,org.jpl7.Term> entries);

	public static class Append extends Advice {
		public LinkedList<Advice> elements;
		public Append() {
			elements = new LinkedList<Advice>();
		}
		public void typeCheck(Language lang,Typer tp) throws TypeError {
			for(Advice adv : elements) {
				adv.typeCheck(lang,tp);
			}
		}
		public java.lang.String evaluate(Map<java.lang.String,org.jpl7.Term> entries) {
			StringBuilder buf = new StringBuilder();
			for(Advice adv : elements) {
				buf.append(adv.evaluate(entries));
			}
			return buf.toString();
		}
	}

	public static class String extends Advice {
		public java.lang.String content;
		public String(java.lang.String str) {
			content = str;
		}
		public void typeCheck(Language lang,Typer tp) {
		}
		public java.lang.String evaluate(Map<java.lang.String,org.jpl7.Term> entries) {
			return content;
		}
	}
	public static class Variable extends Advice {
		public java.lang.String name;
		public Variable(java.lang.String str) {
			name = str;
		}
		public void typeCheck(Language lang,Typer tp) throws TypeError.VariableNotBound {
			if(tp.getTypeInformation(name)==null) {
				throw new TypeError.VariableNotBound(line,column,name);
			}
		}
		public java.lang.String evaluate(Map<java.lang.String,org.jpl7.Term> entries) {
			return extract(entries).toString();
		}
        public Object extract(Map<java.lang.String,org.jpl7.Term> entries) {
            org.jpl7.Term term = entries.get(name);
            if(term.isJTrue()) {
                return Boolean.TRUE;
            }
            if(term.isJFalse()) {
                return Boolean.FALSE;
            }
            if(term.isInteger()) {
                return new Integer(term.intValue());
            }
            if(term.isAtom()) {
                return term.name();
            }
            if(term.isJRef()) {
                return term.jrefToObject();
            }
            throw new IllegalArgumentException("BUG: Term "+term.toString()+" (Type: "+term.typeName()+") from variable "+name+" can't be casted back");
        }
	}
	public static class Property extends Advice {
		private java.lang.String name;
		private Variable var;
		private Language.PropertyDescription descr;
		public Property(java.lang.String n,Variable v) {
			name = n;
			var = v;
		}
		public void typeCheck(Language lang,Typer tp) throws TypeError {
			TypeInformation info = tp.getTypeInformation(var.name);
			if(info==null) {
				throw new TypeError.VariableNotBound(line,column,var.name);
			}
			descr = lang.getProperty(name);
			if(descr==null) {
				throw new TypeError.PropertyNotFound(line,column,name);
			}
			if(!info.type.subtypeOf(descr.getSignature())) {
				throw new TypeError.FlawedArgument(line,column,info.type,descr.getSignature());
			}
		}
		public java.lang.String evaluate(Map<java.lang.String,org.jpl7.Term> entries) {

			return descr.apply(var.extract(entries));
		}
	}
}
