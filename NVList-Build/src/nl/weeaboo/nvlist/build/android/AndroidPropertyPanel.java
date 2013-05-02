package nl.weeaboo.nvlist.build.android;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nl.weeaboo.awt.property.IProperty;
import nl.weeaboo.awt.property.IPropertyGroupListener;
import nl.weeaboo.awt.property.Property;
import nl.weeaboo.awt.property.PropertyGroupBuilder;
import nl.weeaboo.awt.property.PropertyModel;
import nl.weeaboo.awt.property.PropertyTable;
import nl.weeaboo.awt.property.PropertyTableModel;
import nl.weeaboo.awt.property.editor.StringEditor;
import nl.weeaboo.io.FileUtil;
import nl.weeaboo.nvlist.build.Build;
import nl.weeaboo.nvlist.build.PackageNameField;
import nl.weeaboo.nvlist.build.PropertyDescPane;
import nl.weeaboo.settings.INIFile;
import nl.weeaboo.settings.Preference;

@SuppressWarnings("serial")
public class AndroidPropertyPanel extends JPanel {

	public static final String GROUP_BUILD = "build";
	public static final String GROUP_ANDROID = "android";
	public static final String GROUP_PREFS = "prefsDefault";
		
	private Map<String, Preference<?>> buildDefs, androidDefs, prefsDefs;
	private INIFile buildINI, androidINI, prefsDefaultINI;
	
	private Build build;
	private PropertyTable propertyTable;
	private PropertyModel propertyModel;
	private PropertyDescPane propertyDescPane;
	
	public AndroidPropertyPanel(Color bg) {
		buildDefs = new LinkedHashMap<String, Preference<?>>();
		androidDefs = new LinkedHashMap<String, Preference<?>>();
		prefsDefs = new LinkedHashMap<String, Preference<?>>();
		
		buildINI = new INIFile();
		androidINI = new INIFile();
		prefsDefaultINI = new INIFile();
		
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
					buildINI.put(name, String.valueOf(newval));
					writeINI(buildINI, Build.PATH_BUILD_INI);
				} else if (id.startsWith(GROUP_ANDROID+'.')) {
					androidINI.put(name, String.valueOf(newval));
					writeINI(androidINI, Build.PATH_ANDROID_INI);
				} else if (id.startsWith(GROUP_PREFS+'.')) {
					prefsDefaultINI.put(name, String.valueOf(newval));
					writeINI(prefsDefaultINI, Build.PATH_PREFS_ANDROID_INI);
					Build.tryUpdateSavedPrefs(build, name, String.valueOf(newval));
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
		
		propertyTable.setEditor(GROUP_BUILD + ".package", new StringEditor(new PackageNameField()));
		
		JScrollPane scrollPane = new JScrollPane(propertyTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		setOpaque(false);
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
	}
	
	//Functions		
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
		backup(Build.PATH_ANDROID_INI);
		backup(Build.PATH_PREFS_ANDROID_INI);
		
		buildINI.clear();
		androidINI.clear();
		prefsDefaultINI.clear();

		readINI(buildINI, Build.PATH_BUILD_INI);
		readINI(androidINI, Build.PATH_ANDROID_INI);
		readINI(prefsDefaultINI, Build.PATH_PREFS_ANDROID_INI);
	}
	
	private void writeINI(INIFile ini, String filename) {
		try {
			File file = new File(build.getProjectFolder(), filename);
			ini.write(file);
		} catch (IOException e) {
			System.err.println("Error saving file: " + filename + " :: " + e);
		}
	}
	
	public void update() {
		load();

		PropertyGroupBuilder b = new PropertyGroupBuilder();
		b.startGroup(GROUP_BUILD, "Build properties");
		Set<String> defaultBuildPrefs = new HashSet<String>(Arrays.asList("package"));
		addIniFile(b, GROUP_BUILD, buildINI, buildDefs, false, defaultBuildPrefs);
		b.stopGroup();
		b.startGroup(GROUP_ANDROID, "Android properties");
		Set<String> defaultAndroidPrefs = new HashSet<String>(Arrays.asList("package"));
		addIniFile(b, GROUP_ANDROID, androidINI, androidDefs, true, defaultAndroidPrefs);
		b.stopGroup();
		b.startGroup(GROUP_PREFS, "Preference overrides");
		Set<String> defaultPrefs = new HashSet<String>(Arrays.asList("vn.textStyle", "vn.textReadStyle"));
		addIniFile(b, GROUP_PREFS, prefsDefaultINI, prefsDefs, false, defaultPrefs);
		b.stopGroup();

		propertyModel.setRoot(b.build());		
		propertyTable.setModel(new PropertyTableModel(propertyModel));
	}
	
	protected Set<String> addIniFile(PropertyGroupBuilder b, String groupId, INIFile file,
			Map<String, Preference<?>> definitions, boolean addMissingDefs,
			Set<String> basicPrefs)
	{
		Set<String> keys = new LinkedHashSet<String>(definitions.keySet());
		for (Iterator<String> itr = keys.iterator(); itr.hasNext(); ) {
			String key = itr.next();
			if (!addMissingDefs && !basicPrefs.contains(key)) {
				itr.remove();
			}
		}
		
		for (String key : file.keySet()) {
			if (basicPrefs.contains(key)) {
				keys.add(key);
			}
		}
		
		for (String key : keys) {
			Preference<?> def = definitions.get(key);
			String val = file.get(key);
			
			if (def != null) {
				b.add(convertProperty(def, val));
			} else {
				if (val == null) val = "";
				b.add(new nl.weeaboo.awt.property.Property(key, key, String.class, val));
			}
		}
		
		return keys;
	}
	
	protected <T> IProperty convertProperty(Preference<T> def, String strval) {
		T val = (strval != null ? def.fromString(strval) : def.getDefaultValue());
		return new Property(def.getKey(), def.getKey(), def.getType(), val);
	}
	
	//Getters
	
	//Setters
	public void setPropertyDefinitions(List<Preference<?>> bdefs, List<Preference<?>> adefs, List<Preference<?>> defPrefs) {
		buildDefs.clear();
		for (Preference<?> def : bdefs) {
			buildDefs.put(def.getKey(), def);
		}
		
		androidDefs.clear();
		for (Preference<?> def : adefs) {
			androidDefs.put(def.getKey(), def);
		}
		
		prefsDefs.clear();
		for (Preference<?> def : defPrefs) {
			prefsDefs.put(def.getKey(), def);
		}
		
		Map<String, Map<String, Preference<?>>> prefs = new HashMap<String, Map<String, Preference<?>>>();
		prefs.put(GROUP_BUILD, buildDefs);
		prefs.put(GROUP_ANDROID, androidDefs);
		prefs.put(GROUP_PREFS, prefsDefs);
		propertyDescPane.setPreferences(prefs);
	}
	
	public void setBuild(Build b) {
		if (build != b) {
			build = b;
			
			update();
		}
	}
	
	public <T> void setProperty(Preference<T> pref, T val) {
		IProperty prop = (IProperty)propertyModel.getRoot().getChildRecursive(GROUP_ANDROID + "." + pref.getKey());
		//System.out.println(GROUP_ANDROID + "." + pref.getKey());
		if (prop == null) {
			System.err.println("Property not found: " + pref.getKey());
		} else {
			prop.setValue(val);
		}
	}
	
}
