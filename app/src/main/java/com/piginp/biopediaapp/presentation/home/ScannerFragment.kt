package com.piginp.biopediaapp.presentation.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.piginp.biopediaapp.R
import com.piginp.biopediaapp.databinding.FragmentScannerBinding
import com.piginp.biopediaapp.presentation.constants.Constants

class ScannerFragment : Fragment(R.layout.fragment_scanner) {

    private lateinit var scannerView: CodeScannerView

    private var flashLightStatus: Boolean = false

    private val Fragment.packageManager get() = activity?.packageManager

    private lateinit var codeScanner: CodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        startScanning()
        checkFlashlightStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onPause() {
        super.onPause()

        codeScanner.releaseResources()
        // TODO: Потестить потом возврат в приложение, тут не было super
    }


    private fun init() {
        scannerView = _binding!!.scannerView
        codeScanner = CodeScanner(requireContext(), scannerView)

        if (requireContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
            activity?.runOnUiThread {
                openWebPage(it.text)
                codeScanner.stopPreview()
            }
        }

        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            activity?.runOnUiThread {
                showCameraInitErrorDialog()
            }
        }

    }

    //--- Открыть ссылку в окне браузера
    private fun openWebPage(url: String?) {
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (packageManager?.let { intent.resolveActivity(it) } != null) {
            startActivity(intent)
        }
    }

    //--- Показать диалог с сообщением об ошибке разрешения на исп. камеры
    private fun showCameraPermissionErrorDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.error))
            .setMessage(resources.getString(R.string.camera_perm_dialog_supporting_text))

            .setNegativeButton(resources.getString(R.string.decline)) { dialog, which ->
                activity?.onBackPressed();
            }
            .setPositiveButton(resources.getString(R.string.grant)) { dialog, which ->
                openApplicationDetailsSettings() // Открыть страницу настроек приложения
            }
            .show()
    }

    //--- Показать диалог с сообщением об ошибке инициализации камеры
    private fun showCameraInitErrorDialog() {
        MaterialAlertDialogBuilder(requireContext())
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
        val uri = Uri.fromParts("package", requireContext().getPackageName(), null)
        intent.data = uri
        startActivity(intent)
    }

    @Deprecated("Deprecated in Java")
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
        if (packageManager?.let { intent.resolveActivity(it) } != null) {
            startActivity(intent)
        }
    }
}