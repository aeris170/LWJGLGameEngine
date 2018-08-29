package renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import models.RawModel;
import models.TexturedModel;
import normalMappingRenderer.NormalMappingRenderer;
import normalMappingRenderer.NormalMappingShader;
import shaders.StaticShader;
import shaders.TerrainShader;
import shadows.ShadowMapMasterRenderer;
import skybox.SkyboxRenderer;
import terrains.Terrain;
import toolbox.Maths;

public class MasterRenderer {

	public static final float FOV = 110;
	public static final float NEAR_PLANE = 0.1f;
	public static final float FAR_PLANE = 2000;

	public static final float RED = 0.5444f;
	public static final float GREEN = 0.62f;
	public static final float BLUE = 0.69f;

	public static final float RED_N = 0.009f;
	public static final float GREEN_N = 0.009f;
	public static final float BLUE_N = 0.05f;
	public static final float ALPHA = 1f;

	public static final Vector3f DAY_FOG_COLOR = new Vector3f(MasterRenderer.RED, MasterRenderer.GREEN, MasterRenderer.BLUE);
	public static final Vector3f NIGHT_FOG_COLOR = new Vector3f(MasterRenderer.RED_N, MasterRenderer.GREEN_N, MasterRenderer.BLUE_N);

	private Matrix4f projectionMatrix;

	private StaticShader shader = new StaticShader();
	private EntityRenderer renderer;

	private TerrainShader terrainShader = new TerrainShader();
	private TerrainRenderer terrainRenderer;

	private NormalMappingShader normalMapShader = new NormalMappingShader();
	private NormalMappingRenderer normalMapRenderer;

	private Map<TexturedModel, List<Entity>> entities = new HashMap<>();
	private Map<TexturedModel, List<Entity>> normalMapEntities = new HashMap<>();
	private Map<RawModel, List<Terrain>> terrains = new HashMap<>();
	private List<Terrain> terrainList = new ArrayList<>();

	private SkyboxRenderer skyboxRenderer;
	private ShadowMapMasterRenderer shadowMapRenderer;

	public MasterRenderer(final Loader loader, final Camera camera) {
		MasterRenderer.enableCulling();
		createProjectionMatrix();
		renderer = new EntityRenderer(shader, projectionMatrix);
		terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
		skyboxRenderer = new SkyboxRenderer(loader, projectionMatrix);
		normalMapRenderer = new NormalMappingRenderer(normalMapShader, projectionMatrix);
		shadowMapRenderer = new ShadowMapMasterRenderer(camera);
	}

	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	public static void enableCulling() {
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
	}

	public static void disableCulling() {
		GL11.glDisable(GL11.GL_CULL_FACE);
	}

	public boolean[] renderScene(final List<Entity> entities, final List<Entity> normalMapEntities, final List<Terrain> terrians, final List<Light> lights, final Camera camera,
			final Vector4f clippingPlane, final int grassTexture, final int mudTexture) {
		for(final Terrain terrain : terrians) {
			processTerrain(terrain);
		}
		for(final Entity entity : entities) {
			processEntity(entity);
		}
		for(final Entity entity : normalMapEntities) {
			processNormalMapEntity(entity);
		}
		return render(lights, camera, clippingPlane, grassTexture, mudTexture);
	}

	public boolean[] render(final List<Light> lights, final Camera camera, final Vector4f clippingPlane, final int grassTexture, final int mudTexture) {
		prepare();

		final boolean[] texDays = skyboxRenderer.render(camera);
		final float blendFactor = SkyboxRenderer.getBlend();

		shader.start();
		shader.loadClippingPlane(clippingPlane);
		if(!texDays[0] && !texDays[1]) {
			shader.loadSkyColors(MasterRenderer.NIGHT_FOG_COLOR, MasterRenderer.NIGHT_FOG_COLOR);
		} else if(!texDays[0] && texDays[1]) {
			shader.loadSkyColors(MasterRenderer.NIGHT_FOG_COLOR, MasterRenderer.DAY_FOG_COLOR);
		} else if(texDays[0] && texDays[1]) {
			shader.loadSkyColors(MasterRenderer.DAY_FOG_COLOR, MasterRenderer.DAY_FOG_COLOR);
		} else {
			shader.loadSkyColors(MasterRenderer.DAY_FOG_COLOR, MasterRenderer.NIGHT_FOG_COLOR);
		}
		shader.loadBlendFactor(blendFactor);
		shader.loadLights(lights);
		shader.loadViewMatrix(camera);
		shader.loadToShadowMapSpaceMatrix(shadowMapRenderer.getToShadowMapSpaceMatrix());
		renderer.render(entities, getShadowMapTexture());
		shader.stop();
		normalMapShader.start();
		normalMapShader.loadClipPlane(clippingPlane);
		if(!texDays[0] && !texDays[1]) {
			normalMapShader.loadSkyColors(MasterRenderer.NIGHT_FOG_COLOR, MasterRenderer.NIGHT_FOG_COLOR);
		} else if(!texDays[0] && texDays[1]) {
			normalMapShader.loadSkyColors(MasterRenderer.NIGHT_FOG_COLOR, MasterRenderer.DAY_FOG_COLOR);
		} else if(texDays[0] && texDays[1]) {
			normalMapShader.loadSkyColors(MasterRenderer.DAY_FOG_COLOR, MasterRenderer.DAY_FOG_COLOR);
		} else {
			normalMapShader.loadSkyColors(MasterRenderer.DAY_FOG_COLOR, MasterRenderer.NIGHT_FOG_COLOR);
		}
		normalMapShader.loadBlendFactor(blendFactor);
		final Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		normalMapShader.loadLights(lights, viewMatrix);
		normalMapShader.loadViewMatrix(viewMatrix);
		normalMapShader.loadToShadowMapSpaceMatrix(shadowMapRenderer.getToShadowMapSpaceMatrix());
		normalMapRenderer.render(normalMapEntities, getShadowMapTexture());
		normalMapShader.stop();
		terrainShader.start();
		terrainShader.loadClippingPlane(clippingPlane);
		if(!texDays[0] && !texDays[1]) {
			terrainShader.loadSkyColors(MasterRenderer.NIGHT_FOG_COLOR, MasterRenderer.NIGHT_FOG_COLOR);
		} else if(!texDays[0] && texDays[1]) {
			terrainShader.loadSkyColors(MasterRenderer.NIGHT_FOG_COLOR, MasterRenderer.DAY_FOG_COLOR);
		} else if(texDays[0] && texDays[1]) {
			terrainShader.loadSkyColors(MasterRenderer.DAY_FOG_COLOR, MasterRenderer.DAY_FOG_COLOR);
		} else {
			terrainShader.loadSkyColors(MasterRenderer.DAY_FOG_COLOR, MasterRenderer.NIGHT_FOG_COLOR);
		}
		terrainShader.loadBlendFactor(blendFactor);
		terrainShader.loadLights(lights);
		terrainShader.loadViewMatrix(camera);
		terrainShader.loadToShadowMapSpaceMatrix(shadowMapRenderer.getToShadowMapSpaceMatrix());
		terrainRenderer.render(terrainList, getShadowMapTexture(), grassTexture, mudTexture);
		terrainShader.stop();

		entities.clear();
		terrains.clear();
		normalMapEntities.clear();
		terrainList.clear();

		return texDays;
	}

	public void processTerrain(final Terrain terrain) {
		terrainList.add(terrain);
	}

	public void processTerrainForShadows(final Terrain terrain) {
		final RawModel rawModel = terrain.getModel();
		final List<Terrain> batch = terrains.get(rawModel);
		if(batch != null) {
			batch.add(terrain);
		} else {
			final List<Terrain> newBatch = new ArrayList<>();
			newBatch.add(terrain);
			terrains.put(rawModel, newBatch);
		}
	}

	public void prepare() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(0.1f, 0.1f, 0.1f, MasterRenderer.ALPHA);
	}

	private void createProjectionMatrix() {
		projectionMatrix = new Matrix4f();
		float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;

		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
		projectionMatrix.m33 = 0;
	}

	public void processEntity(final Entity entity) {
		final TexturedModel entityModel = entity.getModel();
		final List<Entity> batch = entities.get(entityModel);
		if(batch != null) {
			batch.add(entity);
		} else {
			final List<Entity> newBatch = new ArrayList<>();
			newBatch.add(entity);
			entities.put(entityModel, newBatch);
		}
	}

	public void processNormalMapEntity(final Entity normalMapEntity) {
		final TexturedModel entityModel = normalMapEntity.getModel();
		final List<Entity> batch = normalMapEntities.get(entityModel);
		if(batch != null) {
			batch.add(normalMapEntity);
		} else {
			final List<Entity> newBatch = new ArrayList<>();
			newBatch.add(normalMapEntity);
			normalMapEntities.put(entityModel, newBatch);
		}
	}

	public void renderShadowMap(final List<Terrain> terrainList, final List<Entity> entityList, final List<Entity> normalMapEntityList, final Light sun) {
		for(final Terrain t : terrainList) {
			processTerrainForShadows(t);
		}
		for(final Entity e : entityList) {
			processEntity(e);
		}
		for(final Entity e : normalMapEntityList) {
			processNormalMapEntity(e);
		}
		shadowMapRenderer.render(terrains, entities, normalMapEntities, sun);
		entities.clear();
		normalMapEntities.clear();
		terrains.clear();
	}

	public int getShadowMapTexture() {
		return shadowMapRenderer.getShadowMap();
	}

	public void purge() {
		shader.purge();
		terrainShader.purge();
		normalMapRenderer.purge();
		shadowMapRenderer.purge();
	}
}
