package mgmt;

import javax.swing.JLabel;

import data.Dir;
import data.Settings;

/**
 * The search view shows detailed information of folders and files in tabular
 * format, as a result of a search. The search view is a specialized
 * {@link FileView} that contains an additional column for the file's path and
 * marks the search term as part of the elements name in red color.
 * 
 * @author Carsten Stockloew
 *
 */
public class SearchView extends FileView {
	private static final long serialVersionUID = 1L;

	private String mark;
	private String fs = "<font color=\"red\">";
	private String fe = "</font>";

	SearchView(JLabel statusLabel, JLabel statusSelectionLabel) {
		super(statusLabel, statusSelectionLabel);

		int[] colWidth = Settings.getSettings().colWidthSearchView;
		for (int i = 0; i < colWidth.length; i++)
			table.getColumnModel().getColumn(i).setPreferredWidth(colWidth[i]);
	}

	@Override
	protected void addColumns() {
		model.addColumn("Path");
	}

	@Override
	protected void changeSubdir(Dir dir) {
		Main.instance.changeSubdir(dir.path + "\\" + dir.name);
	}

	public void setMark(String mark) {
		this.mark = mark.toLowerCase();
	}

	@Override
	protected String modName(String s) {
		// s = s.replace(mark, "<font color=\"red\">" + mark + "</font>");
		// s = s.replace(mark, "<mark>" + mark + "</mark>");
		// s = s.replaceAll("(?i)"+Pattern.quote(mark), "<font color=\"red\">" +
		// mark + "</font>");
		if (mark.length() == 0)
			return s;

		String low = s.toLowerCase();
		StringBuffer sb = new StringBuffer();

		int idx = low.indexOf(mark, 0);
		int idxlast = 0;
		if (idx == -1) {
			System.err.println("no search result in string, this shouldn't happen");
			return s;
		}

		do {
			// copy from idxlast to idx
			sb.append(s.substring(idxlast, idx));
			// copy search string (from original!)
			sb.append(fs);
			sb.append(s.substring(idx, idx + mark.length()));
			sb.append(fe);

			// calc new
			idxlast = idx + mark.length();
			idx = low.indexOf(mark, idxlast);
		} while (idx != -1);
		// copy tail
		sb.append(s.substring(idxlast));
		return "<html>" + sb.toString() + "</html>";
	}
}
