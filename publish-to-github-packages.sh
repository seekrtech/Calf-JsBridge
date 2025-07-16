#!/bin/bash

# Script to publish Calf WebView to GitHub Packages (Private Repository Only)
# Usage: ./publish-to-github-packages.sh [version]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_info "🔒 Publishing Calf WebView to GitHub Packages (Private Repository)"

# Check if GitHub CLI is installed and user is authenticated
if ! command -v gh &> /dev/null; then
    print_error "GitHub CLI (gh) is not installed. Please install it first:"
    echo "  brew install gh  # On macOS"
    echo "  # or visit https://cli.github.com/"
    exit 1
fi

# Check if user is authenticated with GitHub CLI
if ! gh auth status &> /dev/null; then
    print_error "Not authenticated with GitHub CLI. Please run:"
    echo "  gh auth login"
    exit 1
fi

# Get GitHub username and repository info
GITHUB_USER=$(gh api user --jq .login)
GITHUB_REPO=$(basename "$(git config --get remote.origin.url)" .git)
GITHUB_REPOSITORY="$GITHUB_USER/$GITHUB_REPO"

print_info "Publishing to private repository: $GITHUB_REPOSITORY"

# Set version
VERSION=${1:-"0.6.1-jsbridge"}
print_info "Version: $VERSION"
print_info "📦 Publishing only: calf-webview"

# Get GitHub token
GITHUB_TOKEN=$(gh auth token)

# Set environment variables
export GITHUB_TOKEN="$GITHUB_TOKEN"
export GITHUB_ACTOR="$GITHUB_USER"
export GITHUB_REPOSITORY="$GITHUB_REPOSITORY"
export VERSION="$VERSION"

print_info "Starting publication to GitHub Packages (Private)..."

# Publish to GitHub Packages
if ./gradlew publishAllPublicationsToGitHubPackagesRepository; then
    print_info "✅ Successfully published calf-webview to GitHub Packages!"
    print_info "📦 Your private package is now available at:"
    echo "  https://github.com/$GITHUB_REPOSITORY/packages"
    print_info ""
    print_info "🔧 To use calf-webview in your projects, add this to your build.gradle.kts:"
    echo "  repositories {"
    echo "      maven {"
    echo "          url = uri(\"https://maven.pkg.github.com/$GITHUB_REPOSITORY\")"
    echo "          credentials {"
    echo "              username = \"$GITHUB_USER\""
    echo "              password = \"<your-github-token-with-read:packages-scope>\""
    echo "          }"
    echo "      }"
    echo "  }"
    echo ""
    echo "  dependencies {"
    echo "      implementation(\"com.mohamedrejeb.calf:calf-webview:$VERSION\")"
    echo "  }"
    print_info ""
    print_info "🔐 Remember: This package is private and requires GitHub authentication to access"
else
    print_error "❌ Publication failed!"
    exit 1
fi 