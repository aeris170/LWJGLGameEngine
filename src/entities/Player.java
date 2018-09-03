package entities;

import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;
import renderEngine.DisplayManager;
import terrains.Terrain;

public class Player extends Entity {

	private static final float MOVE_SPEED = 100;
	public static final float TURN_SPEED = 80;
	public static final float GRAVITY = -70;
	private static final float JUMP_POWER = 50;
	private static final float FRICTION_POWER = MOVE_SPEED / 100;
	private static final float MAX_ROTATION_SPEED = 50;

	private Vector3f currentSpeed = new Vector3f(0, 0, 0);
	private float currentTurnSpeed = 0;
	private Vector3f currentRotation = new Vector3f(0, 0, 0);
	private Vector3f gazeVector = new Vector3f(0, 0, 0);
	private Vector3f perpVector = new Vector3f(0, 0, 0);
	private Vector3f upVector = new Vector3f(0, 0, 0);

	public int flagF = 0;
	public int flagS = 0;

	private boolean airborne = false;
	public Light light = new Light(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), new Vector3f(1, 0.001f, 0.0002f));

	public Player(final TexturedModel model, int i, final Vector3f position, final float rotX, final float rotY,
			final float rotZ, final float scale) {
		super(model, i, position, rotX, rotY, rotZ, scale);
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

		float yRotation = super.getRotY();
		gazeVector.x = (float) Math.sin(Math.toRadians(yRotation));
		gazeVector.z = (float) Math.cos(Math.toRadians(yRotation));
		Vector3f.cross(new Vector3f(0, 1, 0), gazeVector, perpVector);
		Vector3f.cross(gazeVector, perpVector, upVector);

		super.increaseRotation(currentRotation.x, 0, currentRotation.z);
		final float distanceX = currentSpeed.x * DisplayManager.getFrameTimeSeconds();
		currentSpeed.y += Player.GRAVITY * DisplayManager.getFrameTimeSeconds();
		final float distanceZ = currentSpeed.z * DisplayManager.getFrameTimeSeconds();

		// X
		super.increasePosition(gazeVector.x * distanceX, 0, gazeVector.z * distanceX);
		// Y
		super.increasePosition(0, currentSpeed.y * DisplayManager.getFrameTimeSeconds(), 0);
		// Z
		super.increasePosition(perpVector.x * distanceZ, 0, perpVector.z * distanceZ);

		if(terrain != null) {
			final float terrainHeight = terrain.getHeightOfTerrain(getPosition().x, getPosition().z);
			// != 0 is teleportation upon terrain change bug fix
			if((super.getPosition().y < terrainHeight + 2) && (terrainHeight != 0)) {
				if(currentSpeed.y > JUMP_POWER / 50 || currentSpeed.y < JUMP_POWER / -50) {
					currentSpeed.y /= -JUMP_POWER / 37.5f;
				} else {
					airborne = false;
					currentSpeed.y = 0;
				}
				super.getPosition().y = terrainHeight + 2;
			}
		} else {
			if(super.getPosition().y < 0) {
				airborne = false;
				currentSpeed.y = 0;
				super.getPosition().y = 0;
			}
		}
	}

	private void jump() {
		if(!airborne) {
			currentSpeed.y = Player.JUMP_POWER;
			airborne = true;
		}
	}

	private void catchEvents() {
		flagF = 0;
		flagS = 0;
		if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
			if(currentSpeed.x < Player.MOVE_SPEED) {
				currentSpeed.x += Player.MOVE_SPEED / 50;
				currentRotation.x += MAX_ROTATION_SPEED / 50;
			}
			flagF++;
		} else if(Keyboard.isKeyDown(Keyboard.KEY_S)) {
			if(currentSpeed.x > -Player.MOVE_SPEED) {
				currentSpeed.x -= Player.MOVE_SPEED / 50;
				currentRotation.x -= MAX_ROTATION_SPEED / 50;
			}
			flagF++;
		} else {
			if(currentSpeed.x > 1) {
				currentSpeed.x -= FRICTION_POWER;
				currentRotation.x -= MAX_ROTATION_SPEED / 100;
			} else if(currentSpeed.x < -1) {
				currentSpeed.x += FRICTION_POWER;
				currentRotation.x += MAX_ROTATION_SPEED / 100;
			} else {
				currentSpeed.x = 0;
				currentRotation.x = 0;
			}
		}

		if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
			if(currentSpeed.z < Player.MOVE_SPEED) {
				currentSpeed.z += Player.MOVE_SPEED / 50;
				currentRotation.z += MAX_ROTATION_SPEED / 50;
			}
			flagS++;
		} else if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
			if(currentSpeed.z > -Player.MOVE_SPEED) {
				currentSpeed.z -= Player.MOVE_SPEED / 50;
				currentRotation.z -= MAX_ROTATION_SPEED / 50;
			}
			flagS++;
		} else {
			if(currentSpeed.z > 1) {
				currentSpeed.z -= FRICTION_POWER;
				currentRotation.z -= MAX_ROTATION_SPEED / 100;
			} else if(currentSpeed.z < -1) {
				currentSpeed.z += FRICTION_POWER;
				currentRotation.z += MAX_ROTATION_SPEED / 100;
			} else {
				currentSpeed.z = 0;
				currentRotation.z = 0;
			}
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

	public Vector3f getGazeVector() {
		return gazeVector;
	}

	public Vector3f getPerpVector() {
		return perpVector;
	}

	public Vector3f getUpVector() {
		return upVector;
	}
}

/*
 * package entities; import java.util.List; import org.lwjgl.input.Keyboard;
 * import org.lwjgl.util.vector.Vector3f; import models.TexturedModel; import
 * renderEngine.DisplayManager; import terrains.Terrain; public class Player
 * extends Entity { private static final float MOVE_SPEED = 100; public static
 * final float TURN_SPEED = 60; public static final float GRAVITY = -70; private
 * static final float JUMP_POWER = 50; private float currentSpeed = 0; private
 * float currentTurnSpeed = 0; private Vector3f currentRotation = new
 * Vector3f(0, 0, 0); private float upwardsSpeed = 0; private boolean airborne =
 * false; public Player(final TexturedModel model, int i, final Vector3f
 * position, final float rotX, final float rotY, final float rotZ, final float
 * scale) { super(model, i, position, rotX, rotY, rotZ, scale); } public void
 * move(final List<Terrain> terrains) { Terrain terrain = null; for(final
 * Terrain t : terrains) { if((t.getX() < super.getPosition().x) && ((t.getX() +
 * t.getSize()) > super.getPosition().x) && (t.getZ() < super.getPosition().z)
 * && ((t.getZ() + t.getSize()) > super.getPosition().z)) { terrain = t; break;
 * } } catchEvents(); super.increaseRotation(currentRotation.x,
 * getCurrentTurnSpeed() * DisplayManager.getFrameTimeSeconds(),
 * currentRotation.z); final float distance = currentSpeed *
 * DisplayManager.getFrameTimeSeconds(); final float dx = (float) (distance *
 * Math.sin(Math.toRadians(super.getRotY()))); final float dz = (float)
 * (distance * Math.cos(Math.toRadians(super.getRotY())));
 * super.increasePosition(dx, 0, dz); upwardsSpeed += Player.GRAVITY *
 * DisplayManager.getFrameTimeSeconds(); super.increasePosition(0, upwardsSpeed
 * * DisplayManager.getFrameTimeSeconds(), 0); if(terrain != null) { final float
 * terrainHeight = terrain.getHeightOfTerrain(getPosition().x, getPosition().z);
 * // != 0 is teleportation upon terrain change bug fix
 * if((super.getPosition().y < terrainHeight + 2) && (terrainHeight != 0)) {
 * airborne = false; upwardsSpeed = 0; super.getPosition().y = terrainHeight +
 * 2; } } else { if(super.getPosition().y < 0) { airborne = false; upwardsSpeed
 * = 0; super.getPosition().y = 0; } } } private void jump() { if(!airborne) {
 * upwardsSpeed = Player.JUMP_POWER; airborne = true; } } private void
 * catchEvents() { if(Keyboard.isKeyDown(Keyboard.KEY_W)) { currentSpeed =
 * Player.MOVE_SPEED; currentRotation.z = 5; } else
 * if(Keyboard.isKeyDown(Keyboard.KEY_S)) { currentSpeed = -Player.MOVE_SPEED;
 * currentRotation.z = -5; } else { if(currentSpeed > 1) { currentSpeed -=
 * Player.MOVE_SPEED / 100.0f; currentRotation.z -= 5.0f / 100; } else
 * if(currentSpeed < -1) { currentSpeed += Player.MOVE_SPEED / 100.0f;
 * currentRotation.z += 5.0f / 100; } else { currentSpeed = 0; currentRotation.z
 * = 0; } } if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
 * setCurrentTurnSpeed(Player.TURN_SPEED); } else
 * if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
 * setCurrentTurnSpeed(-Player.TURN_SPEED); } else { setCurrentTurnSpeed(0); }
 * if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)) { jump(); } } public float
 * getCurrentTurnSpeed() { return currentTurnSpeed; } public void
 * setCurrentTurnSpeed(float currentTurnSpeed) { this.currentTurnSpeed =
 * currentTurnSpeed; } }
 */