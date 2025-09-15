package org.axolotlj.remotehealth.desktop.service;

import java.util.function.Consumer;

import org.axolotlj.remotehealth.core.AppContext;
import org.axolotlj.remotehealth.core.cmd.CommandCommunicator;

public class CommandManager implements CommandCommunicator {
	
	public Consumer<String> dataListener;

	@Override
	public void onDataReceived(Consumer<String> dataRecived) {
		this.dataListener = dataRecived;
	}

	@Override
	public void sendCommand(String command) {
		AppContext.getInstance().getWsManager().sendTextMessage(command);
	}

	
	public void dispatch(String json) {
	    if (dataListener != null) {
	        dataListener.accept(json);
	    }
	}

}
