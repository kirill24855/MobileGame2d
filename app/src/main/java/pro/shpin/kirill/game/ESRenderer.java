package pro.shpin.kirill.game;

import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

public class ESRenderer implements GLSurfaceView.Renderer {

	private static final int BYTES_PER_FLOAT = 4;

	private final FloatBuffer triVerts;

	private float[] viewMatrix = new float[16];
	private float[] modelMatrix = new float[16];

	private int MVPMatrixHandle;
	private int positionHande;
	private int colorHandle;

	private float[] MVPMatrix = new float[16];

	private static final int POSITION_OFFSET = 0;
	private static final int POSITION_DATA_SIZE = 3;
	private static final int COLOR_OFFSET = POSITION_OFFSET+POSITION_DATA_SIZE;
	private static final int COLOR_DATA_SIZE = 4;

	private static final int STRIDE_BYTES = (POSITION_DATA_SIZE+COLOR_DATA_SIZE)*BYTES_PER_FLOAT;

	private float[] projectionMatrix = new float[16];

	private final String vertexShader =
		  "uniform mat4 u_MVPMatrix;      \n"     // A constant representing the combined model/view/projection matrix.

		+ "attribute vec4 a_Position;     \n"     // Per-vertex position information we will pass in.
		+ "attribute vec4 a_Color;        \n"     // Per-vertex color information we will pass in.

		+ "varying vec4 v_Color;          \n"     // This will be passed into the fragment shader.

		+ "void main()                    \n"     // The entry point for our vertex shader.
		+ "{                              \n"
		+ "   v_Color = a_Color;          \n"     // Pass the color through to the fragment shader.
												  // It will be interpolated across the triangle.
		+ "   gl_Position = u_MVPMatrix   \n"     // gl_Position is a special variable used to store the final position.
		+ "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in
		+ "}                              \n";    // normalized screen coordinates.

	private final String fragmentShader =
		  "precision mediump float;       \n"     // Set the default precision to medium. We don't need as high of a
												  // precision in the fragment shader.
		+ "varying vec4 v_Color;          \n"     // This is the color from the vertex shader interpolated across the
												  // triangle per fragment.
		+ "void main()                    \n"     // The entry point for our fragment shader.
		+ "{                              \n"
		+ "   gl_FragColor = v_Color;     \n"     // Pass the color directly through the pipeline.
		+ "}                              \n";

	public Game game;

	public ESRenderer(Game game) {
		final float[] triVertData = {
				// X, Y, Z,
				// R, G, B, A
				-0.5f, -0.25f, 0.0f,
				1.0f, 0.0f, 0.0f, 1.0f,

				0.5f, -0.25f, 0.0f,
				0.0f, 0.0f, 1.0f, 1.0f,

				0.0f, 0.559016994f, 0.0f,
				0.0f, 1.0f, 0.0f, 1.0f
		};

		triVerts = ByteBuffer.allocateDirect(triVertData.length * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
		triVerts.put(triVertData).position(0);

		this.game = game;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

		final float eyeX = 0f;
		final float eyeY = 0f;
		final float eyeZ = 1.5f;

		final float lookX = 0f;
		final float lookY = 0f;
		final float lookZ = -5f;

		final float upX = 0f;
		final float upY = 1f;
		final float upZ = 0f;

		Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

		int vertexShaderHandle = glCreateShader(GL_VERTEX_SHADER);
		if (vertexShaderHandle != 0) {
			glShaderSource(vertexShaderHandle, vertexShader);
			glCompileShader(vertexShaderHandle);

			int[] compileStatus = new int[1];
			glGetShaderiv(vertexShaderHandle, GL_COMPILE_STATUS, compileStatus, 0);

			if (compileStatus[0] == 0) {
				glDeleteShader(vertexShaderHandle);
				throw new RuntimeException("Vertex shader compilation fucked up");
			}
		}

		if (vertexShaderHandle == 0) throw new RuntimeException("Vertex shader didn't create");

		int fragmentShaderHandle = glCreateShader(GL_FRAGMENT_SHADER);
		if (fragmentShaderHandle != 0) {
			glShaderSource(fragmentShaderHandle, fragmentShader);
			glCompileShader(fragmentShaderHandle);

			int[] compileStatus = new int[1];
			glGetShaderiv(fragmentShaderHandle, GL_COMPILE_STATUS, compileStatus, 0);

			if (compileStatus[0] == 0) {
				glDeleteShader(fragmentShaderHandle);
				throw new RuntimeException("Fragment shader compilation fucked up");
			}
		}

		if (fragmentShaderHandle == 0) throw new RuntimeException("Fragment shader didn't create");

		int programHandle = glCreateProgram();
		if (programHandle == 0) throw new RuntimeException("Program creation fucked up");
		else {
			glAttachShader(programHandle, vertexShaderHandle);
			glAttachShader(programHandle, fragmentShaderHandle);

			glBindAttribLocation(programHandle, 0, "a_Position");
			glBindAttribLocation(programHandle, 1, "a_Color");

			glLinkProgram(programHandle);

			final int[] linkStatus = new int[1];
			glGetProgramiv(programHandle, GL_LINK_STATUS, linkStatus, 0);

			if (linkStatus[0] == 0) {
				glDeleteProgram(programHandle);
				throw new RuntimeException("Shader linking fucked up");
			}
		}

		MVPMatrixHandle = glGetUniformLocation(programHandle, "u_MVPMatrix");
		positionHande = glGetAttribLocation(programHandle, "a_Position");
		colorHandle = glGetAttribLocation(programHandle, "a_Color");

		glUseProgram(programHandle);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		glViewport(0, 0, width, height);

		final float ratio = (float) width/(float) height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1f;
		final float top = 1f;
		final float near = 1f;
		final float far = 10f;

		Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

		long time = SystemClock.uptimeMillis() % 10000L;
		float angleDeg = 360f/10000f*time;

		fillTri(triVerts, game.getTouchX(), game.getTouchY(), angleDeg);
	}

	private void fillTri(FloatBuffer triBuffer, float shiftX, float shiftY, float angleDeg) {
		Matrix.setIdentityM(modelMatrix, 0);
		Matrix.translateM(modelMatrix, 0, shiftX, shiftY, 0f);
		Matrix.rotateM(modelMatrix, 0, angleDeg, 0f, 0f, 1f);

		triBuffer.position(POSITION_OFFSET);
		glVertexAttribPointer(positionHande, POSITION_DATA_SIZE, GL_FLOAT, false, STRIDE_BYTES, triBuffer);
		glEnableVertexAttribArray(positionHande);

		triBuffer.position(COLOR_OFFSET);
		glVertexAttribPointer(colorHandle, COLOR_DATA_SIZE, GL_FLOAT, false, STRIDE_BYTES, triBuffer);
		glEnableVertexAttribArray(colorHandle);

		Matrix.multiplyMM(MVPMatrix, 0, viewMatrix, 0, modelMatrix, 0);
		Matrix.multiplyMM(MVPMatrix, 0, projectionMatrix, 0, MVPMatrix, 0);

		glUniformMatrix4fv(MVPMatrixHandle, 1, false, MVPMatrix, 0);
		glDrawArrays(GL_TRIANGLES, 0, 3);
	}
}
