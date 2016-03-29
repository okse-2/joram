package joram.rest;

import java.net.URI;
import java.net.URLConnection;
import java.util.Set;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;

public class Helper {

  public static URI getBaseJmsURI() {  
    return UriBuilder.fromUri("http://localhost:8989/joram/").build();  
  }
  
  public static URI getBaseJmsURI(int port) {  
    return UriBuilder.fromUri("http://localhost:"+port+"/joram/").build();  
  }
  
  public static URI getBaseAdminURI() {  
    return UriBuilder.fromUri("http://localhost:8989/joram/admin").build();  
  }
  
  public static URI getBaseAdminURI(int port) {  
    return UriBuilder.fromUri("http://localhost:"+port+"/joram/admin").build();  
  }
  
  public static void print(Set<Link> links) {
    System.out.println("  link :");
    for (Link link : links)
      System.out.println("\t" + link.getRel() + " : " + link.getUri());
  }
  
  public static boolean isConnected(URI uri) {
    try {
      URLConnection connection = uri.toURL().openConnection();
      connection.getInputStream();
      return true;
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
    return false;
  }
  
  public static void waitConnection(URI uri, int timeOut) throws Exception {
    long start = System.currentTimeMillis();
    while (true) {
      try {
        URLConnection connection = uri.toURL().openConnection();
        connection.getInputStream();
        return;
      } catch (Exception e) {
        if ((timeOut * 1000) < (System.currentTimeMillis() - start))
          throw new Exception("Connection impossible in " + timeOut + " second");
        Thread.sleep(100);
      }
    }
  }
}
