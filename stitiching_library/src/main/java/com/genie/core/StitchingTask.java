package com.genie.core;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;


import com.genie.listeners.CompletionListener;
import com.genie.utils.VideoStitchingRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Karthik on 22/01/16.
 */
public class StitchingTask implements Runnable {

    private Context context;
    private VideoStitchingRequest videoStitchingRequest;
    private CompletionListener completionListener;
    private String mFfmpegInstallPath;

    public StitchingTask(Context context, String mFfmpegInstallPath, VideoStitchingRequest stitchingRequest, CompletionListener completionListener) {
        this.context = context;
        this.mFfmpegInstallPath = mFfmpegInstallPath;
        this.videoStitchingRequest = stitchingRequest;
        this.completionListener = completionListener;
    }


    @Override
    public void run() {
        stitchVideo(context, mFfmpegInstallPath, videoStitchingRequest, completionListener);
    }


    private void stitchVideo(Context context, String mFfmpegInstallPath, VideoStitchingRequest videoStitchingRequest, final CompletionListener completionListener) {


        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ffmpeg_videos";
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File inputfile = new File(path, "input.txt");

        try {
            inputfile.createNewFile();
            FileOutputStream out = new FileOutputStream(inputfile);
            for (String string : videoStitchingRequest.getInputVideoFilePaths()) {
                out.write(("file " + "'" + string + "'").getBytes());
                out.write("\n".getBytes());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        String[] sampleFFmpegcommand = {mFfmpegInstallPath, "-f", "concat", "-i", inputfile.getAbsolutePath(), "-codec", "copy", videoStitchingRequest.getOutputPath()};
        try {
            Process ffmpegProcess = new ProcessBuilder(sampleFFmpegcommand)
                    .redirectErrorStream(true).start();

            String line;

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(ffmpegProcess.getInputStream()));
            Log.d("***", "*******Starting FFMPEG");
            while ((line = reader.readLine()) != null) {

                Log.d("***", "***" + line + "***");
            }
            Log.d(null, "****ending FFMPEG****");

            ffmpegProcess.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        inputfile.delete();

        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                completionListener.onProcessCompleted("Video Stitiching Comleted");
            }
        });

    }
}
