package com.hbk619.intellisay.mock

abstract class Mock {
    private val callList = ArrayList<Call>()

    fun addCall(call: Call) {
        callList.add(call)
    }

    open fun reset() {
        callList.clear()
    }
    val calls: ArrayList<Call>
        get() = callList
}