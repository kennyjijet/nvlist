package nl.weeaboo.vn.sound.impl;

import java.io.IOException;
import java.util.Collection;

import nl.weeaboo.game.entity.Entity;
import nl.weeaboo.vn.IEnvironment;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.IScreen;
import nl.weeaboo.vn.impl.EntityHelper;
import nl.weeaboo.vn.impl.ResourceLoader;
import nl.weeaboo.vn.sound.ISoundController;
import nl.weeaboo.vn.sound.ISoundModule;
import nl.weeaboo.vn.sound.SoundType;

public abstract class SoundModule implements ISoundModule {

    protected final IEnvironment env;
    protected final INotifier notifier;
    protected final ResourceLoader resourceLoader;
    protected final EntityHelper entityHelper;

    private final SoundStore soundStore;
    private final ISoundController soundController;

    public SoundModule(IEnvironment env, ResourceLoader resourceLoader, SoundStore soundStore) {
        this(env, resourceLoader, soundStore, new SoundController());
    }

    public SoundModule(IEnvironment env, ResourceLoader resourceLoader, SoundStore soundStore,
            ISoundController soundController) {

        this.env = env;
        this.notifier = env.getNotifier();
        this.resourceLoader = resourceLoader;
        this.entityHelper = new EntityHelper(env.getPartRegistry());

        this.soundStore = soundStore;
        this.soundController = soundController;
    }

    @Override
    public Entity createSound(IScreen screen, SoundType stype, String filename, String[] callStack) throws IOException {
        // TODO LVN-01 Re-enable analytics, seen log

        Entity e = entityHelper.createScriptableEntity(screen);
        entityHelper.addImageParts(e);
        return e;
    }

    @Override
    public String getDisplayName(String filename) {
        String normalizedFilename = resourceLoader.normalizeFilename(filename);
        if (normalizedFilename == null) {
            return null;
        }
        return soundStore.getDisplayName(normalizedFilename);
    }

    @Override
    public Collection<String> getSoundFiles(String folder) {
        return resourceLoader.getMediaFiles(folder);
    }

    @Override
    public ISoundController getSoundController() {
        return soundController;
    }

}
