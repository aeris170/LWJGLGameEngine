package water;

public class WaterTile {

	public static final float TILE_SIZE = 400;

	private float height;
	private float x, z;
	private boolean shouldLavaRise = false;

	public WaterTile(final float centerX, final float centerZ, final float height) {
		x = centerX;
		z = centerZ;
		this.height = height;
	}

	public float shouldLavaRise() {
		return height;
	}

	public float getX() {
		return x;
	}

	public float getZ() {
		return z;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public float getHeight() {
		return height;
	}

	public void move() {
		if(shouldLavaRise) {
			setHeight(getHeight() + 0.1f);
			if(getHeight() > -30) {
				shouldLavaRise = false;
			}
		} else {
			setHeight(getHeight() - 0.05f);
			if(getHeight() < -60) {
				shouldLavaRise = true;
			}
		}
	}
}
