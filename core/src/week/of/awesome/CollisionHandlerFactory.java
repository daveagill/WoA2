package week.of.awesome;

import com.badlogic.gdx.physics.box2d.Contact;

public interface CollisionHandlerFactory {
	public CollisionHandler createCollisionHandlerForAToB(Contact contact, Object a, Object b);
}
