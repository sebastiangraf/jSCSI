package org.jscsi.target.settings;

/**
 * A TargetInfo is created from the config file for each Target element.  It contains
 * the TargetName and Alias.  Extend this class for other information specific to the
 * StorageModule (e.g. storageFilePath)
 * @author David L. Smith-Uchida
 *
 *jSCSI
 *
 * Copyright (C) 2009 iGeek, Inc.  All Rights Reserved
 */
public class TargetInfo
{
    private String targetName;
    private String targetAlias;
    
    public TargetInfo(String targetName, String targetAlias)
    {
        this.targetName = targetName;
        this.targetAlias = targetAlias;
    }

    public String getTargetName()
    {
        return targetName;
    }

    public String getTargetAlias()
    {
        return targetAlias;
    }
}
