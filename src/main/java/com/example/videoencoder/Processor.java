package com.example.videoencoder;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Processor {

    private final List<VideoEncodingSetting> pixelHeights = Arrays.asList(
            new VideoEncodingSetting(1080, 6000),
            new VideoEncodingSetting(720, 3000),
            new VideoEncodingSetting(480, 1000),
            new VideoEncodingSetting(360, 600)
    );

    private JmsTemplate jmsTemplate;

    public Processor(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @JmsListener(destination = "video-file")
    public void start(String path) {
        try {
            processVideo(path);
            jmsTemplate.convertAndSend("encoder-response", 1 + " " + path);
        } catch (Exception e) {
            e.printStackTrace();
            jmsTemplate.convertAndSend("encoder-response", -1 + " " + path);
        }
    }

    private void processVideo(String path) throws Exception {

        final int videoPixelHeight = getVideoPixelHeight(path);

        List<VideoEncodingSetting> relevantEncodingSettings = pixelHeights.stream().filter(encodingSetting -> encodingSetting.getPixelHeight() <= videoPixelHeight).collect(Collectors.toList());

        for (VideoEncodingSetting encodingSetting : relevantEncodingSettings) {
            executeCommand(encodingSetting.getEncodingCommand(path), "Failed encoding " + encodingSetting.getPixelHeight() + "!");
        }

        createManifest(relevantEncodingSettings, path);
    }

    private int getVideoPixelHeight(String path) throws Exception{

        String output = executeCommand("packager input=" + path + " --dump_stream_info", "Could not find video pixel height");

        String pixelHeight = output.substring(output.indexOf("height") + 8, output.indexOf("height") + 12).trim();

        return Integer.parseInt(pixelHeight);
    }

    private void createManifest(List<VideoEncodingSetting> encodingSettings, String path) throws Exception {

        StringBuilder manifestCommand = new StringBuilder("packager ");

        String firstVideoPath = encodingSettings.get(0).getEncodedPath(path);
        manifestCommand.append("input=" + firstVideoPath + ",stream=audio,output=" + firstVideoPath.substring(0, firstVideoPath.length() - 4) + "audio.mp4 ");

        for (VideoEncodingSetting encodingSetting : encodingSettings) {
            manifestCommand.append("input=" + encodingSetting.getEncodedPath(path) + ",stream=video,output=" + encodingSetting.getEncodedPath(path) + " ");
        }

        manifestCommand.append("--mpd_output " + path.substring(0, path.length() - 4) + ".mpd");

        executeCommand(manifestCommand.toString(), "Could not create manifest");
    }

    private String executeCommand(String command, String errorMessage) throws Exception {

        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");

        Process process;

        if (isWindows) {
            process = Runtime.getRuntime()
                    .exec("cmd /c " + command);
        } else {
            process = Runtime.getRuntime()
                    .exec("sh -c " + command);
        }

        BufferedReader reader;

        if (command.startsWith("ffmpeg")) {
            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        }

        StringBuilder output = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            output.append(line + " ");
        }

        int exitVal = process.waitFor();
        if (exitVal == 0 ) {
            return output.toString();
        } else {
            throw new Exception(errorMessage);
        }
    }
}
