package org.jscsi.target.configuration;

import java.io.File;

public class StorageFileTargetInfo extends TargetInfo {
    private File storageFilePath;

    public StorageFileTargetInfo(String targetName, String targetAlias, File storageFilePath,
        final long storageTargetLength) {
        super(targetName, targetAlias, storageTargetLength);
        this.storageFilePath = storageFilePath;
    }

    public File getStorageFilePath() {
        return storageFilePath;
    }
}
