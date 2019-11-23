package com.example.localnetworkingandroidapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.view.MenuItem
import android.view.Menu
import android.widget.TextView


class MainActivity : AppCompatActivity() {

    var joined = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_join -> {
            if (joined) {
                item.title = getString(R.string.join)

                findViewById<TextView>(R.id.main_activity_textview).text = ""
            } else {
                item.title = getString(R.string.leave)

                val string = getString(R.string.connection_server_message)
                val spannable = SpannableString(string)
                spannable.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, string.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                findViewById<TextView>(R.id.main_activity_textview).text = spannable
            }
            joined = !joined
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
}
