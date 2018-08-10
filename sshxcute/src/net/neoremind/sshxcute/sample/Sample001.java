package net.neoremind.sshxcute.sample;

import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;

public class Sample001 {

	public static void main(String[] args) {
		SSHExec ssh = null;
		try {
			ConnBean cb = new ConnBean("10.14.200.252", "root","root123");
			ssh = SSHExec.getInstance(cb);		
			CustomTask echo = new ExecCommand("pwd");
			ssh.connect();
			Result res = ssh.exec(echo);
			if (res.isSuccess)
			{
				System.out.println("Return code: " + res.rc);
				System.out.println("sysout: " + res.sysout);
			}
			else
			{
				System.out.println("Return code: " + res.rc);
				System.out.println("error message: " + res.error_msg);
			}
		} catch (TaskExecFailException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} finally {
			ssh.disconnect();	
		}
	}

}
