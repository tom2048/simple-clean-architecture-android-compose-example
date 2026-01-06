configure<JacocoPluginExtension> {
    toolVersion = "0.8.12"
}
tasks.withType<Test>().configureEach {
    extensions.configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}
tasks.register<JacocoReport>(name = "testDebugUnitTestCoverageReport") {
    dependsOn(tasks.named("testDebugUnitTest"))
    reports {
        html.required.set(true)
        xml.required.set(true)
    }

    val includes = listOf(
        "**/ViewModel.*",
        "**/UseCase.*",
        "**/UseCaseImpl.*",
        "**/UiState.*",
        "**/UiEffect.*",
        "**/UiEvent.*"
    )
    val excludes = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Activity.*",
        "**/*Activity$*..*",
        "**/*Fragment$*..*",
        "**/*ScreenKt.*",
        "**/*Kt.*",
        "**/*\$lambda*.*",
        "**/*\$1*.*",
        "**/*\$2*.*",
        "**/*\$3*.*",
        "**/*\$4*.*",
        "**/*\$5*.*",
    )
    classDirectories.setFrom(files(
        fileTree("${buildDir}/tmp/kotlin-classes/debug") {
            exclude(excludes)
        },
        fileTree("${buildDir}/intermediates/javac/debug") {
            exclude(excludes)
        }
    ))
    sourceDirectories.setFrom(files("src/main/java", "scr/main/kotlin"))
    executionData.setFrom(fileTree(buildDir) {
        include("jacoco/testDebugUnitTest.exec")
    })
    doLast {
        logger.lifecycle("\nJacoco unit test coverage report generate: xml file:///${reports.xml.outputLocation.get().asFile.absolutePath.replace("\\", "/")}")
        logger.lifecycle("\nJacoco unit test coverage report generate: html file:///${reports.html.outputLocation.get().asFile.absolutePath.replace("\\", "/")}/index.html")
    }
}
