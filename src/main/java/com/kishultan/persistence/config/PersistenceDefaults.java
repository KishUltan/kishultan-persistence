package com.kishultan.persistence.config;

import java.util.Locale;

/**
 * 默认配置类
 * 用于存储默认的数据源名称和其他配置
 */
public class PersistenceDefaults {
    private static String dDataSource = "default";
    private static String dDigestAlgorithm = null; //"MD5";
    private static Locale dLocale = new Locale("zh", "CN");

    /**
     * Returns string representing the default datasource's name.
     *
     * @return Datasource name.
     */
    public static String getDataSourceName() {
        return dDataSource;
    }

    /**
     * Set the default datasource name.
     *
     * @param newDS The new datasource name to be set.
     */
    public static void setDataSourceName(String newDS) {
        dDataSource = newDS;
    }

    /**
     * Returns the default encryption algorithm.
     * <code>null</code> if the encryption is not used.
     *
     * @return The name of the algorithm.
     */
    public static String getEncryptionAlgorithm() {
        return dDigestAlgorithm;
    }

    /**
     * Sets the default encryption algorithm.
     * Set to <code>null</code> if encryption is not used.
     *
     * @param algorithm The algorithm name.
     */
    public static void setEncryptionAlgorithm(String algorithm) {
        dDigestAlgorithm = algorithm;
    }

    /**
     * Returns the default locale.
     *
     * @return The default locale.
     */
    public static Locale getLocale() {
        return dLocale;
    }

    /**
     * Set the default locale.
     *
     * @param locale The new locale to be set.
     */
    public static void setLocale(Locale locale) {
        dLocale = locale;
    }
}
