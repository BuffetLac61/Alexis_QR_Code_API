package com.example.pmr_project

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.util.forEach
import androidx.core.util.isNotEmpty
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector

class ScanQrCodeActivity : AppCompatActivity() {

    companion object {
        const val QR_CODE_KEY = "qr_code_key"
        private const val CAMERA_REQUEST_CODE = 23
    //23 comme Micheal Jordan
    }

    private lateinit var scanSurfaceView: SurfaceView

    // on a ajouter dans le build gradle la librairie google view (il faudra le refaire sur le code final)
    private lateinit var barCodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_qr_code)

        scanSurfaceView = findViewById(R.id.scan_surface_view)
        initBarcodeDetector()
    }

    // Gérer si l'utilisateur accepte ou non la requête pour la caméra

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(cameraPermissionGranted(requestCode,grantResults)){
            finish()
            // Pour éviter une transition entre les deux
            overridePendingTransition(0,0)
            startActivity(intent)
            //Idem
            overridePendingTransition(0,0)
        } else {
            //SI la personne n'accepte pas
            Toast.makeText(this,"Big brother is watching you, please accept to scan QR code.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
// L'utilisation du request Code ici permet de demander dans une même application plusieurs permissions (peut être le cas quand on fusionnera nos codes...)
    private fun cameraPermissionGranted(requestCode: Int, grantResults: IntArray): Boolean {
        return requestCode == CAMERA_REQUEST_CODE
                && grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private fun initBarcodeDetector() {
        barCodeDetector=BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        initCameraSource()
        initScanSurfaceView()

        //Dernière fonction récupérer les infos du QR code
        barCodeDetector.setProcessor(object : Detector.Processor<Barcode>{
            override fun release() {
                //Quand la caméra est release il n'y a rien à faire donc vide
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                //On récupère tout les items détéctés et on les stocks dans une variable

                if(barcodes.isNotEmpty()){
                    barcodes.forEach { _, barcode ->
                        if (barcode.displayValue.isNotEmpty()){
                            onQrCodeScanned(barcode.displayValue)
                        }
                    }
                }
            }

        })
    }

    private fun onQrCodeScanned(value: String) {
        //Quand on arrive à scanner un QR code il faut le récupérer ici le String en paramètre et l'envoyer à la main activity
        val intent = Intent()
        intent.putExtra(QR_CODE_KEY, value)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }


    private fun initCameraSource() {
        cameraSource=CameraSource.Builder(this,barCodeDetector)
            .setRequestedPreviewSize(1920,1080)
            .setAutoFocusEnabled(true)
            .build()
    }

    private fun initScanSurfaceView() {
        //ajouter un callback à notre surface view pour pouvoir gérer les permissions quand la surfaceView sera créée
        scanSurfaceView.holder.addCallback(object : SurfaceHolder.Callback{
            override fun surfaceCreated(holder: SurfaceHolder) {
                // Savoir si une permission est donnée ou non
                if(ActivityCompat.checkSelfPermission(this@ScanQrCodeActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraSource.start(scanSurfaceView.holder)
                }
                else{
                    ActivityCompat.requestPermissions(this@ScanQrCodeActivity, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
                }

            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.release()
            }

        })
    }

}