package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import com.scottyab.rootbeer.RootBeer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import org.jsoup.Jsoup
import javax.inject.Inject

fun interface TaskCompleteListener {
    fun onTaskCompleteListener(result: Any?)
}

object PrefManager {
    fun <T> getData(mContext: Context?, key: Int): T? = null
}

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    @ApplicationContext val context: Context
) : ViewModel() {

    fun isRootedDevice() {
        val strAlertMessage = """
            Your current device is not secured. 
            We don't recommend using %s on this device as your data could be compromised.
            Please use a secure phone to access %s.
        """.trimIndent()
            .replace(
                "%s",
                context.resources.getString(R.string.app_name)
            )
        val rootBeer = RootBeer(context)
        if (rootBeer.isRooted && rootBeer.isRootedWithoutBusyBoxCheck) {
            // we found indication of root
//            (navigator as? SplashScreenNavigator)?.showRootedInfoToUser(strAlertMessage)
        } else
            getVersionNumber()
    }

    private fun getVersionNumber() {
        checkVersionNumber { response ->
            isShowVersionUpdateAlert(response.toString())
        }
    }

    private fun checkVersionNumber(mListener: TaskCompleteListener) {
        object : Thread() {
            override fun run() = mListener.onTaskCompleteListener(versionCheckAction())
        }.start()
    }

    @VisibleForTesting
    internal val versionCheckAction: () -> String = {
        try {
            Jsoup.connect(
                "https://play.google.com/store/apps/details?id=" + context.packageName.toString()
            )
                .timeout(30000)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .get()
                .select(".hAyfc .htlgb")[7]
                .ownText()
        } catch (e: Exception) {
            val appPackageName: String = context.packageName
            val pInfo: PackageInfo =
                context.packageManager.getPackageInfo(appPackageName, 0)
            val versionName = pInfo.versionName.toString().replace(".", "")
            versionName
        }
    }

    @VisibleForTesting
    internal fun isShowVersionUpdateAlert(googlePlayVersion: String) {
        if (BuildConfig.DEBUG) {
            decideNextActivity()
            return
        }
        try {
            val appPackageName: String = context.packageName
            val pInfo: PackageInfo = context.packageManager.getPackageInfo(appPackageName, 0)
            val versionName = pInfo.versionName.toString().replace(".", "")
            val appVersion: Int = versionName.toInt()
            val googlePlayVersionFinal: Int = googlePlayVersion.replace(".", "").toInt()
            if (appVersion < googlePlayVersionFinal) {

                // show force update dialog
//                (navigator as? SplashScreenNavigator)?.showUpdateAppDialog(
//                    "You are using a version of the app that is no longer supported. Please upgrade to the latest app version.",
//                    appPackageName
//                )
            } else {
                // login inside application
                decideNextActivity()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun decideNextActivity() {
        if (PrefManager.getData<Any>(
                context,
                7
            ) != ""
        ) {
            // navigate to biometric login screen
//            (navigator as? SplashScreenNavigator)?.navigateToBiometricLoginScreen()
        } else if (PrefManager.getData<Any>(
                context,
                8
            ) != ""
        ) {
            // navigate to pin login screen
//            (navigator as? SplashScreenNavigator)?.navigateToPinLoginScreen()
        } else {
            // navigate to login using email/password
//            (navigator as? SplashScreenNavigator)?.navigateToLoginScreen()
        }
    }
}
