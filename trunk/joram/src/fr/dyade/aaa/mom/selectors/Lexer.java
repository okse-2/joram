/*
 * Copyright (C) 1996 - 2001 BULL
 * Copyright (C) 1996 - 2001 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, fr.dyade.aaa.jndi 
 * and fr.dyade.aaa.joram, released October, 2001. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
/* The following code was generated by JFlex 1.3.2 on 07/09/01 11:06 */

/*
  Subset of SQL 92 syntax for JMS selectors.
  Author: Frederic Maistre, INRIA, July 2001.
*/

package fr.dyade.aaa.mom.selectors;

/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.3.2
 * on 07/09/01 11:06 from the specification file
 * <tt>file:/C:/WORK/JORAM/joramSelec/JMS/selec.flex</tt>
 */
public class Lexer implements Scanner {

  /** This character denotes the end of file */
  final public static int YYEOF = -1;

  /** initial size of the lookahead buffer */
  final private static int YY_BUFFERSIZE = 16384;

  /** lexical states */
  final public static int YYINITIAL = 0;

  /** 
   * Translates characters to character classes
   */
  final private static char [] yycmap = {
     0,  0,  0,  0,  0,  0,  0,  0,  0, 19,  2,  0,  3,  1,  0,  0, 
     0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 
    38, 18,  0, 18, 16, 18, 18, 17, 23, 24, 21, 20, 28,  7,  8, 22, 
     4,  5,  5,  5,  5,  5,  5,  5,  5,  5, 18, 18, 26, 25, 27, 18, 
    18, 13, 36, 34, 30,  6, 12, 16, 16, 32, 16, 33, 14, 16, 29, 31, 
    35, 16, 10, 15,  9, 11, 16, 37, 16, 16, 16, 18, 18, 18,  0, 16, 
     0, 13, 36, 34, 30,  6, 12, 16, 16, 32, 16, 33, 14, 16, 29, 31, 
    35, 16, 10, 15,  9, 11, 16, 37, 16, 16, 16, 18,  0, 18, 18,  0
  };

  /** 
   * Translates a state to a row index in the transition table
   */
  final private static int yy_rowMap [] = { 
        0,    39,    78,    39,    39,   117,   156,    39,   195,   234, 
      273,   312,   351,   390,    39,    39,    39,    39,    39,    39, 
      429,   468,    39,   507,   546,   585,   624,   663,   702,   741, 
      780,   819,   858,   897,   936,   975,    39,    39,    39,  1014, 
     1053,   234,   234,   234,  1092,  1131,  1131,  1170,  1209,  1248, 
     1287,   234,  1326,   975,  1365,  1404,  1443,  1482,   234,   234, 
      234,  1521,  1560,  1599,  1638,  1677,   234,  1716,  1755,  1794, 
      234,  1833,  1872,  1911,    39
  };

  /** 
   * The packed transition table of the DFA (part 0)
   */
  final private static String yy_packed0 = 
    "\1\2\1\3\2\4\1\5\1\6\1\7\1\10\1\2"+
    "\1\11\2\12\1\13\1\14\1\15\2\12\1\16\1\2"+
    "\1\4\1\17\1\20\1\21\1\22\1\23\1\24\1\25"+
    "\1\26\1\27\1\30\1\12\1\31\1\32\3\12\1\33"+
    "\1\12\1\4\51\0\1\4\50\0\2\6\1\34\1\0"+
    "\1\35\42\0\3\12\2\0\6\12\1\36\1\12\14\0"+
    "\11\12\5\0\3\12\2\0\1\12\1\37\6\12\14\0"+
    "\11\12\5\0\3\12\2\0\10\12\14\0\11\12\5\0"+
    "\3\12\2\0\4\12\1\40\3\12\14\0\11\12\5\0"+
    "\3\12\2\0\10\12\14\0\1\41\10\12\5\0\3\12"+
    "\2\0\10\12\14\0\3\12\1\42\5\12\5\0\15\43"+
    "\1\44\10\43\2\0\13\43\31\0\1\45\1\0\1\46"+
    "\44\0\1\47\21\0\3\12\2\0\2\12\1\50\5\12"+
    "\14\0\2\12\1\51\6\12\5\0\3\12\2\0\1\12"+
    "\1\52\6\12\14\0\11\12\5\0\3\12\2\0\6\12"+
    "\1\53\1\12\14\0\1\54\10\12\5\0\2\12\1\55"+
    "\2\0\10\12\14\0\11\12\5\0\2\56\1\0\1\57"+
    "\43\0\2\60\45\0\3\12\2\0\10\12\14\0\5\12"+
    "\1\61\3\12\5\0\3\12\2\0\2\12\1\62\5\12"+
    "\14\0\11\12\5\0\3\12\2\0\5\12\1\63\2\12"+
    "\14\0\11\12\5\0\3\12\2\0\10\12\14\0\1\12"+
    "\1\64\7\12\5\0\3\12\2\0\10\12\14\0\4\12"+
    "\1\65\4\12\5\0\15\43\1\66\10\43\2\0\13\43"+
    "\21\0\1\43\31\0\3\12\2\0\5\12\1\67\2\12"+
    "\14\0\11\12\5\0\3\12\2\0\1\70\7\12\14\0"+
    "\11\12\5\0\3\12\2\0\1\71\7\12\14\0\11\12"+
    "\5\0\2\56\45\0\2\60\1\34\44\0\3\12\2\0"+
    "\4\12\1\72\3\12\14\0\11\12\5\0\2\12\1\73"+
    "\2\0\10\12\14\0\11\12\5\0\3\12\2\0\6\12"+
    "\1\62\1\12\14\0\11\12\5\0\2\12\1\74\2\0"+
    "\10\12\14\0\11\12\5\0\3\12\2\0\5\12\1\75"+
    "\2\12\14\0\11\12\5\0\3\12\2\0\10\12\14\0"+
    "\11\12\1\76\4\0\3\12\2\0\10\12\14\0\10\12"+
    "\1\77\5\0\3\12\2\0\10\12\14\0\6\12\1\100"+
    "\2\12\45\0\1\101\6\0\2\12\1\102\2\0\10\12"+
    "\14\0\11\12\5\0\2\12\1\103\2\0\10\12\14\0"+
    "\11\12\7\0\1\104\44\0\2\12\1\105\2\0\10\12"+
    "\14\0\11\12\12\0\1\106\41\0\3\12\2\0\10\12"+
    "\14\0\1\107\10\12\46\0\1\110\7\0\1\111\46\0"+
    "\1\112\75\0\1\113\11\0";

  /** 
   * The transition table of the DFA
   */
  final private static int yytrans [] = yy_unpack();


  /* error codes */
  final private static int YY_UNKNOWN_ERROR = 0;
  final private static int YY_ILLEGAL_STATE = 1;
  final private static int YY_NO_MATCH = 2;
  final private static int YY_PUSHBACK_2BIG = 3;

  /* error messages for the codes above */
  final private static String YY_ERROR_MSG[] = {
    "Unkown internal scanner error",
    "Internal error: unknown state",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * YY_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private final static byte YY_ATTRIBUTE[] = {
     0,  9,  1,  9,  9,  1,  1,  9,  1,  1,  1,  1,  1,  1,  9,  9, 
     9,  9,  9,  9,  1,  1,  9,  1,  1,  1,  1,  0,  0,  1,  1,  1, 
     1,  1,  0,  0,  9,  9,  9,  1,  1,  1,  1,  1,  1,  1,  0,  1, 
     1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  0,  1,  1, 
     0,  1,  1,  0,  1,  0,  1,  0,  0,  0,  9
  };

  /** the input device */
  private java.io.Reader yy_reader;

  /** the current state of the DFA */
  private int yy_state;

  /** the current lexical state */
  private int yy_lexical_state = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private char yy_buffer[] = new char[YY_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int yy_markedPos;

  /** the textposition at the last state to be included in yytext */
  private int yy_pushbackPos;

  /** the current text position in the buffer */
  private int yy_currentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int yy_startRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int yy_endRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /**
   * the number of characters from the last newline up to the start of the 
   * matched text
   */
  private int yycolumn; 

  /** 
   * yy_atBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean yy_atBOL = true;

  /** yy_atEOF == true <=> the scanner is at the EOF */
  private boolean yy_atEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean yy_eof_done;

  /* user code: */
    /* To create a new Symbol with information about
       the current token, the token will have no value in this
       case. */
    private Symbol symbol(int type) {
      return new Symbol(type, yyline, yycolumn);
    }
    
    /* Also creates a new Symbol with information
       about the current token, but this object has a value. */
    private Symbol symbol(int type, Object value) {
      return new Symbol(type, yyline, yycolumn, value);
    }


  /**
   * Creates a new scanner
   * There is also a java.io.InputStream version of this constructor.
   *
   * @param   in  the java.io.Reader to read input from.
   */
  Lexer(java.io.Reader in) {
    this.yy_reader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  Lexer(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  public Lexer(String in)
  {
    this(new java.io.StringReader(in));
  }

  /** 
   * Unpacks the split, compressed DFA transition table.
   *
   * @return the unpacked transition table
   */
  private static int [] yy_unpack() {
    int [] trans = new int[1950];
    int offset = 0;
    offset = yy_unpack(yy_packed0, offset, trans);
    return trans;
  }

  /** 
   * Unpacks the compressed DFA transition table.
   *
   * @param packed   the packed transition table
   * @return         the index of the last entry
   */
  private static int yy_unpack(String packed, int offset, int [] trans) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do trans[j++] = value; while (--count > 0);
    }
    return j;
  }


  /**
   * Gets the next input character.
   *
   * @return      the next character of the input stream, EOF if the
   *              end of the stream is reached.
   * @exception   IOException  if any I/O-Error occurs
   */
  private int yy_advance() throws java.io.IOException {

    /* standard case */
    if (yy_currentPos < yy_endRead) return yy_buffer[yy_currentPos++];

    /* if the eof is reached, we don't need to work hard */ 
    if (yy_atEOF) return YYEOF;

    /* otherwise: need to refill the buffer */

    /* first: make room (if you can) */
    if (yy_startRead > 0) {
      System.arraycopy(yy_buffer, yy_startRead, 
                       yy_buffer, 0, 
                       yy_endRead-yy_startRead);

      /* translate stored positions */
      yy_endRead-= yy_startRead;
      yy_currentPos-= yy_startRead;
      yy_markedPos-= yy_startRead;
      yy_pushbackPos-= yy_startRead;
      yy_startRead = 0;
    }

    /* is the buffer big enough? */
    if (yy_currentPos >= yy_buffer.length) {
      /* if not: blow it up */
      char newBuffer[] = new char[yy_currentPos*2];
      System.arraycopy(yy_buffer, 0, newBuffer, 0, yy_buffer.length);
      yy_buffer = newBuffer;
    }

    /* finally: fill the buffer with new input */
    int numRead = yy_reader.read(yy_buffer, yy_endRead, 
                                            yy_buffer.length-yy_endRead);

    if ( numRead == -1 ) return YYEOF;

    yy_endRead+= numRead;

    return yy_buffer[yy_currentPos++];
  }


  /**
   * Closes the input stream.
   */
  final public void yyclose() throws java.io.IOException {
    yy_atEOF = true;            /* indicate end of file */
    yy_endRead = yy_startRead;  /* invalidate buffer    */

    if (yy_reader != null)
      yy_reader.close();
  }


  /**
   * Closes the current stream, and resets the
   * scanner to read from a new input stream.
   *
   * All internal variables are reset, the old input stream 
   * <b>cannot</b> be reused (internal buffer is discarded and lost).
   * Lexical state is set to <tt>YY_INITIAL</tt>.
   *
   * @param reader   the new input stream 
   */
  final public void yyreset(java.io.Reader reader) throws java.io.IOException {
    yyclose();
    yy_reader = reader;
    yy_atBOL  = true;
    yy_atEOF  = false;
    yy_endRead = yy_startRead = 0;
    yy_currentPos = yy_markedPos = yy_pushbackPos = 0;
    yyline = yychar = yycolumn = 0;
    yy_lexical_state = YYINITIAL;
  }


  /**
   * Returns the current lexical state.
   */
  final public int yystate() {
    return yy_lexical_state;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  final public void yybegin(int newState) {
    yy_lexical_state = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  final public String yytext() {
    return new String( yy_buffer, yy_startRead, yy_markedPos-yy_startRead );
  }


  /**
   * Returns the character at position <tt>pos</tt> from the 
   * matched text. 
   * 
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. 
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  final public char yycharat(int pos) {
    return yy_buffer[yy_startRead+pos];
  }


  /**
   * Returns the length of the matched text region.
   */
  final public int yylength() {
    return yy_markedPos-yy_startRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of 
   * yypushback(int) and a match-all fallback rule) this method 
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void yy_ScanError(int errorCode) {
    String message;
    try {
      message = YY_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = YY_ERROR_MSG[YY_UNKNOWN_ERROR];
    }

    throw new Error(message);
  } 


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  private void yypushback(int number)  {
    if ( number > yylength() )
      yy_ScanError(YY_PUSHBACK_2BIG);

    yy_markedPos -= number;
  }


  /**
   * Contains user EOF-code, which will be executed exactly once,
   * when the end of file is reached
   */
  private void yy_do_eof() throws java.io.IOException {
    if (!yy_eof_done) {
      yy_eof_done = true;
      yyclose();
    }
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   IOException  if any I/O-Error occurs
   */
  public Symbol next_token() throws java.io.IOException {
    int yy_input;
    int yy_action;


    while (true) {

      boolean yy_r = false;
      for (yy_currentPos = yy_startRead; yy_currentPos < yy_markedPos;
                                                      yy_currentPos++) {
        switch (yy_buffer[yy_currentPos]) {
        case '\u000B':
        case '\u000C':
        case '\u0085':
        case '\u2028':
        case '\u2029':
          yyline++;
          yycolumn = 0;
          yy_r = false;
          break;
        case '\r':
          yyline++;
          yycolumn = 0;
          yy_r = true;
          break;
        case '\n':
          if (yy_r)
            yy_r = false;
          else {
            yyline++;
            yycolumn = 0;
          }
          break;
        default:
          yy_r = false;
          yycolumn++;
        }
      }

      if (yy_r) {
        if ( yy_advance() == '\n' ) yyline--;
        if ( !yy_atEOF ) yy_currentPos--;
      }

      yy_action = -1;

      yy_currentPos = yy_startRead = yy_markedPos;

      yy_state = yy_lexical_state;


      yy_forAction: {
        while (true) {

          yy_input = yy_advance();

          if ( yy_input == YYEOF ) break yy_forAction;

          int yy_next = yytrans[ yy_rowMap[yy_state] + yycmap[yy_input] ];
          if (yy_next == -1) break yy_forAction;
          yy_state = yy_next;

          int yy_attributes = YY_ATTRIBUTE[yy_state];
          if ( (yy_attributes & 1) > 0 ) {
            yy_action = yy_state; 
            yy_markedPos = yy_currentPos; 
            if ( (yy_attributes & 8) > 0 ) break yy_forAction;
          }

        }
      }


      switch (yy_action) {

        case 53: 
          {  return symbol(sym.STRING, new String(yytext())); }
        case 76: break;
        case 6: 
        case 8: 
        case 9: 
        case 10: 
        case 11: 
        case 12: 
        case 23: 
        case 24: 
        case 25: 
        case 26: 
        case 29: 
        case 30: 
        case 31: 
        case 32: 
        case 33: 
        case 39: 
        case 40: 
        case 44: 
        case 48: 
        case 49: 
        case 50: 
        case 52: 
        case 54: 
        case 56: 
        case 57: 
        case 62: 
        case 63: 
        case 65: 
        case 68: 
          {  return symbol(sym.ID, new String(yytext())); }
        case 77: break;
        case 74: 
          {  return symbol(sym.NOTBETWEEN);  }
        case 78: break;
        case 70: 
          {  return symbol(sym.BETWEEN);  }
        case 79: break;
        case 66: 
          {  return symbol(sym.ESCAPE);  }
        case 80: break;
        case 60: 
          {  return symbol(sym.NULL);  }
        case 81: break;
        case 59: 
          {  return symbol(sym.LIKE);  }
        case 82: break;
        case 58: 
          {  return symbol(sym.BOOLEAN, new Boolean(yytext()));  }
        case 83: break;
        case 55: 
          {  return symbol(sym.NOT);  }
        case 84: break;
        case 51: 
          {  return symbol(sym.AND);  }
        case 85: break;
        case 43: 
          {  return symbol(sym.IN);  }
        case 86: break;
        case 42: 
          {  return symbol(sym.IS);  }
        case 87: break;
        case 41: 
          {  return symbol(sym.OR);  }
        case 88: break;
        case 16: 
          {  return symbol(sym.DIVIDE);  }
        case 89: break;
        case 15: 
          {  return symbol(sym.TIMES);  }
        case 90: break;
        case 14: 
          {  return symbol(sym.PLUS);  }
        case 91: break;
        case 7: 
          {  return symbol(sym.MINUS);  }
        case 92: break;
        case 4: 
        case 5: 
        case 45: 
        case 47: 
          {  return symbol(sym.DOUBLE, new Double(yytext()));  }
        case 93: break;
        case 2: 
        case 3: 
          {  /* just skip what was found, do nothing */  }
        case 94: break;
        case 1: 
        case 13: 
          {  throw new Error("Illegal character <"+yytext()+">");  }
        case 95: break;
        case 17: 
          {  return symbol(sym.LPAREN);  }
        case 96: break;
        case 18: 
          {  return symbol(sym.RPAREN);  }
        case 97: break;
        case 19: 
          {  return symbol(sym.EQ);  }
        case 98: break;
        case 20: 
          {  return symbol(sym.LT);  }
        case 99: break;
        case 21: 
          {  return symbol(sym.GT);  }
        case 100: break;
        case 22: 
          {  return symbol(sym.COMMA);  }
        case 101: break;
        case 36: 
          {  return symbol(sym.LE);  }
        case 102: break;
        case 37: 
          {  return symbol(sym.NEQ);  }
        case 103: break;
        case 38: 
          {  return symbol(sym.GE);  }
        case 104: break;
        default: 
          if (yy_input == YYEOF && yy_startRead == yy_currentPos) {
            yy_atEOF = true;
            yy_do_eof();
              { return new Symbol(sym.EOF); }
          } 
          else {
            yy_ScanError(YY_NO_MATCH);
          }
      }
    }
  }


}
