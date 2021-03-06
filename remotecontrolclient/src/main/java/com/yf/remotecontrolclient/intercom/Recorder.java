package com.yf.remotecontrolclient.intercom;

import android.media.AudioRecord;
import android.os.Handler;

import com.yf.remotecontrolclient.service.imp.IntercomServiceImpl;
import com.yuanfang.intercom.data.AudioData;
import com.yuanfang.intercom.job.JobHandler;
import com.yuanfang.intercom.util.AudioDataUtil;
import com.yuanfang.intercom.util.Constants;

/**
 * 音频录制数据格式ENCODING_PCM_16BIT，返回数据类型为short[]
 *
 * @author yanghao1
 */
public class Recorder extends JobHandler {
    private static final String TAG = "Recorder";
    private AudioRecord audioRecord;
    // 音频大小
    private int inAudioBufferSize;
    // 录音标志
    private boolean isRecording = false;

    public Recorder(Handler handler) {
        super(handler);
        init();
    }

    private synchronized void init() {
        // 获取音频数据缓冲段大小
        inAudioBufferSize = AudioRecord.getMinBufferSize(
                Constants.sampleRateInHz, Constants.inputChannelConfig, Constants.audioFormat);
        // 初始化音频录制
        audioRecord = new AudioRecord(Constants.audioSource,
                Constants.sampleRateInHz, Constants.inputChannelConfig, Constants.audioFormat, inAudioBufferSize);
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    @Override
    public void run() {
        while (isRecording) {
            if (audioRecord == null)
                init();
            if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
                audioRecord.startRecording();
            }
            // 实例化音频数据缓冲
            short[] rawData = new short[inAudioBufferSize];
            audioRecord.read(rawData, 0, inAudioBufferSize);
            AudioData audioData = new AudioData(rawData);

            audioData.setEncodedData(AudioDataUtil.raw2spx(audioData.getRawData()));
            // 通过MINA，发送数据
            try {
                IntercomServiceImpl.get().send(audioData.getEncodedData());
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void free() {
        // 释放音频录制资源
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        AudioDataUtil.free();
    }
}
