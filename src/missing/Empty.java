package missing;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Empty<T> implements Iterator<T> {
	public boolean hasNext() {
		return false;
	}
	public T next() {
		throw new NoSuchElementException("Empty.next");
	}
	public void remove() {
		throw new UnsupportedOperationException("Empty.remove");
	}
}
