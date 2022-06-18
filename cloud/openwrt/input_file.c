/*******************************************************************************
#                                                                              #
#      MJPG-streamer allows to stream JPG frames from an input-plugin          #
#      to several output plugins                                               #
#                                                                              #
#      Copyright (C) 2007 Tom StÃ¶veken                                         #
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
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <getopt.h>
#include <pthread.h>
#include <syslog.h>
#include <sys/types.h>
#include <sys/inotify.h>
#include <dirent.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/socket.h>
#include <arpa/inet.h>

#include "../../mjpg_streamer.h"
#include "../../utils.h"

#define INPUT_PLUGIN_NAME "FILE input plugin"
#define QUEUE   10

#define CMD_BUF_SIZE (1024)
#define JPEG_BUF_SIZE (32*1024)
#define LOG_BUF_SIZE (20)

/* private functions and variables to this plugin */
static pthread_t uplinkThreadWorker;
static pthread_t downlinkThreadWorker;

static pthread_t downlinkRecvThreadWorker;//new add to fix new proxy not recognized issue
static pthread_t uplinkRecvThreadWorker;
static int downlinkRecvThreadId = -1;
static int uplinkRecvThreadId = -1;

static globals     *pglobal;

void *uplink_thread(void *);//upload wificar cmd&media to cloud
void *downlink_thread(void *);//upload command to wificar

void *downlink_recv_thread(void *);//new add to fix new proxy not recognized issue
void *uplink_recv_thread(void *);

void thread_cleanup(void *);





static int plugin_number;

static int DOWNLINK_PORT = 19090;//use by client
static int UPLINK_PORT = 19092;//use by proxy

static int downlink_fd = 0;
static int uplink_fd = 0;
static int last_client_fd = -1;//last connected android clent fd
static int last_proxy_fd = -1;//last connected proxy clent fd
static int last_downlink_client_fd = -1;
static int last_uplink_client_fd = -1;

static char uplinkBuf[JPEG_BUF_SIZE] = {0};
static char downlinkBuf[CMD_BUF_SIZE] = {0};
/* global variables for this plugin */
int input_cmd(int plugin, int command_id, int group, int value, char* sValue);

/*** plugin interface functions ***/
int input_init(input_parameter *param, int id)
{
    int i;
    plugin_number = id;

    param->argv[0] = INPUT_PLUGIN_NAME;

    /* show all parameters for DBG purposes */
    for(i = 0; i < param->argc; i++) {
        DBG("argv[%d]=%s\n", i, param->argv[i]);
    }

    reset_getopt();
    

    pglobal = param->global;



    param->global->in[id].name = malloc((strlen(INPUT_PLUGIN_NAME) + 1) * sizeof(char));
    sprintf(param->global->in[id].name, INPUT_PLUGIN_NAME);

    return 0;
}

int input_stop(int id)
{
    DBG("will cancel input thread\n");
    pthread_cancel(uplinkThreadWorker);
    pthread_cancel(downlinkThreadWorker);
    pthread_cancel(uplinkRecvThreadWorker);
    pthread_cancel(downlinkRecvThreadWorker);
    return 0;
}


int input_run(int id)
{
    pglobal->in[id].buf = NULL;




    //for uplink
    uplink_fd = socket(AF_INET,SOCK_STREAM, 0);
    int bufSize = CMD_BUF_SIZE;
    int optlen = sizeof(bufSize);
    if (setsockopt(uplink_fd, SOL_SOCKET, SO_RCVBUF, &bufSize, optlen) < 0) {
        perror("set uplink sockopt");
        exit(1);
    }

    //make socket reuse, to avoid "address in use" error
    int option = 1;
    setsockopt(uplink_fd, SOL_SOCKET,(SO_REUSEPORT | SO_REUSEADDR | SO_DEBUG), (char*) &option,sizeof(option));

    struct sockaddr_in server_sockaddr;
    bzero(&server_sockaddr, sizeof(server_sockaddr));
    server_sockaddr.sin_family = AF_INET;//IPv4
    server_sockaddr.sin_port = htons(UPLINK_PORT);
    server_sockaddr.sin_addr.s_addr = INADDR_ANY;


    ///
    if(bind(uplink_fd,(struct sockaddr *)&server_sockaddr,sizeof(server_sockaddr))==-1) {
        perror("bind uplink socket");
        exit(1);
    }
        
    DBG("bind uplink socket success, start listen!\n");
 
    ///listen
    if(listen(uplink_fd,QUEUE) == -1) {
        perror("listen uplink socket");
        exit(1);
    }

    if(pthread_create(&uplinkThreadWorker, 0, uplink_thread, NULL) != 0) {
        perror("uplink_thread");
        exit(1);
    }

    pthread_detach(uplinkThreadWorker);


    //for downlink, thread for comunication of client
    downlink_fd = socket(AF_INET,SOCK_STREAM, 0);
    bufSize = JPEG_BUF_SIZE;
    optlen = sizeof(bufSize);
    if (setsockopt(downlink_fd, SOL_SOCKET, SO_RCVBUF, &bufSize, optlen) < 0) {
        perror("set uplink sockopt");
        exit(1);
    }

    //make socket reuse, to avoid "address in use" error
    option = 1;
    setsockopt(downlink_fd, SOL_SOCKET,(SO_REUSEPORT | SO_REUSEADDR | SO_DEBUG), (char*) &option,sizeof(option));

    bzero(&server_sockaddr, sizeof(server_sockaddr));
    server_sockaddr.sin_family = AF_INET;//IPv4
    server_sockaddr.sin_port = htons(DOWNLINK_PORT);
    server_sockaddr.sin_addr.s_addr = INADDR_ANY;


    ///
    if(bind(downlink_fd,(struct sockaddr *)&server_sockaddr,sizeof(server_sockaddr))==-1) {
        perror("bind downlink socket");
        exit(1);
    }
        
    DBG("bind downlink socket success, start listen!\n");
 
    ///listen
    if(listen(downlink_fd,QUEUE) == -1) {
        perror("listen downlink socket");
        exit(1);
    }

    if(pthread_create(&downlinkThreadWorker, 0, downlink_thread, NULL) != 0) {
        perror("downlink_thread");
        exit(1);
    }

    pthread_detach(downlinkThreadWorker);

    DBG("input_run ..finish\n");
    return 0;
}
void *uplink_thread(void *arg)
{
    int proxy_fd;//this socket id of android client

    DBG("uplink_thread starting .... server socket id: %d\n", uplink_fd);
    while(!pglobal->stop) {
        

        struct sockaddr_in client_socket;
        socklen_t length = sizeof(client_socket);
 
        if (-1 == (proxy_fd = accept(uplink_fd, (struct sockaddr*)&client_socket, &length))){
            perror("uplind socket accept");
            continue;
        }

        char buf_ip[INET_ADDRSTRLEN];
        memset(buf_ip, '\0', sizeof(buf_ip));
        inet_ntop(AF_INET,&client_socket.sin_addr, buf_ip, sizeof(buf_ip));
        DBG("A proxy connect! socket id:%d, ip:%s \n", proxy_fd, buf_ip);
        last_proxy_fd = proxy_fd;
        
        if (uplinkRecvThreadId == 0) {
            //pthread_cancel(uplinkRecvThreadWorker);
        }

        uplinkRecvThreadId = pthread_create(&uplinkRecvThreadWorker, 0, uplink_recv_thread, NULL);
        if(uplinkRecvThreadId != 0) {
            perror("uplink_recv_thread");
            exit(1);
        }

        pthread_detach(uplinkRecvThreadWorker);
    }
}

/* receive proxy data, then send to client */
void *uplink_recv_thread(void *arg)
{
    
    int tmp_proxy_fd = last_proxy_fd;
    while(!pglobal->stop) {
        int recv_length = read(tmp_proxy_fd, uplinkBuf, sizeof(uplinkBuf));
        if (recv_length > 0) {

            if (last_client_fd > 0) {
                //send data
                if(send(last_client_fd, uplinkBuf, recv_length, 0) < 0) {
                    DBG("uplink send cmd to client error! \n");
                }
            }
        } else {
            //break;
            DBG("proxy disconnect, waiting.....\n");
            break;
        }
    }

}

void *downlink_thread(void *arg) {
int client_fd;//this socket id of android client

    DBG("downlink_thread starting .... server socket id: %d\n", downlink_fd);
    while(!pglobal->stop) {
        

        struct sockaddr_in client_socket;
        socklen_t length = sizeof(client_socket);
 
        if (-1 == (client_fd = accept(downlink_fd, (struct sockaddr*)&client_socket, &length))){
            perror("downlink socket accept");
            continue;
        }
        
        char buf_ip[INET_ADDRSTRLEN];
        memset(buf_ip, '\0', sizeof(buf_ip));
        inet_ntop(AF_INET,&client_socket.sin_addr, buf_ip, sizeof(buf_ip));
        DBG("A client connect! client id:%d, ip:%s \n", client_fd, buf_ip);
        
        last_client_fd = client_fd;
        
        void *res;
        if (downlinkRecvThreadId == 0) {
            //pthread_cancel(downlinkRecvThreadWorker);
            //pthread_join(downlinkRecvThreadWorker, &res);
        }

        downlinkRecvThreadId = pthread_create(&downlinkRecvThreadWorker, 0, downlink_recv_thread, NULL);
        if(downlinkRecvThreadId != 0) {
            perror("downlink_recv_thread");
            exit(1);
        }

        pthread_detach(downlinkRecvThreadWorker);
    }

}
/* receive proxy data, then send to client */
void *downlink_recv_thread(void *arg)
{
    int tmp_client_fd = last_client_fd;
    while(!pglobal->stop) {
        int recv_length = read(tmp_client_fd, downlinkBuf, sizeof(downlinkBuf));
        DBG("downlink_thread rec:%d, proxy fd%d! \n", recv_length, last_proxy_fd);
        if (recv_length > 0) {
            if (last_proxy_fd > 0) {
                //send data
                if(send(last_proxy_fd, downlinkBuf, recv_length, 0) < 0) {
                    DBG("downlink send cmd to proxy error! \n");
                }
            }
        } else {
            //break;
            DBG("client disconnect, waiting.....\n");
            break;
        }
    }
}

int input_cmd(int plugin, int command_id, int group, int value, char* sValue)
{ 
    int res;
    int i;
    DBG("Requested cmd (id: %d) for the %d plugin. Group: %d value: %d string: %s \n", command_id, plugin, group, value, sValue);
    return 0;
}

int findstr(char* buf, int len, char* str, int strLen) {
    if (buf != NULL && len > strLen && str != NULL && strlen(str) == strLen) {
        int i;
        for (i = 0; i < len -strLen + 1; i++) {
            /*if (buf[i] == str[0] && buf[i+1] == str[1]
                && buf[i+2] == str[2] && buf[i+3] == str[3]) {
                return i;
            }*/
            int match = 0;
            for (int j = 0; j < strLen; j++) {
                if (buf[i + j] != str[j]) {
                    break;
                } else {
                    match ++;
                }
            }
            if (match == strLen) return i;
        }
    } 
    return -1;
}

int arrayCopy(char* dest, int dest_pos, char* src, int src_pos, int len) {
    if (dest_pos < 0 || len <= 0) {
        return -1;//error
    }
    int i = 0;
    int j = src_pos;
    for (i = dest_pos; i < dest_pos + len; i++) {
        dest[i] = src[j];
        j++;
    }
    // DBG("arrayCopy returned! len:%d\n", len);
    return len;
}KKK
    
