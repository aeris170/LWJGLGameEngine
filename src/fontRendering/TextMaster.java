package fontRendering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fontMeshCreator.FontType;
import fontMeshCreator.GUIText;
import fontMeshCreator.TextMeshData;
import renderEngine.Loader;

public class TextMaster {

	private static Loader loader;
	private static Map<FontType, List<GUIText>> texts = new HashMap<>();
	private static FontRenderer renderer;

	public static void init(final Loader Loader) {
		TextMaster.renderer = new FontRenderer();
		TextMaster.loader = Loader;
	}

	public static void render() {
		TextMaster.renderer.render(TextMaster.texts);
	}

	public static void loadText(final GUIText text) {
		final FontType font = text.getFont();
		final TextMeshData data = font.loadText(text);
		final int vao = TextMaster.loader.loadToVAO(data.getVertexPositions(), data.getTextureCoords());
		text.setMeshInfo(vao, data.getVertexCount());
		List<GUIText> textBatch = TextMaster.texts.get(font);
		if(textBatch == null) {
			textBatch = new ArrayList<>();
			TextMaster.texts.put(font, textBatch);
		}
		textBatch.add(text);
	}

	public static void removeText(final GUIText text) {
		final List<GUIText> textBatch = TextMaster.texts.get(text.getFont());
		textBatch.remove(text);
		if(textBatch.isEmpty()) {
			TextMaster.texts.remove(text.getFont());
		}
	}

	public static void purge() {
		TextMaster.renderer.purge();
	}
}
