/*******************************************************************************
#                                                                              #
#      Copyright (C) 2011 Eugene Katsevman                                     #
#                                                                              #
# This program is free software; you can redistribute it and/or modify         #
# it under the terms of the GNU General Public License as published by         #
# the Free Software Foundation; version 2 of the License.                      #
#                                                                              #
# This program is distributed in the hope that it will be useful,              #
# but WITHOUT ANY WARRANTY; without even the implied warranty of               #
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                #
# GNU General Public License for more details.                                 #
#                                                                              #
# You should have received a copy of the GNU General Public License            #
# along with this program; if not, write to the Free Software                  #
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA    #
#                                                                              #
*******************************************************************************/

#ifndef CLOUD_PROXY_H
#define CLOUD_PROXY_H

struct proxy_state {
    char * port;
    char * hostname;
    int isIpAddr;
    // this is current result
    int recvBufSize;
    int sendBufSize;
    char *recvBuf;
    char *sendBuf;
    int sendLen;

    int sockfd;

    int should_stop;
    pthread_t sendThread;
    pthread_t recvThread;

    void (*on_data_received)(char * data, int length);
    void (*send_data)(char * data, int length);
};

void init_proxy(struct proxy_state * state);
void close_proxy(struct proxy_state * state);

void connect_and_stream(struct proxy_state * state);

void send_request_and_process_response(struct proxy_state * state);

void *recvThreadFunc(void *arg);
void *sendThreadFunc(void *arg);
#endif
