
package org.springframework.context;

public interface Lifecycle {

	// Start this component.
	void start();

	// Stop this component
	void stop();

	// Check whether this component is currently running.
	boolean isRunning();

}
