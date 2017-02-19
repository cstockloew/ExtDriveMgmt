package data;

import mgmt.Util;

/**
 * Contains information about a drive.
 * 
 * @author Carsten Stockloew
 *
 */
public class Drive {
	public String letter;
	public String name;
	public String serial;
	public int filenum;
	public int dirnum;
	public long bytesUsed;
	public long bytesFree;

	public Dir root = new Dir(this, "");

	public void addEntry(String dir, DirEntry e) {
		Dir d = getOrAddDir(dir);
		d.addEntry(e);
	}

	public Dir getOrAddDir(String dir) {
		String[] arr = dir.split("\\\\");

		Dir d = root;
		for (String s : arr) {
			d = d.getOrAddSubDir(this, s);
		}

		return d;
	}

	@Override
	public String toString() {
		return name;
	}

	public void out() {
		long total = bytesUsed + bytesFree;
		System.out.println("Drive: ");
		System.out.println("    Letter:      " + letter);
		System.out.println("    Name:        " + name);
		System.out.println("    Serial:      " + serial);
		System.out.println("    FileNum:     " + filenum);
		System.out.println("    Bytes Used:  " + bytesUsed + " = " + Util.convertByteUnit(bytesUsed));
		System.out.println("    Bytes Free:  " + bytesFree + " = " + Util.convertByteUnit(bytesFree));
		System.out.println("    Bytes Total: " + total + " = " + Util.convertByteUnit(total));
	}
}
