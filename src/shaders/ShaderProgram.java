package shaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public abstract class ShaderProgram {

	private int programID;
	private int vertexShaderID;
	private int fragmentShaderID;

	private static FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

	public ShaderProgram(final String vertexFile, final String fragmentFile) {
		vertexShaderID = ShaderProgram.loadShader(vertexFile, GL20.GL_VERTEX_SHADER);
		fragmentShaderID = ShaderProgram.loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER);
		programID = GL20.glCreateProgram();
		GL20.glAttachShader(programID, vertexShaderID);
		GL20.glAttachShader(programID, fragmentShaderID);
		bindAttributes();
		GL20.glLinkProgram(programID);
		GL20.glValidateProgram(programID);
		getAllUniformLocations();
	}

	protected abstract void getAllUniformLocations();

	protected int getUniformLocation(final String uniformName) {
		return GL20.glGetUniformLocation(programID, uniformName);
	}

	public void start() {
		GL20.glUseProgram(programID);
	}

	public void stop() {
		GL20.glUseProgram(0);
	}

	public void purge() {
		stop();
		GL20.glDetachShader(programID, vertexShaderID);
		GL20.glDetachShader(programID, fragmentShaderID);
		GL20.glDeleteShader(vertexShaderID);
		GL20.glDeleteShader(fragmentShaderID);
		GL20.glDeleteProgram(programID);
	}

	protected abstract void bindAttributes();

	protected void bindAttribute(final int attribute, final String variableName) {
		GL20.glBindAttribLocation(programID, attribute, variableName);
	}

	protected void loadFloat(final int location, final float value) {
		GL20.glUniform1f(location, value);
	}

	protected void loadInt(final int location, final int value) {
		GL20.glUniform1i(location, value);
	}

	protected void loadBoolean(final int location, final boolean value) {
		final float toLoad = value ? 1 : 0;
		GL20.glUniform1f(location, toLoad);
	}

	protected void loadMatrix(final int location, final Matrix4f matrix) {
		matrix.store(ShaderProgram.matrixBuffer);
		ShaderProgram.matrixBuffer.flip();
		GL20.glUniformMatrix4(location, false, ShaderProgram.matrixBuffer);
	}

	protected void load2DVector(final int location, final Vector2f vector) {
		GL20.glUniform2f(location, vector.x, vector.y);
	}

	protected void loadVector(final int location, final Vector3f vector) {
		GL20.glUniform3f(location, vector.x, vector.y, vector.z);
	}

	protected void loadVector(final int location, final Vector4f vector) {
		GL20.glUniform4f(location, vector.x, vector.y, vector.z, vector.w);
	}

	private static int loadShader(final String file, final int type) {
		final StringBuilder shaderSource = new StringBuilder();
		try {
			final InputStream is = ShaderProgram.class.getResourceAsStream(file);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while((line = reader.readLine()) != null) {
				shaderSource.append(line).append("\n");
			}
			reader.close();
		} catch(final IOException ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
		final int shaderID = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, shaderSource);
		GL20.glCompileShader(shaderID);
		if(GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.out.println(GL20.glGetShaderInfoLog(shaderID, 500));
			System.err.println("Compile error on shader file: " + file);
			System.exit(-1);
		}
		return shaderID;
	}
}
