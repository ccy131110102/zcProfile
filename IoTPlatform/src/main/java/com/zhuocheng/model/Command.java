package com.zhuocheng.model;

import java.util.Date;

public class Command {
	private int commandId;
	private String commandType;
	private String commandContent;
	private int commandState;
	private String commandDeviceId;
	private String commandAppId;
	private String commandProfileId;
	private String commandServiceId;
	private String commandServiceType;
	private String commandDirection;
	private Date commandTimestamp;

	public int getCommandId() {
		return commandId;
	}

	public void setCommandId(int commandId) {
		this.commandId = commandId;
	}

	public String getCommandType() {
		return commandType;
	}

	public void setCommandType(String commandType) {
		this.commandType = commandType;
	}

	public String getCommandContent() {
		return commandContent;
	}

	public void setCommandContent(String commandContent) {
		this.commandContent = commandContent;
	}

	public int getCommandState() {
		return commandState;
	}

	public void setCommandState(int commandState) {
		this.commandState = commandState;
	}

	public String getCommandDeviceId() {
		return commandDeviceId;
	}

	public void setCommandDeviceId(String commandDeviceId) {
		this.commandDeviceId = commandDeviceId;
	}

	public String getCommandDirection() {
		return commandDirection;
	}

	public void setCommandDirection(String commandDirection) {
		this.commandDirection = commandDirection;
	}

	public Date getCommandTimestamp() {
		return commandTimestamp;
	}

	public void setCommandTimestamp(Date commandTimestamp) {
		this.commandTimestamp = commandTimestamp;
	}

	public String getCommandAppId() {
		return commandAppId;
	}

	public void setCommandAppId(String commandAppId) {
		this.commandAppId = commandAppId;
	}

	public String getCommandProfileId() {
		return commandProfileId;
	}

	public void setCommandProfileId(String commandProfileId) {
		this.commandProfileId = commandProfileId;
	}

	public String getCommandServiceId() {
		return commandServiceId;
	}

	public void setCommandServiceId(String commandServiceId) {
		this.commandServiceId = commandServiceId;
	}

	public String getCommandServiceType() {
		return commandServiceType;
	}

	public void setCommandServiceType(String commandServiceType) {
		this.commandServiceType = commandServiceType;
	}
}
