package com.example.diagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    private val DIAGRAM_FRAGMENT_TAG  = "DIAGRAM_FRAGMENT_TAG"
    private lateinit var mDiagramFragment : FragmentCustomView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(savedInstanceState == null){
            mDiagramFragment = FragmentCustomView().getInstance()
        }else{
            mDiagramFragment = supportFragmentManager.findFragmentByTag(DIAGRAM_FRAGMENT_TAG) as FragmentCustomView
        }

        supportFragmentManager.beginTransaction()
                .replace(R.id.fr_container, mDiagramFragment)
                .commit()
    }
}