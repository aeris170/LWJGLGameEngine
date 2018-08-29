package fontMeshCreator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.Display;

/**
 * Provides functionality for getting the values from a font file.
 *
 * @author Karl
 */
public class MetaFile {

	private static final int PAD_TOP = 0;
	private static final int PAD_LEFT = 1;
	private static final int PAD_BOTTOM = 2;
	private static final int PAD_RIGHT = 3;

	private static final int DESIRED_PADDING = 8;

	private static final String SPLITTER = " ";
	private static final String NUMBER_SEPARATOR = ",";

	private double aspectRatio;

	private double verticalPerPixelSize;
	private double horizontalPerPixelSize;
	private double spaceWidth;
	private int[] padding;
	private int paddingWidth;
	private int paddingHeight;

	private Map<Integer, Character> metaData = new HashMap<>();

	private BufferedReader reader;
	private Map<String, String> values = new HashMap<>();

	/**
	 * Opens a font file in preparation for reading.
	 *
	 * @param path - the font file.
	 */
	protected MetaFile(final String path) {
		aspectRatio = (double) Display.getWidth() / (double) Display.getHeight();
		openFile(path);
		loadPaddingData();
		loadLineSizes();
		final int imageWidth = getValueOfVariable("scaleW");
		loadCharacterData(imageWidth);
		close();
	}

	protected double getSpaceWidth() {
		return spaceWidth;
	}

	protected Character getCharacter(final int ascii) {
		return metaData.get(ascii);
	}

	/**
	 * Read in the next line and store the variable values.
	 *
	 * @return {@code true} if the end of the file hasn't been reached.
	 */
	private boolean processNextLine() {
		values.clear();
		String line = null;
		try {
			line = reader.readLine();
		} catch(final IOException e1) {}
		if((line == null) || line.startsWith("kerning")) {
			return false;
		}
		for(final String part : line.split(MetaFile.SPLITTER)) {
			final String[] valuePairs = part.split("=");
			if(valuePairs.length == 2) {
				values.put(valuePairs[0], valuePairs[1]);
			}
		}
		return true;
	}

	/**
	 * Gets the {@code int} value of the variable with a certain name on the
	 * current line.
	 *
	 * @param variable - the name of the variable.
	 * @return The value of the variable.
	 */
	private int getValueOfVariable(final String variable) {
		return Integer.parseInt(values.get(variable));
	}

	/**
	 * Gets the array of ints associated with a variable on the current line.
	 *
	 * @param variable - the name of the variable.
	 * @return The int array of values associated with the variable.
	 */
	private int[] getValuesOfVariable(final String variable) {
		final String[] numbers = values.get(variable).split(MetaFile.NUMBER_SEPARATOR);
		final int[] actualValues = new int[numbers.length];
		for(int i = 0; i < actualValues.length; i++) {
			actualValues[i] = Integer.parseInt(numbers[i]);
		}
		return actualValues;
	}

	/**
	 * Closes the font file after finishing reading.
	 */
	private void close() {
		try {
			reader.close();
		} catch(final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Opens the font file, ready for reading.
	 *
	 * @param file - the font file.
	 */
	private void openFile(final String path) {
		try {
			reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/" + path + ".fnt")));
		} catch(final Exception e) {
			e.printStackTrace();
			System.err.println("Couldn't read font meta file!");
		}
	}

	/**
	 * Loads the data about how much padding is used around each character in
	 * the texture atlas.
	 */
	private void loadPaddingData() {
		processNextLine();
		padding = getValuesOfVariable("padding");
		paddingWidth = padding[MetaFile.PAD_LEFT] + padding[MetaFile.PAD_RIGHT];
		paddingHeight = padding[MetaFile.PAD_TOP] + padding[MetaFile.PAD_BOTTOM];
	}

	/**
	 * Loads information about the line height for this font in pixels, and uses
	 * this as a way to find the conversion rate between pixels in the texture
	 * atlas and screen-space.
	 */
	private void loadLineSizes() {
		processNextLine();
		final int lineHeightPixels = getValueOfVariable("lineHeight") - paddingHeight;
		verticalPerPixelSize = TextMeshCreator.LINE_HEIGHT / lineHeightPixels;
		horizontalPerPixelSize = verticalPerPixelSize / aspectRatio;
	}

	/**
	 * Loads in data about each character and stores the data in the
	 * {@link Character} class.
	 *
	 * @param imageWidth - the width of the texture atlas in pixels.
	 */
	private void loadCharacterData(final int imageWidth) {
		processNextLine();
		processNextLine();
		while(processNextLine()) {
			final Character c = loadCharacter(imageWidth);
			if(c != null) {
				metaData.put(c.getId(), c);
			}
		}
	}

	/**
	 * Loads all the data about one character in the texture atlas and converts
	 * it all from 'pixels' to 'screen-space' before storing. The effects of
	 * padding are also removed from the data.
	 *
	 * @param imageSize - the size of the texture atlas in pixels.
	 * @return The data about the character.
	 */
	private Character loadCharacter(final int imageSize) {
		final int id = getValueOfVariable("id");
		if(id == TextMeshCreator.SPACE_ASCII) {
			spaceWidth = (getValueOfVariable("xadvance") - paddingWidth) * horizontalPerPixelSize;
			return null;
		}
		final double xTex = ((double) getValueOfVariable("x") + (padding[MetaFile.PAD_LEFT] - MetaFile.DESIRED_PADDING)) / imageSize;
		final double yTex = ((double) getValueOfVariable("y") + (padding[MetaFile.PAD_TOP] - MetaFile.DESIRED_PADDING)) / imageSize;
		final int width = getValueOfVariable("width") - (paddingWidth - (2 * MetaFile.DESIRED_PADDING));
		final int height = getValueOfVariable("height") - ((paddingHeight) - (2 * MetaFile.DESIRED_PADDING));
		final double quadWidth = width * horizontalPerPixelSize;
		final double quadHeight = height * verticalPerPixelSize;
		final double xTexSize = (double) width / imageSize;
		final double yTexSize = (double) height / imageSize;
		final double xOff = ((getValueOfVariable("xoffset") + padding[MetaFile.PAD_LEFT]) - MetaFile.DESIRED_PADDING) * horizontalPerPixelSize;
		final double yOff = (getValueOfVariable("yoffset") + (padding[MetaFile.PAD_TOP] - MetaFile.DESIRED_PADDING)) * verticalPerPixelSize;
		final double xAdvance = (getValueOfVariable("xadvance") - paddingWidth) * horizontalPerPixelSize;
		return new Character(id, xTex, yTex, xTexSize, yTexSize, xOff, yOff, quadWidth, quadHeight, xAdvance);
	}
}
