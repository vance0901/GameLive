#include <jni.h>
#include <string>
#include "packt.h"
#include <stdarg.h>


extern "C" extern void Java_com_baiduar_panomap_PanoManager_nativePause(JNIEnv*, jobject );

Live *live = 0;

int sendVideo(jbyte *data, jint len, jlong tms);

int sendAudio(jbyte *data, jint len, jint type, jlong tms);

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_vance_gamelive_ScreenLive_connect(JNIEnv *env, jobject instance, jstring url_) {
    const char *url = env->GetStringUTFChars(url_, 0);
    // java: RTMP rtmp = new RTMP()
    // socket->url
    int ret = 0;
    do {
        //申请内存
        live = (Live *) malloc(sizeof(Live));
        //初始化
        memset(live, 0, sizeof(Live));
        live->rtmp = RTMP_Alloc();
        RTMP_Init(live->rtmp);
        // url: rtmp://xxxxx  abcdef
        ret = RTMP_SetupURL(live->rtmp, (char *) url);
        if (!ret) {
            break;
        }
        // 拉流播放、推流写出
        RTMP_EnableWrite(live->rtmp);
        ret = RTMP_Connect(live->rtmp, 0);
        if (!ret) {
            break;
        }
        ret = RTMP_ConnectStream(live->rtmp, 0);
    } while (0);
    env->ReleaseStringUTFChars(url_, url);
    return ret;
}

int sendPackage(RTMPPacket *packet) {
    int ret = RTMP_SendPacket(live->rtmp, packet, 0);
    RTMPPacket_Free(packet);
    free(packet);
    return ret;
}

// int: 0 false,非0就是true
int sendVideo(jbyte *data, jint len, jlong tms) {
    int ret = 0;
    // 0x00 00 00 01 nal(第1个字节低五位表示类型)
    if ((data[4] & 0x1f) == 7) {
        //sps ,mediacodec编码 sps与pps合并到一起输出
        // 0001 sps 0001 pps
        prepareVideo(data, len, live);
        ret = 1;
    } else {
        //关键帧
        if ((data[4] & 0x1f) == 5) {
            // 发送sps与pps
            RTMPPacket *packet = createVideoPackage(live);
            ret = sendPackage(packet);
            if (!ret) {
                return ret;
            }
        }
        RTMPPacket *packet = createVideoPackage(data, len, tms, live);
        ret = sendPackage(packet);
    }
    return ret;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_vance_gamelive_ScreenLive_disConnect(JNIEnv *env, jobject instance) {

    if (live) {
        if (live->rtmp) {
            RTMP_Close(live->rtmp);
            RTMP_Free(live->rtmp);
            live->rtmp = 0;
        }
        if (live->sps) {
//            new->delete
//          malloc -> free
            free(live->sps);
            live->sps = 0;
        }
        if (live->pps) {
            free(live->pps);
            live->pps = 0;
        }
        free(live);
        live = 0;
    }

}

int sendAudio(jbyte *data, jint len, jint type, jlong tms) {
    return sendPackage(createAudioPacket(data, len, type, tms, live));
}


extern "C"
JNIEXPORT jboolean
JNICALL
Java_com_vance_gamelive_ScreenLive_sendData(JNIEnv *env, jobject instance,
                                            jbyteArray data_,
                                            jint len, jint type, jlong tms) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    int ret = 1;
    if (type == 0) { // video
        ret = sendVideo(data, len, tms);
        LOGI("发送视频.....");
    } else {
        ret = sendAudio(data, len, type, tms);
        LOGI("发送声音.....");
    }
    env->ReleaseByteArrayElements(data_, data, 0);
    return ret;
}


