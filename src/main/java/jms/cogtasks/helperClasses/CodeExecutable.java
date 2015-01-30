package jms.cogtasks.helperClasses;

import ch.tatool.core.display.swing.ExecutionDisplayUtils;
import ch.tatool.core.display.swing.SwingExecutionDisplay;
import ch.tatool.core.display.swing.action.ActionPanel;
import ch.tatool.core.display.swing.action.ActionPanelListener;
import ch.tatool.core.display.swing.action.InputActionPanel;
import ch.tatool.core.display.swing.container.ContainerUtils;
import ch.tatool.core.display.swing.container.RegionsContainer;
import ch.tatool.core.display.swing.container.RegionsContainer.Region;
import ch.tatool.core.display.swing.panel.TextAreaPanel;
import ch.tatool.core.executable.BlockingAWTExecutable;
import ch.tatool.exec.ExecutionContext;
import ch.tatool.exec.ExecutionPhaseListener;

/**
 * 
 * @author Andre Locher
 */
public class CodeExecutable extends BlockingAWTExecutable implements
ActionPanelListener, ExecutionPhaseListener {

	private TextAreaPanel displayPanel;
	private SwingExecutionDisplay display;
	private InputActionPanel actionPanel;
	private ExecutionContext context;
	private RegionsContainer regionsContainer;

	@Override
	protected void startExecutionAWT() {
		context = getExecutionContext();
		display = ExecutionDisplayUtils.getDisplay(context);
		regionsContainer = ContainerUtils.getRegionsContainer();
		ContainerUtils.showRegionsContainer(display);
		regionsContainer.removeAllContent();
		regionsContainer.setRegionVisibility(Region.NORTH, false);
		regionsContainer.setRegionContentVisibility(Region.CENTER, false);
		
		displayPanel = new TextAreaPanel();
		displayPanel.setText("Participant Code:");
		
		actionPanel = new InputActionPanel();
		actionPanel.setTextDocument(5, InputActionPanel.FORMAT_ONLY_DIGITS);
		actionPanel.addActionPanelListener(this);

		regionsContainer.setRegionContent(Region.CENTER, displayPanel);
		regionsContainer.setRegionContent(Region.SOUTH, actionPanel);
		regionsContainer.setRegionContentVisibility(Region.CENTER, true);
		regionsContainer.setRegionContentVisibility(Region.SOUTH, true);
		actionPanel.enableActionPanel();
	}

	public void actionTriggered(ActionPanel source, Object actionValue) {
		String code = (String) actionValue;
		actionPanel.disableActionPanel();
		if (code.length() != 5) {
			displayPanel.setText("Enter a unique 5-digit code for the current session\n\n Please try again.");
			actionPanel.clearTextField();
			actionPanel.enableActionPanel(); 
		} else {
			context.getExecutionData().getModule().getModuleProperties().put("subject-code", code);
			context.getExecutionData().getModuleSession().putValue("subject", "subject-code", code);
			
			if (getFinishExecutionLock()) {
				finishExecution();
			}
			
		}
	}

	public void processExecutionPhase(ExecutionContext event) {
		// TODO Auto-generated method stub
		
	}
}
