import nl.littlerobots.vcu.plugin.resolver.VersionSelectors

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.version.catalog.update)
}

versionCatalogUpdate {
    sortByKey = true

    versionSelector(VersionSelectors.PREFER_STABLE)
}