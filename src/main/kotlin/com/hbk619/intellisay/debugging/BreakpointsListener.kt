package com.hbk619.intellisay.debugging

import com.hbk619.intellisay.sound.Sound
import com.hbk619.intellisay.sound.playSound
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointListener

class BreakpointsListener : XBreakpointListener<XBreakpoint<*>> {
    override fun breakpointAdded(breakpoint: XBreakpoint<*>) {
        play(Sound.BREAKPOINT_ADDED)
    }

    override fun breakpointRemoved(breakpoint: XBreakpoint<*>) {
        play(Sound.BREAKPOINT_REMOVED)
    }

    private fun play(sound: Sound) {
        playSound(null,"Playing sound", sound)
    }
}