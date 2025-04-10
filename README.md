# Alumni Association Platform

A comprehensive platform connecting alumni with their alma mater, featuring professional networking, job portal, event management, and more.

## Project Structure
```
alumni-association/
├── backend/           # Node.js backend server
└── android/           # Android mobile application
```

## Backend Setup (Node.js)

### Prerequisites
- Node.js (v14 or higher)
- MongoDB
- npm (Node Package Manager)

### Installation & Setup

1. Navigate to backend directory:
```bash
cd backend
```

2. Install dependencies:
```bash
npm install
```

3. Create .env file in the backend directory with following variables:
```env
PORT=3000
MONGODB_URI=mongodb://localhost:27017/alumni_db
JWT_SECRET=your_jwt_secret_key
```

4. Start the server:
```bash
npm start
```

The server will start running at `http://localhost:3000`

### API Documentation

#### Authentication Endpoints

```
POST /api/auth/register
- Register new user (student/alumni)
Request body:
{
    "email": "user@example.com",
    "password": "password123",
    "name": "John Doe",
    "role": "alumni",
    "graduationYear": "2020",
    "department": "Computer Science"
}

POST /api/auth/login
- Login user
Request body:
{
    "email": "user@example.com",
    "password": "password123"
}
```

#### User Endpoints

```
GET /api/users
- Get all users
Headers: Authorization: Bearer {token}

GET /api/users/:id
- Get user by ID
Headers: Authorization: Bearer {token}

PUT /api/users/:id
- Update user profile
Headers: Authorization: Bearer {token}
```

#### Jobs Endpoints

```
GET /api/jobs
- Get all job postings
Headers: Authorization: Bearer {token}

POST /api/jobs
- Create new job posting
Headers: Authorization: Bearer {token}
Request body:
{
    "title": "Software Engineer",
    "company": "Tech Corp",
    "description": "Job description...",
    "requirements": ["React", "Node.js"],
    "location": "New York"
}
```

#### Events Endpoints

```
GET /api/events
- Get all events
Headers: Authorization: Bearer {token}

POST /api/events
- Create new event
Headers: Authorization: Bearer {token}
Request body:
{
    "title": "Alumni Meet 2024",
    "description": "Annual alumni gathering",
    "date": "2024-03-15T18:00:00Z",
    "location": "University Campus"
}
```

#### Donations Endpoints

```
POST /api/donations
- Make a donation
Headers: Authorization: Bearer {token}
Request body:
{
    "amount": 1000,
    "purpose": "Student Scholarship"
}

GET /api/donations/history
- Get donation history
Headers: Authorization: Bearer {token}
```

## Android App Setup

### Prerequisites
- Android Studio (latest version)
- JDK 11 or higher
- Android SDK

### Configuration

1. Open the project in Android Studio:
   - File -> Open -> Select the `android` directory

2. Update the API base URL:
   - Open `app/src/main/java/com/example/alumni/networking/ApiService.kt`
   - Update the BASE_URL constant with your backend server URL

3. Build and run the app:
   - Select your device/emulator
   - Click Run 'app' or press Shift + F10

### App Features

1. Authentication
   - Login
   - Registration
   - Password reset

2. Student Features
   - View college events
   - Discussion forum
   - Chat with alumni
   - Submit feedback

3. Alumni Features
   - Post job openings
   - View alumni directory
   - Make donations
   - Share success stories

## API Integration in Android App

### Setting up Retrofit

```kotlin
// ApiService.kt
interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @GET("users")
    suspend fun getUsers(@Header("Authorization") token: String): Response<List<User>>

    @POST("jobs")
    suspend fun createJob(
        @Header("Authorization") token: String,
        @Body jobRequest: JobRequest
    ): Response<Job>
}

// Initialize Retrofit
val retrofit = Retrofit.Builder()
    .baseUrl("http://your-backend-url:3000/api/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val apiService = retrofit.create(ApiService::class.java)
```

### Making API Calls

```kotlin
// Example of making an API call in a ViewModel
class LoginViewModel : ViewModel() {
    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = apiService.login(LoginRequest(email, password))
                if (response.isSuccessful) {
                    _loginResult.value = Result.success(response.body()!!)
                } else {
                    _loginResult.value = Result.failure(Exception("Login failed"))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            }
        }
    }
}
```

## Error Handling

The app implements comprehensive error handling:

1. Network Errors
   - Connection timeout
   - No internet connection
   - Server errors

2. Validation Errors
   - Input validation
   - Form validation
   - API response validation

3. Authentication Errors
   - Invalid credentials
   - Token expiration
   - Unauthorized access

## Security Considerations

1. API Security
   - JWT token authentication
   - HTTPS encryption
   - Input sanitization

2. Data Security
   - Secure storage of sensitive data
   - Encryption of user credentials
   - Session management

## Testing

1. Backend Testing
```bash
cd backend
npm test
```

2. Android Testing
   - Run unit tests: `./gradlew test`
   - Run instrumented tests: `./gradlew connectedAndroidTest`

## Troubleshooting

Common issues and solutions:

1. Cannot connect to backend
   - Verify backend server is running
   - Check API base URL configuration
   - Ensure correct network permissions

2. Authentication issues
   - Verify correct credentials
   - Check token expiration
   - Clear app data and retry

3. Database connection issues
   - Verify MongoDB is running
   - Check database connection string
   - Ensure correct database credentials

## Support

For any issues or questions, please create an issue in the repository or contact the development team.
