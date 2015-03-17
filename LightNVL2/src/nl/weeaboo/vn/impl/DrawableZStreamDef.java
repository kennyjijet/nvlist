package nl.weeaboo.vn.impl;

import nl.weeaboo.game.entity.Entity;
import nl.weeaboo.game.entity.EntityStreamDef;
import nl.weeaboo.game.entity.PartType;
import nl.weeaboo.vn.IDrawablePart;
import nl.weeaboo.vn.ILayer;

final class DrawableZStreamDef extends EntityStreamDef {

	private final ILayer layer;
	private final PartType<? extends IDrawablePart> drawablePart;
	private final int direction;

	public DrawableZStreamDef(ILayer layer, PartType<? extends IDrawablePart> part, int direction) {
		this.layer = layer;
		this.drawablePart = part;
		this.direction = direction;
	}

	@Override
	public int compare(Entity a, Entity b) {
		short az = a.getPart(drawablePart).getZ();
		short bz = b.getPart(drawablePart).getZ();
		return az < bz ? -direction : (az == bz ? 0 : direction);
	}

	@Override
	public boolean accept(Entity e) {
		IDrawablePart part = e.getPart(drawablePart);
		return part != null && part.getParentLayer() == layer;
	}

}