package joram.carrieriq;

import joram.carrieriq.ObjectListener;

public class ExamplePojo implements ObjectListener {
  public void onObject(Object o) {
    System.out.println("Received msg: " + o);
  }
	
  public void init() {
    System.out.println("init ExamplePojo");
  }
	
  public void cleanup() {
    System.out.println("cleanup ExamplePojo");
  }
}
