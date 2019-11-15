package io.github.zoha131.flowsharedpreference

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pref = this.getSharedPreferences("TaalaPreferences", Context.MODE_PRIVATE)
        val prefClient = SharedPrefClient(pref)

        engBtn.setOnClickListener {
            val a  = prefClient.edit()
            a.putString(SharedPrefClient.LANGUAGE, "English")
            a.apply()

            Log.d("Preferences", "Button Clicked: English")
        }

        bngBtn.setOnClickListener {
            val a  = prefClient.edit()
            a.putString(SharedPrefClient.LANGUAGE, "Bangla")
            a.apply()

            Log.d("Preferences", "Button Clicked: Bangla")
        }

        lifecycleScope.launchWhenResumed {
            var flip = false

            for (i in 1..1000){
                delay(1000)
                if(flip) engBtn.performClick()
                else bngBtn.performClick()

                flip = !flip
            }
        }

        lifecycleScope.launchWhenResumed {
            prefClient.prefsFlow.collect {
                Log.d("Preferences", "Flow Value changed ${prefClient.getString(it.first, "")}")
            }
        }

        pref.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            Log.d("Preferences", "Listener of sharedpref ${sharedPreferences.getString(key, "")}")
        }

    }
}


@ExperimentalCoroutinesApi
class SharedPrefClient(private val prefs: SharedPreferences) : SharedPreferences by prefs {

    companion object {
        const val LANGUAGE = "LANGUAGE"
    }

    val prefsFlow = callbackFlow {

        prefs.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            Log.d("Preferences client", key)

            offer(key to sharedPreferences)
        }

        awaitClose { Log.d("Preferences client", "Coroutine Closed") }

    }.buffer(Channel.UNLIMITED)

}
