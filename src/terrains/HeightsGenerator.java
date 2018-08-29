package terrains;

import java.util.Random;

public class HeightsGenerator {

	private static final float AMPLITUDE = 50f;
	private static final int OCTAVES = 4;
	private static final float ROUGHNESS = 0.4f;

	private Random random = new Random();
	private int seed;
	private int xOffset = 0;
	private int zOffset = 0;

	public HeightsGenerator() {
		seed = random.nextInt(1000000000);
	}

	// only works with POSITIVE gridX and gridZ values!
	public HeightsGenerator(final int gridX, final int gridZ, final int vertexCount, final int seed) {
		this.seed = seed;
		xOffset = gridX * (vertexCount - 1);
		zOffset = gridZ * (vertexCount - 1);
	}

	public float generateHeight(final int x, final int z) {
		float total = 0;
		final float d = (float) Math.pow(2, HeightsGenerator.OCTAVES - 1);
		for(int i = 0; i < HeightsGenerator.OCTAVES; i++) {
			final float freq = (float) (Math.pow(2, i) / d);
			final float amp = (float) Math.pow(HeightsGenerator.ROUGHNESS, i) * HeightsGenerator.AMPLITUDE;
			total += getInterpolatedNoise((x + xOffset) * freq, (z + zOffset) * freq) * amp;
		}
		return total;
	}

	private float getInterpolatedNoise(final float x, final float z) {
		final int intX = (int) x;
		final int intZ = (int) z;
		final float fracX = x - intX;
		final float fracZ = z - intZ;

		final float v1 = getSmoothNoise(intX, intZ);
		final float v2 = getSmoothNoise(intX + 1, intZ);
		final float v3 = getSmoothNoise(intX, intZ + 1);
		final float v4 = getSmoothNoise(intX + 1, intZ + 1);
		final float i1 = interpolate(v1, v2, fracX);
		final float i2 = interpolate(v3, v4, fracX);
		return interpolate(i1, i2, fracZ);
	}

	private float interpolate(final float a, final float b, final float blend) {
		final double theta = blend * Math.PI;
		final float f = (float) (1f - Math.cos(theta)) * 0.5f;
		return (a * (1f - f)) + (b * f);
	}

	private float getSmoothNoise(final int x, final int z) {
		final float corners = (getNoise(x - 1, z - 1) + getNoise(x + 1, z - 1) + getNoise(x - 1, z + 1)
				+ getNoise(x + 1, z + 1)) / 16f;
		final float sides = (getNoise(x - 1, z) + getNoise(x + 1, z) + getNoise(x, z - 1)
				+ getNoise(x, z + 1)) / 8f;
		final float center = getNoise(x, z) / 4f;
		return corners + sides + center;
	}

	private float getNoise(final int x, final int z) {
		random.setSeed((x * 49632) + (z * 325176) + seed);
		return (random.nextFloat() * 2f) - 1f;
	}

}
