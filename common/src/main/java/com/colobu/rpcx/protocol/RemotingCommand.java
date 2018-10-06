package com.colobu.rpcx.protocol;

import com.colobu.rpcx.config.Constants;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by goodjava@qq.com.
 */
public class RemotingCommand {

    private static final int RPC_TYPE = 0;

    private static final int RPC_ONEWAY = 1;


    private static AtomicInteger requestId = new AtomicInteger(0);

    private static SerializeType serializeTypeConfigInThisServer = SerializeType.JSON;

    private int code;
    private int version = 0;
    private int opaque = requestId.getAndIncrement();
    private int flag = 0;
    /**
     * 解码的时候会用到,不会实际传输
     */
    private transient byte[] data;


    private Message message;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
        this.setOpaque((int) this.message.getSeq());
    }

    protected RemotingCommand() {
    }

    public static RemotingCommand createRequestCommand(int code, CommandCustomHeader customHeader) {
        RemotingCommand cmd = new RemotingCommand();
        cmd.setCode(code);
        return cmd;
    }

    public static RemotingCommand createRequestCommand(int code) {
        RemotingCommand cmd = new RemotingCommand();
        cmd.setCode(code);
        return cmd;
    }


    public static RemotingCommand createResponseCommand() {
        RemotingCommand cmd = new RemotingCommand();
        cmd.markResponseType();
        return cmd;
    }


    public static RemotingCommand createResponseCommand(Message message) {
        RemotingCommand cmd = new RemotingCommand();
        cmd.markResponseType();
        cmd.setMessage(message);
        return cmd;
    }

    public void markResponseType() {
        int bits = 1 << RPC_TYPE;
        this.flag |= bits;
    }


    public static int createNewRequestId() {
        return requestId.incrementAndGet();
    }

    public static SerializeType getSerializeTypeConfigInThisServer() {
        return serializeTypeConfigInThisServer;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getOpaque() {
        return this.opaque;
    }

    public void setOpaque(int opaque) {
        this.opaque = opaque;
    }


    public boolean isOnewayRPC() {
        int bits = 1 << RPC_ONEWAY;
        return (this.flag & bits) == bits;
    }


    public RemotingCommandType getType() {
        if (this.isResponseType()) {
            return RemotingCommandType.RESPONSE_COMMAND;
        }
        return RemotingCommandType.REQUEST_COMMAND;
    }

    public boolean isResponseType() {
        int bits = 1 << RPC_TYPE;
        return (this.flag & bits) == bits;
    }

    public void markOnewayRPC() {
        int bits = 1 << RPC_ONEWAY;
        this.flag |= bits;
        if (null != message) {
            this.message.setOneway(true);
        }
    }

    public static RemotingCommand createResponseCommand(int systemError, String s) {
        RemotingCommand cmd = new RemotingCommand();
        cmd.markResponseType();
        cmd.setCode(systemError);
        return cmd;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * rpcx 是通过 meta 传递错误信息的
     * @param code
     * @param message
     */
    public void setErrorMessage(String code,String message) {
        //带有错误的返回结果
        getMessage().setMessageStatusType(MessageStatusType.Error);
        getMessage().metadata.put(Constants.RPCX_ERROR_CODE, code);
        getMessage().metadata.put(Constants.RPCX_ERROR_MESSAGE, message);
    }


}