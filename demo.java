import io.muserver.*;
import uk.co.ferrier.sseclient.SSEClient;

public class SSEClientDemo {
    public void testMuServer() throws Exception {
        MuServer server = MuServerBuilder.httpServer()
            .addHandler(Method.GET, "/sse/counter", (request, response, pathParams) -> {
                    SsePublisher publisher = SsePublisher.start(request, response);
                    publisher.send("1");
                    publisher.send("2");
                    publisher.send("3");
                    publisher.close();
                }).start();
        
        URL sseUrl = new URL(server.uri() + "/sse/counter");
        try {
            SSEClient client
                = new SSEClient(sseUrl, new SSEClient.EventListener() {
                        int count = 0;
                        public void message(SSEClient.SSEEvent evt) {
                            String data = evt.data;
                            String number = data.trim();
                            int dataInt = Integer.parseInt(number);
                            System.out.println("arrived:" + dataInt);
                        }
                    });
            client.connect();
        }
        catch (EOFException e) {
            System.out.println("the stream ended!");
        }
    }
}
