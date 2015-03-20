package nl.weeaboo.vn.awt;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import nl.weeaboo.io.BufferUtil;
import nl.weeaboo.vn.image.impl.DecodingScreenshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwtDecodingScreenshot extends DecodingScreenshot {

	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(AwtDecodingScreenshot.class);

	public AwtDecodingScreenshot(ByteBuffer b) {
		super(b);
	}

	@Override
	protected void tryLoad(ByteBuffer data) {
		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(BufferUtil.toArray(data)));
			int[] argb = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
			setPixels(argb, img.getWidth(), img.getHeight());
		} catch (IOException e) {
			LOG.warn("Exception while trying to decode the image: " + this, e);
		}
	}

}
