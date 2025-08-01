#!/bin/bash

# setup.sh - Prepares codespace environment for APP-android_arm64 repository
# This script installs all required tools and dependencies for building the Blender Android app

set -e  # Exit on any error

echo "🚀 Setting up environment for APP-android_arm64 (Blender Android App)"
echo "====================================================================="

# Update system packages
echo "📦 Updating system packages..."
sudo apt-get update -q

# Install essential packages
echo "🔧 Installing essential packages..."
sudo apt-get install -y \
    curl \
    wget \
    unzip \
    git \
    build-essential \
    file \
    cmake \
    ninja-build

# Install Java JDK 8 (required for Android development with this project)
echo "☕ Installing OpenJDK 8..."
sudo apt-get install -y openjdk-8-jdk
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

# Set up Android SDK directory
ANDROID_HOME="$HOME/android-sdk"
mkdir -p "$ANDROID_HOME"

echo "📱 Setting up Android SDK in $ANDROID_HOME..."

# For codespace environments, we'll set up the basic structure and environment variables
# The actual SDK components can be downloaded when needed during build

# Set up environment variables
export ANDROID_HOME="$ANDROID_HOME"
export ANDROID_NDK_VERSION="21.4.7075529"
export ANDROID_NDK_HOME="$ANDROID_HOME/ndk/$ANDROID_NDK_VERSION"

echo "🔨 Setting up project environment..."
PROJECT_DIR="/workspaces/APP-android_arm64"
if [ ! -d "$PROJECT_DIR" ]; then
    PROJECT_DIR="/home/runner/work/APP-android_arm64/APP-android_arm64"
fi

cd "$PROJECT_DIR"

# Make gradlew executable
if [ -f "OBlender/gradlew" ]; then
    chmod +x OBlender/gradlew
    echo "✅ Made gradlew executable"
fi

# Create environment setup script for persistent environment variables
ENV_FILE="$HOME/.android_env"
cat > "$ENV_FILE" << EOF
# Android environment variables for APP-android_arm64
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export ANDROID_HOME=$ANDROID_HOME
export ANDROID_NDK_VERSION=21.4.7075529
export ANDROID_NDK_HOME=\$ANDROID_HOME/ndk/\$ANDROID_NDK_VERSION
export PATH=/usr/lib/jvm/java-8-openjdk-amd64/bin:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools:\$PATH
EOF

# Add to bashrc if not already present
if ! grep -q "source $ENV_FILE" ~/.bashrc; then
    echo "source $ENV_FILE" >> ~/.bashrc
    echo "✅ Added environment variables to ~/.bashrc"
fi

# Source the environment for current session
source "$ENV_FILE"

echo ""
echo "🎉 Setup completed successfully!"
echo ""
echo "Environment Details:"
echo "==================="
echo "JAVA_HOME: $JAVA_HOME"
echo "ANDROID_HOME: $ANDROID_HOME"
echo "ANDROID_NDK_HOME: $ANDROID_NDK_HOME"
echo ""
echo "Required Components (install when needed):"
echo "- Android SDK Platform API 30"
echo "- Android Build Tools 30.0.3"
echo "- Android NDK 21.4.7075529"
echo "- CMake 3.10.2, 3.18.1, 3.22.1"
echo ""
echo "📝 To activate environment variables in current session, run:"
echo "   source ~/.android_env"
echo ""
echo "🏗️  To install Android SDK components and build the project:"
echo "   1. Download Android SDK command line tools from:"
echo "      https://developer.android.com/studio#command-tools"
echo "   2. Extract to \$ANDROID_HOME/cmdline-tools/latest/"
echo "   3. Install SDK components:"
echo "      sdkmanager \"platform-tools\" \"platforms;android-30\" \"build-tools;30.0.3\" \"ndk;21.4.7075529\""
echo "   4. Build the project:"
echo "      cd OBlender && ./gradlew assembleDebug"
echo ""
echo "⚠️  Note: This project requires additional dependencies from related repositories:"
echo "   - lib-android_arm64 (third-party libraries)"
echo "   - Utilities-android_arm64 (build utilities)"  
echo "   - Blender-android_arm64 (Blender library files)"
echo "   See Doc/Compile Flow_en.txt for complete setup instructions."