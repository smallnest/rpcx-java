package com.colobu.rpcx.client;

import com.colobu.rpcx.protocol.*;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;

// Client is a class to connect rpcx services and invoke services sync or async.
// It can compress/decompress according to CompressType setting.
// Notice current clients does not serialize/deserialize payload so you need to handle it manually.
public class Client {
    public int callTimeoutInMills = 10000;
    private Socket socket;
    private ExecutorService executor;
    private Map<Long, CompletableFuture<Message>> futures = new ConcurrentHashMap<>();
    // callback for handling responses which seq is missing in futures. Those messages may be requests from servers.
    private Callable missingSeqCallbacks;


    public Client() {
        executor = Executors.newCachedThreadPool();
    }

    public Client(ExecutorService executor) {
        this.executor = executor;
    }

    // recommend you handle messages in callback in another thread pool to avoid blocking receiving messages from server.
    public void setCallback(Callable callback) {
        missingSeqCallbacks = callback;
    }

    // connect the server.
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

    // call services asynchronously.
    // Please invoke cancelFuture if you cancel this future by `f.cancel(bool)` manually, otherwise there may be memory leak.
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

    public void cancelFuture(double seq,CompletableFuture<Message> f) {
        if (!f.isCancelled() && !f.isDone() && !f.isCompletedExceptionally()) {
            f.cancel(true);
        }

        futures.remove(seq);
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
                } else if (missingSeqCallbacks != null){
                    missingSeqCallbacks.call(res);
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

    // call services synchronously.
    public Message call(Message req) throws Exception {
        CompletableFuture<Message> f = asyncCall(req);
        return f.get(callTimeoutInMills, TimeUnit.MILLISECONDS);
    }


}


