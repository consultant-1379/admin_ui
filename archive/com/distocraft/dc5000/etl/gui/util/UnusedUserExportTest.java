package com.distocraft.dc5000.etl.gui.util;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ericsson.eniq.common.testutilities.DirectoryHelper;
import com.ericsson.eniq.ldap.util.USERSTATE;
import com.ericsson.eniq.ldap.vo.IValueObject;
import com.ericsson.eniq.ldap.vo.UserVO;

public class UnusedUserExportTest {
	
	private static final String EOL = "\n";
	private static final String DEFAULT_EXPORT_PATH = "/eniq/home/dcuser/unused_users.tab";
	private static final String ALT_EXPORT_PATH = "/eniq/home/dcuser/alt_file.tab";

	private static final String EXPECTED_ALL_USERS = "UserId\tFirst Name\tLast Name\tEmail\tPhone\tOrg\tStatus\tPredefined\tRoles\tDays Since Last Login\nadmin\tApp\tAdmin\tadmin@ericsson.com\t\t\t\ttrue\t[sysadmin]\t0\nnbi\tNBI\tNBI\tnbi@ericsson.com\t\t\t\ttrue\t[sysadmin]\t-\ntest1\tTest\tOne\t\t\tEricsson\t\tfalse\t[sysadmin]\t10\ntest2\tTest\tTwo\t\t\t\t\tfalse\t[RoleOne, RoleThree, RoleTwo]\t3\ntest3\tTest\tThree\t\t\t\tUser is Locked\tfalse\t[RoleOne, RoleThree, RoleTwo]\t0\n";
	
	private static final File TMP_DIR = new File(
			System.getProperty("java.io.tmpdir"), "UnusedUserExportTest");

	private static Set<String> basicRole = new TreeSet<String>();

	private static Set<String> multiRoles = new TreeSet<String>();

	private UnusedUserExport exportTest;
	private static File testExportFile;
	private static File readOnlyExportFile;
	private static List<IValueObject> testUserList;

	@BeforeClass
	public static void init() {
		if (!TMP_DIR.exists() && !TMP_DIR.mkdirs()) {
			fail("Failed to create " + TMP_DIR.getPath());
		}
		createTestUserList();

		basicRole.add("sysadmin");
		multiRoles.add("RoleOne");
		multiRoles.add("RoleTwo");
		multiRoles.add("RoleThree");
	}

	@AfterClass
	public static void afterClass() {
		if (testExportFile.exists()) {
			testExportFile.delete();
		}
		if (readOnlyExportFile.exists()) {
			readOnlyExportFile.setWritable(true);
			readOnlyExportFile.delete();
		}
		DirectoryHelper.delete(TMP_DIR);
	}

	@Before
	public void setUp() throws Exception {
		exportTest = new UnusedUserExport();
		testExportFile = new File(TMP_DIR, "test_export.tab");
		readOnlyExportFile = new File(TMP_DIR, "read_only_test.tab");
	}

	@After
	public void tearDown() throws Exception {
	}

	private static void createTestUserList() {
		testUserList = new ArrayList<IValueObject>();

		final Date today = new Date();

		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -3);
		final Date threeDaysAgo = cal.getTime();
		cal.add(Calendar.DATE, -7);
		final Date tenDaysAgo = cal.getTime();

		testUserList.add(createUser("admin", "App", "Admin",
				"admin@ericsson.com", "", "", USERSTATE.STATE_NORMAL, true,
				basicRole, today));
		testUserList.add(createUser("nbi", "NBI", "NBI", "nbi@ericsson.com",
				"", "", USERSTATE.STATE_NORMAL, true, basicRole, null));
		testUserList.add(createUser("test1", "Test", "One", "", "", "Ericsson",
				USERSTATE.STATE_NORMAL, false, basicRole, tenDaysAgo));
		testUserList.add(createUser("test2", "Test", "Two", "", "", "",
				USERSTATE.STATE_NORMAL, false, multiRoles, threeDaysAgo));
		testUserList.add(createUser("test3", "Test", "Three", "", "", "",
				USERSTATE.STATE_LOCKED, false, multiRoles, today));

		// File Output If ALL Users are exported
		// UserId,First Name,Last
		// Name,Email,Phone,Org,Status,Predefined,Roles,Days Since Last Login
		// admin,App,Admin,admin@ericsson.com,,,,true,[[sysadmin]],0
		// nbi,NBI,NBI,nbi@ericsson.com,,,,true,[[sysadmin]],-
		// test1,Test,One,,,Ericsson,,false,[[sysadmin]],10
		// test2,Test,Two,,,,,false,[RoleOne, RoleThree, RoleTwo],3
		// test3,Test,Three,,,,User is Locked,false,[RoleOne, RoleThree,
		// RoleTwo],0

	}

	private static UserVO createUser(final String userId, final String fname,
			final String lname, final String email, final String phone,
			final String org, final USERSTATE userState,
			final boolean isPredefined, final Set<String> roles,
			final Date lastLoginDate) {
		final UserVO user = new UserVO();

		user.setUserId(userId);
		user.setFname(fname);
		user.setLname(lname);
		user.setEmail(email);
		user.setPhone(phone);
		user.setOrg(org);
		user.setUserState(userState);
		user.setPredefined(isPredefined);
		user.setRoles(roles);
		user.setLastLoginDate(lastLoginDate);

		return user;
	}

	/**
	 * Reads file f and returns content as string. Includes ALL lines in the
	 * file, using eol as the end of line character(s)
	 * 
	 * @param f
	 *            File to read from
	 * @param eol
	 *            End of Line character(s) used in returned string
	 * @return Contents of file as a string
	 * @throws Exception
	 */
	public String readFullFileToString(final File f, final String eol)
			throws Exception {

		BufferedReader reader = null;
		StringBuffer result = new StringBuffer();

		try {
			reader = new BufferedReader(new FileReader(f));
			String input;
			while ((input = reader.readLine()) != null) {
				result.append(input + eol);
			}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Exception e) {
					System.out.println("Error occured during closing the file");
					e.printStackTrace();
				}
			}
		}
		return result.toString();
	}
	
	@Test
	public void checkDefaultExportPath() {
		assertEquals(DEFAULT_EXPORT_PATH, exportTest.getExportPath());
	}

	@Test
	public void checkSetExportPath() {
		exportTest.setExportPath(ALT_EXPORT_PATH);
		assertEquals(ALT_EXPORT_PATH, exportTest.getExportPath());
	}

	@Test
	public void checkGetExportPathForNullPath() {
		exportTest.setExportPath(null);

		//getExportPath should revert to default path if exportPath is set to null
		assertEquals(DEFAULT_EXPORT_PATH, exportTest.getExportPath());
	}
	
	@Test
	public void checkExportFile_ZeroDayThreshold() throws Exception {
		exportTest.setExportPath(testExportFile.getAbsolutePath());
		final String message = exportTest.export(testUserList, 0); // days since last login=0 - export
											// all users
		final String actual = readFullFileToString(testExportFile, EOL);

		assertEquals("Exported file contains wrong contents.", EXPECTED_ALL_USERS, actual);
		assertEquals("Wrong message returned by export.", "Exported 5 user(s) to file: " + testExportFile.getAbsolutePath(),message);
	}
	
	@Test
	public void checkExportFile_FileAlreadyExists() throws Exception {
		
		// create file before exporting
		testExportFile.createNewFile();
		
		exportTest.setExportPath(testExportFile.getAbsolutePath());
		exportTest.export(testUserList, 0); // days since last login=0 - export
											// all users
		final String actual = readFullFileToString(testExportFile, EOL);

		assertEquals("Exported file contains wrong contents.", EXPECTED_ALL_USERS, actual);
	}

	@Test
	public void checkExportFile_FileExistsAndReadOnly() throws Exception {
		
		// create file before exporting
		readOnlyExportFile.createNewFile();
		readOnlyExportFile.setReadOnly();
		
		final String expectedMessage = "Export aborted! Export file: " + readOnlyExportFile.getAbsolutePath() + " is read only.";
		
		exportTest.setExportPath(readOnlyExportFile.getAbsolutePath());
		final String message = exportTest.export(testUserList, 0); // days since last login=0 - export
															 // all users

		assertEquals("Wrong message returned by export.", expectedMessage, message);
	}

	@Test
	public void checkExportFile_ThreeDayThreshold() throws Exception {
		final String expected = "UserId\tFirst Name\tLast Name\tEmail\tPhone\tOrg\tStatus\tPredefined\tRoles\tDays Since Last Login\ntest1\tTest\tOne\t\t\tEricsson\t\tfalse\t[sysadmin]\t10\ntest2\tTest\tTwo\t\t\t\t\tfalse\t[RoleOne, RoleThree, RoleTwo]\t3\n";

		exportTest.setExportPath(testExportFile.getAbsolutePath());
		final String message = exportTest.export(testUserList, 3); // days since last login=3 
		final String actual = readFullFileToString(testExportFile, EOL);

		assertEquals("Exported file contains wrong contents.", expected, actual);
		assertEquals("Wrong message returned by export.", "Exported 2 user(s) to file: " + testExportFile.getAbsolutePath(),message);
	}	
	
	@Test
	public void checkExportFile_TenDayThreshold() throws Exception {
		final String expected = "UserId\tFirst Name\tLast Name\tEmail\tPhone\tOrg\tStatus\tPredefined\tRoles\tDays Since Last Login\ntest1\tTest\tOne\t\t\tEricsson\t\tfalse\t[sysadmin]\t10\n";

		exportTest.setExportPath(testExportFile.getAbsolutePath());
		final String message = exportTest.export(testUserList, 10); // days since last login=10
		final String actual = readFullFileToString(testExportFile, EOL);

		assertEquals("Exported file contains wrong contents.", expected, actual);
		assertEquals("Wrong message returned by export.", "Exported 1 user(s) to file: " + testExportFile.getAbsolutePath(),message);
	}

	@Test
	public void checkExportFile_HighThreshold() throws Exception {
		final String expected = "UserId\tFirst Name\tLast Name\tEmail\tPhone\tOrg\tStatus\tPredefined\tRoles\tDays Since Last Login\n";

		exportTest.setExportPath(testExportFile.getAbsolutePath());
		final String message = exportTest.export(testUserList, 100); // days since last login=100 - export no users
		final String actual = readFullFileToString(testExportFile, EOL);

		assertEquals("Exported file contains wrong contents.", expected, actual);
		assertEquals("Wrong message returned by export.", "Exported 0 user(s) to file: " + testExportFile.getAbsolutePath(),message);
	}

}
