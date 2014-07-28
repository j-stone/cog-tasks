package jms.cogtasks.executables;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jms.cogtasks.helperClasses.RotatedIcon;
import jms.cogtasks.helperClasses.iconPanel;
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
import ch.tatool.core.display.swing.panel.CenteredTextPanel;
import ch.tatool.core.display.swing.status.StatusPanel;
import ch.tatool.core.display.swing.status.StatusRegionUtil;
import ch.tatool.core.element.ElementUtils;
import ch.tatool.core.element.IteratedListSelector;
import ch.tatool.core.element.handler.pause.PauseHandlerUtil;
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.data.DescriptivePropertyHolder;
import ch.tatool.data.Property;
import ch.tatool.data.Trial;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionOutcome;
import ch.tatool.exec.ExecutionPhase;
import ch.tatool.exec.ExecutionPhaseListener;

/**
 * displays a sequence of arrows at various lengths(short or long)/orientations(0-315 at 45 degree intervals) 
 * that the participant must remember and then reproduce at recall.
 * @author James Stone
 *
 */

public class ArrowSpan extends BlockingAWTExecutable implements DescriptivePropertyHolder, 
		ExecutionPhaseListener {

	Logger logger = LoggerFactory.getLogger(ArrowSpan.class);
	
	private RegionsContainer regionsContainer;
	
	//phases of task//
	public enum Phase {
		INIT, MEMO, RECALL
	}
	private Phase currentPhase;	
	
	//properties of interest//
	private IntegerProperty loadProperty = new IntegerProperty("load");
	private IntegerProperty trialNoProperty = new IntegerProperty("trialNo");
	
	//panels//
	private CenteredTextPanel imagePanel;
	private JPanel recallReminderPanel; //stick in region.SOUTH to display answer given on the fly //
	private JPanel holderPanel;
	private JPanel recallPanel;
	private JPanel textPanel;
	private ArrayList<JPanel> blankPanels;
	private JPanel inputPromptPanel;
	private recallPanelHandler recallPanelHandler = new recallPanelHandler();
	private Dimension thisScreenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
	
	private ArrayList<iconPanel> iPanelsLong;
	private ArrayList<iconPanel> iPanelsShort;
	
	//timing
	private Timer timer;
	private TimerTask suspendExecutableTask;
	private int displayDuration = 1000; //duration string should be displayed in ms//
	private long startTime;
	private long endTime;
	
	//stimuli/trials
	private ArrayList<Integer> spansList = new ArrayList<Integer>();
	private int spanTwoTrials = 0; //default zero, if set in the xml then these will be replaced.
	private int spanThreeTrials = 0;
	private int spanFourTrials = 0;
	private int spanFiveTrials = 0;
	private int spanSixTrials = 0;
	private int spanSevenTrials = 0;
	private int spanEightTrials = 0;
	private int spanNineTrials = 0;
	private int randomisedTrials = 0; //default is 0 which is for in sequence, set to 1 in the XML for randomised order.
	private ArrayList<Integer> stimuliLengths;
	private ArrayList<Integer> stimuliDegrees;
	private ImageIcon arrowLong;
	private ImageIcon arrowShort;
	private ImageIcon arrowLong45;
	private ImageIcon arrowShort45;
	private RotatedIcon thisTrialIcon;
	private int ComplexSpan = 0;
	
	//variables//
	private int trialCounter = 0;
	private int memCounter; //counts memoranda presented per trial//
	private int respCounter; //counts responses given//
	private ArrayList<Integer> correctResponse; 
	private ArrayList<Integer> givenResponse;
	private ArrayList<Integer> givenDegrees;
	private ArrayList<Integer> givenLengths;
	private int[] degrees = {0,45,90,135,180,225,270,315};
	private Random rand;
	final Font textFont = new Font("Source Code Pro", 1, 24);
	private String current_user;
	//private Color recallBG = new Color(253,249,249);
	
	//icon panels
	private iconPanel long_0;
	private iconPanel long_1;
	private iconPanel long_2;
	private iconPanel long_3;
	private iconPanel long_4;
	private iconPanel long_5;
	private iconPanel long_6;
	private iconPanel long_7;
	private iconPanel short_0;
	private iconPanel short_1;
	private iconPanel short_2;
	private iconPanel short_3;
	private iconPanel short_4;
	private iconPanel short_5;
	private iconPanel short_6;
	private iconPanel short_7;
	
	
	/**
	 * Constructor for the ArrowSpan executable. 
	 */
	public ArrowSpan() {
		rand = new Random();
		timer = new Timer();
		imagePanel = new CenteredTextPanel();
		recallReminderPanel = new JPanel();
		holderPanel = new JPanel();
		
		//load base icons for use later//
		arrowShort = new ImageIcon(getClass().getResource("/jms/cogtasks/stimuli/imgs/arrows/UP_SHORT_NEW.png"));
		arrowLong = new ImageIcon(getClass().getResource("/jms/cogtasks/stimuli/imgs/arrows/UP_LONG_NEW.png"));
		arrowShort45 = new ImageIcon(getClass().getResource("/jms/cogtasks/stimuli/imgs/arrows/UP_SHORT_NEW_45.png"));
		arrowLong45 = new ImageIcon(getClass().getResource("/jms/cogtasks/stimuli/imgs/arrows/UP_LONG_NEW_45.png"));
		
		recallReminderPanel.setBackground(Color.LIGHT_GRAY);
		holderPanel.setBackground(Color.WHITE);
		inputPromptPanel = new JPanel();
		JLabel inputPromptText = new JLabel("Input: ");
		
		//blank panels
		blankPanels = new ArrayList<JPanel>();
		inputPromptText.setFont(textFont);
		inputPromptPanel.setBackground(Color.LIGHT_GRAY);
		inputPromptPanel.add(inputPromptText);
		
		for (int i = 0; i < 9; i++) {
			blankPanels.add(new JPanel());
		}
	
		for (JPanel p : blankPanels) {
			p.setPreferredSize(new Dimension(100,100));
			p.setBackground(Color.WHITE);
		}
		
	}
	
	/**
	 * Method called at start of execution
	 */
	protected void startExecutionAWT() {
		
		//initialise environment//
		ExecutionContext context = getExecutionContext();
		SwingExecutionDisplay display = ExecutionDisplayUtils.getDisplay(context);
		ContainerUtils.showRegionsContainer(display);
		regionsContainer = ContainerUtils.getRegionsContainer();
		
		if (trialCounter == 0) {
			current_user = context.getExecutionData().getModule().getUserAccount().getName();
			StatusPanel customNamePanel = (StatusPanel) StatusRegionUtil.getStatusPanel("custom1");
			customNamePanel.setProperty("title","User");
			customNamePanel.setProperty("value", current_user);		
		}
		
		regionsContainer.setRegionContent(Region.SOUTH, recallReminderPanel);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, true);
		
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
	
	/**
	 * Method that handles setting up a trial
	 */
	private void startInitPhase() {
		
		//reset arrays//
		correctResponse = new ArrayList<Integer>();
		givenResponse = new ArrayList<Integer>();
		givenDegrees = new ArrayList<Integer>();
		givenLengths = new ArrayList<Integer>();
		stimuliLengths = new ArrayList<Integer>();
		stimuliDegrees = new ArrayList<Integer>();

		//generate stimuli for the trial
		generateStimuli();
		
		//reset stim counter//
		memCounter = 0;
		respCounter = 0;
		
		Result.getResultProperty().setValue(this,null);
		
		//start memo phase//
		currentPhase = Phase.MEMO;
		startMemoPhase();
	}
	
	/**
	 * Method that handles displaying the memoranda to participants
	 */
	private void startMemoPhase() {
		
		PauseHandlerUtil.setCurrentInterElementPauseDuration(getExecutionContext(), 1000);
		
		//set the icon as determined by the stimuli selected//
		setThisTrialIcon();
		imagePanel.setIcon(thisTrialIcon);
		
		//incremement memCounter
		memCounter++; 
		
		//if this is is last stim then change to recall phase//
		if (memCounter == stimuliLengths.size()) {
			currentPhase = Phase.RECALL;
		}
		
		//suspend for specified amount of time//
		suspendExecutableTask = new TimerTask() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						suspendExecutable(); // suspend task
					}
				});
			}
		};

		regionsContainer.setRegionContent(Region.CENTER, imagePanel);
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		timer.schedule(suspendExecutableTask, displayDuration);		
	}
	
	/**
	 * Method handles the recall phase
	 */
	private void startRecallPhase() {
		PauseHandlerUtil.setCurrentInterElementPauseDuration(getExecutionContext(), 250);
		generateRecallPanel();
		holderPanel.add(textPanel);
		holderPanel.add(recallPanel);
		regionsContainer.setRegionContent(Region.CENTER, holderPanel);
		Timing.getStartTimeProperty().setValue(this, new Date());
		startTime = System.nanoTime();
		
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);		
	}	

	/**
	 * generate stimuli
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

		for (int j = 0; j < thisTrialSpan; j++) {
			//generate random int of 0 or 1 that will determine the length of the arrow 0=short 1=long
			int tmpLength = rand.nextInt(2);
			//generate random int to use as selection criteria for orientation.
			int tmpDegree = rand.nextInt(degrees.length);
			//now we add these values to their respective arraylists
			//but first we need to check the combination does not already exist
			boolean alreadyExist = false;
			for (int i = 0; i < stimuliLengths.size(); i++) {
				if (stimuliLengths.get(i) == tmpLength & stimuliDegrees.get(i) == degrees[tmpDegree]) {
					alreadyExist = true;
				}
			}
			
			if (alreadyExist) {
				j--;
			} else {
				stimuliLengths.add(tmpLength);
				stimuliDegrees.add(degrees[tmpDegree]);
			}			
		}
		
		loadProperty.setValue(this, thisTrialSpan);

	}
	
	/**
	 * helper method, sets thisTrialIcon to the appropriate image. 
	 */
	private void setThisTrialIcon() {
		//get the degree of rotation for this item as decided by initialisation of stims
		int thisItemRotation = stimuliDegrees.get(memCounter);
		//if it is a 45,135,225,315 degree rotation we use the _45 image as it is clearer and solves a sizing issue after rotation.
		//so we get a marker for if it is one of these rotations and use this marker to select the right image icon.
		boolean marker45;
		if (thisItemRotation % 10 > 0)
			marker45 = true;
		else
			marker45 = false;		
		
		if (stimuliLengths.get(memCounter) == 0) {
			//then its a short arrow
			if (marker45)
				thisTrialIcon = new RotatedIcon(arrowShort45, thisItemRotation - 45);
			else
				thisTrialIcon = new RotatedIcon(arrowShort, thisItemRotation);
		} else if (stimuliLengths.get(memCounter) == 1) {
			//then a long arrow
			if (marker45)
				thisTrialIcon = new RotatedIcon(arrowLong45, thisItemRotation - 45);
			else
				thisTrialIcon = new RotatedIcon(arrowLong, thisItemRotation);
		}
	}
	
	/**
	 * generate the recall panel for users to input their response
	 */
	private void generateRecallPanel() {
		textPanel = new JPanel();
		textPanel.setPreferredSize(new Dimension(thisScreenSize.width,215));
		textPanel.setBackground(Color.WHITE);
		
		JLabel text = new JLabel("Click on the arrows you were presented with in sequence");
		text.setFont(textFont);
		
		textPanel.add(text);
		
		recallPanel = new JPanel();
		recallPanel.setPreferredSize(new Dimension(875,265));
		recallPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		recallPanel.setBackground(Color.WHITE);
		recallPanel.setBorder(BorderFactory.createTitledBorder("Click the arrows in the order they were presented"));
		
		
		if (trialCounter == 0) {
			long_0 = new iconPanel(arrowLong,degrees[0],100,100,false);
			long_1 = new iconPanel(arrowLong45,degrees[1]-45,100,100,true);
			long_2 = new iconPanel(arrowLong,degrees[2],100,100,false);
			long_3 = new iconPanel(arrowLong45,degrees[3]-45,100,100,true);
			long_4 = new iconPanel(arrowLong,degrees[4],100,100,false);
			long_5 = new iconPanel(arrowLong45,degrees[5]-45,100,100,true);
			long_6 = new iconPanel(arrowLong,degrees[6],100,100,false);
			long_7 = new iconPanel(arrowLong45,degrees[7]-45,100,100,true);
			
			short_0 = new iconPanel(arrowShort,degrees[0],100,100,false);
			short_1 = new iconPanel(arrowShort45,degrees[1]-45,100,100,true);
			short_2 = new iconPanel(arrowShort,degrees[2],100,100,false);
			short_3 = new iconPanel(arrowShort45,degrees[3]-45,100,100,true);
			short_4 = new iconPanel(arrowShort,degrees[4],100,100,false);
			short_5 = new iconPanel(arrowShort45,degrees[5]-45,100,100,true);
			short_6 = new iconPanel(arrowShort,degrees[6],100,100,false);
			short_7 = new iconPanel(arrowShort45,degrees[7]-45,100,100,true);				
		}

		
		iPanelsLong = new ArrayList<iconPanel>();
		iPanelsShort = new ArrayList<iconPanel>();
		
		iPanelsLong.add(long_0);iPanelsLong.add(long_1);iPanelsLong.add(long_2);
		iPanelsLong.add(long_3);iPanelsLong.add(long_4);iPanelsLong.add(long_5);
		iPanelsLong.add(long_6);iPanelsLong.add(long_7);
		
		iPanelsShort.add(short_0);iPanelsShort.add(short_1);iPanelsShort.add(short_2);
		iPanelsShort.add(short_3);iPanelsShort.add(short_4);iPanelsShort.add(short_5);
		iPanelsShort.add(short_6);iPanelsShort.add(short_7);
		
		for (iconPanel t : iPanelsLong) {
			recallPanel.add(t);
			t.addMouseListener(recallPanelHandler);
		}
		for (iconPanel t : iPanelsShort) {
			recallPanel.add(t);
			t.addMouseListener(recallPanelHandler);
		}
		
		recallReminderPanel.add(inputPromptPanel);
		
	}
	
	/**
	 * 
	 */
	public void processExecutionPhase(ExecutionContext context) {
		if (context.getPhase().equals(ExecutionPhase.SESSION_START)) {
			currentPhase = Phase.INIT;
			trialCounter = 0;
		}
	}
	
	/**
	 * 
	 */
	protected void cancelExecutionAWT() {
		timer.cancel();
		currentPhase = Phase.INIT;
    }		
	
	/**
	 * 
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
	 * 
	 */
	private void processProperties() {
		endTime = System.nanoTime();
		Timing.getEndTimeProperty().setValue(this, new Date());
		
		correctResponse = stimuliLengths;
		correctResponse.addAll(stimuliDegrees);
		
		givenResponse = givenLengths;
		givenResponse.addAll(givenDegrees);

		Question.getResponseProperty().setValue(this, givenResponse);
		Question.setQuestionAnswer(this, String.valueOf(correctResponse), correctResponse);
		boolean success = correctResponse.equals(givenResponse);
		
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
	 * 
	 */
	private void endTask() {
		currentPhase = Phase.INIT;
		trialCounter++;
		
		holderPanel.removeAll();
		recallPanel.removeAll();
		recallReminderPanel.removeAll();
		
		for (iconPanel t : iPanelsShort) {
			t.removeMouseListener(recallPanelHandler);
		}
		
		for (iconPanel t : iPanelsLong) {
			t.removeMouseListener(recallPanelHandler);
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
	 * 
	 * @param reg
	 */
	private void refreshRegion(Region reg) {
		regionsContainer.setRegionContentVisibility(reg, false);
		recallReminderPanel.revalidate();
		regionsContainer.setRegionContentVisibility(reg, true);
	}
	
	/**
	 * 
	 * @author James Stone
	 *
	 */
	private class recallPanelHandler implements MouseListener {

		public void mouseClicked(MouseEvent event) {
			iconPanel panelClicked = (iconPanel)(event.getSource());
			
			recallReminderPanel.add(panelClicked);
			
			//add the length selection of the participant to the array//
			if (iPanelsLong.contains(panelClicked)) {
				givenLengths.add(1);
			} else if (iPanelsShort.contains(panelClicked)) {
				givenLengths.add(0);
			}
			
			//add the degree selection of the participant to the array//
			givenDegrees.add(panelClicked.getDegree());
			
			respCounter++;
			
			refreshRecallPanel();
			refreshRegion(Region.SOUTH);
			
			if (respCounter >= stimuliLengths.size()) {
				regionsContainer.setRegionContentVisibility(Region.CENTER, false);
				processProperties();
				endTask();
			}
			
			
			
		}
		
		public void mousePressed(MouseEvent event){}
		
		public void mouseReleased(MouseEvent event){}
		
		public void mouseEntered(MouseEvent event){}
		
		public void mouseExited(MouseEvent event){}	
		
	}
	
	/**
	 * 
	 */
	private void refreshRecallPanel() {
		recallPanel.removeAll();
		
		Component[] tmp = recallReminderPanel.getComponents();
		ArrayList<Component> tmp2 = new ArrayList<Component>();
		
		int counter = 0;
		
		for (Component x : tmp) {
			tmp2.add(x);
		}
		
		for (iconPanel t : iPanelsLong) {
			if (tmp2.contains(t)) {
				recallPanel.add(blankPanels.get(counter));
				counter++;
			} else {
				recallPanel.add(t);
			}
		}
		
		for (iconPanel t : iPanelsShort) {
			if (tmp2.contains(t)) {
				recallPanel.add(blankPanels.get(counter));
				counter++;
			} else {
				recallPanel.add(t);
			}
		}
		
		recallPanel.revalidate();
		refreshRegion(Region.CENTER);
	}
	
	/*
	 * the actual number of iterations is not the number of trials you want to run. Each iteration means every 
	 * time this aprticularly executable should be called. In any one trial it is called more than once, it is 
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
	
	/**
	 * 
	 * @return
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
	
	//get-set methods to allow the xml to pass values to the executable//
	
	public int getComplexSpan() {
		return ComplexSpan;
	}
	
	public void setComplexSpan(int n) {
		this.ComplexSpan = n;
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