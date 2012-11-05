/*
 * XORAM: Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 CNES
 * Copyright (C) 2006 ScalAgent Distributed Technologies
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
#ifndef TYPES_H
#define TYPES_H

#ifndef NULL
#define NULL    0
#endif /* NULL */

#ifndef TRUE
#define TRUE	(0==0)
#define FALSE	(!TRUE)
#endif /* TRUE */

#ifndef INTEGER_MAX_VALUE
#define INTEGER_MAX_VALUE 0x7FFFFFFF
#endif /* INTEGER_MAX_VALUE */

#ifndef DEBUG
#define DEBUG    (FALSE)
#endif /* DEBUG */

typedef char byte;
typedef byte boolean;

class Exception {
 protected:
  char* msg;

 public:
  Exception() {
    this->msg = (char*) NULL;
  }

  Exception(char* msg) {
    this->msg = msg;
  }

  char* getMessage() {
    return msg;
  }
};

class IOException : public Exception {
 public:
  IOException() : Exception() {}
  IOException(char* msg) : Exception(msg) {}
};

class UnknownClass {};

#endif /* TYPES_H */
