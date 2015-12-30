package com.project.filemanager.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.project.filemanager.activities.BaseExplorerActivity;
import com.project.filemanager.R;
import com.project.filemanager.activities.SearchActivity;
import com.project.filemanager.adapters.ExplorerListAdapter;
import com.project.filemanager.controllers.ActionModeController;
import com.project.filemanager.dialogs.CreateFileDialog;
import com.project.filemanager.dialogs.CreateFolderDialog;
import com.project.filemanager.dialogs.DirectoryInfoDialog;
import com.project.filemanager.dialogs.UnpackDialog;
import com.project.filemanager.managers.FileManagerCache;
import com.project.filemanager.managers.MultiFileManager;
import com.project.filemanager.settings.Settings;
import com.project.filemanager.actions.PasteActionExecutor;
import com.project.filemanager.utils.ClipBoard;
import com.project.filemanager.utils.SimpleUtils;
import com.melnykov.fab.FloatingActionButton;

import java.io.File;
import java.lang.ref.WeakReference;

public abstract class BaseBrowserFragment extends UserVisibleHintFragment implements
        MultiFileManager.OnEventListener, PopupMenu.OnMenuItemClickListener {
    private Activity mActivity;
    private FragmentManager fm;

    private MultiFileManager mObserver;
    private FileManagerCache mObserverCache;
    private Runnable mLastRunnable;
    private static Handler sHandler;

    private onUpdatePathListener mUpdatePathListener;
    private ActionModeController mActionController;
    private ExplorerListAdapter mListAdapter;
    public String mCurrentPath;
    private AbsListView mListView;

    private boolean mUseBackKey = true;

    public interface onUpdatePathListener {
        void onUpdatePath(String path);
    }

    @Override
    public void onCreate(Bundle state) {
        setRetainInstance(true);
        setHasOptionsMenu(true);
        super.onCreate(state);
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        mActivity = getActivity();
        Intent intent = mActivity.getIntent();
        fm = getFragmentManager();
        mObserverCache = FileManagerCache.getInstance();
        mUpdatePathListener = (onUpdatePathListener) mActivity;
        mActionController = new ActionModeController(mActivity);
        mActionController.setListView(mListView);

        if (sHandler == null) {
            sHandler = new Handler(mActivity.getMainLooper());
        }

        initDirectory(state, intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_browser, container, false);

        initList(inflater, rootView);
        initFab(rootView);
        return rootView;
    }

    @Override
    protected void onVisible() {
        final BaseExplorerActivity activity = (BaseExplorerActivity) getActivity();
        navigateTo(mCurrentPath);

        if (!ClipBoard.isEmpty())
            activity.supportInvalidateOptionsMenu();
    }

    @Override
    protected void onInvisible() {
        mObserver.stopWatching();

        if (mActionController != null) {
            mActionController.finishActionMode();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("location", mCurrentPath);
    }

    public void initList(LayoutInflater inflater, View rootView) {
        final BaseExplorerActivity context = (BaseExplorerActivity) getActivity();
        mListAdapter = new ExplorerListAdapter(context, inflater);

        mListView = (ListView) rootView.findViewById(android.R.id.list);
        mListView.setEmptyView(rootView.findViewById(android.R.id.empty));
        mListView.setAdapter(mListAdapter);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                final File file = new File((mListView.getAdapter()
                        .getItem(position)).toString());

                if (file.isDirectory()) {
                    navigateTo(file.getAbsolutePath());

                    mListView.setSelection(0);
                } else {
                    listItemAction(file);
                }
            }
        });
    }

    protected void initFab(View rootView) {
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fabbutton);
        fab.attachToListView(mListView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenu(view);
            }
        });
    }

    private void showMenu(View v) {
        PopupMenu popup = new PopupMenu(mActivity, v);

        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.fab_menu);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.createfile:
                final DialogFragment dialog1 = new CreateFileDialog();
                dialog1.show(fm, BaseExplorerActivity.TAG_DIALOG);
                return true;
            case R.id.createfolder:
                final DialogFragment dialog2 = new CreateFolderDialog();
                dialog2.show(fm, BaseExplorerActivity.TAG_DIALOG);
                return true;
            default:
                return false;
        }
    }

    public void listItemAction(File file) {
        if (SimpleUtils.isSupportedArchive(file)) {
            final DialogFragment dialog = UnpackDialog.instantiate(file);
            dialog.show(fm, BaseExplorerActivity.TAG_DIALOG);
        } else {
            SimpleUtils.openFile(mActivity, file);
        }
    }

    public void navigateTo(String path) {
        mCurrentPath = path;

        if (!mUseBackKey)
            mUseBackKey = true;

        if (mObserver != null) {
            mObserver.stopWatching();
            mObserver.removeOnEventListener(this);
        }

        mListAdapter.addFiles(path);

        mObserver = mObserverCache.getOrCreate(path);

        if (mObserver.listeners.isEmpty())
            mObserver.addOnEventListener(this);
        mObserver.startWatching();

        mUpdatePathListener.onUpdatePath(path);
    }

    @Override
    public void onEvent(int event, String path) {
        switch (event & FileObserver.ALL_EVENTS) {
            case FileObserver.CREATE:
            case FileObserver.CLOSE_WRITE:
            case FileObserver.MOVE_SELF:
            case FileObserver.MOVED_TO:
            case FileObserver.MOVED_FROM:
            case FileObserver.ATTRIB:
            case FileObserver.DELETE:
            case FileObserver.DELETE_SELF:
                sHandler.removeCallbacks(mLastRunnable);
                sHandler.post(mLastRunnable =
                        new NavigateRunnable((BaseExplorerActivity) getActivity(), path));
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);

        if (BaseExplorerActivity.isDrawerOpen()) {
            menu.findItem(R.id.paste).setVisible(false);
            menu.findItem(R.id.folderinfo).setVisible(false);
            menu.findItem(R.id.search).setVisible(false);
        } else {
            menu.findItem(R.id.paste).setVisible(!ClipBoard.isEmpty());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final BaseExplorerActivity activity = (BaseExplorerActivity) getActivity();
        final FragmentManager fm = getFragmentManager();

        switch (item.getItemId()) {
            case R.id.folderinfo:
                final DialogFragment dirInfo = new DirectoryInfoDialog();
                dirInfo.show(fm, BaseExplorerActivity.TAG_DIALOG);
                return true;
            case R.id.search:
                Intent sintent = new Intent(activity, SearchActivity.class);
                startActivity(sintent);
                return true;
            case R.id.paste:
                final PasteActionExecutor ptc = new PasteActionExecutor(activity, mCurrentPath);
                ptc.start();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onNavigate(String path) {
        if (mActionController.isActionMode()) {
            mActionController.finishActionMode();
        }
        navigateTo(path);
        mListView.setSelection(0);
    }

    private void initDirectory(Bundle savedInstanceState, Intent intent) {
        String defaultdir;

        if (savedInstanceState != null) {
            defaultdir = savedInstanceState.getString("location");
        } else {
            try {
                File dir = new File(intent.getStringExtra(BaseExplorerActivity.EXTRA_SHORTCUT));

                if (dir.exists() && dir.isDirectory()) {
                    defaultdir = dir.getAbsolutePath();
                } else {
                    if (dir.exists() && dir.isFile())
                        listItemAction(dir);
                    defaultdir = Settings.getDefaultDir();
                }
            } catch (Exception e) {
                defaultdir = Settings.getDefaultDir();
            }
        }

        File dir = new File(defaultdir);

        if (dir.exists() && dir.isDirectory())
            navigateTo(dir.getAbsolutePath());
    }

    private static final class NavigateRunnable implements Runnable {
        private final WeakReference<BaseExplorerActivity> abActivityWeakRef;
        private final String target;

        NavigateRunnable(final BaseExplorerActivity abActivity, final String path) {
            this.abActivityWeakRef = new WeakReference<>(abActivity);
            this.target = path;
        }

        @Override
        public void run() {
            BaseExplorerActivity abActivity = abActivityWeakRef.get();
            if (abActivity != null) {
                abActivity.getCurrentBrowserFragment().navigateTo(target);
            } else {
                Log.w(this.getClass().getName(),
                        "NavigateRunnable: activity weakref returned null, can't navigate");
            }
        }
    }

    public void onBookmarkClick(File file) {
        if (!file.exists()) {
            Toast.makeText(mActivity, getString(R.string.cantopenfile),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (file.isDirectory()) {
            navigateTo(file.getAbsolutePath());

            mListView.setSelection(0);
        } else {
            listItemAction(file);
        }
    }

    public boolean onBackPressed() {
        if (mUseBackKey && mActionController.isActionMode()) {
            mActionController.finishActionMode();
            return true;
        } else if (mUseBackKey && !mCurrentPath.equals("/")) {
            File file = new File(mCurrentPath);
            navigateTo(file.getParent());

            mListView.setSelection(mListAdapter.getPosition(file.getPath()));
            return true;
        } else if (mUseBackKey && mCurrentPath.equals("/")) {
            Toast.makeText(mActivity, getString(R.string.pressbackagaintoquit),
                    Toast.LENGTH_SHORT).show();

            mUseBackKey = false;
            return false;
        } else if (!mUseBackKey && mCurrentPath.equals("/")) {
            mActivity.finish();
            return false;
        }

        return true;
    }
}
