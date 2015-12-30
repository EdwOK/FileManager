package com.project.filemanager.actions;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.project.filemanager.R;

import java.io.File;

public class GroupOwnerAction extends AsyncTask<File, Void, Boolean> {

    private final Context context;
    private final String group, owner;

    public GroupOwnerAction(Context context, String group1, String owner1) {
        this.context = context;
        this.group = group1;
        this.owner = owner1;
    }

    @Override
    protected Boolean doInBackground(final File... params) {
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        this.finish(result);
    }

    @Override
    protected void onCancelled(Boolean result) {
        super.onCancelled(result);
        this.finish(result);
    }

    private void finish(Boolean result) {
        if (result)
            Toast.makeText(this.context,
                    this.context.getString(R.string.permissionschanged),
                    Toast.LENGTH_SHORT).show();
    }
}