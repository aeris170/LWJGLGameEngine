package water;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;

import renderEngine.DisplayManager;

public class WaterFrameBuffers {

	protected static final int REFLECTION_WIDTH = 320;
	private static final int REFLECTION_HEIGHT = 180;

	protected static final int REFRACTION_WIDTH = DisplayManager.WIDTH;
	private static final int REFRACTION_HEIGHT = DisplayManager.HEIGHT;

	private int reflectionFrameBuffer;
	private int reflectionTexture;
	private int reflectionDepthBuffer;

	private int refractionFrameBuffer;
	private int refractionTexture;
	private int refractionDepthTexture;

	public WaterFrameBuffers() {// call when loading the game
		initialiseReflectionFrameBuffer();
		initialiseRefractionFrameBuffer();
	}

	public void purge() {// call when closing the game
		GL30.glDeleteFramebuffers(reflectionFrameBuffer);
		GL30.glDeleteFramebuffers(refractionFrameBuffer);

		GL11.glDeleteTextures(reflectionTexture);
		GL11.glDeleteTextures(refractionTexture);

		GL30.glDeleteRenderbuffers(reflectionDepthBuffer);
		GL11.glDeleteTextures(refractionDepthTexture);
	}

	public void bindReflectionFrameBuffer() {// call before rendering to this
												// FBO
		bindFrameBuffer(reflectionFrameBuffer, WaterFrameBuffers.REFLECTION_WIDTH, WaterFrameBuffers.REFLECTION_HEIGHT);
	}

	public void bindRefractionFrameBuffer() {// call before rendering to this
												// FBO
		bindFrameBuffer(refractionFrameBuffer, WaterFrameBuffers.REFRACTION_WIDTH, WaterFrameBuffers.REFRACTION_HEIGHT);
	}

	public void unbindCurrentFrameBuffer() {// call to switch to default frame
											// buffer
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
	}

	public int getReflectionTexture() {// get the resulting texture
		return reflectionTexture;
	}

	public int getRefractionTexture() {// get the resulting texture
		return refractionTexture;
	}

	public int getRefractionDepthTexture() {// get the resulting depth texture
		return refractionDepthTexture;
	}

	private void initialiseReflectionFrameBuffer() {
		reflectionFrameBuffer = createFrameBuffer();
		reflectionTexture = createTextureAttachment(WaterFrameBuffers.REFLECTION_WIDTH, WaterFrameBuffers.REFLECTION_HEIGHT);
		reflectionDepthBuffer = createDepthBufferAttachment(WaterFrameBuffers.REFLECTION_WIDTH, WaterFrameBuffers.REFLECTION_HEIGHT);
		unbindCurrentFrameBuffer();
	}

	private void initialiseRefractionFrameBuffer() {
		refractionFrameBuffer = createFrameBuffer();
		refractionTexture = createTextureAttachment(WaterFrameBuffers.REFRACTION_WIDTH, WaterFrameBuffers.REFRACTION_HEIGHT);
		refractionDepthTexture = createDepthTextureAttachment(WaterFrameBuffers.REFRACTION_WIDTH, WaterFrameBuffers.REFRACTION_HEIGHT);
		unbindCurrentFrameBuffer();
	}

	private void bindFrameBuffer(final int frameBuffer, final int width, final int height) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);// To make sure the texture
													// isn't bound
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
		GL11.glViewport(0, 0, width, height);
	}

	private int createFrameBuffer() {
		final int frameBuffer = GL30.glGenFramebuffers();
		// generate name for frame buffer
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer);
		// create the framebuffer
		GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
		// indicate that we will always render to color attachment 0
		return frameBuffer;
	}

	private int createTextureAttachment(final int width, final int height) {
		final int texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height,
				0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
				texture, 0);
		return texture;
	}

	private int createDepthTextureAttachment(final int width, final int height) {
		final int texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT32, width, height,
				0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
				texture, 0);
		return texture;
	}

	private int createDepthBufferAttachment(final int width, final int height) {
		final int depthBuffer = GL30.glGenRenderbuffers();
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
		GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, width,
				height);
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
				GL30.GL_RENDERBUFFER, depthBuffer);
		return depthBuffer;
	}

}
