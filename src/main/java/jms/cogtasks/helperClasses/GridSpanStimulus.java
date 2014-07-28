package jms.cogtasks.helperClasses;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * class for creating a panel that will be used to display stimuli in 
 * symmetry span and mastrix span tasks.
 * 
 * @author James Stone
 */

public class GridSpanStimulus extends JPanel {
	
	private ArrayList<JButton> buttons = new ArrayList<JButton>();
	
	/*constructor*/
	public GridSpanStimulus(int num_boxes, int panel_size) {
		super();
		setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		//set size of panel//
		this.setPreferredSize(new Dimension(panel_size + num_boxes, panel_size + num_boxes));
		this.setMaximumSize(new Dimension(panel_size + num_boxes, panel_size + num_boxes));
		this.setBackground(Color.WHITE);
		
		//initialise the JButtons we need and add them to an arraylist//
		for (int i = 0; i < (num_boxes * num_boxes); i++) {
			buttons.add(new JButton());
		}

		
		//construct and set values for buttons//
		
		for (JButton item : buttons) {
			item.setPreferredSize(new Dimension(panel_size / num_boxes, panel_size / num_boxes));
			item.setBackground(Color.WHITE);
			this.add(item);
		}
		
		enableAllButtons(false);
		
	}
	
	public void enableAllButtons(boolean b) {
		for (JButton item : buttons) {
			item.setEnabled(b);
		}
	}
	
	public void enableSingleButton(boolean b, int buttonToAffect) {
		buttons.get(buttonToAffect).setEnabled(b);
	}
	
	public void fillButton(int f, Color c) {
		buttons.get(f).setBackground(c);
	}
	
	public void fillListButtons(int[] list, Color c){
		for (int j : list) {
			buttons.get(j).setBackground(c);
		}
	}
	
	public void fillListButtons(ArrayList<Integer> list, Color c){
		for (int j : list) {
			buttons.get(j).setBackground(c);
		}
	}
	
	public ArrayList<Integer> getPattern(boolean symmetry, int n) {
		Random rand;
		rand = new Random();
		
		ArrayList<Integer> returnList = new ArrayList<Integer>();;
		
		int[] half_one = {1,2,3,4,9,10,11,12,17,18,19,20,25,26,27,28,33,34,35,36,41,42,43,44,49,50,51,52,57,58,59,60};
		int[] half_two_symm_counterparts = {8,7,6,5,16,15,14,13,24,23,22,21,32,31,30,29,40,39,38,37,48,47,46,45,56,55,54,53,64,63,62,61};
		ArrayList<Integer> halfOneAL = new ArrayList<Integer>();
		ArrayList<Integer> halfTwoAL = new ArrayList<Integer>();
		for (int i : half_one) {
			halfOneAL.add(i - 1);
		}
		for (int i : half_two_symm_counterparts) {
			halfTwoAL.add(i - 1);
		}
		
		
		//if we want a symmetrical pattern then take random numbers from half_one and SAME indice numbers from half_two//
		int[] indicesToFill = new int[n];;
		ArrayList<Integer> used = new ArrayList<Integer>();
		
		for (int i = 0; i < n; i++) {
			int tmp = rand.nextInt(half_one.length);
			if (!used.contains(tmp)) {
				indicesToFill[i] = tmp;
				used.add(tmp);
			} else {
				i--; //number already used so need to go again.
			}			
		}

		ArrayList<Integer> values_half_one = new ArrayList<Integer>();
		ArrayList<Integer> values_half_two = new ArrayList<Integer>();
		
		for (int i : indicesToFill) {
			values_half_one.add(halfOneAL.get(i));
		}

		if (symmetry) {
			//get the exact counterparts for half two
			for (int i : indicesToFill) {
				values_half_two.add(halfTwoAL.get(i));
			}
		} else {
			// not symmetrical //
			// so need half_two to be different to the exact counterparts //
			// just get a a new rando set of indices but with a check to ensure that we do not stumble on 
			// a symmetrical pattern by accident
			int[] indicesToFill_two = new int[n];
			ArrayList<Integer> used_two = new ArrayList<Integer>();
			for (int i = 0; i < n; i++) {
				int tmp = rand.nextInt(half_two_symm_counterparts.length);
				if (!used_two.contains(tmp)) {
					indicesToFill_two[i] = tmp;
					used_two.add(tmp);
				} else {
					i--; //number already used so need to go again.
				}			
			}
			
			for (int i : indicesToFill_two) {
				values_half_two.add(halfTwoAL.get(i));
			}			
		}
		
		returnList.addAll(values_half_one);
		returnList.addAll(values_half_two);
		
		return returnList;
	}

	
}
