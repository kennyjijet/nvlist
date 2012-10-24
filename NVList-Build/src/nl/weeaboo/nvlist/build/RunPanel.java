package nl.weeaboo.nvlist.build;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import nl.weeaboo.awt.AwtUtil;
import nl.weeaboo.common.StringUtil;
import nl.weeaboo.system.ProcessUtil;
import nl.weeaboo.system.SystemUtil;

@SuppressWarnings("serial")
public class RunPanel extends JPanel {

	private final BuildCommandPanel buildCommandPanel;
	private final ConsoleOutputPanel outputPanel;
	private final JComboBox combo;
	private final JButton button;
	private File engineFolder;
	private File projectFolder;
	private Collection<Launcher> launchers;	
	
	public RunPanel(BuildCommandPanel buildPanel, ConsoleOutputPanel console) {
		buildCommandPanel = buildPanel;
		outputPanel = console;
		launchers = new ArrayList<Launcher>();
		
		JLabel label = new JLabel("Launcher ");
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		
		combo = new JComboBox(launchers.toArray());
		combo.setOpaque(false);
		
		button = new RunButton();
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (combo.getSelectedItem() instanceof Launcher) {
					try {
						run((Launcher)combo.getSelectedItem());
						return;
					} catch (IOException ioe) {
						ioe.printStackTrace();
						//Try a rebuild..?
					}
				}
				
				if (buildCommandPanel != null) {
					int r = JOptionPane.showConfirmDialog(RunPanel.this, "A rebuild is required to run. Do you want to start a rebuild now?",
							"Rebuild required", JOptionPane.OK_CANCEL_OPTION);
					if (r != JOptionPane.OK_OPTION) {
						return;
					}
					
					buildCommandPanel.rebuild(new ProcessCallback() {
						@Override
						public void run(int exitCode) {
							update();
							try {
								RunPanel.this.run((Launcher)combo.getSelectedItem());
							} catch (IOException ioe) {
								AwtUtil.showError(ioe);
							}
						}
					});
				}
			}
		});

		updateRunButtonEnabled();
		combo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateRunButtonEnabled();
			}
		});

		setOpaque(false);
		setLayout(new BorderLayout(10, 10));
		add(label, BorderLayout.WEST);
		add(combo, BorderLayout.CENTER);
		add(button, BorderLayout.EAST);
	}
	
	//Functions
	protected void run(Launcher launcher) throws IOException {
		if (launcher == null) {
			return;
		}
		
		String cmd = String.format("\"%s\"", launcher.file.getAbsolutePath().replace('\\', '/'));
		outputPanel.process(ProcessUtil.execInDir(cmd, engineFolder.toString()), null);			
	}
	
	public void update() {
		launchers.clear();
		
		Object oldSelected = combo.getSelectedItem();
		
		for (File file : projectFolder.listFiles()) {
			String name = file.getName();
			String fext = StringUtil.getExtension(name);
			if (name.startsWith("run") && (fext.equals("bat") || fext.equals("sh")
					|| fext.equals("command")))
			{
				launchers.add(new Launcher(file, name));
			} else if (fext.equals("exe")) {
				launchers.add(new Launcher(file, name));
			}
		}
		combo.setModel(new DefaultComboBoxModel(launchers.toArray()));
		
		Launcher best = selectDefaultLauncher(launchers);
		combo.setSelectedItem(best != null ? best : oldSelected);
		updateRunButtonEnabled();
	}
	
	private void updateRunButtonEnabled() {
		button.setEnabled(isEnabled()); //button.setEnabled(combo.getSelectedItem() instanceof Launcher);
	}
	
	public static Launcher selectDefaultLauncher(Collection<Launcher> launchers) {
		boolean is64bit = false;
		
		try {
			Process process = Runtime.getRuntime().exec("java -version");
			String output = ProcessUtil.read(process).toLowerCase();
			ProcessUtil.kill(process);
			is64bit = output.contains("64-bit");
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
		
		Launcher best = null;
		int bestScore = Integer.MIN_VALUE;
		for (Launcher l : launchers) {
			String name = l.file.getName();
			
			//Score for filetype per OS
			int windows = 0;
			int linux = 0;
			int mac = 0;
			if (name.endsWith(".sh")) {
				linux += 10;
				mac += 10;
			} else if (name.endsWith(".command")) {
				mac += 100;
			} else if (name.endsWith(".bat") || name.endsWith(".exe")) {
				windows += 100;
			}
			
			int score;			
			if (SystemUtil.isWindowsOS()) {
				score = windows;
			} else {
				String osName = System.getProperty("os.name", "linux").toLowerCase();
				if (osName.indexOf("mac") >= 0) {
					score = mac;
				} else {
					score = linux;
				}				
			}
			
			if (name.indexOf("64") >= 0) {
				if (is64bit) {
					score += 5;
				}
			} else {
				if (!is64bit) {
					score += 5;
				}
			}
			
			if (score > bestScore) {
				bestScore = score;
				best = l;
			}
		}
		return best;
	}
	
	private void updateEnabled() {
		boolean e = isEnabled();
		
		combo.setEnabled(e);
		updateRunButtonEnabled();
	}
	
	//Getters
	
	//Setters
	public void setBuild(Build b) {
		engineFolder = b.getEngineFolder();
		projectFolder = b.getProjectFolder();				
		
		update();
		updateEnabled();
		
		if (button.isEnabled()) {
			button.requestFocus();
		}
	}
	
	@Override
	public void setEnabled(boolean e) {
		if (isEnabled() != e) {
			super.setEnabled(e);
			
			updateEnabled();
		}
	}
	
	//Inner Classes
	private static final class Launcher {
		
		public final File file;
		public final String name;
		
		public Launcher(File f, String n) {
			file = f;
			name = n;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
	}
	
	private static class RunButton extends JButton {
		
		public RunButton() {
			super(" Run Game ");

			setOpaque(false);
			//setPreferredSize(new Dimension(60, 23));
		}
		
	}
	
}
