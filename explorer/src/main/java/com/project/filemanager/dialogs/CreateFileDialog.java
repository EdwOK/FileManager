package com.project.filemanager.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.project.filemanager.R;
import com.project.filemanager.adapters.ExplorerTabsAdapter;
import com.project.filemanager.utils.SimpleUtils;

import java.io.File;

public final class CreateFileDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity a = getActivity();

        final EditText inputf = new EditText(a);
        inputf.setHint(R.string.enter_name);

        final AlertDialog.Builder b = new AlertDialog.Builder(a);
        b.setTitle(R.string.newfile);
        b.setView(inputf);
        b.setPositiveButton(R.string.create,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = inputf.getText().toString();
                        String path = ExplorerTabsAdapter.getCurrentBrowserFragment().mCurrentPath;

                        if (name.length() >= 1) {
                            boolean success = SimpleUtils.createFile(new File(path, name));

                            if (success)
                                Toast.makeText(a, R.string.filecreated, Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(a, R.string.error, Toast.LENGTH_SHORT).show();
                        }

                        dialog.dismiss();
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
