package mgmt;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import data.Dir;
import data.DirEntry;
import data.SearchDir;

/**
 * The search panel is shown on the bottom right to enter a search string and to
 * perform the search. Search results are shown in the {@link SearchView}.
 * 
 * @author Carsten Stockloew
 *
 */
public class SearchPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTextField text;
	private JButton startButton;

	SearchPanel() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		text = new JTextField();
		// text.setText("ab");
		text.setPreferredSize(new Dimension(250, text.getPreferredSize().height));
		add(text);

		startButton = new JButton("Search");
		add(startButton);

		Action action = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				List<Dir> lstDirs = new LinkedList<Dir>();
				List<DirEntry> lstEntries = new LinkedList<DirEntry>();

				String searchText = text.getText();
				if ("*".equals(searchText.trim()) || "*.*".equals(searchText.trim()))
					searchText = "";

				collectData(Main.instance.combDir, lstDirs, lstEntries, "", searchText.toLowerCase());
				if (lstDirs.size() + lstEntries.size() > 100) {
					DecimalFormat nf = new DecimalFormat();
					String s1 = nf.format(lstDirs.size());
					String s2 = nf.format(lstEntries.size());

					int res = JOptionPane.showConfirmDialog(null,
							"Found " + s1 + " folders and " + s2 + " files. Do you really want to show them all?",
							"Too many results", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
					if (res == JOptionPane.CANCEL_OPTION)
						return;
				}

				// now show the results
				Main main = Main.instance;
				// set the correct view on the right window
				int pos = main.split.getDividerLocation();
				main.split.setRightComponent(main.searchView);
				main.split.setDividerLocation(pos);

				Dir d = new SearchDir(lstDirs, lstEntries);
				main.searchView.setMark(searchText);
				main.searchView.setDir(d);
			}
		};

		text.addActionListener(action);
		startButton.addActionListener(action);
	}

	private void collectData(Dir d, List<Dir> lstDirs, List<DirEntry> lstEntries, String path, String text) {
		for (DirEntry e : d.getEntries()) {
			if (e.name.toLowerCase().contains(text)) {
				e.path = path;
				lstEntries.add(e);
			}
		}
		for (Dir sub : d.getSubDirs()) {
			if (sub.name.toLowerCase().contains(text)) {
				sub.path = path;
				lstDirs.add(sub);
			}
			collectData(sub, lstDirs, lstEntries, path + "\\" + sub.name, text);
		}
	}
}
