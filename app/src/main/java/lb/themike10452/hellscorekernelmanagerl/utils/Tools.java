package lb.themike10452.hellscorekernelmanagerl.utils;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by Mike on 2/22/2015.
 */
public class Tools {

    public final static int FLAG_ROOT_STATE = 0001;

    private static Tools instance;

    private List<String> stdOut, stdErr;
    private Shell.Interactive interactive;

    public static final Tools getInstance() {
        return instance != null ? instance : new Tools();
    }

    private Tools() {
        instance = this;
        stdOut = new ArrayList<>();
        stdErr = new ArrayList<>();
    }

    public void initRootShell(final Handler handler) {
        interactive = new Shell.Builder()
                .useSU()
                .setWantSTDERR(true)
                .setMinimalLogging(true)
                .open(new Shell.OnCommandResultListener() {
                    @Override
                    public void onCommandResult(int commandCode, int exitCode, List<String> output) {
                        Message message = new Message();
                        message.arg1 = FLAG_ROOT_STATE;
                        message.arg2 = exitCode;
                        handler.sendMessage(message);
                    }
                });
    }

    public void exec(String... cmd) {
        List<String> cmds = new ArrayList<>();
        for (String line : cmd)
            cmds.add(line);

        interactive.addCommand(cmds);
    }

    public String readLineFromFile(String filePath) {
        File file = new File(filePath);
        return readLineFromFile(file);
    }

    public String readLineFromFile(File file) {
        FileReader fileReader = null;
        if (file.exists()) {
            if (file.canRead()) {
                BufferedReader bufferedReader = null;
                try {
                    bufferedReader = new BufferedReader(fileReader = new FileReader(file));
                    return bufferedReader.readLine();
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
                try {
                    Process p = new ProcessBuilder("su", "-c", "/system/bin/sh").start();

                    OutputStream outputStream = p.getOutputStream();
                    OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                    writer.write(String.format("cat %s", file.getAbsolutePath()));
                    writer.flush();
                    writer.close();
                    outputStream.close();

                    stdOut.clear();
                    stdErr.clear();

                    Streamer outStreamer = new Streamer(p.getInputStream(), stdOut);
                    Streamer errStreamer = new Streamer(p.getErrorStream(), stdOut);

                    outStreamer.start();
                    errStreamer.start();

                    try {
                        int exitCode = p.waitFor();
                        try {
                            outStreamer.join();
                            errStreamer.join();
                        } catch (InterruptedException ignored) {
                        }

                        if (exitCode == 0) {
                            if (stdErr.isEmpty()) {
                                if (!stdOut.isEmpty())
                                    return stdOut.get(0);
                                else
                                    return null;
                            } else return null;
                        } else {
                            return null;
                        }
                    } catch (InterruptedException e) {
                        return null;
                    }

                } catch (IOException ioe) {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    private class Streamer extends Thread {

        private InputStream inputStream;
        private List<String> result;

        public Streamer(@NonNull InputStream inputStream, @NonNull List<String> result) {
            this.result = result;
            this.inputStream = inputStream;
        }

        @Override
        public void run() {

            BufferedReader reader = null;
            InputStreamReader inputStreamReader = null;
            try {
                reader = new BufferedReader(inputStreamReader = new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.length() > 0) {
                        result.add(line);
                    }
                }
            } catch (IOException ignored) {
            } finally {
                try {
                    if (reader != null)
                        reader.close();
                    if (inputStreamReader != null)
                        inputStreamReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
