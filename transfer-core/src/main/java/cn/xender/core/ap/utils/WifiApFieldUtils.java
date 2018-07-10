package cn.xender.core.ap.utils;

import java.lang.reflect.Field;


public class WifiApFieldUtils {
	private WifiApFieldUtils(){}
	
	public static Object getFieldValue(Object config,String field_name) throws Exception{
		Field localField1 = config.getClass().getDeclaredField(field_name);
		localField1.setAccessible(true);
		Object localObject = localField1.get(config);
		localField1.setAccessible(false); 
		
		return localObject;
	}
	
	public static void setFieldValue(Object needSet,String field_name,String field_value) throws Exception{
		Field localField2 = needSet.getClass().getDeclaredField(field_name); 
		localField2.setAccessible(true);
		localField2.set(needSet, field_value);
		localField2.setAccessible(false);   
	}
	
	public static void setFieldValue(Object needSet,String field_name,int field_value) throws Exception{
		Field localField2 = needSet.getClass().getDeclaredField(field_name); 
		localField2.setAccessible(true);
		localField2.setInt(needSet, field_value);
		localField2.setAccessible(false);   
	}
}
