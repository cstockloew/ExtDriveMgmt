package data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Contains information about a file, an entry of a directory (to not confuse
 * this with java.io.File).
 * 
 * @author Carsten Stockloew
 *
 */
public class DirEntry extends Base {

	public int refNum;
	public Date date;
	private List<Drive> refs = new ArrayList<Drive>();

	@Override
	public int getRefNum() {
		return refNum;
	}

	@Override
	public List<Drive> getRefs() {
		return refs;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DirEntry))
			return false;

		DirEntry d = (DirEntry) obj;
		return name.equals(d.name) && date.equals(d.date) && size == d.size;
	}

	public void out() {
		System.out.println("DirEntry: ");
		System.out.println("    Name: " + name);
		System.out.println("    date: " + date);
		System.out.println("    size: " + size);
	}
}
