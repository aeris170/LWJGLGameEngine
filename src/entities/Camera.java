package entities;

import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

public class Camera {

	public static Camera Instance;

	private float distanceToPlayer = 50;
	private float angleAroundPlayer = 0;

	private Vector3f position = new Vector3f(0, 0, 0);
	private float pitch = 20;
	private float yaw = 0;
	private float roll;
	private boolean isMouseLookEnabled = true;

	private Player player;

	public Camera(final Player player) {
		this.player = player;
		Instance = this;
	}

	public void move() {
		calculateZoom();
		calculatePitch();
		calculateAngleAroundPlayer();
		final float horizontalDistance = calculateHorizontalDistance();
		final float verticalDistance = calculateVerticalDistance();
		calculateCameraPosition(horizontalDistance, verticalDistance);
		// Order of operations below, BEWARE!!!!
		yaw = 180 - (player.getRotY() + angleAroundPlayer);
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public float getRoll() {
		return roll;
	}

	private void calculateCameraPosition(final float horizontalDistance, final float verticalDistance) {
		final float theta = player.getRotY() + angleAroundPlayer;
		final float xOffset = (float) (horizontalDistance * Math.sin(Math.toRadians(theta)));
		final float zOffset = (float) (horizontalDistance * Math.cos(Math.toRadians(theta)));
		position.x = player.getPosition().x - xOffset;
		position.y = player.getPosition().y + verticalDistance;
		position.z = player.getPosition().z - zOffset;
	}

	private float calculateHorizontalDistance() {
		if(isMouseLookEnabled) {
			return 12;
		}
		return (float) (distanceToPlayer * Math.cos(Math.toRadians(pitch)));
	}

	private float calculateVerticalDistance() {
		if(isMouseLookEnabled) {
			return 12;
		}
		return (float) (distanceToPlayer * Math.sin(Math.toRadians(pitch)));
	}

	private void calculateZoom() {
		final float zoomLevel = Mouse.getDWheel() * 0.05f;
		distanceToPlayer -= zoomLevel;
	}

	private void calculatePitch() {
		final float pitchChange = Mouse.getDY() * 0.1f;
		float newPitch = pitch - pitchChange;
		if(isMouseLookEnabled) {
			if(newPitch <= 90 && newPitch >= -90) {
				pitch = newPitch;
			}
		} else {
			if(Mouse.isButtonDown(1)) {
				pitch = newPitch;
			}
		}
	}

	private void calculateAngleAroundPlayer() {
		final float angleChange = Mouse.getDX() * 0.1f;
		if(isMouseLookEnabled) {
			angleAroundPlayer -= angleChange;
			if(angleChange != 0) {
				player.increaseRotation(0, angleAroundPlayer, 0);
			}
			angleAroundPlayer = 0;
		} else {
			if(Mouse.isButtonDown(0)) {
				angleAroundPlayer -= angleChange;
			}
		}
	}

	public void invertPitch() {
		pitch *= -1;
	}
}
