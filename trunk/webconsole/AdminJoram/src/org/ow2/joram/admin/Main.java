package org.ow2.joram.admin;

public class Main {

  public static void main(String[] args) {
    JoramAdmin admin = new JoramAdminImpl();
    admin.connect("root", "root");
    admin.start(new SysoListener());
  }

}
