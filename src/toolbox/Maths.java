package toolbox;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;

public class Maths {

	public static float barryCentric(final Vector3f p1, final Vector3f p2, final Vector3f p3, final Vector2f pos) {
		final float det = ((p2.z - p3.z) * (p1.x - p3.x)) + ((p3.x - p2.x) * (p1.z - p3.z));
		final float l1 = (((p2.z - p3.z) * (pos.x - p3.x)) + ((p3.x - p2.x) * (pos.y - p3.z))) / det;
		final float l2 = (((p3.z - p1.z) * (pos.x - p3.x)) + ((p1.x - p3.x) * (pos.y - p3.z))) / det;
		final float l3 = 1.0f - l1 - l2;
		return (l1 * p1.y) + (l2 * p2.y) + (l3 * p3.y);
	}

	public static Matrix4f createTransformationMatrix(final Vector2f translation, final Vector2f scale) {
		final Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.scale(new Vector3f(scale.x, scale.y, 1f), matrix, matrix);
		return matrix;
	}

	public static Matrix4f createTransformationMatrix(final Vector3f translation, final float rx, final float ry, final float rz, final float scale) {
		final Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rx), new Vector3f(1, 0, 0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(ry), new Vector3f(0, 1, 0), matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rz), new Vector3f(0, 0, 1), matrix, matrix);
		Matrix4f.scale(new Vector3f(scale, scale, scale), matrix, matrix);
		return matrix;
	}

	public static Matrix4f createTransformationMatrix(final Vector3f translation, final float rx, final float ry, final float rz, final float scale, Camera camera) {
		final Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f rotation = new Matrix4f();
		rotation.setZero();
		if(Math.cos(camera.getYaw() * (Math.PI / 180)) >= 0.0f) {
			rotation.m00 = camera.getPitch();
			rotation.m01 = (float) (Math.cos(camera.getYaw() * (Math.PI / 180)));
			rotation.m11 = (float) (-Math.sin(camera.getYaw() * (Math.PI / 180)));
			rotation.m22 = 1.0f;
			rotation.m33 = 1.0f;
		} else {
			rotation.m00 = camera.getPitch();
			rotation.m01 = (float) (-Math.cos(camera.getYaw() * (Math.PI / 180)));
			rotation.m11 = (float) (Math.sin(camera.getYaw() * (Math.PI / 180)));
			rotation.m22 = 1.0f;
			rotation.m33 = 1.0f;
		}
		Matrix4f.mul(rotation, matrix, matrix);
		Matrix4f.scale(new Vector3f(scale, scale, scale), matrix, matrix);
		return matrix;
	}

	public static Matrix4f createTransformationMatrix(Vector3f translation, Quaternion rotation, float scale) {
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);
		Matrix4f.mul(matrix, convertQuaternionToMatrix4f(rotation), matrix);
		Matrix4f.scale(new Vector3f(scale, scale, scale), matrix, matrix);
		return matrix;
	}

	public static Matrix4f createViewMatrix(final Camera camera) {
		final Matrix4f viewMatrix = new Matrix4f();
		viewMatrix.setIdentity();
		Matrix4f.rotate((float) Math.toRadians(camera.getPitch()), new Vector3f(1, 0, 0), viewMatrix, viewMatrix);
		Matrix4f.rotate((float) Math.toRadians(camera.getYaw()), new Vector3f(0, 1, 0), viewMatrix, viewMatrix);
		final Vector3f cameraPos = camera.getPosition();
		final Vector3f negativeCameraPos = new Vector3f(-cameraPos.x, -cameraPos.y, -cameraPos.z);
		Matrix4f.translate(negativeCameraPos, viewMatrix, viewMatrix);
		return viewMatrix;
	}

	private static Matrix4f convertQuaternionToMatrix4f(Quaternion q) {
		Matrix4f matrix = new Matrix4f();

		matrix.m00 = 1.0f - 2.0f * (q.getY() * q.getY() + q.getZ() * q.getZ());
		matrix.m01 = 2.0f * (q.getX() * q.getY() + q.getZ() * q.getW());
		matrix.m02 = 2.0f * (q.getX() * q.getZ() - q.getY() * q.getW());
		matrix.m03 = 0.0f;

		// Second row
		matrix.m10 = 2.0f * (q.getX() * q.getY() - q.getZ() * q.getW());
		matrix.m11 = 1.0f - 2.0f * (q.getX() * q.getX() + q.getZ() * q.getZ());
		matrix.m12 = 2.0f * (q.getZ() * q.getY() + q.getX() * q.getW());
		matrix.m13 = 0.0f;

		// Third row
		matrix.m20 = 2.0f * (q.getX() * q.getZ() + q.getY() * q.getW());
		matrix.m21 = 2.0f * (q.getY() * q.getZ() - q.getX() * q.getW());
		matrix.m22 = 1.0f - 2.0f * (q.getX() * q.getX() + q.getY() * q.getY());
		matrix.m23 = 0.0f;

		// Fourth row
		matrix.m30 = 0;
		matrix.m31 = 0;
		matrix.m32 = 0;
		matrix.m33 = 1.0f;

		return matrix;
	}

	public static Quaternion convertMatrix4fToQuaternion(Matrix4f matrix) {
		Quaternion quat = new Quaternion();
		quat.setFromMatrix(matrix);
		return quat;

	}
}