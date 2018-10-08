package com.colobu.rpcx.common;

import com.colobu.rpcx.protocol.Message;
import com.colobu.rpcx.protocol.RemotingCommand;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class TestPoolFactory extends BasePooledObjectFactory<RemotingCommand> {

    @Override
    public RemotingCommand create() {
        return RemotingCommand.createRequestCommand(new Message());
    }

    @Override
    public PooledObject<RemotingCommand> wrap(RemotingCommand remotingCommand) {
        return new DefaultPooledObject<>(remotingCommand);
    }
}
