package skybox;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import renderEngine.DisplayManager;
import shaders.ShaderProgram;
import toolbox.Maths;

public class SkyboxShader extends ShaderProgram {

	private static final String VERTEX_FILE = "/skybox/skyboxVertexShader.txt";
	private static final String FRAGMENT_FILE = "/skybox/skyboxFragmentShader.txt";

	private static final float ROTATE_SPEED = 0.5f;

	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int location_day;
	private int location_night;
	private int location_dayFogColor;
	private int location_nightFogColor;
	private int location_blendFactor;

	private float rotation = 0;

	public SkyboxShader() {
		super(SkyboxShader.VERTEX_FILE, SkyboxShader.FRAGMENT_FILE);
	}

	public void loadProjectionMatrix(final Matrix4f matrix) {
		super.loadMatrix(location_projectionMatrix, matrix);
	}

	public void loadViewMatrix(final Camera camera) {
		final Matrix4f matrix = Maths.createViewMatrix(camera);
		matrix.m30 = 0;
		matrix.m31 = 0;
		matrix.m32 = 0;
		rotation += SkyboxShader.ROTATE_SPEED * DisplayManager.getFrameTimeSeconds();
		Matrix4f.rotate((float) Math.toRadians(rotation), new Vector3f(0, 1, 0), matrix, matrix);
		super.loadMatrix(location_viewMatrix, matrix);
	}

	public void connectTextureUnits() {
		super.loadInt(location_day, 0);
		super.loadInt(location_night, 1);
	}

	public void loadBlendFactor(final float blend) {
		super.loadFloat(location_blendFactor, blend);
	}

	public void loadFogColors(final Vector3f dayFogColor, final Vector3f nightFogColor) {
		super.loadVector(location_dayFogColor, dayFogColor);
		super.loadVector(location_nightFogColor, nightFogColor);
	}

	@Override
	protected void getAllUniformLocations() {
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
		location_day = super.getUniformLocation("day");
		location_night = super.getUniformLocation("night");
		location_dayFogColor = super.getUniformLocation("dayFogColor");
		location_nightFogColor = super.getUniformLocation("nightFogColor");
		location_blendFactor = super.getUniformLocation("blendFactor");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
	}
}