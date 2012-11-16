package nl.weeaboo.vn.impl.nvlist;

import java.io.ObjectStreamException;

import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IImageTween;
import nl.weeaboo.vn.IInterpolator;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.impl.base.CrossFadeTween;
import nl.weeaboo.vn.impl.lua.LuaTweenLib;

@LuaSerializable
public class TweenLib extends LuaTweenLib {

	private final ImageFactory imgfac;
	private final ShaderFactory shfac;
	private final EnvironmentSerializable es;
	
	public TweenLib(INotifier ntf, ImageFactory imgfac, ShaderFactory shfac) {
		super(ntf, imgfac, shfac);
		
		this.imgfac = imgfac;
		this.shfac = shfac;
		this.es = new EnvironmentSerializable(this);
	}

	//Functions
	private Object writeReplace() throws ObjectStreamException {	
		return es.writeReplace();
	}
	
	@Override
	protected IImageTween newCrossFadeTween(double duration, IInterpolator i) {
		return new CrossFadeTween(duration, i);
	}

	@Override
	protected IImageTween newBitmapTween(String fadeFilename, double duration, double range, IInterpolator i,
			boolean fadeTexTile)
	{
		return new BitmapTween(notifier, imgfac, shfac, fadeFilename, duration, range, i, fadeTexTile);
	}

	//Getters
	@Override
	public boolean isCrossFadeTweenAvailable() {
		for (String ext : BlendQuadRenderer.REQUIRED_EXTENSIONS) {
			if (!imgfac.isGLExtensionAvailable(ext)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isBitmapTweenAvailable() {
		return BitmapTween.isAvailable(shfac);
	}
	
	//Setters
	
}
