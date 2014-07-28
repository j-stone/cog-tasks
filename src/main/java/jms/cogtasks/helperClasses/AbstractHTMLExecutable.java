/**
 * 
 */
package jms.cogtasks.helperClasses;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.tatool.core.data.Level;
import ch.tatool.core.data.Misc;
import ch.tatool.core.data.Points;
import ch.tatool.core.data.Question;
import ch.tatool.core.data.Result;
import ch.tatool.core.data.Timing;
import ch.tatool.core.display.swing.ExecutionDisplayUtils;
import ch.tatool.core.display.swing.SwingExecutionDisplay;
import ch.tatool.core.display.swing.action.ActionPanel;
import ch.tatool.core.display.swing.action.ActionPanelListener;
import ch.tatool.core.display.swing.action.KeyActionPanel;
import ch.tatool.core.display.swing.container.ContainerUtils;
import ch.tatool.core.display.swing.container.RegionsContainer;
import ch.tatool.core.display.swing.container.RegionsContainer.Region;
import jms.cogtasks.helperClasses.HTMLPanel;
import ch.tatool.core.element.handler.timeout.DefaultVisualTimeoutHandler;
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.data.DescriptivePropertyHolder;
import ch.tatool.data.Messages;
import ch.tatool.data.Property;
import ch.tatool.data.PropertyHolder;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionPhaseListener;

/**
 * HTML Instruction
 * 
 * Displays a HTML page
 * 
 * @author Andre Locher
 */
public abstract class AbstractHTMLExecutable extends BlockingAWTExecutable implements
		ActionPanelListener, ExecutionPhaseListener, DescriptivePropertyHolder {

	Logger logger = LoggerFactory.getLogger(AbstractHTMLExecutable.class);

	private DefaultVisualTimeoutHandler timeoutHandler;
	private RegionsContainer regionsContainer;

	/** Panels */
	private HTMLPanel htmlPanel;
	private KeyActionPanel actionPanel;

	/** HTML data */
	List<String> pages = new ArrayList<String>();
	private String base = "/ch/tatool/data/instructions/";
	public PropertyHolder scoreHandler;
	private Locale currLocale;

	private int currentIndex;
	
	private ExecutionContext context;

	private long duration = 0;

	private Timer execTimer;

	private boolean i18nEnabled = false;

	/** Default Constructor. */
	public AbstractHTMLExecutable() {
		super("html-instruction");
		initComponents();
	}

	private void initComponents() {
		// question panel
		htmlPanel = new HTMLPanel();

		// action panel
		actionPanel = new KeyActionPanel();
		actionPanel.addActionPanelListener(this);
	}

	@Override
	protected void startExecutionAWT() {
		context = getExecutionContext();
		currLocale = context.getExecutionData().getModule().getMessages().getLocale();
		execTimer = new Timer();
		regionsContainer = ContainerUtils
				.getRegionsContainer();
		SwingExecutionDisplay display = ExecutionDisplayUtils
				.getDisplay(context);
		ContainerUtils.showRegionsContainer(display);
		regionsContainer.setRegionVisibility(Region.NORTH, false);
		
		TimerTask endTimerTask = new TimerTask() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (getFinishExecutionLock()) {
							cancelExecutionAWT();
							doCleanup();
							finishExecution();
						}
					}
				});
			}
		};

		if (duration > 0) {
			execTimer.schedule(endTimerTask, duration);
		}
		
		setupPage(0);
	}
	
	private void setupActionPanelKeys(int index) {
		actionPanel.removeKeys();
		Messages messages = context.getExecutionData().getModule().getMessages();
		
		if (index > 0) {
			actionPanel.addKey(KeyEvent.VK_LEFT, messages.getString("AbstractHTMLExecutable.keyActionPanel.labelBack"), -1);
			actionPanel.addKey(KeyEvent.VK_RIGHT, messages.getString("AbstractHTMLExecutable.keyActionPanel.labelNext"), 1);	
		} else {
			actionPanel.addKey(KeyEvent.VK_RIGHT, messages.getString("AbstractHTMLExecutable.keyActionPanel.labelNext"), 1);
		}
		actionPanel.validate();
	}

	private int getPagesCount() {
		if (pages != null) {
			return pages.size();
		} else {
			return 0;
		}
	}

	private void setupPage(int index) {
		String html = getHTMLString(pages.get(index));

		html = replaceVariables(html);
		htmlPanel.setHTMLString(html, base);
		setupActionPanelKeys(index);
		regionsContainer.removeRegionContent(Region.SOUTH);

		// setup views
		regionsContainer.setRegionContent(Region.CENTER, htmlPanel);
		if (duration == 0) {
			regionsContainer.setRegionContent(Region.SOUTH, actionPanel);
		}
		
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, true);

		// enable the actions
		if (duration == 0) {
			actionPanel.enableActionPanel(); 
		}                          
		currentIndex = index;

		// start timer
		if (timeoutHandler != null) {
			timeoutHandler.startTimeout(getExecutionContext());
		}
	}

	public abstract String replaceVariables(String html);

	private String getHTMLString(String page) {
		if (i18nEnabled) {
			page = page.substring(0, page.lastIndexOf(".")) + "_" + currLocale.getLanguage() + page.substring(page.lastIndexOf("."));
		}

		InputStream is = getClass().getResourceAsStream(base + page);
		String html = "";
		try {
			html = inputStreamToString(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return html;
	}
	
	private String inputStreamToString(InputStream in) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(in));
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;

		while ((line = bufferedReader.readLine()) != null) {
			stringBuilder.append(line + "\n");
		}

		bufferedReader.close();
		return stringBuilder.toString();
	}

	protected void cancelExecutionAWT() {
		actionPanel.disableActionPanel();
	}

	/** Called when action panel gets triggered */
	public void actionTriggered(ActionPanel source, Object actionValue) {
		int newIndex = 0;
		int addIndex = (Integer) actionValue;
		actionPanel.disableActionPanel();
		// increment the displayed image
		if (currentIndex + addIndex >= 0 ) {
			newIndex = currentIndex + addIndex;
		}

		// setup next instruction / finish executable
		if (newIndex < getPagesCount()) {
			setupPage(newIndex);
			return;
		} else {
			if (getFinishExecutionLock()) {
				cancelExecutionAWT();
				doCleanup();
				finishExecution();
			}
			return;
		}
	}

	private void doCleanup() {
		if (timeoutHandler != null) {
			timeoutHandler.cancelTimeout();
		}
		actionPanel.disableActionPanel();
		regionsContainer.removeRegionContent(Region.SOUTH);
		regionsContainer.removeRegionContent(Region.CENTER);
	}

	public Property<?>[] getPropertyObjects() {
		return new Property[] { Level.getLevelProperty(),
				Points.getMinPointsProperty(), Points.getPointsProperty(),
				Points.getMaxPointsProperty(), Question.getQuestionProperty(),
				Question.getAnswerProperty(), Question.getResponseProperty(),
				Misc.getOutcomeProperty(), Result.getResultProperty(),
				Timing.getStartTimeProperty(), Timing.getEndTimeProperty(),
				Timing.getDurationTimeProperty() };
	}

	public DefaultVisualTimeoutHandler getTimeoutHandler() {
		return timeoutHandler;
	}

	public void setTimeoutHandler(DefaultVisualTimeoutHandler timeoutHandler) {
		this.timeoutHandler = timeoutHandler;
		this.timeoutHandler.setParent(this);
	}

	public void processExecutionPhase(ExecutionContext context) {
		
	}

	public void setPages(List<String> pages) {
		this.pages = pages;
	}
	
	public List<String> getPages() {
		return pages;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}
	
	public void setScoreHandler(PropertyHolder scoreHandler) {
		this.scoreHandler = scoreHandler;
	}

	public PropertyHolder getScoreHandler() {
		return scoreHandler;
	}
	
	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	public boolean geti18nEnabled() {
		return i18nEnabled;
	}
	
	public void seti18nEnabled(boolean i18nEnabled) {
		this.i18nEnabled  = i18nEnabled;
	}

}
