package objConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import models.RawModel;
import renderEngine.Loader;

public class OBJFileLoader {

	public static RawModel loadOBJ(final String fileName, final Loader loader) {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(OBJFileLoader.class.getResourceAsStream("/" + fileName + ".obj")));
		String line;
		final List<Vertex> vertices = new ArrayList<>();
		final List<Vector2f> textures = new ArrayList<>();
		final List<Vector3f> normals = new ArrayList<>();
		final List<Integer> indices = new ArrayList<>();
		try {
			while(true) {
				line = reader.readLine();
				if(line.startsWith("v ")) {
					final String[] currentLine = line.split(" ");
					final Vector3f vertex = new Vector3f(Float.valueOf(currentLine[1]),
							Float.valueOf(currentLine[2]),
							Float.valueOf(currentLine[3]));
					final Vertex newVertex = new Vertex(vertices.size(), vertex);
					vertices.add(newVertex);

				} else if(line.startsWith("vt ")) {
					final String[] currentLine = line.split(" ");
					final Vector2f texture = new Vector2f(Float.valueOf(currentLine[1]),
							Float.valueOf(currentLine[2]));
					textures.add(texture);
				} else if(line.startsWith("vn ")) {
					final String[] currentLine = line.split(" ");
					final Vector3f normal = new Vector3f(Float.valueOf(currentLine[1]),
							Float.valueOf(currentLine[2]),
							Float.valueOf(currentLine[3]));
					normals.add(normal);
				} else if(line.startsWith("f ")) {
					break;
				}
			}
			while((line != null) && line.startsWith("f ")) {
				final String[] currentLine = line.split(" ");
				final String[] vertex1 = currentLine[1].split("/");
				final String[] vertex2 = currentLine[2].split("/");
				final String[] vertex3 = currentLine[3].split("/");
				final Vertex v0 = OBJFileLoader.processVertex(vertex1, vertices, indices);
				final Vertex v1 = OBJFileLoader.processVertex(vertex2, vertices, indices);
				final Vertex v2 = OBJFileLoader.processVertex(vertex3, vertices, indices);
				OBJFileLoader.calculateTangents(v0, v1, v2, textures);
				line = reader.readLine();
			}
			reader.close();
		} catch(final IOException e) {
			System.err.println("Error reading the file");
		}
		OBJFileLoader.removeUnusedVertices(vertices);
		final float[] verticesArray = new float[vertices.size() * 3];
		final float[] texturesArray = new float[vertices.size() * 2];
		final float[] normalsArray = new float[vertices.size() * 3];
		final float[] tangentsArray = new float[vertices.size() * 3];
		final float furthest = OBJFileLoader.convertDataToArrays(vertices, textures, normals, verticesArray, texturesArray, normalsArray, tangentsArray);
		final int[] indicesArray = OBJFileLoader.convertIndicesListToArray(indices);
		// ModelData data = new ModelData(verticesArray, texturesArray,
		// normalsArray, tangentsArray, indicesArray,
		// furthest);
		return loader.loadToVAO(verticesArray, texturesArray, normalsArray, indicesArray);
	}

	private static void calculateTangents(final Vertex v0, final Vertex v1, final Vertex v2,
			final List<Vector2f> textures) {
		final Vector3f delatPos1 = Vector3f.sub(v1.getPosition(), v0.getPosition(), null);
		final Vector3f delatPos2 = Vector3f.sub(v2.getPosition(), v0.getPosition(), null);
		final Vector2f uv0 = textures.get(v0.getTextureIndex());
		final Vector2f uv1 = textures.get(v1.getTextureIndex());
		final Vector2f uv2 = textures.get(v2.getTextureIndex());
		final Vector2f deltaUv1 = Vector2f.sub(uv1, uv0, null);
		final Vector2f deltaUv2 = Vector2f.sub(uv2, uv0, null);

		final float r = 1.0f / ((deltaUv1.x * deltaUv2.y) - (deltaUv1.y * deltaUv2.x));
		delatPos1.scale(deltaUv2.y);
		delatPos2.scale(deltaUv1.y);
		final Vector3f tangent = Vector3f.sub(delatPos1, delatPos2, null);
		tangent.scale(r);
		v0.addTangent(tangent);
		v1.addTangent(tangent);
		v2.addTangent(tangent);
	}

	private static Vertex processVertex(final String[] vertex, final List<Vertex> vertices,
			final List<Integer> indices) {
		final int index = Integer.parseInt(vertex[0]) - 1;
		final Vertex currentVertex = vertices.get(index);
		final int textureIndex = Integer.parseInt(vertex[1]) - 1;
		final int normalIndex = Integer.parseInt(vertex[2]) - 1;
		if(!currentVertex.isSet()) {
			currentVertex.setTextureIndex(textureIndex);
			currentVertex.setNormalIndex(normalIndex);
			indices.add(index);
			return currentVertex;
		}
		return OBJFileLoader.dealWithAlreadyProcessedVertex(currentVertex, textureIndex, normalIndex, indices,
				vertices);
	}

	private static int[] convertIndicesListToArray(final List<Integer> indices) {
		final int[] indicesArray = new int[indices.size()];
		for(int i = 0; i < indicesArray.length; i++) {
			indicesArray[i] = indices.get(i);
		}
		return indicesArray;
	}

	private static float convertDataToArrays(final List<Vertex> vertices,
			final List<Vector2f> textures, final List<Vector3f> normals, final float[] verticesArray, final float[] texturesArray, final float[] normalsArray,
			final float[] tangentsArray) {
		float furthestPoint = 0;
		for(int i = 0; i < vertices.size(); i++) {
			final Vertex currentVertex = vertices.get(i);
			if(currentVertex.getLength() > furthestPoint) {
				furthestPoint = currentVertex.getLength();
			}
			final Vector3f position = currentVertex.getPosition();
			final Vector2f textureCoord = textures.get(currentVertex.getTextureIndex());
			final Vector3f normalVector = normals.get(currentVertex.getNormalIndex());
			final Vector3f tangent = currentVertex.getAverageTangent();
			verticesArray[i *
					3] = position.x;
			verticesArray[(i * 3) + 1] = position.y;
			verticesArray[(i * 3) + 2] = position.z;
			texturesArray[i * 2] = textureCoord.x;
			texturesArray[(i * 2) + 1] = 1 - textureCoord.y;
			normalsArray[i * 3] = normalVector.x;
			normalsArray[(i * 3) + 1] = normalVector.y;
			normalsArray[(i * 3) + 2] = normalVector.z;
			tangentsArray[i * 3] = tangent.x;
			tangentsArray[(i * 3) + 1] = tangent.y;
			tangentsArray[(i * 3) + 2] = tangent.z;
		}
		return furthestPoint;
	}

	private static Vertex dealWithAlreadyProcessedVertex(final Vertex previousVertex, final int newTextureIndex,
			final int newNormalIndex, final List<Integer> indices, final List<Vertex> vertices) {
		if(previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) {
			indices.add(previousVertex.getIndex());
			return previousVertex;
		}
		final Vertex anotherVertex = previousVertex.getDuplicateVertex();
		if(anotherVertex != null) {
			return OBJFileLoader.dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex,
					newNormalIndex, indices, vertices);
		}
		final Vertex duplicateVertex = new Vertex(vertices.size(), previousVertex.getPosition());
		duplicateVertex.setTextureIndex(newTextureIndex);
		duplicateVertex.setNormalIndex(newNormalIndex);
		previousVertex.setDuplicateVertex(duplicateVertex);
		vertices.add(duplicateVertex);
		indices.add(duplicateVertex.getIndex());
		return duplicateVertex;
	}

	private static void removeUnusedVertices(final List<Vertex> vertices) {
		for(final Vertex vertex : vertices) {
			vertex.averageTangents();
			if(!vertex.isSet()) {
				vertex.setTextureIndex(0);
				vertex.setNormalIndex(0);
			}
		}
	}

}