package nl.weeaboo.vn.save;

import nl.weeaboo.vn.IStorage;
import nl.weeaboo.vn.impl.Storage;
import nl.weeaboo.vn.impl.UnmodifiableStorage;

public final class SaveParams {

    private final IStorage userData = new Storage();
    private ThumbnailInfo thumbnailInfo;
    private byte[] thumbnailData;

    public IStorage getUserData() {
        return UnmodifiableStorage.fromCopy(userData);
    }

    public void setUserData(IStorage data) {
        userData.clear();
        userData.addAll(data);
    }

    public ThumbnailInfo getThumbnailInfo() {
        return thumbnailInfo;
    }

    public byte[] getThumbnailData() {
        return thumbnailData;
    }

    public void setThumbnail(ThumbnailInfo info, byte[] data) {
        thumbnailInfo = info;
        thumbnailData = data;
    }

}
