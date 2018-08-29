package toolbox;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import terrains.Terrain;

public class MousePicker {

	private static final int RECURSION_COUNT = 200;
	private static final float RAY_RANGE = 600;

	private Vector3f currentRay = new Vector3f();

	private Matrix4f projectionMatrix;
	private Matrix4f viewMatrix;
	private Camera camera;

	private Terrain terrain;
	private Vector3f currentTerrainPoint;

	public MousePicker(final Camera cam, final Matrix4f projection, final Terrain terrain) {
		camera = cam;
		projectionMatrix = projection;
		viewMatrix = Maths.createViewMatrix(camera);
		this.terrain = terrain;
	}

	public Vector3f getCurrentTerrainPoint() {
		return currentTerrainPoint;
	}

	public Vector3f getCurrentRay() {
		return currentRay;
	}

	public void update() {
		viewMatrix = Maths.createViewMatrix(camera);
		currentRay = calculateMouseRay();
		if(intersectionInRange(0, MousePicker.RAY_RANGE, currentRay)) {
			currentTerrainPoint = binarySearch(0, 0, MousePicker.RAY_RANGE, currentRay);
		} else {
			currentTerrainPoint = null;
		}
	}

	private Vector3f calculateMouseRay() {
		final float mouseX = Mouse.getX();
		final float mouseY = Mouse.getY();
		final Vector2f normalizedCoords = getNormalisedDeviceCoordinates(mouseX, mouseY);
		final Vector4f clipCoords = new Vector4f(normalizedCoords.x, normalizedCoords.y, -1.0f, 1.0f);
		final Vector4f eyeCoords = toEyeCoords(clipCoords);
		final Vector3f worldRay = toWorldCoords(eyeCoords);
		return worldRay;
	}

	private Vector3f toWorldCoords(final Vector4f eyeCoords) {
		final Matrix4f invertedView = Matrix4f.invert(viewMatrix, null);
		final Vector4f rayWorld = Matrix4f.transform(invertedView, eyeCoords, null);
		final Vector3f mouseRay = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
		mouseRay.normalise();
		return mouseRay;
	}

	private Vector4f toEyeCoords(final Vector4f clipCoords) {
		final Matrix4f invertedProjection = Matrix4f.invert(projectionMatrix, null);
		final Vector4f eyeCoords = Matrix4f.transform(invertedProjection, clipCoords, null);
		return new Vector4f(eyeCoords.x, eyeCoords.y, -1f, 0f);
	}

	private Vector2f getNormalisedDeviceCoordinates(final float mouseX, final float mouseY) {
		final float x = ((2.0f * mouseX) / Display.getWidth()) - 1f;
		final float y = ((2.0f * mouseY) / Display.getHeight()) - 1f;
		return new Vector2f(x, y);
	}

	// **********************************************************

	private Vector3f getPointOnRay(final Vector3f ray, final float distance) {
		final Vector3f camPos = camera.getPosition();
		final Vector3f start = new Vector3f(camPos.x, camPos.y, camPos.z);
		final Vector3f scaledRay = new Vector3f(ray.x * distance, ray.y * distance, ray.z * distance);
		return Vector3f.add(start, scaledRay, null);
	}

	private Vector3f binarySearch(final int count, final float start, final float finish, final Vector3f ray) {
		final float half = start + ((finish - start) / 2f);
		if(count >= MousePicker.RECURSION_COUNT) {
			final Vector3f endPoint = getPointOnRay(ray, half);
			final Terrain terrain = getTerrain(endPoint.getX(), endPoint.getZ());
			if(terrain != null) {
				return endPoint;
			}
			return null;
		}
		if(intersectionInRange(start, half, ray)) {
			return binarySearch(count + 1, start, half, ray);
		}
		return binarySearch(count + 1, half, finish, ray);
	}

	private boolean intersectionInRange(final float start, final float finish, final Vector3f ray) {
		final Vector3f startPoint = getPointOnRay(ray, start);
		final Vector3f endPoint = getPointOnRay(ray, finish);
		if(!isUnderGround(startPoint) && isUnderGround(endPoint)) {
			return true;
		}
		return false;
	}

	private boolean isUnderGround(final Vector3f testPoint) {
		final Terrain terrain = getTerrain(testPoint.getX(), testPoint.getZ());
		float height = 0;
		if(terrain != null) {
			height = terrain.getHeightOfTerrain(testPoint.getX(), testPoint.getZ());
		}
		if(testPoint.y < height) {
			return true;
		}
		return false;
	}

	private Terrain getTerrain(final float worldX, final float worldZ) {
		return terrain;
	}

}