package nl.weeaboo.vn.image;


public interface IWritableScreenshot extends IScreenshot {

	void setPixels(int[] argb, int w, int h, int screenWidth, int screenHeight);

	void setVolatilePixels(ITexture tex, int screenWidth, int screenHeight);

}
