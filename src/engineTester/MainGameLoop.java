package engineTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import fontMeshCreator.FontType;
import fontMeshCreator.GUIText;
import fontRendering.TextMaster;
import guis.GUIRenderer;
import guis.GUITexture;
import models.TexturedModel;
import normalMappingObjConverter.NormalMappedObjLoader;
import objConverter.OBJFileLoader;
import particles.ParticleMaster;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.MousePicker;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;
import water.lava.LavaRenderer;

public class MainGameLoop {

	public static Light sun;
	private static String fps;
	public static Player player;

	public static void main(final String[] args) {

		DisplayManager.init();

		final Loader loader = new Loader();
		TextMaster.init(loader);
		final ModelTexture sphereTextureAtlas = new ModelTexture(loader.loadTexture("sphereTexture", false));
		sphereTextureAtlas.setNumberOfRows(2);
		final TexturedModel playerModel = new TexturedModel(NormalMappedObjLoader.loadOBJ("sphere", loader), sphereTextureAtlas);
		playerModel.getTexture().setHasTransparency(true);
		playerModel.getTexture().setUseFakeLighting(true);
		playerModel.getTexture().setNormalMap(loader.loadTexture("sphereNormal", false));
		// player = new Player(playerModel, 0, new Vector3f(1218, 83, 1133), 0,
		// 0, 0, 0.1f);
		player = new Player(playerModel, 0, new Vector3f(1000, 0, 1000), 0, 0, 0, 0.1f);
		final Camera camera = new Camera(player);
		final MasterRenderer renderer = new MasterRenderer(loader, camera);
		ParticleMaster.init(loader, renderer.getProjectionMatrix());

		final FontType font = new FontType(loader.loadTexture("georgia", false), "georgia");
		final GUIText loadingText = new GUIText("LOADING", 3, font, new Vector2f(0.0f, 0.4f), 1f, true);
		loadingText.setColour(0.5f, 0.5f, 0.2f);
		loadingText.setFontFXOutlineColor(new Vector3f(0.2f, 0.2f, 0.0f));
		TextMaster.render();
		DisplayManager.loop();
		loadingText.remove();
		GUIText FPSText = new GUIText("FPS: " + DisplayManager.getFPS(), 1, font, new Vector2f(0f, 0.97f), 1f, false);
		FPSText.setColour(0.7f, 0.7f, 0.2f);
		FPSText.setFontFXOutlineColor(new Vector3f(0f, 0f, 0f));

		// *********TERRAIN TEXTURE STUFF**********

		final TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy3", true));
		final TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("mud", true));
		final TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers", true));
		final TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path", true));
		final TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap", false));

		final TerrainTexturePack texturePackForest = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);

		final TerrainTexture backgroundTextureLava = new TerrainTexture(loader.loadTexture("goldyMountain", true));
		final TerrainTexture rTextureLava = new TerrainTexture(loader.loadTexture("stoneFloor", true));
		final TerrainTexture gTextureLava = new TerrainTexture(loader.loadTexture("grassyMountain", true));
		final TerrainTexture bTextureLava = new TerrainTexture(loader.loadTexture("path", true));
		final TerrainTexturePack texturePackLava = new TerrainTexturePack(backgroundTextureLava, rTextureLava, gTextureLava, bTextureLava);

		// *****************************************

		final ModelTexture grassTexture = new ModelTexture(loader.loadTexture("grassy2", true));
		final ModelTexture mudTexture = new ModelTexture(loader.loadTexture("mud", true));

		final ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture("fern", true));
		fernTextureAtlas.setNumberOfRows(2);

		final TexturedModel fern = new TexturedModel(OBJFileLoader.loadOBJ("fern", loader), fernTextureAtlas);

		final TexturedModel bobble = new TexturedModel(OBJFileLoader.loadOBJ("pine", loader), new ModelTexture(loader.loadTexture("pine", true)));
		bobble.getTexture().setHasTransparency(true);

		fern.getTexture().setHasTransparency(true);

		// THERE IS ALSO A "texturePackForest" IF YOU WANT TO CHANGE
		final Terrain terrain = new Terrain(0, 0, loader, texturePackLava, blendMap, "heightMap");
		final List<Terrain> terrains = new ArrayList<>();
		terrains.add(terrain);

		final TexturedModel lamp = new TexturedModel(OBJLoader.loadOBJModel("lamp", loader), new ModelTexture(loader.loadTexture("lamp", true)));
		lamp.getTexture().setUseFakeLighting(true);

		final List<Entity> entities = new ArrayList<>();
		final List<Entity> normalMapEntities = new ArrayList<>();

		// ******************NORMAL MAP MODELS************************

		final TexturedModel boulderModel = new TexturedModel(NormalMappedObjLoader.loadOBJ("boulder", loader), new ModelTexture(loader.loadTexture("boulder", false)));
		boulderModel.getTexture().setNormalMap(loader.loadTexture("boulderNormal", false));
		boulderModel.getTexture().setShineDamper(10);
		boulderModel.getTexture().setReflectivity(0.5f);

		// ************ENTITIES*******************

		final Random random = new Random(5666778);
		for (int j = 0; j < 6000; j++) {
			final float terrainSize = terrain.getSize();
			final float x = random.nextFloat() * terrainSize;
			final float z = random.nextFloat() * terrainSize;
			final float y = terrain.getHeightOfTerrain(x, z);
			if ((j % 2) == 0) {
				if (y > -10) {
					final float rotX = random.nextFloat() * 180;
					final float rotZ = random.nextFloat() * 180;
					if (((rotX > 45) && (rotX < 135)) || ((rotZ > 45) && (rotZ < 135))) {
						normalMapEntities
						        .add(new Entity(boulderModel, 1, new Vector3f(x, y - 2, z), rotX, random.nextFloat() * 360, rotZ, (random.nextFloat() * 0.6f) + 0.8f));
					}
				}
			} else if ((j % 3) == 0) {
				entities.add(new Entity(fern, 1, new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, 0.9f));
			} /* else if((j % 2) == 0) { if(y > 0) { entities.add(new Entity(bobble, 1, new
			   * Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, (random.nextFloat() *
			   * 0.6f) + 2.3f)); } } */
		}

		// *******************OTHER SETUP***************

		final List<Light> lights = new ArrayList<>();
		sun = new Light(new Vector3f(1000000, 0, -1000000), new Vector3f(1.3f, 1.3f, 1.3f));
		lights.add(sun);

		final List<GUITexture> guiTextures = new ArrayList<>();
		final GUIRenderer guiRenderer = new GUIRenderer(loader);
		final GUITexture shadowMap = new GUITexture(renderer.getShadowMapTexture(), new Vector2f(0.5f, 0.5f), new Vector2f(0.5f, 0.5f));
		// FIXME
		// guiTextures.add(shadowMap);
		final MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);

		// **********Water Renderer Set-up************************

		final WaterFrameBuffers buffers = new WaterFrameBuffers();
		final WaterShader waterShader = new WaterShader();
		final WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), buffers);
		final LavaRenderer lavaRenderer = new LavaRenderer(loader, renderer.getProjectionMatrix(), buffers);

		final List<WaterTile> waters = new ArrayList<>();
		final WaterTile water = new WaterTile(400, 400, 0);

		final List<WaterTile> lavas = new ArrayList<>();
		for (int x = 400; x <= 2800; x += 800) {
			for (int y = 400; y <= 2800; y += 800) {
				final WaterTile lava = new WaterTile(x, y, -30);
				lavas.add(lava);
			}
		}

		sun.setPosition(new Vector3f(339868.97f, -933593.25f, 339868.97f));
		normalMapEntities.add(player);
		lights.add(player.light);
		// ****************Game Loop Below*********************

		while (!Display.isCloseRequested()) {
			player.move(terrains);
			camera.move();
			picker.update();

			ParticleMaster.update(camera);
			renderer.renderShadowMap(terrains, entities, normalMapEntities, sun);
			GL11.glEnable(GL30.GL_CLIP_DISTANCE0);

			// render reflection texture
			buffers.bindReflectionFrameBuffer();
			final float distance = 2 * (camera.getPosition().y - water.getHeight());
			camera.getPosition().y -= distance;
			camera.invertPitch();
			renderer.renderScene(entities, normalMapEntities, terrains, lights, camera, new Vector4f(0, 1, 0, -water.getHeight() + 1), grassTexture.getID(),
			        mudTexture.getID());
			camera.getPosition().y += distance;
			camera.invertPitch();

			// render refraction texture
			buffers.bindRefractionFrameBuffer();
			renderer.renderScene(entities, normalMapEntities, terrains, lights, camera, new Vector4f(0, -1, 0, water.getHeight() + 1), grassTexture.getID(),
			        mudTexture.getID());

			// render to screen
			GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
			buffers.unbindCurrentFrameBuffer();
			boolean[] texDays = renderer.renderScene(entities, normalMapEntities, terrains, lights, camera, new Vector4f(0, -1, 0, 100000), grassTexture.getID(),
			        mudTexture.getID());
			waterRenderer.render(lavas, camera, sun, texDays);
			// lavaRenderer.render(lavas, camera, sun, texDays);
			ParticleMaster.renderParticles(camera);
			guiRenderer.render(guiTextures);
			TextMaster.render();

			for (WaterTile wt : lavas) {
				// wt.move();
			}

			DisplayManager.loop();
			FPSText.remove();
			DisplayManager.updateFPS();
			FPSText = new GUIText("FPS: " + DisplayManager.getFPS(), 1, font, new Vector2f(0f, 0.97f), 1f, false);
			FPSText.setColour(0.7f, 0.7f, 0.2f);
			FPSText.setFontFXOutlineColor(new Vector3f(0f, 0f, 0f));
		}

		// *********Clean Up Below**************

		ParticleMaster.purge();
		TextMaster.purge();
		buffers.purge();
		waterShader.purge();
		guiRenderer.purge();
		renderer.purge();
		loader.purge();
		DisplayManager.exit();
	}
}