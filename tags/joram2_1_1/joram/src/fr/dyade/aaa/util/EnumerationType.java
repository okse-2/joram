package fr.dyade.aaa.util;

/**
 *
 * Enumerations should implement this interface.
 * A code example is given below.
 * <code>
 * <br>
 * package fr.dyade.aaa.nw.ar.tests; <br>
 *  <br>
 * import fr.dyade.aaa.util.EnumerationType; <br>
 *  <br>
 * public class CnxState implements EnumerationType { <br>
 *  <br>
 *   public static CnxState valueOf(String name) throws Exception { <br>
 *     for (int i = 0; i < names.length; i++) { <br>
 *       if (name.equals(names[i])) <br>
 *         return values[i]; <br>
 *     } <br>
 *     throw new Exception("Format exception: " + name + <br>
 *                         " is not a CnxState."); <br>
 *   } <br>
 *  <br>
 *   public final static String[] names = { <br>
 *     "no value", <br>
 *     "opened", <br>
 *     "established", <br>
 *     "reset",	<br>
 *     "closed", <br>
 *     "free", <br>
 *     "timeout", <br>
 *     "unchanged" <br>
 *   }; <br>
 *  <br>
 *  <br>
 *   public final static int _CNX_NOVALUE = 0; <br>
 *    <br>
 *   public final static int _CNX_OPENED = 1; <br>
 *    <br>
 *   public final static int _CNX_ESTABLISHED = 2; <br>
 *    <br>
 *   public final static int _CNX_RESET = 3; <br>
 *    <br>
 *   public final static int _CNX_CLOSED = 4; <br>
 *    <br>
 *   public final static int _CNX_FREE = 5; <br>
 *    <br>
 *   public final static int _CNX_TIMEOUT = 6; <br>
 *    <br>
 *   public final static int _CNX_UNCHANGED = 7; <br>
 *  <br>
 *  <br>
 *   public final static CnxState CNX_NOVALUE = new CnxState(_CNX_NOVALUE); <br>
 *    <br>
 *   public final static CnxState CNX_OPENED = new CnxState(_CNX_OPENED); <br>
 *    <br>
 *   public final static CnxState CNX_ESTABLISHED = new CnxState(_CNX_ESTABLISHED); <br>
 *    <br>
 *   public final static CnxState CNX_RESET = new CnxState(_CNX_RESET); <br>
 *    <br>
 *   public final static CnxState CNX_CLOSED = new CnxState(_CNX_CLOSED); <br>
 *    <br>
 *   public final static CnxState CNX_FREE = new CnxState(_CNX_FREE); <br>
 *    <br>
 *   public final static CnxState CNX_TIMEOUT = new CnxState(_CNX_TIMEOUT); <br>
 *    <br>
 *   public final static CnxState CNX_UNCHANGED = new CnxState(_CNX_UNCHANGED); <br>
 *  <br>
 *   public final static CnxState[] values = {CNX_NOVALUE, <br>
 *                                            CNX_OPENED, <br>
 *                                            CNX_ESTABLISHED, <br>
 *                                            CNX_RESET, <br>
 *                                            CNX_CLOSED, <br>
 *                                            CNX_FREE, <br>
 *                                            CNX_TIMEOUT, <br>
 *                                            CNX_UNCHANGED}; <br>
 *  <br>
 *   private int index; <br>
 *  <br>
 *   private CnxState(int index) {  <br>
 *     this.index = index; <br>
 *   } <br>
 *  <br>
 *   public int intValue() { <br>
 *     return index; <br>
 *   } <br>
 *  <br>
 *   public String toString() { <br>
 *     return names[index]; <br>
 *   } <br>
 *  <br>
 *  <br>
 * } <br>
 * <br>
 * </code>
 */

public interface EnumerationType extends java.io.Serializable {
  public int intValue();
}
