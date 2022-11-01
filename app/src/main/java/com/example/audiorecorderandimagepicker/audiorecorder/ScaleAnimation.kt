package com.example.audiorecorderandimagepicker.audiorecorder

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

class ScaleAnimation(private var view: View?) {

    @SuppressLint("ObjectAnimatorBinding")
    fun start() {
        val set = AnimatorSet()
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.5f)//2.0f
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.5f)//2.0f
        set.duration = 150
        set.interpolator = AccelerateDecelerateInterpolator()
        set.playTogether(scaleY, scaleX)
        set.start()
    }


    @SuppressLint("ObjectAnimatorBinding")
    fun stop() {
        val set = AnimatorSet()
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f)
        //scaleY.setDuration(250);
        //scaleY.setInterpolator(new DecelerateInterpolator());
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f)
        //scaleX.setDuration(250);
        //scaleX.setInterpolator(new DecelerateInterpolator());
        set.duration = 150
        set.interpolator = AccelerateDecelerateInterpolator()
        set.playTogether(scaleY, scaleX)
        set.start()
    }
}