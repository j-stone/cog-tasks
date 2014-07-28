package jms.cogtasks.executables;

import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Date;
import java.util.Random;

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jms.cogtasks.helperClasses.RotatedIcon;
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
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.data.DescriptivePropertyHolder;
import ch.tatool.data.Property;
import ch.tatool.data.Trial;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionOutcome;

/**
 * Displays an F,G, or R in the centre of the screen. The image has been rotated to some degree.
 * Participant must mentally rotate the image back to base and make a judgement on whether it 
 * is normal or mirror image.
 * @author James Stone
 *
 */

public class RotationProcessingTask extends BlockingAWTExecutable implements 
		ActionPanelListener, DescriptivePropertyHolder {

	Logger logger = LoggerFactory.getLogger(RotationProcessingTask.class);
	
	//panels//
	private CenteredTextPanel displayPanel;
	private KeyActionPanel actionPanel; //for displaying options and collecting responses//		
	
	//stimuli variables//
	private boolean mirror; //should the letter be a mirror image or normal//
	private int correctResponse;
	private int givenResponse;
	private long startTime;
	private long endTime;
	private Random rand;
	
	private int[] degrees = {0,45,90,135,180,225,270,315};
	
	private RotatedIcon thisTrialIcon;
	private ImageIcon tmpIcon;
	
	private String rotAppend = "_45";
	private String png = ".png";
	private String base = "/jms/cogtasks/stimuli/imgs/";	
	private URL FiconURL = getClass().getResource(base + "f" + png);
	private URL RiconURL = getClass().getResource(base + "r" + png);
	private URL GiconURL = getClass().getResource(base + "g" + png);
	private URL FiconURLm = getClass().getResource(base + "f_mirror" + png);
	private URL RiconURLm = getClass().getResource(base + "r_mirror" + png);
	private URL GiconURLm = getClass().getResource(base + "g_mirror" + png);
	private URL FiconURL45 = getClass().getResource(base + "f" + rotAppend + png);
	private URL RiconURL45 = getClass().getResource(base + "r" + rotAppend + png);
	private URL GiconURL45 = getClass().getResource(base + "g" + rotAppend + png);
	private URL FiconURLm45 = getClass().getResource(base + "f_mirror" + rotAppend + png);
	private URL RiconURLm45 = getClass().getResource(base + "r_mirror" + rotAppend + png);
	private URL GiconURLm45 = getClass().getResource(base + "g_mirror" + rotAppend + png);	
	private ImageIcon Ficon = new ImageIcon(FiconURL);
	private ImageIcon Ricon = new ImageIcon(RiconURL);
	private ImageIcon Gicon = new ImageIcon(GiconURL);
	private ImageIcon FiconM = new ImageIcon(FiconURLm);
	private ImageIcon RiconM = new ImageIcon(RiconURLm);
	private ImageIcon GiconM = new ImageIcon(GiconURLm);
	private ImageIcon Ficon45 = new ImageIcon(FiconURL45);
	private ImageIcon Ricon45 = new ImageIcon(RiconURL45);
	private ImageIcon Gicon45 = new ImageIcon(GiconURL45);
	private ImageIcon FiconM45 = new ImageIcon(FiconURLm45);
	private ImageIcon RiconM45 = new ImageIcon(RiconURLm45);
	private ImageIcon GiconM45 = new ImageIcon(GiconURLm45);
	
	private RegionsContainer regionsContainer;
	
	/**
	 * Rotation Processing Task Constructor
	 */
	public RotationProcessingTask() {
		actionPanel = new KeyActionPanel();
		actionPanel.addActionPanelListener(this);
		displayPanel = new CenteredTextPanel();
		rand = new Random();
	}	

	/**
	 * Method called whenever this executable is launched. 
	 */
	protected void startExecutionAWT() {
				
		
		ExecutionContext context = getExecutionContext();
		SwingExecutionDisplay display = ExecutionDisplayUtils.getDisplay(context);
		ContainerUtils.showRegionsContainer(display);
		regionsContainer = ContainerUtils.getRegionsContainer();
		
		generateTrial(); // construct the trial details //
		setImageToPanel(); // set the image to be displayed on the panel //
		
		actionPanel.addKey(KeyEvent.VK_LEFT, "Normal", 1);
		actionPanel.addKey(KeyEvent.VK_RIGHT, "Mirror", 0);
		
		regionsContainer.setRegionContent(Region.CENTER, displayPanel);
		regionsContainer.setRegionContent(Region.SOUTH,  actionPanel);
		
		Timing.getStartTimeProperty().setValue(this, new Date());
		startTime = System.nanoTime();
		
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, true);
		actionPanel.enableActionPanel(); 
	}
	
	/**
	 * this method sets all the variables we need for a trial, 
	 * ranodmly selects if it should be a mirrored image or not, 
	 * and then at what rotation it should be delivered. 
	 */
	private void generateTrial() {
		//mirror image or not? //
		mirror = rand.nextInt(2) == 1; // if true then a mirror image //
		if (mirror) {
			correctResponse = 0;
		} else {correctResponse = 1;}
		
		//which letter?//
		int letter = rand.nextInt(3);
		//get a random degree of rotation from the list//
		int thisTrialRotation = degrees[rand.nextInt(degrees.length)];
		
		boolean marker45;
		if (thisTrialRotation % 10 > 0)
			marker45 = true;
		else
			marker45 = false;
		
		//set thisTrialIcon to be the initial base image based on letter chosen and mirrored or not //
		if (letter == 0 & mirror) {
			tmpIcon = FiconM;
			if (marker45)
				tmpIcon = FiconM45;
		} else if (letter == 0 & !mirror) {
			tmpIcon = Ficon;
			if (marker45)
				tmpIcon = Ficon45;
		} else if (letter == 1 & mirror) {
			tmpIcon = RiconM;
			if (marker45)
				tmpIcon = RiconM45;
		} else if (letter == 1 & !mirror) {
			tmpIcon = Ricon;
			if (marker45)
				tmpIcon = Ricon45;
		} else if (letter == 2 & mirror) {
			tmpIcon = GiconM;
			if (marker45)
				tmpIcon = GiconM45;
		} else if (letter == 2 & !mirror) {
			tmpIcon = Gicon;
			if (marker45)
				tmpIcon = Gicon45;
		}
		
		//apply the rotation //
		if (marker45)
			thisTrialIcon = new RotatedIcon(tmpIcon, thisTrialRotation - 45);
		else
			thisTrialIcon = new RotatedIcon(tmpIcon, thisTrialRotation);
	}
	
	/**
	 * add the selected icon to the image panel for display
	 */
	private void setImageToPanel() {
		displayPanel.setIcon(thisTrialIcon);
	}
	
	/**
	 * method called when the action panel receives one of the inputs 
	 * it is waiting for.
	 */
	public void actionTriggered(ActionPanel source, Object actionValue) {
		actionPanel.disableActionPanel();
		
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
	 * method called after the repsonse is given, deals with logging information and moving 
	 * on.
	 */
	private void endTask() {
		regionsContainer.setRegionContentVisibility(Region.CENTER, false);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, false);
		regionsContainer.removeRegionContent(Region.CENTER);
		regionsContainer.removeRegionContent(Region.SOUTH);
		
		Question.getResponseProperty().setValue(this, givenResponse);
		Question.setQuestionAnswer(this, "mirror_judgement", correctResponse);
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
	
}
