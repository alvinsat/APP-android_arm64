package com.epai.oblfiles;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class InstallOBLFiles {
    private final String TAG="InstallOBLFiles";
    private boolean copyAsset(AssetManager theAssetMgr,
                              String thePathFrom,
                              String thePathTo) {
        try {
            Log.i(TAG,"copyAsset 1 "+thePathFrom+" "+thePathTo);
            File aFileTo = new File(thePathTo);
            InputStream aStreamIn = theAssetMgr.open(thePathFrom);
            boolean newFile = aFileTo.createNewFile();
            if (!newFile) {
                return false;
            }
            Log.i(TAG,"copyAsset 2 "+thePathFrom+" "+thePathTo);
            OutputStream aStreamOut = new FileOutputStream(thePathTo);
            FileUtils.copyStreamContent(aStreamIn, aStreamOut);
            aStreamIn.close();
            aStreamIn = null;
            aStreamOut.flush();
            aStreamOut.close();
            aStreamOut = null;
            Log.i(TAG,"copyAsset 3 "+thePathFrom+" "+thePathTo);
            return true;
        } catch (Exception theError) {
            theError.printStackTrace();
            return false;
        }
    }
    private boolean copyAssetFolder(AssetManager theAssetMgr,
                                    String theAssetFolder,
                                    String theFolderPathTo) {
        try {
            Log.i(TAG,"copyAssetFolder 1 "+theAssetFolder+" "+theFolderPathTo);
            String[] aFiles = theAssetMgr.list(theAssetFolder);
            File aFolder = new File(theFolderPathTo);
            if (aFolder.exists()){
                return true;
            }
            boolean mkdirs = aFolder.mkdirs();
            boolean isOk = true;
            for (String aFileIter : aFiles) {
                Log.i(TAG,"copyAssetFolder 2 "+theAssetFolder+" "+theFolderPathTo+" "+theAssetFolder+File.separator+aFileIter);
                if (theAssetMgr.list(theAssetFolder+File.separator+aFileIter).length<1) {
                    String stringFilePathTo = theFolderPathTo + "/" + aFileIter;
                    if (!FileUtils.exist(stringFilePathTo)) {
                        Log.i(TAG,"copyAssetFolder 3 "+theAssetFolder+" "+theFolderPathTo);
                        isOk &= copyAsset(theAssetMgr,
                                theAssetFolder + "/" + aFileIter,
                                stringFilePathTo);
                    }
                } else {
                    isOk &= copyAssetFolder(theAssetMgr,
                            theAssetFolder + "/" + aFileIter,
                            theFolderPathTo + "/" + aFileIter);
                }
            }
            return isOk;
        } catch (Exception theError) {
            theError.printStackTrace();
            return false;
        }
    }
    public class OBLFilePath{
        public String mStringHomePath;
        public String mStringConfigPath;
    }
    public OBLFilePath installOBLFiles(Context context){
        OBLFilePath oblFilePath=new OBLFilePath();
        AssetManager assetManager=context.getAssets();
        {
            //  创建home文件夹
            oblFilePath.mStringHomePath=FileUtils.getExternStorageDir(context,"")+ File.separator;

            String[] stringsAppRootFilesFolders = {
                    "examples","Desktop","Documents","Downloads","Music","Pictures","Videos"
            };
            for (String stringFolder : stringsAppRootFilesFolders) {
                copyAssetFolder(assetManager, stringFolder, oblFilePath.mStringHomePath  + stringFolder);
            }
        }
        {
            oblFilePath.mStringConfigPath = context.getFilesDir().getAbsolutePath()+ File.separator;
            String[] stringsAppRootFilesFolders = {
                    "python","4.0", "scripts","usd"
            };
            for (String stringFolder : stringsAppRootFilesFolders) {
                copyAssetFolder(assetManager, stringFolder, oblFilePath.mStringConfigPath  + stringFolder);
            }
        }
        return oblFilePath;
    }
}
