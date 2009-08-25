/*
 * XORAM: Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s):  ScalAgent Distributed Technologies
 * Contributor(s):
 */
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <math.h>
#include <string>
#include <sstream>
#include <fstream>
#include <map>

#include <sys/time.h>

#include "Vector.H"

using namespace std;
class BaseTestCase {
  
  protected :
    static BaseTestCase *current;
    char * name;
    boolean summary; 
    Vector<Exception> *failures;
    Vector<Exception> *errors;
    Vector<Exception> *exceptions;
    std::ofstream *writer;
    double startDate;
    double endDate;
    
public :

    BaseTestCase(char * argv[]);
    ~BaseTestCase();

 

    BaseTestCase & operator=(const BaseTestCase & b) ;
    void setName();

    void addException(Exception *t);
    void addFailure(Exception *t);
    void addError(Exception *t);
    
    int failureCount();
    int errorCount();
    
    static void fail(char * message);
    static void error(Exception *t);
    static void exception(Exception *t);
    
    static void setStartDate();
    
    static void assertTrue(char * message,int  condition);
    static void assertTrue(int condition);
    
    static void assertFalse(char * message, boolean condition);
    static void assertFalse(boolean condition);
    
    static void assertEquals(char * message, char * expected,char *  actual);
    static void assertEquals(char * expected, char * actual);

    static void assertEquals(char * message, double expected, double actual, double delta);
    static void assertEquals(double expected, double actual, double delta);
    static void assertEquals(double expected, double actual);

    static void assertEquals(char * message, float expected, float actual, float delta);
    static void assertEquals(float expected, float actual, float delta);

    static void assertEquals(char * message, long expected, long actual);
    static void assertEquals(long expected, long actual);

    static void assertEquals(char * message,long long expected,long long actual);
    static void assertEquals(long long expected,long long actual);

  // static void assertEquals(char * message, boolean expected, boolean actual);
  // static void assertEquals(boolean expected, boolean actual);
   
    static void assertEquals(char * message,byte expected, byte actual);
    static void assertEquals(byte expected, byte actual);

    static void assertEquals(char * message,int expected, int actual);
    static void assertEquals(int expected, int actual);

    static void assertEquals(char * message,short expected, short actual);
    static void assertEquals(short expected, short actual);

    static void assertEquals(byte* tab1, byte* tab2, int taille);
    
    static boolean check(std::ifstream *f1,std::ifstream  *f2);
    static void assertFileSameContent(char * message,char *  expected,char *  actual);
    static void assertFileSameContent(char *  expected,char *  actual);
    static boolean isSameContent(std::ifstream *file1,std::ifstream *file2);
    static void assertFileExist(char* message,char*  expected);
    static void assertFileExist(char*  expected);
    
    static void startTest(char * argv[]);

    static void endTest(char * msg, boolean exit);
    static void endTest();
    static void endTest(boolean ex);
    static void endTest(char *msg);
    
    static void writeIntoFile(char * str);
    
    template<typename T>
      static void failNotEquals(char * message,T & expected, T & actual) {
      char *temp=new char[300];
      if (message != NULL){
	strcpy(temp,message);
	strcat(temp," , ");
      }else{
	strcpy(temp,"");
      }
      ostringstream oss;
      oss<<"expected:<" << expected <<  "> but was:<" << actual << ">";
      strcat(temp, oss.str().c_str());
      fail(temp);
    }
    
};
