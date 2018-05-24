package modelica.ast;

import java.util.List;
import java.util.Iterator;
import java.io.PrintStream;

public class Comment extends ModelicaAST {
	public List<String> stringComment;
	public Annotation annotation;
	public void prettyPrint(PrintStream stream) {
        if(stringComment != null) {
		    Iterator<String> it = stringComment.iterator();
		    while(it.hasNext()) {
		    	stream.print("\"");
		    	stream.print(it.next());
		    	stream.print("\"");
		    	if(it.hasNext()) {
		    		stream.print(" + ");
		    	}
		    }
	    }
        if(annotation!=null) {
            annotation.prettyPrint(stream);
        }
    }
}
