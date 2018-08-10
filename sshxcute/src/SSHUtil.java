
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import net.neoremind.sshxcute.core.ConnBean;
import net.neoremind.sshxcute.core.IOptionName;
import net.neoremind.sshxcute.core.Result;
import net.neoremind.sshxcute.core.SSHExec;
import net.neoremind.sshxcute.exception.TaskExecFailException;
import net.neoremind.sshxcute.task.CustomTask;
import net.neoremind.sshxcute.task.impl.ExecCommand;
import net.neoremind.sshxcute.task.impl.ExecShellScript;

/**
 * sshxcute.jar二次封装类 
 *  在执行命令时，命令前面已经是他的完全路径，多条命令用分号隔开
 *
 */
public final class SSHUtil {
	private SSHExec sshx = null;
	private ConnBean cb = null;

	public SSHUtil(String ip, String username, String password) {
		// 如果遇到失败，仍然想继续执行剩下的任务
		SSHExec.setOption(IOptionName.HALT_ON_FAILURE, true);
		// 修改错误日志输入目录
		SSHExec.setOption(IOptionName.ERROR_MSG_BUFFER_TEMP_FILE_PATH, "/var/log/sshxcute_err.msg");
		this.cb = new ConnBean(ip, username, password);
	}
	
	public boolean connect() {
		if (null == cb) {
			return false;
		}else if(sshx!=null&&sshx.connect()) {
			return true;
		}else {
			sshx = SSHExec.getInstance(cb);
			return sshx.connect();	
		}
	}
	/**
	 * 执行命令，返回Result
	 * @param command,keyword表示错误信息过滤关键字
	 * @return
	 */
	public Result execute(String cmd,String[] keyword) {
		this.connect();
		// 执行的命令行任务
		CustomTask sampleTask = new ExecCommand(cmd);
		sampleTask.resetErrSysoutKeyword(keyword);
		// 执行，并对执行后的结果进行处理
		try {
			return sshx.exec(sampleTask);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			sshx.disconnect();
		}
	}

	/**
	 * 执行命令，返回Result
	 * @param command
	 * @return
	 */
	public Result execute(String... cmd) {
		this.connect();
		// 执行的命令行任务
		CustomTask sampleTask = new ExecCommand(cmd);
		// 执行，并对执行后的结果进行处理
		try {
			return sshx.exec(sampleTask);
		} catch (TaskExecFailException e) {
			e.printStackTrace();
			return null;
		} finally {
			sshx.disconnect();
		}
	}
	
	/**
	 * 执行命令，返回字符串
	 * @param command
	 * @return
	 */
	public String exec(String... cmd) {
		this.connect();
		// 执行的命令行任务
		CustomTask sampleTask = new ExecCommand(cmd);
		// 执行，并对执行后的结果进行处理
		try {
			Result result= sshx.exec(sampleTask);
			if (result.isSuccess) {
				return result.sysout;
			}else {
				return "error";
			}
		} catch (TaskExecFailException e) {
			e.printStackTrace();
			return null;
		} finally {
			sshx.disconnect();
		}
	}
	/**
	 * 执行命令，无返回值
	 * @param command
	 * @return
	 */
	public void exe(String... cmd) {
		this.connect();
		// 执行的命令行任务
		CustomTask sampleTask = new ExecCommand(cmd);
		// 执行，并对执行后的结果进行处理
		try {
			sshx.exec(sampleTask);
		} catch (TaskExecFailException e) {
			e.printStackTrace();
		} finally {
			sshx.disconnect();
		}
	}

	/**
	 * 快速执行
	 * @param ip
	 * @param username
	 * @param password
	 * @param cmd
	 * @return
	 */
	public static String exe(String ip,String username,String password,String cmd) {
		SSHUtil ssh =new SSHUtil(ip,username,password);
		return ssh.exec(cmd);
	} 
	
	/**
	 * shellPath 代表脚本执行路径
	 * @param shellPath
	 * @return
	 */
	public Result exeScript(String shellPath) {
		this.connect();
		CustomTask task = new ExecShellScript(shellPath);
		try {
			return sshx.exec(task);
		} catch (TaskExecFailException e) {
			e.printStackTrace();
			return null;
		} finally {
			sshx.disconnect();
		}
	}
	
	/**
	 * 带参执行脚本
	 * @param shellPath
	 * @param args
	 * @return
	 */
	public Result exeScript(String shellPath, String args) {
		this.connect();
		CustomTask task = new ExecShellScript(shellPath, args);
		try {
			return sshx.exec(task);
		} catch (TaskExecFailException e) {
			e.printStackTrace();
			return null;
		} finally {
			sshx.disconnect();
		}
	}
	
	/**
	 * 上传文件
	 * @param localFile
	 * @param remotePath
	 */
	public boolean putFile(String localFile, String remoteDir) {
		this.connect();
		try {
			sshx.uploadSingleDataToServer(localFile, remoteDir);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			sshx.disconnect();
		}
	}
	
	/**
	 * 上传目录
	 * @param localPath
	 * @param remotePath
	 */
	public boolean putDir(String localPath, String remotePath) {
		this.connect();
		try {
			sshx.uploadAllDataToServer(localPath, remotePath);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			sshx.disconnect();
		}
	}

	public String getFile(String remoteFile) {
		this.connect();
		CustomTask task = new ExecCommand("cat "+remoteFile);
		try {
			return sshx.exec(task).sysout;
		} catch (TaskExecFailException e) {
			e.printStackTrace();
			return null;
		} finally {
			sshx.disconnect();
		}
	}
	
	/**
	 * 相同文件名
	 * @param remoteFile
	 * @param localDir
	 * @return
	 */
	public boolean getFile(String remoteFile, String localDir) {
		String fileName=remoteFile.substring(remoteFile.lastIndexOf(File.separator));
		//System.out.println(fileName);
		this.connect();
		CustomTask task = new ExecCommand("cat "+remoteFile);
		try {
			String content= sshx.exec(task).sysout;
			writeNewFile(localDir+fileName, content);
			return true;
		} catch (TaskExecFailException e) {
			e.printStackTrace();
			return false;
		} finally {
			sshx.disconnect();
		}
	}
	/**
	 * 解析结果
	 * @param result
	 * @return
	 */
	public Map<String, String> parseResult(Result result) {
		if(null==result){
			System.out.println("Result空值！");
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		// Check result and print out messages.
		if (result.isSuccess) {
			map.put("isSuccess", "true");
			map.put("returnCode", result.rc + "");
			map.put("sysout", result.sysout);
		} else {
			map.put("isSuccess", "false");
			map.put("returnCode", result.rc + "");
			map.put("sysout", result.error_msg);
		}
		return map;
	}
	/**
	 * 解析结果
	 * @param result
	 * @return
	 */
	public String getString(Result result) {
		if(null==result){
			System.out.println("Result空值！");
			return null;
		}
		if (result.isSuccess) {
			return result.sysout;
		} else {
			return result.error_msg;
		}
	}
	
	/**
	 * 将1行数据写入空文件
	 * @param fileName
	 * @param lines
	 * @return
	 */
	public static boolean writeNewFile(String fileName, String line) {
		File file = new File(fileName);
		try {
			if (file.isFile()) {
				file.delete();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
			bw.write(line);
			bw.flush();
			bw.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 测试方法
	 * @param args
	 */
	public static void main(String[] args) {
		SSHUtil ssh = new SSHUtil("192.168.1.160", "root", "123456");
		System.out.println(ssh.getFile("/root/yarn-site.xml","/root"));
		
		/*System.out.println("判定是否连接：connect="+ssh.connect());
		System.out.println("-------------exec()方法，执行命令，返回字符串---------------------------");
		System.out.println(ssh.exec("hostname"));
		System.out.println("-------------execute方法，执行命令，返回Result对象---------------------");
		Result result=ssh.execute("date");
		Map<String, String> map=ssh.parseResult(result);
		for(String key:map.keySet()){
			System.out.println(key+":"+map.get(key));
		}
		System.out.println("---------------执行多条命令--------------------");
		result=ssh.execute("ls -l /opt", "uname");
		map=ssh.parseResult(result);
		for(String key:map.keySet()){
			System.out.println(key+":"+map.get(key));
		}
	  System.out.println("----------------上传文件---------------------");
		boolean success=ssh.putFile("/root/test/sshxcuteTest.sh", "/root/sshxcuteTest.sh");
		if(success){
			System.out.println("上传文件成功！");
		}else{
			System.out.println("上传文件失败！");
		}
		System.out.println("---------------上传目录----------------------");
		if(ssh.putDir("/root/test", "/root")) {
			System.out.println("上传目录成功！");
		}else {
			System.out.println("上传目录失败！");
		}
		System.out.println("---------------执行脚本----------------------");
		//ssh.exe("chmod +x /root/test/*");
		//参数放到同一个字符串中，通过空格分割
		result=ssh.exeScript("/root/test/sshxcuteTest.sh", "hello world");
		System.out.println(ssh.getString(result));
		result=ssh.exeScript("/root/test/hello");
		System.out.println(ssh.getString(result));
		
		System.out.println("-------------exe()方法，执行命令，无返回值---------------------------");
		ssh.exe("systemctl stop firewalld && systemctl disable firewalld");
		ssh.exe("sed -i 's/SELINUX=enforcing/SELINUX=disabled/' /etc/selinux/config");
		*//**
		 *  2017-08-09 解决执行“setenforce 0”失败的问题！
		 * net.neoremind.sshxcute.exception.TaskExecFailException: The task has failed to execute : Exec command setenforce 0
		 * [root@hadron ~]# setenforce 0
	   *  setenforce: SELinux is disabled
	   * [root@hadron log]# cat sshxcute_err.msg 
		 *	 setenforce: SELinux is disabled
		 *//*
		ssh.exe("setenforce 0");*/
		
	}
	/**
	 * 主方法执行结果如下：
	 * 

判定是否连接：connect=true
-------------exec()方法，执行命令，返回字符串---------------------------
nb0

-------------execute方法，执行命令，返回Result对象---------------------
returnCode:0
sysout:Wed Aug  9 03:06:53 EDT 2017

isSuccess:true
---------------执行多条命令--------------------
returnCode:0
sysout:total 5218652
drwxr-xr-x  3 root root        31 Jun 28 22:14 a
-rw-------. 1 root root      1264 Apr  5 05:31 anaconda-ks.cfg
-rw-r--r--  1 root root     12999 May 15 00:44 a.xml
-rw-r--r--  1 root root       333 Jul 18 22:59 books.bak
-rw-r--r--  1 root root     13004 Apr 11 03:33 b.xml
drwxrwxr-x  2 4021 4021     16384 Jul 19 21:10 data
-rw-r--r--  1 root root       658 Apr 22 23:55 derby.log
drwxr-xr-x  2 root root       102 Apr  6 00:33 export
-rw-r--r--  1 root root       977 Apr 12 05:41 File1.java
-rw-r--r--  1 root root       977 Apr 12 05:41 File.java
-rw-r--r--  1 root root 118564010 Jul 20 20:52 root
-rw-------  1 root root       524 Apr  5 01:57 sshKeyGen.sh
-rw-r--r--  1 root root       256 Aug  9 03:03 sshxcuteTest.sh
l
Linux

isSuccess:true
----------------上传文件---------------------
上传文件成功！
---------------上传目录----------------------
上传目录成功！
---------------执行脚本----------------------
Login success

hello

-------------exe()方法，执行命令，无返回值---------------------------



	 */
	
}
