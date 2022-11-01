package com.example.audiorecorderandimagepicker.audiorecorder

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import com.example.audiorecorderandimagepicker.R

open class AudioRecordButton : AppCompatImageView, OnTouchListener, View.OnClickListener {

    // if you want to click the button (in case if you want to make the record button a Send Button for example.
    //recordButton.isEnableRecording=false
    var isEnableRecording = true
    var onRecordButtonClickListener: OnRecordButtonClickListener? = null
    private var scaleAnim: ScaleAnimation? = null

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AudioRecordButton)
            val imageResource = typedArray.getResourceId(R.styleable.AudioRecordButton_mic_icon, -1)
            if (imageResource != -1) {
                setTheImageResource(imageResource)
            }
            typedArray.recycle()
        }
        scaleAnim = ScaleAnimation(this)
        setOnTouchListener(this)
        setOnClickListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setClip(this)
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (isEnableRecording) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> onRecordButtonClickListener?.onClickStart(
                    v as AudioRecordButton,
                    event
                )
                MotionEvent.ACTION_MOVE -> {
                }
                MotionEvent.ACTION_UP -> onRecordButtonClickListener?.onClickEnd(
                    v as AudioRecordButton,
                    event
                )
            }
        }
        return isEnableRecording
    }

    override fun onClick(v: View?) {
        if (!isEnableRecording) onRecordButtonClickListener!!.onClick(v)
    }

    fun setClip(v: View) {
        if (v.parent == null) {
            return
        }
        if (v is ViewGroup) {
            v.clipChildren = false
            v.clipToPadding = false
        }
        if (v.parent is View) {
            setClip(v.parent as View)
        }
    }

    private fun setTheImageResource(imageResource: Int) {
        val image = AppCompatResources.getDrawable(context, imageResource)
        setImageDrawable(image)
    }

    fun startScale() {
        scaleAnim!!.start()
    }

    fun stopScale() {
        scaleAnim!!.stop()
    }

    fun registerOnRecordButtonClickListener(onRecordButtonClickListener: OnRecordButtonClickListener?) {
        this.onRecordButtonClickListener = onRecordButtonClickListener
    }

    interface OnRecordButtonClickListener {
        fun onClickStart(audioRecordButton: AudioRecordButton, event: MotionEvent?)
        fun onClickEnd(audioRecordButton: AudioRecordButton, event: MotionEvent?)
        fun onClick(v: View?) {}
    }
}