package com.oky2abbas.reactor.security

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.provider.Settings
import android.util.Base64
import com.oky2abbas.reactor.helper.AESHelper
import java.security.MessageDigest


internal class SecurityModel(
    private val appContext: Context,
    private val isEncrypt: Boolean
) {

    private val crypt = AESHelper()
    private val password = getPassword()

    internal fun encryptValue(value: String): String {
        return if (isEncrypt) encryptAES(value) else value
    }

    internal fun decryptValue(value: String): String {
        return if (isEncrypt) decryptAES(value) else value
    }

    private fun encryptAES(value: String): String {
        return crypt.encrypt(password, value)
    }

    private fun decryptAES(value: String): String {
        return crypt.decrypt(password, value)
    }

    private fun getPassword(): String {
        val uuid = (getSign() + getUUID())
        return getSHA256(uuid) ?: uuid
    }

    private fun getSHA256(value: String): String? {
        val bytes = value.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)

        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    /* this method returns the "sign" key to the target application.
    this will maintain the security of the information and
    encrypt the information based on a dynamic and unique key. */

    @SuppressLint("PackageManagerGetSignatures")
    private fun getSign(): String? {

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
    private fun getUUID(): String? {
        return Settings.Secure.getString(
            appContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }
}