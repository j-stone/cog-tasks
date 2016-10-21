package jms.cogtasks.app;

import ch.tatool.app.App;
import javax.swing.UIManager;

/**
 * Entry point for the application, launches the tatool main 
 * application, then custom modules/executables are used.
 * 
 * @author James Stone
 *
 */

public class startApp {

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			System.out.println("Failed to set look and feel");
		}
		
		App.main(null);

	}

}
