# AI Resume Analyzer

A production-level Java Full Stack application that allows users to upload their resumes in PDF format, get it analyzed by a simulated AI engine, receive a compatibility score based on modern tech stacks, and interact with an AI Chat Assistant for feedback.

## Features

- **User Authentication**: Secure JWT-based Login/Registration.
- **Resume Upload**: Handles PDF uploads with size restrictions (10MB).
- **Text Analysis**: Extracts text via Apache PDFBox.
- **Skill Engine**: Matches skills against predefined categories (Backend, Frontend, Database, Cloud, Soft Skills).
- **Scoring**: Computes overall and category-level match percentages.
- **Smart Feedback**: Generates dynamic improvement suggestions based on weak areas.
- **AI Chat Assistant**: Context-aware floating chat widget that knows about the user's latest analysis.
- **Premium UI**: Glassmorphism, CSS gradients, dynamic chart animations.

## Tech Stack

- **Backend**: Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA, JWT
- **Frontend**: Vanilla JS, HTML5, CSS3, FontAwesome
- **Database**: PostgreSQL
- **Libraries**: Lombok, Apache PDFBox, SpringDoc OpenAPI (Swagger)

## API Reference (Swagger)

After running the application, view full API documentation here:
`http://localhost:8080/swagger-ui.html`

### Endpoints (v1)
- `POST /api/v1/users/register`
- `POST /api/v1/users/login`
- `POST /api/v1/resumes/upload`
- `GET /api/v1/resumes/{id}/analysis`
- `POST /api/v1/chat`

## Setup and Running

1. **Database Setup**
   Ensure PostgreSQL is running locally on port 5432.
   Database config is set in `application.properties`:
   - URL: `jdbc:postgresql://localhost:5432/resume_analyzer`
   - User: `postgres`
   - Password: `123`
   
   *Create the database `resume_analyzer` manually if your PostgreSQL instance doesn't auto-create it.*

2. **Build and Run**
   Navigate to the project directory:
   ```bash
   mvn clean install -DskipTests
   mvn spring-boot:run
   ```

3. **Access Application**
   Open your browser and navigate to:
   `http://localhost:8080/`

## Project Structure

- `com.resumeanalyzer.config` - Security, CORS, Swagger configs.
- `com.resumeanalyzer.controller` - REST Endpoints.
- `com.resumeanalyzer.dto` - Request/Response objects.
- `com.resumeanalyzer.entity` - JPA Entities & attribute converters.
- `com.resumeanalyzer.exception` - Global exception handling.
- `com.resumeanalyzer.repository` - Spring Data JPA Repositories.
- `com.resumeanalyzer.security` - JWT Filters and UserDetails components.
- `com.resumeanalyzer.service` - Business logic (Analysis, Skill Map, Chat logic).
- `src/main/resources/static` - Frontend files (HTML/CSS/JS).
