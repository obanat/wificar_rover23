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
#define LOG_BUF_SIZE (20)

#define ROLE_CLIENT (0x10)
#define ROLE_PROXY (0x11)

#define OP_REG_REQ (32)
#define OP_REG_RESP (33)

#define SERVER_PORT (19092)

#define STATE_INIT (0x20)
#define STATE_CLIENT_CONNECT (0x21)
#define STATE_CLIENT_REGED (0x22)
#define STATE_PROXY_REGED (0x23)

/* private functions and variables to this plugin */
static pthread_t threadWorker;

static int clientIp = 0;
static int clientPort = 0;//use by proxy to make p2p connection

static int downlinkRecvThreadId = -1;

static globals     *pglobal;

void *server_thread(void *);//pthread
void *recv_thread(void *);

void thread_cleanup(void *);





static int plugin_number;



static int link_fd = 0;
static int mState = 0;//
static int last_fd = -1;//last connected android clent fd

//support only 2 socket client, one as proxy, one as client
static int fd1 = -1;
static int fd2 = -1;//
static int recvThreadId1 = -1;
static int recvThreadId2 = -1;

static pthread_t recvThreadWorker1;
static pthread_t recvThreadWorker2;

static char cmdBuf[CMD_BUF_SIZE] = {0};
/* global variables for this plugin */
int input_cmd(int plugin, int command_id, int group, int value, char* sValue);
void saveInt2bytes(unsigned char* data, unsigned int value);
unsigned int bytes2int(char* data);
unsigned int bytes2short(char* data);
int arrayCopy(char* dest, int dest_pos, char* src, int src_pos, int len);

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
    pthread_cancel(threadWorker);
    pthread_cancel(recvThreadWorker1);
    pthread_cancel(recvThreadWorker2);
    return 0;
}


int input_run(int id)
{
    pglobal->in[id].buf = NULL;

    DBG("input_run starting >>>>>>>>>>>>>>>>>> id:%d \n", id);
    mState = STATE_INIT;
    //for uplink
    link_fd = socket(AF_INET,SOCK_STREAM, 0);
    int bufSize = CMD_BUF_SIZE;
    int optlen = sizeof(bufSize);
    if (setsockopt(link_fd, SOL_SOCKET, SO_RCVBUF, &bufSize, optlen) < 0) {
        perror("set uplink sockopt");
        exit(1);
    }

    //make socket reuse, to avoid "address in use" error
    int option = 1;
    setsockopt(link_fd, SOL_SOCKET,(SO_REUSEPORT | SO_REUSEADDR | SO_DEBUG), (char*) &option,sizeof(option));

    struct sockaddr_in server_sockaddr;
    bzero(&server_sockaddr, sizeof(server_sockaddr));
    server_sockaddr.sin_family = AF_INET;//IPv4
    server_sockaddr.sin_port = htons(SERVER_PORT);
    server_sockaddr.sin_addr.s_addr = INADDR_ANY;


    ///
    if(bind(link_fd,(struct sockaddr *)&server_sockaddr,sizeof(server_sockaddr))==-1) {
        perror("bind uplink socket");
        exit(1);
    }
        
    DBG("bind server socket success, start listen!\n");
 
    ///listen
    if(listen(link_fd,QUEUE) == -1) {
        perror("listen server socket");
        exit(1);
    }

    if(pthread_create(&threadWorker, 0, server_thread, NULL) != 0) {
        perror("server_thread create error!");
        exit(1);
    }

    pthread_detach(threadWorker);

    DBG("input_run ..finish\n");
    return 0;
}

/*this is server thread*/
void *server_thread(void *arg)
{
    int newfd;//this socket id of android client

    DBG("server_thread starting .... server socket id: %d\n", link_fd);
    while(!pglobal->stop) {
        

        struct sockaddr_in client_socket;
        socklen_t length = sizeof(client_socket);
 
        if (-1 == (newfd = accept(link_fd, (struct sockaddr*)&client_socket, &length))){
            perror("uplind socket accept");
            continue;
        }

        //a simple logic to prepare 2 fds
        //TODO: store multi clients in list & add mac check for couple client;
        if (last_fd == -1) {
            fd1 = newfd;
            recvThreadId1 = pthread_create(&recvThreadWorker1, 0, recv_thread, &fd1);
            pthread_detach(recvThreadWorker1);
            DBG("new socket fd:%d, recvThreadId:%d \n", fd1, recvThreadId1);
        } else if ( last_fd == fd1) {
            fd2 = newfd;
            recvThreadId2 = pthread_create(&recvThreadWorker2, 0, recv_thread, &fd2);
            pthread_detach(recvThreadWorker2);
            DBG("new socket fd:%d, recvThreadId:%d \n", fd2, recvThreadId2);
        } else {
            fd1 = newfd;
            recvThreadId1 = pthread_create(&recvThreadWorker1, 0, recv_thread, &fd1);
            pthread_detach(recvThreadWorker1);
            DBG("new socket fd:%d, recvThreadId:%d \n", fd1, recvThreadId1);
        }
        last_fd = newfd;

        char buf_ip[INET_ADDRSTRLEN];
        memset(buf_ip, '\0', sizeof(buf_ip));
        inet_ntop(AF_INET,&client_socket.sin_addr, buf_ip, sizeof(buf_ip));
        DBG("A client socket connect! socket id:%d, ip:%s, port:%d\n", newfd, buf_ip, client_socket.sin_port);

    }
}

/* receive proxy data, then send to client */
void *recv_thread(void *arg)
{
    
    int clientFd = *((int *)arg);
    struct sockaddr_in client_socket;
    socklen_t length = sizeof(client_socket);
    while(!pglobal->stop) {
        int recv_length = read(clientFd, cmdBuf, sizeof(cmdBuf));
        if (recv_length > 0) {

            int op = bytes2short(cmdBuf + 4);
            int len = bytes2int(cmdBuf + 15);
            int role = findstr(cmdBuf, recv_length, "MO_C", 4) == 0 ? ROLE_CLIENT : ROLE_PROXY;
            DBG("socket data recved! len:%d, op:%d, role:%d\n", len, op, role);
            if (len == 24 && op == OP_REG_REQ && role == ROLE_CLIENT) {
                //register signal from client
                int ipv4 = bytes2int(cmdBuf + 23+16);
                int port = bytes2int(cmdBuf + 23+16+4);
                
                char buf_ip[INET_ADDRSTRLEN];
                memset(buf_ip, '\0', sizeof(buf_ip));
                inet_ntop(AF_INET, &ipv4, buf_ip, sizeof(buf_ip));
                DBG("recv client reg ip:%s, port:%d  \n", buf_ip, port);
                mState = STATE_CLIENT_CONNECT;

                getpeername(clientFd, (struct sockaddr *)&client_socket, (socklen_t*)&length); //sockfd
                if (ipv4 == client_socket.sin_addr.s_addr) {
                    clientIp = ipv4;
                    clientPort = client_socket.sin_port;
                    mState = STATE_CLIENT_REGED;
                    DBG("client has be registered, ip&port saved, waiting proxy reg.... \n");
                }
            } else if (op == OP_REG_REQ && role == ROLE_PROXY) {
                //register signal from proxy
                DBG("recv register signal from proxy, mState:%d \n",mState);

                char buf[31] = {0};//total len is 31(23+4+4)
                buf[0] = 'M';buf[1] = 'O';buf[2] = '_';buf[3] = 'U';//means cloud
                saveshort2bytes(buf+4, OP_REG_RESP);//op
                saveInt2bytes(buf+15, 4+4);//length 0f data( ip & port)
                saveInt2bytes(buf+23, clientIp);//ipv4
                saveInt2bytes(buf+27, clientPort);//ipv4

                if(mState == STATE_CLIENT_REGED ) {
                    if (send(clientFd, buf, 31, 0) < 0) {
                        DBG("send OK response to proxy error! \n");
                    }
                    mState == STATE_PROXY_REGED;
                    DBG("send OK response to proxy successfully, wait next! \n");
                }
            } else {
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
}

void saveInt2bytes(unsigned char* data, unsigned int value) {
    data[3] = (unsigned char)((value >> 24) & 0xFF);
    data[2] = (unsigned char)((value >> 16) & 0xFF);
    data[1] = (unsigned char)((value >> 8) & 0xFF);
    data[0] = (unsigned char)(value & 0xFF);
}

void saveshort2bytes(unsigned char* data, unsigned int value) {
    data[0] = (unsigned char)(value & 0xFF);
    data[1] = (unsigned char)((value >> 8) & 0xFF);
}

unsigned int bytes2int(char* data) {
    return (unsigned int)((unsigned char)data[0] | (unsigned char)data[1]<<8 | (unsigned char)data[2]<<16 | (unsigned char)data[3]<<24);
}

unsigned int bytes2short(char* data) {
    return (unsigned int)((unsigned char)data[0] | (unsigned char)data[1]<<8 );
}