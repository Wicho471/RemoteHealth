package org.axolotlj.remotehealth.core.util.cmd;

import org.apache.commons.lang3.tuple.ImmutablePair;

public interface CommandResponseListener {
    void onCommandResponse(ImmutablePair<CommandType, String> response);
}