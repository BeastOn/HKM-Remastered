package lb.themike10452.hellscorekernelmanagerl.utils;

import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Mike on 3/6/2015.
 */
public class SUShell {

    protected static final String VALID_EOF = "--@M^I*K#E_--";
    protected static final String ERR_EOF = "--!@M^I*K#E_--";
    protected static boolean running;
    private ArrayList<String> STDIN;
    private Handler mHandler;
    private OutputStreamWriter outputStreamWriter;
    private Process process;
    private StreamReader streamReader;
    private Thread readerThread;

    public SUShell(Handler handler) {
        mHandler = handler;
        running = false;
        STDIN = new ArrayList<>();
    }

    public boolean startShell() {
        if (!running) {
            try {
                process = new ProcessBuilder("/system/bin/sh", "-c", "su")
                        .redirectErrorStream(false)
                        .start();

                outputStreamWriter = new OutputStreamWriter(process.getOutputStream());
                streamReader = new StreamReader(process.getInputStream(), STDIN);
                readerThread = new Thread(streamReader);
                readerThread.start();

                run("id");

                /*while (STDIN.size() == 0) {
                    if (run("id") == null)
                        return false;
                }*/

                return STDIN.size() > 0
                        && STDIN.get(0).contains("uid=0");

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public void stopShell() {
        running = false;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                readerThread.interrupt();
                try {
                    outputStreamWriter.close();
                    process.destroy();
                } catch (Exception ignored) {
                }
            }
        }, 100);
    }

    public synchronized List<String> run(String cmd) {
        clear();
        try {
            streamReader.addCommand(cmd);

            //wait for unfinished jobs
            while (streamReader.hasUnfinishedJobs()) ;

            if (STDIN.size() == 0 || STDIN.contains(ERR_EOF)) {
                return null;
            } else {
                return (List<String>) STDIN.clone();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void clear() {
        STDIN.clear();
    }

    public class StreamReader implements Runnable {

        private List<String> STDIN;
        private InputStream inputStream;
        private boolean hasUnfinishedJobs;

        public StreamReader(InputStream inputStream, List<String> STDIN) {
            this.inputStream = inputStream;
            this.STDIN = STDIN;
        }

        @Override
        public void run() {
            running = true;
            Scanner scanner = new Scanner(inputStream);
            while (running && scanner.hasNextLine()) {
                String line = scanner.nextLine();
                boolean reachedEOF = line.equals(VALID_EOF) || line.equals(ERR_EOF);

                if (!line.equals(VALID_EOF))
                    addToStdIn(line);

                setHasUnfinishedJobs(!reachedEOF);
            }
            setHasUnfinishedJobs(false);
            running = false;
            scanner.close();
        }

        public synchronized void addCommand(String command) throws IOException {
            setHasUnfinishedJobs(true);
            outputStreamWriter.write(command.concat(" && echo ").concat(VALID_EOF).concat(" || echo ").concat(ERR_EOF).concat("\n"));
            outputStreamWriter.flush();
        }

        private synchronized void addToStdIn(String line) {
            STDIN.add(line);
        }

        public synchronized boolean hasUnfinishedJobs() {
            return hasUnfinishedJobs;
        }

        private synchronized void setHasUnfinishedJobs(boolean hasJobs) {
            hasUnfinishedJobs = hasJobs;
        }
    }
}
