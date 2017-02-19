package data;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A specialized directory as result of a search. May contain the some duplicate
 * elements (e.g. files with the same name, size, and date) if they are in
 * different sub folders.
 * 
 * @author Carsten Stockloew
 *
 */
public class SearchDir extends Dir {

	List<Dir> lstDirs = new LinkedList<Dir>();
	List<DirEntry> lstEntries = new LinkedList<DirEntry>();

	public SearchDir(List<Dir> lstDirs, List<DirEntry> lstEntries) {
		super(null, "");
		this.lstDirs = lstDirs;
		this.lstEntries = lstEntries;
	}

	@Override
	public Collection<Dir> getSubDirs() {
		return lstDirs;
	}

	@Override
	public Collection<DirEntry> getEntries() {
		return lstEntries;
	}
}
