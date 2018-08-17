package cn.xender.core.server.utils;

import java.util.ArrayList;
import java.util.List;

public class NeedSharedFiles {


    private static List<FileItem> needShared;

    private static boolean shareMyApk = true;



    /**
     * 多次 appendNewFile 之前先调用一下
     *
     * 例如：
     *
     * NeedSharedFiles.confirmStart();
     *
     * NeedSharedFiles.appendNewFile("path1", FileItem.CATE_APP);
     *
     * NeedSharedFiles.appendNewFile("path2", FileItem.CATE_APP);
     *
     * */
    public static void confirmStart(){

        needShared = null;

        shareMyApk = true;

        TaskCountCalcultor.init();
    }

    /**
     * @param filepath 文件全路径
     * @param cate 文件类型，必须为 FileItem.CATE_APP ,CATE_AUDIO, CATE_VIDEO,CATE_IMAGE ,CATE_OTHER中的一个
     * */
    public static void appendNewFile(String filepath,String cate){
        if(needShared == null){
            needShared = new ArrayList<>();
        }

        needShared.add(createOneItem(filepath,cate));
    }

    public static void setShareMyApk(boolean shareMyApk) {
        NeedSharedFiles.shareMyApk = shareMyApk;
    }

    public static boolean isShareMyApk() {
        return shareMyApk;
    }

    public static List<FileItem> getNeedShared() {
        return needShared;
    }

    private static FileItem createOneItem(String path,String cate){

        return new FileItem(path,cate);
    }

    public static class FileItem{

        public static final String CATE_APP = "app";
        public static final String CATE_AUDIO = "audio";
        public static final String CATE_VIDEO = "video";
        public static final String CATE_IMAGE = "image";
        public static final String CATE_OTHER = "other";


        private String path;

        private String cate;


        public FileItem(String path, String cate) {
            this.path = path;
            this.cate = cate;
        }


        private void checkCate(){

            if(!CATE_APP.equals(cate)
                    && !CATE_AUDIO.equals(cate)
                    && !CATE_VIDEO.equals(cate)
                    && !CATE_IMAGE.equals(cate)
                    && !CATE_OTHER.equals(cate)){
                throw new IllegalArgumentException("your cate was wrong");
            }

        }


        public String getCate() {

            checkCate();

            return cate;
        }


        public String getPath() {
            return path;
        }
    }

}
