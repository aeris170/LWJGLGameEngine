package particles;

public class ParticleTexture {

	private int textureID;
	private int numberOfRows;
	private boolean additive;

	public ParticleTexture(final int textureID, final int numberOfRows, final boolean additive) {
		super();
		this.textureID = textureID;
		this.numberOfRows = numberOfRows;
		this.additive = additive;
	}

	protected boolean usesAdditiveBlending() {
		return additive;
	}

	protected int getTextureID() {
		return textureID;
	}

	protected int getNumberOfRows() {
		return numberOfRows;
	}
}
