package com.colobu.rpcx.client;

import com.colobu.rpcx.protocol.*;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;

public class Client {
    public int callTimeoutInMills = 10000;
    private Socket socket;
    private ExecutorService executor;
    private Map<Double, CompletableFuture<Message>> futures = new ConcurrentHashMap<>();

    public Client() {
        executor = Executors.newCachedThreadPool();
    }

    public Client(ExecutorService executor) {
        this.executor = executor;
    }

    public void connect(String serverAddress, int serverPort) throws IOException {
        socket = new Socket(serverAddress, serverPort);
        new Thread(() -> {
            try {
                readResponses();
            } catch (Exception e) {
                //
            }
        }).start();
    }

    public CompletableFuture<Message> asyncCall(Message req) throws Exception {

        CompletableFuture<Message> f = new CompletableFuture<>();
        futures.put(req.getSeq(),f);

        executor.submit(() -> {
            try {
                byte[] data = req.encode();
                //System.out.println(Arrays.toString(data));
                socket.getOutputStream().write(data);
            } catch (IOException e) {
                f.completeExceptionally(e);
                try {
                    socket.close();
                } catch (IOException e1) {

                }
                futures.remove(req.getSeq());
                clearWaitingFutures(e);

            } catch (Exception e) {
                f.completeExceptionally(e);
                futures.remove(req.getSeq());
                clearWaitingFutures(e);
            }
        });


        return f;
    }

    private void clearWaitingFutures(Exception e) {
        futures.forEach((k,v) -> {
            v.completeExceptionally(e);
        });

        futures = new ConcurrentHashMap<>();
    }

    private void readResponses() throws Exception {
        InputStream is =  new BufferedInputStream(socket.getInputStream());

        for (;;) {
            Message res = new Message();
            double seq = 0;
            try {
                res.decode(is);

                seq = res.getSeq();
                CompletableFuture<Message> f = futures.get(seq);
                if (f != null) {
                    f.complete(res);
                } else {
                    //dropped
                }
            } catch (Exception e) {
                try {
                    socket.close();
                } catch (IOException e1) {

                }
                futures.remove(seq);
                clearWaitingFutures(e);
                throw  e;
            }

            futures.remove(seq);
        }
    }

    public Message call(Message req) throws Exception {
        CompletableFuture<Message> f = asyncCall(req);
        return f.get(callTimeoutInMills, TimeUnit.MILLISECONDS);
    }


}


