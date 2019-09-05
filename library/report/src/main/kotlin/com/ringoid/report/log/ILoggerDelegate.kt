package com.ringoid.report.log

interface ILoggerDelegate {

    fun breadcrumb(message: String, vararg data: Pair<String, String>)

    fun d(message: String, extras: List<Pair<String, String>>? = null)
    fun i(message: String, extras: List<Pair<String, String>>? = null)
    fun w(message: String, extras: List<Pair<String, String>>? = null)
    fun e(message: String, extras: List<Pair<String, String>>? = null)
    fun a(message: String, extras: List<Pair<String, String>>? = null)

    fun capture(e: Throwable, message: String? = null, level: ReportLevel = ReportLevel.ERROR,
                `object`: Any? = null, tag: String? = null, extras: List<Pair<String, String>>? = null)

    // ------------------------------------------
    fun setUser(currentUserId: String?)
    fun clear()
}
