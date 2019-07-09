package water;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Light;
import shaders.ShaderProgram;
import toolbox.Maths;

public class WaterShader extends ShaderProgram {

	private final static String VERTEX_FILE = "/water/waterVertex.txt";
	private final static String FRAGMENT_FILE = "/water/waterFragment.txt";

	private int location_modelMatrix;
	private int location_viewMatrix;
	private int location_projectionMatrix;
	private int location_reflectionTexture;
	private int location_refractionTexture;
	private int location_dudvMap;
	private int location_moveFactor;
	private int location_cameraPosition;
	private int location_normalMap;
	private int location_lightPosition;
	private int location_lightColor;
	private int location_depthMap;
	private int location_skyColorDay;
	private int location_skyColorNight;
	private int location_blendFactor;

	public WaterShader() {
		super(WaterShader.VERTEX_FILE, WaterShader.FRAGMENT_FILE);
	}

	@Override
	protected void bindAttributes() {
		bindAttribute(0, "position");
	}

	@Override
	protected void getAllUniformLocations() {
		location_projectionMatrix = getUniformLocation("projectionMatrix");
		location_viewMatrix = getUniformLocation("viewMatrix");
		location_modelMatrix = getUniformLocation("modelMatrix");
		location_reflectionTexture = getUniformLocation("reflectionTexture");
		location_refractionTexture = getUniformLocation("refractionTexture");
		location_dudvMap = getUniformLocation("dudvMap");
		location_moveFactor = getUniformLocation("moveFactor");
		location_cameraPosition = getUniformLocation("cameraPosition");
		location_normalMap = getUniformLocation("normalMap");
		location_lightPosition = getUniformLocation("lightPosition");
		location_lightColor = getUniformLocation("lightColor");
		location_depthMap = getUniformLocation("depthMap");
		location_skyColorDay = super.getUniformLocation("skyColorDay");
		location_skyColorNight = super.getUniformLocation("skyColorNight");
		location_blendFactor = super.getUniformLocation("blendFactor");
	}

	public void connectTextureUnits() {
		super.loadInt(location_reflectionTexture, 0);
		super.loadInt(location_refractionTexture, 1);
		super.loadInt(location_dudvMap, 2);
		super.loadInt(location_normalMap, 3);
		super.loadInt(location_depthMap, 4);
	}

	public void loadProjectionMatrix(final Matrix4f projection) {
		super.loadMatrix(location_projectionMatrix, projection);
	}

	public void loadViewMatrix(final Camera camera) {
		final Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		super.loadMatrix(location_viewMatrix, viewMatrix);
		super.loadVector(location_cameraPosition, camera.getPosition());
	}

	public void loadModelMatrix(final Matrix4f modelMatrix) {
		super.loadMatrix(location_modelMatrix, modelMatrix);
	}

	public void loadMoveFactor(final float moveFactor) {
		super.loadFloat(location_moveFactor, moveFactor);
	}

	public void loadLight(final Light light) {
		super.loadVector(location_lightPosition, light.getPosition());
		super.loadVector(location_lightColor, light.getColor());
	}

	public void loadSkyColors(final Vector3f dayColor, final Vector3f nightColor) {
		super.loadVector(location_skyColorDay, dayColor);
		super.loadVector(location_skyColorNight, nightColor);
	}

	public void loadBlendFactor(final float blendFactor) {
		super.loadFloat(location_blendFactor, blendFactor);
	}
}
