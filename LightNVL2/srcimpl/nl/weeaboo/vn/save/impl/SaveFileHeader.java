package nl.weeaboo.vn.save.impl;

import nl.weeaboo.vn.core.impl.Storage;
import nl.weeaboo.vn.core.impl.UnmodifiableStorage;
import nl.weeaboo.vn.save.IStorage;
import nl.weeaboo.vn.save.ThumbnailInfo;

public final class SaveFileHeader {

    private final long timestamp;
    private ThumbnailInfo thumbnail;
    private IStorage userData = new Storage();
    private IStorage savepointStorage = new Storage();

    public SaveFileHeader(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ThumbnailInfo getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(ThumbnailInfo thumbnail) {
        this.thumbnail = thumbnail;
    }

    public IStorage getUserData() {
        return UnmodifiableStorage.fromCopy(userData);
    }

    public void setUserData(IStorage data) {
        userData.clear();
        userData.addAll(data);
    }

    public IStorage getSavepointStorage() {
        return UnmodifiableStorage.fromCopy(savepointStorage);
    }

    public void setSavepointStorage(IStorage data) {
        savepointStorage.clear();
        savepointStorage.addAll(data);
    }

}
