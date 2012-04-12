package org.jscsi.target.settings;

/**
 * A TargetInfo is created from the config file for each Target element. It contains
 * the TargetName and Alias. Extend this class for other information specific to the
 * StorageModule (e.g. storageFilePath)
 * 
 * @author David L. Smith-Uchida
 * 
 *         jSCSI
 * 
 *         Copyright (C) 2009 iGeek, Inc. All Rights Reserved
 */
public class TargetInfo {
    private final String targetName;
    private final String targetAlias;
    private final long targetLength;

    public TargetInfo(final String targetName, final String targetAlias, final long targetLength) {
        this.targetName = targetName;
        this.targetAlias = targetAlias;
        this.targetLength = targetLength;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getTargetAlias() {
        return targetAlias;
    }

    public long getTargetLength() {
        return targetLength;
    }
}
