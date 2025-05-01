package org.axolotlj.RemoteHealth.util.cmd;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.axolotlj.RemoteHealth.util.cmd.CommandHandler.CommandType;

public interface CommandResponseListener {
    void onCommandResponse(ImmutablePair<CommandType, String> response);
}