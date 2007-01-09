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
#ifndef XORAM_EXCEPTION_H
#define XORAM_EXCEPTION_H

#include "Types.H"

class XoramException : public Exception {
 public:
  XoramException() : Exception() {}
  XoramException(char* msg) : Exception(msg) {}
};

class InvalidDestinationException : public XoramException {
 public:
  InvalidDestinationException() : XoramException() {}
  InvalidDestinationException(char* msg) : XoramException(msg) {}
};

class MessageFormatException : public XoramException {
 public:
  MessageFormatException() : XoramException() {}
  MessageFormatException(char* msg) : XoramException(msg) {}
};
 
class IllegalStateException : public XoramException {
 public:
  IllegalStateException() : XoramException() {}
  IllegalStateException(char* msg) : XoramException(msg) {}
};

class IllegalArgumentException : public XoramException {
 public:
  IllegalArgumentException() : XoramException() {}
  IllegalArgumentException(char* msg) : XoramException(msg) {}
};

class ConnectException : public XoramException {
 public:
  ConnectException() : XoramException() {}
  ConnectException(char* msg) : XoramException(msg) {}
};

class NotYetImplementedException : public XoramException {
 public:
  NotYetImplementedException() : XoramException() {}
  NotYetImplementedException(char* msg) : XoramException(msg) {}
};

class AbortedRequestException : public XoramException {
 public:
  AbortedRequestException() : XoramException() {}
  AbortedRequestException(char* msg) : XoramException(msg) {}
};

class SecurityException : public XoramException {
 public:
  SecurityException() : XoramException() {}
  SecurityException(char* msg) : XoramException(msg) {}
};

class MOMException : public XoramException {
 public:
  MOMException() : XoramException() {}
  MOMException(char* msg) : XoramException(msg) {}
};

#endif /* XORAM_EXCEPTION_H */
