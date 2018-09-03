package entities;

import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

import models.TexturedModel;

public class Entity {

	private TexturedModel model;
	private Vector3f position;
	private float rotX, rotY, rotZ;
	private float scale;

	private int textureIndex = 0;

	public Entity(final TexturedModel model, final Vector3f position, final float rotX, final float rotY,
			final float rotZ, final float scale) {
		this.model = model;
		this.position = position;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.scale = scale;
	}

	public Entity(final TexturedModel model, final int textureIndex, final Vector3f position, final float rotX,
			final float rotY, final float rotZ, final float scale) {
		this.model = model;
		this.position = position;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.scale = scale;
		this.textureIndex = textureIndex;
	}

	public float getTextureXOffset() {
		final int column = textureIndex % model.getTexture().getNumberOfRows();
		return (float) column / (float) model.getTexture().getNumberOfRows();
	}

	public float getTextureYOffset() {
		final int row = textureIndex / model.getTexture().getNumberOfRows();
		return (float) row / (float) model.getTexture().getNumberOfRows();
	}

	public void increasePosition(final float dx, final float dy, final float dz) {
		position.x += dx;
		position.y += dy;
		position.z += dz;
	}

	public void increaseRotation(final float dx, final float dy, final float dz) {
		rotX += dx;
		rotX %= 360;
		rotY += dy;
		rotY %= 360;
		rotZ += dz;
		rotZ %= 360;
	}

	public TexturedModel getModel() {
		return model;
	}

	public void setModel(final TexturedModel model) {
		this.model = model;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(final Vector3f position) {
		this.position = position;
	}

	public float getRotX() {
		return rotX;
	}

	public void setRotX(final float rotX) {
		this.rotX = rotX;
	}

	public float getRotY() {
		return rotY;
	}

	public void setRotY(final float rotY) {
		this.rotY = rotY;
	}

	public float getRotZ() {
		return rotZ;
	}

	public void setRotZ(final float rotZ) {
		this.rotZ = rotZ;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(final float scale) {
		this.scale = scale;
	}

	public int getTextureIndex() {
		return textureIndex;
	}

	public void setTextureIndex(int textureIndex) {
		this.textureIndex = textureIndex;
	}

	/**
	 * @param rotX rotation in x axis in degrees
	 * @param rotY rotation in y axis in degrees
	 * @param rotZ rotation in z axis in degrees
	 * @return
	 */
	public static Quaternion getRotationQuat(float rotX, float rotY, float rotZ) {
		float attitude = (float) Math.toRadians(rotX);
		float heading = (float) Math.toRadians(rotY);
		float bank = (float) Math.toRadians(rotZ);

		float c1 = (float) Math.cos(heading);
		float s1 = (float) Math.sin(heading);

		float c2 = (float) Math.cos(attitude);
		float s2 = (float) Math.sin(attitude);

		float c3 = (float) Math.cos(bank);
		float s3 = (float) Math.sin(bank);

		float w = (float) (Math.sqrt(1.0 + c1 * c2 + c1 * c3 - s1 * s2 * s3 + c2 * c3) / 2.0);
		float w4 = (4.0f * w);
		float x = (c2 * s3 + c1 * s3 + s1 * s2 * c3) / w4;
		float y = (s1 * c2 + s1 * c3 + c1 * s2 * s3) / w4;
		float z = (-s1 * s3 + c1 * s2 * c3 + s2) / w4;

		return new Quaternion(x, y, z, w);
	}

	public static Quaternion eulerToQuaternion(float pitch, float yaw, float roll) {
		// pitch x
		// yaw y
		// roll z
		/*
		 * eulerX = (float) Math.toRadians(eulerX); eulerY = (float)
		 * Math.toRadians(eulerY); eulerZ = (float) Math.toRadians(eulerZ);
		 * double sx = Math.sin(eulerX / 2); double sy = Math.sin(eulerY / 2);
		 * double sz = Math.sin(eulerZ / 2); double cx = Math.cos(eulerX / 2);
		 * double cy = Math.cos(eulerY / 2); double cz = Math.cos(eulerZ / 2);
		 * double cycz = cy * cz; double sysz = sy * sz; double d = cycz * cx -
		 * sysz * sx; double a = cycz * sx + sysz * cx; double b = sy * cz * cx
		 * + cy * sz * sx; double c = cy * sz * cx - sy * cz * sx; Quaternion q
		 * = new Quaternion((float) a, (float) b, (float) c, (float) d);
		 * q.normalise(); return(q);
		 */

		Quaternion q = new Quaternion();
		// Abbreviations for the various angular functions
		double cy = Math.cos(yaw * 0.5);
		double sy = Math.sin(yaw * 0.5);
		double cr = Math.cos(roll * 0.5);
		double sr = Math.sin(roll * 0.5);
		double cp = Math.cos(pitch * 0.5);
		double sp = Math.sin(pitch * 0.5);

		q.w = (float) (cy * cr * cp + sy * sr * sp);
		q.x = (float) (cy * sr * cp - sy * cr * sp);
		q.y = (float) (cy * cr * sp + sy * sr * cp);
		q.z = (float) (sy * cr * cp - cy * sr * sp);
		return q;
	}
}
