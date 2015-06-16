package lb.themike10452.hellscorekernelmanagerl.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static void clearDirectory(File dir) {
        final File[] filesInDir = dir.listFiles();
        if (filesInDir != null) {
            for (File file : filesInDir) {
                if (file.isDirectory()) {
                    clearDirectory(file);
                }
                file.delete();
            }
        }
    }

    public static String getFormattedKernelVersion() {
        return getFormattedKernelVersion(0);
    }

    public static String getFormattedKernelVersion(int group) {
        String procVersionStr;

        try {
            procVersionStr = new BufferedReader(new FileReader(new File("/proc/version"))).readLine();

            final String PROC_VERSION_REGEX =
                    "Linux version (\\S+) " +
                            "\\((\\S+?)\\) " +
                            "(?:\\(gcc.+? \\)) " +
                            "(#\\d+) " +
                            "(?:.*?)?" +
                            "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)";

            Pattern p = Pattern.compile(PROC_VERSION_REGEX);
            Matcher m = p.matcher(procVersionStr);

            if (!m.matches()) {
                return "Unavailable";
            } else if (m.groupCount() < 4) {
                return "Unavailable";
            } else {
                return group == 0 ? (new StringBuilder(m.group(1)).append("\n").append(
                        m.group(2)).append(" ").append(m.group(3)).append("\n")
                        .append(m.group(4))).toString() : m.group(group);
            }
        } catch (IOException e) {
            return "Unavailable";
        }
    }

    public static String getCpuArchitecture() {
        try {
            return HKMTools.getInstance().getCommandOutput("cat /proc/cpuinfo | grep ARM").get(0).split(":")[1].trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "n/a";
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

    public void clear() {
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

        public static final Object[][] SCRIPTS = new Object[][]{
                {CPU_SETTINGS_SCRIPT_NAME, R.string.cpuCtl},
                {GOV_SETTINGS_SCRIPT_NAME, R.string.govTweaks},
                {GPU_SETTINGS_SCRIPT_NAME, R.string.gpuCtl},
                {LCD_SETTINGS_SCRIPT_NAME, R.string.lcdCtl},
                {SND_SETTINGS_SCRIPT_NAME, R.string.soundCtl},
                {TTC_SETTINGS_SCRIPT_NAME, R.string.touchCtl},
                {MSC_SETTINGS_SCRIPT_NAME, R.string.miscCtl}
        };

        private static String scriptsDir;

        public static void createScript(Context context, SharedPreferences preferences, String prefKey, String scriptName, List<String> commandList) {
            boolean sobEnabled = preferences.getBoolean(prefKey, false);

            try {
                writeScript(context, scriptName, commandList, isDelicate(scriptName), sobEnabled);
            } catch (IOException e) {
                Toast.makeText(context, R.string.message_script_failed, Toast.LENGTH_SHORT).show();
                Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }

        public static void writeScript(Context appContext, String scriptName, List<String> commandList, boolean isDelicate, boolean executable) throws IOException {
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
            tools.addCommand((executable ? "chmod 700 " : "chmod 600 ") + f.getAbsolutePath());
            tools.flush();
        }

        public static void clearScript(Context appContext, String scriptName) {
            initScriptsDir(appContext);

            new File(scriptsDir.concat(scriptName)).delete();
        }

        public static void createSystemScript(Context appContext) throws IOException {
            initScriptsDir(appContext);

            File tmpFile = new File(scriptsDir);

            if (!tmpFile.exists() || !tmpFile.isDirectory()) {
                tmpFile.mkdir();
            }

            tmpFile = new File(scriptsDir.concat("tmp"));

            PrintWriter writer = new PrintWriter(tmpFile);
            writer.println(getSysScriptDefaultContent());
            writer.close();

            HKMTools tools = HKMTools.getInstance();
            tools.clear();
            tools.addCommand(
                    "busybox mount -o remount,rw /system",
                    "mkdir /system/su.d",
                    String.format("mv %s %s", tmpFile, SYS_SCRIPT_PATH),
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
