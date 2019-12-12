package com.example.videoencoder;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
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
            jmsTemplate.convertAndSend("encoder-response", -1 + " " + path);
            e.printStackTrace();
        }
    }

    private void processVideo(String path) throws Exception {

        final int videoPixelHeight = getVideoPixelHeight(path);

        List<VideoEncodingSetting> relevantEncodingSettings = pixelHeights.stream().filter(encodingSetting -> encodingSetting.getPixelHeight() <= videoPixelHeight).collect(Collectors.toList());

        for (VideoEncodingSetting encodingSetting : relevantEncodingSettings) {
            Process process = executeCommand(encodingSetting.getEncodingCommand(path));

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("Successfully encoded in " + encodingSetting.getPixelHeight() + "!");
            } else {
                throw new Exception("Failed encoding " + encodingSetting.getPixelHeight() + "! Exit value: " + exitVal);
            }
        }

        createManifest(relevantEncodingSettings, path);
    }

    private void createManifest(List<VideoEncodingSetting> encodingSettings, String path) throws Exception {

        StringBuilder manifestCommand = new StringBuilder("packager ");

        String firstVideoPath = encodingSettings.get(0).getEncodedPath(path);
        manifestCommand.append("input=" + firstVideoPath + ",stream=audio,output=" + firstVideoPath.substring(0, firstVideoPath.length() - 4) + "audio.mp4 ");

        for (VideoEncodingSetting encodingSetting : encodingSettings) {
            manifestCommand.append("input=" + encodingSetting.getEncodedPath(path) + ",stream=video,output=" + encodingSetting.getEncodedPath(path) + " ");
        }

        manifestCommand.append("--mpd_output " + path.substring(0, path.length() - 4) + ".mpd");

        System.out.println(manifestCommand.toString());

        Process process = executeCommand(manifestCommand.toString());

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        int exitVal = process.waitFor();
        if (exitVal == 0) {
            System.out.println("Successfully created manifest!");
        } else {
            System.err.println("Failed to create manifest! Exit value: " + exitVal);
            throw new Exception("Could not create manifest");
        }
    }

    private Process executeCommand(String command) throws IOException {

        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");

        if (isWindows) {
            return Runtime.getRuntime()
                    .exec("cmd /c " + command);
        } else {
            return Runtime.getRuntime()
                    .exec("sh -c " + command);
        }
    }

    private int getVideoPixelHeight(String path) throws Exception{

        Process process = executeCommand("packager input=" + path + " --dump_stream_info");

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        int pixelHeight = -1;

        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            if (line.startsWith(" height:"))
                pixelHeight = Integer.parseInt(line.substring(line.indexOf(":") + 2));
        }

        int exitVal = process.waitFor();
        if (exitVal == 0 && pixelHeight != -1) {
            System.out.println("Successfully found video pixel height!");
            return pixelHeight;
        } else {
            System.err.println("Failed to find video pixel height! Exit value: " + exitVal);
        }

        throw new Exception("Could not find video pixel height");
    }
}
