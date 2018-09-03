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

	public static Matrix4f createTransformationMatrix(final Vector3f translation, Vector3f gazeVector, Vector3f perpVector, final Vector3f upVector, final float rx,
			final float ry, final float rz, final float scale, boolean a) {
		Matrix4f matrix = new Matrix4f();
		matrix.setIdentity();
		Matrix4f.translate(translation, matrix, matrix);

		System.out.println("\tGAZE: " + gazeVector);
		System.out.println("\tPERP: " + perpVector);
		System.out.println("\tUP  : " + upVector);
		System.out.println(Vector3f.dot(gazeVector, perpVector));

		if(a) {
			matrix = doRotateSideway(matrix, gazeVector, perpVector, upVector, rx, ry, -rz);
		} else {
			matrix = doRotateForward(matrix, gazeVector, perpVector, upVector, rx, ry, -rz);
		}

		Matrix4f.scale(new Vector3f(scale, scale, scale), matrix, matrix);
		return matrix;
	}

	private static Matrix4f doRotateForward(Matrix4f matrix, Vector3f gazeVector, Vector3f perpVector, final Vector3f upVector, final float rx,
			final float ry, final float rz) {
		Matrix4f.rotate((float) Math.toRadians(rx), perpVector, matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rz), gazeVector, matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(ry), upVector, matrix, matrix);
		return matrix;
	}

	private static Matrix4f doRotateSideway(Matrix4f matrix, Vector3f gazeVector, Vector3f perpVector, final Vector3f upVector, final float rx,
			final float ry, final float rz) {
		Matrix4f.rotate((float) Math.toRadians(rz), gazeVector, matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(rx), perpVector, matrix, matrix);
		Matrix4f.rotate((float) Math.toRadians(ry), upVector, matrix, matrix);
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

	public static Vector3f rotateAroundXY(final Vector3f zVec, final float rx, final float ry) {
		Vector3f v = new Vector3f(zVec);
		float x = v.x;
		float y = v.y;
		float z = v.z;
		float rxRad = (float) Math.toRadians(rx);
		float ryRad = (float) Math.toRadians(ry);
		// rotate around x
		y = (float) (zVec.y * Math.cos(rxRad) - zVec.z * Math.sin(rxRad));
		z = (float) (zVec.y * Math.sin(rxRad) + zVec.z * Math.cos(rxRad));
		float zz = z;
		// rotate around y
		x = (float) (x * Math.cos(ryRad) + z * Math.sin(ryRad));
		zz = (float) (x * -Math.sin(ryRad) + z * Math.cos(ryRad));
		return new Vector3f(x, y, zz);
	}

	public static Vector3f rotateAroundXZ(final Vector3f yVec, final float rx, final float rz) {
		Vector3f v = new Vector3f(yVec);
		float x = v.x;
		float y = v.y;
		float z = v.z;
		float rxRad = (float) Math.toRadians(rx);
		float rzRad = (float) Math.toRadians(rz);
		// rotate around x
		y = (float) (yVec.y * Math.cos(rxRad) - yVec.z * Math.sin(rxRad));
		z = (float) (yVec.y * Math.sin(rxRad) + yVec.z * Math.cos(rxRad));
		float yy = y;
		// rotate around z
		x = (float) (x * Math.cos(rzRad) - y * Math.sin(rzRad));
		yy = (float) (x * Math.sin(rzRad) + y * Math.cos(rzRad));
		return new Vector3f(x, yy, z);
	}

	public static Vector3f rotateAroundYZ(final Vector3f xVec, final float ry, final float rz) {
		Vector3f v = new Vector3f(xVec);
		float x = v.x;
		float y = v.y;
		float z = v.z;
		float ryRad = (float) Math.toRadians(ry);
		float rzRad = (float) Math.toRadians(rz);
		// rotate around x
		x = (float) (xVec.x * Math.cos(ryRad) + xVec.z * Math.sin(ryRad));
		z = (float) (xVec.x * -Math.sin(ryRad) + xVec.z * Math.cos(ryRad));
		float xx = x;
		// rotate around z
		xx = (float) (x * Math.cos(rzRad) - y * Math.sin(rzRad));
		y = (float) (x * Math.sin(rzRad) + y * Math.cos(rzRad));
		return new Vector3f(xx, y, z);
	}

	public static Vector3f rotateAroundX(final Vector3f vec, final float rx) {
		Vector3f v = new Vector3f(vec);
		float x = v.x;
		float y = v.y;
		float z = v.z;
		float rxRad = (float) Math.toRadians(rx);
		// rotate around x
		y = (float) (vec.y * Math.cos(rxRad) - vec.z * Math.sin(rxRad));
		z = (float) (vec.y * Math.sin(rxRad) + vec.z * Math.cos(rxRad));
		return new Vector3f(x, y, z);
	}

	public static Vector3f rotateAroundY(final Vector3f vec, final float ry) {
		Vector3f v = new Vector3f(vec);
		float x = v.x;
		float y = v.y;
		float z = v.z;
		float ryRad = (float) Math.toRadians(ry);
		// rotate around y
		x = (float) (vec.x * Math.cos(ryRad) + vec.z * Math.sin(ryRad));
		z = (float) (vec.x * -Math.sin(ryRad) + vec.z * Math.cos(ryRad));
		return new Vector3f(x, y, z);
	}

	private static Vector3f rotateAroundZ(Vector3f vec, float rz) {
		Vector3f v = new Vector3f(vec);
		float x = v.x;
		float y = v.y;
		float z = v.z;
		float rzRad = (float) Math.toRadians(rz);
		// rotate around z
		x = (float) (vec.x * Math.cos(rzRad) - vec.y * Math.sin(rzRad));
		y = (float) (vec.x * Math.sin(rzRad) + vec.y * Math.cos(rzRad));
		return new Vector3f(x, y, z);
	}
}