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
#include <stdlib.h>
#include <stdio.h>

#include "XoramWrapper.H"

int main(int argc, char *argv[]) {

    char* cf = create_tcp_connection_factory("localhost", 16010);
    char* cnx = create_connection(cf, "anonymous", "anonymous");
    start_connection(cnx);

    char* sess = create_session(cnx);
    char* queue = create_queue("#0.0.1026", "queue");
    char* prod = create_producer(sess, queue);
    char* cons = create_consumer(sess, queue);
    printf("prod = %x, cons = %x\n", prod, cons);

    char* name =  "prop_name";
    char* value = "my property";
    char* keyInt =  "prop_int";
    
    char* msg = create_message(sess);
    set_string_property(msg, name, value);
    set_int_property(msg, keyInt, 1234);
    
    send_message(prod, msg);
    printf("##### Message sent on queue: %s, prop = %s, i = %i\n", get_message_id(msg), get_string_property(msg, name), get_int_property(msg, keyInt));
    delete_message(msg);
    
    msg = receive_message(cons);
    printf("##### Message received: %s, prop = %s, i = %i\n", get_message_id(msg), get_string_property(msg, name), get_int_property(msg, keyInt));
    
    close_connection(cnx);
    delete_destination(queue);
    
    printf("##### bye\n");
}
