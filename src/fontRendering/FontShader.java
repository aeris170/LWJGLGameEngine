package fontRendering;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import fontMeshCreator.FontFX;
import shaders.ShaderProgram;

public class FontShader extends ShaderProgram {

	private static final String VERTEX_FILE = "/fontRendering/fontVertex.txt";
	private static final String FRAGMENT_FILE = "/fontRendering/fontFragment.txt";

	private int location_color;
	private int location_translation;
	private int location_width;
	private int location_edge;
	private int location_borderWidth;
	private int location_borderEdge;
	private int location_offset;
	private int location_outlineColor;

	public FontShader() {
		super(FontShader.VERTEX_FILE, FontShader.FRAGMENT_FILE);
	}

	@Override
	protected void getAllUniformLocations() {
		location_color = super.getUniformLocation("color");
		location_translation = super.getUniformLocation("translation");
		location_width = super.getUniformLocation("width");
		location_edge = super.getUniformLocation("edge");
		location_borderWidth = super.getUniformLocation("borderWidth");
		location_borderEdge = super.getUniformLocation("borderEdge");
		location_offset = super.getUniformLocation("offset");
		location_outlineColor = super.getUniformLocation("outlineColor");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "textureCoords");
	}

	protected void loadColor(final Vector3f color) {
		super.loadVector(location_color, color);
	}

	protected void loadTransformation(final Vector2f translation) {
		super.load2DVector(location_translation, translation);
	}

	protected void loadFX(final FontFX fontFx) {
		super.loadFloat(location_width, fontFx.getWidth());
		super.loadFloat(location_edge, fontFx.getEdge());
		super.loadFloat(location_borderWidth, fontFx.getBorderWidth());
		super.loadFloat(location_borderEdge, fontFx.getBorderEdge());
		super.load2DVector(location_offset, fontFx.getOffset());
		super.loadVector(location_outlineColor, fontFx.getOutlineColor());
	}
}
