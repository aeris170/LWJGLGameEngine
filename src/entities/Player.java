package entities;

import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;
import renderEngine.DisplayManager;
import terrains.Terrain;

public class Player extends Entity {

	public static final float GRAVITY = -70;
	private static final float JUMP_POWER = 50;

	private float moveSpeed = 2;
	private float upwardSpeed = 0;
	private float currentRotation = 0;

	private Vector3f forwardVector = new Vector3f();
	private Vector3f sidewaysVector = new Vector3f();
	private Vector3f upVector = new Vector3f(0, 1, 0);
	private Vector3f velocityVector = new Vector3f();
	private Vector3f oldVelocityVector = new Vector3f(0, 1, 0);

	private boolean airborne = false;
	public Light light = new Light(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), new Vector3f(1, 0.001f, 0.0002f));

	public Player(final TexturedModel model, int textureIndex, final Vector3f position, final float rotX, final float rotY,
			final float rotZ, final float scale) {
		super(model, textureIndex, position, rotX, rotY, rotZ, scale);
	}

	public void move(final List<Terrain> terrains) {
		Terrain terrain = null;
		for(final Terrain t : terrains) {
			if((t.getX() < super.getPosition().x) && ((t.getX() + t.getSize()) > super.getPosition().x)
					&& (t.getZ() < super.getPosition().z) && ((t.getZ() + t.getSize()) > super.getPosition().z)) {
				terrain = t;
				break;
			}
		}

		catchEvents();
		float ft = DisplayManager.getFrameTimeSeconds();
		upwardSpeed += Player.GRAVITY * ft;
		super.increasePosition(velocityVector.x * moveSpeed * ft, upwardSpeed * ft, velocityVector.z * moveSpeed * ft);

		if(terrain != null) {
			final float terrainHeight = terrain.getHeightOfTerrain(getPosition().x, getPosition().z);
			// != 0 is teleportation upon terrain change bug fix
			if((super.getPosition().y < terrainHeight + 2) && (terrainHeight != 0)) {
				if(upwardSpeed > JUMP_POWER / 50 || upwardSpeed < -JUMP_POWER / 50) {
					upwardSpeed /= -JUMP_POWER / 37.5f;
				} else {
					airborne = false;
					upwardSpeed = 0;
				}
				super.getPosition().y = terrainHeight + 2;
			}
		} else {
			if(super.getPosition().y < 0) {
				airborne = false;
				upwardSpeed = 0;
				super.getPosition().y = 0;
			}
		}
	}

	private void jump() {
		if(!airborne) {
			upwardSpeed = Player.JUMP_POWER;
			airborne = true;
		}
	}

	private void catchEvents() {
		if(Keyboard.isKeyDown(Keyboard.KEY_O)) {
			System.out.println(getPosition());
		}

		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			jump();
		}
		int forwardDirection = 0;
		int sidewaysDirection = 0;
		if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
			forwardDirection++;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
			sidewaysDirection++;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
			forwardDirection--;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
			sidewaysDirection--;
		}
		currentRotation += velocityVector.length() * 1.2;
		if((forwardDirection == 0 && sidewaysDirection == 0)) {
			if(velocityVector.x >= 0.2)
				velocityVector.x -= 0.2;
			else if(velocityVector.x <= -0.2)
				velocityVector.x += 0.2;
			else
				velocityVector.x = 0;
			if(velocityVector.y >= 0.2)
				velocityVector.y -= 0.2;
			else if(velocityVector.y <= -0.2)
				velocityVector.y += 0.2;
			else
				velocityVector.y = 0;
			if(velocityVector.z >= 0.2)
				velocityVector.z -= 0.2;
			else if(velocityVector.z <= -0.2)
				velocityVector.z += 0.2;
			else
				velocityVector.z = 0;
			return;
		}
		forwardVector.scale(0);
		sidewaysVector.scale(0);
		if(forwardDirection != 0) {
			forwardVector = (Vector3f) new Vector3f(Camera.Instance.getGazeVector()).scale(forwardDirection);
		}
		if(sidewaysDirection != 0) {
			sidewaysVector = (Vector3f) Vector3f.cross(upVector, Camera.Instance.getGazeVector(), new Vector3f(0, 0, 0)).scale(sidewaysDirection);
		}
		Vector3f compositeVector = new Vector3f();
		Vector3f.add(forwardVector, sidewaysVector, compositeVector);
		compositeVector.normalise();
		Vector3f.add(velocityVector, compositeVector, velocityVector);
		if(velocityVector.lengthSquared() > 400.0f) {
			velocityVector.scale(400.0f / velocityVector.lengthSquared());
		}
		oldVelocityVector.set(velocityVector);
	}

	public Vector3f getForwardVector() {
		return forwardVector;
	}

	public Vector3f getSidewaysVector() {
		return sidewaysVector;
	}

	public Vector3f getUpVector() {
		return upVector;
	}

	public Vector3f getVelocityVector() {
		if(velocityVector.length() == 0) {
			return oldVelocityVector;
		}
		return velocityVector;
	}

	public float getCurrentRotation() {
		return currentRotation;
	}
}