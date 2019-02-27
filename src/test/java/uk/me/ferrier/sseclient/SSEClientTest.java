package uk.me.ferrier.sseclient;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.net.URL;
import io.muserver.*;
import java.io.EOFException;

public class SSEClientTest extends TestCase {

    public SSEClientTest(String testName)
    {
        super(testName);
    }

    public static Test suite()
    {
        return new TestSuite(SSEClientTest.class);
    }

    public void testMuServer() throws Exception {
        MuServer server = MuServerBuilder.httpServer()
            .addHandler(Method.GET, "/sse/counter", (request, response, pathParams) -> {
                    SsePublisher publisher = SsePublisher.start(request, response);
                    publisher.send("1");
                    publisher.send("2");
                    publisher.send("3");
                    publisher.close();
                }).start();

        boolean thrown = false;
        URL sseUrl = new URL(server.uri() + "/sse/counter");
        try {
            SSEClient client
                = new SSEClient(sseUrl, new SSEClient.EventListener() {
                    int count = 0;
                    public void message(SSEClient.SSEEvent evt) {
                        // System.out.println("number:" + evt.data + "<");
                        String data = evt.data;
                        String number = data.trim();
                        int dataInt = Integer.parseInt(number);
                        assertTrue(dataInt == (++count));
                    }
                });
            client.connect();
        }
        catch (EOFException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }
}
