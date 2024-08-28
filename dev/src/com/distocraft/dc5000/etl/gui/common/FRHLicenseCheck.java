package com.distocraft.dc5000.etl.gui.common;

import java.rmi.Naming;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ericsson.eniq.exception.LicensingException;
import com.ericsson.eniq.licensing.cache.DefaultLicenseDescriptor;
import com.ericsson.eniq.licensing.cache.LicenseDescriptor;
import com.ericsson.eniq.licensing.cache.LicenseInformation;
import com.ericsson.eniq.licensing.cache.LicensingCache;
import com.ericsson.eniq.licensing.cache.LicensingResponse;

public class FRHLicenseCheck {

	private final Log log = LogFactory.getLog(this.getClass());
	private static FRHLicenseCheck obj = null;
	private final String LICENCE_REFNAME = "LicensingCache";
	private final String LICENCE_HOSTNAME = "licenceservice";
	private final String LICENCE_PORT = "1200";
	private final String FRHLicense = "CXC4012107";
	private String activeFRHlicense = null;

	private FRHLicenseCheck() {

	}

	public static FRHLicenseCheck getInstance() {
		if (obj == null) {
			obj = new FRHLicenseCheck();
		}
		return obj;
	}
	public boolean checkFRHLicense(LicensingCache cache, String FRHLicense) {
		boolean isValid = false;

		try {
			final LicenseDescriptor license = new DefaultLicenseDescriptor(FRHLicense);
			final LicensingResponse response = cache.checkLicense(license);

			isValid = response.isValid();

			if (isValid) {
				log.debug("Checking if FRH license is time-restricted");

				for (LicenseInformation li : cache.getLicenseInformation()) {
					if (li.getFeatureName().equalsIgnoreCase(FRHLicense)) {
						if (li.getDeathDay() > 0) {
							log.debug("Expiry date in long of FRH license: " + li.getDeathDay());
							if (li.getDeathDay() < System.currentTimeMillis()) {
								log.error("FRH license " + FRHLicense + " is expired.");
							} else {
								log.debug("FRH license is not an unlimited expiry license.");
								isValid = true;
							}
						} else {
							log.info("Unlimited expiry started license successfully validated.");
							isValid = true;
						}
						break;
					}
				}
			}

		} catch (Exception e) {
			log.warn("Starter license validation failed", e);
		}

		if (isValid) {
			activeFRHlicense = FRHLicense;
		}

		return isValid;
	}

	private LicensingCache connectLicensingCache() throws LicensingException {
		try {
			LicensingCache cache=(LicensingCache) Naming.lookup("//"+LICENCE_HOSTNAME+":"+LICENCE_PORT+"/"+LICENCE_REFNAME);
			log.debug("url lookup successfull");
			return cache;
		} catch (Exception e) {
			throw new LicensingException("Unable to connect to LicenseManager. Pls check that licmgr is running.", e);
		}
	}


	/**
	 * Checks if the system has valid license to start.
	 *
	 * @exception Exception
	 *                is thrown is no valid license available
	 */
	public final boolean checkFrhLicense() throws LicensingException {
		boolean isLicenseValid=false;
		final LicensingCache cache = connectLicensingCache();
		if (cache == null) {
			isLicenseValid=false;
			throw new LicensingException("Unable to connect to LicenseManager @ " + LICENCE_HOSTNAME);
		}
		if (checkFRHLicense(cache, FRHLicense)) {
			isLicenseValid=true;
			return isLicenseValid;
		} else {
			isLicenseValid=false;
			throw new LicensingException("No valid FRH license available");
		}
		
	}
}
