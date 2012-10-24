package nl.weeaboo.nvlist.debug;

import static nl.weeaboo.nvlist.debug.BugReporter.F_DATE;
import static nl.weeaboo.nvlist.debug.BugReporter.F_FILENAME;
import static nl.weeaboo.nvlist.debug.BugReporter.F_MESSAGE;
import static nl.weeaboo.nvlist.debug.BugReporter.F_STACK_TRACE;
import static nl.weeaboo.nvlist.debug.BugReporter.F_VERSION_CODE;
import static nl.weeaboo.nvlist.debug.BugReporter.F_VISIBLE_TEXT;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import nl.weeaboo.awt.AwtUtil;
import nl.weeaboo.awt.TTextField;
import nl.weeaboo.awt.TableLayout;
import nl.weeaboo.common.StringUtil;

@SuppressWarnings("serial")
public class BugReportPanel extends JPanel {

	private final Map<String, String> record;
	
	private final JTextField filenameField;
	private final JTextField stackTraceField;
	private final JTextField visibleTextField;
	private final JTextArea messageArea;
	
	private int versionCode;
	
	public BugReportPanel() {
		record = new LinkedHashMap<String, String>();
		
		JLabel filenameLabel = new JLabel("Filename");
		filenameField = new TTextField();
		filenameField.setEditable(false);
		
		JLabel stacktraceLabel = new JLabel("Stack Trace");
		stackTraceField = new TTextField();
		stackTraceField.setEditable(false);
		stackTraceField.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		JPanel stackTracePanel = new JPanel(new BorderLayout());
		stackTracePanel.add(stackTraceField, BorderLayout.NORTH);
		JScrollPane stackTracePane = new JScrollPane(stackTracePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		stackTracePane.setPreferredSize(new Dimension(100, 40));
		
		JLabel visibleTextLabel = new JLabel("Visible Text");
		visibleTextField = new TTextField();
		visibleTextField.setEditable(false);
		visibleTextField.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		JPanel visibleTextPanel = new JPanel(new BorderLayout());
		visibleTextPanel.add(visibleTextField, BorderLayout.NORTH);
		JScrollPane visibleTextPane = new JScrollPane(visibleTextPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		visibleTextPane.setPreferredSize(new Dimension(100, 50));
		
		JLabel commentLabel = new JLabel("Comment");
		messageArea = new JTextArea(5, 40);
		messageArea.setLineWrap(true);
		messageArea.setWrapStyleWord(true);
		JScrollPane commentPane = new JScrollPane(messageArea,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		setLayout(new TableLayout(2, 5, 5));
		add(filenameLabel);    add(filenameField);
		add(stacktraceLabel);  add(stackTracePane);
		add(visibleTextLabel); add(visibleTextPane);
		add(commentLabel);     add(commentPane);
	}
	
	//Functions
	public static void main(String[] args) throws IOException {
		AwtUtil.setDefaultLAF();
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		
		BugReportPanel panel = new BugReportPanel();
		panel.setFilename("filename.lua");
		panel.setStackTrace("a.lvn\nb.lvn\nc.lvn");
		panel.setVisibleText("aaaa\nbbbbb\ncccc");
		if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, panel, "Bug Report", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)) {
			BugReporter bugReporter = new BugReporter(bout);
			panel.writeTo(bugReporter);
		}
		
		System.out.println(StringUtil.fromUTF8(bout.toByteArray(), 0, bout.size()));
	}
	
	public void writeTo(BugReporter bugReporter) throws IOException {
		String filename = filenameField.getText().trim();
		String dateString = new Date().toString();
		String stackTrace = stackTraceField.getText().trim();
		String visibleText = visibleTextField.getText().trim();
		String message = messageArea.getText().trim();
		
		try {
			if (versionCode > 0)          record.put(F_VERSION_CODE, ""+versionCode);
			if (filename.length() > 0)    record.put(F_FILENAME, filename);
			if (dateString.length() > 0)  record.put(F_DATE, dateString);
			if (stackTrace.length() > 0)  record.put(F_STACK_TRACE, stackTrace);
			if (visibleText.length() > 0) record.put(F_VISIBLE_TEXT, visibleText);
			if (message.length() > 0)     record.put(F_MESSAGE, message);
			
			bugReporter.write(record);
		} finally {
			record.clear();
		}
	}
	
	public void clear() {
		filenameField.setText("");
		stackTraceField.setText("");
		visibleTextField.setText("");
		messageArea.setText("");
	}
	
	//Getters
	
	//Setters
	public void setFilename(String filename) {
		filenameField.setText(filename);
	}
	
	public void setStackTrace(String[] stackTrace) {
		StringBuilder sb = new StringBuilder();
		for (String line : stackTrace) {
			if (sb.length() > 0) sb.append('\n');
			sb.append(line);
		}
		setStackTrace(sb.toString());
	}
	public void setStackTrace(String stackTrace) {
		stackTraceField.setText(stackTrace);
	}
	public void setVisibleText(String text) {
		visibleTextField.setText(text);
	}
	public void setVersionCode(int code) {
		versionCode = code;
	}
	
}
