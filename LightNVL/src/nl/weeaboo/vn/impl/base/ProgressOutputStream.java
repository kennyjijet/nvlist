package nl.weeaboo.vn.impl.base;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import nl.weeaboo.vn.IProgressListener;

public class ProgressOutputStream extends FilterOutputStream {

	private final int updateBytes;
	private final IProgressListener pl;

	private int lastReportedPos;
	private int pos;
	
	public ProgressOutputStream(OutputStream out, int updateBytes, IProgressListener pl) {
		super(out);
		
		if (pl == null) {
			throw new IllegalArgumentException("Progress listener must not be null");
		}
		
		this.updateBytes = updateBytes;
		this.pl = pl;
	}

	//Functions
	@Override
    public void write(byte b[], int off, int len) throws IOException {
		out.write(b, off, len);

		pos += len;
   		if (pos - lastReportedPos >= updateBytes) {   			
   			if (updateBytes > 0) {
   				lastReportedPos = (pos / updateBytes) * updateBytes;
   			} else {
   				lastReportedPos = pos;
   			}
   			pl.onProgressChanged(lastReportedPos);
   		}    		
	}	
	
	@Override
	public void write(int b) throws IOException {
		out.write(b);
		
   		pos++;
   		if (pos - lastReportedPos >= updateBytes) {
   			if (updateBytes > 0) {
   				lastReportedPos = (pos / updateBytes) * updateBytes;
   			} else {
   				lastReportedPos = pos;
   			}
   			pl.onProgressChanged(lastReportedPos);
   		}    		
	}
	
	//Getters
	
	//Setters
	
}
