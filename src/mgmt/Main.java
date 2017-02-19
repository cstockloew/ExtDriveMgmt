package mgmt;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import data.CombDir;
import data.Dir;
import data.Drive;
import data.Settings;

/**
 * Main Frame
 * 
 * @author Carsten Stockloew
 *
 */
public class Main extends JFrame implements TreeSelectionListener, TreeWillExpandListener {

	private static final long serialVersionUID = 1L;
	public static Main instance = null;
	private JTree tree;
	private DefaultMutableTreeNode root = null;
	private DefaultTreeModel treeModel;

	private DefaultMutableTreeNode nodeDevices;
	private DefaultMutableTreeNode nodeCombined;

	private Icon iconDrive;
	private Icon iconComputer;
	//private Icon iconHome;

	public JSplitPane split;
	private JLabel statusLabel;
	private JLabel statusSelectionLabel;

	public FileView fileView;
	public SearchView searchView;
	public DriveView driveView;
	private SearchPanel searchPanel;

	private List<Drive> drives;
	public CombDir combDir;

	public Main() {
		super("External Drive Manager");
		instance = this;
		Settings.loadSettings();

		drives = loadAllFiles();
		List<Dir> rootdirs = new ArrayList<Dir>();
		for (Drive d : drives) {
			rootdirs.add(d.root);
		}
		combDir = new CombDir("-root-", rootdirs);
		combDir.calcSize();

		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			UIManager.put("Tree.leafIcon", UIManager.get("Tree.openIcon"));
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		SwingUtilities.updateComponentTreeUI(this);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocation(Settings.getSettings().windowPosX, Settings.getSettings().windowPosY);
		this.setLayout(new BorderLayout());

		// status bar
		JPanel statusPanel = new JPanel();
		// statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusPanel.setBorder(new EmptyBorder(4, 6, 4, 6));
		add(statusPanel, BorderLayout.SOUTH);
		// statusPanel.setPreferredSize(new Dimension(getWidth(), 40));
		statusPanel.setLayout(new BorderLayout());
		statusLabel = new JLabel("");
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusSelectionLabel = new JLabel("");
		statusSelectionLabel.setHorizontalAlignment(SwingConstants.LEFT);
		JPanel statusFilePanel = new JPanel();
		statusFilePanel.add(statusLabel);
		statusFilePanel.add(statusSelectionLabel);
		statusPanel.add(statusFilePanel, BorderLayout.WEST);

		searchPanel = new SearchPanel();
		statusPanel.add(searchPanel, BorderLayout.EAST);

		// Tree view
		root = new DefaultMutableTreeNode("root");
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		tree.setBorder(new EmptyBorder(4, 4, 4, 4));
		tree.setRootVisible(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(this);
		tree.addTreeWillExpandListener(this);

		nodeDevices = new DefaultMutableTreeNode("Devices");
		nodeCombined = new DefaultMutableTreeNode("Combined");
		root.add(nodeDevices);
		root.add(nodeCombined);
		// add drives
		for (Drive d : drives) {
			// add the drive
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(d);
			nodeDevices.add(node);

			// and add all root dirs
			Dir root = d.root;
			addChildren(node, root.getSubDirs());
		}
		// add combined
		addChildren(nodeCombined, combDir.getSubDirs());
		// reload
		treeModel.reload(root);
		expand(nodeDevices);
		expand(nodeCombined);

		// create views
		fileView = new FileView(statusLabel, statusSelectionLabel);
		searchView = new SearchView(statusLabel, statusSelectionLabel);
		driveView = new DriveView(drives);

		// Split pane
		JScrollPane pane = new JScrollPane();
		pane.setViewportView(tree);
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pane, driveView);
		split.setDividerSize(3);
		add(split, BorderLayout.CENTER);

		pack();
		split.setDividerLocation(Settings.getSettings().dividerLocation);
		setSize(Settings.getSettings().windowWidth, Settings.getSettings().windowHeight);
		if (Settings.getSettings().isMaximized)
			setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);

		iconDrive = UIManager.getIcon("FileView.hardDriveIcon");
		iconComputer = UIManager.getIcon("FileView.computerIcon");
		//iconHome = UIManager.getIcon("FileChooser.homeFolderIcon");

		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
					boolean isLeaf, int row, boolean focused) {
				Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, focused);
				if (value == nodeDevices || value == nodeCombined) {
					setIcon(iconComputer);
				} else {
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
					if (node.getUserObject() instanceof Drive) {
						setIcon(iconDrive);
					}
				}
				return c;
			}
		});

		tree.setSelectionRow(1 + drives.size());

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Settings.saveSettings();
				e.getWindow().dispose();
			}
		});

		setVisible(true);
	}

	private void expand(DefaultMutableTreeNode node) {
		TreeNode[] path = node.getPath();
		TreePath treePath = new TreePath(path);
		tree.expandPath(treePath);
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		TreePath path = e.getPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

		// set the correct view on the right window
		int pos = split.getDividerLocation();
		if (node == nodeDevices) {
			statusLabel.setText("");
			split.setRightComponent(driveView);
		} else {
			split.setRightComponent(fileView);
		}
		split.setDividerLocation(pos);

		if (node == nodeCombined) {
			fileView.setDir(combDir);
			return;
		}

		// if a dir is selected, populate the right view
		Object o = node.getUserObject();
		if (o instanceof Drive)
			fileView.setDir(((Drive) o).root);
		if (o instanceof Dir) {
			fileView.setDir((Dir) o);
		}
	}

	private void addChildren(DefaultMutableTreeNode node, Collection<Dir> dirs) {
		List<Dir> l = new ArrayList<Dir>(dirs);
		Collections.sort(l, new Comparator<Dir>() {
			@Override
			public int compare(Dir o1, Dir o2) {
				return o1.name.compareToIgnoreCase(o2.name);
			}
		});
		for (Dir subdir : l) {
			// add subdir as child of node (=subnode)
			DefaultMutableTreeNode subnode = new DefaultMutableTreeNode(subdir);
			node.add(subnode);
		}
	}

	@Override
	public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
		TreePath path = event.getPath();
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();

		// For each of the children, check if anyone of the has child nodes.
		// This would mean that the children have been set already.
		for (Enumeration<?> e = parent.children(); e.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			if (node.getChildCount() != 0)
				return;
		}

		// no sub-children available -> populate all children with sub-children
		for (Enumeration<?> e = parent.children(); e.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			Dir d = (Dir) node.getUserObject();
			addChildren(node, d.getSubDirs());
		}
	}

	@Override
	public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
	}

	public void changeSubdir(Dir d) {
		TreePath path = tree.getSelectionPath();
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getLastPathComponent();

		for (Enumeration<?> e = parent.children(); e.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			Dir dir = (Dir) node.getUserObject();
			if (dir == d) {
				expand(node);
				tree.setSelectionPath(path.pathByAddingChild(node));
				return;
			}
		}
	}

	public void changeSubdir(String filepath) {
		String el[] = filepath.substring(1).split("\\\\");

		DefaultMutableTreeNode parent = nodeCombined;
		for (int i = 0; i < el.length; i++) {
			for (Enumeration<?> e = parent.children(); e.hasMoreElements();) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
				Dir dir = (Dir) node.getUserObject();
				if (dir.name.equals(el[i])) {
					expand(node);
					if (i == el.length - 1)
						tree.setSelectionPath(new TreePath(node.getPath()));
					parent = node;
					break;
				}
			}
		}
	}

	private List<Drive> loadAllFiles() {
		List<Drive> l = new LinkedList<Drive>();
		DirParser p = new DirParser();

		File root = new File(".");
		File files[] = root.listFiles();
		for (File f : files) {
			if (f.isFile() && f.getName().endsWith(".txt")) {
				// System.out.println(f.getName());
				if (p.parse(f.getName())) {
					l.add(p.drive);
					p.drive.root.calcSize();
				}
			}
		}

		return l;
	}

	public static void main(String[] args) {
		new Main();
	}
}
