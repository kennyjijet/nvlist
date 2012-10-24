package nl.weeaboo.vn.impl.lua;

import java.io.FileNotFoundException;
import java.io.IOException;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.lua2.lib.LuaLibrary;
import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.vn.IVideo;
import nl.weeaboo.vn.IVideoState;
import nl.weeaboo.vn.impl.base.BaseNotifier;
import nl.weeaboo.vn.impl.base.BaseVideoFactory;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.Varargs;

@LuaSerializable
public class LuaVideoLib extends LuaLibrary {

	private static final long serialVersionUID = LuaImpl.serialVersionUID;

	private static final String[] NAMES = {
		"movie"
	};

	private static final int INIT  = 0;
	private static final int MOVIE = 1;
	
	private final BaseNotifier notifier;
	private final BaseVideoFactory videoFactory;
	private final IVideoState videoState;

	public LuaVideoLib(BaseNotifier ntf, BaseVideoFactory fac, IVideoState vs) {
		this.notifier = ntf;
		this.videoFactory = fac;
		this.videoState = vs;
	}
	
	@Override
	protected LuaLibrary newInstance() {
		return new LuaVideoLib(notifier, videoFactory, videoState);
	}

	@Override
	public Varargs invoke(Varargs args) {
		switch (opcode) {
		case INIT: return initLibrary("Video", NAMES, 1);
		case MOVIE: return start(args);
		default: return super.invoke(args);
		}
	}
	
	protected Varargs start(Varargs args) {
		String filename = args.tojstring(1);
		
		try {
			IVideo video = videoFactory.movie(filename);
			if (video != null) {
				videoState.add(video, true);
				return LuajavaLib.toUserdata(video, IVideo.class);
			}
		} catch (FileNotFoundException fnfe) {
			notifier.d("Video file not found ("+filename+")");
		} catch (IOException e) {
			throw new LuaError(e);
		}
		return NIL;
	}
			
}
