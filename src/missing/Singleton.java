package missing;

import java.util.Iterator;

public class Singleton<T> implements Iterator<T> {
	private T object;
	public Singleton(T o) {
		object = o;
	}
	public boolean hasNext() {
		return object!=null;
	}
	public T next() {
		T ret = object;
		object = null;
		return ret;
	}
	public void remove() {	
		object = null;
	}
}
