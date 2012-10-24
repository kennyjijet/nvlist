package nl.weeaboo.vn.impl.lua;

import static nl.weeaboo.vn.impl.lua.LuaSaveHandler.AUTO_SAVE_OFFSET;
import static nl.weeaboo.vn.impl.lua.LuaSaveHandler.QUICK_SAVE_OFFSET;
import static nl.weeaboo.vn.impl.lua.LuaSaveHandler.isAutoSaveSlot;
import static nl.weeaboo.vn.impl.lua.LuaSaveHandler.isQuickSaveSlot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import nl.weeaboo.vn.ISaveInfo;
import nl.weeaboo.vn.IScreenshot;
import nl.weeaboo.vn.IStorage;
import nl.weeaboo.vn.impl.base.BaseStorage;

public class SaveInfo implements ISaveInfo {

	private final int slot;
	long timestamp;
	IScreenshot screenshot;
	IStorage metaData;
	
	SaveInfo(int slot) {
		this.slot = slot;
		this.metaData = new BaseStorage();
	}
	
	//Functions	
	//Getters
	@Override
	public int getSlot() {
		return slot;
	}

	@Override
	public String getTitle() {
		String s = "save";
		int offset = 0;
		if (isQuickSaveSlot(slot)) {
			s = "quicksave";
			offset = QUICK_SAVE_OFFSET;
		} else if (isAutoSaveSlot(slot)) {
			s = "autosave";
			offset = AUTO_SAVE_OFFSET;
		}
		return String.format("%s %d", s, getSlot()-offset);
	}

	@Override
	public String getLabel() {		
		return String.format("%s\n%s", getTitle(), getDateString());
	}
	
	@Override
	public long getTimestamp() {
		return timestamp;
	}
	
	@Override
	public String getDateString() {
		String dateString = "";
		if (timestamp != 0) {
			Date date = new Date(timestamp);
			DateFormat dateFormat;
			if (isQuickSaveSlot(slot) || isAutoSaveSlot(slot)) {
				dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			} else {
				dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			}
			dateString = dateFormat.format(date);
		}
		return dateString;
	}

	@Override
	public IScreenshot getScreenshot() {
		return screenshot;
	}
	
	@Override
	public IStorage getMetaData() {
		return metaData;
	}
	
	//Setters
	
}
