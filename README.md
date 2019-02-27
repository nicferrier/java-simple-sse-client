# Java SSEClient

I needed a Java SSE client for doing some tests... 

It doesn't have to be that performant, I'm not going to use it to do
anything real, goodness knows, it's Java.

So here is a very simple, synchronous only SSE client.

There's a good example test, using [mu-server](http://muserver.io/sse)
but here's an example:

```java
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
    }
}
```
