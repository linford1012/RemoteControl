package com.yf.remotecontrolclient.activity.baidu.asr;

import android.os.Handler;
import android.os.Message;

import com.yf.asr.recognization.RecogResult;
import com.yf.asr.recognization.StatusRecogListener;
import com.yf.remotecontrolclient.domain.Setplaystatus;
import com.yf.remotecontrolclient.domain.Setvolumeadd;
import com.yf.remotecontrolclient.service.MusicBusinessService;
import com.yf.remotecontrolclient.service.imp.MusicBusinessServiceImpl;

/**
 * Created by fujiayi on 2017/6/16.
 */

public class MessageStatusRecogListener extends StatusRecogListener {
    private Handler handler;

    private long speechEndTime;

    private boolean needTime = true;
    private MusicBusinessService musicBusinessService;

    public MessageStatusRecogListener(Handler handler) {
        this.handler = handler;
        musicBusinessService = new MusicBusinessServiceImpl();
    }


    @Override
    public void onAsrReady() {
        super.onAsrReady();
        sendStatusMessage("引擎就绪，可以开始说话。");
    }

    @Override
    public void onAsrBegin() {
        super.onAsrBegin();
        sendStatusMessage("检测到用户说话");
    }

    @Override
    public void onAsrEnd() {
        super.onAsrEnd();
        speechEndTime = System.currentTimeMillis();
        sendMessage("检测到用户说话结束");
    }

    @Override
    public void onAsrPartialResult(String[] results, RecogResult recogResult) {
        sendStatusMessage("临时识别结果，结果是“" + results[0] + "”；原始json：" + recogResult.getOrigalJson());
        super.onAsrPartialResult(results, recogResult);
    }

    @Override
    public void onAsrFinalResult(String[] results, RecogResult recogResult) {
        super.onAsrFinalResult(results, recogResult);
        String message = "识别结束，结果是”" + results[0]+"”";
        String mr=results[0];
        if (mr.contains("上一首")||mr.contains("上一曲")){
            Setplaystatus setplaystatus = new Setplaystatus();
            setplaystatus.setCmd("BSsetplaystatus");
            setplaystatus.setStatus("previous");
            musicBusinessService.sendBssetplaystatus(setplaystatus);
        } else if (mr.contains("下一首")||mr.contains("下一曲")){
            Setplaystatus setplaystatus = new Setplaystatus();
            setplaystatus.setCmd("BSsetplaystatus");
            setplaystatus.setStatus("next");
            musicBusinessService.sendBssetplaystatus(setplaystatus);
        } else if (mr.contains("播放")){
            Setplaystatus setplaystatus1 = new Setplaystatus();
            setplaystatus1.setCmd("BSsetplaystatus");
            setplaystatus1.setStatus("start");
            musicBusinessService.sendBssetplaystatus(setplaystatus1);
        }else if(mr.contains("暂停")){
            Setplaystatus setplaystatus1 = new Setplaystatus();
            setplaystatus1.setCmd("BSsetplaystatus");
            setplaystatus1.setStatus("pause");
            musicBusinessService.sendBssetplaystatus(setplaystatus1);
        } else if (mr.contains("调小音量")||mr.contains("调少音量")||mr.contains("减小音量")||
                mr.contains("减少音量")||mr.contains("减音量")){
            Setvolumeadd setvolumeadd = new Setvolumeadd();
            setvolumeadd.setCmd("BSsetvolumeadd");
            setvolumeadd.setValume("-");
            musicBusinessService.sendBssetvolumeadd(setvolumeadd);
        } else if (mr.contains("调大音量")||mr.contains("增加音量")||mr.contains("加大音量")||
                mr.contains("加音量")){
            Setvolumeadd setvolumeadd1 = new Setvolumeadd();
            setvolumeadd1.setCmd("BSsetvolumeadd");
            setvolumeadd1.setValume("+");
            musicBusinessService.sendBssetvolumeadd(setvolumeadd1);
        }

        sendStatusMessage(message + "“；原始json：" + recogResult.getOrigalJson());
        if (speechEndTime > 0) {
            long diffTime = System.currentTimeMillis() - speechEndTime;
            message += "；说话结束到识别结束耗时【" + diffTime + "ms】";
        }
        speechEndTime = 0;
        sendMessage(message, status, true);
    }

    @Override
    public void onAsrFinishError(int errorCode, String errorMessage, String descMessage) {
        super.onAsrFinishError(errorCode, errorMessage, descMessage);
        String message = "识别错误, 错误码：" + errorCode;
        sendStatusMessage(message + "；错误消息:" + errorMessage + "；描述信息：" + descMessage);
        if (speechEndTime > 0) {
            long diffTime = System.currentTimeMillis() - speechEndTime;
            message += "。说话结束到识别结束耗时【" + diffTime + "ms】";
        }
        speechEndTime = 0;
        sendMessage(message, status, true);
        speechEndTime = 0;
    }

    @Override
    public void onAsrOnlineNluResult(String nluResult) {
        super.onAsrOnlineNluResult(nluResult);
        if (!nluResult.isEmpty()) {
            sendStatusMessage("原始语义识别结果json：" + nluResult);
        }
    }

    @Override
    public void onAsrFinish(RecogResult recogResult) {
        super.onAsrFinish(recogResult);
        sendStatusMessage("识别一段话结束。如果是长语音的情况会继续识别下段话。");

    }

    /**
     * 长语音识别结束
     */
    @Override
    public void onAsrLongFinish() {
        super.onAsrLongFinish();
        sendStatusMessage("长语音识别结束。");
    }


    /**
     * 使用离线语法时，有该回调说明离线语法资源加载成功
     */
    @Override
    public void onOfflineLoaded() {
        sendStatusMessage("【重要】离线资源加载成功。没有此回调可能离线语法功能不能使用。");
    }
    /**
     * 使用离线语法时，有该回调说明离线语法资源加载成功
     */
    @Override
    public void onOfflineUnLoaded() {
        sendStatusMessage(" 离线资源卸载成功。");
    }

    @Override
    public void onAsrExit() {
        super.onAsrExit();
        sendStatusMessage("识别引擎结束并空闲中");
    }

    private void sendMessage(String message) {
        sendMessage(message, WHAT_MESSAGE_STATUS);
    }

    private void sendStatusMessage(String message) {
        sendMessage(message, status);
    }

    private void sendMessage(String message, int what) {
        sendMessage(message, what, false);
    }


    private void sendMessage(String message, int what, boolean highlight) {
        if (needTime && what != STATUS_FINISHED) {
            message += "  ;time=" + System.currentTimeMillis();
        }
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = status;
        if (highlight) {
            msg.arg2 = 1;
        }
        msg.obj = message + "\n";
        handler.sendMessage(msg);
    }
}
