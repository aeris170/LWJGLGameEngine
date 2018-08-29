package entities;

import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;
import renderEngine.DisplayManager;
import terrains.Terrain;

public class Player extends Entity {

	private static final float MOVE_SPEED = 100;
	public static final float TURN_SPEED = 60;
	public static final float GRAVITY = -70;
	private static final float JUMP_POWER = 50;

	private float currentSpeed = 0;
	private float currentTurnSpeed = 0;
	private Vector3f currentRotation = new Vector3f(0, 0, 0);
	private float upwardsSpeed = 0;

	private boolean airborne = false;

	public Player(final TexturedModel model, final Vector3f position, final float rotX, final float rotY, final float rotZ, final float scale) {
		super(model, position, rotX, rotY, rotZ, scale);
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
		super.increaseRotation(currentRotation.x, getCurrentTurnSpeed() * DisplayManager.getFrameTimeSeconds(), currentRotation.z);
		final float distance = currentSpeed * DisplayManager.getFrameTimeSeconds();
		final float dx = (float) (distance * Math.sin(Math.toRadians(super.getRotY())));
		final float dz = (float) (distance * Math.cos(Math.toRadians(super.getRotY())));
		super.increasePosition(dx, 0, dz);
		upwardsSpeed += Player.GRAVITY * DisplayManager.getFrameTimeSeconds();
		super.increasePosition(0, upwardsSpeed * DisplayManager.getFrameTimeSeconds(), 0);
		if(terrain != null) {
			final float terrainHeight = terrain.getHeightOfTerrain(getPosition().x, getPosition().z);
			// != 0 is teleportation upon terrain change bug fix
			if((super.getPosition().y < terrainHeight + 2) && (terrainHeight != 0)) {
				airborne = false;
				upwardsSpeed = 0;
				super.getPosition().y = terrainHeight + 2;
			}
		} else {
			if(super.getPosition().y < 0) {
				airborne = false;
				upwardsSpeed = 0;
				super.getPosition().y = 0;
			}
		}
	}

	private void jump() {
		if(!airborne) {
			upwardsSpeed = Player.JUMP_POWER;
			airborne = true;
		}
	}

	private void catchEvents() {
		if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
			currentSpeed = Player.MOVE_SPEED;
			currentRotation.z = 5;
		} else if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
			currentSpeed = -Player.MOVE_SPEED;
			currentRotation.z = -5;
		} else {
			if(currentSpeed > 1) {
				currentSpeed -= Player.MOVE_SPEED / 100.0f;
				currentRotation.z -= 5.0f / 100;
			} else if(currentSpeed < -1) {
				currentSpeed += Player.MOVE_SPEED / 100.0f;
				currentRotation.z += 5.0f / 100;
			} else {
				currentSpeed = 0;
				currentRotation.z = 0;
			}
		}

		if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
			setCurrentTurnSpeed(Player.TURN_SPEED);
		} else if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
			setCurrentTurnSpeed(-Player.TURN_SPEED);
		} else {
			setCurrentTurnSpeed(0);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			jump();
		}
	}

	public float getCurrentTurnSpeed() {
		return currentTurnSpeed;
	}

	public void setCurrentTurnSpeed(float currentTurnSpeed) {
		this.currentTurnSpeed = currentTurnSpeed;
	}
}
