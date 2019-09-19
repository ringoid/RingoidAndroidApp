package com.ringoid.config

/**
 * Marks method is used to provide migration from application's version to the newer one.
 * When all users migrate from that version, so there will be no devices with that or older
 * version installed, then these methods marked with such annotation could be removed forever.
 *
 * @note: This annotation involves dependency from :config module for modules.
 *        Once all annotated methods removed, such dependency could be removed as well.
 *
 * [version] represents build code number.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class AppMigrationFrom(val version: Int)
