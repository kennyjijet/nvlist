package nl.weeaboo.vn.impl.base;

import java.io.IOException;
import java.io.Serializable;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.ISeenLog;

@LuaSerializable
public final class NullSeenLog implements ISeenLog, Serializable {
	
	private static final long serialVersionUID = BaseImpl.serialVersionUID;

	//Functions
	@Override
	public void load() throws IOException {
		//Do nothing
	}

	@Override
	public void save() throws IOException {
		//Do nothing
	}
	
	@Override
	public void registerScriptFile(String filename, int numTextLine) {
	}

	@Override
	public void addImage(String filename) {
	}

	@Override
	public void addSound(String filename) {
	}

	@Override
	public void addVideo(String filename) {
	}
	
	//Getters
	@Override
	public boolean isChoiceSelected(String choiceId, int index) {
		return false;
	}

	@Override
	public boolean isTextLineRead(String filename, int textLineIndex) {
		return false;
	}
	
	@Override
	public boolean hasImage(String filename) {
		return false;
	}

	@Override
	public boolean hasSound(String filename) {
		return false;
	}

	@Override
	public boolean hasVideo(String filename) {
		return false;
	}
	
	//Setters
	@Override
	public void setChoiceSelected(String choiceId, int index) {
	}

	@Override
	public void setTextLineRead(String filename, int textLineIndex) {
	}
	
}
