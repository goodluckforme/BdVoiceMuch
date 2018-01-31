package com.xiaomakj.bdvoice.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.baidu.speech.EventManager
import com.baidu.speech.EventManagerFactory
import com.baidu.speech.asr.SpeechConstant
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy
import com.baidu.tts.client.SpeechSynthesizer
import com.baidu.tts.client.TtsMode
import com.banyue.huiwanjia.util.launchActivity
import com.networkbench.agent.impl.NBSAppAgent
import com.xiaomakj.bdvoice.R
import com.xiaomakj.bdvoice.common.App
import com.xiaomakj.bdvoice.common.FileUtil
import com.xiaomakj.bdvoice.common.InitConfig
import com.xiaomakj.bdvoice.common.Logger
import com.xiaomakj.bdvoice.complex.FileSaveListener
import com.xiaomakj.bdvoice.complex.RecogEventAdapter
import com.xiaomakj.bdvoice.complex.RecogResult
import com.xiaomakj.bdvoice.complex.StatusRecogListener
import com.xiaomakj.bdvoice.play.*
import com.xiaomakj.bdvoice.recognition.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.util.*


open class MainActivity : AppCompatActivity() {
    // 主控制类，所有合成控制方法从这个类开始
    protected var synthesizer: MySyntherizer? = null

    private var asr: EventManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        voicewave_view.setThemeStyle(BaiduASRDialogTheme.THEME_RED_DEEPBG)
//        progress.setTheme(BaiduASRDialogTheme.THEME_RED_DEEPBG)
        NBSAppAgent.setLicenseKey("d4e2a375c16d4c33855fd654ed20bb5a").withLocationServiceEnabled(true).start(this.applicationContext)
        initPermission()
        //initialTts_play()
        initialTts_save()
        //播放部分
        speak.onClick {
            speak()
        }
        pause.onClick {
            pause()
        }
        resume.onClick {
            resume()
        }
        stop.onClick {
            stop()
        }
        loadmodel.onClick {
            loadModel()
        }
        batch_speak.onClick {
            batchSpeak()
        }
        synthesize.onClick {
            synthesize("luyin1")
        }
        //合成部分
        //initialAsr()
        initialAsr_recog()
        start_record.onClick {
            //startAsr()
            launchActivity<BaiduASRDigitalDialog>(1112) { }
        }
        offline.onClick {
            toast("这个只是开启命令离线词 不存在纯粹的离线识别，并且目前没搞明白离线词的具体意义")
            myRecognizer?.release()
            enableOffline = !enableOffline
            initialAsr_recog()
            offline.text="开启命令离线词：$enableOffline"
        }
        offline.text="开启命令离线词：$enableOffline"
        stop_record.onClick {
            stop_record()
        }
        cancel_record.onClick {
            cancel_record()
        }
    }

    /*
     * 停止合成引擎。即停止播放，合成，清空内部合成队列。
     */
    private fun stop() {
        val result = synthesizer?.stop()
        checkResult(result ?: return, "stop")
    }

    /**
     * 暂停播放。仅调用speak后生效
     */
    private fun pause() {
        val result = synthesizer?.pause()
        checkResult(result ?: return, "pause")
    }

    /**
     * 继续播放。仅调用speak后生效，调用pause生效
     */
    private fun resume() {
        val result = synthesizer?.resume()
        checkResult(result ?: return, "resume")
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private fun initPermission() {
        val permissions = arrayOf<String>(Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_SETTINGS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE)
        val toApplyList = ArrayList<String>()
        for (perm in permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm)
                //进入到这里代表没有权限.
                toast("你有一些权限没有赋予APP")
            }
        }
        val tmpList = arrayOfNulls<String>(toApplyList.size)
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。

    }

    // TtsMode.MIX 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    protected var ttsMode = TtsMode.MIX

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_speech_female.data为离线男声模型；bd_etts_speech_female.data为离线女声模型
    protected var offlineVoice = OfflineResource.VOICE_FEMALE

    private val mainHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            val what = msg?.what
            when (what) {
                901 -> {
                    val result = "success : " + msg.obj.toString()
                    result_confirm?.text = result
                    speak()
                }
            }
            super.handleMessage(msg)
        }
    }

    /**
     * 初始化引擎，需要的参数均在InitConfig类里
     *
     *
     * DEMO中提供了3个SpeechSynthesizerListener的实现
     * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
     * UiMessageListener 在MessageListener的基础上，对handler发送消息，实现UI的文字更新
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
    val App_ID = 10764243
    val API_Key = "DqVhm2lXkf7si4fNV7TKQ2N9"
    val Secret_Key = "05ff2e8df512ad592db24c61363d196b"
     */
    protected fun initialTts_play() {
        LoggerProxy.printable(true)// 日志打印在logcat中
        // 设置初始化参数
        val params = getParams()
        // 设置初始化参数
        val listener = UiMessageListener(mainHandler) // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类

        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        val initConfig = InitConfig("10764243", "DqVhm2lXkf7si4fNV7TKQ2N9", "05ff2e8df512ad592db24c61363d196b",
                ttsMode, offlineVoice,
                params,
                listener)
        synthesizer = NonBlockSyntherizer(this, initConfig, mainHandler) // 此处可以改为MySyntherizer 了解调用过程
    }

    /**
     * 与SynthActivity相比，修改listener为FileSaveListener 可实现保存录音功能。
     * 获取的音频内容同speak方法播出的声音
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     */
    fun initialTts_save() {
        val tmpDir = FileUtil.createTmpDir(this)
        // 设置初始化参数
        val listener = FileSaveListener(mainHandler, tmpDir, FileSaveListener.SaveListener { ttsFile ->
            mainHandler.post {
                toast("录制完毕：ttsFile=${ttsFile?.absolutePath}")
            }
            Log.i("SaveListener", "录制完毕：ttsFile=${ttsFile?.absolutePath}")
            Log.i("SaveListener", "录制完毕：文件存在否=${ttsFile?.exists() ?: false}")
            val defaultParams = HashMap<String, Any>()
            defaultParams.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false)
            // defaultParams.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 0) // 长语音。默认普通识别，识别出音频文件的第一句话。
            // defaultParams.put(SpeechConstant.PID, 1537) // 中文输入法模型，有逗号
            // 请先使用如‘在线识别’界面测试和生成识别参数。 params同ActivityRecog类中myRecognizer.start(params)
            // =========================
            val params = HashMap<String, Any>(defaultParams)
            val speechTestCase = SpeechTestCase(ttsFile?.name, ttsFile?.absolutePath, params)
            synthesize_speak.onClick {
                begin(speechTestCase)
            }
        }) // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类

        val params = getParams()
        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        val initConfig = InitConfig("10764243", "DqVhm2lXkf7si4fNV7TKQ2N9", "05ff2e8df512ad592db24c61363d196b",
                ttsMode, offlineVoice,
                params,
                listener)
        synthesizer = MySyntherizer(this, initConfig, mainHandler) // 此处可以改为MySyntherizer 了解调用过程


        //识别本地音频
        asr = EventManagerFactory.create(this, "asr")
        val armlistener = AlarmListener(mainHandler)
        asr?.registerListener(RecogEventAdapter(armlistener))
    }

    private fun begin(testCase: SpeechTestCase) {
        val runningTestName = testCase.name
        val params = testCase.params
//        val str = this.javaClass.classLoader.getResourceAsStream(testCase.fileName)
        val str = FileInputStream(File(testCase.fileName))
        InFileStream.reset()
        InFileStream.setInputStream(str)
        params.put(SpeechConstant.IN_FILE,
                "#com.xiaomakj.bdvoice.play.InFileStream.create16kStream()")
        Log.i("MainActivity", "file:" + testCase.fileName)
        val json = JSONObject(params).toString()
        Log.i("MainActivity", runningTestName + " ," + json)
        asr?.send(SpeechConstant.ASR_START, json, null, 0, 0)
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected fun getParams(): Map<String, String> {
        val params = HashMap<String, String>()
        // 以下参数均为选填
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0") // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_VOLUME, "5") // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5")// 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5")// 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT)         // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        return params
    }

    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    private fun speak() {
        var text = mShowText.text.toString()
        //需要合成的文本text的长度不能超过1024个GBK字节。
        if (TextUtils.isEmpty(text)) {
            text = "欢迎使用百度语音合成SDK,百度语音为你提供支持。"
        }
        // 合成前可以修改参数：
        // Map<String, String> params = getParams()
        // synthesizer.setParams(params)
        val result = synthesizer?.speak(text)
        checkResult(result ?: return, "speak")
    }

    /**
     * 合成但是不播放，
     * 音频流保存为文件的方法可以参见SaveFileActivity及FileSaveListener
     */
    private fun synthesize(tag: String = "") {
        var text = mShowText.text.toString()
        //需要合成的文本text的长度不能超过1024个GBK字节。
        if (TextUtils.isEmpty(text)) {
            text = "欢迎使用百度语音合成SDK,百度语音为你提供支持。"
        }
        if (!tag.isNullOrEmpty()) {
            val result = synthesizer?.synthesize(text, tag)
            checkResult(result ?: return, "synthesize")
        } else {
            val result = synthesizer?.synthesize(text)
            checkResult(result ?: return, "synthesize")
        }
    }

    /**
     * 切换离线发音。注意需要添加额外的判断：引擎在合成时该方法不能调用
     */
    private fun loadModel() {
        if (offlineVoice == OfflineResource.VOICE_FEMALE) {
            offlineVoice = OfflineResource.VOICE_MALE
        } else {
            offlineVoice = OfflineResource.VOICE_FEMALE
        }
        val result = synthesizer?.loadModel(offlineVoice)
        checkResult(result ?: return, "loadModel")
    }


    /**
     * 批量播放
     */
    private fun batchSpeak() {
        mShowText.setText("")
        val texts = ArrayList<Pair<String, String>>()
        texts.add(Pair("开始批量播放，", "a0"))
        texts.add(Pair("123456，", "a1"))
        texts.add(Pair("欢迎使用百度语音，，，", "a2"))
        texts.add(Pair("重(chong2)量这个是多音字示例", "a3"))
        val result = synthesizer?.batchSpeak(texts)
        checkResult(result ?: return, "batchSpeak")
    }

    private fun checkResult(result: Int, method: String) {
        if (result != 0) {
            toast("error code :$result method:$method, 错误码文档:http://yuyin.baidu.com/docs/tts/122 ")
        }
    }

    override fun onDestroy() {
        synthesizer?.release()
        Log.i("MainActivity", "释放资源成功")
        myRecognizer?.release()
        Log.i("MainActivity", "onDestory")
        super.onDestroy()
    }


    private var myRecognizer: MyRecognizer? = null
    /*
     * 本Activity中是否需要调用离线命令词功能。根据此参数，判断是否需要调用SDK的ASR_KWS_LOAD_ENGINE事件
     */
    protected var enableOffline = false

    private fun initialAsr() {
        //val listener = TtsRecogListener(synthesizer)
        Log.i("MainActivity", "识别引擎初始化开始")
        myRecognizer = MyRecognizer(this, object : StatusRecogListener() {
            private val TAG = "TtsRecogListener"

            override fun onAsrFinalResult(results: Array<String>, recogResult: RecogResult) {
                super.onAsrFinalResult(results, recogResult)
                val msg = "识别成功：" + results[0]
                synthesizer?.speak(msg)
                Log.i(TAG, msg)
                result_record?.text = "${msg}"
                mShowText?.setText("${msg}")
            }

            override fun onAsrFinishError(errorCode: Int, subErrorCode: Int, errorMessage: String, descMessage: String, recogResult: RecogResult) {
                super.onAsrFinishError(errorCode, subErrorCode, errorMessage, descMessage, recogResult)
                val msg = "错误码是：" + errorCode
                synthesizer?.speak(msg)
                Log.i(TAG, msg)
                result_record?.text = "${msg}"
                mShowText?.setText("${msg}")
            }
        })
        Log.i("MainActivity", "识别引擎初始化结束")
    }

    private fun initialAsr_recog() {
        val listener = ChainRecogListener()
        /**
         * 有2个listner，一个是用户自己的业务逻辑，如MessageStatusRecogListener。另一个是UI对话框的。
         * 使用这个ChainRecogListener把两个listener和并在一起
         */
        // DigitalDialogInput 输入 ，MessageStatusRecogListener可替换为用户自己业务逻辑的listener
        listener.addListener(MessageStatusRecogListener(mainHandler))
        myRecognizer = MyRecognizer(this, listener)
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val params = OnlineRecogParams(this).fetch(sp) // params可以手动填入
        val input = DigitalDialogInput(myRecognizer, listener, params)
        // 在BaiduASRDialog中读取
        (applicationContext as App).digitalDialogInput = input
        if (enableOffline) {
            myRecognizer?.loadOfflineEngine(OfflineRecogParams.fetchOfflineParams())
        }
    }

    private fun startAsr() {
        val params = TreeMap<String, Any>()
        params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 2000)
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false)
        params.put(SpeechConstant.OUT_FILE, "/storage/emulated/0/baiduASR/outfile.pcm")
        params.put(SpeechConstant.ACCEPT_AUDIO_DATA, true)
        params.put(SpeechConstant.DISABLE_PUNCTUATION, false)
        myRecognizer?.start(params)
        Log.i("MainActivity", "请开始说话")
        Toast.makeText(this, "请开始说话", Toast.LENGTH_LONG).show()
    }


    private fun stop_record() {
        myRecognizer?.stop()
    }

    private fun cancel_record() {
        myRecognizer?.cancel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i("MainActivity", "requestCode" + requestCode)
        if (requestCode == 1112) {
            var message = "对话框的识别结果："
            if (resultCode == RESULT_OK) {
                val results = data?.getStringArrayListExtra("results")
                if (results != null && results.size > 0) {
                    message += results[0]
                }
            } else {
                message += "没有结果"
            }
            result_confirm.text = message
            Logger.info(message)
        }
    }
}
