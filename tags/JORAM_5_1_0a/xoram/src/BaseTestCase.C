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
# include "BaseTestCase.H"
  
BaseTestCase *BaseTestCase::current=NULL;

BaseTestCase::BaseTestCase(char * argv[]){
  current = this; 
  name = new char[50]; 
  failures = NULL;
  errors = NULL;
  exceptions = NULL;
  strcpy(name,argv[0]);
  current->writer=new std::ofstream();
  current->writer->open("../tests/report.txt", std::ios_base::app );
  current->setStartDate();
    
}
 

BaseTestCase::~BaseTestCase(){
  if(name != NULL)
    delete [] name;
  if(failures != NULL)
    delete failures ;
  
  if(errors != NULL)
    delete errors;
  
  if(exceptions != NULL)
    delete exceptions ;
  
}
void BaseTestCase::setName(){
  strcpy(name,typeid(this).name());
}

BaseTestCase & BaseTestCase::operator=(const BaseTestCase & b) {
  if (&b != this) {
    delete [] name;
    name=new char[50];
    strcpy(name,b.name);
       
    delete failures;
    failures = new Vector<Exception>(b.failures->capacity());
    
    delete errors ;
    errors  = new Vector<Exception>(b.errors->capacity());
    
    delete exceptions ;
    exceptions = new Vector<Exception>(b.exceptions->capacity());
    
    // delete writer;
    writer =b.writer; 

    }
  return *this;
  
}



void BaseTestCase::addException(Exception *t){
 
  if (exceptions != NULL){
    exceptions->addElement(t);
  } else{
    exceptions = new Vector<Exception>(50);
  }
 
}

/**
 * Adds a failure to the list of failures. The passed in exception
 * caused the failure.
 * The test framework distinguishes between <i>failures</i> and
 * <i>errors</i>. A failure is anticipated and checked for with
 * assertions.
 */
void BaseTestCase::addFailure(Exception *t) {
  if (failures == NULL ) failures = new Vector<Exception>(50);
  failures->addElement(t);
}

/**
 * Gets the number of detected failures.
 */
int BaseTestCase::failureCount() {
  if (failures == NULL) return 0;
  return failures->size();
}

/**
 * Adds an error to the list of errors. The passed in exception
 * caused the error.
 * The test framework distinguishes between <i>failures</i> and
 * <i>errors</i>. A failure is anticipated and checked for with
 * assertions.
   */
void BaseTestCase::addError(Exception *t) {
  if (errors == NULL) errors = new Vector<Exception>();
  errors->addElement(t);
}

/**
 * Gets the number of detected errors.
 */
int BaseTestCase::errorCount() {
  if (errors == NULL) return 0;
  return errors->size();
}

/**
 * Asserts that a condition is true.
 */
void BaseTestCase::assertTrue(char * message, int condition) {
  if (!condition) fail(message);
}

/**
 * Asserts that a condition is true.
 */
void  BaseTestCase::assertTrue(int condition) {
  assertTrue("erreur", condition);
}

/**
 * Asserts that a condition is false.
 */
void BaseTestCase::assertFalse(char * message, boolean condition) {
  if (condition) fail(message);
}
 
/**
 * Asserts that a condition is false.
 */
void BaseTestCase::assertFalse(boolean condition) {
  assertFalse(NULL, condition);
}

 

void BaseTestCase::assertEquals(char * message,char * expected,char * actual){
   if(strcmp(expected,actual)!=0)
     failNotEquals(message, expected, actual);

}
void BaseTestCase::assertEquals(char * expected, char * actual){
  assertEquals(NULL,expected,actual);
  }

/**
 * Asserts that two doubles are equal concerning a delta. If the expected
 * value is infinity then the delta value is ignored.
 */
void BaseTestCase::assertEquals(char * message,double expected, double actual,double delta) {
  if (expected == FP_INFINITE ) {
    if (!(expected == actual))
      failNotEquals(message,expected, actual);
  } else if (!(fabs(expected-actual) <= delta))
    // Because comparison with NaN always returns false
    failNotEquals(message, expected, actual);
 }

/**
 * Asserts that two doubles are equal concerning a delta. If the expected
 * value is infinity then the delta value is ignored.
 */
void BaseTestCase::assertEquals(double expected, double actual,
				double delta) {
  assertEquals(NULL, expected, actual, delta);
}
void BaseTestCase::assertEquals(double expected, double actual) {
  assertEquals(NULL, expected, actual, 0);
}

/**
 * Asserts that two floats are equal concerning a delta. If the expected
 * value is infinity then the delta value is ignored.
 */
void BaseTestCase::assertEquals(char * message,	float expected, float actual,float delta) {
  if (expected == FP_INFINITE ) {
    if (!(expected == actual))
      failNotEquals(message, expected, actual);
  } else if (!(fabs(expected-actual) <= delta))
    failNotEquals(message, expected, actual);
}

/**
 * Asserts that two floats are equal concerning a delta. If the expected
 * value is infinity then the delta value is ignored.
 */
void BaseTestCase::assertEquals(float expected, float actual,
				float delta) {
  assertEquals(NULL, expected, actual, delta);
}

/**
 * Asserts that two longs are equal.
 */
void  BaseTestCase::assertEquals(char * message,long expected, long actual) {
  if (!(expected == actual))
    failNotEquals(message, expected, actual);
}

/**
 * Asserts that two longs are equal.
 */
void  BaseTestCase::assertEquals(long expected, long actual) {
  assertEquals(NULL, expected, actual);
}
/**
 * Asserts that two longs are equal.
 */
void  BaseTestCase::assertEquals(char * message,long long expected,long long actual) {
  if (!(expected == actual))
    failNotEquals(message, expected, actual);
}

/**
 * Asserts that two longs are equal.
 */
void  BaseTestCase::assertEquals(long long expected,long long actual) {
  assertEquals(NULL, expected, actual);
}






/**
 * Asserts that two booleans are equal.

void BaseTestCase::assertEquals(char * message,	boolean expected, boolean actual) {
  if (!(expected == actual))
    failNotEquals(message, expected, actual);
}
  */
/**
 * Asserts that two booleans are equal.

void BaseTestCase::assertEquals(boolean expected, boolean actual) {
  assertEquals(NULL, expected, actual);
}
 */
/**
 * Asserts that two bytes are equal.
 */
void BaseTestCase::assertEquals(char * message,byte expected, byte actual) {
  if (!(expected == actual))
    failNotEquals(message, expected, actual);
}

/**
 * Asserts that two bytes are equal.
 */
void BaseTestCase::assertEquals(byte expected, byte actual) {
  assertEquals(NULL, expected, actual);
}

/**
 * Asserts that two ints are equal.
 */
void BaseTestCase::assertEquals(char * message,int expected, int actual) {
   if (!(expected == actual))
    failNotEquals(message, expected, actual);
}

/**
 * Asserts that two ints are equal.
 */
void BaseTestCase::assertEquals(int expected, int actual) {
  assertEquals(NULL, expected, actual);
}

/**
 * Asserts that two shorts are equal.
 */
void BaseTestCase::assertEquals(char * message,short expected, short actual) {
  if (!(expected == actual))
    failNotEquals(message, expected, actual);
}


/**
 * Asserts that two shorts are equal.
 */
void BaseTestCase::assertEquals(short expected, short actual) {
  assertEquals(NULL, expected, actual);
}

/**
 * Asserts that two byte[] are equal.
 */
void BaseTestCase::assertEquals(byte* tab1, byte* tab2, int taille) {
  boolean ok=true;
  for(int j=0; j< taille && ok==true;j++){
    if(tab1[j]!= tab2[j]){
      failNotEquals(NULL,tab1,tab2);
      ok=false;
    }
  }
}


/**
 * Checks that two files are identical, ignoring address mismatch.
 */   
boolean BaseTestCase::check(std::ifstream *f1,std::ifstream  *f2) {
  while (true) {
    int c = f1->get();
    int c2 = f2->get();
    if (c2 != c) {
      // checks for a \r\n \n equivalence
      if ((c == '\r') && (c2 == '\n')) {
	c = f1->get();
	if (c == c2)
	  continue;
      } else if ((c2 == '\r') && (c == '\n')) {
	c2 = f2->get();
	if (c == c2)
	  continue;
      }
	  return FALSE;
    }
    if (c == -1) break;
  }
  return TRUE;
}

/**
 * Asserts that two files are same content.
 */
void BaseTestCase::assertFileSameContent(char * expected,char *  actual) {
  assertFileSameContent(NULL,expected,actual);
}
/**
 * Asserts that two files are same content.
 */
void BaseTestCase::assertFileSameContent(char * message,char *  expected,char *  actual) {
  boolean ok = true;
  std::ifstream *file1;
  std::ifstream *file2;

  char * formatted= new char[300];
  if (message != NULL) {
    strcpy(formatted,message);
    strcat(formatted, ", ");
  }else{
     strcpy(formatted,"");
  }
  try {
    file1->open(expected);
  } catch (Exception exc) {
    strcat(formatted,"cannot access file <" );
    strcat(formatted,expected);
    strcat(formatted, ">");
    fail(formatted);
    ok = false;
  }
  try {
    file2->open(actual);
  } catch (IOException exc) {
    strcat(formatted,"cannot access file <" );
    strcat(formatted,expected);
    strcat(formatted, ">");
    fail(formatted);
    ok = false;
  }
  if ((! ok) || isSameContent(file1, file2)) return;
  strcat(formatted,"file <" );
  strcat(formatted,expected);
  strcat(formatted,"> and <" );
  strcat(formatted,actual);
  strcat(formatted,"> differs");
  fail(formatted);
}

boolean BaseTestCase::isSameContent(std::ifstream *f1,std::ifstream  *f2) {
  long l2= 0;
  long pfile;
  std::map<long,long> h;
  try {
    int i=100;
    while (i-- > 0) {
      char* line1=new char[200];
      f1->getline(line1,200);
      l2 = 0;
      if (strcmp(line1,"")==0) {
	break;
      }
      int j=100;
      while (j-- > 0) {
	f2->seekg(l2);
	map<long,long>::iterator iter = h.find(l2);
	if( iter != h.end() ){ 
	  l2=iter->second;
	  continue;
	}
	char *line2=new char[200];
	f2->getline(line2,200);
	pfile=	f2->tellg();
	if (strcmp(line2,"")==0){
	  return FALSE;
	}
	
	if (strcmp(line1,line2)==0) {
	  h.insert( make_pair(l2, pfile));
	  break;
	} else {
	  l2 = pfile;
	  continue;
	}
      }
    }
    return TRUE;
  } catch (IOException exc) {
    return FALSE;
  }
}


/**
 * Asserts that a file exists.
 */
void BaseTestCase::assertFileExist(char* expected) {
  assertFileExist(NULL, expected);
}

/**
 * Asserts that a file exists.
 */
void BaseTestCase::assertFileExist(char* message,char*  expected) {
  char * formatted= new char[200];
  if (message != NULL) {
    strcpy(formatted,message);
    strcat(formatted, ", ");
  }else{
    strcpy(formatted,"");
  }
  std::ifstream fichier(expected); 
  if (fichier.fail()){
    strcat(formatted,"cannot access file <" );
    strcat(formatted,expected);
    strcat(formatted,">");
    fail(formatted);
  }
}

/**
 * Fails a test with the given message. 
 */
void BaseTestCase::fail(char * message) {
  if (message != NULL){
    current->addFailure(new Exception(message));
  }
}

void BaseTestCase::error(Exception *t) {
  if(t->getMessage() == NULL)
    current->addError(new Exception("unknown exception"));
  else
    current->addError(t);
}

void  BaseTestCase::exception(Exception *t) {
  if(t->getMessage() == NULL)
    current->addException(new Exception("unknown exception"));
  else
    current->addException(t);
}

void BaseTestCase::setStartDate() {
  struct timeval tp;
  gettimeofday(&tp,NULL); 
  current->startDate = double(tp.tv_sec)*1e+3 + double(tp.tv_usec)*1e-3 ;
}

void BaseTestCase::startTest(char * argv[]){
  new BaseTestCase(argv);
}


void BaseTestCase::endTest(char * msg, boolean ex) {
  //sleep(3); 
  char buf[500];
  struct timeval tp;
  gettimeofday(&tp,NULL); 
  current->endDate = double(tp.tv_sec)*1e+3 + double(tp.tv_usec)*1e-3 ;
  
  int status = 0;
  
  if ((current->failures != NULL) || (current->errors != NULL )) {
    std::cerr << "TEST \"" << current->name << "\" FAILED" << ", failures: ";
    std::cerr << current->failureCount() <<", errors: " << current->errorCount();
    std::cerr <<" ["<< (long)(current->endDate-current->startDate)<<" ms]"<<endl;
    
    
    if (current->writer != NULL){
      char *temp=new char[600];
      *(current->writer) << "TEST \""<< current->name<< "\" FAILED, failures: " << 
	current->failureCount() << ", errors: "<< current->errorCount()<< 
	", ["<< (long)(current->endDate-current->startDate)<< " ms]."<< endl;
      
    }
    
  } else {
    std::cerr << "TEST \"" << current->name << "\" OK [" << (long)(current->endDate - current->startDate) <<  " ms]." << endl;
    
    if (current->writer != NULL)
      *(current->writer) <<"TEST \"" << current->name << "\" OK [" <<
	(long) (current->endDate - current->startDate) << " ms]."<<endl;
    
  }
 
  if (msg != NULL) {
    std::cerr << msg<< endl;
    if (current->writer != NULL)
      *(current->writer) <<  msg<< endl;
  }
  
  if (current->failures != NULL) {
    status += current->failures->size();
    if (current->writer != NULL) {
      for (int i=0; i<current->failures->size(); i++) {
	*(current->writer)<<"+" << i << ") "<<endl;
	*(current->writer)<<(current->failures->elementAt(i))->getMessage()<<endl;
      }
    }
  }
  
  if (current->errors != NULL) {
    status += current->errors->size();
    if (current->writer != NULL) {
      for (int i=0; i<current->errors->size(); i++) {
	*(current->writer)<<"+" << i << ") "<<endl;
	*(current->writer)<<(current->errors->elementAt(i))->getMessage()<<endl;
      }
    }
  }
  
  if (current->exceptions != NULL) {
    if (current->writer != NULL) {
      for (int i=0; i<current->exceptions->size(); i++) {
	*(current->writer)<<"+" << i << ") "<<endl;
	*(current->writer)<<(current->exceptions->elementAt(i))->getMessage()<<endl;
      }
    }
  }
  
  if (current->writer != NULL) {
    current->writer->flush();
    current->writer->close();
  }
  
  if (ex) 
    exit(status);
  
}

void BaseTestCase::writeIntoFile(char * str){
  if (current->writer != NULL) {
    *(current->writer) << str << endl;
    current->writer->flush();
  }
}

void BaseTestCase::endTest() {
  endTest(true);
}

void BaseTestCase::endTest(boolean ex) {
  endTest(NULL, ex);
  }

void BaseTestCase::endTest(char * msg) {
  endTest(msg, true);
}


