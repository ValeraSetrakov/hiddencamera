package com.valerasetrakov.hiddencamerarecorder

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.commonandroid.log
import com.valerasetrakov.hiddencamerarecorder.databinding.ActivityMainBinding

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {

    companion object {
        val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityMainBinding
    private val currentFragment: Fragment?
        get() = supportFragmentManager.findFragmentById(binding.fragmentHost.id)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        if (savedInstanceState == null) {
            changeFragment(CameraFragment.create())
        }
    }

    private fun changeFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentHost.id, fragment)
        fragmentTransaction.commit()
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        log("onKeyDown keyCode $keyCode KeyEvent $event")
        val fragment = currentFragment
        if (fragment is CameraFragment)
            return fragment.onKeyDown(keyCode, event)
        return super.onKeyDown(keyCode, event)
    }
}
