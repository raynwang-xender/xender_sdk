package cn.xender.core.server.utils;

public class NeedSharedFiles {

    public static final String CATE_APP = "app";
    public static final String CATE_AUDIO = "audio";
    public static final String CATE_VIDEO = "video";
    public static final String CATE_IMAGE = "image";
    public static final String CATE_OTHER = "other";

    private static String[] needShared;

    private static String cate;

    /**
     * @param needShared 文件全路径
     * @param cate 文件类型，必须为 CATE_APP ,CATE_AUDIO, CATE_VIDEO,CATE_IMAGE ,CATE_OTHER中的一个
     * */
    public static void setNeedShared(String[] needShared,String cate) {
        NeedSharedFiles.needShared = needShared;
        NeedSharedFiles.cate = cate;
    }


    public static String[] getNeedShared() {
        return needShared;
    }

    public static String getCate() {
        checkCate();
        return cate;
    }

    private static void checkCate(){

        if(!CATE_APP.equals(cate)
                && !CATE_AUDIO.equals(cate)
                && !CATE_VIDEO.equals(cate)
                && !CATE_IMAGE.equals(cate)
                && !CATE_OTHER.equals(cate)){
            throw new IllegalArgumentException("your cate was wrong");
        }

    }
}
