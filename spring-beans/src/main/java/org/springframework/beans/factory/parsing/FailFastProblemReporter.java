
package org.springframework.beans.factory.parsing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class FailFastProblemReporter implements ProblemReporter {

	private Log logger = LogFactory.getLog(getClass());
	public void setLogger(Log logger) {
		this.logger = (logger != null ? logger : LogFactory.getLog(getClass()));
	}

	public void fatal(Problem problem) {
		throw new BeanDefinitionParsingException(problem);
	}

	public void error(Problem problem) {
		throw new BeanDefinitionParsingException(problem);
	}

	public void warning(Problem problem) {
		this.logger.warn(problem, problem.getRootCause());
	}

}
