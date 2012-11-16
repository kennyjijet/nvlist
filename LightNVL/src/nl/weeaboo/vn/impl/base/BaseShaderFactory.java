package nl.weeaboo.vn.impl.base;

import java.io.Serializable;

import nl.weeaboo.common.StringUtil;
import nl.weeaboo.vn.IShaderFactory;

public abstract class BaseShaderFactory implements IShaderFactory, Serializable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	protected final BaseNotifier notifier;
	
	public BaseShaderFactory(BaseNotifier ntf) {
		notifier = ntf;
	}
	
	//Functions
	
	//Getters
	@Override
	public boolean isGLSLVersionSupported(String version) {
		return StringUtil.compareVersion(getGLSLVersion(), version) >= 0;
	}
	
	//Setters
	
}
