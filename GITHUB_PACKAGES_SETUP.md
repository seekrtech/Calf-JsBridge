# GitHub Packages Publishing Setup (Private Repository - WebView Only)

This guide explains how to publish your **Calf WebView** library to GitHub Packages for your **private repository only**. No public publishing is configured.

## Overview

Your project is configured to publish **only the calf-webview module** exclusively to GitHub Packages for private distribution:
- ✅ **calf-webview** → GitHub Packages (private repository only)
- ❌ **All other modules** → Not published
- ❌ **No public publishing** (Maven Central disabled)

## Quick Start

### 1. Automatic Publishing (Recommended)

The easiest way is to use GitHub Actions:

1. **Create a release tag:**
   ```bash
   git tag v0.6.1-jsbridge
   git push origin v0.6.1-jsbridge
   ```

2. **Or trigger manual workflow:**
   - Go to your repository on GitHub
   - Navigate to Actions → "Publish to GitHub Packages"
   - Click "Run workflow"
   - Enter your desired version (e.g., `0.6.1-jsbridge`)

### 2. Manual Publishing (Local)

If you prefer to publish from your local machine:

1. **Install GitHub CLI (if not already installed):**
   ```bash
   brew install gh  # On macOS
   # or visit https://cli.github.com/ for other platforms
   ```

2. **Authenticate with GitHub:**
   ```bash
   gh auth login
   ```

3. **Run the publish script:**
   ```bash
   ./publish-to-github-packages.sh 0.6.1-jsbridge
   ```

## Configuration Details

### What Gets Published

**Only `calf-webview` module** is configured for publishing with these targets:
- Android (androidRelease)
- iOS (iosArm64, iosX64, iosSimulatorArm64)
- Kotlin Multiplatform metadata

**Not published:**
- calf-core
- calf-ui
- calf-permissions
- calf-file-picker
- calf-io
- calf-navigation
- calf-geo
- (all other modules)

### Environment Variables

The publishing system uses these environment variables:

| Variable | Description | Source |
|----------|-------------|--------|
| `GITHUB_TOKEN` | GitHub authentication token | Auto-provided by GitHub Actions or `gh auth token` |
| `GITHUB_ACTOR` | GitHub username | Auto-detected |
| `GITHUB_REPOSITORY` | Repository in format `username/repo` | Auto-detected |
| `VERSION` | Version to publish | Tag name or manual input |

### Repository Structure

Your **private calf-webview package** will be available at:
```
https://github.com/YourUsername/YourRepositoryName/packages
```

The package will be named: `com.mohamedrejeb.calf:calf-webview`

## Using Published Package

To use your **private calf-webview package** in other projects:

### 1. Add Repository to build.gradle.kts

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/YourUsername/YourRepositoryName")
        credentials {
            username = "YourGitHubUsername"
            password = "your_github_token_here"
        }
    }
}
```

### 2. Add Dependency

```kotlin
dependencies {
    implementation("com.mohamedrejeb.calf:calf-webview:0.6.1-jsbridge")
}
```

### 3. GitHub Token Setup (Required for Private Packages)

For consuming private packages, you'll need a GitHub Personal Access Token with `read:packages` permission:

1. Go to GitHub Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token with `read:packages` scope
3. Use this token in your `credentials.password`

⚠️ **Important**: Private packages require authentication even for reading!

## Troubleshooting

### Common Issues

1. **Authentication Failed**
   - Ensure you're authenticated: `gh auth status`
   - Check token permissions include `write:packages` (for publishing) or `read:packages` (for consuming)

2. **Repository Not Found**
   - Verify `GITHUB_REPOSITORY` environment variable
   - Ensure repository exists and you have access

3. **Version Already Exists**
   - GitHub Packages doesn't allow overwriting published versions
   - Use a new version number

4. **Build Failures**
   - Check that calf-webview builds successfully: `./gradlew :calf-webview:build`
   - Ensure all required dependencies are available

5. **Cannot Access Private Package**
   - Verify you have the correct GitHub token with `read:packages` scope
   - Ensure you have access to the private repository

### Manual Environment Setup

If you need to set environment variables manually:

```bash
export GITHUB_TOKEN="your_github_token"
export GITHUB_ACTOR="your_username"
export GITHUB_REPOSITORY="your_username/your_repo_name"
export VERSION="0.6.1-jsbridge"

./gradlew publishAllPublicationsToGitHubPackagesRepository
```

## Version Management

The project version is determined by:
1. `VERSION` environment variable (if set)
2. Git tag name (in GitHub Actions)
3. Default: `0.6.1-jsbridge` (as defined in `root.publication.gradle.kts`)

To change the default version, update the `version` property in:
```kotlin
// convention-plugins/src/main/kotlin/root.publication.gradle.kts
allprojects {
    group = "com.mohamedrejeb.calf"
    version = System.getenv("VERSION") ?: "0.6.1-jsbridge"  // <- Change this
}
```

## Advanced Configuration

### Publishing Only calf-webview

Since this is configured for calf-webview only:

```bash
./gradlew publishAllPublicationsToGitHubPackagesRepository
```

Or specifically for calf-webview:

```bash
./gradlew :calf-webview:publishAllPublicationsToGitHubPackagesRepository
```

### Dry Run

To test the build without actually publishing:

```bash
./gradlew :calf-webview:publishToMavenLocal
```

This publishes to your local Maven repository (`~/.m2/repository`) for testing.

## Security & Privacy Notes

- ✅ **WebView Only**: Only calf-webview module is published
- 🔒 **Authentication Required**: Package access requires GitHub authentication
- 🔑 **Token Security**: Never commit GitHub tokens to your repository
- 🏢 **Team Access**: Only users with repository access can consume packages
- 📦 **Package Visibility**: Package inherits repository visibility (private)

## Support

If you encounter issues:
1. Check the GitHub Actions logs for detailed error messages
2. Verify your authentication with `gh auth status`
3. Ensure your repository has Packages enabled in Settings
4. Check that you have the necessary permissions for the repository
5. Verify that your GitHub token has the correct scopes (`read:packages` or `write:packages`)

## Why WebView Only?

This configuration ensures:
- 🎯 **Focused Publishing**: Only the webview module you need is published
- 🔐 **Complete Privacy**: Your code and package remain private
- 🏢 **Team Control**: Only authorized team members can access the package
- 🚫 **No Accidental Publishing**: Other modules won't be accidentally published
- 🎯 **Simplified Setup**: No need for GPG signing or Maven Central credentials 