package shaders;

import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Light;
import toolbox.Maths;

public class TerrainShader extends ShaderProgram {

	private static final int MAX_LIGHTS = 4;

	private static final String VERTEX_FILE = "/shaders/terrainVertexShader.txt";
	private static final String FRAGMENT_FILE = "/shaders/terrainFragmentShader.txt";

	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int location_transformationMatrix;
	private int location_lightPositions[];
	private int location_lightColors[];
	private int location_attenuation[];
	private int location_shineDamper;
	private int location_reflectivity;
	private int location_skyColorDay;
	private int location_skyColorNight;
	private int location_blendFactor;
	private int location_backgroundTexture;
	private int location_rTexture;
	private int location_gTexture;
	private int location_bTexture;
	private int location_blendMap;
	private int location_clippingPlane;
	private int location_toShadowMapSpace;
	private int location_shadowMap;
	private int location_grassTexture;
	private int location_mudTexture;

	public TerrainShader() {
		super(TerrainShader.VERTEX_FILE, TerrainShader.FRAGMENT_FILE);
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "textureCoordinates");
		super.bindAttribute(2, "normal");
	}

	@Override
	protected void getAllUniformLocations() {
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
		location_transformationMatrix = super.getUniformLocation("transformationMatrix");

		location_lightPositions = new int[TerrainShader.MAX_LIGHTS];
		location_lightColors = new int[TerrainShader.MAX_LIGHTS];
		location_attenuation = new int[TerrainShader.MAX_LIGHTS];
		for(int i = 0; i < TerrainShader.MAX_LIGHTS; i++) {
			location_lightPositions[i] = super.getUniformLocation("lightPositions[" + i + "]");
			location_lightColors[i] = super.getUniformLocation("lightColors[" + i + "]");
			location_attenuation[i] = super.getUniformLocation("attenuation[" + i + "]");
		}

		location_shineDamper = super.getUniformLocation("shineDamper");
		location_reflectivity = super.getUniformLocation("reflectivity");
		location_skyColorDay = super.getUniformLocation("skyColorDay");
		location_skyColorNight = super.getUniformLocation("skyColorNight");
		location_blendFactor = super.getUniformLocation("blendFactor");
		location_backgroundTexture = super.getUniformLocation("backgroundTexture");
		location_rTexture = super.getUniformLocation("rTexture");
		location_gTexture = super.getUniformLocation("gTexture");
		location_bTexture = super.getUniformLocation("bTexture");
		location_blendMap = super.getUniformLocation("blendMap");
		location_clippingPlane = super.getUniformLocation("clippingPlane");
		location_toShadowMapSpace = super.getUniformLocation("toShadowMapSpace");
		location_shadowMap = super.getUniformLocation("shadowMap");
		location_grassTexture = super.getUniformLocation("grassTexture");
		location_mudTexture = super.getUniformLocation("mudTexture");
	}

	public void connectTextureUnits() {
		super.loadInt(location_backgroundTexture, 0);
		super.loadInt(location_rTexture, 1);
		super.loadInt(location_gTexture, 2);
		super.loadInt(location_bTexture, 3);
		super.loadInt(location_blendMap, 4);
		super.loadInt(location_shadowMap, 5);
		super.loadInt(location_grassTexture, 6);
		super.loadInt(location_mudTexture, 7);
	}

	public void loadProjectionMatrix(final Matrix4f matrix) {
		super.loadMatrix(location_projectionMatrix, matrix);
	}

	public void loadViewMatrix(final Camera camera) {
		final Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		super.loadMatrix(location_viewMatrix, viewMatrix);
	}

	public void loadTransformationMatrix(final Matrix4f matrix) {
		super.loadMatrix(location_transformationMatrix, matrix);
	}

	public void loadLights(final List<Light> lights) {
		for(int i = 0; i < TerrainShader.MAX_LIGHTS; i++) {
			if(i < lights.size()) {
				super.loadVector(location_lightPositions[i], lights.get(i).getPosition());
				super.loadVector(location_lightColors[i], lights.get(i).getColor());
				super.loadVector(location_attenuation[i], lights.get(i).getAttenuation());
			} else {
				super.loadVector(location_lightPositions[i], new Vector3f(0, 0, 0));
				super.loadVector(location_lightColors[i], new Vector3f(0, 0, 0));
				super.loadVector(location_attenuation[i], new Vector3f(1, 0, 0));
			}
		}
	}

	public void loadShineVariables(final float damper, final float reflectivity) {
		super.loadFloat(location_shineDamper, damper);
		super.loadFloat(location_reflectivity, reflectivity);
	}

	public void loadSkyColors(final Vector3f dayColor, final Vector3f nightColor) {
		super.loadVector(location_skyColorDay, dayColor);
		super.loadVector(location_skyColorNight, nightColor);
	}

	public void loadBlendFactor(final float blendFactor) {
		super.loadFloat(location_blendFactor, blendFactor);
	}

	public void loadClippingPlane(final Vector4f clippingPlane) {
		super.loadVector(location_clippingPlane, clippingPlane);
	}

	public void loadToShadowMapSpaceMatrix(final Matrix4f matrix) {
		super.loadMatrix(location_toShadowMapSpace, matrix);
	}
}
