package com.project.filemanager.actions;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import com.project.filemanager.R;
import com.project.filemanager.adapters.ExplorerTabsAdapter;
import com.project.filemanager.utils.SimpleUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public final class RenameAction extends AsyncTask<String, Void, List<String>> {

    private final WeakReference<Activity> activity;

    private ProgressDialog dialog;

    private boolean succes = false;

    public RenameAction(final Activity activity) {
        this.activity = new WeakReference<>(activity);
    }

    @Override
    protected void onPreExecute() {
        final Activity activity = this.activity.get();

        if (activity != null) {
            this.dialog = new ProgressDialog(activity);
            this.dialog.setMessage(activity.getString(R.string.rename));
            this.dialog.setCancelable(true);
            this.dialog
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            cancel(false);
                        }
                    });
            if (!activity.isFinishing()) {
                this.dialog.show();
            }
        }
    }

    @Override
    protected List<String> doInBackground(String... files) {
        final List<String> failed = new ArrayList<>();
        String path = ExplorerTabsAdapter.getCurrentBrowserFragment().mCurrentPath;

        try {
            if (SimpleUtils.renameTarget(path + "/" + files[0], files[1]))
                succes = true;
        } catch (Exception e) {
            failed.add(files[1]);
            succes = false;
        }
        return failed;
    }

    @Override
    protected void onPostExecute(final List<String> failed) {
        super.onPostExecute(failed);
        this.finish(failed);
    }

    @Override
    protected void onCancelled(final List<String> failed) {
        super.onCancelled(failed);
        this.finish(failed);
    }

    private void finish(final List<String> failed) {
        if (this.dialog != null) {
            this.dialog.dismiss();
        }

        final Activity activity = this.activity.get();

        if (succes)
            Toast.makeText(activity,
                    activity.getString(R.string.filewasrenamed),
                    Toast.LENGTH_LONG).show();

        if (activity != null && !failed.isEmpty()) {
            Toast.makeText(activity, activity.getString(R.string.cantopenfile),
                    Toast.LENGTH_SHORT).show();
            if (!activity.isFinishing()) {
                dialog.show();
            }
        }
    }
}
