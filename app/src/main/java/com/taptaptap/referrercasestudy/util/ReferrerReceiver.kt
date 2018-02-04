package com.taptaptap.referrercasestudy.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.net.URL
import java.net.URLDecoder

open class ReferrerReceiver : BroadcastReceiver() {
    private val TAG = "ReferrerReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val referrer = intent.getStringExtra("referrer")

        // Decode the URL from UTF-8 and set it to a URL object
        val referrerUrl = URL(URLDecoder.decode(referrer, "UTF-8" ))

        // Grab the query params from the referrerUrl
        val referrerQuery = referrerUrl.query
        Log.i(TAG, "Referrer query params received: $referrerQuery")

        val referrerIntent = Intent("referrerBroadcast").putExtra("referrerQuery", referrerQuery)
        Log.i(TAG, "Sending broadcast with referrerQuery...")
        context.sendBroadcast(referrerIntent)
    }
}

