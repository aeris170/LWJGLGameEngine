package skybox;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import engineTester.MainGameLoop;
import entities.Camera;
import models.RawModel;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;

public class SkyboxRenderer {

	public static final float SIZE = 1200f;

	/*
	 * DAY STARTS AT 0 DEGREES, ENDS AT 180, THEN TRANSITION UNTIL 250, NIGHT
	 * STARTS AT 250, ENDS AT 290, THEN TRANSITION UNTIL 360 (or 0).
	 */
	private static final int NIGHT_START = 0; // 250 -> 290
	private static final int NIGHT_END = 2667; // 290 -> 0
	private static final int DAY_TRANSITION_END = 7333; // 290 -> 0
	private static final int DAY_END = 19333; // 0 -> 180
	private static final int DAY_LENGTH = 24000; // 180 -> 250

	private static final float[] VERTICES = {
			-SkyboxRenderer.SIZE, SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE,
			-SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE,
			SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE,
			SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE,
			SkyboxRenderer.SIZE, SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE,
			-SkyboxRenderer.SIZE, SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE,

			-SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE, SkyboxRenderer.SIZE,
			-SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE,
			-SkyboxRenderer.SIZE, SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE,
			-SkyboxRenderer.SIZE, SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE,
			-SkyboxRenderer.SIZE, SkyboxRenderer.SIZE, SkyboxRenderer.SIZE,
			-SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE, SkyboxRenderer.SIZE,

			SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE,
			SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE, SkyboxRenderer.SIZE,
			SkyboxRenderer.SIZE, SkyboxRenderer.SIZE, SkyboxRenderer.SIZE,
			SkyboxRenderer.SIZE, SkyboxRenderer.SIZE, SkyboxRenderer.SIZE,
			SkyboxRenderer.SIZE, SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE,
			SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE,

			-SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE, SkyboxRenderer.SIZE,
			-SkyboxRenderer.SIZE, SkyboxRenderer.SIZE, SkyboxRenderer.SIZE,
			SkyboxRenderer.SIZE, SkyboxRenderer.SIZE, SkyboxRenderer.SIZE,
			SkyboxRenderer.SIZE, SkyboxRenderer.SIZE, SkyboxRenderer.SIZE,
			SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE, SkyboxRenderer.SIZE,
			-SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE, SkyboxRenderer.SIZE,

			-SkyboxRenderer.SIZE, SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE,
			SkyboxRenderer.SIZE, SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE,
			SkyboxRenderer.SIZE, SkyboxRenderer.SIZE, SkyboxRenderer.SIZE,
			SkyboxRenderer.SIZE, SkyboxRenderer.SIZE, SkyboxRenderer.SIZE,
			-SkyboxRenderer.SIZE, SkyboxRenderer.SIZE, SkyboxRenderer.SIZE,
			-SkyboxRenderer.SIZE, SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE,

			-SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE,
			-SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE, SkyboxRenderer.SIZE,
			SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE,
			SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE,
			-SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE, SkyboxRenderer.SIZE,
			SkyboxRenderer.SIZE, -SkyboxRenderer.SIZE, SkyboxRenderer.SIZE
	};

	private static float blendFactor;

	private static String[] DAY_TEXTURE_FILES = {"right", "left", "top", "bottom", "back", "front"};
	private static String[] NIGHT_TEXTURE_FILES = {"nightRight", "nightLeft", "nightTop", "nightBottom", "nightBack", "nightFront"};

	private RawModel cube;
	private int dayTexture;
	private int nightTexture;
	private SkyboxShader shader;
	private float time = DAY_TRANSITION_END;

	private double angle;

	public SkyboxRenderer(final Loader loader, final Matrix4f projectionMatrix) {
		cube = loader.loadToVAO(SkyboxRenderer.VERTICES, 3);
		dayTexture = loader.loadCubeMap(SkyboxRenderer.DAY_TEXTURE_FILES);
		nightTexture = loader.loadCubeMap(SkyboxRenderer.NIGHT_TEXTURE_FILES);
		shader = new SkyboxShader();
		shader.start();
		shader.connectTextureUnits();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}

	public boolean[] render(final Camera camera) {
		shader.start();
		shader.loadViewMatrix(camera);
		GL30.glBindVertexArray(cube.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		final boolean[] texDays = bindTextures();
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, cube.getVertexCount());
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		shader.stop();
		return texDays;
	}

	public static float getBlend() {
		return blendFactor;
	}

	private boolean[] bindTextures() {
		boolean tex1Day;
		boolean tex2Day;
		float delta = DisplayManager.getFrameTimeSeconds() * 100;
		time += delta;
		time %= DAY_LENGTH;
		int texture1;
		int texture2;
		if((time >= NIGHT_START) && (time < NIGHT_END)) {
			texture1 = nightTexture;
			texture2 = nightTexture;
			shader.loadFogColors(MasterRenderer.NIGHT_FOG_COLOR, MasterRenderer.NIGHT_FOG_COLOR);
			SkyboxRenderer.blendFactor = (time - NIGHT_START) / (NIGHT_END - NIGHT_START);
			tex1Day = false;
			tex2Day = false;
		} else if((time >= NIGHT_END) && (time < DAY_TRANSITION_END)) {
			texture1 = nightTexture;
			texture2 = dayTexture;
			shader.loadFogColors(MasterRenderer.NIGHT_FOG_COLOR, MasterRenderer.DAY_FOG_COLOR);
			SkyboxRenderer.blendFactor = (time - NIGHT_END) / (DAY_TRANSITION_END - NIGHT_END);
			tex1Day = false;
			tex2Day = true;
		} else if((time >= DAY_TRANSITION_END) && (time < DAY_END)) {
			texture1 = dayTexture;
			texture2 = dayTexture;
			shader.loadFogColors(MasterRenderer.DAY_FOG_COLOR, MasterRenderer.DAY_FOG_COLOR);
			SkyboxRenderer.blendFactor = (time - DAY_TRANSITION_END) / (DAY_END - DAY_TRANSITION_END);
			tex1Day = true;
			tex2Day = true;
		} else {
			texture1 = dayTexture;
			texture2 = nightTexture;
			shader.loadFogColors(MasterRenderer.DAY_FOG_COLOR, MasterRenderer.NIGHT_FOG_COLOR);
			SkyboxRenderer.blendFactor = (time - DAY_END) / (DAY_LENGTH - DAY_END);
			tex1Day = true;
			tex2Day = false;
		}
		Vector3f sunPos = MainGameLoop.sun.getPosition();
		float sunY = sunPos.y;
		float sunXZ = sunPos.z;
		double theta = (360.0 * delta) / DAY_LENGTH;
		double thetaRadians = Math.toRadians(theta);
		sunY = (float) (sunY * Math.cos(thetaRadians) - sunXZ * Math.sin(thetaRadians));
		sunXZ = (float) (sunY * Math.sin(thetaRadians) + sunXZ * Math.cos(thetaRadians));
		MainGameLoop.sun.setPosition(new Vector3f(sunXZ, sunY, sunXZ));
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture1);
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, texture2);
		shader.loadBlendFactor(SkyboxRenderer.blendFactor);
		angle += theta;
		// System.out.println(angle);
		return new boolean[] {tex1Day, tex2Day};
	}
}
