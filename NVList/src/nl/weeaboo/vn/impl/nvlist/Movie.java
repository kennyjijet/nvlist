package nl.weeaboo.vn.impl.nvlist;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.IntBuffer;
import java.util.concurrent.ThreadFactory;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;

import nl.weeaboo.game.GameLog;
import nl.weeaboo.gl.GLDraw;
import nl.weeaboo.gl.GLInfo;
import nl.weeaboo.gl.GLManager;
import nl.weeaboo.gl.GLUtil;
import nl.weeaboo.gl.PBO;
import nl.weeaboo.gl.jogl.JoglGLManager;
import nl.weeaboo.gl.tex.GLTexRect;
import nl.weeaboo.gl.tex.GLTexture;
import nl.weeaboo.gl.tex.GLWritableTexture;
import nl.weeaboo.gl.tex.ITextureData;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.ogg.StreamUtil;
import nl.weeaboo.ogg.player.Player;
import nl.weeaboo.ogg.player.PlayerListener;
import nl.weeaboo.ogg.player.RGBVideoSink;
import nl.weeaboo.ogg.player.VideoSink;
import nl.weeaboo.ogg.player.YUVVideoSink;
import nl.weeaboo.vn.impl.base.BaseVideo;

import com.fluendo.jheora.YUVBuffer;

@LuaSerializable
public final class Movie extends BaseVideo {

	private static final long serialVersionUID = NVListImpl.serialVersionUID;

	private final VideoFactory vfac;
	private final String filename;
	
	private transient GLWritableTexture[] textures;
	private transient int readIndex;
	private transient PBO pbo;
	private transient Player player;
	private transient VideoSink videoSink;
	
	public Movie(VideoFactory vfac, String filename) {
		this.vfac = vfac;
		this.filename = filename;		
	}
	
	//Functions
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		//Restart automatically when loaded
		if (!isStopped()) {
			_start();
			if (isPaused()) {
				_pause();
			}
		}
	}	
	
	protected void createPlayer() throws IOException {
		//videoSink = new RGBVideoSink();		
		videoSink = new YUVVideoSink();
		
		player = new Player(new PlayerListener() {
			public void onPauseChanged(boolean p) {
				//Ignore
			}
			public void onTimeChanged(double t, double et, double frac) {
			}
		}, videoSink, new ThreadFactory() {
			int id = 0;
			
			public Thread newThread(Runnable r) {
				return new Thread(r, "VideoPlayer-" + (++id));
			}
		});				
		
		InputStream in = vfac.getVideoInputStream(filename);
		player.setInput(StreamUtil.getOggInput(in));
				
		int w = player.getWidth();
		int h = player.getHeight();
		double fps = player.getFPS();
		
		GameLog.v(String.format("Starting video playback: %dx%d %.2ffps", w, h, fps));
		
		if (w > 1280 || h > 720) {
			GameLog.d("Video sizes over 1280x720 aren't recommended, video decoding is too slow");
		}
	}
	
	protected void cleanupGL() {
		if (pbo != null) {
			pbo.glUnload();
			pbo = null;
		}
		
		if (textures != null) {
			for (GLTexture tex : textures) {
				if (tex != null) {
					tex.glUnload();
				}
			}
			textures = null;			
		}
	}
	
	@Override
	protected void _prepare() throws IOException {
		if (player == null) {
			createPlayer();
		}
	}
	
	@Override
	protected void _start() throws IOException {
		if (player == null) {
			createPlayer();
		}		
		player.start();
	}

	@Override
	protected void _stop() {
		if (player != null) {
			player.stop();
			
			cleanupGL();
		}
	}

	@Override
	protected void _pause() {
		if (player != null) {
			player.setPaused(true);
		}
	}
	
	@Override
	protected void _resume() {
		if (player != null) {
			player.setPaused(false);
		}
	}
	
	@Override
	protected void onVolumeChanged() {
		if (player != null) {
			player.setVolume(getVolume());
		}
	}
	
	public void draw(GLManager glm, int drawW, int drawH) {
		if (player == null || player.isEnded()) {
			return;
		}

		GLDraw glDraw = glm.getGLDraw();
		int w = player.getWidth();
		int h = player.getHeight();
		
		if (textures == null) {
			textures = new GLWritableTexture[2];
		}
		
		IntBuffer rgbPixels = null;
		YUVBuffer yuvPixels = null;
		if (videoSink instanceof YUVVideoSink) {
			YUVVideoSink vs = (YUVVideoSink)videoSink;
			yuvPixels = vs.get();			
		} else {
			RGBVideoSink vs = (RGBVideoSink)videoSink;
			rgbPixels = vs.get();
		}
		
		if ((yuvPixels != null || rgbPixels != null) && w > 0 && h > 0) {
			readIndex = (readIndex + 1) % textures.length;			

			GLWritableTexture writeTex = textures[(readIndex + 1) % textures.length];
			GLTexRect writeRect = (writeTex != null ? writeTex.getSubRect(null) : null);
			if (writeRect != null && (writeRect.getWidth() != w || writeRect.getHeight() != h)) {
				writeTex.glUnload();
				writeTex = null;
			}
				
			if (writeTex == null) {
				writeTex = vfac.newTexture(w, h, 0, 0, 0, 0);
				textures[(readIndex + 1) % textures.length] = writeTex;
			}
			writeTex.glTryLoad(glm);

			if (yuvPixels != null && rgbPixels == null) {
				//synchronized block prevent tearing and flushes buffered updates to YUVBuffer from other threads.
				synchronized (yuvPixels) {
					rgbPixels = ((YUVVideoSink)videoSink).convertToRGB(yuvPixels);
				}													
			}
			
			if (!uploadPixelsPBO(glm, rgbPixels, w, h, writeTex)) { // Try RGB async			
				uploadPixels(glm, rgbPixels, w, h, writeTex); //Try RGB
			}
		}

		GLTexture readTex = textures[readIndex];
		if (readTex != null && readTex.glId() != 0) {
			glDraw.setTexture(readTex);
			glDraw.draw(readTex.getSubRect(null), 0, 0, drawW, drawH);
			glDraw.setTexture(null);
		}		
	}
	
	private boolean uploadPixelsPBO(GLManager glm, IntBuffer pixels, int w, int h,
			GLWritableTexture writeTex)
	{
		GLDraw glDraw = glm.getGLDraw();
		GLInfo glInfo = glm.getGLInfo();
		GL2ES1 gl = JoglGLManager.getGL(glm);
		if (!gl.isGL2ES2()) {
			return false;
		}
				
		if (pbo == null || pbo.glId() == 0) {
			//Init PBO
			pbo = vfac.newPBO();
			if (pbo == null || pbo.glId() == 0) {
				return false;
			}
		}
				
		//long t0 = System.nanoTime();
		
		pbo.bindUpload(glm);
		try {
			int glInternalFormat, glFormat, glType;
			{
				glInternalFormat = GL.GL_RGBA;
				glFormat = glInfo.getDefaultPixelFormatARGB();
				glType = glInfo.getDefaultPixelTypeARGB();

				if (glFormat != GL2.GL_BGRA) {
					//Should never happen, BGRA is preferred and should always be supported when PBO's are available
					GLUtil.swapRedBlue(pixels, pixels);
				}
			}
			/* else {
				glInternalFormat = GL.GL_LUMINANCE;
				glFormat = GL.GL_LUMINANCE;
				glType = GL.GL_UNSIGNED_BYTE;
				YUVBuffer ybuf = (YUVBuffer)pixels;
				pixelData = ShortBuffer.wrap(ybuf.data);
				System.out.println(ybuf.data + " " + (w*h) + " " + GLTexUtil.getBytesPerPixel(glFormat, glType));
			}*/
			pbo.setData(glm, pixels, w * h * GLUtil.getBytesPerPixel(glFormat, glType));				
						
			//Stream PBO data to texture
			glDraw.setTexture(writeTex);
			gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, glInternalFormat, w, h, 0, glFormat, glType, 0);
			glDraw.setTexture(null);			
		} finally {
			pbo.unbind(glm);
		}

		//long t1 = System.nanoTime();
		//System.out.printf("%.2fms\n", (t1-t0)/1000000.0);
		
		return true;
	}
	
	protected void uploadPixels(GLManager glm, IntBuffer pixels, int w, int h,
			GLWritableTexture writeTex)
	{
		ITextureData tdata = vfac.newTextureData(pixels, w, h);
		writeTex.setPixels(tdata);
		writeTex.glTryLoad(glm);
	}
	
	//Getters
	@Override
	public boolean isStopped() {
		if (player != null && player.isEnded()) {
			//System.out.println("STOP " + this);
			stop();
		}
		return super.isStopped();
	}
	
	//Setters
	
}
