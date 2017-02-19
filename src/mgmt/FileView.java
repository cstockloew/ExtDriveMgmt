package mgmt;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import data.Base;
import data.Dir;
import data.DirEntry;
import data.Drive;
import data.Settings;

/**
 * The file view shows detailed information about a specific folder in tabular
 * format, i.e. information about all the files and sub folder contained in this
 * folder.
 * 
 * @author Carsten Stockloew
 *
 */
public class FileView extends JScrollPane {

	private static final long serialVersionUID = 1L;
	public JTable table;
	private SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy  HH:mm");
	private ArrayList<Dir> subdirs = new ArrayList<Dir>();
	protected DefaultTableModel model;
	private final int idxName = 1;
	private final int idxType = 2;
	private final int idxSize = 3;
	private final int idxDate = 4;
	// private final int idxRef = 5;
	private final int idxDir = 6;
	private FileInfoProvider fip = new FileInfoProvider();
	private JLabel statusLabel;
	private JLabel statusSelectionLabel;
	private boolean hideRef = true;
	private Date nulldate = new Date(0);

	FileView(JLabel statusLabel, JLabel statusSelectionLabel) {
		this.statusLabel = statusLabel;
		this.statusSelectionLabel = statusSelectionLabel;
		model = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == idxDate)
					return Date.class;
				if (columnIndex == idxSize)
					return Long.class;
				return super.getColumnClass(columnIndex);
			}
		};
		String[] columns = { "#Ref", "Name", "Type", "Size", "Date", "References", "isDir" };
		for (String col : columns) {
			model.addColumn(col);
		}
		addColumns();

		table = new JTable(model);
		// table.getColumnModel().getColumn(0).setPreferredWidth(35);
		// table.getColumnModel().getColumn(idxName).setPreferredWidth(350);
		// table.getColumnModel().getColumn(idxType).setPreferredWidth(180);
		// table.getColumnModel().getColumn(idxSize).setPreferredWidth(100);
		// table.getColumnModel().getColumn(idxDate).setPreferredWidth(100);
		// table.getColumnModel().getColumn(idxRef).setPreferredWidth(200);
		// table.getColumnModel().getColumn(idxDir).setPreferredWidth(0);
		// table.getSelectionModel().addListSelectionListener(this);
		table.removeColumn(table.getColumnModel().getColumn(idxDir));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		int[] colWidth = Settings.getSettings().colWidthFileView;
		for (int i = 0; i < colWidth.length; i++)
			table.getColumnModel().getColumn(i).setPreferredWidth(colWidth[i]);

		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		table.getColumnModel().getColumn(idxSize).setCellRenderer(rightRenderer);
		table.getColumnModel().getColumn(idxDate).setCellRenderer(new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				if (value instanceof Date) {
					if (value == nulldate)
						value = "";
					else
						value = df.format(value);
				}
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				// setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
				return c;
			}
		});
		table.getColumnModel().getColumn(idxSize).setCellRenderer(new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				if (value instanceof Long) {
					value = Util.convertDecimal((Long) value);
				}
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				setHorizontalAlignment(SwingConstants.RIGHT);
				return c;
			}
		});
		table.getColumnModel().getColumn(idxName).setCellRenderer(new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {

				String s = (String) value;
				Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if (row < subdirs.size())
					((JLabel) cell).setIcon(fip.getDirIcon());
				else
					((JLabel) cell).setIcon(fip.getIcon(s));
				((JLabel) cell).setText(modName(s));
				((JLabel) cell).setForeground(Color.BLACK);

				return cell;
			}
		});

		TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<DefaultTableModel>(model) {
			private void checkColumn(int column) {
				if (column < 0 || column >= getModelWrapper().getColumnCount()) {
					throw new IndexOutOfBoundsException("column beyond range of TableModel");
				}
			}

			private SortKey toggle(SortKey key) {
				if (key.getSortOrder() == SortOrder.ASCENDING) {
					return new SortKey(key.getColumn(), SortOrder.DESCENDING);
				}
				return new SortKey(key.getColumn(), SortOrder.ASCENDING);
			}

			@Override
			public void toggleSortOrder(int column) {
				checkColumn(column);
				if (isSortable(column)) {
					List<SortKey> keys = new ArrayList<SortKey>(getSortKeys());
					SortKey sortKey;
					int sortIndex;
					for (sortIndex = keys.size() - 1; sortIndex >= 0; sortIndex--) {
						if (keys.get(sortIndex).getColumn() == column) {
							break;
						}
					}
					if (sortIndex == -1) {
						// Key doesn't exist
						sortKey = new SortKey(column, SortOrder.ASCENDING);
						keys.add(0, sortKey);
					} else if (sortIndex == 1) {
						// It's the primary sorting key, toggle it
						keys.set(1, toggle(keys.get(1)));
					} else {
						// It's not the first, but was sorted on, remove old
						// entry, insert as first with ascending.
						keys.remove(sortIndex);
						keys.add(0, new SortKey(column, SortOrder.ASCENDING));
					}
					if (keys.size() > getMaxSortKeys()) {
						keys = keys.subList(0, getMaxSortKeys());
					}
					setSortKeys(keys);
				}
			}

			@Override
			public void setSortKeys(List<? extends SortKey> sortKeys) {
				List<SortKey> keysnew = new ArrayList<>();
				keysnew.add(new RowSorter.SortKey(idxDir, SortOrder.DESCENDING));
				for (SortKey key : sortKeys) {
					if (key.getColumn() != idxDir)
						keysnew.add(new RowSorter.SortKey(key.getColumn(), key.getSortOrder()));
				}
				super.setSortKeys(keysnew);
			}

		};
		table.setAutoCreateRowSorter(false);

		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
		sortKeys.add(new RowSorter.SortKey(idxDir, SortOrder.DESCENDING));
		sortKeys.add(new RowSorter.SortKey(idxType, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		table.setRowSorter(sorter);

		table.setShowGrid(false);
		setViewportView(table);

		Font myFont = new Font("Segoe UI", Font.PLAIN, 12);
		table.setFont(myFont);
		table.setForeground(Color.DARK_GRAY);
		getViewport().setBackground(table.getBackground());
		table.getColumnModel().setColumnMargin(10);

		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				JTable table = (JTable) me.getSource();
				Point p = me.getPoint();
				int row = table.rowAtPoint(p);
				if (me.getClickCount() != 2)
					return;
				row = table.convertRowIndexToModel(row);
				if (row >= subdirs.size())
					return;
				// 'row' is now the index in subdirs
				changeSubdir(subdirs.get(row));
			}
		});

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();

				if (lsm.isSelectionEmpty()) {
					statusSelectionLabel.setText("");
				} else {
					int num = 0;
					long size = 0;
					// Find out which indexes are selected.
					int minIndex = lsm.getMinSelectionIndex();
					int maxIndex = lsm.getMaxSelectionIndex();
					for (int i = minIndex; i <= maxIndex; i++) {
						if (lsm.isSelectedIndex(i)) {
							num++;
							size += (Long) table.getValueAt(i, idxSize);
						}
					}
					statusSelectionLabel
							.setText("     " + num + " elements selected (" + Util.convertByteUnit(size) + ")");
				}
			}
		});
	}

	protected String modName(String s) {
		return s;
	}

	protected void addColumns() {
	}

	protected void changeSubdir(Dir dir) {
		Main.instance.changeSubdir(dir);
	}

	private String getRefs(Base b) {
		String s = "";
		if (!hideRef) {
			List<Drive> drives = b.getRefs();
			int i = drives.size();
			for (Drive drive : drives) {
				s += drive.name;
				if (i-- > 1)
					s += ", ";
			}
		}
		return s;
	}

	public void setDir(Dir d) {
		hideRef = false;// !(d instanceof CombDir);
		subdirs.clear();
		subdirs.addAll(d.getSubDirs());
		Collection<DirEntry> entries = d.getEntries();

		model.setRowCount(0);
		for (Dir dir : subdirs) {
			model.addRow(new Object[] { hideRef ? "" : dir.getRefNum(), dir.name, fip.getDirType(), dir.size, nulldate,
					getRefs(dir), true, dir.path });
		}
		for (DirEntry e : entries) {
			model.addRow(new Object[] { hideRef ? "" : e.getRefNum(), e.name, fip.getType(e.name), e.size, e.date,
					getRefs(e), false, e.path });
		}

		// when using a custom table model: notify for changes
		// model.fireTableDataChanged();

		int tot = subdirs.size() + entries.size();
		long size = 0;
		for (DirEntry e : entries)
			size += e.size;
		statusLabel.setText(tot + " Elements (" + subdirs.size() + " folders, " + entries.size()
				+ " files)          Total size: " + Util.convertByteUnit(size));
		statusSelectionLabel.setText("");
	}
}
