package nl.weeaboo.vn;

import java.util.Collection;

import nl.weeaboo.game.entity.Entity;

public interface IImageModule {

    public Entity createImage(ILayer layer);

    public Entity createTextDrawable(ILayer layer);

    public Entity createButton(ILayer layer);

    /**
     * Creates a texture object from the specified filename.
     *
     * @param filename The file to load
     * @param callStack The Lua callstack from which the this function was called.
     * @param suppressErrors If <code>true</code> doesn't log any errors that may occur.
     */
    public ITexture getTexture(String filename, String[] callStack, boolean suppressErrors);

    /**
     * Creates a texture from the given image data. The {@code scaleX} and {@code scaleY} factors scale from
     * pixel coordinates to the coordinates of image state.
     */
    public ITexture createTexture(int[] argb, int w, int h, double scaleX, double scaleY);

    /**
     * Creates a texture from a screenshot.
     */
    public ITexture createTexture(IScreenshot ss);

    /**
     * Returns the paths for all image files in the specified folder
     */
    public Collection<String> getImageFiles(String folder);

}
