package nl.weeaboo.vn;

import java.io.Serializable;

public interface IShader extends Serializable {

	// === Functions ===========================================================
	/**
	 * @param effectSpeed The suggested relative animation speed
	 * @return <code>true</code> If a redraw is required
	 */
	public boolean update(double effectSpeed);

	// === Getters =============================================================
	public double getTime();

	// === Setters =============================================================
	public void setTime(double t);

}
