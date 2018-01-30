package com.xiaomakj.bdvoice.play;

import android.os.Handler;
import android.os.Message;

import com.xiaomakj.bdvoice.common.Logger;
import com.xiaomakj.bdvoice.complex.RecogResult;


/**
 * Created by fujiayi on 2017/11/3.
 */

public class AlarmListener implements IRecogListener {

    private Handler handler;


    public AlarmListener(Handler handler) {
        this.handler = handler;
    }


    /**
     * ASR_START 输入事件调用后，引擎准备完毕
     */
    @Override
    public void onAsrReady() {

    }

    /**
     * onAsrReady后检查到用户开始说话
     */
    @Override
    public void onAsrBegin() {

    }

    /**
     * 检查到用户开始说话停止，或者ASR_STOP 输入事件调用后，
     */
    @Override
    public void onAsrEnd() {

    }

    /**
     * onAsrBegin 后 随着用户的说话，返回的临时结果
     *
     * @param results     可能返回多个结果，请取第一个结果
     * @param recogResult 完整的结果
     */
    @Override
    public void onAsrPartialResult(String[] results, RecogResult recogResult) {

    }

    /**
     * 最终的识别结果
     *
     * @param results     可能返回多个结果，请取第一个结果
     * @param recogResult 完整的结果
     */
    @Override
    public void onAsrFinalResult(String[] results, RecogResult recogResult) {
        sendMessage(results[0], 901);
    }

    @Override
    public void onAsrFinish(RecogResult recogResult) {

    }

    @Override
    public void onAsrFinishError(int errorCode, int subErrorCode, String errorMessage, String descMessage,
                                 RecogResult recogResult) {
        sendMessage(recogResult.getOrigalJson(), -801);
    }

    /**
     * 长语音识别结束
     */
    @Override
    public void onAsrLongFinish() {

    }

    @Override
    public void onAsrVolume(int volumePercent, int volume) {

    }

    @Override
    public void onAsrAudio(byte[] data, int offset, int length) {

    }

    @Override
    public void onAsrExit() {
        sendMessage("", 900);
    }

    @Override
    public void onAsrOnlineNluResult(String nluResult) {

    }

    @Override
    public void onOfflineLoaded() {

    }

    @Override
    public void onOfflineUnLoaded() {

    }

    private void sendMessage(String message, int what) {
        Message msg = handler.obtainMessage();
        msg.what = what;
        msg.obj = message;
        handler.sendMessage(msg);
    }

    private void showMessage(String msg, boolean isError) {
        if (isError) {
            Logger.error(msg);
        } else {
            Logger.info(msg);
        }
    }
}
