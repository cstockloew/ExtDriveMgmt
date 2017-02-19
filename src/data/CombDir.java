package data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Combined directory. Contains all folders and files for a path, combined for
 * all drives.
 * 
 * @author Carsten Stockloew
 *
 */
public class CombDir extends Dir {
	List<Dir> dirs;

	public CombDir(String name, List<Dir> dirs) {
		super(null, name);
		this.dirs = dirs;
	}

	@Override
	public int getRefNum() {
		return dirs.size();
	}

	@Override
	public List<Drive> getRefs() {
		List<Drive> lst = new ArrayList<Drive>();
		for (Dir d : dirs) {
			lst.add(d.drive);
		}
		return lst;
	}

	@Override
	public Collection<Dir> getSubDirs() {
		if (!subdirs.isEmpty()) {
			return super.getSubDirs();
		}

		// either this dir has no subdirs or the subdirs are not calculated yet
		// for this comb dir
		HashMap<String, List<Dir>> temp = new HashMap<String, List<Dir>>();
		for (Dir dir : dirs) {
			for (Dir subdir : dir.getSubDirs()) {
				List<Dir> lst = temp.get(subdir.name);
				if (lst == null) {
					lst = new ArrayList<Dir>();
					temp.put(subdir.name, lst);
				}
				lst.add(subdir);
			}
		}

		for (Entry<String, List<Dir>> e : temp.entrySet()) {
			subdirs.put(e.getKey(), new CombDir(e.getKey(), e.getValue()));
		}

		return super.getSubDirs();
	}

	@Override
	public Collection<DirEntry> getEntries() {
		if (!entries.isEmpty()) {
			return super.getEntries();
		}

		// either this dir has no entries or the entries are not calculated yet
		// for this comb dir
		HashMap<DirEntry, DirEntry> temp = new HashMap<DirEntry, DirEntry>();
		for (Dir d : dirs) {
			Collection<DirEntry> set = d.getEntries();
			for (DirEntry e : set) {
				DirEntry tmpe = temp.get(e);
				if (tmpe == null) {
					// first time for this entry -> add
					e.refNum = 1;
					e.getRefs().clear();
					e.getRefs().add(d.drive);
					temp.put(e, e);
				} else {
					// this entry is already available -> increase ref
					tmpe.refNum++;
					tmpe.getRefs().add(d.drive);
				}
			}
		}

		entries.addAll(temp.keySet());

		return super.getEntries();
	}
}
