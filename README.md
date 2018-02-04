# ReferrerCaseStudy

This is a native Android application written in Kotlin that accepts a broadcast INSTALL_REFERRER 
and displays the content from the referrer, along with several device identifiers, on the UI.

This information can be posted to an API by pressing the post button. The full JSON response will 
appear in a dialog.
 
## How to Test: 

_*Note: After running this the first time, the referrer data is saved to shared preferences and is
preloaded on all subsequent app loads. This can be overwrote by sending another broadcast to the 
INSTALL_REFERRER*_

1) Run the app on AVD (or plug in Android device),  
1) In the terminal enter the Android Debugging Bridge shell:
    ```
    cd Library/Android/sdk/platform-tools
    ./adb shell
    ```
1) Send a test broadcast (the `referrer` will contain the data that will be passed to the app UI)
    ```
    am broadcast -a com.android.vending.INSTALL_REFERRER \
    -n com.taptaptap.referrercasestudy/.util.ReferrerReceiver \
    --es "referrer" "https%3a%2f%2ftest.com%2fget%2fapp%3fuserId%3d123%26implementationid%3dtest%26trafficSource%3dtest%26userClass%3d20170101"
    ```
1) UI should show contents of referrer and some device information. Press post to send to API and 
dialog will appear showing the successful JSON response.