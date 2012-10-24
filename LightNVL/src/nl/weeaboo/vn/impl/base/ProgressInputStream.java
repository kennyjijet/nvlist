package nl.weeaboo.vn.impl.base;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import nl.weeaboo.vn.IProgressListener;

public class ProgressInputStream extends FilterInputStream {

	private final int updateBytes;
	private final long length;
	private final IProgressListener pl;

	private long lastReportedPos;
	private long pos;
	
	public ProgressInputStream(InputStream in, int updateBytes, long length, IProgressListener pl) {
		super(in);
		
		if (length < 0) {
			throw new IllegalArgumentException("Invalid length: " + length);
		}		
		if (pl == null) {
			throw new IllegalArgumentException("Progress listener must not be null");
		}
		
		this.updateBytes = updateBytes;
		this.length = length;
		this.pl = pl;
	}
	
	//Functions
	@Override
    public synchronized long skip(long n) throws IOException {
    	n = in.skip(n);
    	
    	if (n >= 0) {
    		pos += n;
    		
    		if (pos - lastReportedPos >= updateBytes || pos == length) {
    			lastReportedPos = pos; 
    			pl.onProgressChanged((float)(pos / (double)length));
    		}    		
    	}
    	
    	return n;
    }
	
	@Override
	public synchronized int read(byte b[], int off, int len) throws IOException {
    	int r = super.read(b, off, len);
    	
    	if (r >= 0) {
    		pos += r;
    		if (pos - lastReportedPos >= updateBytes || pos == length) {
    			lastReportedPos = pos; 
    			pl.onProgressChanged((float)(pos / (double)length));
    		}    		
    	}
    	
    	return r;
	}
	
	@Override
    public synchronized int read() throws IOException {
    	int r = super.read();
    	
    	if (r >= 0) {
    		pos++;
    		if (pos - lastReportedPos >= updateBytes || pos == length) {
    			lastReportedPos = pos; 
    			pl.onProgressChanged((float)(pos / (double)length));
    		}    		
    	}
    	
    	return r;
    }
    
	@Override
	public boolean markSupported() {
		return false;
	}
	
	@Override
	public void mark(int readlimit) {		
	}
	
	@Override
    public void reset() throws IOException {
    	throw new IOException("mark/reset not supported");
    }
	
	//Getters
	
	//Setters
	
}
