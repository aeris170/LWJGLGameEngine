package particles;

import java.util.Random;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import renderEngine.DisplayManager;

public class ParticleSystem {

	private float pps, averageSpeed, gravityComplient, averageLifeLength, averageScale;

	private float speedError, lifeError, scaleError = 0;
	private boolean randomRotation = false;
	private Vector3f direction;
	private float directionDeviation = 0;

	private ParticleTexture texture;

	private Random random = new Random();

	public ParticleSystem(final ParticleTexture texture, final float pps, final float speed, final float gravityComplient, final float lifeLength, final float scale) {
		this.texture = texture;
		this.pps = pps;
		averageSpeed = speed;
		this.gravityComplient = gravityComplient;
		averageLifeLength = lifeLength;
		averageScale = scale;
	}

	/**
	 * @param direction - The average direction in which particles are emitted.
	 * @param deviation - A value between 0 and 1 indicating how far from the
	 *                  chosen direction particles can deviate.
	 */
	public void setDirection(final Vector3f direction, final float deviation) {
		this.direction = new Vector3f(direction);
		directionDeviation = (float) (deviation * Math.PI);
	}

	public void randomizeRotation() {
		randomRotation = true;
	}

	/**
	 * @param error - A number between 0 and 1, where 0 means no error margin.
	 */
	public void setSpeedError(final float error) {
		speedError = error * averageSpeed;
	}

	/**
	 * @param error - A number between 0 and 1, where 0 means no error margin.
	 */
	public void setLifeError(final float error) {
		lifeError = error * averageLifeLength;
	}

	/**
	 * @param error - A number between 0 and 1, where 0 means no error margin.
	 */
	public void setScaleError(final float error) {
		scaleError = error * averageScale;
	}

	public void generateParticles(final Vector3f systemCenter) {
		final float delta = DisplayManager.getFrameTimeSeconds();
		final float particlesToCreate = pps * delta;
		final int count = (int) Math.floor(particlesToCreate);
		final float partialParticle = particlesToCreate % 1;
		for(int i = 0; i < count; i++) {
			emitParticle(systemCenter);
		}
		if(Math.random() < partialParticle) {
			emitParticle(systemCenter);
		}
	}

	private void emitParticle(final Vector3f center) {
		Vector3f velocity = null;
		if(direction != null) {
			velocity = ParticleSystem.generateRandomUnitVectorWithinCone(direction, directionDeviation);
		} else {
			velocity = generateRandomUnitVector();
		}
		velocity.normalise();
		velocity.scale(generateValue(averageSpeed, speedError));
		final float scale = generateValue(averageScale, scaleError);
		final float lifeLength = generateValue(averageLifeLength, lifeError);
		new Particle(texture, new Vector3f(center), velocity, gravityComplient, lifeLength, generateRotation(), scale);
	}

	private float generateValue(final float average, final float errorMargin) {
		final float offset = (random.nextFloat() - 0.5f) * 2f * errorMargin;
		return average + offset;
	}

	private float generateRotation() {
		if(randomRotation) {
			return random.nextFloat() * 360f;
		} else {
			return 0;
		}
	}

	private static Vector3f generateRandomUnitVectorWithinCone(final Vector3f coneDirection, final float angle) {
		final float cosAngle = (float) Math.cos(angle);
		final Random random = new Random();
		final float theta = (float) (random.nextFloat() * 2f * Math.PI);
		final float z = cosAngle + (random.nextFloat() * (1 - cosAngle));
		final float rootOneMinusZSquared = (float) Math.sqrt(1 - (z * z));
		final float x = (float) (rootOneMinusZSquared * Math.cos(theta));
		final float y = (float) (rootOneMinusZSquared * Math.sin(theta));

		final Vector4f direction = new Vector4f(x, y, z, 1);
		if((coneDirection.x != 0) || (coneDirection.y != 0) || ((coneDirection.z != 1) && (coneDirection.z != -1))) {
			final Vector3f rotateAxis = Vector3f.cross(coneDirection, new Vector3f(0, 0, 1), null);
			rotateAxis.normalise();
			final float rotateAngle = (float) Math.acos(Vector3f.dot(coneDirection, new Vector3f(0, 0, 1)));
			final Matrix4f rotationMatrix = new Matrix4f();
			rotationMatrix.rotate(-rotateAngle, rotateAxis);
			Matrix4f.transform(rotationMatrix, direction, direction);
		} else if(coneDirection.z == -1) {
			direction.z *= -1;
		}
		return new Vector3f(direction);
	}

	private Vector3f generateRandomUnitVector() {
		final float theta = (float) (random.nextFloat() * 2f * Math.PI);
		final float z = (random.nextFloat() * 2) - 1;
		final float rootOneMinusZSquared = (float) Math.sqrt(1 - (z * z));
		final float x = (float) (rootOneMinusZSquared * Math.cos(theta));
		final float y = (float) (rootOneMinusZSquared * Math.sin(theta));
		return new Vector3f(x, y, z);
	}
}