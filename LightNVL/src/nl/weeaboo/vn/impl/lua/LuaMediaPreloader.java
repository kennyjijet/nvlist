package nl.weeaboo.vn.impl.lua;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IMediaPreloader;
import nl.weeaboo.vn.MediaFile;
import nl.weeaboo.vn.MediaFile.MediaType;
import nl.weeaboo.vn.impl.base.BaseImageFactory;
import nl.weeaboo.vn.impl.base.BaseSoundFactory;
import nl.weeaboo.vn.impl.base.PreloaderData;
import nl.weeaboo.vn.parser.ParserUtil;

@LuaSerializable
public final class LuaMediaPreloader extends EnvironmentSerializable implements IMediaPreloader {

	protected final BaseImageFactory imgfac;
	protected final BaseSoundFactory sndfac;
	
	private PreloaderData preloaderData;
	private int lookAhead;
	private int maxItemsPerLine;
	private float branchLookAheadScale;
	private String lastCallSite;
	
	public LuaMediaPreloader(BaseImageFactory imgfac, BaseSoundFactory sndfac) {
		this.imgfac = imgfac;
		this.sndfac = sndfac;
		
		preloaderData = new PreloaderData();
		lookAhead = 30;
		maxItemsPerLine = 2;
		branchLookAheadScale = .5f;
		lastCallSite = null;
	}
	
	//Functions
	public void clear() {
		preloaderData = new PreloaderData();
		lastCallSite = null;
	}
	
	public void update(String callSite) {
		if (lookAhead <= 0) {
			return; //Nothing to do
		}

		if (callSite != null && !callSite.equals(lastCallSite)) {
			// Callsite exists and is different from last frame (prevents
			// hammering the same preloads over and over when idle).
			
			String script = ParserUtil.getSrclocFilename(callSite);
			int nextLine = ParserUtil.getSrclocLine(callSite) + 1;
			
			List<MediaFile> future = new ArrayList<MediaFile>(8);
			getBranchingFutureMedia(future, script, nextLine, nextLine + lookAhead, 0f,
					10 * maxItemsPerLine); 
			
			int t = 0;
			for (MediaFile mf : future) {
				if (t >= maxItemsPerLine) break;
				
				//System.out.println(mf);
				
				MediaType type = mf.getType();
				if (type == MediaType.IMAGE) {
					imgfac.preload(mf.getFilename(), true);
					t++;
				} else if (type == MediaType.SOUND) {
					sndfac.preload(mf.getFilename(), true);
					t++;
				}
			}
		}
		lastCallSite = callSite;
	}
	
	public void load(InputStream in) throws IOException {
		ObjectInputStream oin = new ObjectInputStream(in);
		try {
			preloaderData.addAll((PreloaderData)oin.readObject());
		} catch (ClassNotFoundException e) {
			throw new IOException(e.getMessage());
		}
	}
	
	//Getters
	protected void getBranchingFutureMedia(List<? super MediaFile> out, String filename,
			int startLine, int endLine, float minProbability, int maxResults)
	{
		//System.out.println("FUTURE");
		
		PriorityQueue<RelativeMediaFile> q = new PriorityQueue<RelativeMediaFile>(32);
		enqueueFuture(q, filename, startLine, endLine, minProbability, 0);
		
		int numResults = 0;
		
		List<RelativeMediaFile> jumpBuffer = new ArrayList<RelativeMediaFile>();
		while (!q.isEmpty() && numResults < maxResults) {
			RelativeMediaFile rmf = q.remove();
			MediaFile mf = rmf.file;
			MediaType type = mf.getType();
			
			//System.out.println("  " + mf);
			
			if (type == MediaType.SCRIPT) {
				jumpBuffer.add(rmf);
			} else {
				out.add(mf);
				numResults++;
			}
			
			if (type != MediaType.SCRIPT || q.isEmpty()) {
				for (RelativeMediaFile jump : jumpBuffer) {
					int baseLookAhead = endLine-rmf.distance;
					
					MediaFile jumpFile = jump.file;
					String script = ParserUtil.getSrclocFilename(jumpFile.getFilename());
					
					int sl = ParserUtil.getSrclocLine(jumpFile.getFilename());
					
					float p = jumpFile.getProbability();
					int range = Math.min(baseLookAhead-1, Math.round(branchLookAheadScale * p * baseLookAhead));
					int el = sl + range;
					
					enqueueFuture(q, script, sl, el, minProbability, rmf.distance);
				}
				jumpBuffer.clear();
			}
		}
	}
	
	private void enqueueFuture(PriorityQueue<RelativeMediaFile> out, String filename,
			int startLine, int endLine, float minProbability, int baseDistance)
	{
		if (endLine <= startLine) return;
		
		//System.out.println("-branch: " + filename + ":" + startLine + "-" + endLine + " off=" + baseDistance);
		for (MediaFile mf : getFutureMedia(filename, startLine, endLine, minProbability)) {
			out.add(new RelativeMediaFile(mf.getLine() - startLine + baseDistance, mf));
		}
	}
	
	@Override
	public MediaFile[] getFutureMedia(String filename, int startLine, int endLine, float minProbability) {
		return preloaderData.getFutureMedia(filename, startLine, endLine, minProbability);
	}
	
	public int getLookAhead() {
		return lookAhead;
	}

	public int getMaxItemsPerLine() {
		return maxItemsPerLine;
	}
	
	//Setters
	public void setLookAhead(int lines) {
		lookAhead = lines;
	}
	public void setMaxItemsPerLine(int max) {
		maxItemsPerLine = max;
	}
	
	//Inner Classes
	private static class RelativeMediaFile implements Comparable<RelativeMediaFile> {
		public final int distance;
		public final MediaFile file;
		
		public RelativeMediaFile(int distance, MediaFile file) {
			this.distance = distance;
			this.file = file;
		}

		@Override
		public int compareTo(RelativeMediaFile m) {
			if (m == null) return -1;
			
			if (distance < m.distance) return -1;
			if (distance > m.distance) return 1;
			
			if (file.getProbability() > m.file.getProbability()) return -1;
			if (file.getProbability() < m.file.getProbability()) return 1;
			
			int c = file.getType().compareTo(m.file.getType());
			if (c != 0) return c;
			
			return file.getFilename().compareTo(m.file.getFilename());
		}
	}
	
}
