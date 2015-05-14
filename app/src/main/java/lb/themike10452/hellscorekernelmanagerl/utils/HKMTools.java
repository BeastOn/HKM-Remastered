package lb.themike10452.hellscorekernelmanagerl.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lb.themike10452.hellscorekernelmanagerl.R;

/**
 * Created by Mike on 2/22/2015.
 */
public class HKMTools {

    public final static int FLAG_ROOT_STATE = 1;

    private static HKMTools instance;

    private List<String> cmds;
    private SUShell mShell;

    private HKMTools() {
        instance = this;
        cmds = new ArrayList<>();
    }

    public static HKMTools getInstance() {
        return instance != null ? instance : new HKMTools();
    }

    public static Integer parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    public static Long parseLong(String str) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    public static int indexOf(long obj, long[] objs) {
        for (int i = 0; i < objs.length; i++) {
            if (objs[i] == obj)
                return i;
        }
        return -1;
    }

    public static int indexOf(String obj, String[] objs) {
        for (int i = 0; i < objs.length; i++) {
            if (objs[i].equals(obj))
                return i;
        }
        return -1;
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics));
    }

    public static void reverseArray(Object[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            Object tmp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = tmp;
        }
    }

    public static void reverseArray(long[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            long tmp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = tmp;
        }
    }

    public void initRootShell(final Handler handler) {
        mShell = new SUShell(handler);
        Message message = new Message();
        message.arg1 = FLAG_ROOT_STATE;
        message.arg2 = mShell.startShell() ? 0 : 1;
        handler.sendMessage(message);
    }

    public void stopShell() {
        if (mShell != null) {
            mShell.stopShell();
        }
    }

    public void getReady() {
        cmds.clear();
    }

    public void addCommand(String... cmd) {
        Collections.addAll(cmds, cmd);
    }

    public void flush() {
        for (String cmd : cmds) {
            mShell.run(cmd);
        }
        cmds.clear();
    }

    public void run(final String command) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mShell.run(command);
            }
        }).start();
    }

    public List<String> getCommandOutput(String command) {
        return mShell.run(command);
    }

    public List<String> getRecentCommandsList() {
        List<String> list = new ArrayList<>();
        list.addAll(cmds);
        return list;
    }

    public String readLineFromFile(String filePath) {
        File file = new File(filePath);
        return readLineFromFile(file);
    }

    public String readLineFromFile(File file) {
        try {
            return readFromFile(file).get(0);
        } catch (Exception e) {
            return null;
        }
    }

    public String readBlockFromFile(File file) {
        try {
            StringBuilder fileData = new StringBuilder();
            for (String line : readFromFile(file)) {
                fileData.append(line).append("\n");
            }
            return fileData.toString().trim();
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> readFromFile(String path) {
        File file = new File(path);
        return readFromFile(file);
    }

    public List<String> readFromFile(File file) {
        FileReader fileReader = null;
        if (file.canRead()) {
            BufferedReader bufferedReader = null;
            List<String> lines = new ArrayList<>();
            try {
                String line;
                bufferedReader = new BufferedReader(fileReader = new FileReader(file));
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.length() > 0)
                        lines.add(line);
                }
                return lines;
            } catch (IOException ioe) {
                return null;
            } finally {
                try {
                    if (bufferedReader != null)
                        bufferedReader.close();
                    if (fileReader != null)
                        fileReader.close();
                } catch (Exception ignored) {
                }
            }
        } else {
            return mShell.run("cat " + file);
        }
    }

    public static class ScriptUtils {

        public static final String CPU_SETTINGS_SCRIPT_NAME = "98cpu_settings";
        public static final String GOV_SETTINGS_SCRIPT_NAME = "99cpu_gov_settings";
        public static final String GPU_SETTINGS_SCRIPT_NAME = "97gpu_settings";
        public static final String LCD_SETTINGS_SCRIPT_NAME = "90lcd_settings";
        public static final String SND_SETTINGS_SCRIPT_NAME = "90sound_settings";
        public static final String TTC_SETTINGS_SCRIPT_NAME = "90touch_settings";
        public static final String MSC_SETTINGS_SCRIPT_NAME = "90misc_settings";
        public static final String SYS_SCRIPT_PATH = "/system/su.d/90kernelSettings";

        private static String scriptsDir;

        public static void createScript(Context context, SharedPreferences preferences, String prefKey, String scriptName, List<String> commandList) {
            boolean sobEnabled = preferences.getBoolean(prefKey, false);
            if (!sobEnabled) {
                clearScript(context, scriptName);
            } else {
                try {
                    writeScript(context, scriptName, commandList, isDelicate(scriptName));
                } catch (IOException e) {
                    Toast.makeText(context, R.string.message_script_failed, Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        public static void writeScript(Context appContext, String scriptName, List<String> commandList, boolean isDelicate) throws IOException {
            initScriptsDir(appContext);

            File f = new File(scriptsDir.concat(scriptName));
            if (f.exists()) {
                f.delete();
            } else {
                new File(scriptsDir).mkdirs();
            }
            f.createNewFile();
            PrintWriter writer = new PrintWriter(f);
            writer.println("#!/system/bin/sh");
            if (isDelicate) {
                writer.println("if [[ \"$1\" != \"--force\" ]]; then exit 0; fi");
            }
            for (String cmd : commandList) {
                writer.println(cmd);
            }
            writer.close();
            HKMTools tools = HKMTools.getInstance();
            tools.addCommand("chmod 700 " + f.getAbsolutePath());
            tools.flush();
        }

        public static void clearScript(Context appContext, String scriptName) {
            initScriptsDir(appContext);

            new File(scriptsDir.concat(scriptName)).delete();
        }

        public static void createSystemScript(Context appContext) throws IOException {
            initScriptsDir(appContext);

            String tmpScript = scriptsDir.concat("tmp");

            PrintWriter writer = new PrintWriter(tmpScript);
            writer.println(getSysScriptDefaultContent());
            writer.close();

            HKMTools tools = HKMTools.getInstance();
            tools.getReady();
            tools.addCommand(
                    "busybox mount -o remount,rw /system",
                    "mkdir /system/su.d",
                    String.format("mv %s %s", tmpScript, SYS_SCRIPT_PATH),
                    "chmod 700 /system/su.d",
                    "chmod 700 " + SYS_SCRIPT_PATH,
                    "busybox mount -o remount,ro /system"
            );
            tools.flush();
        }

        public static boolean checkSystemScript(Context appContext) {
            initScriptsDir(appContext);
            String s = getInstance().readBlockFromFile(new File(SYS_SCRIPT_PATH));
            return s != null && s.trim().equals(getSysScriptDefaultContent());
        }

        public static String getScriptsDir(Context context) {
            return scriptsDir == null ?
                    scriptsDir = context.getFilesDir().getAbsolutePath().concat("/scripts/") : scriptsDir;
        }

        public static String getSysScriptDefaultContent() {
            return "/system/xbin/busybox run-parts ".concat(scriptsDir);
        }

        private static void initScriptsDir(Context context) {
            getScriptsDir(context);
        }

        private static boolean isDelicate(String scriptName) {
            return scriptName.equals(CPU_SETTINGS_SCRIPT_NAME) ||
                    scriptName.equals(GPU_SETTINGS_SCRIPT_NAME);
        }

    }
}
