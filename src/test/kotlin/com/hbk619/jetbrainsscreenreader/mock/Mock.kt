package com.hbk619.jetbrainsscreenreader.mock

abstract class Mock {
    private val callList = ArrayList<Call>()

    fun addCall(call: Call) {
        callList.add(call)
    }

    fun reset() {
        callList.clear()
    }
    val calls: ArrayList<Call>
        get() = callList
}