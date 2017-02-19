package mgmt;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import data.Drive;
import data.Settings;

/**
 * The drive view shows information about all drives.
 * 
 * @author Carsten Stockloew
 *
 */
public class DriveView extends JScrollPane {
	private int warnThres = 90;

	private static final long serialVersionUID = 1L;
	public JTable table;

	class ProgressCellRender extends JProgressBar implements TableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			int progress = 0;
			if (value instanceof Float) {
				progress = Math.round(((Float) value) * 100f);
			}
			setValue(progress);
			if (progress < warnThres)
				setForeground(Color.blue);
			else
				setForeground(Color.RED);
			setBackground(Color.WHITE);
			setStringPainted(true);
			return this;
		}
	}

	DriveView(List<Drive> drives) {
		DefaultTableModel model = new DefaultTableModel();
		model.addColumn("Name");
		model.addColumn("Serial");
		model.addColumn("#Files");
		model.addColumn("#Dirs");
		model.addColumn("Used");
		model.addColumn("Free");
		model.addColumn("Total");
		model.addColumn("Percent Used");

		table = new JTable(model);
		// getContentPane().add(table, BorderLayout.WEST);
		// JScrollPane scrollpaneTable = new JScrollPane(table);
		// table.getColumnModel().getColumn(0).setPreferredWidth(120);
		// table.getColumnModel().getColumn(1).setPreferredWidth(20);
		// table.getColumnModel().getColumn(2).setPreferredWidth(100);
		// table.getSelectionModel().addListSelectionListener(this);
		int[] colWidth = Settings.getSettings().colWidthDriveView;
		for (int i = 0; i < colWidth.length; i++)
			table.getColumnModel().getColumn(i).setPreferredWidth(colWidth[i]);

		for (Drive d : drives) {
			float percent = (float) d.bytesUsed / (float) (d.bytesUsed + d.bytesFree);

			model.addRow(new Object[] { d.name, d.serial, d.filenum, d.dirnum, Util.convertByteUnit(d.bytesUsed),
					Util.convertByteUnit(d.bytesFree), Util.convertByteUnit(d.bytesUsed + d.bytesFree), percent });
		}

		setViewportView(table);

		TableRowSorter<DefaultTableModel> trs = new TableRowSorter<DefaultTableModel>(model);
		// trs.setComparator(column, comparator);
		table.setAutoCreateRowSorter(false);
		table.setRowSorter(trs);

		table.getColumnModel().getColumn(7).setCellRenderer(new ProgressCellRender());

	}
}
