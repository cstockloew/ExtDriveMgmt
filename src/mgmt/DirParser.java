package mgmt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import data.DirEntry;
import data.Drive;

/**
 * Parser for a file that is the result of calling 'dir' on the DOS command
 * line. Currently, only german language is supported (tested with Windows 10).
 * 
 * @author Carsten Stockloew
 *
 */
public class DirParser {

	public static String DRIVE_START = " Datentr";
	public static String SERIAL_NR_START = " Volumeseriennummer";
	public static String DIR_START = " Verzeichnis von ";
	public static String FINAL_START = "     Anzahl der angezeigten Dateien:";
	public static String DIR_ENTRY = "<DIR>";

	public static int posLetter = 25;
	public static int posName = 32;
	public static int posSerial = 21;
	public static int posDir = 20;
	public static int posDirEntry = 21;

	Drive drive;
	int linenum;
	long size;
	BufferedReader br;

	String read() throws IOException {
		linenum++;
		String line = br.readLine();
		if (line == null)
			throw new IOException("End of file reached");
		return line;
	}

	void skipEmptyLine() throws IOException {
		// skip one empty line
		if (read().length() != 0)
			throw new IOException("Empty line not available at line " + linenum);
	}

	void readEntry(String dir, String line) throws IOException {
		// skip dirs, they have their own section
		if (line.contains(DIR_ENTRY))
			return;

		DirEntry e = new DirEntry();

		// date
		try {
			e.date = new SimpleDateFormat().parse(line.substring(0, 17));
		} catch (ParseException ex) {
			ex.printStackTrace();
		}

		// size
		String sizestr = line.substring(17, 35);
		sizestr = sizestr.replace(".", "");
		e.size = Long.parseLong(sizestr.trim());
		size += e.size;

		// name
		e.name = line.substring(36);

		drive.addEntry(dir, e);
	}

	void readDir(String line) throws IOException {
		String dir = line.substring(posDir);
		drive.getOrAddDir(dir);

		skipEmptyLine();

		while (!(line = read()).startsWith(" ")) {
			// parse entry
			readEntry(dir, line);
		}
		skipEmptyLine();
	}

	void readFinal() throws IOException {
		String s;
		String line = read().trim();

		s = line.substring(0, line.indexOf(' '));
		int filenum = Integer.parseInt(s);
		drive.filenum = filenum;

		s = line.substring(line.indexOf(',') + 1, line.indexOf(" Bytes"));
		Long bytes = Long.parseLong(s.replace(".", "").trim());
		drive.bytesUsed = bytes;

		line = read().trim();

		s = line.substring(0, line.indexOf(' '));
		int dirnum = Integer.parseInt(s);
		drive.dirnum = dirnum;

		s = line.substring(line.indexOf(',') + 1, line.indexOf(" Bytes"));
		Long free = Long.parseLong(s.replace(".", "").trim());
		drive.bytesFree = free;

		if (drive.bytesUsed != size) {
			System.err.println("Size does not sum up (total: " + drive.bytesUsed + ", cumulated: " + size + ")");
		}
	}

	boolean parse(String filename) {
		drive = new Drive();
		linenum = 0;
		size = 0;

		File file = new File(filename);
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		String line;
		try {
			line = read();
			if (!line.startsWith(DRIVE_START))
				throw new IOException("File does not start with '" + DRIVE_START + "'");
			drive.letter = line.substring(posLetter, posLetter + 1);
			drive.name = line.substring(posName);

			line = read();
			if (!line.startsWith(SERIAL_NR_START))
				throw new IOException("File does not start with '" + SERIAL_NR_START + "'");
			drive.serial = line.substring(posSerial);

			skipEmptyLine();

			// read dirs
			while (true) {
				line = read();
				if (line.startsWith(FINAL_START)) {
					readFinal();
					break;
				}

				if (line.startsWith(DIR_START))
					readDir(line);
				else
					throw new IOException("Unexpected end of file at line " + linenum);
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				br.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return false;
		}

		drive.out();
		try {
			br.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return true;
	}
}
