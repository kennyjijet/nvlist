package nl.weeaboo.vn.sound;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import nl.weeaboo.game.entity.Entity;
import nl.weeaboo.vn.IScreen;

public interface ISoundModule {

    /**
     * Creates a new sound entity that can be used for playing the audio file specified by {@code filename}.
     *
     * @param stype The kind of sound to start playing (voice, sfx, music, ...)
     * @param filename Relative resource path of the audio file.
     * @param callStack The Lua callstack from which this method was called.
     *
     * @throws FileNotFoundException If no sound data could be found for the specified filename.
     */
    public Entity createSound(IScreen screen, SoundType stype, String filename, String[] callStack)
            throws IOException;

    /**
     * Returns the human-readable name of a sound file.
     *
     * @return The name, or {@code null} if no human-readable name is defined in {@code snd.xml}.
     */
    public String getDisplayName(String filename);

    /**
     * Returns the paths for all sound files in the specified folder.
     */
    public Collection<String> getSoundFiles(String folder);

    /**
     * Returns the sound controller, which provides functions for manipulating audio playback.
     */
    public ISoundController getSoundController();

}
