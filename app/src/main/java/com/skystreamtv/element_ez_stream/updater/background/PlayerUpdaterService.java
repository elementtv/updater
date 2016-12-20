package com.skystreamtv.element_ez_stream.updater.background;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.util.JsonWriter;
import android.util.Log;

import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.model.Skin;
import com.skystreamtv.element_ez_stream.updater.player.PlayerInstaller;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;
import com.skystreamtv.element_ez_stream.updater.utils.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PlayerUpdaterService extends IntentService implements Files.ProgressListener {

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_UPDATE_READY = 3;
    public static final int MSG_UPDATE_PROGRESS = 4;
    public static final int MSG_UPDATE_CANCELLED = 5;
    public static final int MSG_UPDATE_COMPLETED = 6;
    private static final String TAG = "PlayerUpdaterService";
    private final Messenger service_messenger = new Messenger(new MessengerHandler(this));
    public Messenger client;
    private DownloadManager download_manager;
    private long download_id;
    private Skin skin;
    private File PLAYER_CONF_DIRECTORY;
    private String other_failure_reason;
    private Status service_status = Status.NEW;
    private boolean cleanInstall;
    private int old_progress = 0;
    public PlayerUpdaterService() {
        super("MadCastService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Called onBind()");
        return service_messenger.getBinder();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Service: indent received");
        if (intent.getBooleanExtra("SERVICE_RESET", false)) {
            cleanInstall = intent.getBooleanExtra(Constants.CLEAN_INSTAL, false);
            Log.d(TAG, "Clean Install: " + cleanInstall);
            Log.d(TAG, "Service: reset.");
            startUpdate(intent);
        } else {
            Log.d(TAG, "Service: NO reset.");
            stopSelf();
        }
    }

    protected void startUpdate(Intent intent) {
        Log.d(TAG, "Call PlayerUpdaterService.startUpdate()");
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "PlayerUpdaterServiceWakeLock");
        wakeLock.acquire();
        service_status = Status.PENDING;
        updateReady();
        this.download_manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        PLAYER_CONF_DIRECTORY = new File(Environment.getExternalStorageDirectory(), "Android/data/" + getString(R.string.player_id) + "/files/.kodi");
        this.skin = intent.getParcelableExtra(Constants.SKINS);
        service_status = Status.RUNNING;
        boolean result = doUpdate();
        if (service_status == Status.CANCELED)
            updateCancelled();
        else {
            updateCompleted(result);
            service_status = Status.FINISHED;
        }
        wakeLock.release();
        stopSelf();
    }

    protected void publishProgress(final int progress) {
        try {
            // Avoid overloading the message queue
            if (old_progress != progress)
                client.send(Message.obtain(null, MSG_UPDATE_PROGRESS, progress, 0));
            old_progress = progress;
        } catch (RemoteException | NullPointerException e) {
            // Activity is disconnected from the service, do nothing
        }
    }

    protected void cancel() {
        service_status = Status.CANCELED;
    }

    protected void sendCallbackMessage(Message msg) {
        boolean sent = false;
        while (!sent)
            try {
                client.send(msg);
                sent = true;
            } catch (RemoteException | NullPointerException e) {
                SystemClock.sleep(100);
            }
    }

    protected void updateReady() {
        Log.d(TAG, "Call PlayerUpdaterService.updateReady()");
        sendCallbackMessage(Message.obtain(null, MSG_UPDATE_READY));
    }

    protected void updateCancelled() {
        Message msg = Message.obtain(null, MSG_UPDATE_CANCELLED);
        Bundle msg_data = new Bundle();
        msg_data.putParcelable("failure_reason", new FailureReason(getFailureReason(download_id)));
        msg.setData(msg_data);
        sendCallbackMessage(msg);
    }

    protected void updateCompleted(boolean updated) {
        if (updated) {
            PlayerInstaller.launchPlayer(this);
        }
        sendCallbackMessage(Message.obtain(null, MSG_UPDATE_COMPLETED));
    }

    protected Boolean doUpdate() {
        try {
            download_id = startDownload();
        } catch (IOException e) {
            other_failure_reason = e.getMessage();
            cancel();
            return false;
        }
        while (service_status == Status.RUNNING) {
            SystemClock.sleep(1000);
            int dl_progress = getDownloadProgress(download_id);
            Log.d(TAG, "Process progress: " + dl_progress + "%");
            if (dl_progress == -1) {
                cancel();
                return false;
            } else if (dl_progress == 101) {
                Log.d(TAG, "get_download_progress() returned 101 (completed)");
                if (cleanInstall) publishProgress(50);
                else publishProgress(33);
                return updatePlayerConfiguration();
            } else
                publishProgress(dl_progress);
        }
        return true;
    }

    protected long startDownload() throws IOException {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            throw new IOException("External storage is not mounted");
        }


        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(skin.getDownloadUrl()));
        File destination = new File(ContextCompat.getExternalFilesDirs(this,
                Environment.DIRECTORY_DOWNLOADS)[0], "media_player_update.zip");
        if (destination.exists() && !destination.delete())
            throw new IOException("Failed to remove old update download from storage. Please, contact our support");
        request.setDestinationInExternalFilesDir(this, null, "media_player_update.zip");
        return download_manager.enqueue(request);
    }

    protected int getDownloadProgress(long download_id) {
        Log.d(TAG, "Call getDownloadProgress()");
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(download_id);
        Cursor cursor = download_manager.query(query);
        try {
            cursor.moveToFirst();
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            if (status == DownloadManager.STATUS_FAILED) {
                cursor.close();
                Log.d(TAG, "Download manager reported a download failure");
                return -1;
            } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                Log.d(TAG, "Download manager reported download completed");
                return 101;
            } else {
                long bytes_downloaded = cursor.getInt(cursor.getColumnIndex(
                        DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                long bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                cursor.close();
                Log.d(TAG, "downloaded: " + bytes_downloaded + ", total: " + bytes_total);
                int divisor = cleanInstall ? 50 : 33;
                return (bytes_downloaded >= 0) && (bytes_total > 0) ? (int) (bytes_downloaded * divisor / bytes_total) : 0;
            }
        } catch (Exception e) {
            cursor.close();
            Log.d(TAG, "Exception while querying Download Manager");
            return -1;
        }
    }

    protected String getFailureReason(long download_id) {
        String reason;
        if (other_failure_reason != null)
            reason = other_failure_reason;
        else {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(download_id);
            Cursor cursor = download_manager.query(query);
            try {
                cursor.moveToFirst();
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_FAILED) {
                    int reason_code = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
                    switch (reason_code) {
                        case DownloadManager.ERROR_CANNOT_RESUME:
                            reason = getString(R.string.cant_resume);
                            break;
                        case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                            reason = getString(R.string.no_device);
                            break;
                        case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                            reason = getString(R.string.file_exists);
                            break;
                        case DownloadManager.ERROR_FILE_ERROR:
                            reason = getString(R.string.device_issue);
                            break;
                        case DownloadManager.ERROR_HTTP_DATA_ERROR:
                            reason = getString(R.string.http_error);
                            break;
                        case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                            reason = getString(R.string.insufficient_space);
                            break;
                        case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                            reason = getString(R.string.many_redirects);
                            break;
                        case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                            reason = getString(R.string.unhandled_http_error);
                            break;
                        default:
                            reason = getString(R.string.unknown_error);
                            break;
                    }
                } else
                    reason = getString(R.string.download_internal_error);
            } catch (Exception e) {
                reason = getString(R.string.download_internal_error);
            }
            Log.d(TAG, reason);
        }
        return reason;
    }

    protected boolean updatePlayerConfiguration() {
        return unzipPlayerConfiguration() && applyPlayerConfiguration();
    }

    protected boolean unzipPlayerConfiguration() {
        try {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                throw new IOException("External storage is not mounted");
            File unzip_directory = new File(ContextCompat.getExternalFilesDirs(this,
                    Environment.DIRECTORY_DOWNLOADS)[0], "media_player_update");
            Log.d(TAG, "Creating destination directory for decompress zip file: " + unzip_directory);
            if (unzip_directory.exists()) {
                if (!deleteDirectory(unzip_directory))
                    throw new IOException("Could not delete old download");
            } else if (!unzip_directory.mkdirs())
                throw new IOException("Could not create destination directory: " + unzip_directory);
            Log.d(TAG, "Querying download manager to get zip filename");
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(download_id);
            Cursor cursor = download_manager.query(query);
            cursor.moveToFirst();
            @SuppressWarnings("deprecation") String zipFilePath = cursor.getString(cursor.getColumnIndex(
                    DownloadManager.COLUMN_LOCAL_FILENAME));

            Log.d(TAG, "Opening zip file stream: " + zipFilePath);
            File zip_file = new File(zipFilePath);
            ZipInputStream zip_stream = new ZipInputStream(new FileInputStream(zip_file));
            Log.d(TAG, "Zip file stream opened");
            download_manager.remove(download_id);
            long max_bytes = Double.valueOf(zip_file.length() * 1.3).longValue();
            long unzipped_bytes = 0;
            ZipEntry zip_entry;
            while ((zip_entry = zip_stream.getNextEntry()) != null) {
                String zip_entry_file_path = zip_entry.getName();
                File new_file = new File(unzip_directory, zip_entry_file_path);
                if (new_file.exists())
                    continue;
                if (zip_entry.isDirectory()) {
                    if (!new_file.mkdirs() && !new_file.exists())
                        throw new IOException("Could not create directory: " + new_file.getAbsolutePath());
                } else {
                    byte[] buffer = new byte[1024];
                    int count;
                    FileOutputStream file_output = new FileOutputStream(new_file);
                    while ((count = zip_stream.read(buffer)) != -1) {
                        file_output.write(buffer, 0, count);
                        unzipped_bytes += count;
                        unzipped_bytes = unzipped_bytes > max_bytes ? max_bytes : unzipped_bytes;
                        int total = cleanInstall ? 50 : 33;
                        int divisor = cleanInstall ? 48 : 33;
                        int percent = (int) (total + (unzipped_bytes * divisor / max_bytes));
                        publishProgress(percent);
                    }
                    file_output.close();
                }
                zip_stream.closeEntry();
            }
            publishProgress(cleanInstall ? 98 : 66);
            zip_stream.close();
            if (zip_file.exists() && !zip_file.delete())
                Log.d(TAG, "Could not delete the update zip file.");
            Log.e(TAG, "UnZip Done");
            return true;
        } catch (IOException e) {
            Log.d(TAG, "Error: " + e.getMessage());
            other_failure_reason = getString(R.string.decompress_error_message);
            cancel();
            return false;
        }
    }

    protected boolean applyPlayerConfiguration() {
        Log.d(TAG, "Call applyPlayerConfiguration()");
        File unzipped_directory = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "media_player_update");
        File addons_origin = new File(unzipped_directory, "/files/.kodi/addons");
        File userdata_origin = new File(unzipped_directory, "/files/.kodi/userdata");
        if (userdata_origin.exists()) {
            Log.e(TAG, "Origin Directory");
            for (File file : userdata_origin.listFiles()) {
                Log.e("File: ", file.getName());
            }
        }
        if (!(addons_origin.exists() && userdata_origin.exists())) {
            other_failure_reason = getString(R.string.update_incomplete);
            cancel();
            return false;
        }
        Log.d(TAG, "Origin directories exists.");

        File addons_destination = new File(PLAYER_CONF_DIRECTORY, "addons");
        File userdata_destination = new File(PLAYER_CONF_DIRECTORY, "userdata");

        if (cleanInstall) {
            if ((addons_destination.exists() && !deleteDirectory(addons_destination)) ||
                    (userdata_destination.exists() && !deleteDirectory(userdata_destination))) {
                other_failure_reason = getString(R.string.update_not_clean);
                cancel();
                return false;
            }
            Log.d(TAG, "Destination directories cleaned.");
        }

        if (!PLAYER_CONF_DIRECTORY.exists())
            if (!PLAYER_CONF_DIRECTORY.mkdirs()) {
                other_failure_reason = getString(R.string.can_not_write_update);
                cancel();
                return false;
            }
        Log.d(TAG, "Player configuration directory exist.");

        if (cleanInstall) {
            if (!(addons_origin.renameTo(addons_destination) && userdata_origin.renameTo(userdata_destination))) {
                other_failure_reason = getString(R.string.update_error_write);
                cancel();
                return false;
            }
            Log.d(TAG, "Update overwrite complete");
        } else {
            if (!(copyFilesToNewDirectory(addons_origin, addons_destination) &&
                    copyFilesToNewDirectory(userdata_origin, userdata_destination))) {
                other_failure_reason = getString(R.string.update_error_write);
                cancel();
                return false;
            }
            Log.d(TAG, "Update copied");
        }


        try {
            JsonWriter writer = new JsonWriter(new FileWriter(new File(PLAYER_CONF_DIRECTORY, "updater.inf")));
            writer.beginObject();
            writer.name("id");
            writer.value(skin.getId());
            writer.name("skin_name");
            writer.value(skin.getName());
            writer.name("version");
            writer.value(skin.getVersion());
            writer.endObject();
            writer.close();
            Log.d("PlayerUpdater", "File updater.inf updated");
        } catch (IOException e) {
            other_failure_reason = getString(R.string.update_tag);
            cancel();
            return false;
        }
        Log.d(TAG, "Apply done.");
        return true;
    }

    protected boolean deleteDirectory(File target_directory) {
        File[] files = target_directory.listFiles();
        boolean all_erased = true;
        for (File file : files) {
            if (file.isFile())
                all_erased &= file.delete();
            else if (file.isDirectory())
                all_erased &= deleteDirectory(file);
        }
        return all_erased && target_directory.delete();
    }

    private boolean copyFilesToNewDirectory(File fromDirectory, File toDirectory) {
        boolean success = true;
        try {
            Files files = new Files(this);
            files.copyDirectory(fromDirectory, toDirectory);
        } catch (IOException exception) {
            success = false;
            Log.e(TAG, exception.getMessage(), exception);
        }
        return success;
    }

    @Override
    public void publishFileProgress(int progress) {
        publishProgress(progress);
    }

    public enum Status {NEW, PENDING, RUNNING, FINISHED, CANCELED}

    private static class MessengerHandler extends Handler {
        private final WeakReference<PlayerUpdaterService> service;

        MessengerHandler(PlayerUpdaterService service) {
            this.service = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            PlayerUpdaterService service = this.service.get();
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    service.client = msg.replyTo;
                    break;
                case MSG_UNREGISTER_CLIENT:
                    service.client = null;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}