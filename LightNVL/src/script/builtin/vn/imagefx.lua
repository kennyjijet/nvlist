-------------------------------------------------------------------------------
-- imagefx.lua
-------------------------------------------------------------------------------
-- Provides the 'built-in' VN image manipulation functions.
-------------------------------------------------------------------------------

module("vn.imagefx", package.seeall)

-- ----------------------------------------------------------------------------
--  Functions
-- ----------------------------------------------------------------------------

---Creates a blurred copy of a texture
-- @param t The texture to create a blurred copy of
-- @param kernelSize Determines the size of the blur kernel
-- @param borderExtend Use <code>true</code> to pad the expanded image area
--        with the image's border pixels, or <code>false</code> to pad with
--        transparent pixels.<br/>
--        You can also use an integer to only extend in certain directions,
--        each digit corresponds to a numpad direction (example: 268 is
--        bottom-right-top).
-- @param cropResult Use <code>true</code> to crop the result texture to the
--        same size as <code>tex</code>, otherwise the result texture will be
--        padded on each side equal to half the size of the blur kernel.
--        You can also use an integer to only crop in certain directions,
--        each digit corresponds to a numpad direction (example: 486 is
--        left-top-right).
-- @return A blurred copy of the input texture
function blur(t, kernelSize, borderExtend, cropResult)
	t = tex(t)
	return ImageFx.blur(t, kernelSize, borderExtend, cropResult)
end

---Creates a collection of progressively more blurred copies of a single image.
-- @param t The texture to create blurred copies of
-- @param levels The number of blur images to generate
-- @param kernelSize Determines the size of the blur kernel
-- @param borderExtend Use <code>true</code> to pad the expanded image area
--        with the image's border pixels, or <code>false</code> to pad with
--        transparent pixels.<br/>
--        You can also use an integer to only extend in certain directions,
--        each digit corresponds to a numpad direction (example: 268 is
--        bottom-right-top).
-- @return A table containing the blurred copies.
-- @see blur
function blurMultiple(t, levels, kernelSize, borderExtend)
	t = tex(t)
	return ImageFx.blurMultiple(t, levels, kernelSize, borderExtend, cropResult)
end

---Creates a copy of the given texture downscaled by a factor
-- <code>2<sup>level</sup></code>.
-- @param t The texture to create a downscaled copy of
-- @param level The mipmap level
-- @return A new, downscaled texture.
function mipmap(t, level)
	t = tex(t)
	return ImageFx.mipmap(t, level)
end

---Creates a brightened copy of a texture
-- @param t The texture to create a brightened copy of
-- @param add A fraction between <code>-1.0</code> and <code>1.0</code> that
--        will be added to the color components of the input texture.
-- @return A brightened copy of the input texture
function brighten(t, add)
	t = tex(t)
	return ImageFx.brighten(t, add)
end

---Creates a copy of the given texture, with a color matrix applied. Pseudocode:<br/>
-- <code>r' = rf[1]*r + rf[2]*g + rf[3]*b + rf[4]*a + rf[5]</code><br/>
-- This assumes non-permultiplied color and color values ranging between
-- <code>0.0</code> and <code>1.0</code>.
-- @param t The source texture
-- @param rf A table containing the multiplication factors for the red channel.
-- @param gf A table containing the multiplication factors for the green channel.
-- @param bf A table containing the multiplication factors for the blue channel.
-- @param af A table containing the multiplication factors for the alpha channel.
-- @return A new texture with the color matrix applied.
function applyColorMatrix(t, rf, gf, bf, af)
	t = tex(t)
	return ImageFx.applyColorMatrix(t, rf, gf, bf, af)
end

---Creates a cropped copy of the given texture
-- @param t The source texture
-- @param x The top-left x-coordinate of the crop rectangle.
-- @param y The top-left y-coordinate of the crop rectangle.
-- @param w The crop rectangle's width.
-- @param h The crop rectangle's height.
-- @return A new texture created from the specified pixels in the source texture.
function crop(t, x, y, w, h)
	t = tex(t)
	return ImageFx.crop(t, x, y, w, h)
end

---Creates a new texture by blending together multiple source textures.
-- @param args A table of the form:<br/>
-- <pre>{
--     {tex=myTexture1},
--     {tex=myTexture2, pos={10, 10}}
--     {tex=myTexture3}
-- }</pre><br/>
-- The <code>pos</code> field is optional and assumed <code>{0, 0}</code> when omitted.
-- @param w Width of the output texture
-- @param h Height of the output texture
-- @return A new texture composed of all the argument textures blended together.
function composite(args, w, h)
	for _,entry in ipairs(args) do
		entry.tex = tex(entry.tex)
	end
	return ImageFx.composite(args, w, h)
end

