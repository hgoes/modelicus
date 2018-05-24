package missing;

import java.util.Iterator;

public class Zip<A,B> implements Iterator<Tuple<A,B>>,Iterable<Tuple<A,B>> {
	private Iterator<A> iterA;
	private Iterator<B> iterB;
	public Zip(Iterable<A> listA,Iterable<B> listB) {
		iterA = listA.iterator();
		iterB = listB.iterator();
	}
	public boolean hasNext() {
		return iterA.hasNext() && iterB.hasNext();
	}
	public Tuple<A,B> next() {
		return new Tuple<A,B>(iterA.next(),iterB.next());
	}
	public void remove() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	public Iterator<Tuple<A,B>> iterator() {
		return this;
	}
}
