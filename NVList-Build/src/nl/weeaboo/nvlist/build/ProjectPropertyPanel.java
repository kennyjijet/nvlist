package nl.weeaboo.nvlist.build;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nl.weeaboo.awt.DirectValidatingField;
import nl.weeaboo.awt.property.IProperty;
import nl.weeaboo.awt.property.IPropertyGroupListener;
import nl.weeaboo.awt.property.Property;
import nl.weeaboo.awt.property.PropertyGroupBuilder;
import nl.weeaboo.awt.property.PropertyModel;
import nl.weeaboo.awt.property.PropertyTable;
import nl.weeaboo.awt.property.PropertyTableModel;
import nl.weeaboo.awt.property.editor.StringEditor;
import nl.weeaboo.io.FileUtil;
import nl.weeaboo.settings.INIFile;
import nl.weeaboo.settings.Preference;

@SuppressWarnings("serial")
public class ProjectPropertyPanel extends JPanel {

	public static final String GROUP_BUILD = "build";
	public static final String GROUP_GAME = "game";
	public static final String GROUP_PREFS = "prefsDefault";
	public static final String GROUP_INSTALLER = "installerConfig";
		
	private final Set<String> importantProperties = new HashSet<String>();
	
	private Build build;
	private Map<String, Preference<?>> buildDefs, gameDefs, prefsDefs, installerDefs;
	private INIFile buildINI, gameINI, prefsDefaultINI, installerConfigINI;
	
	private final ConsoleOutputPanel outputPanel;
	private PropertyTable propertyTable;
	private PropertyModel propertyModel;
	private PropertyDescPane propertyDescPane;
	
	private boolean basicMode = true;
	
	public ProjectPropertyPanel(ConsoleOutputPanel output, Color bg) {
		outputPanel = output;
		
		addImportantProperties();
		
		final JCheckBox basicModeCheck = new JCheckBox("Basic Mode");
		basicModeCheck.setFont(basicModeCheck.getFont().deriveFont(Font.BOLD));
		basicModeCheck.setOpaque(false);
		basicModeCheck.setSelected(basicMode);
		basicModeCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				basicMode = basicModeCheck.isSelected();
				update();				
			}
		});
		
		buildDefs = new LinkedHashMap<String, Preference<?>>();
		gameDefs = new LinkedHashMap<String, Preference<?>>();
		prefsDefs = new LinkedHashMap<String, Preference<?>>();
		installerDefs = new LinkedHashMap<String, Preference<?>>();

		buildINI = new INIFile();
		gameINI = new INIFile();
		prefsDefaultINI = new INIFile();
		installerConfigINI = new INIFile();
		
		propertyDescPane = new PropertyDescPane(bg);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setOpaque(false);
		bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
		bottomPanel.add(propertyDescPane, BorderLayout.CENTER);
		
		propertyModel = new PropertyModel();
		propertyModel.addListener(new IPropertyGroupListener() {
			public void onStructureChanged() {
			}
			public void onChildPropertyChanged(String id, Object oldval, final Object newval) {
				final String name = id.substring(id.indexOf('.')+1);
				if (id.startsWith(GROUP_BUILD+'.')) {
					if (name.equals("project-name")) {
						try {
							outputPanel.process(build.ant(build.getCleanTarget()), new ProcessCallback() {
								public void run(int exitCode) {
									buildINI.put(name, String.valueOf(newval));
									save();
								}
							});
							return;
						} catch (IOException e) {
							System.err.println("Attempt to start build.clean() failed: " + e);
						}
					}
					buildINI.put(name, String.valueOf(newval));
					save();
				} else if (id.startsWith(GROUP_GAME+'.')) {
					gameINI.put(name, String.valueOf(newval));
					save();
				} else if (id.startsWith(GROUP_PREFS+'.')) {
					prefsDefaultINI.put(name, String.valueOf(newval));
					//System.out.println("prefs.ini changed: " + name + " = " + newval);
					save();
					
					tryUpdateSavedPrefs(name, String.valueOf(newval));
				} else if (id.startsWith(GROUP_INSTALLER+'.')) {
					installerConfigINI.put(name, String.valueOf(newval));
					save();
				}
			}			
		});
		
		propertyTable = new PropertyTable();
		propertyTable.setModel(new PropertyTableModel(propertyModel));
		
		ListSelectionModel selectionModel = propertyTable.getSelectionModel();
		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int row = propertyTable.getSelectedRow();
				PropertyTableModel model = (PropertyTableModel)propertyTable.getModel();
				if (row >= 0 && row < model.getRowCount()) {
					IProperty prop = model.getProperty(row);
					propertyDescPane.setSelected(prop != null ? prop.getId() : null);
				}
			}
		});
		
		DirectValidatingField projectNameField = new DirectValidatingField() {
			@Override
			public boolean isValid(String s) {
				if (s == null) {
					return false;
				}
				
				//Same replace happens in BaseLauncher
				s = s.replace(' ', '_');
				
				//Check if it's a valid ID (basically the same as a valid POSIX filename)
				for (int n = 0; n < s.length(); n++) {
					char c = s.charAt(n);
					boolean isLowercaseAlpha = (c >= 'a' && c <= 'z');
					boolean isUppercaseAlpha = (c >= 'A' && c <= 'Z');
					boolean isNumeric = (c >= '0' && c <= '9');
					if (c != '_' && !isLowercaseAlpha && !isUppercaseAlpha
							&& (n == 0 || !isNumeric))
					{
						return false;
					}
				}
				return true;
			}
			@Override
			protected void onValidTextEntered(String s) {
				//Do nothing, the StringEditor shoudl take care of the update 
			}
			
		};
		propertyTable.setEditor(GROUP_BUILD + ".project-name", new StringEditor(projectNameField));
		
		JScrollPane scrollPane = new JScrollPane(propertyTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//scrollPane.getViewport().setBackground(BuildGUIUtil.brighter(bg));
		
		setOpaque(false);
		setLayout(new BorderLayout());
		add(basicModeCheck, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
	}
	
	//Functions
	private static void addAsGroup(Collection<String> out, String groupId, Collection<String> groupMemberIds) {
		String prefix = (groupId.length() > 0 ? groupId+"." : groupId);
		for (String id : groupMemberIds) {
			out.add(prefix + id);
		}
	}
	
	private void addImportantProperties() {
		List<String> buildImp = Arrays.asList("project-name", "program-args", "obfuscate"
				/*", applet-width", "applet-height"*/ );		
		addAsGroup(importantProperties, GROUP_BUILD, buildImp);
		
		List<String> gameImp = Arrays.asList("title", "fps", "width", "height", "vn.enableProofreaderTools");
		addAsGroup(importantProperties, GROUP_GAME, gameImp);

		List<String> prefsImp = Arrays.asList("debug", "startFullscreen", "vn.audio.musicVolume",
				"vn.audio.soundVolume", "vn.textStyle", "vn.textReadStyle");
		addAsGroup(importantProperties, GROUP_PREFS, prefsImp);


		List<String> installerImp = Arrays.asList("compression");
		addAsGroup(importantProperties, GROUP_INSTALLER, installerImp);
	}
	
	private void backup(String filename) {
		try {
			File file = new File(build.getProjectFolder(), filename);
			File bakF = new File(build.getProjectFolder(), filename+".bak");
			if (!bakF.exists()) {
				//Only create a backup of the very first config files ever seen
				FileUtil.copyFile(file, bakF);
			}
		} catch (IOException e) {
			System.err.println("Error trying to backup file: " + filename + " :: " + e);
		}
	}
	
	private void readINI(INIFile file, String filename) {
		try {
			file.read(new File(build.getProjectFolder(), filename));
		} catch (FileNotFoundException fnfe) {
			//Ignore
		} catch (IOException ioe) {
			System.err.println("Error opening file: " + filename + " :: " + ioe);
		}				
	}
	
	public void load() {
		backup(Build.PATH_BUILD_INI);
		backup(Build.PATH_GAME_INI);
		backup(Build.PATH_PREFS_INI);
		backup(Build.PATH_INSTALLER_INI);
		
		buildINI.clear();
		gameINI.clear();
		prefsDefaultINI.clear();
		installerConfigINI.clear();

		readINI(buildINI, Build.PATH_BUILD_INI);
		readINI(gameINI, Build.PATH_GAME_INI);
		readINI(prefsDefaultINI, Build.PATH_PREFS_INI);
		readINI(installerConfigINI, Build.PATH_INSTALLER_INI);		
	}
	
	private void writeINI(INIFile ini, String filename) {
		try {
			File file = new File(build.getProjectFolder(), filename);
			ini.write(file);
		} catch (IOException e) {
			System.err.println("Error saving file: " + filename + " :: " + e);
		}
	}
	
	public void save() {
		writeINI(buildINI, Build.PATH_BUILD_INI);
		writeINI(gameINI, Build.PATH_GAME_INI);
		writeINI(prefsDefaultINI, Build.PATH_PREFS_INI);
		writeINI(installerConfigINI, Build.PATH_INSTALLER_INI);
	}
	
	public void update() {
		load();
		
		PropertyGroupBuilder b = new PropertyGroupBuilder();
		b.startGroup(GROUP_BUILD, "Build properties");
		addIniFile(b, GROUP_BUILD, buildINI, buildDefs);
		b.stopGroup();
		b.startGroup(GROUP_GAME, "Game properties");
		Set<String> consts = addIniFile(b, GROUP_GAME, gameINI, gameDefs);
		b.stopGroup();
		b.startGroup(GROUP_PREFS, "Preference Defaults");
		addIniFile(b, GROUP_PREFS, prefsDefaultINI, prefsDefs, true, consts);
		b.stopGroup();
		b.startGroup(GROUP_INSTALLER, "Installer Creation Settings");
		addIniFile(b, GROUP_INSTALLER, installerConfigINI, installerDefs);
		b.stopGroup();
		
		propertyModel.setRoot(b.build());
		propertyTable.setModel(new PropertyTableModel(propertyModel));
	}
	
	protected Set<String> addIniFile(PropertyGroupBuilder b, String groupId, INIFile file,
			Map<String, Preference<?>> definitions)
	{
		Set<String> set = Collections.emptySet();
		return addIniFile(b, groupId, file, definitions, false, set);
	}
	protected Set<String> addIniFile(PropertyGroupBuilder b, String groupId, INIFile file,
			Map<String, Preference<?>> definitions, boolean addMissingDefs,
			Set<String> ignoreMissing)
	{
		Set<String> keys = new LinkedHashSet<String>(definitions.keySet());
		for (Iterator<String> itr = keys.iterator(); itr.hasNext(); ) {
			String key = itr.next();
			if (!file.containsKey(key)) {
				if (!addMissingDefs || ignoreMissing.contains(key)) {
					itr.remove();
				}
			}
		}
		for (String key : file.keySet()) {
			keys.add(key);
		}
		
		if (basicMode) {
			for (Iterator<String> itr = keys.iterator(); itr.hasNext(); ) {
				String key = itr.next();
				if (!importantProperties.contains(groupId+"."+key)) {
					itr.remove();
				}
			}
		}
		
		//Collections.sort(keys);
		
		for (String key : keys) {
			Preference<?> def = definitions.get(key);
			String val = file.get(key);
			if (val == null) val = "";
			
			if (def != null) {
				b.add(convertProperty(def, val));
			} else {
				b.add(new nl.weeaboo.awt.property.Property(key, key, String.class, val));
			}
		}
		
		return keys;
	}
	
	protected <T> IProperty convertProperty(Preference<T> def, String strval) {
		T val = (strval != null ? def.fromString(strval) : def.getDefaultValue());
		return new Property(def.getKey(), def.getKey(), def.getType(), val);
	}
		
	protected boolean tryUpdateSavedPrefs(String key, String val) {
		File file = new File(build.getProjectFolder(), "save/prefs.ini");
		if (!file.exists()) {
			return false; //Unable to find saved prefs. Maybe it's in a different location or doesn't exist yet.
		}
		
		try {
			INIFile ini = new INIFile();
			ini.read(file);
			if (ini.containsKey(key)) {
				ini.put(key, val);
				ini.write(file);
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	//Getters
	
	//Setters
	public void setPropertyDefinitions(List<Preference<?>> buildProperties,
			List<Preference<?>> gameINI, List<Preference<?>> prefsDefaultINI,
			List<Preference<?>> installerConfigINI)
	{
		buildDefs.clear();
		for (Preference<?> def : buildProperties) {
			buildDefs.put(def.getKey(), def);
		}
		gameDefs.clear();
		for (Preference<?> def : gameINI) {
			gameDefs.put(def.getKey(), def);
		}
		prefsDefs.clear();
		for (Preference<?> def : prefsDefaultINI) {
			prefsDefs.put(def.getKey(), def);
		}
		installerDefs.clear();
		for (Preference<?> def : installerConfigINI) {
			installerDefs.put(def.getKey(), def);
		}
		
		Map<String, Map<String, Preference<?>>> prefs = new HashMap<String, Map<String, Preference<?>>>();
		prefs.put(GROUP_BUILD, buildDefs);
		prefs.put(GROUP_GAME, gameDefs);
		prefs.put(GROUP_PREFS, prefsDefs);
		prefs.put(GROUP_INSTALLER, installerDefs);
		propertyDescPane.setPreferences(prefs);
	}
	
	public void setBuild(Build b) {
		if (build != b) {
			build = b;
			
			update();
		}
	}
	
}
