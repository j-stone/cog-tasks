package jms.cogtasks.executables;

import ch.tatool.core.data.*;
import ch.tatool.core.display.swing.status.StatusPanel;
import ch.tatool.core.display.swing.status.StatusRegionUtil;
import ch.tatool.data.Trial;
import ch.tatool.exec.ExecutionOutcome;

public class BackwardDigitSpan extends WordDigitSpan{
 	public void processProperties() {
        int stimulus_num = numbers[numbers.length - (respCounter+1)];
        correctResponseDigits = Integer.valueOf(stimulus_num);
        Question.getResponseProperty().setValue(this, givenResponseDigits);
        Question.setQuestionAnswer(this, String.valueOf(stimulus_num), correctResponseDigits);


		Boolean success = correctResponseDigits == givenResponseDigits;
        loadProperty.setValue(this, numbers.length);

		Points.setZeroOneMinMaxPoints(this);
		Points.setZeroOnePoints(this, success);
		Result.getResultProperty().setValue(this, success);

        if (respCounter < (numbers.length - 1)) {
            Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.SUSPENDED);
        } else {
            Misc.getOutcomeProperty().setValue(this, ExecutionOutcome.FINISHED);
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

}
