package lb.themike10452.hellscorekernelmanagerl.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import at.markushi.ui.action.CloseAction;
import at.markushi.ui.action.DrawerAction;
import lb.themike10452.filebrowser.Adapter;
import lb.themike10452.filebrowser.FileBrowser;
import lb.themike10452.hellscorekernelmanagerl.CustomAdapters.ProfileListAdapter;
import lb.themike10452.hellscorekernelmanagerl.MainActivity;
import lb.themike10452.hellscorekernelmanagerl.R;
import lb.themike10452.hellscorekernelmanagerl.utils.HKMTools;
import lb.themike10452.hellscorekernelmanagerl.utils.UIHelper;

/**
 * Created by Mike on 6/3/2015.
 */
public class ProfileManager extends Fragment implements HKMFragment, View.OnClickListener {

    public static class MetaInfo {
        String Name, Author, Device, Kernel, Date, Description;
    }

    public static final String EXT = "kpf";
    public static final String META = "meta.inf";
    private static final int META_VER = 1;

    private static ProfileManager instance;

    private Activity mActivity;
    private File scriptsDir;
    private View mView;
    private boolean animFromCorner;
    private int dummyInt;

    public static ProfileManager getInstance() {
        return instance != null ? instance : new ProfileManager();
    }

    public ProfileManager() {
        instance = this;
        animFromCorner = false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        scriptsDir = new File(HKMTools.ScriptUtils.getScriptsDir(mActivity));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_profile_manager, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViewById(R.id.exportBtn).setOnClickListener(this);
        findViewById(R.id.importBtn).setOnClickListener(this);
        findViewById(R.id.saveBtn).setOnClickListener(this);
        findViewById(R.id.saveProfileBtn).setOnClickListener(this);
        findViewById(R.id.loadBtn).setOnClickListener(this);
        findViewById(R.id.deleteBtn).setOnClickListener(this);
        findViewById(R.id.clearBtn).setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == FileBrowser.RESULT_DIRECTORY_SELECTED) {
                final EditText editText = (EditText) findViewById(R.id.outDirEt);
                if (editText != null) {
                    editText.setText(data.getStringExtra(FileBrowser.DATA_DIRECTORY));
                }
            }
        } else if (requestCode == 1) {
            if (resultCode == FileBrowser.RESULT_FILE_SELECTED) {
                final File file = new File(data.getStringExtra(FileBrowser.DATA_FILEPATH));
                MetaInfo meta = extractMeta(file);
                if (meta != null) {
                    previewProfile(meta, file);
                }
            }
        }
    }

    private final void exportProfile() {
        final ViewGroup overlayView = (ViewGroup) findViewById(R.id.overlayView);
        final View contentView = LayoutInflater.from(mActivity).inflate(R.layout.dialog_export_profile, null, false);
        ((TextView) contentView.findViewById(R.id.deviceName)).setText(getString(R.string.device_name, Build.DEVICE));
        ((TextView) contentView.findViewById(R.id.kernelVersion)).setText(getString(R.string.kernel_version, HKMTools.getFormattedKernelVersion(1)));
        ((EditText) contentView.findViewById(R.id.outDirEt)).setText(Environment.getExternalStorageDirectory().toString());

        final ArrayList<Switch> switches = new ArrayList<>();
        dummyInt = 0;

        if (scriptsDir.exists() && scriptsDir.isDirectory()) {
            final ViewGroup switchHolder = (ViewGroup) contentView.findViewById(R.id.switchHolder);
            final File[] scriptFiles = scriptsDir.listFiles();
            final LayoutInflater inflater = LayoutInflater.from(mActivity);
            final Object[][] ScripInfo = HKMTools.ScriptUtils.SCRIPTS;
            for (File script : scriptFiles) {
                if (script.isFile()) {
                    for (Object[] scriptInf : ScripInfo) {
                        if (script.getName().equals(scriptInf[0])) {
                            Switch swt = (Switch) inflater.inflate(R.layout.small_switch, null, false);
                            swt.setText(getString((int) scriptInf[1]));
                            swt.setContentDescription(scriptsDir.toString().concat(File.separator).concat(scriptInf[0].toString()));
                            switches.add(swt);
                            switchHolder.addView(swt);
                        }
                    }
                }
            }
        }

        if (switches.size() == 0) {
            Toast.makeText(mActivity, R.string.message_action_impossible1, Toast.LENGTH_LONG).show();
            closeOverlay(animFromCorner);
            return;
        }

        contentView.findViewById(R.id.forward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View forward) {
                if (dummyInt == 0) {
                    boolean bool = false;
                    for (Switch swt : switches) {
                        if (swt.isChecked()) {
                            bool = true;
                            break;
                        }
                    }
                    if (!bool) {
                        Toast.makeText(mActivity, R.string.message_action_impossible2, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    UIHelper.fadeIn(contentView.findViewById(R.id.back));
                    UIHelper.fadeOut(contentView.findViewById(R.id.switchHolder));
                    UIHelper.fadeIn(contentView.findViewById(R.id.infoHolder));
                    ((ImageButton) forward).setImageResource(R.drawable.ic_accept);
                    MainActivity.transactionManager.setActionBarTitle(getString(R.string.profile_details));
                    dummyInt++;
                } else if (dummyInt == 1) {
                    final String name = ((EditText) findViewById(R.id.profNameEt)).getText().toString().trim();
                    final String author = ((EditText) contentView.findViewById(R.id.profAuthEt)).getText().toString().trim();

                    if (name.length() < 1 || author.length() < 1) {
                        Toast.makeText(mActivity, R.string.message_action_impossible3, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    hideSoftKeyboard();
                    final File outputFile = new File(((EditText) findViewById(R.id.outDirEt)).getText().toString()
                            .concat(File.separator)
                            .concat(name)
                            .concat(".")
                            .concat(EXT)
                    );
                    if (outputFile.exists() && outputFile.isFile()) {
                        buildDialog(getString(R.string.dialog_title_fileExists), getString(R.string.dialog_message_fileExists, outputFile.toString()), true,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        closeOverlay(animFromCorner);
                                        packProfile(
                                                outputFile,
                                                name,
                                                author,
                                                ((EditText) contentView.findViewById(R.id.descEt)).getText().toString(),
                                                switches
                                        );
                                    }
                                },
                                null
                        ).show();
                    } else {
                        closeOverlay(animFromCorner);
                        packProfile(
                                outputFile,
                                ((EditText) contentView.findViewById(R.id.profNameEt)).getText().toString(),
                                ((EditText) contentView.findViewById(R.id.profAuthEt)).getText().toString(),
                                ((EditText) contentView.findViewById(R.id.descEt)).getText().toString(),
                                switches
                        );
                    }
                }
            }
        });

        contentView.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View back) {
                UIHelper.fadeOut(back);
                UIHelper.fadeOut(contentView.findViewById(R.id.infoHolder));
                UIHelper.fadeIn(contentView.findViewById(R.id.switchHolder));
                ((ImageButton) contentView.findViewById(R.id.forward)).setImageResource(R.drawable.ic_arrow_forward);
                MainActivity.transactionManager.setActionBarTitle(getString(R.string.settings_to_inc));
                dummyInt--;
            }
        });


        contentView.findViewById(R.id.outDirEt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.clearFocus();
                final Intent intent = new Intent(mActivity, FileBrowser.class);
                intent.putExtra(FileBrowser.EXTRA_SHOW_FOLDERS_ONLY, true);
                intent.putExtra(FileBrowser.EXTRA_START_DIRECTORY, ((EditText) v).getText().toString());
                startActivityForResult(intent, 0);
            }
        });

        overlayView.addView(contentView);
        MainActivity.transactionManager.setActionBarTitle(getString(R.string.settings_to_inc));
    }

    private final void importProfile() {
        FileBrowser.setFileDescriptionCallback(new FileBrowser.FileDescriptionCallback() {
            @Override
            public String onLoadDescription(File f) {
                if (Adapter.getFileExtension(f).equals(EXT)) {
                    MetaInfo meta = extractMeta(f);
                    if (meta != null) {
                        return ("Name -> ").concat(meta.Name).concat("\r\n")
                                .concat("Author -> ").concat(meta.Author).concat("\r\n")
                                .concat("Device -> ").concat(meta.Device).concat("\r\n")
                                .concat("Kernel -> ").concat(meta.Kernel).concat("\r\n")
                                .concat("Date -> ").concat(meta.Date);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        });
        Intent intent = new Intent(mActivity, FileBrowser.class);
        intent.putExtra(FileBrowser.EXTRA_ALLOWED_EXTENSIONS, new String[]{EXT});
        intent.putExtra(FileBrowser.EXTRA_USE_DESCRIPTION_CALLBACK, true);
        startActivityForResult(intent, 1);
    }

    private final void saveProfile(final File targetDir) {
        boolean bool;
        if (targetDir.exists() && targetDir.isDirectory()) {
            bool = true;
            if (targetDir.listFiles().length > 0) {
                final File[] files = targetDir.listFiles();
                for (File file : files) file.delete();
            }
        } else {
            targetDir.mkdirs();
            bool = false;
        }

        final File[] files = scriptsDir.listFiles();
        if (files != null && files.length > 0) {
            HKMTools tools = HKMTools.getInstance();
            tools.clear();
            for (final File file : files) {
                if (file.isFile()) {
                    tools.addCommand(String.format("cp %s %s", file, targetDir));
                }
            }
            if (tools.getRecentCommandsList().size() > 0) {
                tools.flush();
                Toast.makeText(mActivity, R.string.message_action_successful, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mActivity, R.string.message_action_impossible1, Toast.LENGTH_LONG).show();
                if (!bool) targetDir.delete();
            }
        }
    }

    private final void loadProfile() {
        boolean bool = true;
        MainActivity.transactionManager.setActionBarTitle(getString(R.string.load_profile));
        final File profilesDir = new File(scriptsDir, "profiles");
        if (profilesDir.exists() && profilesDir.isDirectory()) {
            final File[] files = profilesDir.listFiles();
            if (files != null && files.length > 0) {
                final ArrayList<Switch> switches = new ArrayList<>();
                final ProfileListAdapter adapter = new ProfileListAdapter(mActivity, files);
                final View contentView = LayoutInflater.from(mActivity).inflate(R.layout.dialog_load_profile, null, false);
                ((ViewGroup) findViewById(R.id.overlayView)).addView(contentView);

                final ViewGroup switchHolder = (ViewGroup) contentView.findViewById(R.id.switchHolder);
                final ViewGroup navigationBar = (ViewGroup) contentView.findViewById(R.id.navigation);
                final ImageButton forward = (ImageButton) navigationBar.findViewById(R.id.forward);
                final ImageButton back = (ImageButton) navigationBar.findViewById(R.id.back);
                final ListView listView = (ListView) contentView.findViewById(R.id.listView);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        switchHolder.removeAllViews();
                        final File[] scriptFiles = ((File) adapter.getItem(position)).listFiles();
                        if (scriptFiles != null) {
                            final LayoutInflater inflater = LayoutInflater.from(mActivity);
                            final Object[][] scriptInfo = HKMTools.ScriptUtils.SCRIPTS;
                            for (File f : scriptFiles) {
                                final String filename = f.getName();
                                for (Object[] aScriptInfo : scriptInfo) {
                                    if (filename.equals(aScriptInfo[0])) {
                                        Switch swt = (Switch) inflater.inflate(R.layout.small_switch, null, false);
                                        swt.setText((int) aScriptInfo[1]);
                                        swt.setContentDescription((String) aScriptInfo[0]);
                                        switches.add(swt);
                                        switchHolder.addView(swt);
                                    }
                                }
                            }
                        }

                        UIHelper.fadeOut(listView);
                        UIHelper.fadeIn(switchHolder);
                        UIHelper.fadeIn(navigationBar, UIHelper.Constants.GRAVITY_BOTTOM);
                        MainActivity.transactionManager.setActionBarTitle(getString(R.string.settings_to_inc));

                        forward.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                boolean bool = false;
                                for (Switch swt : switches) {
                                    if (swt.isChecked()) {
                                        bool = true;
                                        break;
                                    }
                                }
                                if (bool) {
                                    activateProfile((File) adapter.getItem(position), switches);
                                    closeOverlay(animFromCorner);
                                } else {
                                    Toast.makeText(mActivity, R.string.message_action_impossible2, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        back.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                UIHelper.fadeIn(listView);
                                UIHelper.fadeOut(switchHolder);
                                UIHelper.fadeOut(navigationBar, UIHelper.Constants.GRAVITY_BOTTOM);
                                MainActivity.transactionManager.setActionBarTitle(getString(R.string.settings_to_inc));
                            }
                        });
                    }
                });
            } else {
                bool = false;
            }
        } else {
            bool = false;
        }

        if (!bool) {
            Toast.makeText(mActivity, R.string.message_profiles_empty, Toast.LENGTH_SHORT).show();
            closeOverlay(animFromCorner);
        }
    }

    private final void activateProfile(final File targetDir, final ArrayList<Switch> switches) {
        if (!scriptsDir.exists() || !scriptsDir.isDirectory()) {
            scriptsDir.mkdirs();
        }
        HKMTools tools = HKMTools.getInstance();
        tools.clear();
        for (Switch swt : switches) {
            if (swt.isChecked()) {
                final File src = new File(targetDir, swt.getContentDescription().toString());
                final File target = new File(scriptsDir, src.getName());
                tools.addCommand(String.format("cp %s %s", src, scriptsDir));
                tools.addCommand(String.format("chown %s %s", android.os.Process.myUid(), target));
                tools.addCommand(String.format("chgrp %s %s", android.os.Process.myUid(), target));
                tools.addCommand(String.format("chmod 700 %s", target));
            }
        }
        tools.addCommand("busybox run-parts -a --force " + scriptsDir);
        tools.flush();
        Toast.makeText(mActivity, R.string.message_action_successful, Toast.LENGTH_SHORT).show();
    }

    private final void showOverlay(final AnimatorListenerAdapter adapter) {
        final View overlayView = findViewById(R.id.overlayView);
        Animator animator;
        if (animFromCorner) {
            animator = ViewAnimationUtils.createCircularReveal(overlayView, 0, 0, 0f, (float) Math.sqrt(Math.pow(overlayView.getHeight(), 2) + Math.pow(overlayView.getWidth(), 2)));
        } else {
            animator = ViewAnimationUtils.createCircularReveal(overlayView, (overlayView.getWidth()) / 2, overlayView.getHeight(), 0f, (float) Math.sqrt(Math.pow(overlayView.getHeight(), 2) + Math.pow(overlayView.getWidth() / 2, 2)));
        }
        animator.setDuration(500);
        animator.setInterpolator(new AccelerateInterpolator(2));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                overlayView.setVisibility(View.VISIBLE);
                MainActivity.transactionManager.setDrawerEnabled(false);
                MainActivity.transactionManager.setDrawerIndicator(new CloseAction());

                MainActivity.setOnBackPressedCallBack(new MainActivity.OnBackPressedCallBack() {
                    @Override
                    public void onBackPressed() {
                        closeOverlay(false);
                    }
                });

                MainActivity.setCloseActionCallback(new MainActivity.CloseActionCallback() {
                    @Override
                    public void onClose() {
                        closeOverlay(true);
                    }
                });

                if (adapter != null) {
                    adapter.onAnimationStart(animation);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                adapter.onAnimationEnd(animation);
            }
        });
        animator.start();
    }

    private final void closeOverlay(boolean toCorner) {
        final View overlayView = findViewById(R.id.overlayView);
        ((ViewGroup) overlayView).removeAllViews();
        Animator animator;
        if (animFromCorner = toCorner) {
            animator = ViewAnimationUtils.createCircularReveal(overlayView, 0, 0, (float) Math.sqrt(Math.pow(overlayView.getHeight(), 2) + Math.pow(overlayView.getWidth(), 2)), 0f);
        } else {
            animator = ViewAnimationUtils.createCircularReveal(overlayView, overlayView.getWidth() / 2, overlayView.getHeight(), (float) Math.sqrt(Math.pow(overlayView.getHeight(), 2) + Math.pow(overlayView.getWidth() / 2, 2)), 0f);
        }
        animator.setDuration(500);
        animator.setInterpolator(new AccelerateInterpolator(2));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                overlayView.setVisibility(View.INVISIBLE);
                MainActivity.transactionManager.setDrawerIndicator(new DrawerAction());
                MainActivity.transactionManager.setDrawerEnabled(true);
                MainActivity.setOnBackPressedCallBack(null);
                MainActivity.setCloseActionCallback(null);
                MainActivity.transactionManager.setActionBarTitle(getString(R.string.profMan));
            }
        });
        animator.start();
    }

    private final void packProfile(final File output, final String name, final String author, final String description, final ArrayList<Switch> switches) {
        final String device = Build.DEVICE;
        final String kernel = HKMTools.getFormattedKernelVersion(1);
        final ArrayList<File> files = new ArrayList<>();
        for (Switch swt : switches) {
            if (swt.isChecked()) {
                files.add(new File(swt.getContentDescription().toString()));
            }
        }

        switches.clear();

        new AsyncTask<Void, Void, Boolean>() {
            ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                dialog = new ProgressDialog(mActivity);
                dialog.setMessage(getString(R.string.dialog_pleaseWait));
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    final byte[] buffer = new byte[1024];
                    int readLength;
                    ZipOutputStream zipOutputStream = null;
                    FileInputStream fileInputStream = null;
                    try {
                        zipOutputStream = new ZipOutputStream(new FileOutputStream(output));
                        zipOutputStream.setMethod(ZipOutputStream.DEFLATED);
                        zipOutputStream.setLevel(9);
                        for (File f : files) {
                            if (f.canRead()) {
                                fileInputStream = new FileInputStream(f);
                                zipOutputStream.putNextEntry(new ZipEntry(f.getName()));
                                while ((readLength = fileInputStream.read(buffer)) != -1) {
                                    zipOutputStream.write(buffer, 0, readLength);
                                }
                                zipOutputStream.closeEntry();
                                fileInputStream.close();
                            } else {
                                Log.e(getClass().getName(), "failed to read from " + f);
                            }
                        }
                        zipOutputStream.putNextEntry(new ZipEntry(META));
                        zipOutputStream.write(Integer.toString(META_VER).concat("\r\n").getBytes("UTF-8"));
                        zipOutputStream.write(name.concat("\r\n").getBytes("UTF-8"));
                        zipOutputStream.write(author.concat("\r\n").getBytes("UTF-8"));
                        zipOutputStream.write(device.concat("\r\n").getBytes("UTF-8"));
                        zipOutputStream.write(kernel.concat("\r\n").getBytes("UTF-8"));
                        zipOutputStream.write(SimpleDateFormat.getDateInstance().format(new Date()).concat("\r\n").getBytes("UTF-8"));
                        zipOutputStream.write(description.getBytes("UTF-8"));
                        zipOutputStream.closeEntry();
                        zipOutputStream.close();
                        return true;
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        return false;
                    } finally {
                        if (zipOutputStream != null) {
                            zipOutputStream.close();
                        }
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                    }
                } catch (IOException ignored) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.setMessage(getString(aBoolean ? R.string.message_action_successful : R.string.message_action_unsuccessful));
                    mView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    }, 1000);
                }
            }
        }.execute();
    }

    private final void unpackProfile(final File file, final File targetDir) {
        if (!targetDir.exists() || !targetDir.isDirectory()) {
            targetDir.mkdirs();
        }

        new AsyncTask<Void, Void, Boolean>() {
            ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                dialog = new ProgressDialog(mActivity);
                dialog.setMessage(getString(R.string.dialog_pleaseWait));
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    int data;
                    InputStream inputStream;
                    FileOutputStream outputStream;
                    File outFile;
                    ZipFile zipFile = new ZipFile(file);
                    Enumeration entries = zipFile.entries();
                    if (entries != null) {
                        while (entries.hasMoreElements()) {
                            ZipEntry entry = (ZipEntry) entries.nextElement();
                            if (entry.getName().equals(META)) continue;
                            outFile = new File(targetDir, entry.getName());
                            inputStream = zipFile.getInputStream(entry);
                            outputStream = new FileOutputStream(outFile);
                            while ((data = inputStream.read()) != -1) {
                                outputStream.write(data);
                            }
                            outputStream.close();
                            inputStream.close();
                            outFile.setExecutable(true);
                            outFile.setReadable(true);
                            outFile.setWritable(true);
                        }
                        return true;
                    } else {
                        throw new IllegalArgumentException("Zero entries in " + file);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean bool) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (bool) {
                    Toast.makeText(mActivity, R.string.message_action_successful, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mActivity, R.string.message_action_unsuccessful, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private final void previewProfile(final MetaInfo meta, final File file) {
        final View contentView = LayoutInflater.from(mActivity).inflate(R.layout.profile_preview, null, false);
        ((TextView) contentView.findViewById(R.id.profNameTv)).setText(meta.Name);
        ((TextView) contentView.findViewById(R.id.profAuthTv)).setText(meta.Author);
        ((TextView) contentView.findViewById(R.id.profDeviceTv)).setText(meta.Device);
        ((TextView) contentView.findViewById(R.id.profKernelTv)).setText(meta.Kernel);
        ((TextView) contentView.findViewById(R.id.profDateTv)).setText(meta.Date);
        ((TextView) contentView.findViewById(R.id.profDescTv)).setText(meta.Description);
        AlertDialog dialog = new AlertDialog.Builder(mActivity)
                .setCancelable(true)
                .setView(contentView)
                .setPositiveButton(R.string.import_prof, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final File targetDir = new File(new File(scriptsDir, "profiles"), meta.Name);
                        if (targetDir.exists() && targetDir.isDirectory()) {
                            new AlertDialog.Builder(mActivity)
                                    .setCancelable(true)
                                    .setTitle(R.string.dialog_title_fileExists)
                                    .setMessage(getString(R.string.dialog_message_fileExists, targetDir))
                                    .setPositiveButton(R.string.replace, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            final File[] files = targetDir.listFiles();
                                            if (files != null) {
                                                for (File f : files) f.delete();
                                            }
                                            unpackProfile(file, targetDir);
                                        }
                                    })
                                    .setNeutralButton(R.string.update, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            unpackProfile(file, targetDir);
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, null)
                                    .show();
                        } else {
                            unpackProfile(file, targetDir);
                        }
                    }
                })
                .show();

        ((Button) dialog.findViewById(android.R.id.button1)).setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    private final MetaInfo extractMeta(File file) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
            Enumeration entries = zipFile.entries();
            ZipEntry entry;
            while (entries.hasMoreElements()) {
                entry = (ZipEntry) entries.nextElement();
                if (entry.getName().equals(META)) {
                    MetaInfo metaInfo = new MetaInfo();
                    InputStream inputStream = zipFile.getInputStream(entry);
                    Scanner scanner = new Scanner(inputStream);
                    int version = Integer.parseInt(scanner.nextLine());
                    if (version == 1) {
                        if (scanner.hasNextLine()) metaInfo.Name = scanner.nextLine();
                        if (scanner.hasNextLine()) metaInfo.Author = scanner.nextLine();
                        if (scanner.hasNextLine()) metaInfo.Device = scanner.nextLine();
                        if (scanner.hasNextLine()) metaInfo.Kernel = scanner.nextLine();
                        if (scanner.hasNextLine()) metaInfo.Date = scanner.nextLine();
                        if (scanner.hasNextLine()) {
                            metaInfo.Description = scanner.nextLine();
                            while (scanner.hasNextLine()) {
                                metaInfo.Description += ("\r\n").concat(scanner.nextLine());
                            }
                        }
                        scanner.close();
                        inputStream.close();
                        return metaInfo;
                    } else {
                        return null;
                    }
                }
            }
            return null;
        } catch (Exception ignored) {
            return null;
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private final AlertDialog.Builder buildDialog(String title, String message, boolean cancelable, DialogInterface.OnClickListener positive, DialogInterface.OnClickListener negative) {
        return new AlertDialog.Builder(mActivity)
                .setCancelable(cancelable)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.yes, positive)
                .setNegativeButton(R.string.no, negative);
    }

    private final void hideSoftKeyboard() {
        InputMethodManager manager = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(mView.getWindowToken(), 0);
    }

    private final View findViewById(int id) {
        return mView.findViewById(id);
    }

    @Override
    public int getTitleId() {
        return R.string.profMan;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exportBtn: {
                showOverlay(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        exportProfile();
                    }
                });
                return;
            }
            case R.id.importBtn: {
                importProfile();
                return;
            }
            case R.id.saveBtn: {
                View mV = v.findViewById(R.id.extraContent);
                mV.setVisibility(mV.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                return;
            }
            case R.id.loadBtn: {
                showOverlay(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loadProfile();
                    }
                });
                return;
            }
            case R.id.saveProfileBtn: {
                final String name = ((EditText) findViewById(R.id.newProfileName)).getText().toString();
                if (name.length() < 1) {
                    Toast.makeText(mActivity, R.string.message_name_empty, Toast.LENGTH_SHORT).show();
                } else {
                    final File profilesDir = new File(scriptsDir, "profiles");
                    final File targetDir = new File(profilesDir, name);
                    if (targetDir.exists() && targetDir.isDirectory()) {
                        new AlertDialog.Builder(mActivity)
                                .setTitle(R.string.dialog_title_fileExists)
                                .setMessage(getString(R.string.dialog_message_fileExists, targetDir))
                                .setCancelable(true)
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        saveProfile(targetDir);
                                        findViewById(R.id.extraContent).setVisibility(View.GONE);
                                        ((EditText) findViewById(R.id.newProfileName)).setText("");
                                        hideSoftKeyboard();
                                    }
                                })
                                .show();
                    } else {
                        saveProfile(targetDir);
                        findViewById(R.id.extraContent).setVisibility(View.GONE);
                        ((EditText) findViewById(R.id.newProfileName)).setText("");
                        hideSoftKeyboard();
                    }
                }
                return;
            }
            case R.id.clearBtn: {
                new AlertDialog.Builder(mActivity)
                        .setCancelable(true)
                        .setTitle(android.R.string.dialog_alert_title)
                        .setMessage(R.string.dialog_confirmation)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final File[] files = scriptsDir.listFiles();
                                if (files != null) {
                                    for (File f : files) f.delete();
                                    Toast.makeText(mActivity, R.string.message_action_successful, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
                return;
            }
            case R.id.deleteBtn: {
                showOverlay(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        boolean bool = true;
                        final LayoutInflater inflater = LayoutInflater.from(mActivity);
                        final ViewGroup overlayView = (ViewGroup) findViewById(R.id.overlayView);
                        final View contentView = inflater.inflate(R.layout.dialog_load_profile, null, false);
                        overlayView.addView(contentView);

                        final ArrayList<Switch> switches = new ArrayList<>();
                        final File[] profiles = new File(scriptsDir, "profiles").listFiles();
                        if (profiles != null && profiles.length > 0) {
                            final ViewGroup switchHolder = (ViewGroup) contentView.findViewById(R.id.switchHolder);
                            for (File f : profiles) {
                                Switch swt = (Switch) inflater.inflate(R.layout.small_switch, null, false);
                                swt.setText(f.getName());
                                swt.setContentDescription(f.getAbsolutePath());
                                switchHolder.addView(swt);
                                switches.add(swt);
                            }
                            if (switches.size() > 0) {
                                final ImageButton forward = (ImageButton) contentView.findViewById(R.id.forward);
                                final ImageButton back = (ImageButton) contentView.findViewById(R.id.back);
                                back.setVisibility(View.GONE);
                                UIHelper.fadeIn(contentView.findViewById(R.id.navigation));
                                UIHelper.fadeIn(switchHolder);

                                forward.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        boolean aBool = false;
                                        for (Switch swt : switches) {
                                            if (swt.isChecked()) {
                                                aBool = true;
                                                File target = new File(swt.getContentDescription().toString());
                                                HKMTools.clearDirectory(target);
                                                target.delete();
                                            }
                                        }

                                        if (!aBool) {
                                            Toast.makeText(mActivity, R.string.message_action_impossible2, Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(mActivity, R.string.message_action_successful, Toast.LENGTH_SHORT).show();
                                            closeOverlay(animFromCorner);
                                        }
                                    }
                                });
                            } else {
                                bool = false;
                            }
                        } else {
                            bool = false;
                        }

                        if (!bool) {
                            Toast.makeText(mActivity, R.string.message_profiles_empty, Toast.LENGTH_SHORT).show();
                            closeOverlay(animFromCorner);
                        }
                    }
                });
            }
        }
    }
}
