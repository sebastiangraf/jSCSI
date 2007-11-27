package org.jscsi.target.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.jscsi.target.task.abstracts.OperationException;
import org.jscsi.target.task.abstracts.Task;
import org.jscsi.target.task.abstracts.TaskDescriptor;
import org.jscsi.target.task.standard.login.LoginRequestTaskDescriptor;

public class TaskClassLoader extends URLClassLoader {

	public TaskClassLoader(URL[] urls) {
		super(urls);
		// TODO Auto-generated constructor stub
	}
	
	
	public static void main(String[] args){
		try {
			Class<?> newClass = ClassLoader.getSystemClassLoader().loadClass("org.jscsi.target.task.standard.login.LoginRequestTaskDescriptor");
		} catch (ClassNotFoundException e1) {
		System.out.println("möööb1");
			e1.printStackTrace();
		} 
		URL url = null;
		Thread.currentThread().getContextClassLoader();
		try {
			url = new File("bin\\org\\jscsi\\target\\task\\standard\\login\\").toURI().toURL();
		} catch (MalformedURLException e) {
			System.out.println("---LoadingURLError----");
			System.out.println(e.getMessage());
		}
		System.out.println("getPath = " + url.getPath());
		System.out.println("getQuery = " + url.getQuery());
		System.out.println("getUserInfo = " + url.getUserInfo());
		System.out.println("getRef = " + url.getRef());
		TaskClassLoader newTL = new TaskClassLoader(new URL[]{url});
		
		for(String path : System.getProperty("java.class.path",".").split(";")){
			URL newPath = null;
			try {
				newPath = new File(path).toURI().toURL();
			} catch (MalformedURLException e) {
				System.out.println("---LoadingURLError----");
				System.out.println(e.getMessage());
			}
			newTL.addURL(newPath);
		}
		for(URL path : newTL.getURLs()){
			System.out.println(path.toString());
		}
		TaskDescriptor newOne = null;
		try {
			newOne = (TaskDescriptor) newTL.loadClass("org.jscsi.target.task.standard.login.LoginRequestTaskDescriptor").newInstance();
		} catch (ClassNotFoundException e) {
			System.out.println("---LoadingClassError----");
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (InstantiationException e) {
			System.out.println("InstantiationException");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.out.println("IllegalAccess");
			e.printStackTrace();
		}
		try {
			Task newTask = newOne.createTask();
		} catch (OperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
