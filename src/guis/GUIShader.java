package guis;

import org.lwjgl.util.vector.Matrix4f;

import shaders.ShaderProgram;

public class GUIShader extends ShaderProgram {

	private static final String VERTEX_FILE = "/guis/guiVertexShader.txt";
	private static final String FRAGMENT_FILE = "/guis/guiFragmentShader.txt";

	private int location_transformationMatrix;

	public GUIShader() {
		super(GUIShader.VERTEX_FILE, GUIShader.FRAGMENT_FILE);
	}

	public void loadTransformation(final Matrix4f matrix) {
		super.loadMatrix(location_transformationMatrix, matrix);
	}

	@Override
	protected void getAllUniformLocations() {
		location_transformationMatrix = super.getUniformLocation("transformationMatrix");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
	}
}