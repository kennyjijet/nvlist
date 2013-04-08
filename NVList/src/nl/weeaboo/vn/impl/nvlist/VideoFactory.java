package nl.weeaboo.vn.impl.nvlist;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import nl.weeaboo.filesystem.FileSystemView;
import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.gl.GLResCache;
import nl.weeaboo.gl.PBO;
import nl.weeaboo.gl.shader.IShaderStore;
import nl.weeaboo.gl.tex.GLWritableTexture;
import nl.weeaboo.gl.tex.ITextureData;
import nl.weeaboo.gl.tex.ITextureStore;
import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.ISeenLog;
import nl.weeaboo.vn.IVideo;
import nl.weeaboo.vn.impl.base.BaseVideoFactory;

@LuaSerializable
public class VideoFactory extends BaseVideoFactory implements Serializable {

	private final IFileSystem rootFS;
	private final ITextureStore texStore;
	//private final ShaderCache shCache;
	private final GLResCache resCache;
	private final EnvironmentSerializable es;

	private FileSystemView fs;
	private int videoWidth, videoHeight;
	
	public VideoFactory(IFileSystem fs, ITextureStore ts, IShaderStore ss, GLResCache rc,
			ISeenLog sl, INotifier ntf)
	{
		super(sl, ntf);
		
		this.rootFS = fs;
		this.fs = new FileSystemView(fs, "video/", true);
		this.texStore = ts;
		//this.shCache = sc;
		this.resCache = rc;
		
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
		
	public GLWritableTexture newTexture(int w, int h,
			int glMinFilter, int glMagFilter, int glWrapS, int glWrapT)
	{
		return texStore.newWritableTexture(w, h, glMinFilter, glMagFilter, glWrapS, glWrapT);
	}
	
	public ITextureData newTextureData(IntBuffer argb, int w, int h) {
		return texStore.newARGB8TextureData(argb, true, w, h);
	}
	
	public PBO newPBO() {
		return resCache.newPBO();
	}
	
	protected void onVideoFolderChanged(int w, int h) {
		videoWidth = w;
		videoHeight = h;
	}
	
	//Getters
	InputStream getVideoInputStream(String filename) throws IOException {
		return fs.newInputStream(filename);
	}
	
	@Override
	protected boolean isValidFilename(String filename) {
		if (filename == null) return false;

		return fs.getFileExists(filename);
	}

	@Override
	protected List<String> getFiles(String folder) {
		List<String> out = new ArrayList<String>();
		try {
			fs.getFiles(out, folder, true);
		} catch (IOException e) {
			notifier.d("Folder doesn't exist or can't be read: " + folder, e);
		}
		return out;
	}
	
	//Setters
	public void setVideoFolder(String folder, int w, int h) {
		if (!folder.endsWith("/")) {
			folder += "/";
		}
		
		if (!fs.getPrefix().equals(folder) || videoWidth != w || videoHeight != h) {
			fs = new FileSystemView(rootFS, folder, true);
			onVideoFolderChanged(w, h);
		}
	}

}
