package nl.weeaboo.nvlist.menu;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import javax.imageio.ImageIO;
import javax.jnlp.FileContents;
import javax.jnlp.FileSaveService;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import nl.weeaboo.awt.ImageUtil;
import nl.weeaboo.common.StringUtil;
import nl.weeaboo.game.GameLog;
import nl.weeaboo.io.ByteChunkOutputStream;
import nl.weeaboo.io.FileUtil;
import nl.weeaboo.jnlp.JnlpUtil;
import nl.weeaboo.nvlist.Game;
import nl.weeaboo.vn.IScreenshot;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class ScreenshotItem extends GameMenuAction {

	private enum Format {
		PNG("PNG Image", "png"),
		JPEG("JPEG Image", "jpg");
		
		private final String label;
		private final String fext;
		
		private final FileFilter fileFilter = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) return true;
				return StringUtil.getExtension(f.getName()).equals(fext);
			}
			public String getDescription() {
				return Format.this.toString();
			}
		};
		
		private Format(String lbl, String ext) {
			this.label = lbl;
			this.fext = ext;
		}
		
		public static Format fromFilename(String fn) {
			String ext = StringUtil.getExtension(fn);
			for (Format format : values()) {
				if (format.fext.equals(ext)) {
					return format;
				}
			}
			return Format.PNG;
		}
		
		@Override
		public String toString() {
			return String.format("%s (*.%s)", label, fext);
		}
	}
	
	@Override
	public JMenuItem createItem(Game game, Novel nvl) {
		JMenuItem item = new JMenuItem("Screenshot...");
		//item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN, 0));
		return item;
	}

	@Override
	public void actionPerformed(JMenuItem item, ActionEvent e, final Game game, Novel nvl) {
		final IScreenshot ss = nvl.getImageFactory().screenshot(Short.MIN_VALUE);
		nvl.getImageState().getRootLayer().getScreenshotBuffer().add(ss, false);
		waitForScreenshot(game.getExecutor(), ss);
	}

	private static byte[] serializeImage(IScreenshot ss, Format format) throws IOException {
		BufferedImage image = new BufferedImage(ss.getWidth(), ss.getHeight(), BufferedImage.TYPE_INT_BGR);
		image.setRGB(0, 0, ss.getWidth(), ss.getHeight(), ss.getARGB(), 0, ss.getWidth());
		
		ByteChunkOutputStream bout = new ByteChunkOutputStream();
		if (format.fext.equals("jpg")) {
			ImageUtil.writeJPEG(bout, image, .90f);
		} else {
			ImageIO.write(image, format.fext, bout);
		}
		return bout.toByteArray();
	}
		
	/**
	 * Called on the event-dispatch thread
	 * @param ss A valid screenshot 
	 */
	protected void onScreenshotTaken(final IScreenshot ss) {
		final String folder = "";
		final String filename = "screenshot.png";
		
		FileSaveService fss = JnlpUtil.getFileSaveService();
		if (fss != null) {
			Format fmt = Format.PNG;
			
			byte[] bytes = null;
			try {
				bytes = serializeImage(ss, fmt);
			} catch (IOException ioe) {
				GameLog.e("Error saving screenshot", ioe);
				return;
			}
			
			FileContents fc = null;
			try {
				fc = fss.saveFileDialog(folder, new String[] {fmt.fext}, new ByteArrayInputStream(bytes), filename);
			} catch (IOException ioe) {
				GameLog.w("Error saving screenshot", ioe);
				return;
			}
			
			if (fc == null) {
				return;
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					showSuccessDialog();
				}
			});
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					final JFileChooser fc = new JFileChooser(folder);
					for (Format fmt : Format.values()) {
						if (fmt == Format.PNG) continue;
						
						fc.addChoosableFileFilter(fmt.fileFilter);
					}					
					fc.setFileFilter(Format.PNG.fileFilter);
					
					FormatAutoExt fae = new FormatAutoExt(fc);					
					fae.updateFormat();

					fc.setSelectedFile(new File(folder, filename));
					
					int res = fc.showSaveDialog(null);
					if (res != JFileChooser.APPROVE_OPTION) {
						return;
					}
					
					File file = fc.getSelectedFile();
					if (file == null || file.isDirectory()) {
						return;
					}

					try {
						Format format = Format.fromFilename(file.getName());
						//System.out.println(format);
						byte[] bytes = serializeImage(ss, format);
						FileUtil.writeBytes(file, new ByteArrayInputStream(bytes));
						showSuccessDialog();
					} catch (IOException ioe) {
						GameLog.w("Error saving screenshot", ioe);
					}
				}
			});
		}
	}
	
	protected void showSuccessDialog() {
		JOptionPane.showMessageDialog(null, "Image saved successfully",
				"Screenshot Saved", JOptionPane.PLAIN_MESSAGE);		
	}
	
	protected void waitForScreenshot(ExecutorService exec, final IScreenshot ss) {
		exec.execute(new Runnable() {
			public void run() {
				//Wait for screenshot
				for (int n = 0; n < 5000 && !ss.isAvailable() && !ss.isCancelled(); n++) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						//Ignore
					}
				}
				
				if (ss.isAvailable()) {
					onScreenshotTaken(ss);
				}
			}
		});		
	}
	
	private static class FormatAutoExt {
		
		private final JFileChooser fileChooser;
		private File oldFile;
		
		private FormatAutoExt(JFileChooser fc) {
			fileChooser = fc;
			oldFile = fc.getSelectedFile();
			
			fc.addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY,
					new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							if (evt.getOldValue() != null && evt.getNewValue() == null) {
								oldFile = (File) evt.getOldValue();
								updateFormat();
							} else {
								oldFile  = (File) evt.getNewValue();
							}
						}
					});
			fc.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY,
					new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent evt) {
							updateFormat();
						}
					});
		}
		
		protected void updateFormat() {
			if (oldFile == null) {
				return;
			}
			
			FileFilter filter = fileChooser.getFileFilter();
			String ext = Format.PNG.fext;
			for (Format format : Format.values()) {
				if (filter == format.fileFilter) {
					ext = format.fext;
				}
			}

			String path = StringUtil.replaceExt(oldFile.toString(), ext);
			oldFile = new File(path);
			fileChooser.setSelectedFile(oldFile);
		}
		
	}
	
}
