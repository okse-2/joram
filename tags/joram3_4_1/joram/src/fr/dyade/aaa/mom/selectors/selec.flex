/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
/*
  Subset of SQL 92 syntax for message selectors.
  Author: Frederic Maistre, INRIA, February 2002.
*/
import java_cup.runtime.*;
      
%%
   
/* 
  Will write the code to the file Lexer.java. 
*/
%class Lexer

/*
  The current line number can be accessed with the variable yyline
  and the current column number with the variable yycolumn.
*/
%line
%column

/*
  Terminals' case is ignored.
*/
%ignorecase
    
/* 
  Will switch to a CUP compatibility mode to interface with a CUP
  generated parser.
*/
%cup
   
/*
  Declarations
   
  Code between %{ and %}, both of which must be at the beginning of a
  line, will be copied letter to letter into the lexer class source.
  Here you declare member variables and functions that are used inside
  scanner actions.  
*/
%{   
    /* To create a new java_cup.runtime.Symbol with information about
       the current token, the token will have no value in this
       case. */
    private Symbol symbol(int type) {
      return new Symbol(type, yyline, yycolumn);
    }
    
    /* Also creates a new java_cup.runtime.Symbol with information
       about the current token, but this object has a value. */
    private Symbol symbol(int type, Object value) {
      return new Symbol(type, yyline, yycolumn, value);
    }
%}
   

/*
  Macro Declarations
  
  These declarations are regular expressions that will be used latter
  in the Lexical Rules Section.  
*/
   
/* A line terminator is a \r (carriage return), \n (line feed), or
   \r\n. */
LineTerminator = \r|\n|\r\n
   
/* White space is a line terminator, space, tab, or line feed. */
WhiteSpace     = {LineTerminator} | [ \t\f]

/* Numeric literal.
   Long and Double are stored as Double
*/   
dec_num_lit = 0 | [1-9][0-9]* | [1-9][0-9]*E(-)?[0-9][0-9]* | [0-9][0-9]*\.[0-9]* | [0-9][0-9]*\.[0-9]*E(-)?[0-9][0-9]* | \.[0-9][0-9]* | \.[0-9][0-9]*E(-)?[0-9][0-9]*

/* Literal boolean. */
dec_boolean_lit = TRUE | FALSE

/* Literal identifier. */   
dec_id_lit = [A-Za-z_$£][A-Za-z0-9_$£]*

/* Literal string. */
dec_string_lit = '([A-Za-z0-9&$£%_@#\{\(\[\]\)\}\-\+\*\/\\=\,\.;:\!\?\~\t ]|'{2})+'
  
%%
/* ------------------------Lexical Rules Section---------------------- */
   
/*
   This section contains regular expressions and actions, i.e. Java
   code, that will be executed when the scanner matches the associated
   regular expression. */
   
   /* YYINITIAL is the state at which the lexer begins scanning.  So
   these regular expressions will only be matched if the scanner is in
   the start state YYINITIAL. */
   
<YYINITIAL> {
   
  /* Print the token found that was declared in the class sym and then
     return it. */
  "+"           { return symbol(sym.PLUS); }
  "-"           { return symbol(sym.MINUS); }
  "*"           { return symbol(sym.TIMES); }
  "/"           { return symbol(sym.DIVIDE); }
  "("           { return symbol(sym.LPAREN); }
  ")"           { return symbol(sym.RPAREN); }
  "="           { return symbol(sym.EQ); }
  "<>"          { return symbol(sym.NEQ); }
  ">"           { return symbol(sym.GT); }
  ">="          { return symbol(sym.GE); }
  "<"           { return symbol(sym.LT); }
  "<="          { return symbol(sym.LE); }
  ","           { return symbol(sym.COMMA); }
  "AND"         { return symbol(sym.AND); }
  "OR"          { return symbol(sym.OR); }
  "LIKE"        { return symbol(sym.LIKE); }
  "ESCAPE"      { return symbol(sym.ESCAPE); }
  "BETWEEN"     { return symbol(sym.BETWEEN); }
  "NOT BETWEEN" { return symbol(sym.NOTBETWEEN); }
  "IN"          { return symbol(sym.IN); } 
  "IS"          { return symbol(sym.IS); }
  "NOT"         { return symbol(sym.NOT); }
  "NULL"        { return symbol(sym.NULL); }
  
  {dec_num_lit}     { return symbol(sym.DOUBLE, new Double(yytext())); }

  {dec_boolean_lit} { return symbol(sym.BOOLEAN, new Boolean(yytext())); }
   
  {dec_id_lit}      { return symbol(sym.ID, new String(yytext()));}

  {dec_string_lit}  { return symbol(sym.STRING, new String(yytext()));}
   
  /* Don't do anything if whitespace is found */
  {WhiteSpace}      { /* just skip what was found, do nothing */ }   
}


/* No token was found for the input so through an error.  Print out an
   Illegal character message with the illegal character that was found. */
[^] { throw new Error("Illegal character <"+yytext()+">"); }
