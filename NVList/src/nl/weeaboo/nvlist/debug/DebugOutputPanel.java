package nl.weeaboo.nvlist.debug;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import nl.weeaboo.awt.LogPane;
import nl.weeaboo.game.GameLog;
import nl.weeaboo.game.GameLogFormatter;
import nl.weeaboo.vn.impl.nvlist.Novel;

@SuppressWarnings("serial")
public class DebugOutputPanel extends JPanel {

	private static final String STYLE_FINE = "fine";
	private static final String STYLE_CONFIG = "config";
	
	private final LogPane outputPane;	
	private final LogHandler logHandler;
	
	public DebugOutputPanel(Object l, Novel nvl) {
		outputPane = new LogPane();
		
		Style styleFine = outputPane.addStyle(STYLE_FINE, null);
		styleFine.addAttribute(StyleConstants.Foreground, Color.LIGHT_GRAY);
		
		Style styleConfig = outputPane.addStyle(STYLE_CONFIG, null);
		styleConfig.addAttribute(StyleConstants.Foreground, Color.GRAY);
		
		setLayout(new BorderLayout(5, 5));
		add(new JScrollPane(outputPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
		
		logHandler = new LogHandler();
		
		addHierarchyListener(new HierarchyListener() {
			public void hierarchyChanged(HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
					if (isDisplayable()) {
						addLoggerHandler();
					} else {					
						removeLoggerHandler();
					}
				}
			}			
		});
	}
	
	//Functions
	private void addLoggerHandler() {
		try {
			Logger logger = GameLog.getLogger();
			logger.addHandler(logHandler);
		} catch (SecurityException se) {
			//Ignore
		}
	}
	
	private void removeLoggerHandler() {
		try {
			Logger logger = GameLog.getLogger();
			logger.removeHandler(logHandler);
		} catch (SecurityException se) {
			//Ignore
		}
	}
		
	//Getters
	
	//Setters
	
	//Inner Classes
	private class LogHandler extends Handler {

		private final Formatter formatter;
		
		public LogHandler() {
			try {
				setLevel(Level.CONFIG);
			} catch (SecurityException se) {
				//Ignore
			}
			
			formatter = new GameLogFormatter(false, false);
		}
		
		@Override
		public void publish(LogRecord record) {
			int level = record.getLevel().intValue();
			if (level < getLevel().intValue()) {
				return;
			}
			
			final String style;
			if (level >= Level.SEVERE.intValue()) {
				style = LogPane.STYLE_ERROR;
			} else if (level >= Level.WARNING.intValue()) {
				style = LogPane.STYLE_WARNING;
			} else if (level <= Level.FINE.intValue()) {
				style = STYLE_FINE;
			} else if (level <= Level.CONFIG.intValue()) {
				style = STYLE_CONFIG;
			} else {
				style = LogPane.STYLE_VERBOSE;				
			}
			
			final String message = formatter.format(record);
			
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					outputPane.append(style, message, null);
				}
			});
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() throws SecurityException {
		}
		
	}
	
}
