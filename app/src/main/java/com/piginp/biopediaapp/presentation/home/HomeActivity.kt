package com.piginp.biopediaapp.presentation.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.budiyev.android.codescanner.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.piginp.biopediaapp.R
import com.piginp.biopediaapp.databinding.ActivityHomeBinding
import com.piginp.biopediaapp.presentation.constants.Constants


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private lateinit var scannerView: CodeScannerView
    private lateinit var codeScanner: CodeScanner

    private var flashLightStatus: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    override fun onResume() {
        super.onResume()
        startScanning()
        checkFlashlightStatus()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun init() {
        scannerView = findViewById(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), Constants.CAMERA_REQUEST_CODE);
        }

        binding.flashButton.setOnClickListener {
            switchFlashlight()
        }
    }

    //--- Переключить фонарик
    private fun switchFlashlight() {
        if (!flashLightStatus) {
            codeScanner.isFlashEnabled = true
            flashLightStatus = true
            binding.flashlightOnTv.isVisible = true
            binding.flashButton.setBackgroundColor(resources.getColor(R.color.primaryDarkColor)) // TODO
        } else {
            codeScanner.isFlashEnabled = false
            flashLightStatus = false
            binding.flashlightOnTv.isVisible = false
            binding.flashButton.setBackgroundColor(resources.getColor(R.color.primaryColor)) // TODO
        }
    }

    //--- Проверить статус фонарика (нужно при перезапуске)
    private fun checkFlashlightStatus() {
        if (flashLightStatus) {
            codeScanner.isFlashEnabled = true
        }
    }

    //--- Запустить сканирование
    private fun startScanning() {

        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.CONTINUOUS
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false
            startPreview()
        }

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                openWebPage(it.text)
                codeScanner.stopPreview()
            }
        }

        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                showCameraInitErrorDialog()
            }
        }

    }

    //--- Открыть ссылку в окне браузера
    private fun openWebPage(url: String?) {
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    //--- Показать диалог с сообщением об ошибке разрешения на исп. камеры
    private fun showCameraPermissionErrorDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.error))
            .setMessage(resources.getString(R.string.camera_perm_dialog_supporting_text))

            .setNegativeButton(resources.getString(R.string.decline)) { dialog, which ->
                finish() // Закрыть приложение
            }
            .setPositiveButton(resources.getString(R.string.grant)) { dialog, which ->
                openApplicationDetailsSettings() // Открыть страницу настроек приложения
            }
            .show()
    }

    //--- Показать диалог с сообщением об ошибке инициализации камеры
    private fun showCameraInitErrorDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.error))
            .setMessage(resources.getString(R.string.camera_init_dialog_supporting_text))

            .setNegativeButton(resources.getString(R.string.decline)) { dialog, which ->
                dialog.cancel()
            }
            .setPositiveButton(resources.getString(R.string.contact)) { dialog, which ->
                sendErrorEmail(
                    addresses = Array(1) { Constants.SUPPORT_ADDRESS },
                    subject = Constants.SUPPORT_EMAIL_SUBJECT,
                    text = Constants.SUPPORT_EMAIL_TEXT
                )
            }
            .show()
    }

    //--- Открыть страницу настроек приложения
    private fun openApplicationDetailsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning()
            } else {
                showCameraPermissionErrorDialog()
            }
        }
    }

    //--- Открыть окно приложения почты для отправки сообщения об ошибке
    private fun sendErrorEmail(addresses: Array<String>, subject: String, text: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, addresses)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}