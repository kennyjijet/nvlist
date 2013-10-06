package nl.weeaboo.vn.impl.base;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import nl.weeaboo.collections.LRUSet;
import nl.weeaboo.io.ByteChunkOutputStream;
import nl.weeaboo.io.StreamUtil;
import nl.weeaboo.vn.IAnalytics;
import nl.weeaboo.vn.MediaFile.MediaType;
import nl.weeaboo.vn.parser.ParserUtil;

public abstract class BaseLoggingAnalytics implements IAnalytics {

	public enum EventType {
		LINE("line"),
		COMPILE_SCRIPT("compile_script"),
		JUMP("jump"),
		LOAD_IMAGE("load_image"),
		LOAD_SOUND("load_sound");
		
		private static final EventType[] values = values();
		private final String label;
		
		private EventType(String lbl) {
			label = lbl;
		}
		
		public static EventType fromString(String lbl) {
			for (EventType et : values) {
				if (et.label.equals(lbl)) {
					return et;
				}
			}
			return null;
		}
		
		@Override
		public String toString() {
			return label;
		}
	}
	
	private static final int VERSION = 1001;

	private final String filename;
	private boolean compressed = true;
	private long timestamp;
	
	private LRUSet<String> filenamePool;
	private int maxStoredEvents;
	private long optimizeLogTriggerSize; //When the log becomes this size, try to optimize it
	
	private List<Event> log;
	private String lastScript;
	private int lastStart, lastEnd;
	
	protected BaseLoggingAnalytics(String filename) {
		this.filename = filename;
		
		filenamePool = new LRUSet<String>(256);
		maxStoredEvents = 8192;
		optimizeLogTriggerSize = 512<<10; //512KiB
		
		reset0();
	}
	
	//Functions
	protected void reset() {
		reset0();
	}
	
	private void reset0() {
		log = new ArrayList<Event>(32);
		lastScript = null;
		lastStart = lastEnd = 1;
	}
			
	@Override
	public void load() throws IOException {
	}
	
	protected List<Event> loadEvents() throws IOException {
		List<Event> results = new ArrayList<Event>();
		
		InputStream raw = null;
		DataInputStream din = null;
		try {
			raw = new BufferedInputStream(openInputStream(filename), 8<<10);
			while (true) {
				din = new DataInputStream(raw);
				int version = din.readInt();
				int length = din.readInt();

				if (version != VERSION) {
					StreamUtil.forceSkip(raw, length);
				} else if (length < 0 || length > (4<<20)) {
					throw new IOException("Invalid chunk length: " + length);
				} else {
					byte[] bytes = new byte[length];
					din.readFully(bytes);
					ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
					
					din = new DataInputStream(bin);
					boolean compressed = din.readBoolean();
					@SuppressWarnings("unused")
					long timestamp = din.readLong();
					
					ObjectInputStream oin;
					if (compressed) {
						oin = new ObjectInputStream(new InflaterInputStream(bin));
					} else {
						oin = new ObjectInputStream(bin);
					}
					
					int count = oin.readInt();
					//System.err.println(" >>> " + count);
					for (int n = 0; n < count; n++) {
						results.add((Event)oin.readObject());
					}
					oin.close();
				}
			}
		} catch (ClassNotFoundException e) {
			throw new IOException("Class not found: " + e);
		} catch (FileNotFoundException fnfe) {
			//Ignore
		} catch (EOFException eofe) {
			//Ignore
		} finally {
			if (raw != null) raw.close();
			if (din != null) din.close();
		}
		
		return results;
	}
		
	@Override
	public final void save() throws IOException {		
		timestamp = System.currentTimeMillis();
		
		//Serialize analytics
		long logSize = 0;
		if (!log.isEmpty()) {
			logSize = saveEvents(log, true);
			log.clear();
		}
		if (logSize >= optimizeLogTriggerSize) {
			optimizeLog(true);
		}
	}
	
	protected long saveEvents(Collection<? extends Event> events, boolean append) throws IOException {
		ByteChunkOutputStream bout = new ByteChunkOutputStream();
		OutputStream out = bout;
		ObjectOutputStream oout = null;
		try {
			if (compressed) {
				out = new DeflaterOutputStream(out);
			}
			
			oout = new ObjectOutputStream(out);

			int count = events.size();
			oout.writeInt(count);
			for (Event e : events) {
				oout.writeObject(e);
			}
		} finally {
			if (oout != null) oout.close();
			else if (out != null) out.close();
		}
		
		//Append analytics
		out = openOutputStream(filename, append);
		try {
			DataOutputStream dout = new DataOutputStream(out);
			dout.writeInt(VERSION);
			dout.writeInt(bout.size() + 9); //Number of bytes to skip to get to the next chunk
			dout.writeBoolean(compressed);
			dout.writeLong(timestamp);			
			bout.writeContentsTo(out);
		} finally {
			out.close();
		}
		
		bout.close();		
		return getFileSize(filename);
	}
	
	public void optimizeLog(boolean writePreloaderData) throws IOException {
		List<Event> events = loadEvents();
		events = optimizeEvents(events); //Makes sure no outdated events exist in the log
		saveEvents(events, false); //Overwrite analytics with optimized version
		
		//Generate preloader info
		PreloaderData pd = new PreloaderData();
		generatePreloaderData(pd, events);
		
		//Save preloader info
		ObjectOutputStream oout = null;
		OutputStream raw = new BufferedOutputStream(openOutputStream("preloader.bin", false), 8<<10);			
		try {
			oout = new ObjectOutputStream(raw);
			oout.writeObject(pd);
			oout.flush();
		} finally {
			if (oout != null) oout.close();
			else if (raw != null) raw.close();
		}
	}
	
	private List<Event> optimizeEvents(List<Event> events) {
		Map<String, Long> modificationTimes = new HashMap<String, Long>();

		List<Event> result = new ArrayList<Event>(events.size());
		List<Event> removeList = new ArrayList<Event>();
		int removeCount = Math.max(0, events.size() - maxStoredEvents);
		
		int t = 0;
		for (Event e : events) {
			t++;
			
			if (t <= removeCount) {
				//Effectively removes the first N results
				continue;
			}
			
			result.add(e);
			
			if (e instanceof CompileScriptEvent) {
				CompileScriptEvent lse = (CompileScriptEvent)e;
				Long oldTime = modificationTimes.put(lse.filename, lse.modificationTime);
				if (oldTime != null && oldTime < lse.modificationTime) {
					//Remove all events caused by an outdated version of the script
					for (Event event : result) {
						if (lse.filename.equals(event.getCallingScript())) {
							removeList.add(event);
						}
					}
				}
			}
		}
		
		//O(n^2), but the size of removeList is usually small
		result.removeAll(removeList);
		
		return result;
	}
	
	protected abstract InputStream openInputStream(String filename) throws IOException;	
	protected abstract OutputStream openOutputStream(String filename, boolean append) throws IOException;	
	protected abstract long getFileSize(String filename);
	
	protected void logEvent(Event event) {
		if (event.type != EventType.LINE) {
			String script = event.getCallingScript();
			int line = event.getStartLine();
			if (script != null && (!script.equals(lastScript) || line != lastEnd)) {
				onJump(script, line);
			}
		}
		
		log.add(event);
		//System.out.println(event.toString());
	}
	
	protected void onJump(String targetFilename, int targetLine) {
		if (lastScript == null) {
			return;
		}
		
		if (lastScript != null && lastScript.equals(targetFilename)) {
			lastEnd = Math.max(lastEnd, targetLine);
		}
		
		logEvent(new LineEvent(lastScript, lastStart, lastEnd));
		lastStart = lastEnd;
		
		if (targetFilename == null) {
			return;
		}
		
		if (!lastScript.equals(targetFilename) || targetLine < lastEnd) {
			JumpEvent je = new JumpEvent(lastScript, lastEnd, targetFilename, targetLine);
			log.add(je);
			
			//System.out.println("* " + je);
		}

		lastScript = targetFilename;
		lastStart = targetLine;
		lastEnd = lastStart;
	}
	
	@Override
	public void logScriptLine(String callSite) {
		if (callSite == null) {
			return;
		}
		
		String file = getSrclocFilename(callSite);
		int line = getSrclocLine(callSite);
		if (lastScript != null && lastScript.equals(file) && line >= lastEnd) {
			lastEnd = line;
			return; //Same line as last time, no need to log it again
		}
		
		if (lastEnd > lastStart) {
			onJump(file, line);
		}
		lastScript = file;
		lastStart = lastEnd = line;
	}
	
	@Override
	public void logScriptCompile(String scriptFilename, long modificationTime) {
		logEvent(new CompileScriptEvent(scriptFilename, modificationTime));
	}
	
	@Override
	public void logImageLoad(String callSite, String image, long loadTimeNanos) {
		String cf = getSrclocFilename(callSite);
		int cl = getSrclocLine(callSite);		
		logEvent(new LoadImageEvent(cf, cl, image, loadTimeNanos));
	}

	@Override
	public void logSoundLoad(String callSite, String sound, long loadTimeNanos) {
		String cf = getSrclocFilename(callSite);
		int cl = getSrclocLine(callSite);		
		logEvent(new LoadSoundEvent(cf, cl, sound, loadTimeNanos));
	}
	
	private static String formatCallSite(String filename, int line) {
		String callSite = null;
		if (filename != null) {
			callSite = filename;
		}
		if (callSite != null && line > 0) {
			callSite += ":" + line;
		}
		return formatCallSite(callSite);
	}
	private static String formatCallSite(String callSite) {
		return (callSite != null ? "@" + callSite : "");
	}
	
	//Getters
	protected void generatePreloaderData(PreloaderData out, List<Event> inEvents) throws IOException {		
		Map<String, List<Event>> fileLogs = new HashMap<String, List<Event>>();
		{ //Group per file
			String lastScript = null;
			int lastLine = 0;
			for (Event e : inEvents) {
				final String script = e.getCallingScript();
				if (script == null || script.equals("?")) {
					//lastScript = null;
					continue;
				}
				
				int line = e.getStartLine();				
				List<Event> es = fileLogs.get(script);
				if (es == null) {
					es = new ArrayList<Event>();
					fileLogs.put(script, es);
				}
				if (!script.equals(lastScript) || line < lastLine) {
					//Insert nulls to separate runs
					es.add(null);
				}

				if (line > 0) {
					es.add(e);
					lastLine = line;
				}
				lastScript = script;
			}
		}
		
		//Process per file
		for (Entry<String, List<Event>> entry : fileLogs.entrySet()) {
			final String script = entry.getKey();
			
			//Process runs
			LoadHistogram histogram = new LoadHistogram();
			int[] lineHistogram = new int[1024];
			BitSet linesSeen = new BitSet();
			
			//System.out.println("[[" + script + "]]");
			
			int runId = 0;
			Iterator<Event> events = entry.getValue().iterator();
			while (events.hasNext()) {
				final int MAX_LINE = 9999;
				linesSeen.clear();
				//System.out.println("RUN");
				
				runId++;
				while (events.hasNext()) {
					Event e = events.next();
					if (e == null) break; //End of run reached
					
					//Get line span for event
					int sl = e.getStartLine();
					int el = e.getEndLine();
					
					//System.out.println(script+":"+sl+"-"+el+" " + e);
					
					//Update line counts
					if (sl > 0 && sl <= MAX_LINE && el > 0 && el <= MAX_LINE) {
						linesSeen.set(sl, el+1);
					}
					histogram.add(runId, e);
				}
				
				if (linesSeen.length() >= lineHistogram.length) {
					int[] temp = new int[Math.max(linesSeen.length(), lineHistogram.length)];
					System.arraycopy(lineHistogram, 0, temp, 0, lineHistogram.length);
					lineHistogram = temp;
				}
			    for (int i = linesSeen.nextSetBit(0); i >= 0; i = linesSeen.nextSetBit(i+1)) {
					lineHistogram[i]++;
			    }
			}
			
			//Analyze histogram
			
			final float THRESHOLD_PROBABILITY = .1f;
			Map<MediaType, List<LoadHistogramEntry>> hm = new EnumMap<MediaType, List<LoadHistogramEntry>>(MediaType.class);
			hm.put(MediaType.SCRIPT, histogram.scripts);
			hm.put(MediaType.IMAGE, histogram.images);
			hm.put(MediaType.SOUND, histogram.sounds);
			for (Entry<MediaType, List<LoadHistogramEntry>> hmEntry : hm.entrySet()) {
				final MediaType type = hmEntry.getKey();
				for (LoadHistogramEntry he : hmEntry.getValue()) {
					int lineCount = (he.line >= 0 && he.line < lineHistogram.length ? lineHistogram[he.line] : 0);
					
					if (lineCount <= 0) {
						continue;
					}
					
					float p = he.count / (float)lineCount;
					//System.out.printf("[%03d%%] %s (%d/%d) %s\n", Math.max(0, Math.min(100, Math.round(100 * p))), he.filename, he.count, lineCount, type);
					if (p >= THRESHOLD_PROBABILITY) {
						out.addMedia(script, he.line, type, he.filename, p);
					}
				}
			}
		}
	}
	
	protected String getSrclocFilename(String srcloc) {
		String fn = ParserUtil.getSrclocFilename(srcloc, null);
		if (fn != null) {
			if (!filenamePool.add(fn)) {
				//If the filename was already in the pool, use the pooled instance
				fn = filenamePool.getStored(fn);
				assert fn != null;
			}
		}
		return fn;
	}
	protected int getSrclocLine(String srcloc) {
		return ParserUtil.getSrclocLine(srcloc);
	}
	
	//Setters

	//Inner Classes
	private static class LoadHistogram {
		
		public final List<LoadHistogramEntry> scripts;
		public final List<LoadHistogramEntry> images;
		public final List<LoadHistogramEntry> sounds;
		
		public LoadHistogram() {
			scripts = new ArrayList<LoadHistogramEntry>();
			images = new ArrayList<LoadHistogramEntry>();
			sounds = new ArrayList<LoadHistogramEntry>();
		}
		
		/**
		 * Adds an event to the histogram, only counts events (filename/line)
		 * once per run (runId must be monotonically increasing).
		 */
		public void add(int runId, Event e) {
			final int line = e.getStartLine();

			final String filename;
			final Collection<LoadHistogramEntry> hes;
			if (e instanceof JumpEvent) {
				JumpEvent je = (JumpEvent)e;
				filename = je.targetFilename + ":" + je.targetLine;
				hes = scripts;
			} else if (e instanceof LoadImageEvent) {
				filename = ((LoadImageEvent)e).filename;
				hes = images;
			} else if (e instanceof LoadSoundEvent) {
				filename = ((LoadSoundEvent)e).filename;
				hes = sounds;
			} else {
				return; //Not a load event				
			}
			
			LoadHistogramEntry entry = null;
			for (LoadHistogramEntry he : hes) {
				if (filename.equals(he.filename) && line == he.line) {
					entry = he;
					break;
				}
			}
			
			if (entry == null) {
				entry = new LoadHistogramEntry(filename, line);
				hes.add(entry);
			}
			if (entry.lastRunId != runId) {
				entry.count++;			
				entry.lastRunId = runId;
			}
		}
		
	}
	
	private static class LoadHistogramEntry {
		
		public final String filename;
		public final int line;
		
		int lastRunId = -1;
		public int count;
		
		public LoadHistogramEntry(String fn, int l) {
			filename = fn;
			line = l;
		}
		
	}
	
	private static class Event implements Serializable {
		protected static final long serialVersionUID = 1L;
		
		public final EventType type;
		
		protected Event(EventType et) {
			type = et;
		}
		
		public String getCallingScript() {
			return null;
		}
		public int getStartLine() {
			return 0;
		}
		public int getEndLine() {
			return getStartLine();
		}
		
		@Override
		public String toString() {
			return toString("");
		}
		protected String toString(String format, Object... args) {
			return String.format("[%s]", type) + " " + String.format(format, args);
		}
	}
	
	private static class LineEvent extends Event {
		protected static final long serialVersionUID = Event.serialVersionUID;		

		public final String filename;
		public final int startLine;
		public final int endLine;
		
		public LineEvent(String fn, int sl, int el) {
			super(EventType.LINE);
			
			this.filename = fn;
			this.startLine = sl;
			this.endLine = el;
		}
		
		@Override
		public String getCallingScript() {
			return filename;
		}
		
		@Override
		public int getStartLine() {
			return startLine;
		}
		
		@Override
		public int getEndLine() {
			return endLine;
		}
		
		@Override
		public String toString() {
			return super.toString("%s:%d-%d", filename, startLine, endLine);
		}
	}
	
	private static class JumpEvent extends Event {
		protected static final long serialVersionUID = Event.serialVersionUID;		

		public final String sourceFilename;
		public final int sourceLine;
		public final String targetFilename;
		public final int targetLine;
		
		public JumpEvent(String sf, int sl, String tf, int tl) {
			super(EventType.JUMP);

			this.sourceFilename = sf;
			this.sourceLine = sl;
			this.targetFilename = tf;
			this.targetLine = tl;
			
			if (targetFilename == null) throw new NullPointerException();
		}
		
		@Override
		public String getCallingScript() {
			return sourceFilename;
		}
		
		@Override
		public int getStartLine() {
			return sourceLine;
		}
		
		@Override
		public String toString() {
			return super.toString("%s:%d -> %s:%d", sourceFilename, sourceLine,
					targetFilename, targetLine);
		}
	}
	
	private static class LoadImageEvent extends Event {
		protected static final long serialVersionUID = Event.serialVersionUID;		

		public final String callFilename;
		public final int callLine;
		public final String filename;
		public final long loadTimeNanos;
		
		public LoadImageEvent(String callFn, int callLn, String fn, long nanos) {
			super(EventType.LOAD_IMAGE);

			this.callFilename = callFn;
			this.callLine = callLn;
			this.filename = fn;
			this.loadTimeNanos = nanos;
		}

		@Override
		public String getCallingScript() {
			return callFilename;
		}
		
		@Override
		public int getStartLine() {
			return callLine;
		}
		
		@Override
		public String toString() {
			return super.toString("%s %d %s", filename, loadTimeNanos, formatCallSite(callFilename, callLine));
		}
	}
	
	private static class LoadSoundEvent extends Event {
		protected static final long serialVersionUID = Event.serialVersionUID;		

		public final String callFilename;
		public final int callLine;
		public final String filename;
		public final long loadTimeNanos;
		
		public LoadSoundEvent(String callFn, int callLn, String fn, long nanos) {
			super(EventType.LOAD_SOUND);

			this.callFilename = callFn;
			this.callLine = callLn;
			this.filename = fn;
			this.loadTimeNanos = nanos;
		}

		@Override
		public String getCallingScript() {
			return callFilename;
		}
		
		@Override
		public int getStartLine() {
			return callLine;
		}
		
		@Override
		public String toString() {
			return super.toString("%s %d %s", filename, loadTimeNanos, formatCallSite(callFilename, callLine));
		}
	}
	
	private static class CompileScriptEvent extends Event {
		protected static final long serialVersionUID = Event.serialVersionUID;		

		public final String filename;
		public final long modificationTime;
		
		public CompileScriptEvent(String fn, long modTime) {
			super(EventType.COMPILE_SCRIPT);

			this.filename = fn;
			this.modificationTime = modTime;
		}

		@Override
		public String toString() {
			return super.toString("%s %d", filename, modificationTime);
		}
	}
	
}
