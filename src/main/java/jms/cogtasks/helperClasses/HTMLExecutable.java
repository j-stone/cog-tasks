/**
 * 
 */
package jms.cogtasks.helperClasses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTML Instruction
 * 
 * Displays a HTML page
 * 
 * @author Andre Locher
 */
public class HTMLExecutable extends AbstractHTMLExecutable {

	Logger logger = LoggerFactory.getLogger(HTMLExecutable.class);

	public String replaceVariables(String html) {
		return html;
	}

}
