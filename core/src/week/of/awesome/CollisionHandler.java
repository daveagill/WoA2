package week.of.awesome;

public interface CollisionHandler {

	public default void onPreSolve() { /* do nothing */ }
	public default void onBegin() { /* do nothing */ }
	public default void onEnd()   { /* do nothing */ }

	public static CollisionHandler onPreSolve(Runnable onPreSolve) {
		return new CollisionHandler() {
			public void onPreSolve() { onPreSolve.run(); }
		};
	}
	
	public static CollisionHandler onBegin(Runnable onBegin) {
		return new CollisionHandler() {
			public void onBegin() { onBegin.run(); }
		};
	}
	
	public static CollisionHandler onEnd(Runnable onEnd) {
		return new CollisionHandler() {
			public void onEnd() { onEnd.run(); }
		};
	}
}
