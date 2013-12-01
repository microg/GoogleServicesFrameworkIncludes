package com.google.android.gsf;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.opengl.GLES10;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import com.google.android.AndroidContext;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import java.util.*;

@SuppressWarnings("HardCodedStringLiteral")
public class PrivacyExtension {

	private static final String TAG = "PrivacyExtension";

	private static void addEglExtensions(List<String> glExtensions) {
		EGL10 egl10 = (EGL10) EGLContext.getEGL();
		if (egl10 != null) {
			EGLDisplay display = egl10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
			egl10.eglInitialize(display, new int[2]);
			int cf[] = new int[1];
			if (egl10.eglGetConfigs(display, null, 0, cf)) {
				EGLConfig[] configs = new EGLConfig[cf[0]];
				if (egl10.eglGetConfigs(display, configs, cf[0], cf)) {
					int[] a1 =
							new int[]{EGL10.EGL_WIDTH, EGL10.EGL_PBUFFER_BIT, EGL10.EGL_HEIGHT, EGL10.EGL_PBUFFER_BIT,
									  EGL10.EGL_NONE};
					int[] a2 = new int[]{12440, EGL10.EGL_PIXMAP_BIT, EGL10.EGL_NONE};
					int[] a3 = new int[1];
					for (int i = 0; i < cf[0]; i++) {
						egl10.eglGetConfigAttrib(display, configs[i], EGL10.EGL_CONFIG_CAVEAT, a3);
						if (a3[0] != EGL10.EGL_SLOW_CONFIG) {
							egl10.eglGetConfigAttrib(display, configs[i], EGL10.EGL_SURFACE_TYPE, a3);
							if ((1 & a3[0]) != 0) {
								egl10.eglGetConfigAttrib(display, configs[i], EGL10.EGL_RENDERABLE_TYPE, a3);
								if ((1 & a3[0]) != 0) {
									addExtensionsForConfig(egl10, display, configs[i], a1, null, glExtensions);
								}
								if ((4 & a3[0]) != 0) {
									addExtensionsForConfig(egl10, display, configs[i], a1, a2, glExtensions);
								}
							}
						}
					}
				}
			}
			egl10.eglTerminate(display);
		}
	}

	private static void addExtensionsForConfig(EGL10 egl10, EGLDisplay egldisplay, EGLConfig eglconfig, int ai[],
											   int ai1[], List<String> set) {
		EGLContext eglcontext = egl10.eglCreateContext(egldisplay, eglconfig, EGL10.EGL_NO_CONTEXT, ai1);
		if (eglcontext != EGL10.EGL_NO_CONTEXT) {
			javax.microedition.khronos.egl.EGLSurface eglsurface =
					egl10.eglCreatePbufferSurface(egldisplay, eglconfig, ai);
			if (eglsurface == EGL10.EGL_NO_SURFACE) {
				egl10.eglDestroyContext(egldisplay, eglcontext);
			} else {
				egl10.eglMakeCurrent(egldisplay, eglsurface, eglsurface, eglcontext);
				String s = GLES10.glGetString(7939);
				if (s != null && !s.isEmpty()) {
					String as[] = s.split(" ");
					int i = as.length;
					for (int j = 0; j < i; j++) {
						set.add(as[j]);
					}

				}
				egl10.eglMakeCurrent(egldisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
				egl10.eglDestroySurface(egldisplay, eglsurface);
				egl10.eglDestroyContext(egldisplay, eglcontext);
			}
		}
	}

	private static void fillAndroidContextFromBuild(ContentResolver r, AndroidContext aContext) {
		aContext.setBuildProduct(getPrivacyString(r, "Build.PRODUCT", Build.PRODUCT));
		aContext.setBuildFingerprint(getPrivacyString(r, "Build.FINGERPRINT", Build.FINGERPRINT));
		aContext.setBuildBootloader(getPrivacyString(r, "Build.BOOTLOADER", Build.BOOTLOADER));
		aContext.setBuildBrand(getPrivacyString(r, "Build.BRAND", Build.BRAND));
		aContext.setBuildDevice(getPrivacyString(r, "Build.DEVICE", Build.DEVICE));
		aContext.setBuildHardware(getPrivacyString(r, "Build.HARDWARE", Build.HARDWARE));
		aContext.setBuildManufacturer(getPrivacyString(r, "Build.MANUFACTURER", Build.MANUFACTURER));
		aContext.setBuildModel(getPrivacyString(r, "Build.MODEL", Build.MODEL));
		aContext.setBuildTime(getPrivacyLong(r, "Build.TIME", Build.TIME));
		aContext.setBuildCpuAbi(getPrivacyString(r, "Build.CPU_ABI", Build.CPU_ABI));
		aContext.setBuildCpuAbi2(getPrivacyString(r, "Build.CPU_ABI2", Build.CPU_ABI2));
		aContext.setBuildRadio(getPrivacyString(r, "Build.radioVersion", getRadioVersion()));
		aContext.setBuildId(getPrivacyString(r, "Build.ID", Build.ID));
		aContext.setBuildSerial(getPrivacyString(r, "Build.SERIAL", Build.SERIAL));
		aContext.setBuildSdkVersion(getPrivacyInt(r, "Build.VERSION.SDK_INT", Build.VERSION.SDK_INT));
	}

	private static void fillAndroidContextFromConfigurationInfo(Context context, ContentResolver r,
																AndroidContext aContext) {
		try {
			ConfigurationInfo config =
					((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getDeviceConfigurationInfo();
			aContext.setDeviceTouchScreen(getPrivacyInt(r, "ConfigurationInfo.reqTouchScreen", config.reqTouchScreen));
			aContext.setDeviceKeyboardType(
					getPrivacyInt(r, "ConfigurationInfo.reqKeyboardType", config.reqKeyboardType));
			aContext.setDeviceNavigation(getPrivacyInt(r, "ConfigurationInfo.reqNavigation", config.reqNavigation));
			aContext.setDeviceInputFeatures(
					getPrivacyInt(r, "ConfigurationInfo.reqInputFeatures", config.reqInputFeatures));
			aContext.setDeviceGlEsVersion(getPrivacyInt(r, "ConfigurationInfo.reqGlEsVersion", config.reqGlEsVersion));
		} catch (Exception e) {
			Log.w(TAG, "could not access activity manager", e);
		}
	}

	private static void fillAndroidContextFromDisplayMetrics(Context context, ContentResolver r,
															 AndroidContext aContext) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		aContext.setDeviceDensityDpi(getPrivacyInt(r, "DisplayMetrics.densityDpi", displayMetrics.densityDpi));
		aContext.setDeviceHeightPixels(getPrivacyInt(r, "DisplayMetrics.heightPixels", displayMetrics.heightPixels));
		aContext.setDeviceWidthPixels(getPrivacyInt(r, "DisplayMetrics.widthPixels", displayMetrics.widthPixels));
	}

	private static void fillAndroidContextFromPackageManager(Context context, AndroidContext aContext) {
		// TODO use pext
		try {
			PackageManager pm = context.getPackageManager();
			aContext.setDeviceSharedLibraries(Arrays.asList(pm.getSystemSharedLibraryNames()));
			List<String> features = new ArrayList<String>();
			for (FeatureInfo feature : pm.getSystemAvailableFeatures()) {
				if (feature.name != null) {
					features.add(feature.name);
				}
			}
			aContext.setDeviceFeatures(features);
		} catch (Exception e) {
			Log.w(TAG, "could not access package manager", e);
		}
	}

	private static void fillAndroidContextFromTelephonyManager(Context context, ContentResolver r,
															   AndroidContext aContext) {
		String deviceId = null;
		int phoneType = TelephonyManager.PHONE_TYPE_NONE;
		try {
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			aContext.setCellOperatorName(
					getPrivacyString(r, "TelephonyManager.getNetworkOperatorName", tm.getNetworkOperatorName()));
			aContext.setCellOperator(
					getPrivacyString(r, "TelephonyManager.getNetworkOperator", tm.getNetworkOperator()));
			aContext.setSimOperatorName(
					getPrivacyString(r, "TelephonyManager.getSimOperatorName", tm.getSimOperatorName()));
			aContext.setSimOperator(getPrivacyString(r, "TelephonyManager.getSimOperator", tm.getSimOperator()));
			deviceId = getPrivacyString(r, "TelephonyManager.getDeviceId", tm.getDeviceId());
			phoneType = getPrivacyInt(r, "TelephonyManager.getPhoneType", tm.getPhoneType());
		} catch (Exception e) {
			Log.w(TAG, "could not access telephony manager", e);
		}
		if (deviceId != null) {
			if (deviceId.length() == 8) {
				aContext.setEsn(deviceId);
			} else if (deviceId.length() == 14) {
				aContext.setMeid(deviceId);
			} else if (deviceId.length() == 15) {
				aContext.setImei(deviceId);
			}
		}
	}

	public static AndroidContext getAndroidContext(Context context) {
		ContentResolver r = context.getContentResolver();
		AndroidContext aContext = new AndroidContext();

		fillAndroidContextFromPackageManager(context, aContext);
		fillAndroidContextFromTelephonyManager(context, r, aContext);
		fillAndroidContextFromConfigurationInfo(context, r, aContext);
		fillAndroidContextFromDisplayMetrics(context, r, aContext);
		fillAndroidContextFromBuild(r, aContext);

		aContext.setDigest(Gservices.getString(r, "digest"));
		aContext.setLoggingId(GoogleSettingsContract.Partner.getLong(r, "logging_id2", 0));
		aContext.setAndroidId(getAndroidId(r));
		aContext.setBuildClientId(GoogleSettingsContract.Partner.getString(r, "client_id", "android-google"));
		int softwareVersion = 8015017; // TODO

		try {
			aContext.setDeviceScreenLayout(getPrivacyInt(r, "Configuration.screenLayout",
														 context.getResources().getConfiguration().screenLayout));
		} catch (Exception e) {
			Log.w(TAG, "could not access configuration", e);
		}


		List<String> locales = null;
		try {
			locales = Arrays.asList(context.getAssets().getLocales());
		} catch (Exception e) {
			Log.w(TAG, "could not access locales", e);
		}
		aContext.setDeviceLocales(locales);

		aContext.setLocale(Locale.getDefault());
		aContext.setTimeZone(getPrivacyString(r, "TimeZone.getDefault.getID", TimeZone.getDefault().getID()));


		List<String> glExtensions = new ArrayList<String>();
		try {
			addEglExtensions(glExtensions);
		} catch (Exception e) {
			Log.w(TAG, "could not egl driver", e);
		}
		aContext.setGlExtensions(glExtensions);

		aContext.setBuildOtaInstalled(false).setRoaming("mobile-notroaming");
		aContext.setUserNumber(0); // TODO

		return aContext;
	}

	public static long getAndroidId(ContentResolver r) {
		String s = Gservices
				.getString(r, Settings.Secure.ANDROID_ID, Settings.Secure.getString(r, Settings.Secure.ANDROID_ID));
		long aid = 0;
		if ((s != null) && (s.length() == 16)) {
			try {
				aid = Long.parseLong(s, 16);
			} catch (NumberFormatException ignored) {
			}
		}
		if (aid == 0) {
			try {
				aid = Long.parseLong(s);
			} catch (NumberFormatException ignored) {
			}
		}
		return aid;
	}

	public static int getPrivacyInt(ContentResolver r, String name, int value) {
		return (int) Gservices.getLong(r, "pext_" + name, value);
	}

	public static long getPrivacyLong(ContentResolver r, String name, long value) {
		return Gservices.getLong(r, "pext_" + name, value);
	}

	public static String getPrivacyString(ContentResolver r, String name, String value) {
		return Gservices.getString(r, "pext_" + name, value);
	}

	@SuppressLint("NewApi")
	private static String getRadioVersion() {
		if (Build.VERSION.SDK_INT >= 14) {
			return Build.getRadioVersion();
		} else {
			return null;
		}
	}
}
