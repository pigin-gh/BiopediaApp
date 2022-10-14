package com.piginp.biopediaapp.presentation.home

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN
import com.budiyev.android.codescanner.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.piginp.biopediaapp.R
import com.piginp.biopediaapp.databinding.FragmentScannerBinding
import com.piginp.biopediaapp.presentation.constants.Constants

class ScannerFragment : Fragment(R.layout.fragment_scanner) {

    private lateinit var scannerView: CodeScannerView

    private var flashLightStatus: Boolean = false

    private lateinit var codeScanner: CodeScanner

    private var fragmentScannerBinding: FragmentScannerBinding? = null

    private var helpCardStatus: Boolean = true

    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentScannerBinding.inflate(inflater, container, false)
        fragmentScannerBinding = binding

        sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)!!

        // Сюда вставлять действия с binding
        // ---
        init()
        // ---
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentScannerBinding?.toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {

                R.id.support -> {
                    sendErrorEmail(
                        addresses = Array(1) { getString(R.string.support_address) },
                        subject = getString(R.string.support_email_subject_error),
                        text = getString(R.string.support_email_text_error)
                    )
                    true
                }

                R.id.about_app -> {
                    openAppInfoFragment()
                    true
                }
                else -> false

            }
        }
    }

    override fun onResume() {
        super.onResume()

        checkFlashlightStatus()
        startScanning()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentScannerBinding = null
    }

    override fun onPause() {
        super.onPause()

        codeScanner.releaseResources()
    }

    private fun init() {
        checkHelpCardStatus()

        scannerView = fragmentScannerBinding!!.scannerView
        codeScanner = CodeScanner(requireContext(), scannerView)

        if (requireContext().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), Constants.CAMERA_REQUEST_CODE)
        }

        fragmentScannerBinding!!.flashButton.setOnClickListener {
            switchFlashlight()
        }

        fragmentScannerBinding!!.openBiopdaBt.setOnClickListener {
            openWebPage(getString(R.string.biopda_url))
        }

        fragmentScannerBinding!!.closeHelpCard.setOnClickListener {
            fragmentScannerBinding!!.helpCard.isVisible = false
            putFalseToCardStatus()
        }
    }

    private fun putFalseToCardStatus() {
        with(sharedPref.edit()) {
            putBoolean(Constants.CLOSE_CARD_KEY, false)
            apply()
        }
    }

    //--- Проверить при запуске статус карточки с помощью,
    //--- если пользователь уже удалял её, то она не высветится
    private fun checkHelpCardStatus() {
        helpCardStatus = sharedPref.getBoolean(Constants.CLOSE_CARD_KEY, true)
        if (!helpCardStatus) {
            fragmentScannerBinding!!.helpCard.isVisible = false
        }
    }

    //--- Открыть Fragment с информацией о приложении
    private fun openAppInfoFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .addToBackStack(null)
            .setTransition(TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.frame_layout, AboutAppFragment()).commit()
    }

    //--- Переключить фонарик
    private fun switchFlashlight() {
        if (!flashLightStatus) {
            codeScanner.isFlashEnabled = true
            flashLightStatus = true
            fragmentScannerBinding!!.flashButton.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.primaryDarkColor
                )
            )
        } else {
            codeScanner.isFlashEnabled = false
            flashLightStatus = false
            fragmentScannerBinding!!.flashButton.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.primaryColor
                )
            )
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
            requireActivity().runOnUiThread {
                openWebPage("${getString(R.string.biopda_url)}${it.text}")
                codeScanner.stopPreview()
            }
        }

        codeScanner.errorCallback = ErrorCallback.SUPPRESS

    }

    //--- Открыть ссылку в окне браузера
    private fun openWebPage(url: String?) {
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Приложение не найдено", Toast.LENGTH_SHORT).show()
        }
    }

    //--- Показать диалог с сообщением об ошибке разрешения на исп. камеры
    private fun showCameraPermissionErrorDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.error))
            .setMessage(resources.getString(R.string.camera_perm_dialog_supporting_text))

            .setNegativeButton(resources.getString(R.string.decline)) { dialog, which ->
                activity?.onBackPressed()
            }
            .setPositiveButton(resources.getString(R.string.grant)) { dialog, which ->
                openApplicationDetailsSettings() // Открыть страницу настроек приложения
            }
            .show()
    }

    //--- Открыть страницу настроек приложения
    private fun openApplicationDetailsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    //--- Действия при выборе в окне доступа к камере
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
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Приложение не найдено", Toast.LENGTH_SHORT).show()
        }
    }
}