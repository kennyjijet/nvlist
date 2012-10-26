package nl.weeaboo.vn.impl.nvlist;

import java.io.ObjectStreamException;
import java.io.Serializable;

import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IChoice;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.ISaveLoadScreen;
import nl.weeaboo.vn.impl.base.BaseGUIFactory;

@LuaSerializable
public class GUIFactory extends BaseGUIFactory implements Serializable {

	private final EnvironmentSerializable es;
	
	public GUIFactory(ImageFactory imgfac, INotifier ntf) {
		super(imgfac, ntf);
		
		this.es = new EnvironmentSerializable(this);
	}
	
	//Functions
	private Object writeReplace() throws ObjectStreamException {	
		return es.writeReplace();
	}
	
	@Override
	public Panel createPanel() {
		return new Panel();
	}
	
	@Override
	public IChoice createChoice(String... options) {
		return null;
	}

	@Override
	public ISaveLoadScreen createSaveScreen() {
		return null;
	}

	@Override
	public ISaveLoadScreen createLoadScreen() {
		return null;
	}
	
	//Getters
	
	//Setters
	
}
