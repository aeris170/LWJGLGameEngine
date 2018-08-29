package particles;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Player;
import renderEngine.DisplayManager;

public class Particle {

	private Vector3f position;
	private Vector3f velocity;
	private float gravityEffect;
	private float lifeLength;
	private float rotation;
	private float scale;

	private ParticleTexture texture;

	private Vector2f textureOffset1 = new Vector2f();
	private Vector2f textureOffset2 = new Vector2f();
	private float blend;

	private float elapsedTime = 0;
	private float distance;

	private Vector3f reuseVector = new Vector3f();

	public Particle(final ParticleTexture texture, final Vector3f position, final Vector3f velocity, final float gravityEffect, final float lifeLength, final float rotation,
			final float scale) {
		super();
		this.texture = texture;
		this.position = position;
		this.velocity = velocity;
		this.gravityEffect = gravityEffect;
		this.lifeLength = lifeLength;
		this.rotation = rotation;
		this.scale = scale;
		ParticleMaster.addParticle(this);
	}

	public float getDistance() {
		return distance;
	}

	public Vector2f getTextureOffset1() {
		return textureOffset1;
	}

	public Vector2f getTextureOffset2() {
		return textureOffset2;
	}

	public float getBlend() {
		return blend;
	}

	public ParticleTexture getTexture() {
		return texture;
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getRotation() {
		return rotation;
	}

	public float getScale() {
		return scale;
	}

	protected boolean update(final Camera camera) {
		velocity.y += Player.GRAVITY * gravityEffect * DisplayManager.getFrameTimeSeconds();
		reuseVector.set(velocity);
		reuseVector.scale(DisplayManager.getFrameTimeSeconds());
		Vector3f.add(reuseVector, position, position);
		updateTextureCoordInfo();
		distance = Vector3f.sub(camera.getPosition(), position, null).lengthSquared();
		elapsedTime += DisplayManager.getFrameTimeSeconds();
		return elapsedTime < lifeLength;
	}

	private void updateTextureCoordInfo() {
		final int stageCount = (int) Math.pow(texture.getNumberOfRows(), 2);
		final float atlasProgression = (elapsedTime / lifeLength) * stageCount;
		final int index1 = (int) Math.floor(atlasProgression);
		final int index2 = index1 < (stageCount - 1) ? index1 + 1 : index1;
		blend = atlasProgression % 1;
		setTextureOffset(textureOffset1, index1);
		setTextureOffset(textureOffset2, index2);
	}

	private void setTextureOffset(final Vector2f offset, final int index) {
		final int numberOfRows = texture.getNumberOfRows();
		final int column = index % numberOfRows;
		final int row = index / numberOfRows;
		offset.x = (float) column / numberOfRows;
		offset.y = (float) row / numberOfRows;
	}
}
