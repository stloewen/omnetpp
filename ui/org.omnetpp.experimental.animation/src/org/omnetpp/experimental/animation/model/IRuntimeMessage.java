package org.omnetpp.experimental.animation.model;

public interface IRuntimeMessage {

	/**
	 * Return true if message was posted by scheduleAt().
	 */
	public boolean isSelfMessage();

	/**
	 * Returns sender module's index in the module vector or -1 if the
	 * message hasn't been sent/scheduled yet.
	 */
	public int getSenderModuleId();

	/**
	 * Returns index of gate sent through in the sender module or -1
	 * if the message hasn't been sent/scheduled yet.
	 */
	public int getSenderGateId();

	/**
	 * Returns receiver module's index in the module vector or -1 if
	 * the message hasn't been sent/scheduled yet.
	 */
	public int getArrivalModuleId();

	/**
	 * Returns index of gate the message arrived on in the sender module
	 * or -1 if the message hasn't sent/scheduled yet.
	 */
	public int getArrivalGateId();

	/**
	 * Returns time when the message was sent/scheduled or 0 if the message
	 * hasn't been sent yet.
	 */
	public double getSendingTime();

	/**
	 * Returns time when the message arrived (or will arrive if it
	 * is currently scheduled or is underway), or 0 if the message
	 * hasn't been sent/scheduled yet.
	 */
	public double getArrivalTime();
}
