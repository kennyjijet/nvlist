package nl.weeaboo.vn;

import java.io.IOException;

public interface ISeenLog {

	//Functions	
	public void load() throws IOException;
	public void save() throws IOException;

	public void registerScriptFile(String filename, int numTextLines);
	public void addImage(String filename);
	public void addSound(String filename);
	public void addVideo(String filename);
	
	//Getters
	public boolean isChoiceSelected(String choiceId, int index);
	public boolean isTextLineRead(String filename, int textLineIndex);
	public boolean hasImage(String filename);
	public boolean hasSound(String filename);
	public boolean hasVideo(String filename);
	
	//Setters
	public void setChoiceSelected(String choiceId, int index);
	public void setTextLineRead(String filename, int textLineIndex);
	
}
