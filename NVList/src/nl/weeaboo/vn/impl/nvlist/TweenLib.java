package nl.weeaboo.vn.impl.nvlist;

import java.io.ObjectStreamException;

import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IImageFactory;
import nl.weeaboo.vn.IImageTween;
import nl.weeaboo.vn.IInterpolator;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.impl.base.CrossFadeTween;
import nl.weeaboo.vn.impl.lua.LuaTweenLib;

@LuaSerializable
public class TweenLib extends LuaTweenLib {

	private final ImageFactory fac;
	private final EnvironmentSerializable es;
	
	public TweenLib(ImageFactory fac, INotifier ntf) {
		super(fac, ntf);
		
		this.fac = fac;
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
	protected IImageTween newBitmapTween(IImageFactory fac, INotifier ntf, String fadeFilename,
			double duration, double range, IInterpolator i, boolean fadeTexTile)
	{
		return new BitmapTween((ImageFactory)fac, ntf, fadeFilename, duration, range, i, fadeTexTile);
	}

	//Getters
	@Override
	public boolean isCrossFadeTweenAvailable() {
		for (String ext : BlendQuadRenderer.REQUIRED_EXTENSIONS) {
			if (!fac.isGLExtensionAvailable(ext)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isBitmapTweenAvailable() {
		return BitmapTween.isAvailable(fac.getGlslVersion());
	}
	
	//Setters
	
}
