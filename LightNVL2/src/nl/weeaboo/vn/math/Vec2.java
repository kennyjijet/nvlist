package nl.weeaboo.vn.math;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import nl.weeaboo.common.FastMath;
import nl.weeaboo.lua2.io.LuaSerializable;

@LuaSerializable
public final class Vec2 implements Cloneable, Externalizable {

	public double x, y;

	public Vec2() {
		this(0, 0);
	}
	public Vec2(double x, double y) {
		this.x = x;
		this.y = y;
	}
	public Vec2(Vec2 v) {
		this.x = v.x;
		this.y = v.y;
	}

	//Functions
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeDouble(x);
		out.writeDouble(y);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		x = in.readDouble();
		y = in.readDouble();
	}

	@Override
	public Vec2 clone() {
		return new Vec2(x, y);
	}

	@Override
	public String toString() {
		return String.format("%s[%.2f, %.2f]",
				getClass().getSimpleName(), x, y);
	}

	@Override
	public int hashCode() {
		return (int)(Double.doubleToLongBits(x) ^ Double.doubleToLongBits(y));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Vec2) {
			Vec2 v = (Vec2)obj;
			return equals(v, 0.0);
		}
		return false;
	}

	public boolean equals(Vec2 v, double epsilon) {
		if (epsilon == 0.0) {
			return x == v.x && y == v.y;
		} else {
			return FastMath.approxEquals(x, v.x, epsilon)
				&& FastMath.approxEquals(y, v.y, epsilon);
		}
	}

	public void add(Vec2 v) {
		x += v.x;
		y += v.y;
	}
	public void sub(Vec2 v) {
		x -= v.x;
		y -= v.y;
	}
	public void scale(double s) {
		x *= s;
		y *= s;
	}
	public void normalize() {
		scale(1.0 / length());
	}

	public Vec2 cross(Vec2 v) {
		return new Vec2(y - v.y, v.x - x);
	}
	public double dot(Vec2 v) {
		return x*v.x + y*v.y;
	}

	public double lengthSquared() {
		return x*x + y*y;
	}
	public double length() {
		return Math.sqrt(x*x + y*y);
	}

	//Getters

	//Setters

}
