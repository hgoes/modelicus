package missing;

public class Tuple<N,M> {
	private N first;
	private M second;
	public Tuple(N a,M b) {
		first = a;
		second = b;
	}
	public N fst() {
		return first;
	}
	public M snd() {
		return second;
	}
}
