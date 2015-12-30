package com.project.filemanager.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;

import com.project.filemanager.R;
import com.project.filemanager.adapters.ExplorerTabsAdapter;
import com.project.filemanager.actions.ExtractionAction;

import java.io.File;

public final class UnpackDialog extends DialogFragment {

    private static File file;

    public static DialogFragment instantiate(File file1) {
        file = file1;
        return new UnpackDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle state) {
        final Activity a = getActivity();

        final EditText inputf = new EditText(a);
        inputf.setHint(R.string.enter_name);
        inputf.setText(ExplorerTabsAdapter.getCurrentBrowserFragment().mCurrentPath + "/");

        final AlertDialog.Builder b = new AlertDialog.Builder(a);
        b.setTitle(R.string.extractto);
        b.setView(inputf);
        b.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newpath = inputf.getText().toString();

                        dialog.dismiss();

                        final ExtractionAction task = new ExtractionAction(a);
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, file,
                                new File(newpath));
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
