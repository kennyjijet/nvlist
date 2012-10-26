package nl.weeaboo.vn.impl.base;

import java.io.Serializable;

import nl.weeaboo.vn.IGUIFactory;
import nl.weeaboo.vn.INotifier;

public abstract class BaseGUIFactory implements IGUIFactory, Serializable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;

	protected final INotifier notifier;
	
	public BaseGUIFactory(BaseImageFactory imgfac, INotifier ntf) {
		notifier = ntf;
	}
	
	//Functions
	
	//Getters
	
	//Setters
	
}
