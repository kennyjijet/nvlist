package nl.weeaboo.vn.impl.nvlist;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import javax.media.opengl.GL2ES1;

import nl.weeaboo.filemanager.FileManager;
import nl.weeaboo.gl.GLResourceCache;
import nl.weeaboo.gl.PBO;
import nl.weeaboo.gl.texture.GLGeneratedTexture;
import nl.weeaboo.gl.texture.TextureCache;
import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.ISeenLog;
import nl.weeaboo.vn.IVideo;
import nl.weeaboo.vn.impl.base.BaseVideoFactory;

@LuaSerializable
public class VideoFactory extends BaseVideoFactory implements Serializable {

	private final FileManager fm;
	private final TextureCache texCache;
	private final GLResourceCache resCache;
	private final EnvironmentSerializable es;

	private String pathPrefix;
	private int videoWidth, videoHeight;
	
	public VideoFactory(FileManager fm, TextureCache tc, GLResourceCache rc,
			ISeenLog sl, INotifier ntf)
	{
		super(sl, ntf);
		
		this.fm = fm;
		this.texCache = tc;
		this.resCache = rc;
		this.pathPrefix = "video/";
		
		this.es = new EnvironmentSerializable(this);
	}
	
	//Functions
	private Object writeReplace() throws ObjectStreamException {	
		return es.writeReplace();
	}
	
	@Override
	protected void preloadNormalized(String filename) {
		//We stream video's directly from disk, nothing to preload...
	}
	
	@Override
	protected IVideo movieNormalized(String filename) throws IOException {
		Movie movie = new Movie(this, filename);
		movie.start();
		return movie;
	}
		
	public GLGeneratedTexture newTexture(int[] argb, int w, int h,
			int glMinFilter, int glMagFilter, int glWrap)
	{
		return texCache.newTexture(argb, w, h, glMinFilter, glMagFilter, glWrap);
	}
	
	public PBO createPBO(GL2ES1 gl) {
		return resCache.createPBO(gl);
	}
	
	protected void onVideoFolderChanged(String folder, int w, int h) {
		pathPrefix = folder;
		videoWidth = w;
		videoHeight = h;
	}
	
	//Getters
	InputStream getVideoInputStream(String filename) throws IOException {
		return fm.getInputStream(pathPrefix + filename);
	}
	
	@Override
	protected boolean isValidFilename(String filename) {
		if (filename == null) return false;

		return fm.getFileExists(pathPrefix + filename);
	}

	@Override
	protected Collection<String> getFiles(String folder) {
		try {
			return fm.getFolderContents(pathPrefix, true);
		} catch (IOException e) {
			notifier.d("Folder doesn't exist or can't be read: " + folder, e);
		}
		return Collections.emptyList();
	}
	
	//Setters
	public void setVideoFolder(String folder, int w, int h) {
		if (!folder.endsWith("/")) {
			folder += "/";
		}
		
		if (!pathPrefix.equals(folder) || videoWidth != w || videoHeight != h) {
			pathPrefix = folder;
			onVideoFolderChanged(pathPrefix, w, h);
		}
	}

}
