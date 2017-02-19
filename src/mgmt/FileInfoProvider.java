package mgmt;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

/**
 * Provides additional system-specific information (icon and type) about files
 * and folders as they are registered in the underlying operating system.
 * 
 * @author Carsten Stockloew
 *
 */
public class FileInfoProvider {
	FileSystemView fileSystemView;

	HashMap<String, String> cacheType = new HashMap<String, String>();
	HashMap<String, Icon> cacheIcon = new HashMap<String, Icon>();

	private Icon iconDir = null;
	private String typeDir = null;

	public FileInfoProvider() {
		fileSystemView = FileSystemView.getFileSystemView();
	}

	private String getExt(String filename) {
		int pos = filename.lastIndexOf('.');
		if (pos == -1)
			return "";
		return filename.substring(pos);
	}

	/**
	 * Get the icon of a folder.
	 */
	public Icon getDirIcon() {
		if (iconDir == null) {
			File f = new File(".");
			iconDir = fileSystemView.getSystemIcon(f);
		}
		return iconDir;
	}

	/**
	 * Get the type of a folder.
	 */
	public String getDirType() {
		if (typeDir == null) {
			File f = new File(".");
			typeDir = fileSystemView.getSystemTypeDescription(f);
		}
		return typeDir;
	}

	/**
	 * Get the icon of a given file. Only the file extension is evaluated.
	 */
	public Icon getIcon(String filename) {
		String ext = getExt(filename);
		Icon icon = cacheIcon.get(ext);
		if (icon != null)
			return icon;

		File f = null;
		try {
			f = File.createTempFile("ExtDriveMgmtDummy", ext);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (f == null)
			return null;

		icon = fileSystemView.getSystemIcon(f);
		cacheIcon.put(ext, icon);
		return icon;
	}

	/**
	 * Get the type of a given file. Only the file extension is evaluated.
	 */
	public String getType(String filename) {
		String ext = getExt(filename);
		String type = cacheType.get(ext);
		if (type != null)
			return type;

		File f = null;
		try {
			f = File.createTempFile("ExtDriveMgmtDummy", ext);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (f == null)
			return "-unknown-";

		type = fileSystemView.getSystemTypeDescription(f);
		cacheType.put(ext, type);
		return type;
	}
}
