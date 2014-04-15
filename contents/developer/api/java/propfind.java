import java.io.IOException;
import java.net.URLDecoder;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.xml.Namespace;


public class demo_propfind {
	public static void main(String[] args) throws IOException, DavException {
		HttpClient client = new HttpClient();
		Credentials creds = new UsernamePasswordCredentials("admin", "admin");
		client.getState().setCredentials(AuthScope.ANY, creds);
		
		//�õ�����
		String dirUrl = "http://localhost:8089/default/files/";
		DavMethod find = new PropFindMethod(dirUrl, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
	    client.executeMethod(find);
	    
		if (find.getStatusCode() != 207){
			System.out.println("����ִ��ʧ�ܣ���������ǣ�" + find.getStatusCode());
			return;
		}
	    
	    MultiStatus multiStatus = find.getResponseBodyAsMultiStatus();
	    MultiStatusResponse[] responses = multiStatus.getResponses();
	    
		Namespace edoNamespace = Namespace.getNamespace("http://ns.everydo.com/basic");
		
	    // ��ӡ����������Ϣ( ���е����Զ��õ��� )
		for (int i=0; i<responses.length; i++) {
			 MultiStatusResponse content = responses[i];
			 DavPropertySet properys = content.getProperties(200);
			 
			 // ��ӡ�ļ������ļ��е�url
		     String docHref = content.getHref();
		     System.out.println("source url: " + URLDecoder.decode(docHref, "utf-8"));
		     
		     //�Ƿ�ΪĿ¼
		     Boolean isFile = properys.get("iscollection").getValue().equals("0");
		     System.out.println("IsFile: " + isFile.toString());
		     
		     // ��ӡ�ļ������ļ��е�����
		     String sourceName = properys.get("displayname").getValue().toString();
             System.out.println("display name: " + sourceName);
             
		     if (isFile){
	             // ��ӡ�ļ��ı�ǩ�� subjects ������webdav��׼���ԣ��������Զ�������ԣ�����Ҫ�����ǵ������ռ�ȥѰ��
	             DavProperty subjects = properys.get("subjects", edoNamespace);
	             // ֻ�д��ϱ�ǩ���ļ����ܻ�ñ�ǩ
	             if (subjects == null || subjects.getValue() == null){
	            	 System.out.println("subjects: null");
	             }else{
	            	 System.out.println("subjects: " + subjects.getValue().toString());
	             }
		     }else{
		    	 System.out.println("subjects: null");
		     }
	    }
	}
}
