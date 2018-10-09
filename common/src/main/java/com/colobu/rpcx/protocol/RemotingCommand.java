package com.colobu.rpcx.protocol;

import com.colobu.rpcx.config.Constants;

/**
 * Created by goodjava@qq.com.
 */
public class RemotingCommand {


    private int code = 0;
    private int version = 0;


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

    public RemotingCommand() {
    }

    public RemotingCommand(Message message, byte[] body) {
        this.message = message;
        this.data = body;
    }


    public static RemotingCommand createRequestCommand(Message message) {
        RemotingCommand cmd = new RemotingCommand();
        cmd.setMessage(message);
        return cmd;
    }


    public static RemotingCommand createResponseCommand(Message message) {
        RemotingCommand cmd = new RemotingCommand();
        cmd.setMessage(message);
        return cmd;
    }



    public void markResponseType() {
        this.message.setMessageType(MessageType.Response);
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
        return (int) this.message.getSeq();
    }

    public void setOpaque(int opaque) {
        this.message.setSeq(opaque);
    }


    public boolean isOnewayRPC() {
        return this.message.isOneway();
    }


    public RemotingCommandType getType() {
        if (this.isResponseType()) {
            return RemotingCommandType.RESPONSE_COMMAND;
        }
        return RemotingCommandType.REQUEST_COMMAND;
    }


    public boolean isResponseType() {
        return this.message.getMessageType().equals(MessageType.Response);
    }

    public void markOnewayRPC() {
        this.message.setOneway(true);
    }

    public static RemotingCommand createResponseCommand(int errorCode, String errorMessage) {
        RemotingCommand cmd = new RemotingCommand();
        cmd.markResponseType();
        cmd.getMessage().setMessageStatusType(MessageStatusType.Error);
        cmd.getMessage().metadata.put(Constants.RPCX_ERROR_CODE, String.valueOf(errorCode));
        cmd.getMessage().metadata.put(Constants.RPCX_ERROR_MESSAGE, errorMessage);
        return cmd;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


    public void setErrorMessage(int code, String message) {
        this.setErrorMessage(String.valueOf(code), message);
    }


    /**
     * rpcx 是通过 meta 传递错误信息的
     *
     * @param code
     * @param message
     */
    public void setErrorMessage(String code, String message) {
        //带有错误的返回结果
        this.message.setMessageStatusType(MessageStatusType.Error);
        this.message.metadata.put(Constants.RPCX_ERROR_CODE, code);
        this.message.metadata.put(Constants.RPCX_ERROR_MESSAGE, message);
    }

    public RemotingCommand requestToResponse() {
        this.message.setMessageType(MessageType.Response);
        this.data = new byte[]{};
        this.message.payload = new byte[]{};
        this.message.metadata.clear();
        return this;
    }

}