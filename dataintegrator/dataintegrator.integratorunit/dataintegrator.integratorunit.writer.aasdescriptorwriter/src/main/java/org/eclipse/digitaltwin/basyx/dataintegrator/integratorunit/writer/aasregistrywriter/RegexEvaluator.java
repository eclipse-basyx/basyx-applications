package org.eclipse.digitaltwin.basyx.dataintegrator.integratorunit.writer.aasregistrywriter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegexEvaluator {

	private Logger logger = LoggerFactory.getLogger(RegexEvaluator.class);

	private String regexPattern;
	private String string;

	public RegexEvaluator(String regexPattern, String string) {
		super();
		this.regexPattern = regexPattern;
		this.string = string;
	}

	public String getRegexPattern() {
		return regexPattern;
	}

	public String getString() {
		return string;
	}

	public String evaluate() {
		Pattern pattern = Pattern.compile(regexPattern);
		Matcher matcher = pattern.matcher(string);

		if (!matcher.matches())
			logger.error("Couldn't evaluate the provided expression : {} on the provided value : {}", regexPattern,
					string);
		
		return matcher.group(2);
	}

}
