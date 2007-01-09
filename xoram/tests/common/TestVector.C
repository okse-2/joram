#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

/* Pour open, read, etc. */
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "Vector.H"

int main(int argc, char* argv[]) {
  Vector<char> v(5);
  Vector<char> *vector = new Vector<char>();
  vector->addElement("abcde");
  vector->addElement("fghijkl");
  printf("%d, %s, %s\n", vector->size(), vector->elementAt(0), vector->elementAt(1));

  exit(0);
}
