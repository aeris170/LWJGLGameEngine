package terrains;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import models.RawModel;
import renderEngine.Loader;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.Maths;

public class Terrain {

	public static final float DEFAULT_SIZE = 800;
	private static final int DEFAULT_VERTEX_COUNT = 256;

	// THESE ARE FOR HEIGHT MAP
	private static final float MAX_HEIGHT = 100;
	private static final float MAX_PIXEL_COLOR = 256 * 256 * 256;

	private static final int SEED = new Random(452).nextInt(1000000000);

	private float size = DEFAULT_SIZE;
	private int vertexCount = DEFAULT_VERTEX_COUNT;
	private float x;
	private float z;
	private RawModel model;
	private TerrainTexturePack texturePack;
	private TerrainTexture blendMap;

	private HeightsGenerator generator;

	private float[][] heights;

	public Terrain(final int gridX, final int gridZ, final Loader loader, final TerrainTexturePack texturePack, final TerrainTexture blendMap) {
		this(gridX, gridZ, loader, texturePack);
		this.blendMap = blendMap;
	}

	public Terrain(final int gridX, final int gridZ, final Loader loader, final TerrainTexturePack texturePack, final TerrainTexture blendMap, String heightMap) {
		this(gridX, gridZ, loader, texturePack, heightMap);
		this.blendMap = blendMap;
	}

	public Terrain(final int gridX, final int gridZ, final Loader loader, final TerrainTexturePack texturePack) {
		this.texturePack = texturePack;
		x = gridX * Terrain.DEFAULT_SIZE;
		z = gridZ * Terrain.DEFAULT_SIZE;
		generator = new HeightsGenerator(gridX, gridZ, Terrain.DEFAULT_VERTEX_COUNT, Terrain.SEED);
		model = generateTerrain(loader);
	}

	public Terrain(final int gridX, final int gridZ, final Loader loader, final TerrainTexturePack texturePack, String heightMap) {
		this.texturePack = texturePack;
		BufferedImage image = null;
		try {
			image = ImageIO.read(this.getClass().getResourceAsStream("/" + heightMap + ".png"));
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		vertexCount = image.getHeight() - 1;
		size *= image.getHeight() / 256f;
		model = generateTerrain(loader, image);
		x = gridX * size;
		z = gridZ * size;
	}

	public float getX() {
		return x;
	}

	public float getZ() {
		return z;
	}

	public RawModel getModel() {
		return model;
	}

	public TerrainTexturePack getTexturePack() {
		return texturePack;
	}

	public TerrainTexture getBlendMap() {
		return blendMap;
	}

	public float getHeightOfTerrain(final float worldX, final float worldZ) {
		final float terrainX = worldX - x;
		final float terrainZ = worldZ - z;
		final float gridSquareSize = size / ((float) heights.length - 1);
		final int gridX = (int) Math.floor(terrainX / gridSquareSize);
		final int gridZ = (int) Math.floor(terrainZ / gridSquareSize);
		if((gridX >= (heights.length - 1)) || (gridZ >= (heights.length - 1)) || (gridX < 0) || (gridZ < 0)) {
			return 0;
		}
		final float xCoord = (terrainX % gridSquareSize) / gridSquareSize;
		final float zCoord = (terrainZ % gridSquareSize) / gridSquareSize;
		if(xCoord <= (1 - zCoord)) {
			return Maths.barryCentric(new Vector3f(0, heights[gridX][gridZ], 0), new Vector3f(1, heights[gridX + 1][gridZ], 0), new Vector3f(0, heights[gridX][gridZ + 1], 1),
					new Vector2f(xCoord, zCoord));
		}
		return Maths.barryCentric(new Vector3f(1, heights[gridX + 1][gridZ], 0), new Vector3f(1, heights[gridX + 1][gridZ + 1], 1),
				new Vector3f(0, heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
	}

	private RawModel generateTerrain(final Loader loader) {
		heights = new float[Terrain.DEFAULT_VERTEX_COUNT][Terrain.DEFAULT_VERTEX_COUNT];
		final int count = Terrain.DEFAULT_VERTEX_COUNT * Terrain.DEFAULT_VERTEX_COUNT;
		final float[] vertices = new float[count * 3];
		final float[] normals = new float[count * 3];
		final float[] textureCoords = new float[count * 2];
		final int[] indices = new int[6 * (Terrain.DEFAULT_VERTEX_COUNT - 1) * (Terrain.DEFAULT_VERTEX_COUNT - 1)];
		int vertexPointer = 0;
		for(int i = 0; i < Terrain.DEFAULT_VERTEX_COUNT; i++) {
			for(int j = 0; j < Terrain.DEFAULT_VERTEX_COUNT; j++) {
				vertices[vertexPointer * 3] = (j / ((float) Terrain.DEFAULT_VERTEX_COUNT - 1)) * Terrain.DEFAULT_SIZE;
				final float height = generateHeight(j, i, generator);
				heights[j][i] = height;
				vertices[(vertexPointer * 3) + 1] = height;
				vertices[(vertexPointer * 3) + 2] = (i / ((float) Terrain.DEFAULT_VERTEX_COUNT - 1)) * Terrain.DEFAULT_SIZE;
				final Vector3f normal = calculateNormal(j, i, generator);
				normals[vertexPointer * 3] = normal.x;
				normals[(vertexPointer * 3) + 1] = normal.y;
				normals[(vertexPointer * 3) + 2] = normal.z;
				textureCoords[vertexPointer * 2] = j / ((float) Terrain.DEFAULT_VERTEX_COUNT - 1);
				textureCoords[(vertexPointer * 2) + 1] = i / ((float) Terrain.DEFAULT_VERTEX_COUNT - 1);
				vertexPointer++;
			}
		}
		int pointer = 0;
		for(int gz = 0; gz < (Terrain.DEFAULT_VERTEX_COUNT - 1); gz++) {
			for(int gx = 0; gx < (Terrain.DEFAULT_VERTEX_COUNT - 1); gx++) {
				final int topLeft = (gz * Terrain.DEFAULT_VERTEX_COUNT) + gx;
				final int topRight = topLeft + 1;
				final int bottomLeft = ((gz + 1) * Terrain.DEFAULT_VERTEX_COUNT) + gx;
				final int bottomRight = bottomLeft + 1;
				indices[pointer++] = topLeft;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = topRight;
				indices[pointer++] = topRight;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = bottomRight;
			}
		}
		return loader.loadToVAO(vertices, textureCoords, normals, indices);
	}

	private RawModel generateTerrain(Loader loader, final BufferedImage image) {
		heights = new float[vertexCount][vertexCount];
		int count = vertexCount * vertexCount;
		float[] vertices = new float[count * 3];
		float[] normals = new float[count * 3];
		float[] textureCoords = new float[count * 2];
		int[] indices = new int[6 * (vertexCount - 1) * (vertexCount - 1)];
		int vertexPointer = 0;
		for(int i = 0; i < vertexCount; i++) {
			for(int j = 0; j < vertexCount; j++) {
				vertices[vertexPointer * 3] = j / ((float) vertexCount - 1) * size;
				final float height = getHeight(j, i, image);
				heights[j][i] = height;
				vertices[vertexPointer * 3 + 1] = height;
				vertices[vertexPointer * 3 + 2] = i / ((float) vertexCount - 1) * size;
				Vector3f normal = calculateNormal(j, i, image);
				normals[vertexPointer * 3] = normal.x;
				normals[vertexPointer * 3 + 1] = normal.y;
				normals[vertexPointer * 3 + 2] = normal.z;
				textureCoords[vertexPointer * 2] = j / ((float) vertexCount - 1);
				textureCoords[vertexPointer * 2 + 1] = i / ((float) vertexCount - 1);
				vertexPointer++;
			}
		}
		int pointer = 0;
		for(int gz = 0; gz < vertexCount - 1; gz++) {
			for(int gx = 0; gx < vertexCount - 1; gx++) {
				int topLeft = (gz * vertexCount) + gx;
				int topRight = topLeft + 1;
				int bottomLeft = ((gz + 1) * vertexCount) + gx;
				int bottomRight = bottomLeft + 1;
				indices[pointer++] = topLeft;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = topRight;
				indices[pointer++] = topRight;
				indices[pointer++] = bottomLeft;
				indices[pointer++] = bottomRight;
			}
		}
		return loader.loadToVAO(vertices, textureCoords, normals, indices);
	}

	private Vector3f calculateNormal(final int x, final int y, final HeightsGenerator generator) {
		final float heightL = generateHeight(x - 1, y, generator);
		final float heightR = generateHeight(x + 1, y, generator);
		final float heightD = generateHeight(x, y - 1, generator);
		final float heightU = generateHeight(x, y + 1, generator);
		final Vector3f normal = new Vector3f(heightL - heightR, 2f, heightD - heightU);
		normal.normalise();
		return normal;
	}

	private Vector3f calculateNormal(final int x, final int y, final BufferedImage heightMap) {
		final float heightL = getHeight(x - 1, y, heightMap);
		final float heightR = getHeight(x + 1, y, heightMap);
		final float heightD = getHeight(x, y - 1, heightMap);
		final float heightU = getHeight(x, y + 1, heightMap);
		final Vector3f normal = new Vector3f(heightL - heightR, 2f, heightD - heightU);
		normal.normalise();
		return normal;
	}

	private float getHeight(int x, int z, BufferedImage bf) {
		if(x < 0 || x > bf.getHeight() || z < 0 || x > bf.getHeight()) {
			return 0;
		}
		float height = bf.getRGB(x, z);
		height += MAX_PIXEL_COLOR / 2f;
		height /= MAX_PIXEL_COLOR / 2f;
		height *= MAX_HEIGHT;
		return height;
	}

	private float generateHeight(final int x, final int z, final HeightsGenerator generator) {
		return generator.generateHeight(x, z);
	}

	public float getSize() {
		return size;
	}
}
