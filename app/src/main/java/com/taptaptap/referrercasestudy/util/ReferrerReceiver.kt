package com.taptaptap.referrercasestudy.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.net.URL
import java.net.URLDecoder

/*
This class is used to listen for an install referrer and decode the URL and grab the query params.
The query params are sent over another broadcast to be picked up by the MainActivity.
 */
open class ReferrerReceiver : BroadcastReceiver() {
    private val TAG = "ReferrerReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val referrer = intent.getStringExtra("referrer")

        // Decode the URL from UTF-8 and set it to a URL object
        val referrerUrl = URL(URLDecoder.decode(referrer, "UTF-8" ))

        // Grab the query params from the referrerUrl
        val referrerQuery = referrerUrl.query
        Log.i(TAG, "Referrer query params received: $referrerQuery")

        // Create and send an intent over a broadcast with the query parameters from the referrer
        val referrerIntent = Intent("referrerBroadcast").putExtra("referrerQuery", referrerQuery)
        Log.i(TAG, "Sending broadcast with referrerQuery...")
        context.sendBroadcast(referrerIntent)
    }
}

