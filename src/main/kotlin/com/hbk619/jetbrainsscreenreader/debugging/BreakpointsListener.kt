package com.hbk619.jetbrainsscreenreader.debugging

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointListener
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

class BreakpointsListener : XBreakpointListener<XBreakpoint<*>> {
    private val beepLock = Any()
    private val log: Logger = Logger.getInstance(BreakpointsListener::class.java)

    override fun breakpointAdded(breakpoint: XBreakpoint<*>) {
        playSound("Basso.aiff")
    }

    override fun breakpointRemoved(breakpoint: XBreakpoint<*>) {
        playSound("Blow.aiff")
    }

    private fun playSound(soundName: String) {
        ApplicationManager.getApplication().executeOnPooledThread {
            synchronized(beepLock) {
                try {
                    val path = BreakpointsListener::class.java.getResource("sounds/" + soundName)
                    val audioInputStream: AudioInputStream = AudioSystem.getAudioInputStream(path)
                    val clip = AudioSystem.getClip()
                    clip.open(audioInputStream)
                    clip.start()
                } catch (e: Exception) {
                    log.error(e)
                }
            }
        }
    }

}