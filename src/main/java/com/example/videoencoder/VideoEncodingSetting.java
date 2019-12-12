package com.example.videoencoder;

public class VideoEncodingSetting {

    private int pixelHeight;
    private int bitrate;

    public VideoEncodingSetting(int pixelHeight, int bitrate) {
        this.pixelHeight = pixelHeight;
        this.bitrate = bitrate;
    }

    public int getPixelHeight() {
        return pixelHeight;
    }

    public String getEncodingCommand(String path) {
        return "ffmpeg -i " + path + " " +
                "-c:a copy " +
                "-vf scale=-2:" + this.getPixelHeight() + " " +
                "-c:v libx264 -profile:v baseline -level:v 3.0 " +
                "-x264-params scenecut=0:open_gop=0:min-keyint=72:keyint=72 " +
                "-minrate " + this.bitrate + "k -maxrate " + this.bitrate + "k -bufsize " + this.bitrate + "k -b:v "+ this.bitrate + "k " +
                "-y " + getEncodedPath(path);
    }

    public String getEncodedPath(String path) {
        String strippedPath = path.substring(0, path.length() - 4);
        return strippedPath + this.pixelHeight + ".mp4";
    }
}
