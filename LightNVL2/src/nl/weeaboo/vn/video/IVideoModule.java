package nl.weeaboo.vn.video;

import java.io.IOException;

public interface IVideoModule {

    /**
     * Starts a full-screen video.
     *
     * @param filename Path to the video file that should be played.
     * @return An {@link IVideo} object that can be used to control playback.
     */
    public IVideo movie(String filename) throws IOException;

    /**
     * @return The currently playing full-screen video, or {@code null} if there's no full-screen video
     *         currently playing.
     */
    public IVideo getBlocking();

    /**
     * Sets the resource folder that videos are loaded from.
     */
    public void setVideoFolder(String videoFolder);

}
