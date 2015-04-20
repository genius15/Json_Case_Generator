package com.sogou.testtool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;


import org.json.*;
import org.apache.http.HttpEntity;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.apache.http.util.EntityUtils;






public class case_generator {
	public static String savePath = null;
	private static int caseCnt = 0;
	
	public static void start_generator(String url,String postdata) throws JSONException{

		String jsonString = null;
		if(postdata == null){
			jsonString = get(url);
		}else{
			jsonString = post(url,postdata);
		}
		if(jsonString == null || "".equals(jsonString))
			return;
		
		Object root = null;
		jsonString = jsonString.trim();
		if (jsonString.startsWith("{")) {	
			root = new JSONObject(jsonString);
			jo_operator(root, root);
		} else if (jsonString.startsWith("[")) {
			root = new JSONArray(jsonString);
			ja_operator(root, root);
		}
		json_empty(root,false);
		json_empty(root,true);
		while(!json_clear(root)){
		}
		
		caseCnt = 0;
	}
	public static void writeCaseToFile(String caseContent){
		if(savePath == null)
			return;
		try {
			String realFileName = savePath + File.separator
					+ String.valueOf(caseCnt)+".txt";

			File tempFile = new File(savePath);
			if (!tempFile.exists()) {
				tempFile.mkdir();
			}
			File file = new File(realFileName);
			Writer writer = new OutputStreamWriter(new FileOutputStream(file,
					true), "UTF-8");
			writer.write(caseContent);
			writer.flush();
			writer.close();
			
		} catch (IOException ioex) {
			ioex.printStackTrace();
		} 
		caseCnt++; 
	}
	public static String get(String url){
		String jsonString = "";
		CloseableHttpClient httpclient = HttpClients.createDefault();  
        try {  
            // 创建httpget.    
            HttpGet httpget = new HttpGet(url);  
            System.out.println("executing request " + httpget.getURI());  
            // 执行get请求.    
            CloseableHttpResponse response = httpclient.execute(httpget);  
            try {  
                // 获取响应实体    
                HttpEntity entity = response.getEntity();  
                System.out.println("--------------------------------------");  
                // 打印响应状态    
                System.out.println(response.getStatusLine());  
                if (entity != null) {  
                    // 打印响应内容长度    
                	System.out.println("Response content length: " + entity.getContentLength());  
                    // 打印响应内容    
                    jsonString = EntityUtils.toString(entity);  
                }  
                System.out.println("------------------------------------");  
            } finally {  
                response.close();  
            }  
        } catch (ClientProtocolException e) {  
            e.printStackTrace();  
        } catch (ParseException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            // 关闭连接,释放资源    
            try {  
                httpclient.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
		
		return jsonString;
	}
	
	/** 
     * 发送 post请求访问本地应用并根据传递参数不同返回不同结果 
     */  
    public static String post(String url,String postData) {  
        // 创建默认的httpClient实例.   
    	String json = "";
        CloseableHttpClient httpclient = HttpClients.createDefault();  
        // 创建httppost    
        HttpPost httppost = new HttpPost(url);  

        try {  
        	StringEntity sEntity = new StringEntity(postData);
        	sEntity.setContentType("application/x-www-form-urlencoded"); //不同接口应该不一样吧          
            httppost.setEntity(sEntity);  
            System.out.println("executing request " + httppost.getURI());  
            CloseableHttpResponse response = httpclient.execute(httppost);  
            try {  
                HttpEntity entity = response.getEntity();  
                if (entity != null) {    
                    json = EntityUtils.toString(entity, "UTF-8");                     
                }  
            } finally {  
                response.close();  
            }  
        } catch (ClientProtocolException e) {  
            e.printStackTrace();  
        } catch (UnsupportedEncodingException e1) {  
            e1.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            // 关闭连接,释放资源    
            try {  
                httpclient.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
        return json;
    }  
	private static void jo_operator(Object jo,Object root) throws JSONException {//传进来的应该保证是jsonobject
		
		JSONObject tmpjo = (JSONObject) jo;
		JSONArray namesStrings = tmpjo.names();
		String errorKeyString = "errKeyName";
		for (int i = 0; i < namesStrings.length(); ++i) {
			String strjo = (String) namesStrings.get(i);
			Object valueObject = tmpjo.get(strjo);
			String objectnameString = valueObject.getClass().getName();
			if (objectnameString.equals("org.json.JSONObject")) {
				//case 8对象值为空
				tmpjo.put(strjo, new JSONObject());//空对象的情况
				writeCaseToFile(root.toString());				
				
				tmpjo.put(strjo,valueObject);
				jo_operator(valueObject,root);
				
			} else if (objectnameString.equals("org.json.JSONArray")) {
				//case 8空数组的情况
				tmpjo.put(strjo, new JSONArray());
				writeCaseToFile(root.toString());				
				
				JSONArray tmp = (JSONArray)valueObject;				
				if(tmp.length()>0){
					//case 13.1 数组少一个的情况
					JSONArray lessArray = new JSONArray(valueObject.toString());
					lessArray.remove(0);
					tmpjo.put(strjo, lessArray);
					writeCaseToFile(root.toString());
				
					//case 13.2数组多一个的情况				
					JSONArray moreArray = new JSONArray(valueObject.toString());
					moreArray.put(tmp.get(0));
					tmpjo.put(strjo, moreArray);
				}				
				
				tmpjo.put(strjo, valueObject);
				ja_operator(valueObject,root);
			} else {
				//case 2.1 多一个错误的名称
				tmpjo.put(errorKeyString,valueObject);				
				writeCaseToFile(root.toString());
				tmpjo.remove(errorKeyString);
				
				//case 2.2少这个名称
				tmpjo.remove(strjo);
				writeCaseToFile(root.toString());
				tmpjo.put(strjo, valueObject);
				
				//case 7 值为空的字符串
				tmpjo.put(strjo, "");			
				writeCaseToFile(root.toString());
				
				//case 8 值为空的数组
				tmpjo.put(strjo, new JSONArray());
				writeCaseToFile(root.toString());
				
				//case 9值为空对象
				tmpjo.put(strjo, new JSONObject());
				writeCaseToFile(root.toString());
				
				//case 10值为null
				tmpjo.put(strjo, JSONObject.NULL);			
				writeCaseToFile(root.toString());
				
				//case 11.1遍历数据类型——整型
				tmpjo.put(strjo, 65535);			
				writeCaseToFile(root.toString());
				
				//case 11.2遍历数据类型——布尔型
				tmpjo.put(strjo, true);			
				writeCaseToFile(root.toString());
				
				//case 11.3遍历数据类型——对象，与case 9 一样，不再重复
				
				//case 11.4遍历数据类型——数组，与case 8 一样，不再重复
				//tmpjo.put(strjo, JSONObject.NULL);			
				//writeCaseToFile(root.toString());
				
				//case 14.1 值为小数
				tmpjo.put(strjo, 3.1415926);			
				writeCaseToFile(root.toString());
				
				//case 14.2值为负数
				tmpjo.put(strjo, -1);			
				writeCaseToFile(root.toString());
				
				//case 14.3值为0
				tmpjo.put(strjo, 0);			
				writeCaseToFile(root.toString());
				
				//case 14.2值为时间戳2014-3-3 03:03
				tmpjo.put(strjo, "1393786980");			
				writeCaseToFile(root.toString());
				
				//case 15.1全角中文
				tmpjo.put(strjo, "这是全角中文的测试");
				writeCaseToFile(root.toString());
				
				//case 15.2字符串长度边界
				tmpjo.put(strjo, "这是全角中文的测试");
				writeCaseToFile(root.toString());
				
				//case 15.3十六进制字符，encode过的
				tmpjo.put(strjo, "%20%E4%BB%8A%E6%97%A5%E7%83%AD%E7%82%B9");
				writeCaseToFile(root.toString());
				
				//case 15.4前空格
				tmpjo.put(strjo, " a");
				writeCaseToFile(root.toString());
				
				//case 15.5中空格
				tmpjo.put(strjo, "a b");
				writeCaseToFile(root.toString());
				
				//case 15.6后空格
				tmpjo.put(strjo, "a ");
				writeCaseToFile(root.toString());
				
				//case 15.7转义字符
				tmpjo.put(strjo, "/t");
				writeCaseToFile(root.toString());
				
				//case 15.8特殊字符全角
				tmpjo.put(strjo, "~！@#￥%……&*（）《》？、");
				writeCaseToFile(root.toString());
				
				//case 15.9特殊字符半角
				tmpjo.put(strjo, "~!@#$%^&*()<>?/\\");
				writeCaseToFile(root.toString());
				
				//case 15.10 %中文
				tmpjo.put(strjo, "%中文");
				writeCaseToFile(root.toString());
				
				
				tmpjo.put(strjo, valueObject);//执行还原操作
			}
		}
	}

	private static void ja_operator(Object ja,Object root) throws JSONException {
		JSONArray tmpjaArray = (JSONArray)ja;
		if(tmpjaArray.length() == 0)
			return;
		Object valueObject = tmpjaArray.get(0);
		String objectnameString = valueObject.getClass().getName();
		if (objectnameString.equals("org.json.JSONObject")) {
			jo_operator(valueObject, root);
		} else if (objectnameString.equals("org.json.JSONArray")) {
			ja_operator(valueObject, root);
		} else {
			String typenameString = valueObject.getClass().getName();
			if(typenameString.equals(String.class.getName())){
				//case 14.2值为时间戳2014-3-3 03:03
				tmpjaArray.put(0,"1393786980");			
				writeCaseToFile(root.toString());
				
				//case 15.1全角中文
				tmpjaArray.put(0,"这是全角中文的测试");
				writeCaseToFile(root.toString());
				
				//case 15.2字符串长度边界
				tmpjaArray.put(0,"这是全角中文的测试");
				writeCaseToFile(root.toString());
				
				//case 15.3十六进制字符，encode过的
				tmpjaArray.put(0,"%20%E4%BB%8A%E6%97%A5%E7%83%AD%E7%82%B9");
				writeCaseToFile(root.toString());
				
				//case 15.4前空格
				tmpjaArray.put(0," a");
				writeCaseToFile(root.toString());
				
				//case 15.5中空格
				tmpjaArray.put(0,"a b");
				writeCaseToFile(root.toString());
				
				//case 15.6后空格
				tmpjaArray.put(0,"a ");
				writeCaseToFile(root.toString());
				
				//case 15.7转义字符
				tmpjaArray.put(0,"/t");
				writeCaseToFile(root.toString());
				
				//case 15.8特殊字符全角
				tmpjaArray.put(0,"~！@#￥%……&*（）《》？、");
				writeCaseToFile(root.toString());
				
				//case 15.9特殊字符半角
				tmpjaArray.put(0,"~!@#$%^&*()<>?/\\");
				writeCaseToFile(root.toString());
				
				//case 15.10 %中文
				tmpjaArray.put(0,"%中文");
				writeCaseToFile(root.toString());
			}else if (typenameString.equals(Integer.class.getName())) {
				//case 14.2值为负数
				tmpjaArray.put(0,-1);			
				writeCaseToFile(root.toString());
				
				//case 14.3值为0
				tmpjaArray.put(0,0);			
				writeCaseToFile(root.toString());
			}else if (typenameString.equals(Boolean.class.getName())) {
				//case 11.2遍历数据类型——布尔型
				tmpjaArray.put(0,true);			
				writeCaseToFile(root.toString());
			}else if (typenameString.equals(Double.class.getName())) {
				//case 14.1 值为小数
				tmpjaArray.put(0,3.1415926);			
				writeCaseToFile(root.toString());
			}
			
			tmpjaArray.put(0, valueObject);//执行还原操作
		}
	}
	/**
	 * @author zhangshuai203407
	 * @param root
	 * 要解析的json数据
	 * @param remove
	 * 是否要删除普通key值
	 * @throws JSONException
	 * 抛出json解析异常
	 * 
	 */
	//与json_clear一起实现 case 17
	public static void json_empty(Object root,boolean remove) throws JSONException{
		String objectnameString = root.getClass().getName();
		if (objectnameString.equals("org.json.JSONObject")) {
			jo_empty(root, root,remove );
		} else if (objectnameString.equals("org.json.JSONArray")) {
			ja_empty(root, root,remove);
		} else {
			
		}
		writeCaseToFile(root.toString());
	}
	
	private static void jo_empty(Object o,Object root,boolean remove) throws JSONException{
		JSONObject JsonObject = (JSONObject)o;
		JSONArray keynames = JsonObject.names();
		for(int i = 0;i<keynames.length();++i){
			String strjo = (String) keynames.get(i);
			Object valueObject = JsonObject.get(strjo);
			String objectnameString = valueObject.getClass().getName();
			if (objectnameString.equals("org.json.JSONObject")) {
				jo_empty(valueObject, root,remove);				
			} else if (objectnameString.equals("org.json.JSONArray")) {
				ja_empty(valueObject, root,remove);
			} else {
				if(remove){
					JsonObject.remove(strjo);
					continue;
				}
				JsonObject.put(strjo, "");
			}			
		}
		//writeCaseToFile(root.toString());
	}
	
	private static void ja_empty(Object o,Object root,boolean remove) throws JSONException{
		JSONArray jsonArray = (JSONArray)o;
		if(jsonArray!=null && jsonArray.length() > 0){
			Object valueObject = jsonArray.get(0);
			clear_jsonArray(jsonArray,1);
			String objectnameString = valueObject.getClass().getName();
			if (objectnameString.equals("org.json.JSONObject")) {				
				jo_empty(valueObject, root,remove);				
			} else if (objectnameString.equals("org.json.JSONArray")) {
				ja_empty(valueObject, root,remove);
			} else {//如果是普通值则清空数据
				clear_jsonArray(jsonArray,0);
				if (!remove) {
					jsonArray.put("");
				}
				
			}
		}
	}
	private static void clear_jsonArray(JSONArray ja,int remainNum){
		int i = ja.length();
		while(i>remainNum){
			ja.remove(i-1);
			i = ja.length();
		}
	}
	
	
	
	/**
	 * 与json_empty一起实现 case 17
	 * @author zhangshuai203407
	 *  
	 * @param root
	 * 要解析的json数据，输入应为JSONObject类型
	 * @throws JSONException
	 * 抛出json解析异常
	 * @return boolean
	 * 表示case生成是否完成了
	 */
	public static boolean json_clear(Object root) throws JSONException {
		String objectnameString = root.getClass().getName();
		boolean over = false;
		if (objectnameString.equals("org.json.JSONObject")) {
			JSONObject JsonObject = (JSONObject)root;
			JSONArray keynames = JsonObject.names();
			if (keynames == null || keynames.length() == 0) {
				over = true;
			}else{
				jo_clear(root, root);
				over = false;
			}			
			
		} else if (objectnameString.equals("org.json.JSONArray")) {
			JSONArray jsonArray = (JSONArray)root;
			if(jsonArray == null || jsonArray.length() == 0){
				over = true;
			}else{
				ja_clear(root, root);
				over = false;
			}
			
		} else {
			throw new JSONException("impossible");
		}
		writeCaseToFile(root.toString());
		return over;
	}
	private static void jo_clear(Object o,Object root) throws JSONException{
		JSONObject JsonObject = (JSONObject)o;
		JSONArray keynames = JsonObject.names();
		for(int i = 0;i<keynames.length();++i){
			Object object = JsonObject.get(keynames.get(i).toString());
			String objectnameString = object.getClass().getName();
			if (objectnameString.equals("org.json.JSONObject")) {
				JSONObject chilejsonObject = (JSONObject)object;
				JSONArray keynames1 = chilejsonObject.names();
				if (keynames1 == null || keynames1.length() == 0) {
					JsonObject.remove(keynames.get(i).toString());
					continue;
				}			
				jo_clear(object, root);
				
			} else if (objectnameString.equals("org.json.JSONArray")) {
				JSONArray jsonArray = (JSONArray)object;
				if(jsonArray.length() == 0){
					JsonObject.remove(keynames.get(i).toString());
					
					continue;
				}
				ja_clear(object, root);
			} else {
				throw new JSONException("impossible");
			}
		}
	}
	
	private static void ja_clear(Object o,Object root) throws JSONException{
		JSONArray jsonArray = (JSONArray)o;
		
		Object object = jsonArray.get(0);
		String objectnameString = object.getClass().getName();
		if (objectnameString.equals("org.json.JSONObject")) {
			JSONObject jsonObject = (JSONObject)object;
			JSONArray keynames = jsonObject.names();
			if (keynames == null || keynames.length() == 0) {
				clear_jsonArray(jsonArray, 0);
				
				return;
			}			
			jo_clear(object, root);
			
		} else if (objectnameString.equals("org.json.JSONArray")) {
			JSONArray childjsonArray = (JSONArray)object;
			if(childjsonArray == null || childjsonArray.length() == 0){
				clear_jsonArray(jsonArray, 0);
				
				return;
			}
			ja_clear(object, root);
		} else {
			throw new JSONException("impossible");
		}
	}
}

