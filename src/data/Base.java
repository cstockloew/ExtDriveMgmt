package data;

import java.util.List;

/**
 * Base class for folders ({@link Dir}) and files ({@link DirEntry}).
 * 
 * @author Carsten Stockloew
 *
 */
public abstract class Base {
	public String name;
	public long size;

	// path is only stored for search-results
	public String path;

	/**
	 * Get the list of references, i.e. the list of drives that contain this
	 * element
	 */
	public abstract List<Drive> getRefs();

	/**
	 * Get the number of references. This corresponds to the size of
	 * {@link #getRefs()}.
	 */
	public abstract int getRefNum();
}
