package com.hbk619.jetbrainsscreenreader.debugging

import com.hbk619.jetbrainsscreenreader.sound.Player
import com.hbk619.jetbrainsscreenreader.sound.Sound
import com.intellij.openapi.progress.BackgroundTaskQueue
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointListener

class BreakpointsListener : XBreakpointListener<XBreakpoint<*>> {
    private val queue = BackgroundTaskQueue(null, "Playing sound")

    override fun breakpointAdded(breakpoint: XBreakpoint<*>) {
        playSound(Sound.BREAKPOINT_ADDED)
    }

    override fun breakpointRemoved(breakpoint: XBreakpoint<*>) {
        playSound(Sound.BREAKPOINT_REMOVED)
    }

    private fun playSound(sound: Sound) {
        queue.run(Player(null, "Playing sound", sound))
    }
}