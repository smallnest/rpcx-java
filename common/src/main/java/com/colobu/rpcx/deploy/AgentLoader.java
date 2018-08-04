package com.colobu.rpcx.deploy;

import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;

public class AgentLoader {


    public void loadAgent(String processId, String jarFileName, String params) {
        VirtualMachine virtualMachine = null;
        try {
            virtualMachine = VirtualMachine.attach(processId);
            virtualMachine.loadAgent(jarFileName, params);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                virtualMachine.detach();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
