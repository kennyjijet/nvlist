package nl.weeaboo.vn;

public interface IWritableScreenshot extends IScreenshot {

	void setPixels(int[] argb, int w, int h, int screenWidth, int screenHeight);

	void setVolatilePixels(ITexture tex, int screenWidth, int screenHeight);

}
