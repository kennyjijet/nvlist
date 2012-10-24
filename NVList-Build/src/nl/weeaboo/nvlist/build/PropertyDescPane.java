package nl.weeaboo.nvlist.build;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import nl.weeaboo.settings.Preference;
import nl.weeaboo.string.HtmlUtil;

@SuppressWarnings("serial")
public class PropertyDescPane extends JPanel {

	private Map<String, Map<String, Preference<?>>> preferenceGroups;
	private String selectedId;
	private String selectedGroup;
	private String selectedKey;
	
	private JLabel label;
		
	public PropertyDescPane(Color bg) {
		preferenceGroups = Collections.emptyMap();
		
		setBackground(BuildGUIUtil.brighter(bg));
		
		label = new JLabel();
		label.setVerticalTextPosition(JLabel.TOP);
		label.setVerticalAlignment(JLabel.TOP);
		label.addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				update();
			}
			@Override
			public void componentMoved(ComponentEvent e) {
			}
			@Override
			public void componentShown(ComponentEvent e) {
				update();
			}
			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
		
		JScrollPane scrollPane = new JScrollPane(label, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getViewport().setBackground(getBackground());
		scrollPane.setBorder(null);
		
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(0x828790)),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		setPreferredSize(new Dimension(200, 100));
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
	}
	
	//Functions
	protected void update() {
		Map<String, Preference<?>> preferences = preferenceGroups.get(selectedGroup);
		Preference<?> pref = (preferences != null ? preferences.get(selectedKey) : null);
		if (pref == null) {
			label.setText("");
		} else {
			label.setText(String.format("<html><div width=%d><b>%s</b><br>%s</div></html>",
					label.getParent().getWidth(),
					HtmlUtil.escapeHtml(pref.getName()),
					HtmlUtil.escapeHtml(pref.getDescription())));
		}		
	}
	
	//Getters
	public String getSelected() {
		return selectedKey;
	}
	
	//Setters
	public void setPreferences(Map<String, Map<String, Preference<?>>> prefs) {
		//Copy preferences map
		preferenceGroups = new HashMap<String, Map<String, Preference<?>>>();
		for (Entry<String, Map<String, Preference<?>>> entry : prefs.entrySet()) {
			preferenceGroups.put(entry.getKey(), new HashMap<String, Preference<?>>(entry.getValue()));
		}
		
		String oldSelected = selectedKey;
		selectedKey = null;
		setSelected(oldSelected);
	}
	public void setSelected(String id) {
		if (selectedId != id && (selectedId == null || !selectedId.equals(id))) {
			selectedId = id;
			if (id == null) {
				selectedGroup = selectedKey = null;
			} else {
				int index = id.indexOf('.');
				if (index > 0) {
					selectedGroup = id.substring(0, index);
				} else {
					selectedGroup = null;
				}
				selectedKey = id.substring(index + 1);
			}
			update();
		}
	}
	
}
