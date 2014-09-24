package week.of.awesome;

public interface CollisionHandler {

	public default void onBegin() { /* do nothing */ }
	public default void onEnd()   { /* do nothing */ }

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
