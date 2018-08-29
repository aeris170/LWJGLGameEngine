package shadows;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Entity;
import models.RawModel;
import models.TexturedModel;
import renderEngine.MasterRenderer;
import terrains.Terrain;
import toolbox.Maths;

public class ShadowMapEntityRenderer {

	private Matrix4f projectionViewMatrix;
	private ShadowShader shader;

	/**
	 * @param shader               - the simple shader program being used for
	 *                             the shadow render pass.
	 * @param projectionViewMatrix - the orthographic projection matrix
	 *                             multiplied by the light's "view" matrix.
	 */
	protected ShadowMapEntityRenderer(final ShadowShader shader, final Matrix4f projectionViewMatrix) {
		this.shader = shader;
		this.projectionViewMatrix = projectionViewMatrix;
	}

	protected void renderTerrain(final Map<RawModel, List<Terrain>> terrains) {
		MasterRenderer.disableCulling();
		for(final RawModel rawModel : terrains.keySet()) {
			bindModel(rawModel);
			for(final Terrain terrain : terrains.get(rawModel)) {
				prepareInstance(terrain);
				GL11.glDrawElements(GL11.GL_TRIANGLES, rawModel.getVertexCount(),
						GL11.GL_UNSIGNED_INT, 0);
			}
		}
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
	}

	/**
	 * Renders entities to the shadow map. Each model is first bound and then
	 * all of the entities using that model are rendered to the shadow map.
	 *
	 * @param entities - the entities to be rendered to the shadow map.
	 */
	protected void render(final Map<TexturedModel, List<Entity>> entities) {
		for(final TexturedModel model : entities.keySet()) {
			final RawModel rawModel = model.getRawModel();
			bindModel(rawModel);
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getID());
			if(model.getTexture().isHasTransparency()) {
				MasterRenderer.disableCulling();
			}
			for(final Entity entity : entities.get(model)) {
				prepareInstance(entity);
				GL11.glDrawElements(GL11.GL_TRIANGLES, rawModel.getVertexCount(),
						GL11.GL_UNSIGNED_INT, 0);
			}
			if(model.getTexture().isHasTransparency()) {
				MasterRenderer.enableCulling();
			}
		}
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
	}

	/**
	 * Binds a raw model before rendering. Only the attribute 0 is enabled here
	 * because that is where the positions are stored in the VAO, and only the
	 * positions are required in the vertex shader.
	 *
	 * @param rawModel - the model to be bound.
	 */
	private void bindModel(final RawModel rawModel) {
		GL30.glBindVertexArray(rawModel.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
	}

	/**
	 * Prepares an entity to be rendered. The model matrix is created in the
	 * usual way and then multiplied with the projection and view matrix (often
	 * in the past we've done this in the vertex shader) to create the
	 * mvp-matrix. This is then loaded to the vertex shader as a uniform.
	 *
	 * @param entity - the entity to be prepared for rendering.
	 */
	private void prepareInstance(final Entity entity) {
		final Matrix4f modelMatrix = Maths.createTransformationMatrix(entity.getPosition(),
				entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
		final Matrix4f mvpMatrix = Matrix4f.mul(projectionViewMatrix, modelMatrix, null);
		shader.loadMvpMatrix(mvpMatrix);
	}

	private void prepareInstance(final Terrain terrain) {
		float x = terrain.getX();
		float z = terrain.getZ();
		// final Matrix4f modelMatrix = Maths.createTransformationMatrix(new
		// Vector3f(x, terrain.getHeightOfTerrain(x / 4, z / 4), z), 0, 0, 0,
		// 1);
		final Matrix4f modelMatrix = Maths.createTransformationMatrix(new Vector3f(x, terrain.getHeightOfTerrain(x, z), z), 0, 0, 0, 1);
		final Matrix4f mvpMatrix = Matrix4f.mul(projectionViewMatrix, modelMatrix, null);
		shader.loadMvpMatrix(mvpMatrix);
	}
}
