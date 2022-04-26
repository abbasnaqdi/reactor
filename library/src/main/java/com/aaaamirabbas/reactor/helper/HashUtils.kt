package com.aaaamirabbas.reactor.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.provider.Settings
import android.util.Base64
import java.security.MessageDigest

object HashUtils {
    fun getSHA256(value: String): String? {
        val bytes = value.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    /* this method returns the "sign" key to the target application.
    this will maintain the security of the information and
    encrypt the information based on a dynamic and unique key. */

    @SuppressLint("PackageManagerGetSignatures")
    fun getSign(appContext: Context): String? {

        val packageInfo: PackageInfo?
        val signatures: Array<Signature>?
        var strSign: String? = null

        try {

            if (Build.VERSION.SDK_INT >= 28) {
                packageInfo = appContext.packageManager
                    .getPackageInfo(appContext.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                signatures = packageInfo?.signingInfo?.apkContentsSigners
            } else {
                packageInfo = appContext.packageManager
                    .getPackageInfo(
                        appContext.packageName,
                        PackageManager.GET_SIGNATURES
                    )

                signatures = packageInfo?.signatures
            }

            val md = MessageDigest.getInstance("SHA")

            signatures?.forEach {
                md.update(it.toByteArray())
                strSign = String(Base64.encode(md.digest(), Base64.DEFAULT))
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
            return strSign
        }

        return strSign
    }

    @SuppressLint("HardwareIds")
    fun getUUID(appContext: Context): String? {
        return Settings.Secure.getString(
            appContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }
}