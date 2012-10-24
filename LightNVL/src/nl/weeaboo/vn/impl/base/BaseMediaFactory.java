package nl.weeaboo.vn.impl.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import nl.weeaboo.collections.LRUSet;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.ISeenLog;

abstract class BaseMediaFactory implements Serializable {
	
	private static final long serialVersionUID = BaseImpl.serialVersionUID;

	private String[] defaultExts;
	
	protected final ISeenLog seenLog;
	protected final INotifier notifier;
	
	private final Set<String> checkedFilenames;
	private boolean checkFileExt;
	
	public BaseMediaFactory(String[] defaultExts, ISeenLog sl, INotifier ntf) {
		this.defaultExts = defaultExts.clone();
		
		this.seenLog = sl;
		this.notifier = ntf;
		
		this.checkedFilenames = new LRUSet<String>(128);
	}
	
	//Functions
	protected String replaceExt(String filename, String ext) {
		return BaseImpl.replaceExt(filename, ext);
	}
	
	protected String normalizeFilename(String filename) {
		if (filename == null) return null;
		
		if (isValidFilename(filename)) {
			return filename; //The given extension works
		}
		
		for (String ext : defaultExts) {
			String fn = replaceExt(filename, ext);
			if (isValidFilename(fn)) {
				return fn; //This extension works
			}
		}
		
		return null;
	}

	protected void checkRedundantFileExt(String filename) {
		if (filename == null || !checkFileExt) {
			return;
		}
		
		if (!checkedFilenames.add(filename)) {
			return;
		}
		
		//Check if a file extension in the default list has been specified.
		for (String ext : defaultExts) {
			if (filename.endsWith("." + ext)) {
				if (isValidFilename(filename)) {
					notifier.d("You don't need to specify the file extension: " + filename);
				} else if (isValidFilename(normalizeFilename(filename))) {
					notifier.w("Incorrect file extension: " + filename);
				}
				break;
			}
		}		
	}
	
	public void preload(String filename) {
		preload(filename, false);
	}
	
	public void preload(String filename, boolean suppressErrors) {
		if (!suppressErrors) {
			checkRedundantFileExt(filename);
		}

		String normalized = normalizeFilename(filename);
		if (normalized != null) {
			preloadNormalized(normalized);
		}
	}	
	
	protected abstract void preloadNormalized(String filename);
	
	//Getters
	/**
	 * @param filename A normalized filename
	 */
	protected abstract boolean isValidFilename(String filename);
	
	protected Collection<String> getMediaFiles(String folder) {
		Collection<String> files = getFiles(folder);
		List<String> filtered = new ArrayList<String>(files.size());
		for (String file : files) {
			if (isValidFilename(file)) {
				filtered.add(file);
			}
		}
		return filtered;
	}
	
	protected abstract Collection<String> getFiles(String folder);
	
	public boolean getCheckForWrongFileExt() {
		return checkFileExt;
	}
	
	//Setters
	public void setDefaultExts(String... exts) {
		defaultExts = exts.clone();
	}
	
	public void setCheckFileExt(boolean check) {
		checkFileExt = check;
	}
	
}
