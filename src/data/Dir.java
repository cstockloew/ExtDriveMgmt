package data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Contains information about a folder/directory.
 * 
 * @author Carsten Stockloew
 *
 */
public class Dir extends Base {

	protected HashMap<String, Dir> subdirs = new HashMap<String, Dir>();
	protected HashSet<DirEntry> entries = new HashSet<DirEntry>();

	public Drive drive;
	private static List<Drive> nullList = new ArrayList<Drive>();

	public Dir(Drive drive, String name) {
		this.name = name;
		this.drive = drive;
	}

	/**
	 * Calculate the size of this folder and, recursively, of all sub folders.
	 */
	public void calcSize() {
		size = 0;
		for (Dir d : getSubDirs()) {
			d.calcSize();
			size += d.size;
		}
		for (DirEntry e : getEntries())
			size += e.size;
	}

	public Collection<Dir> getSubDirs() {
		return subdirs.values();
	}

	public Collection<DirEntry> getEntries() {
		return entries;
	}

	public Dir getOrAddSubDir(Drive drive, String name) {
		if ("".equals(name)) {
			// the root dir = this dir
			return this;
		}
		Dir d = subdirs.get(name);
		if (d == null) {
			d = new Dir(drive, name);
			subdirs.put(name, d);
		}
		return d;
	}

	public void addEntry(DirEntry e) {
		if (!entries.add(e)) {
			// throw new RuntimeException("Element already available: " +
			// e.name);
			System.err.println("Element already available: " + e.name);
		}
	}

	@Override
	public int getRefNum() {
		return 1;
	}

	@Override
	public List<Drive> getRefs() {
		return nullList;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return name.equals(obj);
	}
}
