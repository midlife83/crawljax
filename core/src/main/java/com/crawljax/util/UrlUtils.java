package com.crawljax.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlUtils {

	private static final Logger LOG = LoggerFactory.getLogger(UrlUtils.class);

	private UrlUtils() {

	}

	/**
	 * @param location
	 *            Current location.
	 * @param link
	 *            Link to check.
	 * @return Whether location and link are on the same domain.
	 */
	public static boolean isLinkExternal(String location, String link) {

		if (!location.contains("://")) {
			// location must always contain :// by rule, it not link is handled as not external
			return false;
		}

		// This will jump out of the local file location
		if (location.startsWith("file") && link.startsWith("/")) {
			return true;
		}

		if (link.contains("://")) {
			if (location.startsWith("file") && link.startsWith("http") || link.startsWith("file")
			        && location.startsWith("http")) {
				// Jump from file to http(s) or from http(s) to file, so external
				return true;
			}
			try {
				URL locationUrl = new URL(location);
				try {
					URL linkUrl = new URL(link);
					if (linkUrl.getHost().equals(locationUrl.getHost())) {
						String linkPath = UrlUtils.getBasePath(linkUrl);
						return !(linkPath.startsWith(UrlUtils.getBasePath(locationUrl)));
					}
					return true;
				} catch (MalformedURLException e) {
					LOG.info("Can not parse link " + link + " to check its externalOf "
					        + location);
					return false;
				}
			} catch (MalformedURLException e) {
				LOG.info("Can not parse location " + location + " to check if " + link
				        + " isExternal", e);
				return false;
			}
		} else {
			// No full url specifier so internal link...
			return false;
		}
	}

	/**
	 * Internal used function to strip the basePath from a given url.
	 * 
	 * @param url
	 *            the url to examine
	 * @return the base path with file stipped
	 */
	static String getBasePath(URL url) {
		String file = url.getFile().replaceAll("\\*", "");

		try {
			return url.getPath().replaceAll(file, "");
		} catch (PatternSyntaxException pe) {
			LOG.error(pe.getMessage());
			return "";
		}

	}

	/**
	 * @param url
	 *            the URL string.
	 * @return the base part of the URL.
	 */
	public static String getBaseUrl(String url) {
		String head = url.substring(0, url.indexOf(":"));
		String subLoc = url.substring(head.length() + DomUtils.BASE_LENGTH);
		return head + "://" + subLoc.substring(0, subLoc.indexOf("/"));
	}

	/**
	 * Retrieve the var value for varName from a HTTP query string (format is
	 * "var1=val1&var2=val2").
	 * 
	 * @param varName
	 *            the name.
	 * @param haystack
	 *            the haystack.
	 * @return variable value for varName
	 */
	public static String getVarFromQueryString(String varName, String haystack) {
		if (haystack == null || haystack.length() == 0) {
			return null;
		}
		if (haystack.charAt(0) == '?') {
			haystack = haystack.substring(1);
		}
		String[] vars = haystack.split("&");

		for (String var : vars) {
			String[] tuple = var.split("=");
			if (tuple.length == 2 && tuple[0].equals(varName)) {
				return tuple[1];
			}
		}
		return null;
	}
}