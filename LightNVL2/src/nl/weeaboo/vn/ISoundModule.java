package nl.weeaboo.vn;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import nl.weeaboo.game.entity.Entity;

public interface ISoundModule {

    /**
     * Creates a new sound entity that can be used for playing the audio file specified by
     * <code>filename</code>.
     *
     * @param stype The kind of sound to start playing (voice, sfx, music, ...)
     * @param filename The file to load
     * @param callStack The Lua callstack from which the preload function was called.
     *
     * @throws FileNotFoundException If no sound data could be found for the specified filename.
     */
    public Entity createSound(SoundType stype, String filename, String[] callStack) throws IOException;

    /**
     * @return The human-readable name of the sound specified by
     *         <code>filename</code>, or <code>null</code> if none is defined
     *         in <code>snd.xml</code>.
     */
    public String getName(String filename);

    /**
     * Returns the paths for all sound files in the specified folder
     */
    public Collection<String> getSoundFiles(String folder);

}
