package jms.cogtasks.executables;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jms.cogtasks.helperClasses.GridSpanStimulus;
import ch.tatool.core.data.DataUtils;
import ch.tatool.core.data.IntegerProperty;
import ch.tatool.core.data.Misc;
import ch.tatool.core.data.Points;
import ch.tatool.core.data.Question;
import ch.tatool.core.data.Result;
import ch.tatool.core.data.Timing;
import ch.tatool.core.display.swing.ExecutionDisplayUtils;
import ch.tatool.core.display.swing.SwingExecutionDisplay;
import ch.tatool.core.display.swing.container.ContainerUtils;
import ch.tatool.core.display.swing.container.RegionsContainer;
import ch.tatool.core.display.swing.container.RegionsContainer.Region;
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
 * displays a sequence of grid locations that the participants
 * must remember and recall in correct serial order.
 * @author James Stone
 *
 */

public class MatrixSpan extends BlockingAWTExecutable implements DescriptivePropertyHolder, 
		ExecutionPhaseListener {

	Logger logger = LoggerFactory.getLogger(MatrixSpan.class);
	
	private RegionsContainer regionsContainer;
	
	//phases of task//
	private Phase currentPhase;		
	public enum Phase {
		INIT, MEMO, RECALL
	}
	
	//properties of interest//
	private IntegerProperty loadProperty = new IntegerProperty("load");
	private IntegerProperty trialNoProperty = new IntegerProperty("trialNo");
	private String current_user;
	
	//panels
	private GridSpanStimulus thisTrialPanel; //this is a JPanel with the grid constructed//
	private JPanel holdingPanel;
	private JPanel recallPanel;
	private JPanel tmpPanel;
	
	//timing
	private Timer timer;
	private TimerTask suspendExecutableTask;
	private long startTime;
	private long endTime;
	private int displayDuration = 1000; //duration string should be displayed in ms//
	
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
	
	//stimuli
	private ArrayList<Integer> stimuli;
	
	private int trialCounter;
	private int memCounter; //counts memoranda presented per trial//
	private int respCounter; //counts responses given//
	private ArrayList<Integer> correctResponse; 
	private ArrayList<Integer> givenResponse; 
	
	//globals
	private int gridPanelSize;
	private int ScalingFactor = 75;
	private int gridDimension = 4;
	private Random rand;
	final Color fillColor = new Color(0,51,102);
	private ArrayList<JButton> buttons;
	private recallButtonListener recallButtonListener = new recallButtonListener();
	
	/*
	 * need an indicator as to whether this executable is being used to run a simple span task 
	 * or complex span tasks as the behaviour required can be slightly different for certain things.
	 */
	private int ComplexSpan = 0; //"0" by default, this will be the code for simple span, "1" if complex.//
	
	/*
	 * Constructor
	 */
	public MatrixSpan() {
		
		try
		{
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch(Exception e)
		{}	
		
		rand = new Random();
		timer = new Timer();
	}
	
	/*
	 * Method called at the start of execution
	 * (non-Javadoc)
	 * @see ch.tatool.core.executable.BlockingAWTExecutable#startExecutionAWT()
	 */
	protected void startExecutionAWT() {
		
		//MetalLookAndFeel, I like to use JButtons as grids.//
		try
		{
			UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch(Exception e)
		{}
		
		
		//initialise environment//
		ExecutionContext context = getExecutionContext();
		SwingExecutionDisplay display = ExecutionDisplayUtils.getDisplay(context);
		ContainerUtils.showRegionsContainer(display);
		regionsContainer = ContainerUtils.getRegionsContainer();
		
		//add user info to the status panel//
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
	 * Method that sets up a trial, populates the display with the objects needed, 
	 * reset a few variables that are used to keep track of a trial, and generate 
	 * the stimuli for this trial (which grids to show).
	 */
	private void startInitPhase() {
		
		holdingPanel = new JPanel(); //overall panel that will fill the screen (minus the status panel)//
		holdingPanel.setBackground(Color.WHITE);
		holdingPanel.setLayout(new BoxLayout(holdingPanel, BoxLayout.Y_AXIS));
		holdingPanel.setBorder(BorderFactory.createTitledBorder("Remember the sequence"));
		regionsContainer.setRegionContent(Region.CENTER, holdingPanel);
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		refreshRegion(Region.CENTER);
		Dimension mainPanelSize = holdingPanel.getSize(); //get dimension of panel so we can work out how big to make the grid//
		mainPanelSize.height -= 100; //take 100 pixels off the height as a buffer//
		mainPanelSize.height = (mainPanelSize.height / 100) * ScalingFactor;//use the scaling factor to reduce the size to the desired amount//
		
		if (mainPanelSize.height % gridDimension == 0) {
			gridPanelSize = mainPanelSize.height;
		} else {
			gridPanelSize = mainPanelSize.height - (mainPanelSize.height % gridDimension);
		}
		
		thisTrialPanel = new GridSpanStimulus(gridDimension,gridPanelSize); 
		tmpPanel = new JPanel();
		tmpPanel.setBorder(BorderFactory.createTitledBorder("Remember the sequence"));
		tmpPanel.add(thisTrialPanel);
		tmpPanel.setBackground(Color.WHITE);
		int bufferedSize = gridPanelSize + 35;
		tmpPanel.setPreferredSize(new Dimension(bufferedSize,bufferedSize));
		tmpPanel.setMaximumSize(new Dimension(bufferedSize,bufferedSize));	
		
		//method that handles stimuli generation//
		generateStimuli();
		
		//reset counters and response containers//
		memCounter = 0;
		respCounter = 0;
		correctResponse = new ArrayList<Integer>();
		givenResponse = new ArrayList<Integer>();
		Result.getResultProperty().setValue(this,null);
		
		//change to memo phase//
		currentPhase = Phase.MEMO;
		//add tmpPanel to holdingPanel vertically centered//
		holdingPanel.add(Box.createVerticalGlue());
		holdingPanel.add(tmpPanel);
		holdingPanel.add(Box.createVerticalGlue());
		
		refreshRegion(Region.CENTER);
		
		//start memo phase
		startMemoPhase();
	}	
	
	/*
	 * Method that handles presenting stimuli
	 */
	private void startMemoPhase() {
		
		//change colour of grid that has been selected as this elements TBR item//
		thisTrialPanel.fillButton(stimuli.get(memCounter), fillColor);

		//create the suspendExec task, it is actually called further down by the timer, creates the 
		//displayDuration delay we want (preesntation time). 
		suspendExecutableTask = new TimerTask() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						
						thisTrialPanel.fillButton(stimuli.get(memCounter), Color.WHITE);
						memCounter++; //incremement memCounter//
						
						//if this is is last stim then change to recall phase//
						if (memCounter == stimuli.size()) {
							currentPhase = Phase.RECALL;		
						}
						
						suspendExecutable(); // suspend task
					}
				});
			}
		};
		
		regionsContainer.setRegionContent(Region.CENTER, holdingPanel);
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		//set the timer off that will run the suspendExecutableTask code when it runs out//
		timer.schedule(suspendExecutableTask, displayDuration);				
	}
	
	/*
	 * Method to handle the recall phase of the task. Present the grid and allow the user to press 
	 * them one at a time to give their response. Move on once the input number reaches the span size 
	 * of the current trial.
	 */
	private void startRecallPhase() {
		produceRecallGrid();
		regionsContainer.setRegionContent(Region.CENTER, holdingPanel);
		Timing.getStartTimeProperty().setValue(this, new Date());
		startTime = System.nanoTime();
		
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);		
	}
	
	/**
	 * called at the end of the trial, important for data logging.
	 */
	private void processProperties() {
		endTime = System.nanoTime();
		Timing.getEndTimeProperty().setValue(this, new Date());
		correctResponse = stimuli;
		Question.getResponseProperty().setValue(this, givenResponse);
		Question.setQuestionAnswer(this, String.valueOf(correctResponse), correctResponse);
		boolean success = correctResponse.equals(givenResponse);
		loadProperty.setValue(this, stimuli.size());
		
		Points.setZeroOneMinMaxPoints(this);
		Points.setZeroOnePoints(this, success);
		Result.getResultProperty().setValue(this, success);
		
		Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED); //may be a prob?!//
		
		trialNoProperty.setValue(this, trialCounter + 1);
		
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
	
	/**
	 * called at the very end of a trial, resets phase to INIT in case there are more trials to run, 
	 * removes listeners from recall buttons, and terminates execution.
	 */
	private void endTask() {
		currentPhase = Phase.INIT;
		trialCounter++;
		
		holdingPanel.removeAll();
		
		for (JButton jb : buttons) {
			jb.removeActionListener(recallButtonListener);
		}
		
		if (getFinishExecutionLock()) {
			finishExecution();
		}		
	}
	
	/**
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
	
	
	
	/**
	 * Is called whenever the Tatool execution phase changes. We use the
	 * SESSION_START phase to read our stimuli list and set the executable phase
	 * to INIT.
	 */
	public void processExecutionPhase(ExecutionContext context) {
		if (context.getPhase().equals(ExecutionPhase.SESSION_START)) {
			currentPhase = Phase.INIT;
			trialCounter = 0;
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
	
	/**
	 * Is called whenever we copy the properties from our executable to a trial
	 * object for persistence with the help of the DataUtils class.
	 * 
	 * (non-Javadoc)
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
	
	/**
	 * randomly generates stimuli for the trial. If this is the first trial then it 
	 * populates the spansList also. 
	 */
	private void generateStimuli() {
		
		//if first trial then generate trial spans
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
		
		stimuli = new ArrayList<Integer>();
		
		for (int i = 0; i < thisTrialSpan; i++) {
			int genNum = rand.nextInt(gridDimension * gridDimension);
			if (!stimuli.contains(genNum)) {
				stimuli.add(genNum);
			} else {
				i--;
			}
		}
		
	}
	
	/*
	 * creates and displays the objects needed to collect responses.
	 */
	private void produceRecallGrid() {
		//initialise the JButtons we need and add them to an arraylist//
		buttons = new ArrayList<JButton>();
		for (int i = 0; i < (gridDimension * gridDimension); i++) {
			buttons.add(new JButton());
		}
		
		recallPanel = new JPanel();
		recallPanel.setPreferredSize(new Dimension(gridPanelSize + 35,gridPanelSize + 35));
		recallPanel.setMaximumSize(new Dimension(gridPanelSize + 35,gridPanelSize + 35));
		recallPanel.setBackground(Color.WHITE);
		recallPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		recallPanel.setBorder(BorderFactory.createTitledBorder("Click the boxes in the order you were shown"));
		
		
		for (JButton item : buttons) {
			item.setPreferredSize(new Dimension((gridPanelSize / gridDimension),(gridPanelSize / gridDimension)));
			item.setBackground(Color.WHITE);
			recallPanel.add(item);
			item.addActionListener(recallButtonListener);
		}
		
		holdingPanel.removeAll();
		holdingPanel.setBorder(BorderFactory.createTitledBorder("Click the boxes in the order you were shown"));
		holdingPanel.add(Box.createVerticalGlue());
		holdingPanel.add(recallPanel);
		holdingPanel.add(Box.createVerticalGlue());
	}
	
	/**
	 * Listener class applied to the buttons during the recall phase. Deals with what the program 
	 * should do when a response is given. 
	 * 
	 * @author James Stone
	 *
	 */
	private class recallButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			//change colour of clicked grid and disable it from being pressed again
			JButton gridClicked = (JButton)(event.getSource());
			gridClicked.setBackground(fillColor);
			gridClicked.setEnabled(false);
			givenResponse.add(buttons.indexOf(gridClicked));
			//increment respCounter
			respCounter++;
			
			//check to see if respCounter is equal to span size of trial, if so - end it//
			if (respCounter == stimuli.size()) {
				regionsContainer.setRegionContentVisibility(Region.CENTER, false);
				processProperties();
				endTask();
			}
			
		}	
	}
	
	/*
	 * quick method to refresh the display of a region
	 */
	private void refreshRegion(Region reg) {
		regionsContainer.setRegionContentVisibility(reg, false);
		holdingPanel.revalidate();
		regionsContainer.setRegionContentVisibility(reg, true);
	}
	
	/*
	 * the actual number of iterations is not the number of trials you want to run. Each iteration means every 
	 * time this particular executable should be called. In any one trial it is called more than once, it is 
	 * called once for each memoranda display and once to run the recall phase for that trial. Therefore rather 
	 * than have a user add this all up and set it in the XML where mistakes can be made, this method will take 
	 * the span size trials asked for, calculate numIterations needed and reset the value. Only called at the 
	 * start.
	 */
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
		ILSS.get(0).setNumIterations(iterationsRequired);
	}	
	
	/*
	 * helper method to populate the spansList based on XML input. 
	 */
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
	
	/**
	 * 
	 * getset methods to allow values to be set in the XML.
	 */
	
	public int getComplexSpan() {
		return ComplexSpan;
	}
	
	public void setComplexSpan(int n) {
		this.ComplexSpan = n;
	}	
	
	public int getgridDimension() {
		return gridDimension;
	}
	
	public void setgridDimension(int n) {
		this.gridDimension = n;
	}
	
	public int getScalingFactor() {
		return ScalingFactor;
	}
	
	public void setScalingFactor(int n) {
		this.ScalingFactor = n;
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
