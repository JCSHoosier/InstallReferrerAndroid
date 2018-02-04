package com.taptaptap.referrercasestudy

import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.taptaptap.referrercasestudy.api.AllTheApps
import com.taptaptap.referrercasestudy.model.PostDataModel
import kotlinx.android.synthetic.main.row.view.*
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var postDataModel: PostDataModel? = null
    private var infoText: TextView? = null
    private var postDataJson: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        infoText = findViewById(R.id.infoText)
        val postButton: Button = findViewById(R.id.postButton)

        //Post to AllTheApps API when Button is clicked
        postButton.setOnClickListener {
            val allTheApps = Retrofit.Builder()
                    .baseUrl("https://api.alltheapps.org/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(AllTheApps::class.java)

            val response = allTheApps.postData(postDataJson!!)
            response.enqueue(object : Callback<ResponseBody>{
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    val jsonObject = JSONObject(response.body()?.string())
                    Log.i(TAG, "Response string is: $jsonObject")
                    responseDialog(this@MainActivity, jsonObject.toString(2))
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "Post failed: $t")
                    showToast(this@MainActivity,"Post failed!")
                }
            })
        }

        // Set up broadcast receiver to get intent from ReferrerReceiver when it is hit
        createReferrerBroadcastReceiver()
    }

    private fun responseDialog(context : Context, response : String?) {
        AlertDialog.Builder(context)
                .setTitle("Successful post, response was:")
                .setMessage(response)
                .setNeutralButton("OK") { dialog, _ -> dialog.dismiss()}
                .show()
    }

    private fun createReferrerBroadcastReceiver() {
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                showToast(context, "Broadcast Received")

                val referrerQuery = intent.getStringExtra("referrerQuery")

                // Split params to a Hash Set
                val queryList = referrerQuery.split("&").map { m -> m.split("=") }.toList()
                postDataJson = getPostDataJson(queryList)
                loadRows(postDataJson!!)
            }
        }
        Log.i(TAG, "Registering internal referrer broadcast receiver...")
        registerReceiver(broadcastReceiver, IntentFilter("referrerBroadcast"))
    }

    private fun loadRows(jsonPostData : JSONObject) {
        val dataTable = findViewById<TableLayout>(R.id.dataTable)

        // Clean up in case this is posted multiple times
        dataTable.removeAllViews()

        val iterator : Iterator<String> = jsonPostData.keys()
        // Add rows to table for each key value pair
        while (iterator.hasNext()) {
            val key = iterator.next()
            val value = jsonPostData.get(key).toString()
            Log.i(TAG, "Adding row data: $key - $value")

            val row = LayoutInflater.from(this).inflate(R.layout.row, null)
            row.dataKey.text = key
            row.dataValue.text = value

            dataTable.addView(row)
        }

        //Set info text and post button visibility
        setInfoText(true)
        setPostButtonVisibility(true)
    }

    fun getPostDataJson(queryList: List<List<String>>) : JSONObject {
        postDataModel = PostDataModel(
                userId = queryList[0][1],
                implmentationid = queryList[1][1],
                trafficSource = queryList[2][1],
                userClass = queryList[3][1],
                deviceId = Build.DEVICE,
                manufacturer = Build.MANUFACTURER,
                model = Build.MODEL)

        val jsonPostData = JSONObject(Gson().toJson(postDataModel))
        Log.i(TAG,"jsonPostData set to: $jsonPostData")

        return jsonPostData
    }

    private fun setInfoText(success : Boolean) {
        val infoText = findViewById<TextView>(R.id.infoText)
        if (success) {
            infoText.text = getString(R.string.info_text_success)
        } else {
            infoText.text = getString(R.string.info_text_waiting)
        }
    }

    private fun setPostButtonVisibility(visible : Boolean) {
        val postButton = findViewById<Button>(R.id.postButton)
        if (visible) {
            postButton.visibility = Button.VISIBLE
        } else {
            postButton.visibility = Button.INVISIBLE
        }
    }

    private fun showToast(context : Context, message : String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
