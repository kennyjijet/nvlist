package nl.weeaboo.nvlist.build.android;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import nl.weeaboo.awt.ImageUtil;
import nl.weeaboo.io.FileUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

final class Handlers {

	private Handlers() {		
	}
	
	/**
	 * Creates the file, also creating any necessary parent directories.
	 */
	private static void createFile(File dstF) throws IOException {
		if (!dstF.getParentFile().exists() && !dstF.getParentFile().mkdirs()) {
			throw new IOException("Unable to create folder: " + dstF.getParentFile());
		}
		if (!dstF.exists() && !dstF.createNewFile()) {
			throw new IOException("Unable to create folder: " + dstF);				
		}
	}
	
	private static Document readXml(File file) throws IOException {
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		try {
			return readXml(in);
		} finally {
			in.close();
		}
	}
	private static Document readXml(InputStream in) throws IOException {
		try {
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder = factory.newDocumentBuilder();
		    InputSource is = new InputSource(in);
		    is.setEncoding("UTF-8");
		    return builder.parse(is);
		} catch (ParserConfigurationException e) {
			throw new IOException(e);
		} catch (SAXException e) {
			throw new IOException(e);
		}
	}
	
	private static void writeXml(File file, Document document) throws IOException {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		try {
			writeXml(out, document);
		} finally {
			out.close();
		}
	}
	private static void writeXml(OutputStream out, Document document) throws IOException {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new OutputStreamWriter(out, "UTF-8"));
			transformer.transform(source, result);
		} catch (TransformerConfigurationException tce) {
			throw new IOException(tce);
		} catch (TransformerException e) {
			throw new IOException(e);
		}
	}
	
	private static String replaceJavaCodeField(String contents, String fieldName, String newValue) {
		return contents.replaceAll(
				"\\s+" + fieldName + "\\s*=\\s*([^;]+);",
				" " + fieldName + " = " + newValue + ";");
	}
	
	public static final FileHandler getDefault() {
		return new FileHandler() {
			@Override
			public void process(String relpath, File srcF, File dstF) throws IOException {
				if (!srcF.equals(dstF)) {
					//System.out.println(relpath + " " + srcF + " " + dstF);					
					FileUtil.copyFile(srcF, dstF);
				}
			}
		};
	}
	
	public static final FileHandler expansionConstants(final String pkg, final String lvlKeyBase64,
			final int mainXAPKVersion, final File mainXAPKFile,
			final int patchXAPKVersion, final File patchXAPKFile)
	{		
		return new FileHandler() {
			@Override
			public void process(String relpath, File srcF, File dstF) throws IOException {
				createFile(dstF);
				String contents = FileUtil.read(srcF);
								
				contents = doJavaHandler(contents, pkg);
				contents = replaceJavaCodeField(contents, "LVL_KEY_BASE64", "\"" + lvlKeyBase64 + "\"");
				
				StringBuilder xapkDefs = new StringBuilder();
				xapkDefs.append("{");
				if (mainXAPKFile != null) {
					xapkDefs.append(String.format("new XAPKFile(true, %d, %dL)", mainXAPKVersion, mainXAPKFile.length()));
				}
				if (patchXAPKFile != null) {
					if (mainXAPKFile != null) {
						xapkDefs.append(", ");
					}
					xapkDefs.append(String.format("new XAPKFile(false, %d, %dL)", patchXAPKVersion, patchXAPKFile.length()));
				}
				xapkDefs.append("}");
				contents = replaceJavaCodeField(contents, "EXPANSION_FILES", xapkDefs.toString());
				
				FileUtil.write(dstF, contents);
			}
		};
	}
	
	private static final String doJavaHandler(String contents, String pkg) {
		contents = contents.replace("import nl.weeaboo.android.nvlist.template.R", "import " + pkg + ".R");
		return contents;
	}
	
	public static final FileHandler javaHandler(final String pkg) {
		return new FileHandler() {
			@Override
			public void process(String relpath, File srcF, File dstF) throws IOException {
				createFile(dstF);
				String contents = FileUtil.read(srcF);
				contents = doJavaHandler(contents, pkg);								
				FileUtil.write(dstF, contents);
			}
		};
	}
	
	public static final FileHandler androidManifestHandler(final String pkg, final int versionCode,
			final String versionName)
	{
		return new FileHandler() {
			@Override
			public void process(String relpath, File srcF, File dstF) throws IOException {
				createFile(dstF);
				
				Document d = readXml(srcF);
				
				Element manifestE = d.getDocumentElement();
				manifestE.setAttribute("package", pkg);
				manifestE.setAttribute("android:versionCode", ""+versionCode);
				manifestE.setAttribute("android:versionName", versionName);
				
				writeXml(dstF, d);
			}
		};
	}
	
	public static final FileHandler stringResHandler(final String title, final String folder) {
		return new FileHandler() {
			@Override
			public void process(String relpath, File srcF, File dstF) throws IOException {
				createFile(dstF);
				
				Document d = readXml(srcF);
				Element rootE = d.getDocumentElement();
				NodeList stringsList = rootE.getElementsByTagName("string");
				for (int n = 0; n < stringsList.getLength(); n++) {
					Element stringE = (Element)stringsList.item(n);
					String name = stringE.getAttribute("name");
					if (name.equals("app_title")) {
						stringE.setTextContent(title);
					} else if (name.equals("folder")) {
						stringE.setTextContent(folder);
					}
				}				
				writeXml(dstF, d);
			}
		};
	}
	
	public static final FileHandler drawableHandler(final File iconFile, final File splashFile) {
		return new FileHandler() {
			@Override
			public void process(String relpath, File srcF, File dstF) throws IOException {
				float scale = 1f;
				if (relpath.contains("drawable-ldpi")) {
					scale = .75f;
				} else if (relpath.contains("drawable-hdpi")) {
					scale = 1.5f;
				} else if (relpath.contains("drawable-xhdpi")) {
					scale = 2.0f;
				}
				
				createFile(dstF);
				if (relpath.contains("AndroidNVList")) {
					//System.out.println(relpath + " " + splashFile.exists() + " " + splashFile);
					
					if (relpath.endsWith("icon.png") && iconFile.exists()) {
						writeImageScaled(iconFile, Math.round(48*scale), Math.round(48*scale), true, dstF);
						return;
					} else if (relpath.endsWith("splash.png") && splashFile.exists()) {
						writeImageScaled(splashFile, Math.round(256*scale), Math.round(256*scale), false, dstF);
						return;
					}
				}
					
				if (!srcF.equals(dstF)) {
					FileUtil.copyFile(srcF, dstF);
				}
			}
			
			private void writeImageScaled(File srcF, int maxW, int maxH, boolean allowUpscale, File dstF)
					throws IOException
			{
				try {
					BufferedImage image = ImageIO.read(srcF);
					if (!allowUpscale) {
						maxW = Math.min(maxW, image.getWidth());
						maxH = Math.min(maxH, image.getHeight());
					}
					image = ImageUtil.getScaledImageProp(image, maxW, maxH, Image.SCALE_AREA_AVERAGING);
					ImageIO.write(image, "png", dstF);
					//System.out.println("Writing icon: " + dstF);
				} catch (IOException ioe) {
					throw new IOException("Unable to read " + iconFile, ioe);
				}				
			}
		};
	}
	
}
