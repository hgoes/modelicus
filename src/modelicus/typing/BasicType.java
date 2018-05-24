package modelicus.typing;

/**
 * Basic types is the building atoms of types.
 * A {@link Type} is built from these.
 */
public abstract class BasicType {
	public static class Number extends BasicType {
		private static Number inst = null;
		public static Number instance() {
			if(inst == null) {
				inst = new Number();
			}
			return inst;
		}
		public java.lang.String toString() {
			return "Number";
		}
	}
	public static class String extends BasicType {
		private static String inst = null;
		public static String instance() {
			if(inst == null) {
				inst = new String();
			}
			return inst;
		}
		public java.lang.String toString() {
			return "String";
		}
	}
	public static class Boolean extends BasicType {
		private static Boolean inst = null;
		public static Boolean instance() {
			if(inst == null) {
				inst = new Boolean();
			}
			return inst;
		}
		public java.lang.String toString() {
			return "Boolean";
		}
	}
}
