package jms.cogtasks.executables;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.tatool.core.data.DataUtils;
import ch.tatool.core.data.IntegerProperty;
import ch.tatool.core.data.Misc;
import ch.tatool.core.data.Points;
import ch.tatool.core.data.Question;
import ch.tatool.core.data.Result;
import ch.tatool.core.data.Timing;
import ch.tatool.core.display.swing.ExecutionDisplayUtils;
import ch.tatool.core.display.swing.SwingExecutionDisplay;
import ch.tatool.core.display.swing.action.ActionPanel;
import ch.tatool.core.display.swing.action.ActionPanelListener;
import ch.tatool.core.display.swing.action.InputActionPanel;
import ch.tatool.core.display.swing.container.ContainerUtils;
import ch.tatool.core.display.swing.container.RegionsContainer;
import ch.tatool.core.display.swing.container.RegionsContainer.Region;
import ch.tatool.core.display.swing.panel.CenteredTextPanel;
import ch.tatool.core.display.swing.status.StatusPanel;
import ch.tatool.core.display.swing.status.StatusRegionUtil;
import ch.tatool.core.element.ElementUtils;
import ch.tatool.core.element.IteratedListSelector;
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.data.DescriptivePropertyHolder;
import ch.tatool.data.Property;
import ch.tatool.data.Trial;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionOutcome;
import ch.tatool.exec.ExecutionPhase;
import ch.tatool.exec.ExecutionPhaseListener;

/**
 * Display a sequence of strings that the participant must remember and then 
 * recall in the correct order at the end.
 * @author James Stone
 *
 */

public class WordDigitSpan extends BlockingAWTExecutable implements 
		ActionPanelListener, DescriptivePropertyHolder, ExecutionPhaseListener {
	
	Logger logger = LoggerFactory.getLogger(WordDigitSpan.class);
	
	private RegionsContainer regionsContainer; 
	String current_user;	
	public int StimuliType = 0; //set this using the XML, 1 for digits, 2 for words.//
	
	final Font treb = new Font("Trebuchet MS", 1, 26);
	final Color fontColor = new Color(0,51,102);
	
	//phases of task//
	public enum Phase {
		INIT, MEMO, RECALL
	}
	
	private Phase currentPhase;
	
	//properties of interest//
	public IntegerProperty loadProperty = new IntegerProperty("load");
	public IntegerProperty trialNoProperty = new IntegerProperty("trialNo");
	
	//panels//
	private CenteredTextPanel displayPanel;
	private InputActionPanel responsePanel;
	
	//timing
	private Timer timer;
	private TimerTask suspendExecutableTask;
	private TimerTask startRecallTask;
	private int displayDuration = 1000; //duration string should be displayed in ms//
	private static int interResponseDuration = 1000; //blank screen between recalls//
	
	//stimuli//
	public int[] numbers; //if digit span//
	public String[] words; //if word span//
	private ArrayList<String> wordBank = new ArrayList<String>(); //method will fill this arraylist with words from an external file//
	private String WordBankFileName = "";
	private Random rand;
	private int minDigit = 1; //the smallest digit that can be selected to-be-remembered, can be changed using XML.
	private int maxDigit = 99; // the largest digit that can be selected to-be-remembered, can be changed using XML.
	
	//trials
	private int spanTwoTrials = 0; //default zero, if set in the xml then these will be replaced.
	private int spanThreeTrials = 0;
	private int spanFourTrials = 0;
	private int spanFiveTrials = 0;
	private int spanSixTrials = 0;
	private int spanSevenTrials = 0;
	private int spanEightTrials = 0;
	private int spanNineTrials = 0;
	//will then use these values to populate the spansList.
	private ArrayList<Integer> spansList = new ArrayList<Integer>();
	//should the list length be ordered presentation or randomised
	private int randomisedTrials = 0; //default is 0 which is for in sequence, set to 1 in the XML for randomised order.
	
	public int trialCounter = 0;
	private int memCounter; //counts memoranda presented per trial//
	public int respCounter; //counts responses given//
	public int correctResponseDigits; //if digit span//
	public String correctResponseWord; //if word span//
	public int givenResponseDigits; //if digit span//
	public String givenResponseWord; //if word span//
	
	/*
	 * need an indicator as to whether this executable is being used to run a simple span task 
	 * or complex span tasks as the behaviour required can be slightly different for certain things.
	 */
	private int ComplexSpan = 0; //"0" by default, this will be the code for simple span, "1" if complex.//
	
	
	public long startTime;
	public long endTime;
	
	/*
	 * constructor
	 */
	public WordDigitSpan() {
		displayPanel = new CenteredTextPanel();
		displayPanel.setTextFont(treb);
		displayPanel.setTextColor(fontColor);
		responsePanel = new InputActionPanel();
		if (StimuliType == 1) {
			responsePanel.setTextDocument(2, InputActionPanel.FORMAT_ONLY_DIGITS);
		} else if (StimuliType == 2) {
			//need to add code for words//
			responsePanel.setTextDocument(20, InputActionPanel.FORMAT_ALL);
		} else {
			//add exception handling//
		}
		
		responsePanel.addActionPanelListener(this);
		
		rand = new Random();
		timer = new Timer();

	}
	
	/*
	 * start method
	 * @see ch.tatool.core.executable.BlockingAWTExecutable#startExecutionAWT()
	 */
	protected void startExecutionAWT() {
		//initialise environment//
		ExecutionContext context = getExecutionContext();
		SwingExecutionDisplay display = ExecutionDisplayUtils.getDisplay(context);
		ContainerUtils.showRegionsContainer(display);
		regionsContainer = ContainerUtils.getRegionsContainer();
		
		//get the username of the current user and uses it to set the values of the custom status panel//
		current_user = context.getExecutionData().getModule().getUserAccount().getName();
		StatusPanel customNamePanel = (StatusPanel) StatusRegionUtil.getStatusPanel("custom1");
		customNamePanel.setProperty("title","User");
		customNamePanel.setProperty("value", current_user);
		
		switch(currentPhase) {
		case INIT:
			startInitPhase();
			break;
		case MEMO:
			startMemoPhase();
			break;
		case RECALL:
			startRecallPhase();
			break;
		}
	}
	
	/*
	 * If this is the very start of a trial then this method is called.
	 * Resets some variables while calling the method to generate the stimuli 
	 * to be used.
	 */
	private void startInitPhase() {
		generateStimuli();
		
		//reset stim counter//
		memCounter = 0;
		respCounter = 0;
		
		Result.getResultProperty().setValue(this,null);
		
		//start memo phase//
		currentPhase = Phase.MEMO;
		startMemoPhase();
	}
	
	/*
	 * If phase is set to memo then the program needs to start displaying the 
	 * words/digits(memoranda). 
	 */
	private void startMemoPhase() {
		
		String stimulus = "";
		
		if (StimuliType == 1) {
			stimulus = String.valueOf(numbers[memCounter]);
		} else if (StimuliType == 2) {
			stimulus = words[memCounter];
		}
		
		displayPanel.setTextSize(120);
		displayPanel.setText(stimulus);
		
		memCounter++; //incremement memCounter as an additional digit/word has been shown//
		
		//if this is is last stim then change to recall phase//
		
		if (StimuliType == 1) {
			if (memCounter == numbers.length) {
				currentPhase = Phase.RECALL;
			}
		} else if (StimuliType == 2) {
			if (memCounter == words.length) {
				currentPhase = Phase.RECALL;
			}
		}
		
		//suspend for specified amount of time, this is presentation time of each digit/word//
		suspendExecutableTask = new TimerTask() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						suspendExecutable(); // suspend task
					}
				});
			}
		};

		regionsContainer.setRegionContent(Region.CENTER, displayPanel);
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		timer.schedule(suspendExecutableTask, displayDuration);		
	}
	
	/*
	 * method that is called if we are in the recall phase. Handles the response 
	 * input from the user.
	 */
	private void startRecallPhase() {
		StringBuilder text = new StringBuilder();
		if (StimuliType == 1) {
			text.append("Number ");
		} else if (StimuliType == 2) {
			text.append("word ");
		}
		text.append(respCounter + 1);
		text.append(": ");
		displayPanel.setTextSize(60);
		displayPanel.setText(text.toString());
		
		responsePanel.clearTextField();
		
		Timing.getStartTimeProperty().setValue(this, new Date());
		startTime = System.nanoTime();
		
		regionsContainer.setRegionContent(Region.CENTER, displayPanel);
		regionsContainer.setRegionContent(Region.SOUTH, responsePanel);
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, true);

		responsePanel.enableActionPanel();		
	}

	/*
	 * Sets the outcome of this executable to SUSPENDED in order for us to be
	 * able to continue where we left after other executables have executed.
	 */
	private void suspendExecutable() {
		regionsContainer.setRegionContentVisibility(Region.CENTER, false);

		// set outcome in the execution context to SUSPENDED to mark compound
		// element as being suspended in order to return later
		if (getExecutionContext() != null) {
			Misc.getOutcomeProperty().setValue(getExecutionContext(), ExecutionOutcome.SUSPENDED);
		}
		// finish the execution and make sure nothing else already did so
		if (getFinishExecutionLock()) {
			finishExecution();
		}
	}
	
	/*
	 * listens to the user input and responds accordingly//(non-Javadoc)
	 * @see ch.tatool.core.display.swing.action.ActionPanelListener#actionTriggered(ch.tatool.core.display.swing.action.ActionPanel, java.lang.Object)
	 */
	public void actionTriggered(ActionPanel source, Object actionValue) {
		if (StimuliType == 1) {
			try {
				givenResponseDigits = Integer.valueOf((String) actionValue);
			} catch (NumberFormatException e) {
				givenResponseDigits = 0;
			}
		} else if (StimuliType == 2) {
				givenResponseWord = (String) actionValue;
		}
		
		endTime = System.nanoTime();
		Timing.getEndTimeProperty().setValue(this, new Date());

		responsePanel.disableActionPanel();
		regionsContainer.setRegionContentVisibility(Region.CENTER, false);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, false);

		// process the properties for this trial
		processProperties();
		
		//decide whether we have to display another recall page or if we can finish executable//
		boolean displayAnother = false;
		switch(StimuliType) {
		case 1:
			if (respCounter < numbers.length -1) {displayAnother = true;}
			break;
		case 2:
			if (respCounter < words.length -1) {displayAnother = true;}
			break;
		}
		
		if (displayAnother) {
			respCounter++;
			//show next recall entry after a pause
			startRecallTask = new TimerTask() {
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							//changeStatusPanelOutcome(null);
							startRecallPhase();
						}
					});
				}
			};
			
			regionsContainer.setRegionContentVisibility(Region.CENTER, false);
			timer.schedule(startRecallTask, interResponseDuration);
			
		} else {
			currentPhase = Phase.INIT;
			trialCounter++;
			
			if (getFinishExecutionLock()) {
				finishExecution();
			}
		}
		
	}
	
	public void processProperties() {
		switch (StimuliType) {
		case 1:
			int stimulus_num = numbers[respCounter];
			correctResponseDigits = Integer.valueOf(stimulus_num);
			Question.getResponseProperty().setValue(this, givenResponseDigits);
			Question.setQuestionAnswer(this, String.valueOf(stimulus_num), correctResponseDigits);
			break;
		
		case 2:
			String stimulus_word = words[respCounter];
			correctResponseWord = (String) stimulus_word;
			Question.getResponseProperty().setValue(this, givenResponseWord);
			Question.setQuestionAnswer(this, String.valueOf(stimulus_word), correctResponseWord);
			break;
		}
		
		Boolean success = null;
		if (StimuliType == 1) {
			success = correctResponseDigits == givenResponseDigits;
			loadProperty.setValue(this, numbers.length);
		} else if (StimuliType == 2) {
			success = correctResponseWord.equals(givenResponseWord);
			loadProperty.setValue(this, words.length);
		}
		
		Points.setZeroOneMinMaxPoints(this);
		Points.setZeroOnePoints(this, success);
		Result.getResultProperty().setValue(this, success);
		
		switch(StimuliType){
		case 1:
			if (respCounter < (numbers.length - 1)) {
				Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.SUSPENDED);
			} else {
				Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);
			}
			break;
		
		case 2:
			if (respCounter < (words.length - 1)) {
				Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.SUSPENDED);
			} else {
				Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);
			}
		}

		
		trialNoProperty.setValue(this, trialCounter + 1);
		
		// change feedback status panel
		changeStatusPanelOutcome(success);

		// set duration time property
		long duration = 0;
		if (endTime > 0) {
			duration = endTime - startTime;
		}
		long ms = (long) duration / 1000000;
		Timing.getDurationTimeProperty().setValue(this, ms);

		if (getExecutionContext() != null) {
			Misc.getOutcomeProperty().setValue(getExecutionContext(), ExecutionOutcome.FINISHED);
		}
		
		// create new trial and store all executable properties in the trial
		Trial currentTrial = getExecutionContext().getExecutionData().addTrial();
		currentTrial.setParentId(getId());
		DataUtils.storeProperties(currentTrial, this);
	}	
	
	/*
	 * Not quite sure why I have included this, although I am sure I had reason to at the time.
	 * Tatool should take care of feedback if I have set the variables and asked for a 
	 * feedback status panel....look into this.
	 */
	public void changeStatusPanelOutcome(Boolean value) {
        StatusPanel panelFeedback = StatusRegionUtil.getStatusPanel(StatusPanel.STATUS_PANEL_OUTCOME);
        if (panelFeedback != null) {
        	if (value == null) {
        		panelFeedback.reset();
        	} else {
        		panelFeedback.setProperty(StatusPanel.PROPERTY_VALUE, value);
        	}
        } 
	}
	
	/**
	 * Is called whenever we copy the properties from our executable to a trial
	 * object for persistence with the help of the DataUtils class.
	 * 
	 * @see ch.tatool.data.DescriptivePropertyHolder#getPropertyObjects()
	 */
	public Property<?>[] getPropertyObjects() {
		return new Property[] { Points.getMinPointsProperty(),
				Points.getPointsProperty(), Points.getMaxPointsProperty(),
				Question.getQuestionProperty(), Question.getAnswerProperty(),
				Question.getResponseProperty(), Result.getResultProperty(),
				Timing.getStartTimeProperty(), Timing.getEndTimeProperty(),
				Timing.getDurationTimeProperty(), Misc.getOutcomeProperty(),
				loadProperty, trialNoProperty };
	}

	/*
	 * Is called whenever the Tatool execution phase changes. We use the
	 * SESSION_START phase to read our stimuli list and set the executable phase
	 * to INIT.
	 * 
	 * @see ch.tatool.exec.ExecutionPhaseListener#processExecutionPhase(ch.tatool.exec.ExecutionContext)
	 */
	public void processExecutionPhase(ExecutionContext context) {
		if (context.getPhase().equals(ExecutionPhase.SESSION_START)) {
			currentPhase = Phase.INIT;
		}
	}
	/*
	 * (non-Javadoc)
	 * @see ch.tatool.core.executable.BlockingAWTExecutable#cancelExecutionAWT()
	 */
	protected void cancelExecutionAWT() {
		timer.cancel();
		currentPhase = Phase.INIT;
    }
	
	/*
	 * If using the executable to run words rather than digits as the stimuli then this 
	 * method is called to generate the word bank from the data file provided.
	 */
	private ArrayList<String> generateWordBank(String filename) {
		
		Scanner s;
		ArrayList<String> list = new ArrayList<String>();
		String file_location = "src/main/resources/jms/cogtasks/stimuli/word_lists/" + filename;
		
		try {
			s = new Scanner(new File(file_location));
			while (s.hasNext()) {
				list.add(s.next());
			}
			s.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	/**
	 * Method called at the start of a trial to select the required stimuli to use
	 * for that trial.
	 */
	private void generateStimuli() {
		
		//if first trial then generate trial spans and set executable iterations
		if (trialCounter == 0) {
			//compile ArrayList for spans//
			spansList = generateSpanList();
			setNumIterations();
		}
		
		//if not randomisedTrials then just take the first element in spansList //
		//if randomisedTrials take random int from spans to use as list length in this trial, then remove that element from spans//
		int thisTrialSpan = 99;
		if (randomisedTrials == 0) {
			thisTrialSpan = spansList.get(0);
			spansList.remove(0);
		} else if (randomisedTrials == 1) {
			int index = rand.nextInt(spansList.size());
			thisTrialSpan = spansList.get(index);
			spansList.remove(index);
		} else {
			System.out.println("thisTrialSpan not set, check the behaviour of 'randomisedTrials' variable");
			System.out.println("randomisedTrials value: " + randomisedTrials);
		}
		
		switch(StimuliType){
		
		case 1: //need digits to use as stimuli//
			numbers = new int[thisTrialSpan];
			List<Integer> numberList = new ArrayList<Integer>();
			for (int i = 0; i < numbers.length; i++) {
				int tmpNumber = minDigit + rand.nextInt(maxDigit - minDigit); //numbers can be between minDigit and maxDigit
				if (!numberList.contains(tmpNumber)) {
					numbers[i] = tmpNumber;
					numberList.add(tmpNumber);
				} else {
					i--;
				}
			}
			break;
			
		case 2: //need words to use as stimuli//	
			//if first trial then need to create word bank
			if (trialCounter == 0) {
				wordBank = generateWordBank(WordBankFileName);
			}
			//build word list for this trial//
			words = new String[thisTrialSpan];
			for (int j = 0; j < words.length; j++) {
				int index = rand.nextInt(wordBank.size());
				words[j] = wordBank.get(index);
				wordBank.remove(index); //remove each word as it is used//
			}

		}
	}
	
	public ArrayList<Integer> generateSpanList() {
		ArrayList<Integer> tmpList = new ArrayList<Integer>();
		for (int j = 0; j < spanTwoTrials; j++) {
			tmpList.add(2);
		}
		for (int j = 0; j < spanThreeTrials; j++) {
			tmpList.add(3);
		}
		for (int j = 0; j < spanFourTrials; j++) {
			tmpList.add(4);
		}
		for (int j = 0; j < spanFiveTrials; j++) {
			tmpList.add(5);
		}
		for (int j = 0; j < spanSixTrials; j++) {
			tmpList.add(6);
		}
		for (int j = 0; j < spanSevenTrials; j++) {
			tmpList.add(7);
		}
		for (int j = 0; j < spanEightTrials; j++) {
			tmpList.add(8);
		}
		for (int j = 0; j < spanNineTrials; j++) {
			tmpList.add(9);
		}
		
		return tmpList;
	}
	
	public void setNumIterations() {
		@SuppressWarnings("unchecked")
		List<IteratedListSelector> ILSS = (List<IteratedListSelector>)(List<?>) ElementUtils.findHandlersInStackByType(getExecutionContext(), IteratedListSelector.class);
		//how many iterations needed?
		int iterationsRequired = 0;
		
		if (ComplexSpan == 0) {
			//sum of spans list + size of spans list.
			for (int i = 0; i < spansList.size(); i++) {
				iterationsRequired += spansList.get(i);
			}
			iterationsRequired += spansList.size();		
		} else if (ComplexSpan == 1) {
			iterationsRequired = spansList.size();
		}

		
		//set the value
		if (iterationsRequired > 0) {
			ILSS.get(0).setNumIterations(iterationsRequired);
		} else {
			System.out.println("Problem with setting number of iterations. Check ComplexSpan value");
			System.out.println("Complex Span value: " + ComplexSpan);
		}
		
	}

	
	//get-set methods to allow the xml to pass values to the executable//
	public int getStimuliType() {
		return StimuliType;
	}
	
	public void setStimuliType(int StimuliType) {
		this.StimuliType = StimuliType;
	}
	
	public String getWordBankFileName() {
		return WordBankFileName;
	}
	
	public void setWordBankFileName(String WordBankFileName) {
		this.WordBankFileName = WordBankFileName;
	}
	
	public int getComplexSpan() {
		return ComplexSpan;
	}
	
	public void setComplexSpan(int n) {
		this.ComplexSpan = n;
	}
	
	public int getminDigit() {
		return minDigit;
	}
	
	public void setminDigit(int n) {
		this.minDigit = n;
	}
	
	public int getmaxDigit() {
		return this.maxDigit;
	}
	
	public void setmaxDigit(int n) {
		this.maxDigit = n;
	}
	
	public int getrandomisedTrials() {
		return randomisedTrials;
	}
	
	public void setrandomisedTrials(int n) {
		this.randomisedTrials = n;
	}
	
	public int getspanTwoTrials() {
		return spanTwoTrials;
	}
	
	public void setspanTwoTrials(int n) {
		this.spanTwoTrials = n;
	}
	
	public int getspanThreeTrials() {
		return spanThreeTrials;
	}
	
	public void setspanThreeTrials(int n) {
		this.spanThreeTrials = n;
	}
	
	public int getspanFourTrials() {
		return spanFourTrials;
	}
	
	public void setspanFourTrials(int n) {
		this.spanFourTrials = n;
	}
	
	public int getspanFiveTrials() {
		return spanFiveTrials;
	}
	
	public void setspanFiveTrials(int n) {
		this.spanFiveTrials = n;
	}
	
	public int getspanSixTrials() {
		return spanSixTrials;
	}
	
	public void setspanSixTrials(int n) {
		this.spanSixTrials = n;
	}
	
	public int getspanSevenTrials() {
		return spanSevenTrials;
	}
	
	public void setspanSevenTrials(int n) {
		this.spanSevenTrials = n;
	}
	
	public int getspanEightTrials() {
		return spanEightTrials;
	}
	
	public void setspanEightTrials(int n) {
		this.spanEightTrials = n;
	}
	
	public int getspanNineTrials() {
		return spanNineTrials;
	}
	
	public void setspanNineTrials(int n) {
		this.spanNineTrials = n;
	}
}