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
#include "blowfish.h"
#include "proxyState.h"

#define INPUT_PLUGIN_NAME "FILE input plugin"
#define QUEUE   10
#define WIFICAR_MEDIA "WIFI_MEDIA"
#define WIFICAR_MEDIA_LEN 10
#define RECV_BUF_SIZE 1024//(8*1024)
#define JPEG_BUF_SIZE (64*1024)
#define LOG_BUF_SIZE (20)
#define CMD_BUF_SIZE (1024)

/* private functions and variables to this plugin */
static globals     *pglobal;


static int plugin_number;

struct proxy_state cloudProxy;
struct proxy_state wificarProxy;

static char s_jpegBuf[JPEG_BUF_SIZE];

//static char g_recvBuf[1024];
//static char g_jpegBuf[32*1024];

void parseCloudCommand(char* data, int len);//receiver of cloud socket
void responseWificarCommand(char* data, int len);//receiver of wificar socket
unsigned int bytes2int(char* data);

/* global variables for this plugin */
int input_cmd(int plugin, int command_id, int group, int value, char* sValue);

/*** plugin interface functions ***/
int input_init(input_parameter *param, int id)
{
    int i;
    plugin_number = id;

    param->argv[0] = INPUT_PLUGIN_NAME;

    /* show all parameters for DBG purposes */
    pglobal = param->global;



    param->global->in[id].name = malloc((strlen(INPUT_PLUGIN_NAME) + 1) * sizeof(char));
    sprintf(param->global->in[id].name, INPUT_PLUGIN_NAME);

    initCloudProxy(&cloudProxy);
    initWificarProxy(&wificarProxy);
    DBG("input_init finish! \n");
    return 0;
}

void initCloudProxy(struct proxy_state * state) {
    state->hostname = strdup("cloud.obana.top");
    state->port = strdup("19090");
    
    state->recvBufSize = CMD_BUF_SIZE;
    state->recvBuf = malloc(CMD_BUF_SIZE * sizeof(char));
    
    state->sendBufSize = 0;
    //state->sendBuf = s_jpegBuf;//malloc(JPEG_BUF_SIZE * sizeof(char));
    
    state->should_stop = 0;
    
    state->on_data_received = parseCloudCommand;
    //state->send_data = ;
}

void initWificarProxy(struct proxy_state * state) {
    state->hostname = strdup("192.168.1.100");
    state->port = strdup("80");
    state->isIpAddr = 1;

    state->recvBufSize = CMD_BUF_SIZE;
    state->recvBuf = malloc(CMD_BUF_SIZE);
    
    state->sendBufSize = CMD_BUF_SIZE;
    state->sendBuf = malloc(CMD_BUF_SIZE);
    state->sendLen = 0;
    state->should_stop = 0;
    
    state->on_data_received = responseWificarCommand;
    //state->send_data = ;
}

int input_stop(int id)
{
    //DBG("will cancel input thread\n");

    return 0;
}


int input_run(int id)
{
    DBG("input_run start...id:%d \n", id);
    pglobal->in[id].buf = NULL;
    //connect_and_stream(&cloudProxy);
    //send_request_and_process_response(&cloudProxy);
    
    //connect wifi car
    connect_and_stream(&wificarProxy);
    send_request_and_process_response(&wificarProxy);


    int len = prepareCmdBuffer(wificarProxy.sendBuf, 100, 0,0,NULL);//login cmd to wificar
    socketSend(&wificarProxy, wificarProxy.sendBuf, len);
    //pthread_detach(recvWorker);
    DBG("input_run finished!.....id:%d \n", id);
    return 0;
}

#define max(a,b) ((a) > (b) ? (a) : (b))
#define min(a,b) ((a) < (b) ? (a) : (b))
//receive thread for client
void parseCloudCommand(char* data, int len)
{
    DBG("parseCloudCommand data len:%d \n", len);
    if (len > 10) {//test code
        char buf[50];
        int len = prepareCmdBuffer(buf, 50, 4,0,NULL);//enable video
        socketSend(&wificarProxy, buf, len);
        DBG("send video enable, and it should receive VideoStartResp op:5\n");
    }
}

//parse data recv from wificar
#define CMD_BUFFER_LEN 100
BLOWFISH_CTX ctx;
void responseWificarCommand(char* recvBuf, int len)
{
    DBG("responseWificarCommand received data len:%d\n", len);
    int op = parseOp(recvBuf, len);
    char* buf = wificarProxy.sendBuf;
    DBG("responseWificarCommand op:%d \n", op);

    switch (op) {
        case 1:
        {
            //login resp => login veriry
            char data[16] = {0};
            unsigned int L1, R1,L2,R2;
            L1 = bytes2int(recvBuf+66);
            R1 = bytes2int(recvBuf+70);
            L2 = bytes2int(recvBuf+74);
            R2 = bytes2int(recvBuf+78);
            //L1 = 1624913772;R1=861471380;L2=1318632752;R2=1248712230;
            DBG("login resp 1:2:3:4----%u:%u:%u;%u \n", L1, R1, L2, R2);
            uint8_t key[35] = {'A','C','1','3',':'
                                ,'0','0','E','0','4','C','0','8','2','3','8','2'
                                ,'-', 's', 'a','v','e','-','p','r','i','v','a','t','e'
                                ,':','A','C','1','3'};
            Blowfish_Init(&ctx, key, 35);
            Blowfish_Encrypt(&ctx, &L1, &R1);
            Blowfish_Encrypt(&ctx, &L2, &R2);
            DBG("login resp 1:2:3:4----%u:%u:%u;%u \n", L1, R1, L2, R2);
            
            char tmp[4] = {0};
            saveInt2bytes(tmp, L1);
            memcpy(data, tmp, 4);
            
            saveInt2bytes(tmp, R1);
            memcpy(data + 4, tmp, 4);
            
            saveInt2bytes(tmp, L2);
            memcpy(data + 8, tmp, 4);
            
            saveInt2bytes(tmp, R2);
            memcpy(data + 12, tmp, 4);
            
            int cmdLen = prepareCmdBuffer(buf, CMD_BUFFER_LEN, 2, 16, data);
            socketSend(&wificarProxy, buf, cmdLen);
            break;
        }
        case 3:
            //login veriry => enable video
            break;
        case 5:
            //start media;
            break;
        default :
            break;
    }
}

int parseOp(char* recvBuf, int len) {
    char tmp[2] = {0};
    tmp[0] = recvBuf[4];tmp[1] = recvBuf[5];
    DBG("parseOp data 1:%d, 2:%d \n", tmp[0], tmp[1]);
    return (int)((unsigned char)tmp[0] | (unsigned char)tmp[1]<<8);
}
void enableVideo(int enable) {
    //send cmd to wifi car to enable video
}

void sendMoveCommand(int dir, int speed) {
    //send move command
}

int prepareCmdBuffer(char* buf, int len, int op, int dataLen, char* data) {
    int ret = -1;
    switch (op) {
        case 0:
            //login
            memset(buf, 0, sizeof(buf));
            buf[0] = 'M';buf[1] = 'O';buf[2] = '_';buf[3] = 'O';
            buf[4] = 0; //op
            buf[15] = 16; //data len
            ret = 32;
            break;
        case 2:
            //login veriry
            //memset(buf, 0, sizeof(buf));
            buf[0] = 'M';buf[1] = 'O';buf[2] = '_';buf[3] = 'O';
            buf[4] = 2;//op
            buf[15] = 16;
            memcpy(buf+16, data, 16);//data len should be 16
            ret = 32;
            break;
        case 4:
            //enable video
            memset(buf, 0, sizeof(buf));
            buf[0] = 'M';buf[1] = 'O';buf[2] = '_';buf[3] = 'O';
            buf[4] = 4;//op
            buf[15] = 4;//send 4 bytes(int)
            char tmp[4] = {0};
            saveInt2bytes(tmp, 1);
            memcpy(buf+16, tmp, 4);//means [1]
            break;
        case 10:
            //move
            break;
        default:
            break;
    }
    DBG("prepareCmdBuffer socket send data len:%d, op:%d \n", ret, op);
    return ret;
}

void socketSend(struct proxy_state* state, char* data, int len) {
    
    if (state && state->sockfd > 0) {
        DBG("socket wait to Send....  len:%d, id:%d \n", len, state->sockfd);

        if(state->sendLen > 0) {
            DBG("send cmd error! data exist");
        } else {
            state->sendLen = len;
        }
    }
}



int input_cmd(int plugin, int command_id, int group, int value, char* sValue)
{

    return 0;
}

int findstr(char* buf, int len, char* str) {
    if (buf != NULL && len > WIFICAR_MEDIA_LEN && str != NULL && strlen(str) == WIFICAR_MEDIA_LEN) {
        int i;
        for (i = 0; i < len -WIFICAR_MEDIA_LEN + 1; i++) {
            /*if (buf[i] == str[0] && buf[i+1] == str[1]
                && buf[i+2] == str[2] && buf[i+3] == str[3]) {
                return i;
            }*/
            int match = 0;
            int j;
            for (j = 0; j < WIFICAR_MEDIA_LEN; j++) {
                if (buf[i + j] != str[j]) {
                    break;
                } else {
                    match ++;
                }
            }
            if (match == WIFICAR_MEDIA_LEN) return i;
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
int BIG_SEC = 0;
unsigned int bytes2int(char* data) {
    if (BIG_SEC) {
        return (unsigned int)((unsigned char)data[3] 
                    | (unsigned char)data[2]<<8 
                    | (unsigned char)data[1]<<16 
                    | (unsigned char)data[0]<<24);
    } else {
        return (unsigned int)((unsigned char)data[0] | (unsigned char)data[1]<<8 | (unsigned char)data[2]<<16 | (unsigned char)data[3]<<24);
    }
}

void saveInt2bytes(unsigned char* data, unsigned int value) {
    if (BIG_SEC) {
        data[0] = (value >> 24) & 0xFF;
        data[1] = (value >> 16) & 0xFF;
        data[2] = (value >> 8) & 0xFF;
        data[3] = value & 0xFF;
    } else {
        data[3] = (unsigned char)((value >> 24) & 0xFF);
        data[2] = (unsigned char)((value >> 16) & 0xFF);
        data[1] = (unsigned char)((value >> 8) & 0xFF);
        data[0] = (unsigned char)(value & 0xFF);
    }
}
    