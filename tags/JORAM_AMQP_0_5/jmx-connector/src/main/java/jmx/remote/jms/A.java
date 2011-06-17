package jmx.remote.jms;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;


public class A implements AMBean {
    private static final JMXServiceURL MBeanServerConnection = null;
	int a,b;
	public int geta(){
		return a;
	}
	public void seta(int newval){
		a = newval;
	}
	public int getb(){
		return b;
	}
	
	public void affiche(){
		System.out.println("Class A");
		System.out.println("valeur de a = "+a);
		System.out.println("valeur de b = "+b);
		
	}
	
	public int addValeurs(int a,int b){
		int res = a + b;
		return res;
	}
	
	
	
	public void main(String args[]){
		a = 2;
		b = 3;
		A objetA = new A();
		objetA.affiche();
		objetA.geta();
		
	}

}
