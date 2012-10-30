package nl.weeaboo.vn.layout;

public abstract class AbstractLayoutComponent implements ILayoutComponent {

	private static final long serialVersionUID = 1L;
	
	private final ILayoutConstraints constraints;
	
	public AbstractLayoutComponent(ILayoutConstraints c) {
		constraints = c;
	}
	
	//Functions
	
	//Getters
	@Override
	public ILayoutConstraints getConstraints() {
		return constraints;
	}
	
	//Setters
	@Override
	public void setX(double x) {
		setPos(x, getY());
	}
	
	@Override
	public void setY(double y) {
		setPos(getX(), y);
	}

	@Override
	public void setWidth(double w) {
		setSize(w, getHeight());
	}
	
	@Override
	public void setHeight(double h) {
		setSize(getWidth(), h);
	}

	public void setBounds(double x, double y, double w, double h) {
		setPos(x, y);
		setSize(w, h);
	}
	
}
