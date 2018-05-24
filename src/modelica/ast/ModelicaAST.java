package modelica.ast;

import java.util.List;
import java.util.Iterator;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

import java.lang.Class;
import java.lang.System;
import java.lang.reflect.Field;

/**
 * The base class for all AST nodes. Makes it possible for every node to have a
 * line and column number attached to them and allows for some default
 * pretty-printing.
 */
public abstract class ModelicaAST implements PrettyPrint {
	public int getLine() {
		return 0;
	}
	public int getColumn() {
		return 0;
	}
	public void prettyPrint() {
		prettyPrint(System.out);
	}
	public String toString() {
		ByteArrayOutputStream outp = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(outp);
		this.prettyPrint(stream);
		return outp.toString();
	}
	/*
	 * This default implementation does some reflection magic to pretty print unknown nodes
	 */
	public void prettyPrint(PrintStream stream) {
		Class<?> cls = getClass();
		stream.print("<ModelicaAST ");
		stream.print(cls.getSimpleName());
		stream.println("> {");
		Field[] fields = cls.getFields();
		for(Field f : fields) {
			stream.print(f.getName());
			stream.print("(");
			stream.print(f.getType().getSimpleName());
			stream.print(")");
			stream.println(":");
			try {
				PrettyPrint print;
				print = (PrettyPrint)(f.get(this));
				if(print==null) {
					stream.println("not");
				} else {
					print.prettyPrint(stream);
					stream.println();
				}
			} catch (Exception e1) {
			try {
				boolean flag;
				flag = f.getBoolean(this);
				stream.println(flag);
			}
			catch(Exception e2) {
			try {
				List list;
				list = (List)(f.get(this));
				stream.print("[");
				for(Object obj : list) {
					try {
						((PrettyPrint)obj).prettyPrint(stream);
					} catch (Exception e3) {
					try {
						stream.print((String)obj);
					} catch (Exception e4) {
						stream.print("<not printable>");
					}
					}
					stream.println(",");
				}
				stream.print("]");
			}
			catch(Exception e3) {
			try {
				String str;
				str = (String)(f.get(this));
				stream.println(str);
			} catch(Exception e4) {
				stream.println("<not printable>");
			}
			}
			}
			}
		}
		stream.print("}");
	}
	protected static void prettyPrintName(PrintStream stream,List<String> name) {
		Iterator<String> it = name.iterator();
		while(it.hasNext()) {
			stream.print(it.next());
			if(it.hasNext()) {
				stream.print(".");
			}
		}
		stream.print(" ");
	}
	protected static void prettyPrintSubscripts(PrintStream stream,List<? extends PrettyPrint> list) {
		if(list!=null) {
			Iterator<? extends PrettyPrint> it = list.iterator();
			stream.print("[");
			while(it.hasNext()) {
				it.next().prettyPrint(stream);
				if(it.hasNext()) {
					stream.print(",");
				}
			}
			stream.print("]");
		}
	}
}
