import java.io.*;
import java.net.*;

import org.objectweb.joram.shared.stream.*;
import org.objectweb.joram.shared.client.*;

public class Proxy {
  public static void main(String args[]) throws Exception {
    int counter = 1;
    ServerSocket server = new ServerSocket(16010);

    while (true) {
      try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        System.out.println("wait");
        Socket socket = server.accept();
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();
        System.out.println("connected");
        int length = StreamUtil.readIntFrom(is);
        System.out.println("reads: " + length);
        String user = StreamUtil.readStringFrom(is);
        String pass = StreamUtil.readStringFrom(is);
        int key = StreamUtil.readIntFrom(is);
        int timeout = StreamUtil.readIntFrom(is);

        System.out.println(user + ", " + pass + ", " + key + ", " + timeout);

        StreamUtil.writeTo(8, os);
        StreamUtil.writeTo(0, os);
        StreamUtil.writeTo(counter++, os);

        int s = StreamUtil.readIntFrom(is);
//         int c = StreamUtil.readIntFrom(is);
//         int r = StreamUtil.readIntFrom(is);
//         String str = StreamUtil.readStringFrom(is);
//         System.out.println("receive: " + s + ", " + c + ", " + r + ", \"" + str + "\"");
        CnxConnectRequest req = (CnxConnectRequest) AbstractJmsMessage.read(is);
        System.out.println("receive: " + req);
        
        CnxConnectReply reply = new CnxConnectReply(req, 12, "#0.0.12");
        reply.write(baos);
        System.out.println("sends: " + baos.size());
        StreamUtil.writeTo(baos.size(), os);
        baos.writeTo(os);
        Thread.sleep(30000L);
        socket.close();
      } catch (IOException exc) {
        exc.printStackTrace();
      }
    }
  }
}
