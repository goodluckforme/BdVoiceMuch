package com.xiaomakj.bdvoice.common;

import android.app.Application;

import com.xiaomakj.bdvoice.recognition.DigitalDialogInput;

/**
 * Created by MaQi on 2018/1/31.
 */

public class App extends Application {
    private DigitalDialogInput digitalDialogInput;


    public DigitalDialogInput getDigitalDialogInput() {
        return digitalDialogInput;
    }

    public void setDigitalDialogInput(DigitalDialogInput digitalDialogInput) {
        this.digitalDialogInput = digitalDialogInput;
    }
}
