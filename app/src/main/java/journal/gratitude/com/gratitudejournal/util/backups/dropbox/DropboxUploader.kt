package journal.gratitude.com.gratitudejournal.util.backups.dropbox

import android.content.Context
import androidx.work.WorkManager
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.InvalidAccessTokenException
import com.dropbox.core.LocalizedText
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.UploadErrorException
import com.dropbox.core.v2.files.WriteMode
import com.presently.coroutine_utils.AppCoroutineDispatchers
import com.presently.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.BuildConfig
import journal.gratitude.com.gratitudejournal.model.CloudUploadResult
import journal.gratitude.com.gratitudejournal.model.UploadError
import journal.gratitude.com.gratitudejournal.model.UploadSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException

interface CloudProvider {
    suspend fun uploadToCloud(file: File): CloudUploadResult
}

class DropboxUploader(val context: Context, val settings: PresentlySettings):
    CloudProvider {

    override suspend fun uploadToCloud(file: File): CloudUploadResult {
        return UploadError(UploadErrorException("routename", "message", LocalizedText("insufficient_space", "en"), com.dropbox.core.v2.files.UploadError.OTHER))
//        return withContext(Dispatchers.IO) {
//            val accessToken = settings.getAccessToken()
//            val requestConfig = DbxRequestConfig.newBuilder("PresentlyAndroid")
//                .build()
//
//            val client = DbxClientV2(requestConfig, accessToken)
//
//            try {
//                FileInputStream(file).use { inputStream ->
//                    client.files().uploadBuilder("/presently-backup.csv")
//                        .withMode(WriteMode.OVERWRITE)
//                        .uploadAndFinish(inputStream)
//                    UploadSuccess
//                }
//            } catch (e: DbxException) {
//                UploadError(e)
//            } catch (e: IOException) {
//                UploadError(e)
//            }
//        }
    }

    companion object {

        fun authorizeDropboxAccess(context: Context, settings: PresentlySettings) {
            settings.markDropboxAuthInitiated()

            val clientIdentifier = "PresentlyAndroid/${BuildConfig.VERSION_NAME}"
            val requestConfig = DbxRequestConfig(clientIdentifier)
            Auth.startOAuth2PKCE(context, BuildConfig.DROPBOX_APP_KEY, requestConfig)
        }

        fun deauthorizeDropboxAccess(context: Context, settings: PresentlySettings) {
            val accessToken = settings.getAccessToken()
            if (accessToken != null) {
                val requestConfig = DbxRequestConfig.newBuilder("PresentlyAndroid")
                    .build()
                val client = DbxClientV2(requestConfig, accessToken)
                client.auth().tokenRevoke()
            }

            settings.clearAccessToken()
            WorkManager.getInstance(context).cancelAllWorkByTag(PRESENTLY_BACKUP)
        }

        const val PRESENTLY_BACKUP = "PRESENTLY_BACKUP"
    }

}