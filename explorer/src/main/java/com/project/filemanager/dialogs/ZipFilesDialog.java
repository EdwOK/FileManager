package com.project.filemanager.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.project.filemanager.R;
import com.project.filemanager.adapters.ExplorerTabsAdapter;
import com.project.filemanager.actions.ZipAction;

import java.io.File;

public final class ZipFilesDialog extends DialogFragment {

    private static String[] files;

    public static DialogFragment instantiate(String[] files1) {
        files = files1;
        return new ZipFilesDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle state) {
        final Activity a = getActivity();
        final String zipfile = ExplorerTabsAdapter.getCurrentBrowserFragment().mCurrentPath
                + "/" + "zipfile.zip";
        final int size = files.length;

        final EditText inputf = new EditText(a);
        inputf.setHint(R.string.enter_name);
        inputf.setText(zipfile);

        final AlertDialog.Builder b = new AlertDialog.Builder(a);
        b.setTitle(getString(R.string.packing) + " (" + String.valueOf(size) + ")");
        b.setView(inputf);
        b.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newpath = inputf.getText().toString();
                        File file = new File(newpath);

                        if (file.exists()) {
                            Toast.makeText(a, a.getString(R.string.fileexists),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        dialog.dismiss();
                        final ZipAction task = new ZipAction(a, newpath);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, files);
                    }
                });
        b.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        return b.create();
    }
}
