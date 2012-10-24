package nl.weeaboo.nvlist.menu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.plaf.basic.BasicButtonUI;

import nl.weeaboo.game.GameLog;
import nl.weeaboo.game.GameUpdater;
import nl.weeaboo.game.IGameDisplay;
import nl.weeaboo.nvlist.Game;
import nl.weeaboo.nvlist.debug.BugReportPanel;
import nl.weeaboo.nvlist.debug.BugReporter;
import nl.weeaboo.settings.ConfigPropertyListener;
import nl.weeaboo.settings.IConfig;
import nl.weeaboo.settings.Preference;
import nl.weeaboo.vn.ITextState;
import nl.weeaboo.vn.NovelPrefs;
import nl.weeaboo.vn.impl.nvlist.Novel;

public class GameMenuFactory {

	private final Game game;
	private final ConfigListener configListener;
	
	public GameMenuFactory(Game game) {
		this.game = game;
		this.configListener = new ConfigListener();

		//IConfig config = game.getConfig();
		//config.addPropertyListener(configListener);
	}
	
	//Functions
	public void dispose() {
		IConfig config = game.getConfig();
		config.removePropertyListener(configListener);
	}
	
	public static JMenuBar createPlaceholderJMenuBar(IGameDisplay display) {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false); //Make all popups mediumweight
		
		JMenuBar menuBar = new JMenuBar();
		JPopupMenu popup = new JPopupMenu();
		popup.setLightWeightPopupEnabled(false);
		menuBar.setComponentPopupMenu(popup);
				
		GameMenu menu = new JGameMenu(null, "Info", '\0');
		menu.add(new AboutItem());
		menuBar.add((Component)menu);
		
		return menuBar;
	}
	
	public JMenuBar createJMenuBar() {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false); //Make all popups mediumweight

		JMenuBar menuBar = new JMenuBar();
		JPopupMenu popup = new JPopupMenu();
		popup.setLightWeightPopupEnabled(false);
		menuBar.setComponentPopupMenu(popup);

		GameMenu[] menus = createMenus();
		configListener.setGameMenus(menus);
		for (GameMenu gm : menus) {
			menuBar.add((Component)gm);
		}
		
		//Bug report button
		final BugReporter bugReporter = game.getBugReporter();
		if (bugReporter != null) {
			final JButton reportItem = new MenuBarButton("Report Bug/Typo (F6)");
			reportItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					synchronized (game) {						
						Novel novel = game.getNovel();
						IConfig config = game.getConfig();
						int versionCode = config.get(GameUpdater.UPDATE_VERSION_CODE);
						
						BugReportPanel panel = new BugReportPanel();
						panel.setFilename(novel.getCurrentCallSite());
						panel.setStackTrace(novel.getStackTrace());
						if (versionCode > 0) panel.setVersionCode(versionCode);
						ITextState textState = novel.getTextState();
						if (textState != null && textState.getTextDrawable() != null) {
							panel.setVisibleText(textState.getTextDrawable().getText().toString());
						}
						
						int r = game.getDisplay().showConfirmDialog(panel, "Report Bug/Typo");
						if (r == JOptionPane.OK_OPTION) {
							try {
								panel.writeTo(bugReporter);
								novel.getNotifier().message("Report added to save/" + BugReporter.OUTPUT_FILENAME);
							} catch (IOException ioe) {
								GameLog.w("Error writing bug report", ioe);
							}
						}
					}
				}
			});
			reportItem.registerKeyboardAction(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						reportItem.doClick();
					}			
				},
				KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
			menuBar.add(reportItem);
		}
		
		return menuBar;
	}
	
	protected GameMenu[] createMenus() {
		return new GameMenu[] {
			createGameMenu(),
			createTextMenu(),
			createImageMenu(),
			createSoundMenu(),
			createWindowMenu(),
			createAdvancedMenu(),
			createInfoMenu()
		};
	}
	
	protected GameMenu createGameMenu() {
		GameMenu menu = createMenu("Game", 'G');
		menu.add(SaveLoadItem.createLoadItem());
		menu.add(SaveLoadItem.createSaveItem());
		menu.addSeparator();
		menu.add(new RestartItem());
		
		IGameDisplay display = game.getDisplay();
		if (!display.isEmbedded()) {
			menu.add(new QuitItem());
		}
		
		return menu;
	}

	protected GameMenu createTextMenu() {
		GameMenu menu = createMenu("Text", 'T');
		menu.add(new SkipModeMenu());
		menu.addSeparator();
		menu.add(new TextSpeedMenu());
		menu.add(new AutoReadWaitMenu());
		menu.add(new AutoReadItem());
		menu.addSeparator();
		menu.add(new TextLogItem());		
		return menu;
	}
	
	protected GameMenu createImageMenu() {
		GameMenu menu = createMenu("Image", 'I');
		menu.add(new EffectSpeedMenu());
		menu.addSeparator();
		menu.add(new ViewCGItem());		
		menu.add(new ScreenshotItem());		
		return menu;
	}
	
	protected GameMenu createSoundMenu() {
		GameMenu menu = createMenu("Sound", 'S');
		menu.add(new AudioVolumeMenu(NovelPrefs.MUSIC_VOLUME, "Music volume"));
		menu.add(new AudioVolumeMenu(NovelPrefs.SOUND_VOLUME, "Sound volume"));
		menu.add(new AudioVolumeMenu(NovelPrefs.VOICE_VOLUME, "Voice volume"));
		return menu;
	}
	
	protected GameMenu createWindowMenu() {
		GameMenu menu = createMenu("Window", 'W');
		menu.add(new WindowScaleMenu());
		menu.add(new FullscreenItem());
		return menu;
	}
	
	protected GameMenu createAdvancedMenu() {
		GameMenu menu = createMenu("Advanced", 'A');
		menu.add(new ImageCacheMenu());
		menu.add(new MaxTexDimensionsMenu());
		menu.add(new ImageFolderSelectorMenu());
		menu.add(new FBOMenu());
		menu.add(new PreloaderMenu());
		menu.addSeparator();
		menu.add(new PreloadGLTexturesItem());
		menu.add(new GLSLItem());
		menu.add(new DebugGLItem());
		menu.add(new LegacyGPUItem());
		menu.addSeparator();
		menu.add(new TrueFullscreenItem());
		menu.add(new VSyncItem());
		return menu;
	}
	
	protected GameMenu createInfoMenu() {
		GameMenu menu = createMenu("Info", '\0');
		menu.add(new CheckForUpdatesItem());
		menu.add(new AboutItem());
		return menu;
	}
	
	protected GameMenu createMenu(String label, char mnemonic) {
		return new JGameMenu(game, label, mnemonic);
	}
	
	//Inner Classes
	private static class ConfigListener implements ConfigPropertyListener {

		private GameMenu[] menus;
		
		public ConfigListener() {			
		}
		
		@Override
		public <T> void onPropertyChanged(Preference<T> pref, T oldval, T newval) {
			final GameMenu[] m = menus;
			if (m != null) {
				for (GameMenu gm : m) {
					gm.onPropertyChanged(pref, oldval, newval);
				}
			}
		}
		
		public void setGameMenus(GameMenu[] ms) {
			menus = ms.clone();
		}
		
	}
	
	@SuppressWarnings("serial")
	private static class MenuBarButton extends JButton {
		
		private static final Color normalBG   = new Color(0xfefecd);
		private static final Color rolloverBG = new Color(0xfefe90);
		private static final Color pressedBG  = Color.GRAY;
		
		public MenuBarButton(String lbl) {
			super(lbl);

			setUI(new BasicButtonUI());
			
			setBackground(normalBG);
			setBorder(BorderFactory.createEmptyBorder(2, 20, 3, 20));
			setFocusable(false);
			setPreferredSize(new Dimension(100, 18));
		}
		
		@Override
		protected void paintComponent(Graphics graphics) {
			ButtonModel model = getModel();
			if (model.isPressed()) {
				setBackground(pressedBG);
			} else if (model.isRollover()) {
				setBackground(rolloverBG);
			} else {
				setBackground(normalBG);				
			}
			
			super.paintComponent(graphics);
		}
		
	}
		
}
