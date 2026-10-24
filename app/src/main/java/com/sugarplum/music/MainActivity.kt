
package com.sugarplum.music

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val background = Color.rgb(255, 242, 248)
        val pink = Color.rgb(224, 112, 160)
        val dark = Color.rgb(91, 53, 72)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
            setBackgroundColor(background)
        }

        val title = TextView(this).apply {
            text = "୨୧ Sugarplum ♡"
            textSize = 34f
            setTextColor(pink)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "YOUR MUSIC, YOUR LITTLE WORLD"
            textSize = 12f
            setTextColor(dark)
            gravity = Gravity.CENTER
        }

        val nowPlaying = TextView(this).apply {
            text = "\n♡  NOW PLAYING  ♡\n\nNo song detected yet"
            textSize = 18f
            setTextColor(dark)
            gravity = Gravity.CENTER
            setPadding(20, 32, 20, 32)
        }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            subtitle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            nowPlaying,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(root)
    }
}
