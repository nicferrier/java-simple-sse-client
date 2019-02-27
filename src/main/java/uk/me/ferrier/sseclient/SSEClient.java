/*
    Simple SSE Client - an EventSource implementation for Java
    Copyright (C) 2019  Nic Ferrier

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package uk.me.ferrier.sseclient;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.EOFException;
import java.io.IOException;

// test
import java.io.ByteArrayInputStream;

public class SSEClient {

    public static class SSEEvent {
        public String data;
        public String type;
    }

    public static interface EventListener {
        public void message(SSEEvent event);
    }

    URL target;
    EventListener listener;
    
    public SSEClient(URL destination, EventListener listener) {
        target = destination;
        this.listener = listener;
    }

    boolean isEol(StringBuilder sb) {
        // there is a condition missing from this...
        //   sb.length() -2 == 13 and sb.length() -1 != 10
        // pretty difficult to do that byte by byte
        return ((sb.length() > 0)
                &&
                (
                 ((sb.length() < 2) && sb.codePointAt(0) == 10)
                 ||
                 (sb.codePointAt(sb.length() - 1) == 10
                  && sb.codePointAt(sb.length() - 2) == 13)
                 ||
                 (sb.codePointAt(sb.length() - 1) == 10)
                 )
                );
    }

    void readEvent(InputStream in) throws IOException {
        String eventType = "message"; // default according to spec
        StringBuilder payLoad = new StringBuilder();
        boolean readEvent = true;
        while(readEvent) {
            StringBuilder sb = new StringBuilder();
            while (!isEol(sb)) {
                int red = in.read();
                if (red == -1) throw new EOFException("end of stream");
                char c = (char)red;
                // System.out.println("readEvent red = " + red);
                sb.append(c);
            }

            String line = sb.toString();
            if (line.startsWith(":")) {
                continue;
            }
            if (line.startsWith("data: ")) {
                payLoad.append(line.substring(6));
                continue;
            }
            if (line.startsWith("event: ")) {
                eventType = line.substring(7);
                continue;
            }
            if ("\n".equals(line) || "\r\n".equals(line)) {
                break;
            }
            // System.out.println("line = " + line + "<");
        }
        // System.out.println("event? " + event.toString());
        SSEEvent evt = new SSEEvent();
        evt.type = eventType;
        evt.data = payLoad.toString();
        listener.message(evt);
    }

    public void connect() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) target.openConnection();
        InputStream in = connection.getInputStream();
        while (true) {
            readEvent(in);
        }
    }
}
