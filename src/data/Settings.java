package data;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.JTable;

import mgmt.Main;

/**
 * The app's settings, e.g. size and position of the frame, column width etc.
 * 
 * @author Carsten Stockloew
 *
 */
public class Settings implements Serializable {
	private static final long serialVersionUID = 1L;

	private static Settings instance = null;

	public int windowPosX = 50;
	public int windowPosY = 50;
	public int windowWidth = 1200;
	public int windowHeight = 600;
	public boolean isMaximized = false;
	public int dividerLocation = 150;
	public int[] colWidthFileView = null;
	public int[] colWidthSearchView = null;
	public int[] colWidthDriveView = null;

	private Settings() {
	}

	public static Settings getSettings() {
		if (instance == null)
			instance = new Settings();
		return instance;
	}

	public static void setSettings(Settings s) {
		instance = s;
		if (s.colWidthFileView == null)
			s.colWidthFileView = new int[] { 35, 350, 180, 100, 100, 200 };
		if (s.colWidthSearchView == null)
			s.colWidthSearchView = new int[] { 35, 350, 180, 100, 100, 200, 400 };
		if (s.colWidthDriveView == null)
			s.colWidthDriveView = new int[] { 80, 80, 80, 80, 80, 80, 80, 80 };
	}

	private static int[] getColWidth(JTable table) {
		int num = table.getColumnCount();
		int[] colWidth = new int[num];
		for (int i = 0; i < num; i++) {
			colWidth[i] = table.getColumnModel().getColumn(i).getPreferredWidth();
		}
		return colWidth;
	}

	public static void saveSettings() {
		// collect values
		Settings.getSettings().isMaximized = (Main.instance.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0;
		if (Settings.getSettings().isMaximized) {
			// remove the maximize state to get normal windows values (x,y,w,h)
			Main.instance.setExtendedState(Main.instance.getExtendedState() & ~JFrame.MAXIMIZED_BOTH);
		}
		Settings.getSettings().windowPosX = Main.instance.getLocation().x;
		Settings.getSettings().windowPosY = Main.instance.getLocation().y;
		Settings.getSettings().windowWidth = Main.instance.getSize().width;
		Settings.getSettings().windowHeight = Main.instance.getSize().height;
		Settings.getSettings().dividerLocation = Main.instance.split.getDividerLocation();
		Settings.getSettings().colWidthDriveView = getColWidth(Main.instance.driveView.table);
		Settings.getSettings().colWidthFileView = getColWidth(Main.instance.fileView.table);
		Settings.getSettings().colWidthSearchView = getColWidth(Main.instance.searchView.table);

		// write to file
		FileOutputStream fout;
		try {
			fout = new FileOutputStream(".\\settings.dat");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(Settings.getSettings());
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadSettings() {
		// read from file
		FileInputStream fin;
		try {
			fin = new FileInputStream(".\\settings.dat");
			ObjectInputStream ois = new ObjectInputStream(fin);
			Settings.setSettings((Settings) ois.readObject());
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			// e.printStackTrace();
		}
	}

}
