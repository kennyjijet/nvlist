package nl.weeaboo.nvlist.debug;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.undo.UndoManager;

import nl.weeaboo.awt.LogPane;
import nl.weeaboo.awt.TTextField;
import nl.weeaboo.common.StringUtil;
import nl.weeaboo.io.ByteChunkOutputStream;
import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.vn.impl.nvlist.Novel;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.Varargs;

@SuppressWarnings("serial")
public class DebugLuaPanel extends JPanel {

	private final Object lock;
	private final Novel novel;
	private JButton printStackTraceButton;
	private final LogPane logPane;
	private final TTextField commandField;
	
	public DebugLuaPanel(Object l, Novel nvl) {
		lock = l;
		novel = nvl;
		
		printStackTraceButton = new JButton("Print Stacktrace");
		printStackTraceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchronized (lock) {
					try {
						ByteChunkOutputStream bout = new ByteChunkOutputStream();
						novel.printStackTrace(new PrintStream(bout, false, "UTF-8"));
						byte[] bytes = bout.toByteArray();
						logPane.append(StringUtil.fromUTF8(bytes, 0, bytes.length));
					} catch (IOException ioe) {
						logPane.append(LogPane.STYLE_WARNING, "Error printing stack trace", ioe);
					}
				}
			}
		});
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(printStackTraceButton);
		
		logPane = new LogPane();
		logPane.append(LogPane.STYLE_VERBOSE, "Type some Lua code and press enter to run it.");
		JScrollPane logScrollPane = new JScrollPane(logPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		//Command field
		commandField = new TTextField();
		
		commandField.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UndoManager um = commandField.getUndoManager();
				if (um.canUndo()) um.undo();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), JComponent.WHEN_FOCUSED);
		
		commandField.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UndoManager um = commandField.getUndoManager();
				if (um.canRedo()) um.redo();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), JComponent.WHEN_FOCUSED);
		
		Font font = commandField.getFont();
		commandField.setFont(new Font(Font.MONOSPACED, font.getStyle(), font.getSize()));
		
		commandField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String code = commandField.getText();
				if (code.trim().length() == 0) {
					return;
				}
				commandField.setText("");
				
				synchronized (lock) {
					try {
						Varargs result = novel.eval(code);
						if (result != null && result.narg() > 0) {
							logPane.append(result.tojstring());
						}
					} catch (LuaException e) {
						Throwable t = e;
						while ((t instanceof LuaException || t instanceof LuaError) && t.getCause() != null) {
							t = t.getCause();
						}
						String message = "Error evaluating Lua code";
						//GameLog.w(message, t);
						logPane.append(LogPane.STYLE_ERROR, message, t);
					}
				}
			}
		});
		
		setLayout(new BorderLayout(5, 5));
		add(buttonPanel, BorderLayout.NORTH);
		add(logScrollPane, BorderLayout.CENTER);
		add(commandField, BorderLayout.SOUTH);
	}
	
	//Functions
	
	//Getters
	
	//Setters
	
}
