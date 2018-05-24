package modelica.resolver;

import java.util.List;
import java.util.Iterator;

public class ResolveException extends Exception {
	public List<String> resolvedName;
	public int line,column;
	public ResolveException(List<String> n,int l, int c) {
		resolvedName = n;
		line = l;
		column = c;
	}
	public String getLocalizedMessage() {
		String msg = "Couldn't resolve name ";
		Iterator<String> it = resolvedName.iterator();
		while(it.hasNext()) {
			msg+=it.next();
			if(it.hasNext()) {
				msg+=".";
			}
		}
		return msg;
	}
}
