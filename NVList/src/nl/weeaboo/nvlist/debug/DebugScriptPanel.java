package nl.weeaboo.nvlist.debug;

import static nl.weeaboo.lua2.LuaUtil.escape;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import nl.weeaboo.awt.AwtUtil;
import nl.weeaboo.awt.MessageBox;
import nl.weeaboo.common.StringUtil;
import nl.weeaboo.filesystem.FileSystemUtil;
import nl.weeaboo.game.GameLog;
import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.vn.impl.lua.BaseScriptLib;
import nl.weeaboo.vn.impl.lua.LuaNovel;
import nl.weeaboo.vn.impl.nvlist.Novel;
import nl.weeaboo.vn.parser.ParseException;

@SuppressWarnings("serial")
public class DebugScriptPanel extends JPanel {

	private final Object lock;
	private final Novel novel;
	
	private final ImageIcon folderI, fileI, lvnI, luaI;
	
	private final JButton showInfoButton, callButton;
	private final JTree tree;
	private final Timer timer;
	
	public DebugScriptPanel(Object l, Novel nvl) {
		lock = l;
		novel = nvl;

		folderI = null; //getFileTypeIcon("folder");
		fileI = getFileTypeIcon("file");
		lvnI = getFileTypeIcon("lvn");
		luaI = getFileTypeIcon("lua");
		
		showInfoButton = new JButton("Script Info");
		showInfoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ScriptFolderInfo si;
				synchronized (lock) {
					si = generateScriptInfo(novel, getSelectedScript());
				}
				
				MessageBox mb = new MessageBox("Script Info", si.toString());
				mb.showMessage(DebugScriptPanel.this);
			}
		});
		
		callButton = new JButton("Call Script");
		callButton.setEnabled(false);
		callButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					callScript(getSelectedScript());
				} catch (LuaException le) {
					GameLog.w("Error calling script", le);
					AwtUtil.showError("Error calling script: " + le);
				}
			}
		});
		
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		topPanel.add(callButton);
		topPanel.add(showInfoButton);
		
		tree = new JTree();
		tree.setBorder(new EmptyBorder(5, 5, 5, 5));
		tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
		tree.setRowHeight(0);
		
		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			
			private final Border LABEL_BORDER = BorderFactory.createEmptyBorder(2, 1, 2, 1);
			
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
					boolean expanded, boolean leaf, int row, boolean hasFocus)
			{
				Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);		        
				if (c instanceof JLabel) {
					JLabel label = (JLabel)c;
					label.setBorder(LABEL_BORDER);
					
					if (value instanceof ScriptNode) {
						ScriptNode node = (ScriptNode)value;
						String path = node.path;
						if (path.endsWith("/")) {
							if (folderI != null) label.setIcon(folderI);
						} else if (path.endsWith(".lvn")) {
							label.setIcon(lvnI);
						} else if (path.endsWith(".lua")) {
							label.setIcon(luaI);
						} else {
							label.setIcon(fileI);
						}
					}
				}
				return c;
			}
		});
		
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				callButton.setEnabled(getSelectedScript() != null);
			}
		});
		
		JScrollPane treePane = new JScrollPane(tree);
		treePane.setPreferredSize(new Dimension(100, 200));
		
		setLayout(new BorderLayout(5, 5));
		add(topPanel, BorderLayout.NORTH);
		add(treePane, BorderLayout.CENTER);
		
		addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				Component c = e.getChanged();
				if (c == null) c = DebugScriptPanel.this;
				
				if (!c.isDisplayable() || !c.isVisible() || !isVisible()) {
					timer.stop();
				} else if (c.isVisible() && isVisible()) {
					timer.start();
					timerUpdate();
				}
			}
		});
		
		timer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timerUpdate();
			}
		});
		timer.setRepeats(false);
		timer.start();		
	}
	
	//Functions
	private ImageIcon getFileTypeIcon(String filename) {
		return new ImageIcon(getClass().getResource("res/filetype-" + filename + ".png"));		
	}
	
	private static ScriptFolderInfo generateScriptInfo(LuaNovel novel, String selected) {
		ScriptFolderInfo si = new ScriptFolderInfo();
		
		BaseScriptLib scriptLib = novel.getScriptLib();
		
		List<ScriptInfo> fileInfo = new ArrayList<ScriptInfo>();
		for (String file : scriptLib.getScriptFiles("", false)) {
			ScriptInfo info = null;
			try {
				info = ScriptInfo.fromScript(scriptLib, file);
			} catch (ParseException pe) {
				GameLog.d("Error reading script info for: " + file, pe);
			} catch (IOException ioe) {
				GameLog.d("Error reading script info for: " + file, ioe);
			}
			
			if (info != null) {
				fileInfo.add(info);
			}
			
			if (file.equals(selected)) {
				si.setSelected(info);
			}
		}
		
		si.setExternalScriptFiles(fileInfo);
						
		return si;
	}
	
	protected void callScript(String filename) throws LuaException {
		if (filename == null) {
			return;
		}
		
		synchronized (lock) {
			final String fn = escape(StringUtil.stripExtension(filename));
			final String cmd = "call(\"" + fn + "\")";
			novel.eval(cmd);
		}
	}
	
	protected void timerUpdate() {
		List<String> scriptFiles;
		synchronized (lock) {
			BaseScriptLib scriptLib = novel.getScriptLib();
			scriptFiles = scriptLib.getScriptFiles("", false);
		}
		Collections.sort(scriptFiles, FileSystemUtil.getFilenameComparator());
		
		ScriptNode rootNode = createFolderNode("/", "script");
		
		Map<String, ScriptNode> nodes = new HashMap<String, ScriptNode>();
		nodes.put("/", rootNode);		
		for (String path : scriptFiles) {
			createScriptNode(nodes, path);
		}
		
		tree.setModel(new DefaultTreeModel(rootNode));
	}
	
	private static ScriptNode createScriptNode(Map<String, ScriptNode> nodes, String path) {
		if (path == null || path.length() == 0) {
			return nodes.get("/");
		}
		
		ScriptNode node = nodes.get(path);
		if (node != null) {
			return node;
		}
		
		String parentPath = path;
		if (parentPath.length() > 1) {
			int i = (parentPath.endsWith("/") ? parentPath.length()-2 : parentPath.length()-1);
			parentPath = parentPath.substring(0, parentPath.lastIndexOf('/', i)+1);
		}
		
		ScriptNode parent = createScriptNode(nodes, parentPath);

		String displayPath = path.substring(parentPath.length());
		if (displayPath.endsWith("/")) {
			displayPath = displayPath.substring(0, displayPath.length()-1);
		}
		
		if (path.endsWith("/")) {
			node = createFolderNode(path, displayPath);
		} else {
			node = createFileNode(path, displayPath);
		}
		parent.add(node);
		nodes.put(path, node);
		return node;
	}
	
	private static ScriptNode createFolderNode(String path, String displayPath) {
		if (!path.endsWith("/")) path = path + "/";
		return new ScriptNode(path, displayPath);
	}
	
	private static ScriptNode createFileNode(String path, String displayPath) {
		while (path.endsWith("/")) {
			path = path.substring(0, path.length()-1);
		}
		return new ScriptNode(path, displayPath);
	}
	
	//Getters
	protected String getSelectedScript() {
		TreePath treePath = tree.getSelectionPath();
		if (treePath != null) {
			Object selected = treePath.getLastPathComponent();
			if (selected instanceof ScriptNode) {
				String path = ((ScriptNode)selected).path;
				return path;
			}
		}
		return null;
	}
	
	//Setters
	
	//Inner Classes
	private static class ScriptFolderInfo {
		
		private final List<ScriptInfo> scriptFiles = new ArrayList<ScriptInfo>();
		private int totalLines, totalWords;
		private ScriptInfo selected;
		
		public void setExternalScriptFiles(Collection<ScriptInfo> files) {
			scriptFiles.clear();
			scriptFiles.addAll(files);
			
			totalLines = 0;
			totalWords = 0;
			for (ScriptInfo si : scriptFiles) {
				totalLines += si.getLineCount();
				totalWords += si.getWordCount();
			}
		}
		
		public void setSelected(ScriptInfo sel) {
			selected = sel;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("<html><pre>");
			sb.append("\n--- Total Stats ---");
			sb.append("\nScript Files: " + scriptFiles.size());
			sb.append("\nLines: " + totalLines);
			sb.append("\nWords: " + totalWords);
			if (selected != null) {
				sb.append("\n--- Selected Script ---");
				sb.append("\nFilename: " + selected.getFilename());
				sb.append("\nLines: " + selected.getLineCount());
				sb.append("\nWords: " + selected.getWordCount());
			}
			sb.append("</pre></html>");
			return sb.toString();
		}
	}
	
	private static class ScriptNode extends DefaultMutableTreeNode {

		private final String path;
		private final String displayPath;
		
		public ScriptNode(String path, String displayPath) {
			this.path = path;
			this.displayPath = displayPath;
		}

		@Override
		public String toString() {
			return displayPath;
		}
		
	}
	
}
