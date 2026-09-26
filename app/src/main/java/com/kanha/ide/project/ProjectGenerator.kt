package com.kanha.ide.project

import android.content.Context
import com.kanha.ide.template.Template
import com.kanha.ide.template.VariableResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStreamReader

class ProjectGenerator(private val context: Context) {

    suspend fun generate(template: Template, config: ProjectConfig): Result<File> = withContext(Dispatchers.IO) {
        try {
            val projectDir = File(config.projectPath)
            if (projectDir.exists() && (projectDir.listFiles()?.isNotEmpty() == true)) {
                return@withContext Result.failure(Exception("Project directory already exists and is not empty"))
            }

            if (!projectDir.exists()) {
                projectDir.mkdirs()
            }

            // Create standard Android Studio project structure manually
            createProjectStructure(projectDir, config)
            
            Result.success(projectDir)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createProjectStructure(destDir: File, config: ProjectConfig) {
        val appDir = File(destDir, "app")
        val srcDir = File(appDir, "src/main")
        val javaDir = File(srcDir, "java/${config.packageName.replace('.', '/')}")
        val resDir = File(srcDir, "res")
        val layoutDir = File(resDir, "layout")
        val valuesDir = File(resDir, "values")
        
        javaDir.mkdirs()
        layoutDir.mkdirs()
        valuesDir.mkdirs()

        // 1. Project level build.gradle.kts
        File(destDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("com.android.application") version "8.2.0" apply false
                id("org.jetbrains.kotlin.android") version "1.9.0" apply false
            }
            """.trimIndent()
        )

        // 2. settings.gradle.kts
        File(destDir, "settings.gradle.kts").writeText(
            """
            pluginManagement {
                repositories {
                    google()
                    mavenCentral()
                    gradlePluginPortal()
                }
            }
            dependencyResolutionManagement {
                repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                repositories {
                    google()
                    mavenCentral()
                }
            }
            rootProject.name = "${config.projectName}"
            include(":app")
            """.trimIndent()
        )

        // 3. gradle.properties
        File(destDir, "gradle.properties").writeText(
            """
            org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
            android.useAndroidX=true
            android.nonTransitiveRClass=true
            """.trimIndent()
        )

        // 4. app/build.gradle.kts
        File(appDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("com.android.application")
                ${if (config.language == "kotlin") "id(\"org.jetbrains.kotlin.android\")" else ""}
            }
            
            android {
                namespace = "${config.packageName}"
                compileSdk = ${config.compileSdk}
            
                defaultConfig {
                    applicationId = "${config.packageName}"
                    minSdk = ${config.minSdk}
                    targetSdk = ${config.targetSdk}
                    versionCode = ${config.versionCode}
                    versionName = "${config.versionName}"
                }
            }
            
            dependencies {
                implementation("androidx.core:core-ktx:1.12.0")
                implementation("androidx.appcompat:appcompat:1.6.1")
                implementation("com.google.android.material:material:1.11.0")
                implementation("androidx.constraintlayout:constraintlayout:2.1.4")
            }
            """.trimIndent()
        )

        // 5. AndroidManifest.xml
        File(srcDir, "AndroidManifest.xml").writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                <application
                    android:allowBackup="true"
                    android:icon="@mipmap/ic_launcher"
                    android:label="@string/app_name"
                    android:roundIcon="@mipmap/ic_launcher_round"
                    android:supportsRtl="true"
                    android:theme="@style/Theme.${config.projectName.replace(" ", "")}">
                    <activity
                        android:name=".MainActivity"
                        android:exported="true">
                        <intent-filter>
                            <action android:name="android.intent.action.MAIN" />
                            <category android:name="android.intent.category.LAUNCHER" />
                        </intent-filter>
                    </activity>
                </application>
            </manifest>
            """.trimIndent()
        )

        // 6. strings.xml
        File(valuesDir, "strings.xml").writeText(
            """
            <resources>
                <string name="app_name">${config.projectName}</string>
            </resources>
            """.trimIndent()
        )
        
        // 7. themes.xml
        File(valuesDir, "themes.xml").writeText(
            """
            <resources xmlns:tools="http://schemas.android.com/tools">
                <style name="Theme.${config.projectName.replace(" ", "")}" parent="Theme.MaterialComponents.DayNight.DarkActionBar">
                    <item name="colorPrimary">@color/purple_500</item>
                    <item name="colorPrimaryDark">@color/purple_700</item>
                    <item name="colorAccent">@color/teal_200</item>
                </style>
            </resources>
            """.trimIndent()
        )
        
        // 8. colors.xml
        File(valuesDir, "colors.xml").writeText(
            """
            <resources>
                <color name="purple_200">#FFBB86FC</color>
                <color name="purple_500">#FF6200EE</color>
                <color name="purple_700">#FF3700B3</color>
                <color name="teal_200">#FF03DAC5</color>
                <color name="teal_700">#FF018786</color>
                <color name="black">#FF000000</color>
                <color name="white">#FFFFFFFF</color>
            </resources>
            """.trimIndent()
        )

        // 9. activity_main.xml
        File(layoutDir, "activity_main.xml").writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <androidx.constraintlayout.widget.ConstraintLayout 
                xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:app="http://schemas.android.com/apk/res-auto"
                xmlns:tools="http://schemas.android.com/tools"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                tools:context=".MainActivity">
            
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Hello KanhaIDE!"
                    app:layout_constraintBottom_toBottomOf="parent"
                    app:layout_constraintEnd_toEndOf="parent"
                    app:layout_constraintStart_toStartOf="parent"
                    app:layout_constraintTop_toTopOf="parent" />
            
            </androidx.constraintlayout.widget.ConstraintLayout>
            """.trimIndent()
        )

        // 10. MainActivity
        if (config.language == "kotlin") {
            File(javaDir, "MainActivity.kt").writeText(
                """
                package ${config.packageName}
                
                import androidx.appcompat.app.AppCompatActivity
                import android.os.Bundle
                
                class MainActivity : AppCompatActivity() {
                    override fun onCreate(savedInstanceState: Bundle?) {
                        super.onCreate(savedInstanceState)
                        setContentView(R.layout.activity_main)
                    }
                }
                """.trimIndent()
            )
        } else {
            File(javaDir, "MainActivity.java").writeText(
                """
                package ${config.packageName};
                
                import androidx.appcompat.app.AppCompatActivity;
                import android.os.Bundle;
                
                public class MainActivity extends AppCompatActivity {
                    @Override
                    protected void onCreate(Bundle savedInstanceState) {
                        super.onCreate(savedInstanceState);
                        setContentView(R.layout.activity_main);
                    }
                }
                """.trimIndent()
            )
        }
    }
}
