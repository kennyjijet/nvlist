package nl.weeaboo.nvlist.build.android;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
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
import nl.weeaboo.nvlist.build.Build;
import nl.weeaboo.nvlist.build.PropertyDescPane;
import nl.weeaboo.settings.INIFile;
import nl.weeaboo.settings.Preference;

@SuppressWarnings("serial")
public class AndroidPropertyPanel extends JPanel {

	public static final String GROUP_ANDROID = "android";
		
	private Map<String, Preference<?>> androidDefs;
	private INIFile androidINI;
	
	private Build build;
	private PropertyTable propertyTable;
	private PropertyModel propertyModel;
	private PropertyDescPane propertyDescPane;
	
	public AndroidPropertyPanel(Color bg) {
		androidDefs = new LinkedHashMap<String, Preference<?>>();
		androidINI = new INIFile();
		
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
				if (id.startsWith(GROUP_ANDROID+'.')) {
					androidINI.put(name, String.valueOf(newval));
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
		
		propertyTable.setEditor(GROUP_ANDROID + ".package", new StringEditor(new PackageNameField()));
		
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
		backup(Build.PATH_ANDROID_INI);
		
		androidINI.clear();

		readINI(androidINI, Build.PATH_ANDROID_INI);
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
		writeINI(androidINI, Build.PATH_ANDROID_INI);
	}
	
	public void update() {
		load();
		
		PropertyGroupBuilder b = new PropertyGroupBuilder();
		b.startGroup(GROUP_ANDROID, "Android properties");
		Set<String> ignore = Collections.emptySet();
		addIniFile(b, GROUP_ANDROID, androidINI, androidDefs, true, ignore);
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
	public void setPropertyDefinitions(List<Preference<?>> defs) {
		androidDefs.clear();
		for (Preference<?> def : defs) {
			androidDefs.put(def.getKey(), def);
		}
		
		Map<String, Map<String, Preference<?>>> prefs = new HashMap<String, Map<String, Preference<?>>>();
		prefs.put(GROUP_ANDROID, androidDefs);
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
	
	//Inner Classes
	private static class PackageNameField extends DirectValidatingField {

		@Override
		public boolean isValid(String text) {
			if (text.startsWith("com.example")) return false;
			
			String[] parts = text.split("\\.");
			if (parts.length == 0) return false;
			
			for (String part : parts) {
				if (part.length() == 0) return false;
				
				for (int n = 0; n < part.length(); ) {
					int c = part.codePointAt(n);
					if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_') {
						//pass
					} else if (n > 0 && c >= '0' && c <= '9') {
						//pass
					} else {
						return false; //fail
					}
					n += Character.charCount(c);
				}
			}
			return true;
		}

		@Override
		protected void onValidTextEntered(String text) {
		}
		
	}
	
}
