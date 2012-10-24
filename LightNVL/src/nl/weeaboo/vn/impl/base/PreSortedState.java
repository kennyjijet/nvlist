package nl.weeaboo.vn.impl.base;

import java.util.Arrays;
import java.util.Comparator;

abstract class PreSortedState<T> {

	private transient boolean isSorted;
	private transient T[] sorted;
	
	public PreSortedState() {
	}
	
	//Functions
	protected void invalidateSorting() {
		if (!isSorted) {
			return;
		}
		
		isSorted = false;
		if (sorted != null) {
			final int MIN_LENGTH = 128;
			if (sorted.length > MIN_LENGTH && sorted.length > 2*size()) {
				sorted = null; //Delete array if way too large
			} else {
				Arrays.fill(sorted, null);
			}
		}
	}
	
	protected T[] initSorted() {
		if (!isSorted || sorted == null || !checkSorted(sorted)) {
			int len = size();
			int oldArrayLen = (sorted == null ? 0 : sorted.length);
			if (oldArrayLen == 0 || oldArrayLen < len) {
				sorted = newArray(Math.max(oldArrayLen*2, len));
			}
			sorted = getUnsorted(sorted);
			if (len > 0) {
				Arrays.sort(sorted, 0, len, getComparator());
			}
			isSorted = true;
		}
		return sorted;
	}
	
	protected boolean checkSorted(T[] sorted) {
		final Comparator<? super T> c = getComparator();
		final int len = size();
		for (int n = 0; n < len-1; n++) {
			if (c.compare(sorted[n], sorted[n+1]) > 0) {
				//System.err.println("Sorting change detected");
				invalidateSorting();
				return false;
			}
		}
		return true;
	}
	
	protected abstract T[] newArray(int length);
	protected abstract T[] getUnsorted(T[] out);
	
	//Getters
	public abstract int size();
	
	protected abstract Comparator<? super T> getComparator();
	
	public T[] getSorted(T[] out, boolean reverse) {
		initSorted();
		
		final int len = size();
		if (out == null || out.length < len) {
			out = newArray(len);
		}
		if (reverse) {
			for (int n = len-1, d = 0; n >= 0; n--, d++) {
				out[d] = sorted[n];
			}
		} else {
			System.arraycopy(sorted, 0, out, 0, len);
		}
		if (out.length > len) {
			out[len] = null;
		}
		return out;
	}
	
	//Setters
	
}
