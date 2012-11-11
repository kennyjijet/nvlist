package nl.weeaboo.vn.impl.base;

import nl.weeaboo.vn.ILayer;

public class LayerRenderCommand extends BaseRenderCommand {

	public static final byte id = ID_LAYER_RENDER_COMMAND;
	
	public final ILayer layer;

	protected LayerRenderCommand(ILayer layer) {
		super(id, layer.getZ(), layer.isClipEnabled(), layer.getBlendMode(), layer.getColorARGB(), (byte)layer.hashCode());
		
		this.layer = layer;
	}

}
