package jms.cogtasks.executables;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.tatool.core.data.DataUtils;
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
import ch.tatool.core.display.swing.panel.CenteredTextPanel;
import ch.tatool.core.display.swing.status.StatusPanel;
import ch.tatool.core.display.swing.status.StatusRegionUtil;
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.data.DescriptivePropertyHolder;
import ch.tatool.data.Property;
import ch.tatool.data.Trial;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionOutcome;


/**
 * Displays a sentence in the centre of the screen and waits for 
 * participants to make a judgement on whether it makes sense or 
 * not.
 * 
 * @author James Stone
 *
 */

public class SentenceProcessingTask extends BlockingAWTExecutable implements
ActionPanelListener, DescriptivePropertyHolder {

	Logger logger = LoggerFactory.getLogger(SentenceProcessingTask.class);
	
	//panels for display
	private CenteredTextPanel sentencePanel;
	private KeyActionPanel responsePanel;
	
	// variables // 
	private String SentenceBankFileName;
	private ArrayList<String> sentenceBank;
	private ArrayList<String> answerBank;
	private int itemno = 0;
	private String thisTrialSentence = "";
	private int correctResponse;
	private int givenResponse;
	private long startTime;
	private long endTime;
	private Random rand;
	private RegionsContainer regionsContainer;
	final Font treb = new Font("Trebuchet MS", 1, 26);
	final Color fontColor = new Color(0,51,102);
	private String current_user;
	
	/**
	 * Constructor for the sentence processing task.
	 */
	public SentenceProcessingTask() {
		sentencePanel = new CenteredTextPanel();
		responsePanel = new KeyActionPanel();
		responsePanel.addActionPanelListener(this);
		rand = new Random();
	}
	
	/**
	 * Method is called at the start of each execution
	 */
	protected void startExecutionAWT() {
		ExecutionContext context = getExecutionContext();
		SwingExecutionDisplay display = ExecutionDisplayUtils.getDisplay(context);
		ContainerUtils.showRegionsContainer(display);
		regionsContainer = ContainerUtils.getRegionsContainer();
		
		/**
		 * Don't need to call this everytime, only the first time. Probably don't need it 
		 * at all because this exec will only ever be used in tandem with a storage 
		 * exec.
		 */
		if (itemno == 0) {
			current_user = context.getExecutionData().getModule().getUserAccount().getName();
			StatusPanel customNamePanel = (StatusPanel) StatusRegionUtil.getStatusPanel("custom1");
			customNamePanel.setProperty("title","User");
			customNamePanel.setProperty("value", current_user);			
		}

		
		String thisTrialSentence = getSentence(); //call a method to get the sentence for this execution//
		
		sentencePanel.setFont(treb);
		sentencePanel.setTextColor(fontColor);
		sentencePanel.setText(thisTrialSentence);	
		
		responsePanel.addKey(KeyEvent.VK_LEFT, "Makes Sense", 1);
		responsePanel.addKey(KeyEvent.VK_RIGHT, "Nonsense", 0);
		
		regionsContainer.setRegionContent(Region.CENTER, sentencePanel);
		regionsContainer.setRegionContent(Region.SOUTH,  responsePanel);
		
		Timing.getStartTimeProperty().setValue(this, new Date());
		startTime = System.nanoTime();
		
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, true);
		responsePanel.enableActionPanel(); 
	}	
	
	/**
	 * Collects a sentence to display for judgement.
	 * If it is the first time called then it reads in the sentence bank 
	 * from a file. 
	 * @return String - The sentence to display.
	 */
	private String getSentence() {
		if (itemno == 0) { //then this is first execution and we need to read in the sentences//
			Scanner s;
			sentenceBank = new ArrayList<String>();
			answerBank = new ArrayList<String>();
			
			s = new Scanner(new InputStreamReader(this.getClass().getResourceAsStream("/jms/cogtasks/stimuli/word_lists/" + SentenceBankFileName)));
			s.useDelimiter("[,\n]");
			while (s.hasNext()) {
				String s1 = s.next();
				String s2 = (String) s.next().substring(0, 1);
				sentenceBank.add(s1);
				answerBank.add(s2);
			}
			s.close();
		}
		
		int SentenceIndex = rand.nextInt(sentenceBank.size());
		String toReturn = sentenceBank.get(SentenceIndex);
		sentenceBank.remove(SentenceIndex);
		if (answerBank.get(SentenceIndex).equals("y")) {
			correctResponse = 1;
			answerBank.remove(SentenceIndex);
		} else if (answerBank.get(SentenceIndex).equals("n")) {
			correctResponse = 0;
			answerBank.remove(SentenceIndex);
		}
		
		itemno++;
		
		return toReturn;
	}

	/**
	 * Processes the response of the user.
	 */
	public void actionTriggered(ActionPanel source, Object actionValue) {
		responsePanel.disableActionPanel();
		
		regionsContainer.setRegionContentVisibility(Region.CENTER, false);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, false);
		regionsContainer.removeRegionContent(Region.CENTER);
		regionsContainer.removeRegionContent(Region.SOUTH);
		
		endTime = System.nanoTime();
		Timing.getEndTimeProperty().setValue(this, new Date());
		
		givenResponse = (Integer) actionValue;
		
		endTask();		
	}

	/**
	 * Called to end the executable.
	 */
	private void endTask() {
		regionsContainer.setRegionContentVisibility(Region.CENTER, false);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, false);
		regionsContainer.removeRegionContent(Region.CENTER);
		regionsContainer.removeRegionContent(Region.SOUTH);
		
		Question.getResponseProperty().setValue(this, givenResponse);
		Question.setQuestionAnswer(this,  thisTrialSentence, correctResponse);
		boolean success = correctResponse == givenResponse;
		Points.setZeroOneMinMaxPoints(this);
		Points.setZeroOnePoints(this, success);
		Result.getResultProperty().setValue(this, success);
		Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);
		Misc.getOutcomeProperty().setValue(getExecutionContext(), ExecutionOutcome.FINISHED);
		
		//duration time property//
		long duration = 0;
		if (endTime > 0) {
			duration = endTime - startTime;
			if (duration <= 0) {
				duration = 0;
			}
		}
		long ms = (long) duration/1000000;
		Timing.getDurationTimeProperty().setValue(this, ms);
		
		//new trial//
		Trial currentTrial = getExecutionContext().getExecutionData().addTrial();
		currentTrial.setParentId(getId());
		
		//add all executable properties//
		DataUtils.storeProperties(currentTrial,  this);
		
		//finish execution //
		if (getFinishExecutionLock()) {
			finishExecution();
		}
	}	
	
	/**
	 * Is called whenever we copy the properties from our executable to a trial
	 * object for persistence with the help of the DataUtils class.
	 */
	public Property<?>[] getPropertyObjects() {
		return new Property[] { Points.getMinPointsProperty(),
				Points.getPointsProperty(), Points.getMaxPointsProperty(),
				Question.getQuestionProperty(), Question.getAnswerProperty(),
				Question.getResponseProperty(), Result.getResultProperty(),
				Timing.getStartTimeProperty(), Timing.getEndTimeProperty(),
				Timing.getDurationTimeProperty(), Misc.getOutcomeProperty() };
	}
	
	/**
	 * 
	 */
	protected void cancelExecutionAWT() {
		//timer.cancel();
    }	
	
	/**
	 * 
	 * @return
	 */
	public String getSentenceBankFileName() {
		return SentenceBankFileName;
	}
	/**
	 * 
	 * @param SentenceBankFileName
	 */
	public void setSentenceBankFileName(String SentenceBankFileName) {
		this.SentenceBankFileName = SentenceBankFileName;
	}

}
