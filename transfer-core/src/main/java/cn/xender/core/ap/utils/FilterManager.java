package cn.xender.core.ap.utils;

public class FilterManager {

    private static cn.xender.core.ap.SSIDFilter SSIDFilter;

    public static void setSSIDFilter(cn.xender.core.ap.SSIDFilter SSIDFilter){

        FilterManager.SSIDFilter = SSIDFilter;
    }


    public static boolean acceptSSID(String ssid){
        if(FilterManager.SSIDFilter != null){
            return SSIDFilter.accept(ssid);
        }
        return true;
    }

    public static cn.xender.core.ap.SSIDFilter getSSIDFilter() {
        return SSIDFilter;
    }
}
