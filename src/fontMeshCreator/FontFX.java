package fontMeshCreator;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class FontFX {

	private float width = 0.5f;
	private float edge = 0.1f;
	private float borderWidth = 0.3f;
	private float borderEdge = 0.4f;
	private Vector2f offset = new Vector2f(0.0f, 0.0f);
	private Vector3f outlineColor = new Vector3f(1.0f, 0.0f, 0.0f);

	public FontFX() {}

	public FontFX(final float width, final float edge, final float borderWidth, final float borderEdge, final Vector2f offset, final Vector3f outlineColor) {
		super();
		this.width = width;
		this.edge = edge;
		this.borderWidth = borderWidth;
		this.borderEdge = borderEdge;
		this.offset = offset;
		this.outlineColor = outlineColor;
	}

	public float getWidth() {
		return width;
	}

	public float getEdge() {
		return edge;
	}

	public float getBorderWidth() {
		return borderWidth;
	}

	public float getBorderEdge() {
		return borderEdge;
	}

	public Vector2f getOffset() {
		return offset;
	}

	public Vector3f getOutlineColor() {
		return outlineColor;
	}

	public void setOutlineColor(final Vector3f outlineColor) {
		this.outlineColor = outlineColor;
	}
}
