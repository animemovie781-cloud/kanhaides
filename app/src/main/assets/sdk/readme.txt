To make the KanhaIDE build engine work natively on Android, you must place the compiled ARM64 binaries of the Android Build Tools inside this folder.

When the app launches, it will automatically extract these files into its internal storage (`filesDir/sdk`) and make the binaries executable.

Required Files:
1. `android.jar` - Standard Android platform SDK jar for API 34.
2. `aapt2` - Native ARM64 binary for AAPT2.
3. `d8.jar` or `d8` script - The D8 dexer.
4. `zipalign` - Native ARM64 binary.
5. `apksigner` - Native ARM64 binary or executable jar script.

Where to get them?
Since Google only officially distributes x86/64 binaries for Linux/Windows/Mac, you have to use community-compiled AArch64 (ARM64) binaries. 
You can extract these from apps like Termux (using `pkg install aapt aapt2 apksigner dx`) or other open-source Android IDE projects (like AIDE or Sketchware communities).

Folder Structure expected by SdkInstaller:
assets/sdk/
├── android.jar
├── aapt2
├── d8 (or d8.jar)
├── zipalign
└── apksigner
