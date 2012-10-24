package nl.weeaboo.vn.impl.base;

import nl.weeaboo.settings.INIFile;
import nl.weeaboo.vn.INovelConfig;

public class BaseNovelConfig implements INovelConfig {

	private final String title;
    private final int width;
    private final int height;
    
	public BaseNovelConfig(INIFile file) {
		this(file.getString("title", null),
				file.getInt("width", 800), file.getInt("height", 600));
	}
    public BaseNovelConfig(String title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;
    }
	
	//Functions
	
	//Getters
	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}
	
	//Setters
	
}
