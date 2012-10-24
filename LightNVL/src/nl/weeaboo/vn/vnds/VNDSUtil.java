package nl.weeaboo.vn.vnds;

import static nl.weeaboo.settings.Preference.newPreference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.settings.Preference;
import nl.weeaboo.vn.IStorage;
import nl.weeaboo.vn.impl.lua.LuaNovel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class VNDSUtil {

	public static final Preference<Boolean> VNDS = newPreference("vnds", false, "VNDS", "Run the engine in VNDS mode");	
	
	public static boolean isVNDSGame(File folder) {
		File infoF = new File(folder, "info.txt");
		File iconF = new File(folder, "icon.png");
		//File thumbnailF = new File(folder, "thumbnail.png");
		return infoF.exists() && iconF.exists(); // && thumbnailF.exists();
	}

	public static void superSkip(LuaNovel novel) {
		try {
			novel.exec("vnds.setSkip(true)");
		} catch (LuaException e) {
			novel.getNotifier().w("superskip failed: " + e, e);
		}
	}
	
	public static String[] getZipFilenames() {
		return new String[] {"foreground.zip", "sound.zip", "background.zip", "script.zip"};		
	}
	
	public static void readDSGlobalSav(IStorage out, InputStream in) throws ParserConfigurationException, SAXException, IOException {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    InputSource is = new InputSource(in);
	    is.setEncoding("UTF-8");
	    Document dom = builder.parse(is);
	    Element root = dom.getDocumentElement();
		NodeList varEList = root.getElementsByTagName("var");
		for (int vi = 0; vi < varEList.getLength(); vi++) {
			Element varE = (Element)varEList.item(vi);
			String name = varE.getAttribute("name");
			String type = varE.getAttribute("type");
			String value = varE.getAttribute("value");
			if (type.equals("string")) {
				out.set(name, value);
			} else if (type.equals("int")) {
				int intval = 0;
				try {
					intval = Integer.parseInt(value);
				} catch (NumberFormatException nfe) {
					//Ignore
				}
				out.set(name, intval);
			} else {
				throw new SAXException("Unsupported var type: " + type);
			}
		}		 
	}
	
}
