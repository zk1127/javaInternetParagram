package com.ssd8.socket.webservice.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import Service.Project;
import Service.User;

@WebService
public class Service {
    
	Vector<User> users; 
    Vector<Project> projects;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    
    public Service() {
    	users = new Vector<User>(); // ��ʼ��User����
    	projects = new Vector<Project>(); // ��ʼ��Meeting����
    }
    
	public boolean register(String username, String password){
		// TODO Auto-generated method stub
		for(int i=0;i<users.size();i++){
			if(users.get(i).getUsername().equals(username)){
				System.out.println("�û����Ѵ���!");
				return false;
			}
		}
		User user = new User(username,password);
		users.add(user);
		System.out.println("����û��ɹ�!");
		return true;
	}

	public boolean add(String username, String password,
			Date start, Date end, int projectId,String tag) {
		// TODO Auto-generated method stub
		User user;
		boolean correct = false;
		// ����user������ȷ���û����������Ƿ���ȷ
		for (int i = 0; i < users.size(); i++) {
		    user = users.elementAt(i);
		    if (user.getUsername().equals(username)
			    && user.getPassword().equals(password)) {
			correct = true;
			break;//�û�����������ȷ������ѭ��
		    }
		}

		// ����û����������벻��ȷ�����������ʾ��Ϣ������false
		if (!correct) {
		    System.out.println("�û��������������");
		    return false;
		}
        //������ڴ��û����������������
		Project project = new Project(username, tag, start,end,projectId);
		projects.addElement(project);
		System.out.println("��Ŀ��ӳɹ�");
		return true;
	}

	public String query(String username, String password, Date start, Date end) {
		// TODO Auto-generated method stub
		boolean isUser = false;
		for(int i=0;i<users.size();i++){
			if((users.get(i).getUsername().equals(username)
				&&users.get(i).getPassword().equals(password))){
				isUser = true;
				break;
			}
		}
		//�û����ڣ����в�ѯ
		if(isUser){
			List<Project> vList = new ArrayList<Project>();
			for(int i=0;i<projects.size();i++){
				//���ҵ����ڸ��û��Ĵ�������
				if(projects.get(i).getUsername().equals(username)){
					if((projects.get(i).getStart().after(start)&&projects.get(i).getEnd().before(end))
					  ||(projects.get(i).getStart().equals(start)&&projects.get(i).getEnd().before(end))
					  ||(projects.get(i).getStart().after(start)&&projects.get(i).getEnd().equals(end))
					  ||(projects.get(i).getStart().equals(start)&&projects.get(i).getEnd().equals(end))){
						vList.add(projects.get(i));
					}
				}
			}
			String info = "";
			for(int i=0;i<vList.size();i++){
				info = info +vList.get(i).getStart()+" "+vList.get(i).getEnd()+" "+vList.get(i).getTag()+" "
			                       +vList.get(i).getUsername()+"\n";
			}
			return info;
		}
		return "�û������������";
	}


	public boolean delete(String username,int ID) {
		// TODO Auto-generated method stub
		for (int i = 0; i < projects.size(); i++) {
		    Project project = projects.get(i);
		    // �ҵ��û���Ҫɾ���Ĵ���������
		    if (project.getProjectID()==ID
			    && project.getUsername().equals(username)) {
			// �����ȷ���򽫸û����������ɾ��
			projects.removeElementAt(i);
			// �����ʾ��Ϣ
			System.out.println("��Ŀ" + ID + "ɾ���ɹ���");
			return true;
		    }
		}
		return false;
	}

	public void clear(String username) {
		// TODO Auto-generated method stub
		for (int i = 0; i < projects.size(); i++) {
		    Project project = projects.get(i);
		    if (project.getUsername().equals(username)) {
			projects.removeElementAt(i);
		    }
		}
		System.out.println("���û�" + username + "��������Ŀ�Ѿ����");
	}

	public static void main(String[] args){
		Endpoint.publish("http://localhost:9090/Service", new Service());
		System.out.println("Server running");
	}
}
