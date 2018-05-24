package modelicus;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class PLResult {
	public org.jpl7.Term term;
	public Set<String> grounded;
	public List<org.jpl7.Term> globalsStart;
	public List<org.jpl7.Term> globalsEnd;
    public Set<org.jpl7.Variable> anons;
	public PLResult(org.jpl7.Term t,Set<String> g) {
		term = t;
		grounded = g;
		globalsStart = new LinkedList<org.jpl7.Term>();
		globalsEnd = new LinkedList<org.jpl7.Term>();
        anons = new HashSet<org.jpl7.Variable>();
	}
	public PLResult(org.jpl7.Term t,Set<String> g,Set<org.jpl7.Variable> anon) {
		term = t;
		grounded = g;
		globalsStart = new LinkedList<org.jpl7.Term>();
		globalsEnd = new LinkedList<org.jpl7.Term>();
        anons = anon;
	}
	public PLResult(org.jpl7.Term t,Set<String> g
		,List<org.jpl7.Term> glob1,List<org.jpl7.Term> glob2) {
		term = t;
		grounded = g;
		globalsStart = glob1;
		globalsEnd = glob2;
        anons = new HashSet<org.jpl7.Variable>();
	}
	public PLResult(org.jpl7.Term t,Set<String> g
		,List<org.jpl7.Term> glob1,List<org.jpl7.Term> glob2,Set<org.jpl7.Variable> anon) {
		term = t;
		grounded = g;
		globalsStart = glob1;
		globalsEnd = glob2;
        anons = anon;
	}
	public org.jpl7.Term toTerm() {
		org.jpl7.Term result = term;
		for(org.jpl7.Term glob : globalsEnd) {
			result = new org.jpl7.Compound(",",new org.jpl7.Term[]
				{result,glob});
		}
		for(org.jpl7.Term glob : globalsStart) {
			result = new org.jpl7.Compound(",",new org.jpl7.Term[]
				{glob,result});
		}
        for(org.jpl7.Variable var : anons) {
            result = new org.jpl7.Compound("^",new org.jpl7.Term[]
                {var,result});
        }
		return result;
	}
}
