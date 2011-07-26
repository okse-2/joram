package jmx.remote.jms;
/**
 * Interface MBean of The MBean A
 * @author Djamel-Eddine Boumchedda
 *
 */
public interface AMBean {
	public int geta();
	public void seta(int newval);
	public int getb();
	
	public void affiche();
	public int addValeurs(int a,int b);

}
