package com.project.filemanager.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.project.filemanager.adapters.ExplorerListAdapter;
import com.project.filemanager.adapters.ExplorerTabsAdapter;
import com.project.filemanager.controllers.ActionModeController;
import com.project.filemanager.utils.SimpleUtils;
import com.project.filemanager.R;

import java.io.File;
import java.util.ArrayList;

public class SearchActivity extends ChangeThemeActivity implements SearchView.OnQueryTextListener {

    private AbsListView mListView;
    private ExplorerListAdapter mAdapter;
    private ActionModeController mActionController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        init();

        if (savedInstanceState != null) {
            mAdapter.addContent(savedInstanceState.getStringArrayList("savedList"));

            getSupportActionBar().setSubtitle(String.valueOf(mAdapter.getCount())
                    + getString(R.string._files));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("savedList", mAdapter.getContent());
    }

    private void init() {
        mAdapter = new ExplorerListAdapter(this, getLayoutInflater());
        mActionController = new ActionModeController(this);

        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setEmptyView(findViewById(android.R.id.empty));
        mListView.setAdapter(mAdapter);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                File f = new File(mAdapter.getItem(position));

                if (f.isDirectory()) {
                    finish();

                    ExplorerTabsAdapter.getCurrentBrowserFragment().navigateTo(f.getAbsolutePath());
                } else if (f.isFile()) {
                    SimpleUtils.openFile(SearchActivity.this, f);
                }
            }
        });

        mActionController.setListView(mListView);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mActionController.isActionMode()) {
            mActionController.finishActionMode();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
            case R.id.action_search:
                this.onSearchRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onQueryTextSubmit(final String query) {
        if (TextUtils.isEmpty(query)) {
            return false;
        }

        SearchTask mTask = new SearchTask(this);
        mTask.execute(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(final String query) {
        return false;
    }

    @Override
    public boolean onKeyDown(int keycode, @NonNull KeyEvent event) {
        if (keycode != KeyEvent.KEYCODE_BACK) {
            return false;
        }

        if (mActionController.isActionMode()) {
            mActionController.finishActionMode();
        }

        return super.onKeyDown(keycode, event);
    }

    private class SearchTask extends AsyncTask<String, Void, ArrayList<String>> {
        public ProgressDialog pr_dialog;
        private final Context context;

        private SearchTask(Context c) {
            context = c;
        }

        @Override
        protected void onPreExecute() {
            pr_dialog = ProgressDialog.show(context, null,
                    getString(R.string.search));
            pr_dialog.setCanceledOnTouchOutside(true);
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            String location = ExplorerTabsAdapter.getCurrentBrowserFragment().mCurrentPath;
            return SimpleUtils.searchInDirectory(location, params[0]);
        }

        @Override
        protected void onPostExecute(final ArrayList<String> files) {
            int len = files != null ? files.size() : 0;

            pr_dialog.dismiss();

            if (len == 0) {
                Toast.makeText(context, R.string.itcouldntbefound,
                        Toast.LENGTH_SHORT).show();
                getSupportActionBar().setSubtitle(null);
            } else {
                mAdapter.addContent(files);
                getSupportActionBar().setSubtitle(String.valueOf(len)
                        + getString(R.string._files));
            }
        }
    }
}
