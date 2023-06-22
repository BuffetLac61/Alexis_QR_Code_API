package com.example.pmr_project

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {

    private lateinit var qrCodeValueTextView : TextView

    private lateinit var startScanButton : Button

// Utilisation de l'API activity result qui permet d'ouvrir l'activité scan QRCode ET de récupérer un result dans la main Activity'

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK) {
// Data que l'on récupère de Scan QR Activity vers Main
            val data = result.data?.getStringExtra(ScanQrCodeActivity.QR_CODE_KEY)
            updateQrCodeTextView(data)
        }

    }

    // data est nullable si jamais on ne scan rien, dans notre cas la plus grande partie du temps
    private fun updateQrCodeTextView(data: String?) {
        // Récupère la data que si elle n'est pas null
        data?.let{
            // Sur le Thread principal car on ne peut pas changer un textView sur un thread en background
            runOnUiThread {
                qrCodeValueTextView.text = it
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        qrCodeValueTextView = findViewById(R.id.qr_code_value_tv)
        startScanButton=findViewById(R.id.start_scan_button)
        initButtonClickListener()


    }

    private fun initButtonClickListener() {
        startScanButton.setOnClickListener{
        val intent = Intent(this, ScanQrCodeActivity ::class.java)
        resultLauncher.launch(intent)
        }
    }
}