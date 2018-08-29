package engineTester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import guis.GUIRenderer;
import guis.GUITexture;
import models.TexturedModel;
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

public class MaingameLoopOLD {

	public static void main(final String[] args) {
		DisplayManager.init();

		final Loader loader = new Loader();

		// ********* TERRAIN TEXTURE *********

		final TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy2"));
		final TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("mud"));
		final TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("pinkFlowers"));
		final TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));

		final TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		final TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));

		// ********* TERRAIN TEXTURE END *****

		final TexturedModel grass = new TexturedModel(OBJLoader.loadOBJModel("grassModel", loader), new ModelTexture(loader.loadTexture("grassTexture")));
		final TexturedModel flower = new TexturedModel(OBJLoader.loadOBJModel("grassModel", loader), new ModelTexture(loader.loadTexture("flower")));
		final TexturedModel fern = new TexturedModel(OBJLoader.loadOBJModel("fern", loader), new ModelTexture(loader.loadTexture("fern"), 2));
		final TexturedModel bobble = new TexturedModel(OBJLoader.loadOBJModel("pine", loader), new ModelTexture(loader.loadTexture("pine")));
		final TexturedModel lamp = new TexturedModel(OBJLoader.loadOBJModel("lamp", loader), new ModelTexture(loader.loadTexture("lamp")));

		grass.getTexture().setHasTransparency(true);
		grass.getTexture().setUseFakeLighting(true);

		flower.getTexture().setHasTransparency(true);
		flower.getTexture().setUseFakeLighting(true);

		fern.getTexture().setHasTransparency(true);

		bobble.getTexture().setHasTransparency(true);

		lamp.getTexture().setUseFakeLighting(true);

		final List<Terrain> terrains = new ArrayList<>();
		final Terrain terrain = new Terrain(0, -1, loader, texturePack, blendMap, "heightMap");
		terrains.add(terrain);

		final List<Entity> entities = new ArrayList<>();
		final Random random = new Random(676452);
		for(int i = 0; i < 400; i++) {
			if((i % 3) == 0) {
				final float x = random.nextFloat() * 800;
				final float z = random.nextFloat() * -600;
				final float y = terrain.getHeightOfTerrain(x, z);
				entities.add(new Entity(fern, random.nextInt(4), new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, 0.9f));
			}
			if((i % 2) == 0) {
				final float x = random.nextFloat() * 800;
				final float z = random.nextFloat() * -600;
				final float y = terrain.getHeightOfTerrain(x, z);
				entities.add(new Entity(bobble, random.nextInt(4), new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, (random.nextFloat() * 0.6f) + 0.8f));
			}
		}

		final List<Light> lights = new ArrayList<>();
		lights.add(new Light(new Vector3f(0, 1000, -7000), new Vector3f(0.4f, 0.4f, 0.4f)));
		lights.add(new Light(new Vector3f(185, 10, -293), new Vector3f(0, 2, 2), new Vector3f(1, 0.01f, 0.002f)));
		lights.add(new Light(new Vector3f(370, 17, -300), new Vector3f(0, 2, 2), new Vector3f(1, 0.01f, 0.002f)));

		entities.add(new Entity(lamp, new Vector3f(185, -4.7f, -293), 0, 0, 0, 1));
		entities.add(new Entity(lamp, new Vector3f(370, 4.2f, -300), 0, 0, 0, 1));

		final MasterRenderer renderer = new MasterRenderer(loader);

		final TexturedModel playerTexture = new TexturedModel(OBJLoader.loadOBJModel("person", loader), new ModelTexture(loader.loadTexture("playerTexture")));
		final Player player = new Player(playerTexture, new Vector3f(153, 5, -274), 0, 100, 0, 0.6f);
		entities.add(player);

		final Camera camera = new Camera(player);

		final List<GUITexture> guis = new ArrayList<>();

		final GUIRenderer guiRenderer = new GUIRenderer(loader);

		final MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);

		final Entity lampEntity = new Entity(lamp, new Vector3f(293, -6.8f, -305), 0, 0, 0, 1);
		final Light lightEntity = new Light(new Vector3f(293, 7, -305), new Vector3f(2, 0, 0), new Vector3f(1, 0.001f, 0.002f));
		entities.add(lampEntity);
		lights.add(lightEntity);

		final WaterFrameBuffers fbos = new WaterFrameBuffers();
		final WaterShader waterShader = new WaterShader();
		final WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), fbos);
		final List<WaterTile> waters = new ArrayList<>();
		waters.add(new WaterTile(0, -1, -5));

		while(!Display.isCloseRequested()) {
			player.move(terrain);
			camera.move();
			picker.update();

			final Vector3f terrainPoint = picker.getCurrentTerrainPoint();
			if(terrainPoint != null) {
				lampEntity.setPosition(terrainPoint);
				lightEntity.setPosition(new Vector3f(terrainPoint.x,
						terrainPoint.y + 15, terrainPoint.z));
			}

			GL11.glEnable(GL30.GL_CLIP_DISTANCE0);

			// render
			fbos.bindReflectionFrameBuffer();
			final float distance = 2 * (camera.getPosition().y - waters.get(0).getHeight());
			camera.getPosition().y -= distance;
			camera.invertPitch();
			renderer.renderScene(entities, terrains, lights, camera, new Vector4f(0, 1, 0, -waters.get(0).getHeight() + 1f));
			camera.getPosition().y += distance;
			camera.invertPitch();

			fbos.bindRefractionFrameBuffer();;
			renderer.renderScene(entities, terrains, lights, camera, new Vector4f(0, -1, 0, waters.get(0).getHeight() + 1f));

			GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
			fbos.unbindCurrentFrameBuffer();
			renderer.renderScene(entities, terrains, lights, camera, new Vector4f(0, 0, 0, 0));
			waterRenderer.render(waters, camera, lights.get(0));
			guiRenderer.render(guis);

			// update
			DisplayManager.loop();
		}
		fbos.purge();
		waterShader.purge();
		guiRenderer.purge();
		renderer.purge();
		loader.purge();
		DisplayManager.exit();
	}

}
