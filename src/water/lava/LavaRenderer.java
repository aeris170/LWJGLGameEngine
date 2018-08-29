package water.lava;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Light;
import models.RawModel;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import skybox.SkyboxRenderer;
import toolbox.Maths;
import water.WaterFrameBuffers;
import water.WaterTile;

public class LavaRenderer {

	private static final String DUDV_MAP = "waterDUDV";
	private static final String NORMAL_MAP = "normal";
	private static final String LAVA_TEXTURE = "lava";
	private static final float WAVE_SPEED = 0.08f;

	private RawModel quad;
	private LavaShader shader;
	private WaterFrameBuffers fbos;

	private float moveFactor = 10000;

	private int dudvTexture;
	private int normalMap;
	private int lavaTexture;

	public LavaRenderer(final Loader loader, final Matrix4f projectionMatrix, final WaterFrameBuffers fbos) {
		this.shader = new LavaShader();
		this.fbos = fbos;
		dudvTexture = loader.loadTexture(LavaRenderer.DUDV_MAP, false);
		normalMap = loader.loadTexture(LavaRenderer.NORMAL_MAP, false);
		lavaTexture = loader.loadTexture(LavaRenderer.LAVA_TEXTURE, false);
		shader.start();
		shader.connectTextureUnits();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
		setUpVAO(loader);
	}

	public void render(final List<WaterTile> water, final Camera camera, final Light light, boolean[] texDays) {
		MasterRenderer.disableCulling();
		prepareRender(camera, light, texDays);
		for(final WaterTile tile : water) {
			final Matrix4f modelMatrix = Maths.createTransformationMatrix(new Vector3f(tile.getX(), tile.getHeight(), tile.getZ()), 0, 0, 0, WaterTile.TILE_SIZE);
			shader.loadModelMatrix(modelMatrix);
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, quad.getVertexCount());
		}
		unbind();
		MasterRenderer.enableCulling();
	}

	private void prepareRender(final Camera camera, final Light light, boolean[] texDays) {
		shader.start();
		shader.loadViewMatrix(camera);
		if(!texDays[0] && !texDays[1]) {
			shader.loadSkyColors(MasterRenderer.NIGHT_FOG_COLOR, MasterRenderer.NIGHT_FOG_COLOR);
		} else if(!texDays[0] && texDays[1]) {
			shader.loadSkyColors(MasterRenderer.NIGHT_FOG_COLOR, MasterRenderer.DAY_FOG_COLOR);
		} else if(texDays[0] && texDays[1]) {
			shader.loadSkyColors(MasterRenderer.DAY_FOG_COLOR, MasterRenderer.DAY_FOG_COLOR);
		} else {
			shader.loadSkyColors(MasterRenderer.DAY_FOG_COLOR, MasterRenderer.NIGHT_FOG_COLOR);
		}
		shader.loadBlendFactor(SkyboxRenderer.getBlend());
		moveFactor += LavaRenderer.WAVE_SPEED * DisplayManager.getFrameTimeSeconds();
		moveFactor %= 1;
		shader.loadMoveFactor(moveFactor);
		shader.loadLight(light);
		GL30.glBindVertexArray(quad.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, lavaTexture);
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, dudvTexture);
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalMap);
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getRefractionDepthTexture());
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	private void unbind() {
		GL11.glDisable(GL11.GL_BLEND);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		shader.stop();
	}

	private void setUpVAO(final Loader loader) {
		// Just x and z vertex positions here, y is set to 0 in v.shader
		final float[] vertices = {-1, -1, -1, 1, 1, -1, 1, -1, -1, 1, 1, 1};
		quad = loader.loadToVAO(vertices, 2);
	}
}
