package particles;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import models.RawModel;
import renderEngine.Loader;
import toolbox.Maths;

public class ParticleRenderer {

	private static final float[] VERTICES = {-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f};
	private static final int MAX_INSTANCES = 10000;
	private static final int INSTANCED_DATA_LENGTH = 21;
	private static final FloatBuffer BUFFER = BufferUtils.createFloatBuffer(ParticleRenderer.INSTANCED_DATA_LENGTH * ParticleRenderer.MAX_INSTANCES);

	private RawModel quad;
	private ParticleShader shader;

	private Loader loader;
	private int vbo;
	private int pointer = 0;

	protected ParticleRenderer(final Loader loader, final Matrix4f projectionMatrix) {
		this.loader = loader;
		vbo = loader.createEmptyVBO(ParticleRenderer.INSTANCED_DATA_LENGTH * ParticleRenderer.MAX_INSTANCES);
		quad = loader.loadToVAO(ParticleRenderer.VERTICES, 2);
		loader.addInstancedAttribute(quad.getVaoID(), vbo, 1, 4, ParticleRenderer.INSTANCED_DATA_LENGTH, 0);
		loader.addInstancedAttribute(quad.getVaoID(), vbo, 2, 4, ParticleRenderer.INSTANCED_DATA_LENGTH, 4);
		loader.addInstancedAttribute(quad.getVaoID(), vbo, 3, 4, ParticleRenderer.INSTANCED_DATA_LENGTH, 8);
		loader.addInstancedAttribute(quad.getVaoID(), vbo, 4, 4, ParticleRenderer.INSTANCED_DATA_LENGTH, 12);
		loader.addInstancedAttribute(quad.getVaoID(), vbo, 5, 4, ParticleRenderer.INSTANCED_DATA_LENGTH, 16);
		loader.addInstancedAttribute(quad.getVaoID(), vbo, 6, 1, ParticleRenderer.INSTANCED_DATA_LENGTH, 20);
		shader = new ParticleShader();
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}

	protected void render(final Map<ParticleTexture, List<Particle>> particles, final Camera camera) {
		final Matrix4f viewMatrix = Maths.createViewMatrix(camera);
		prepare();
		for(final ParticleTexture texture : particles.keySet()) {
			bindTexture(texture);
			final List<Particle> particleList = particles.get(texture);
			pointer = 0;
			final float[] vboData = new float[particleList.size() * ParticleRenderer.INSTANCED_DATA_LENGTH];
			for(final Particle particle : particleList) {
				updateModelViewMatrix(particle.getPosition(), particle.getRotation(), particle.getScale(), viewMatrix, vboData);
				updateTexCoordInfo(particle, vboData);
			}
			loader.updateVBO(vbo, vboData, ParticleRenderer.BUFFER);
			GL31.glDrawArraysInstanced(GL11.GL_TRIANGLE_STRIP, 0, quad.getVertexCount(), particleList.size());
		}
		finishRendering();
	}

	private void bindTexture(final ParticleTexture texture) {
		if(texture.usesAdditiveBlending()) {
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		} else {
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureID());
		shader.loadNumberOfRows(texture.getNumberOfRows());
	}

	private void updateModelViewMatrix(final Vector3f position, final float rotation, final float scale, final Matrix4f viewMatrix, final float[] vboData) {
		final Matrix4f modelMatrix = new Matrix4f();
		Matrix4f.translate(position, modelMatrix, modelMatrix);

		// TRANSPOSE HERE
		modelMatrix.m00 = viewMatrix.m00;
		modelMatrix.m01 = viewMatrix.m10;
		modelMatrix.m02 = viewMatrix.m20;
		modelMatrix.m10 = viewMatrix.m01;
		modelMatrix.m11 = viewMatrix.m11;
		modelMatrix.m12 = viewMatrix.m21;
		modelMatrix.m20 = viewMatrix.m02;
		modelMatrix.m21 = viewMatrix.m12;
		modelMatrix.m22 = viewMatrix.m22;
		// TRANSPOSE DONE

		Matrix4f.rotate((float) Math.toRadians(rotation), new Vector3f(0, 0, 1), modelMatrix, modelMatrix);
		Matrix4f.scale(new Vector3f(scale, scale, scale), modelMatrix, modelMatrix);
		final Matrix4f modelViewMatrix = Matrix4f.mul(viewMatrix, modelMatrix, null);
		storeMatrixData(modelViewMatrix, vboData);
	}

	private void storeMatrixData(final Matrix4f matrix, final float[] vboData) {
		vboData[pointer++] = matrix.m00;
		vboData[pointer++] = matrix.m01;
		vboData[pointer++] = matrix.m02;
		vboData[pointer++] = matrix.m03;
		vboData[pointer++] = matrix.m10;
		vboData[pointer++] = matrix.m11;
		vboData[pointer++] = matrix.m12;
		vboData[pointer++] = matrix.m13;
		vboData[pointer++] = matrix.m20;
		vboData[pointer++] = matrix.m21;
		vboData[pointer++] = matrix.m22;
		vboData[pointer++] = matrix.m23;
		vboData[pointer++] = matrix.m30;
		vboData[pointer++] = matrix.m31;
		vboData[pointer++] = matrix.m32;
		vboData[pointer++] = matrix.m33;
	}

	protected void purge() {
		shader.purge();
	}

	private void updateTexCoordInfo(final Particle particle, final float[] data) {
		data[pointer++] = particle.getTextureOffset1().x;
		data[pointer++] = particle.getTextureOffset1().y;
		data[pointer++] = particle.getTextureOffset2().x;
		data[pointer++] = particle.getTextureOffset2().y;
		data[pointer++] = particle.getBlend();
	}

	private void prepare() {
		shader.start();
		GL30.glBindVertexArray(quad.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);
		GL20.glEnableVertexAttribArray(4);
		GL20.glEnableVertexAttribArray(5);
		GL20.glEnableVertexAttribArray(6);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDepthMask(false);
	}

	private void finishRendering() {
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL20.glDisableVertexAttribArray(3);
		GL20.glDisableVertexAttribArray(4);
		GL20.glDisableVertexAttribArray(5);
		GL20.glDisableVertexAttribArray(6);
		GL30.glBindVertexArray(0);
		shader.stop();
	}

}
