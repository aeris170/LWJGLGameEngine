package renderEngine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import models.RawModel;

public class OBJLoader {

	public static RawModel loadOBJModel(final String fileName, final Loader loader) {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(OBJLoader.class.getResourceAsStream("/" + fileName + ".obj")));
		String line;
		final List<Vector3f> vertices = new ArrayList<>();
		final List<Vector2f> textures = new ArrayList<>();
		final List<Vector3f> normals = new ArrayList<>();
		final List<Integer> indices = new ArrayList<>();
		float[] verticesArray = null;
		float[] normalsArray = null;
		float[] textureArray = null;
		int[] indicesArray = null;
		try {

			while(true) {
				line = reader.readLine();
				final String[] currentLine = line.split(" ");
				if(line.startsWith("v ")) {
					final Vector3f vertex = new Vector3f(Float.parseFloat(currentLine[1]),
							Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3]));
					vertices.add(vertex);
				} else if(line.startsWith("vt ")) {
					final Vector2f texture = new Vector2f(Float.parseFloat(currentLine[1]),
							Float.parseFloat(currentLine[2]));
					textures.add(texture);
				} else if(line.startsWith("vn ")) {
					final Vector3f normal = new Vector3f(Float.parseFloat(currentLine[1]),
							Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3]));
					normals.add(normal);
				} else if(line.startsWith("f ")) {
					textureArray = new float[vertices.size() * 2];
					normalsArray = new float[vertices.size() * 3];
					break;
				}
			}

			while(line != null) {
				if(!line.startsWith("f ")) {
					line = reader.readLine();
					continue;
				}
				final String[] currentLine = line.split(" ");
				final String[] vertex1 = currentLine[1].split("/");
				final String[] vertex2 = currentLine[2].split("/");
				final String[] vertex3 = currentLine[3].split("/");

				OBJLoader.processVertex(vertex1, indices, textures, normals, textureArray, normalsArray);
				OBJLoader.processVertex(vertex2, indices, textures, normals, textureArray, normalsArray);
				OBJLoader.processVertex(vertex3, indices, textures, normals, textureArray, normalsArray);
				line = reader.readLine();
			}
			reader.close();

		} catch(final Exception e) {
			e.printStackTrace();
		}

		verticesArray = new float[vertices.size() * 3];
		indicesArray = new int[indices.size()];

		int vertexPointer = 0;
		for(final Vector3f vertex : vertices) {
			verticesArray[vertexPointer++] = vertex.x;
			verticesArray[vertexPointer++] = vertex.y;
			verticesArray[vertexPointer++] = vertex.z;
		}

		for(int i = 0; i < indices.size(); i++) {
			indicesArray[i] = indices.get(i);
		}
		return loader.loadToVAO(verticesArray, textureArray, normalsArray, indicesArray);

	}

	private static void processVertex(final String[] vertexData, final List<Integer> indices,
			final List<Vector2f> textures, final List<Vector3f> normals, final float[] textureArray,
			final float[] normalsArray) {
		final int currentVertexPointer = Integer.parseInt(vertexData[0]) - 1;
		indices.add(currentVertexPointer);
		final Vector2f currentTex = textures.get(Integer.parseInt(vertexData[1]) - 1);
		textureArray[currentVertexPointer * 2] = currentTex.x;
		textureArray[(currentVertexPointer * 2) + 1] = 1 - currentTex.y;
		final Vector3f currentNorm = normals.get(Integer.parseInt(vertexData[2]) - 1);
		normalsArray[currentVertexPointer * 3] = currentNorm.x;
		normalsArray[(currentVertexPointer * 3) + 1] = currentNorm.y;
		normalsArray[(currentVertexPointer * 3) + 2] = currentNorm.z;
	}

}