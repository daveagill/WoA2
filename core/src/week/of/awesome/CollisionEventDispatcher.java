package week.of.awesome;

import java.util.ArrayList;
import java.util.Collection;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

public class CollisionEventDispatcher implements ContactListener {

	private Collection<CollisionHandler> collisionsOnBegin = new ArrayList<CollisionHandler>();
	private Collection<CollisionHandler> collisionsOnEnd = new ArrayList<CollisionHandler>();
	
	private CollisionHandlerFactory collisionHandlerFactory;
	
	public CollisionEventDispatcher(CollisionHandlerFactory collisionHandlerFactory) {
		this.collisionHandlerFactory = collisionHandlerFactory;
	}
	
	@Override
	public void beginContact(Contact contact) {
		CollisionHandler c = findHandler(contact);
		
		if (c != null) {
			collisionsOnBegin.add(c);
		}
	}

	@Override
	public void endContact(Contact contact) {
		CollisionHandler c = findHandler(contact);
		
		if (c != null) {
			collisionsOnEnd.add(c);
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		CollisionHandler c = findHandler(contact);
		if (c != null) {
			c.onPreSolve();
		}
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub
		
	}
	
	public void dispatchCollisionEvents() {
		for (CollisionHandler c : collisionsOnBegin) {
			c.onBegin();
		}
		for (CollisionHandler c : collisionsOnEnd) {
			c.onEnd();
		}
		
		collisionsOnBegin.clear();
		collisionsOnEnd.clear();
	}
	
	private CollisionHandler findHandler(Contact contact) {
		Object a = contact.getFixtureA().getBody().getUserData();
		Object b = contact.getFixtureB().getBody().getUserData();
		
		if (a == null || b == null) { return null; }
		
		CollisionHandler c = collisionHandlerFactory.createCollisionHandlerForAToB(contact, a, b);
		if (c == null) {
			c = collisionHandlerFactory.createCollisionHandlerForAToB(contact, b, a);
		}
		
		return c;
	}
}
