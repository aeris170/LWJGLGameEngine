package entities;

import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import toolbox.Maths;

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

	private Vector3f gaze = new Vector3f(0, 0, 1);

	public Camera(final Player player2) {
		this.player = player2;
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
			gaze = Maths.rotateAroundY(gaze, -angleChange);
		} else {
			if(Mouse.isButtonDown(0)) {
				angleAroundPlayer -= angleChange;
			}
		}
	}

	public void invertPitch() {
		pitch *= -1;
	}

	public Vector3f getGazeVector() {
		return gaze;
	}
}
