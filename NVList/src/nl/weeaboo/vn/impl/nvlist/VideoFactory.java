package nl.weeaboo.vn.impl.nvlist;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2ES1;

import nl.weeaboo.filesystem.FileSystemView;
import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.gl.GLResourceCache;
import nl.weeaboo.gl.PBO;
import nl.weeaboo.gl.shader.ShaderCache;
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

	private final IFileSystem rootFS;
	private final TextureCache texCache;
	//private final ShaderCache shCache;
	private final GLResourceCache resCache;
	private final EnvironmentSerializable es;

	private FileSystemView fs;
	private int videoWidth, videoHeight;
	
	public VideoFactory(IFileSystem fs, TextureCache tc, ShaderCache sc, GLResourceCache rc,
			ISeenLog sl, INotifier ntf)
	{
		super(sl, ntf);
		
		this.rootFS = fs;
		this.fs = new FileSystemView(fs, "video/", true);
		this.texCache = tc;
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
		
	public GLGeneratedTexture newTexture(int[] argb, int w, int h,
			int glMinFilter, int glMagFilter, int glWrap)
	{
		return texCache.newTexture(argb, w, h, glMinFilter, glMagFilter, glWrap);
	}
	
	public PBO createPBO(GL2ES1 gl) {
		return resCache.createPBO(gl);
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
