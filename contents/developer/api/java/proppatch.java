import java.io.IOException;
import java.net.URLEncoder;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.Status;
import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.xml.Namespace;


public class demo_proppatch {
	public static void main(String[] args) throws IOException, DavException {
		HttpClient client = new HttpClient();
		Credentials creds = new UsernamePasswordCredentials("admin", "admin");
		client.getState().setCredentials(AuthScope.ANY, creds);
                client.getParams().setAuthenticationPreemptive(true);
		
		String dirUrl = "http://localhost:8089/default/files/";
		DavPropertyNameSet removeProperties = new DavPropertyNameSet(); 
		
		/*
		 * �޸�webdav����
		 * displayname ���ļ����У���Title, �ǿ��޸ĵġ�
		 * iscollection ���ļ����У����ض����ԣ��ļ��̶�����0�� �ļ��й̶�����1��ֻ�����ԡ�
		 * ������������޸�iscollection, ���������޸�ʧ�ܡ�
		 */
		System.out.println(">>�� �ı�.txt �ļ�����Ϊ�� �����ϴ�.txt, ���ҳ����޸�iscollection����");
		String fileName = "�ı�.txt";
		String serverFileUrl = dirUrl + URLEncoder.encode(fileName,"utf-8");
		DefaultDavProperty newName = new DefaultDavProperty(DavPropertyName.create("displayname"), "�����ϴ�.txt");
		DefaultDavProperty newIsCollection = new DefaultDavProperty(DavPropertyName.create("iscollection"), 0);
		DavPropertySet newNameSet = new DavPropertySet(); 
		newNameSet.add(newName); 
		// ע������һ�У� ����������޸ĳɹ�ִ��
		newNameSet.add(newIsCollection);
		PropPatchMethod newNameProPatch = new PropPatchMethod(serverFileUrl, newNameSet, removeProperties);
		client.executeMethod(newNameProPatch); 
		// �������ص�response
		show_response(newNameProPatch);
		
	    /*
	     * �޸��Զ�������
	     * subjects ���ļ������е����ԣ����ļ��ı�ǩ���á��ԡ������ŷָ
	     * ������׶��Զ������Խ��ܣ���ο��ĵ��� �ĵ������ɹ淶.pptx����
	     */
		serverFileUrl = dirUrl + URLEncoder.encode("�����ϴ�.txt","utf-8");
		DavPropertySet newSubjectSet = new DavPropertySet(); 
		DefaultDavProperty newSubject = new DefaultDavProperty("subjects", "������,��ͬ,����", Namespace.getNamespace("http://ns.everydo.com/basic")); 
		newSubjectSet.add(newSubject);
		PropPatchMethod newSubjectProPatch = new PropPatchMethod(serverFileUrl, newSubjectSet, removeProperties); 
		client.executeMethod(newSubjectProPatch); 
		System.out.println("***********************************");
		System.out.println(">>�� �����ϴ�.txt �ļ���� ������������ ����ͬ�� ������ǩ");
		show_response(newSubjectProPatch);
	}
	
	/*
	 * ������ӡһ���ļ�������Ҫ�޸ĵ����ԣ�һ�����߶���޸Ľ����
	 * ���ĳ��ĳ������ִ�н����424��������Ϊ�ĵ�ϵͳ�����ݿ���������ġ�
	 * ����һ��������Ϊ���ԭ��ִ��ʧ�ܣ��ᵼ������������Ҳʧ�ܣ�����424��
	 */
	public static void show_response(PropPatchMethod methods)throws IOException, DavException {
		System.out.println("���ؽ����");
		
		if (methods.getStatusCode() != 207){
			System.out.println("����ִ��ʧ�ܣ���������ǣ�" + methods.getStatusCode());
			return;
		}
		
	    MultiStatus multiStatus = methods.getResponseBodyAsMultiStatus();
	    for (MultiStatusResponse content : multiStatus.getResponses()) {
	    	for (Status status : content.getStatus()) {
	    		int statusCode = status.getStatusCode();
	    		DavPropertyNameSet nameSet = content.getPropertyNames(statusCode);
	    		if (statusCode >= 200 & statusCode < 300){
	    			System.out.println("�ɹ�������: ");
	    		}else{
	    			System.out.println("������������ʧ�ܣ���������ǣ� " + statusCode);
	    		}
    			for (DavPropertyName name : nameSet){
    				System.out.println(name);
    			}
	    	}
	    }
	}
	
	
}
