package textures;

import java.nio.ByteBuffer;

public class TextureData {

	private int width;
	private int height;
	private ByteBuffer buffer;

	public TextureData(final ByteBuffer buffer, final int width, final int height) {
		this.buffer = buffer;
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}
}
