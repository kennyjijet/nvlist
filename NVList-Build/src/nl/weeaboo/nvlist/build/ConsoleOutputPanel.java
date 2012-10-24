package nl.weeaboo.nvlist.build;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import nl.weeaboo.awt.LogPane;
import nl.weeaboo.system.ProcessUtil;

@SuppressWarnings("serial")
public class ConsoleOutputPanel extends JPanel implements Runnable {

	private final LogPane outputPane;
	private long flushThreshold = 500 * 1000000;
	private volatile Process process;	
	
	public ConsoleOutputPanel() {
		outputPane = new LogPane();
		
		JScrollPane scrollPane = new JScrollPane(outputPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		
		setLayout(new BorderLayout(10, 10));
		add(scrollPane, BorderLayout.CENTER);
	}
	
	//Functions
	public void process(final Process p, final ProcessCallback postBuildCallback) {
		process = p;
		
		outputPane.setText("Waiting for process to start...\n");
		
		Thread readerThread = new Thread(new Runnable() {
			public void run() {
				try {
					ConsoleOutputPanel.this.run();
				} finally {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							outputPane.append("v", "Done", null);
						}
					});
					
					if (postBuildCallback != null) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								int exitCode = -1;
								try {
									exitCode = p.exitValue();
								} catch (IllegalThreadStateException e) {
									System.err.println(e);
								}
								postBuildCallback.run(exitCode);
							}							
						});
					}
				}
			}
		}, "ProcessOutputReader-" + p);
		readerThread.setDaemon(true);
		readerThread.start();
	}

	private long flush(final Process p, final String style, StringBuilder buffer, long lastTime) {
		long time = System.nanoTime();
		if (process == p && (lastTime == 0 || time - lastTime > flushThreshold)) {
			final String string = buffer.toString();
			buffer.delete(0, buffer.length());
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (process == p) {
						outputPane.append(style, string, null);
					}
				}
			});
			return time;
		} else {
			return lastTime;
		}
	}
	
	@Override
	public void run() {
		final Process p = process;
		if (p == null) {
			return;
		}
		
		final BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		final BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		
		StringBuilder errBuffer = new StringBuilder();
		StringBuilder outBuffer = new StringBuilder();
		
		long lastFlush = System.nanoTime();
		while (true) {
			try {				
				p.exitValue();				
				try {
					while (err.ready() || in.ready()) {
						if (maybeReadLine(err, errBuffer)) {
							lastFlush = flush(p, "e", errBuffer, 0);
						} else {
							lastFlush = flush(p, "e", errBuffer, lastFlush);						
						}
						if (maybeReadLine(in, outBuffer)) {
							lastFlush = flush(p, "v", outBuffer, 0);
						} else {
							lastFlush = flush(p, "v", outBuffer, lastFlush);
						}
					}
				} catch (IOException ioe) {
					//Ignore
				}
				flush(p, "e", errBuffer, 0);
				flush(p, "v", outBuffer, 0);
				break;
			} catch (IllegalThreadStateException e) {				
				try {
					while (err.ready() || in.ready()) {
						if (maybeReadLine(err, errBuffer)) {
							lastFlush = flush(p, "e", errBuffer, 0);
						} else {
							lastFlush = flush(p, "e", errBuffer, lastFlush);						
						}
						if (maybeReadLine(in, outBuffer)) {
							lastFlush = flush(p, "v", outBuffer, 0);
						} else {
							lastFlush = flush(p, "v", outBuffer, lastFlush);
						}
					}
					
					try {
						Thread.sleep(1);
					} catch (InterruptedException ie) {
						//Ignore
					}
				} catch (IOException ioe) {
					errBuffer.append("\nERROR: " + ioe + "\n");
					break;
				}
			}
		}
		
		ProcessUtil.kill(p);
	}
	
	protected boolean maybeReadLine(BufferedReader in, StringBuilder out) throws IOException {
		if (!in.ready()) {
			return false;
		}
		
		int t = 0;
		while (in.ready()) {
			int c = in.read();
			if (c == '\n' || c <= 0) {
				t = 0;
				return true;
			} else if (c != '\r') {
				if (t > 0 || !Character.isWhitespace(c)) {
					out.append((char)c);
					t++;
				}
			}
		}
		
		return false;
	}
	
	//Getters
	
	//Setters
	
}
