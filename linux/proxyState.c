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

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <unistd.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <getopt.h>
#include <errno.h>

#include "proxyState.h"
#include "../../mjpg_streamer.h"

// TODO: consider moving delays to plugin command line arguments
void connect_and_stream(struct proxy_state * state){
    struct addrinfo * info, * rp;
    int errorcode;
    struct hostent *hptr;
    struct sockaddr_in serv_addr;
    char ip[50];
    DBG("connect_and_stream, host: %s\n", state->hostname);

    while (1) {
        syslog(LOG_INFO, "gethostbyname:%s", state->hostname);
        if(state->isIpAddr != 1 && (hptr = gethostbyname(state->hostname)) == NULL)
        {
            syslog(LOG_INFO, "gethostbyname error£¬%s", state->hostname);
            perror("gethostbyname error, will retry in 5 sec");
            sleep(5);
            continue;
        }
        

    
        syslog(LOG_INFO, "create socket:%s", state->hostname);
        if((state->sockfd = socket(AF_INET,SOCK_STREAM,0)) <  0) {
            perror("socket");
            break;
        }

        int rcvBufSize = 1024;
        int optlen = sizeof(rcvBufSize);
        if (setsockopt(state->sockfd, SOL_SOCKET, SO_RCVBUF, &rcvBufSize, optlen) < 0)//important, make sure buffer size
        {
            perror("set sockopt buf size");
            break;
        }

        syslog(LOG_INFO, "create socket success:%d", state->sockfd);
        serv_addr.sin_family = AF_INET;
        serv_addr.sin_port = htons(atoi(state->port));
        
        memset(ip,0,sizeof(ip));
        char* sip = (state->isIpAddr==1) ? state->hostname : inet_ntop(hptr->h_addrtype,hptr->h_addr_list[0],ip,sizeof(ip));
        serv_addr.sin_addr.s_addr = inet_addr(sip);//*((struct in_addr *)hptr->h_addr);
        bzero(&(serv_addr.sin_zero),8);
        
        syslog(LOG_INFO, "connect socket ip:%s", sip);
        if((connect(state->sockfd,(struct sockaddr *)&serv_addr,sizeof(struct sockaddr))) == -1) {
            perror("Can't connect to server, will retry in 5 sec");
            sleep(5);
            continue;
        } else {
            syslog(LOG_INFO, "connect socket success:%d", state->sockfd);
            break;
        }
    }

}

void send_request_and_process_response(struct proxy_state * state) {
    if (0!= pthread_create(&state->sendThread, 0, sendThreadFunc, state))
    {
        perror("pthread_create send");
        //break; 
    }
    pthread_detach(state->sendThread);

    if (0!= pthread_create(&state->recvThread, 0, recvThreadFunc, state))
    {
        perror("pthread_create recv");
        //break; 
    }
    pthread_detach(state->recvThread);
    syslog(LOG_INFO, "send_request_and_process_response finished....");
}

void close_proxy(struct proxy_state * state){
    if (state->hostname) free(state->hostname);
    if (state->port) free(state->port);
}

void *recvThreadFunc(void *arg){
    struct proxy_state* state = (struct proxy_state*)arg;
    int recv_length;
    syslog(LOG_INFO, "recvThreadFunc running....");
    if (state != NULL && state->on_data_received) {
        
        while ( (recv_length = recv(state->sockfd, state->recvBuf, state->recvBufSize, 0)) > 0 
                    && !state->should_stop) {
            syslog(LOG_INFO, "recvThreadFunc %d data received....", recv_length);
            state->on_data_received(state->recvBuf, recv_length);
        }
    }
}

void *sendThreadFunc(void *arg){
    struct proxy_state* state = (struct proxy_state*)arg;
    int recv_length;
    syslog(LOG_INFO, "sendThreadFunc running....");
    if (state != NULL) {
        while (!state->should_stop) {
            if (state->sendLen > 0) {
                int ret = send(state->sockfd, state->sendBuf, state->sendLen ,0);
                if (ret > 0) {
                    DBG("send success! data len:%d, sock:%d\n",state->sendLen , state->sockfd);
                } else {
                    DBG("send error! data len:%d, sock:%d,err:%d errno:%d\n",state->sendLen , state->sockfd,ret,errno);
                }
                state->sendLen = 0;
            }
            usleep(1);
        }
    }
}
