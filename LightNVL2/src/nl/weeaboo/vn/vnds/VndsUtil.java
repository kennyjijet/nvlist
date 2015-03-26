package nl.weeaboo.vn.vnds;

import static nl.weeaboo.settings.Preference.newPreference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.settings.Preference;
import nl.weeaboo.vn.core.impl.Storage;
import nl.weeaboo.vn.save.IStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class VndsUtil {

    private static final Logger LOG = LoggerFactory.getLogger(VndsUtil.class);

	public static final Preference<Boolean> VNDS = newPreference("vnds", false, "VNDS", "Run the engine in VNDS mode");
	public static final Preference<Boolean> TEXT_STREAM = newPreference("vnds.textStream", false, "Text Stream", "Activates continuous text stream mode, causing lines to be appended to the text box while they fit instead of clearing the text box for each line.");

	public static boolean isVNDSGame(File folder) {
		File infoF = new File(folder, "info.txt");
		File iconF = new File(folder, "icon.png");
		//File thumbnailF = new File(folder, "thumbnail.png");
		return infoF.exists() && iconF.exists(); // && thumbnailF.exists();
	}

	public static List<String> getZipFilenames() {
		return Arrays.asList("foreground.zip", "sound.zip", "background.zip", "script.zip");
	}

	public static IStorage readVndsGlobalSav(IFileSystem fs) {
        try {
            InputStream in = null;
            if (fs.getFileExists("save/global.sav")) {
                in = fs.newInputStream("save/global.sav");
            } else if (fs.getFileExists("save/GLOBAL.sav")) {
                in = fs.newInputStream("save/GLOBAL.sav");
            }

            if (in != null) {
                try {
                    return VndsUtil.readNdsGlobalSav(in);
                } catch (ParserConfigurationException e) {
                    LOG.warn("Error reading VNDS global.sav", e);
                } catch (SAXException e) {
                    LOG.warn("Error reading VNDS global.sav", e);
                } finally {
                    in.close();
                }
            }
        } catch (IOException ioe) {
            LOG.warn("Error reading VNDS global.sav", ioe);
        }

        return new Storage();
    }

    /** Reads a VNDS global.sav save file created by the Nintendo DS version of VNDS */
    private static IStorage readNdsGlobalSav(InputStream in) throws ParserConfigurationException,
            SAXException, IOException {

	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();

	    InputSource is = new InputSource(in);
	    is.setEncoding("UTF-8");
	    Document doc = builder.parse(is);

	    IStorage result = new Storage();
	    Element root = doc.getDocumentElement();
		NodeList varEList = root.getElementsByTagName("var");
		for (int vi = 0; vi < varEList.getLength(); vi++) {
			Element varE = (Element)varEList.item(vi);
			String name = varE.getAttribute("name");
			String type = varE.getAttribute("type");
			String value = varE.getAttribute("value");
			if (type.equals("string")) {
			    result.setString(name, value);
			} else if (type.equals("int")) {
				int intval = 0;
				try {
					intval = Integer.parseInt(value);
				} catch (NumberFormatException nfe) {
				    LOG.warn("Unparseable int: " + value, nfe);
				}
				result.setInt(name, intval);
			} else {
			    LOG.warn("Unsupported variable type: " + type);
			}
		}
		return result;
	}

}
