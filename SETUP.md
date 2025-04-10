# Step-by-Step Setup Guide

This guide provides detailed instructions for setting up both the backend server and Android application.

## Backend Setup

### Prerequisites
- Node.js (v14 or higher)
- MongoDB (v4.4 or higher)
- Git

### Step 1: Clone and Setup Backend

1. Clone the repository:
```bash
git clone <repository-url>
cd alumni-network
```

2. Install backend dependencies:
```bash
cd backend
npm install
```

3. Create environment configuration:
```bash
# Create .env file
touch .env

# Add the following content to .env
PORT=3000
MONGODB_URI=mongodb://localhost:27017/alumni_db
JWT_SECRET=your_secure_jwt_secret
```

4. Initialize the database:
```bash
# Start MongoDB service
sudo service mongodb start   # Linux
brew services start mongodb # macOS

# Verify MongoDB is running
mongo --eval "db.serverStatus()"
```

5. Start the development server:
```bash
npm run dev
```

6. Verify the server is running:
- Open your browser and navigate to `http://localhost:3000/api/health`
- You should see a "Server is healthy" message

### Step 2: Test Backend API

1. Test the authentication endpoints:
```bash
# Register a test user
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","name":"Test User"}'

# Login with the test user
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

2. Save the JWT token from the login response for testing other endpoints.

## Android Setup

### Prerequisites
- Android Studio (Latest Version)
- JDK 11 or higher
- Android SDK Platform 33 (Android 13.0) or higher
- Android Virtual Device or physical Android device

### Step 1: Android Studio Setup

1. Open Android Studio

2. Install required SDK components:
   - Tools → SDK Manager
   - Select "SDK Platforms" tab
   - Check "Android 13.0 (Tiramisu)" or higher
   - Select "SDK Tools" tab
   - Check "Android SDK Build-Tools"
   - Click "Apply" to install

3. Create/Configure Android Virtual Device:
   - Tools → Device Manager
   - Click "Create Device"
   - Select "Pixel 2" or any preferred device
   - Select Android 13.0 system image
   - Complete the AVD creation

### Step 2: Import Project

1. Open the project:
   - Click "Open" in Android Studio
   - Navigate to the cloned repository
   - Select the `android` directory
   - Click "OK"

2. Wait for project sync:
   - Gradle will sync and download dependencies
   - This may take several minutes
   - Resolve any dependency issues if prompted

### Step 3: Configure Backend Connection

1. Update API configuration:
   - Open `android/app/src/main/java/com/example/alumni/network/NetworkModule.kt`
   - Locate the `BASE_URL` constant
   - Update it based on your setup:
     ```kotlin
     // For Android Emulator
     const val BASE_URL = "http://10.0.2.2:3000/"
     
     // For physical device (use your computer's IP address)
     const val BASE_URL = "http://192.168.1.xxx:3000/"
     ```

2. Update network security configuration:
   - Open `android/app/src/main/res/xml/network_security_config.xml`
   - Ensure development server is allowed:
     ```xml
     <domain-config cleartextTrafficPermitted="true">
         <domain includeSubdomains="true">10.0.2.2</domain>
     </domain-config>
     ```

### Step 4: Build and Run

1. Build the project:
   - Build → Clean Project
   - Build → Rebuild Project

2. Run the application:
   - Select your device/emulator from the toolbar
   - Click the "Run" button (green play icon)
   - Wait for the app to install and launch

### Step 5: Testing the Integration

1. Test user registration:
   - Launch the app
   - Click "Register"
   - Fill in the form with test data
   - Submit and verify account creation

2. Test login:
   - Enter credentials
   - Verify successful login
   - Check if JWT token is stored properly

3. Test event features:
   - Navigate to Events tab
   - Create a new event
   - Verify it appears in the list
   - Test event registration
   - Check if changes reflect in MongoDB

## Troubleshooting

### Backend Issues

1. MongoDB Connection Errors:
```bash
# Check MongoDB status
sudo service mongodb status

# Check MongoDB logs
tail -f /var/log/mongodb/mongodb.log
```

2. Port Conflicts:
```bash
# Check if port 3000 is in use
lsof -i :3000

# Kill process if needed
kill -9 <PID>
```

### Android Issues

1. Build Errors:
   - File → Invalidate Caches / Restart
   - Delete `build` directory and rebuild
   - Sync project with Gradle files

2. Connection Errors:
   - Verify backend server is running
   - Check BASE_URL configuration
   - Ensure device has internet access
   - Check Logcat for detailed error messages

3. Emulator Issues:
   - Cold boot the emulator
   - Wipe emulator data
   - Recreate AVD if needed

## Verification Checklist

- [ ] Backend server running
- [ ] MongoDB connected
- [ ] API endpoints responding
- [ ] Android app builds successfully
- [ ] Network connection established
- [ ] User registration working
- [ ] Login successful
- [ ] Event creation functional
- [ ] Real-time updates working
- [ ] Data persistence verified

For additional support or issues, please refer to the project documentation or create an issue in the repository.
