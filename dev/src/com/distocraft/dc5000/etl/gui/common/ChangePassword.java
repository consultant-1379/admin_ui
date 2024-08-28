package com.distocraft.dc5000.etl.gui.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import com.ericsson.eniq.common.RemoteExecutor;
import com.ericsson.eniq.repository.AsciiCrypter;
import com.ericsson.catalina.users.Encryption;
import com.jcraft.jsch.JSchException;

public class ChangePassword extends EtlguiServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4703095925910424364L;

	private static final ReentrantLock lock = new ReentrantLock();
	private static final String CRYPT_STAMP_LABEL = "ENCRYPTION";
	private static final String CRYPT_STAMP_LABELS = "AsciiCrypter";
	private static final String CRYPT_STAMP = "{AsciiCrypter}";
	private final Set<String> notAllowedChars = new HashSet<>();
	private boolean result = false;
	private static final String ERROR_FLAG = "errorOccured";
	private static final String INSTANCE_FLAG = "anotherInstanceRunning";
	private static final Log log = LogFactory.getLog(ChangePassword.class);
	private static final String PASS = "password";
	private static final String PASS_PATTERN = "(?<=" + PASS + "=\").*?(?=\")";
	private static final String APPLICATION_USER = "dcuser";
	private static final String HOST_ADD = "webserver";
	private String oldPassSet = "";
	private String newPassSet = "";
	private String userNameSet = "";

	@SuppressWarnings("deprecation")
	@Override
	public Template doHandleRequest(HttpServletRequest request, HttpServletResponse response, Context ctx)
			throws Exception {
		String page = "ChangeTomcatPasword.vm";
		String oldPassword = request.getParameter("oldpassword");
		String newPassword = request.getParameter("newpassword");

		
		if (this.result && userNameSet.equals(getEniqUserName(request))) {
			log.debug("Tomcat restart called for user " + userNameSet);
			restartTomcat(ctx);
		}

		if (newPassword != null && oldPassword != null) {
			changePass(request, oldPassword, newPassword, ctx);
		}

		return getTemplate(page);
	}

	private void changePass(HttpServletRequest request, String oldPassword, String newPassword, Context ctx) {
		boolean lockStatus = lock.isLocked();		
		if (lockStatus) {
			ctx.put(INSTANCE_FLAG, true);
		} else {
			lock.lock();
			try {
				Thread.sleep(1000);
				formatTomcatUsersXml();
				String oldPassEncrypted = getOldPassword(request, ctx);
				this.oldPassSet = oldPassEncrypted;
				String oldPass = decryptPassword(oldPassEncrypted, ctx);
				boolean oldPassCheck = oldPass.equals(oldPassword);				
				boolean newPassCheck = checkNewPassCompliance(newPassword);
				boolean scriptInstanceCheck = checkOtherInstance(ctx);
				this.newPassSet = newPassword;
				if (oldPassCheck && !newPassCheck && !scriptInstanceCheck) {
					this.userNameSet = getEniqUserName(request);
					/*
					 * replaceOldWithNewPassword(oldPassEncrypted, newPassword, true, ctx);
					 * 
					 * EQEV-122481 : AdminUI password should not update in .xml directly from UI
					 * Password should be validate and update in tomcat-users.xml file through script
					 */
					replaceOldWithNewPasswordCLI(request, oldPassword, newPassword, true, ctx);
				}

				ctx.put("wrongOldPass", oldPassCheck);
				ctx.put("passComplianceFailed", newPassCheck);
			} catch (InterruptedException e1) {
				log.warn("Thread Interrupted: " + e1);
				ctx.put(INSTANCE_FLAG, true);
				Thread.currentThread().interrupt();
			} finally {
				lock.unlock();
			}
		}
	}

	private String decryptPassword(String oldPassEncrypted, Context ctx) {
		String oldPassDecrypted = "";
		try {
			if(oldPassEncrypted.startsWith(CRYPT_STAMP)) {
				oldPassDecrypted = AsciiCrypter.getInstance()
						.decrypt(oldPassEncrypted.replaceFirst("\\{" + CRYPT_STAMP_LABELS + "\\}", ""));
			}
			else
			{
				oldPassDecrypted = Encryption
						.decrypt(oldPassEncrypted.replaceFirst("\\{" + CRYPT_STAMP_LABEL + "\\}", ""));
			}
			
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {			log.warn("unable to decrypt the password: " + e);
			ctx.put(ERROR_FLAG, true);
		}
		return oldPassDecrypted;
	}

	private boolean checkOtherInstance(Context ctx) {
		boolean scriptStatus = false;
		try {
			String value = RemoteExecutor.executeComandSshKey(APPLICATION_USER, HOST_ADD,
					"ps -aef | grep ./manage_tomcat_user.bsh | grep -v grep | wc -l");
			log.debug("Number of script instances running:" + value);
			if (Integer.parseInt(value.trim()) > 0) {
				scriptStatus = true;
				ctx.put(INSTANCE_FLAG, true);
			}
		} catch (JSchException | IOException e) {
			log.warn("Exception while checking the script running instance: " + e);
			this.result = false;
			ctx.put(ERROR_FLAG, true);
			scriptStatus = true;
		}
		return scriptStatus;
	}

	private boolean checkNewPassCompliance(String newPassword) {
		addCharactersInSet();
		for (String character : notAllowedChars) {
			int index = newPassword.indexOf(character);
			if (index != -1) {
				return true;
			}
		}
		return false;
	}

	private void addCharactersInSet() {
		notAllowedChars.add(" ");
		notAllowedChars.add("\\");
		notAllowedChars.add("/");
		notAllowedChars.add("\"");
		notAllowedChars.add("&");
		notAllowedChars.add("<");
		notAllowedChars.add(">");
		notAllowedChars.add("'");
	}
	
	private synchronized void replaceOldWithNewPasswordCLI(HttpServletRequest request, String oldPassword,
			String newPassword, boolean value, Context ctx) {
		boolean flag = false;
		String changePasswordResult = null;
		try {
			String cmd = "/eniq/sw/installer/manage_tomcat_user.bsh -A CHANGE_PASSWORD_UI " + getEniqUserName(request)
					+ " " + oldPassword + " " + newPassword;
			changePasswordResult = RemoteExecutor.executeComandSshKey(APPLICATION_USER, HOST_ADD, cmd);
		} catch (JSchException | IOException e) {
			log.warn("Exception while calling replaceOldWithNewPasswordCLI : " + e);
			this.result = flag;
			ctx.put(ERROR_FLAG, true);
		}
		if (value && changePasswordResult != null) {
			if (changePasswordResult.contains("successfully")) {
				flag = true;
				ctx.put("passChangedSuccessfully", flag);
				this.result = flag;
			} else {
				flag = true;
				ctx.put("scriptErrorMessageFlag", flag);
				ctx.put("scriptErrorMessage", changePasswordResult);
				this.result = flag;
			}
		}
		log.info("ADMINUI passChangedSuccessfully final");
	}

	private synchronized void restartTomcat(Context ctx) {
		try {
			RemoteExecutor.executeComandSshKey(APPLICATION_USER, HOST_ADD,
					"/eniq/sw/installer/manage_tomcat_user.bsh -T RESTART_TOMCAT");
		} catch (JSchException | IOException e) {
			log.warn("Exception while restarting the webserver: " + e);
			this.result = false;
			ctx.put(ERROR_FLAG, true);
			/*
			 * If unable to run the script to restart tomcat then again replace the
			 * newPassword with oldEncryptedPassword in the tomcat file
			 */
			replaceOldWithNewPassword(this.newPassSet, this.oldPassSet, false, ctx);
		}
	}

	private synchronized void replaceOldWithNewPassword(String oldPassEncrypted, String newPassword, boolean value,
			Context ctx) {
		Path path = Paths.get("/eniq/sw/runtime/tomcat/conf/" + "tomcat-users.xml"); // NOSONAR
		boolean flag = true;
		try {
			Charset charset = StandardCharsets.UTF_8;
			try (Stream<String> lines = Files.lines(path, charset)) {
				List<String> replaced = lines.map(line -> {
					if (line.contains("username=\"" + userNameSet + "\"")) {
						return line.replace("password=\"" + oldPassEncrypted + "\"",
								"password=\"" + newPassword + "\"");
					} else {
						return line;
					}
				}).collect(Collectors.toList());
				Files.write(Paths.get("/eniq/sw/runtime/tomcat/conf/tomcat-users.xml"), replaced);
			} catch (IOException e) {
				flag = false;
				e.printStackTrace();
			}
		} catch (Exception e) {
			flag = false;
			e.printStackTrace();
		}
		if (value) {
			ctx.put("passChangedSuccessfully", flag);
			this.result = flag;
		}
	}

	private String getOldPassword(HttpServletRequest request, Context ctx) {
		String eniqUserName = getEniqUserName(request);
		String searchPatternforuser = "username=\"" + eniqUserName + "\"";
		String getSearchedLine = "";
		String oldEncryptedPass = "";
		log.info("Logged in as: " + eniqUserName);
		Path path = Paths.get("/eniq/sw/runtime/tomcat/conf/" + "tomcat-users.xml"); // NOSONAR

		try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8);) {
			Iterator<String> itr = lines.iterator();
			while (itr.hasNext()) {
				String line = itr.next().trim();
				if (line.contains(searchPatternforuser)) {
					getSearchedLine = line;
					break;
				}
			}
		} catch (FileNotFoundException e) {
			log.warn("Tomcat file not found: " + e);
			ctx.put(ERROR_FLAG, true);
		} catch (IOException e1) {
			ctx.put(ERROR_FLAG, true);
			log.warn("Exception while reading the tomcat users file: " + e1);
		}
		Pattern pattern = Pattern.compile(PASS_PATTERN);
		Matcher match = pattern.matcher(getSearchedLine);
		while (match.find()) {
			oldEncryptedPass = getSearchedLine.substring(match.start(), match.end());
		}
		return oldEncryptedPass;
	}

	private String getEniqUserName(HttpServletRequest request) {
		String userName = null;
		if (request.getUserPrincipal() != null) {
			userName = request.getUserPrincipal().getName();
		}
		return userName;
	}

	private void formatTomcatUsersXml() {
		
		final String tomCatUsersFilePath = "/eniq/sw/runtime/tomcat/conf/" + "tomcat-users.xml";
		try (BufferedReader bReader = Files.newBufferedReader(Paths.get(tomCatUsersFilePath)) ;) {
			String tempLine = "";
			String origText = "";

			while ((tempLine = bReader.readLine()) != null) {
				StringBuilder bld = new StringBuilder();
				bld.append(origText + tempLine + "\n");
				origText = bld.toString();
			}
			
				String replacedText = origText.replace("roles=\"eniq\"/> ", "roles=\"eniq\"/>\n");				
				replacedText = replacedText.replace("</tomcat-users>", "\n</tomcat-users>");
				Files.write(Paths.get(tomCatUsersFilePath), replacedText.getBytes(StandardCharsets.UTF_8));
				
		} catch (Exception e) {
			log.warn("Exception while reading Tomcat users file:" + e);
		}
	}
	
	}