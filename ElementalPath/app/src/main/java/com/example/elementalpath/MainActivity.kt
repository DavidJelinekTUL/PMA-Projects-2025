package com.example.elementalpath

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Pouze nastavíme layout. O zbytek se postará Navigation Component v XML.
        setContentView(R.layout.activity_main)
    }
}