package renderEngine;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.PixelFormat;

public class DisplayManager {

	public static final int WIDTH = 1280;
	public static final int HEIGHT = 720;
	public static final int FPS_CAP = 249;

	private static long lastTime;
	private static float delta;
	private static int fps, fpsCounter;
	private static long lastFPS;

	private static boolean isFullScreen = false;

	public static void init() {
		final ContextAttribs attribs = new ContextAttribs(3, 3).withForwardCompatible(true).withProfileCore(true);
		try {
			if(isFullScreen) {
				Display.setDisplayModeAndFullscreen(Display.getDesktopDisplayMode());
			} else {
				Display.setDisplayMode(new DisplayMode(DisplayManager.WIDTH, DisplayManager.HEIGHT));
			}
			Display.create(new PixelFormat().withSamples(8).withDepthBits(24), attribs);
			Display.setTitle("OpenGL Frame");
			Mouse.setGrabbed(true);
			GL11.glEnable(GL13.GL_MULTISAMPLE);
		} catch(final LWJGLException ex) {
			ex.printStackTrace();
		}
		GL11.glViewport(0, 0, DisplayManager.WIDTH, DisplayManager.HEIGHT);
		DisplayManager.lastTime = DisplayManager.currentTime();
		lastFPS = currentTime();
	}

	public static void loop() {
		long time = currentTime();
		Display.sync(DisplayManager.FPS_CAP);
		Display.update();
		DisplayManager.delta = (time - DisplayManager.lastTime) / 1000f;
		DisplayManager.lastTime = time;
	}

	public static void exit() {
		Display.destroy();
	}

	public static float getFrameTimeSeconds() {
		return DisplayManager.delta;
	}

	public static int getFPS() {
		return fps;
	}

	private static long currentTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}

	public static void updateFPS() {
		fpsCounter++;
		if(currentTime() - lastFPS > 1000) {
			fps = fpsCounter;
			fpsCounter = 0;
			lastFPS += 1000;
		}
	}
}
