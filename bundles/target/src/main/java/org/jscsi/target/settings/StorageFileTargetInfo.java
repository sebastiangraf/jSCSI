package org.jscsi.target.settings;

public class StorageFileTargetInfo extends TargetInfo
{
    private String storageFilePath;
    
    public StorageFileTargetInfo(String targetName, String targetAlias, String storageFilePath)
    {
        super(targetName, targetAlias);
        this.storageFilePath = storageFilePath;
    }

    public String getStorageFilePath()
    {
        return storageFilePath;
    }
}
