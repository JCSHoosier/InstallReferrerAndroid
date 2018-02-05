package com.taptaptap.referrercasestudy

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
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
    private var broadcastReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        infoText = findViewById(R.id.infoText)
        val postButton: Button = findViewById(R.id.postButton)

        //Recover saved JSON if applicable and preload view
        val savedJson = PreferenceManager.getDefaultSharedPreferences(this).getString("jsonObject", "")
        Log.i(TAG, "saved json: $savedJson")
        if (savedJson != "") {
            postDataJson = JSONObject(savedJson)
            loadRows(postDataJson!!)
        }

        //Post to AllTheApps when button is clicked and send the response body to responseDialog().
        //Button is only visible when postDataJson contains a value so it can rely on that data.
        postButton.setOnClickListener {
            setPostButtonVisibility(false)
            setPostProgressBarVisibility(true)
            val allTheApps = Retrofit.Builder()
                    .baseUrl("https://api.alltheapps.org/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(AllTheApps::class.java)

            val response = allTheApps.postData(postDataJson!!)

            response.enqueue(object : Callback<ResponseBody>{
                override fun onResponse(call: Call<ResponseBody>,
                                        response: Response<ResponseBody>) {
                    setPostButtonVisibility(true)
                    setPostProgressBarVisibility(false)
                    val jsonObject = JSONObject(response.body()?.string())
                    Log.i(TAG, "Response string is: $jsonObject")
                    responseDialog(this@MainActivity, jsonObject.toString(2))
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "Post failed: $t")
                    setPostButtonVisibility(true)
                    setPostProgressBarVisibility(false)
                    showToast(this@MainActivity,"Post failed!")
                }
            })
        }

    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "Registering internal referrer broadcast receiver...")
        createReferrerBroadcastReceiver()
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "Deregistering internal referrer broadcast receiver...")
        unregisterReceiver(broadcastReceiver)
    }

    //Creates a broadcast receiver used to listen for an intent that contains a referrerBroadcast
    private fun createReferrerBroadcastReceiver() {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                showToast(context, "Broadcast Received")

                val referrerQuery = intent.getStringExtra("referrerQuery")

                // Split params to a Hash Set
                val queryList = referrerQuery.split("&").map { m -> m.split("=") }.toList()
                postDataJson = getPostDataJson(queryList)

                //Save JSONObject.toString() to SharedPreferences for recovery in onCreate()
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("jsonObject", postDataJson.toString()).apply()

                loadRows(postDataJson!!)
            }
        }
        registerReceiver(broadcastReceiver, IntentFilter("referrerBroadcast"))
    }

    //Sets up a table in the main UI of a table with a list of values from the JSONObject
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

    //Converts the queryList from the referrer, adds some device identifiers, and converts it to a
    //JSONObject
    fun getPostDataJson(queryList: List<List<String>>) : JSONObject {
        postDataModel = PostDataModel(
                userId = queryList[0][1],
                implmentationid = queryList[1][1],
                trafficSource = queryList[2][1],
                userClass = queryList[3][1],
                deviceId = Build.DEVICE,
                manufacturer = Build.MANUFACTURER,
                model = Build.MODEL)

        //Sets the value at a higher level to be used by other funtions
        val jsonPostData = JSONObject(Gson().toJson(postDataModel))
        Log.i(TAG,"jsonPostData set to: $jsonPostData")

        return jsonPostData
    }

    private fun responseDialog(context : Context, response : String?) {
        AlertDialog.Builder(context)
                .setTitle("Successful post, response was:")
                .setMessage(response)
                .setNeutralButton("OK") { dialog, _ -> dialog.dismiss()}
                .show()
    }

    private fun setInfoText(success : Boolean) {
        val infoText = findViewById<TextView>(R.id.infoText)
        if (success) {
            infoText.text = getString(R.string.info_text_success)
        } else {
            infoText.text = getString(R.string.info_text_waiting)
        }
    }

    private fun setPostProgressBarVisibility(visible: Boolean) {
        val postProgressBar = findViewById<ProgressBar>(R.id.postProgressBar)
        if (visible) {
            postProgressBar.visibility = Button.VISIBLE
        } else {
            postProgressBar.visibility = Button.GONE
        }
    }

    private fun setPostButtonVisibility(visible : Boolean) {
        val postButton = findViewById<Button>(R.id.postButton)
        if (visible) {
            postButton.visibility = Button.VISIBLE
        } else {
            postButton.visibility = Button.GONE
        }
    }

    private fun showToast(context : Context, message : String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
