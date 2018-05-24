package missing;

import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.LinkedList;

public class SkipList<T> implements Iterable<T> {
	private Set<Integer> out;
	private List<T> backstore;
	public SkipList() {
		out = new HashSet<Integer>();
		backstore = new LinkedList<T>();
	}
	public SkipList(List<T> bs) {
		out = new HashSet<Integer>();
		backstore = bs;
	}
	public SkipListIterator iterator() {
		return new SkipListIterator();
	}
	public class SkipListIterator implements Iterator<T> {
		private int curPos;
		private Iterator<T> it;
		public SkipListIterator() {
			it = backstore.iterator();
			curPos = 0;
		}
		public boolean hasNext() {
			while(out.contains(curPos)) {
				if(!it.hasNext()) {
					return false;
				} else {
					it.next();
					curPos++;
				}
			}
			return it.hasNext();
		}
		public T next() {
			while(out.contains(curPos)) {
				it.next();
				curPos++;
			}
			T ret = it.next();
			curPos++;
			return ret;
		}
		public void remove() {
			out.add(curPos);
		}
	}
}
