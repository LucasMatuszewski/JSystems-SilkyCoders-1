# Sinsay Returns & Complaints Verification System

AI-driven verification system for automating returns (Zwrot) and complaints (Reklamacja) using local Ollama with Gemma3 model.

## Architecture

This is a **monorepo** with an embedded deployment model:
- **Backend**: Spring Boot 3.5.9 (Java 21) with Spring AI
- **Frontend**: React 19 with TypeScript, TailwindCSS, and Shadcn UI
- **Deployment**: Single JAR file containing both frontend and backend

## Technology Stack

### Backend
- **Java 21** with Virtual Threads
- **Spring Boot 3.5.9**
- **Spring AI 1.0.1** (Ollama integration with Gemma3)
- **Maven** for build and dependency management

### Frontend
- **React 19** with TypeScript
- **Vite** for build tooling
- **TailwindCSS** for styling
- **Shadcn UI** for component library
- **Vercel AI SDK** for streaming chat interface
- **React Hook Form** + **Zod** for form validation

## Project Structure

```
.
├── frontend/              # React 19 application
│   ├── src/
│   │   ├── components/    # React components
│   │   ├── lib/          # Utilities (Shadcn UI)
│   │   └── ...
│   ├── package.json
│   └── vite.config.ts
├── src/
│   └── main/
│       ├── java/          # Spring Boot application
│       └── resources/
│           └── static/   # Frontend build output (generated)
├── pom.xml               # Maven configuration
└── README.md
```

## Prerequisites

- **Java 21** (JDK)
- **Node.js 20+** and **npm**
- **Maven 3.8+** (or use `./mvnw` wrapper)
- **Ollama** installed locally and running
- **gemma3** model pulled in Ollama (`ollama pull gemma3`)

## Setup Instructions

### 1. Clone and Install Dependencies

```bash
# Install frontend dependencies
cd frontend
npm install

# Return to root
cd ..
```

### 2. Configure Ollama

Ensure Ollama is running on your machine. The default configuration assumes it's available at `http://localhost:11434`.

Pull the `gemma3` model:
```bash
ollama pull gemma3
```

The application is configured to use Ollama in `src/main/resources/application.yml`:

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: gemma3
```

### 3. Development Mode

#### Option A: Run Separately (Recommended for Development)

**Terminal 1 - Backend:**
```bash
./mvnw spring-boot:run
```
Backend runs on `http://localhost:8080`

**Terminal 2 - Frontend:**
```bash
cd frontend
npm run dev
```
Frontend runs on `http://localhost:5173` with proxy to backend `/api` endpoints

#### Option B: Full Build (Production-like)

```bash
# Build frontend and package into JAR
./mvnw clean install

# Run the JAR
java -jar target/JSystems-SilkyCodders-1-0.0.1-SNAPSHOT.jar
```

Access the application at `http://localhost:8080`

## Build Process

The Maven build process:

1. **Frontend Build**: `frontend-maven-plugin` runs `npm install` and `npm run build`
2. **Copy Assets**: Built files from `frontend/dist` are copied to `src/main/resources/static`
3. **Package JAR**: Spring Boot Maven plugin packages everything into a single JAR

## Key Features

### Backend
- **Spring AI Integration**: Ollama Gemma3 for image analysis
- **SSE Streaming**: Server-Sent Events for real-time AI responses
- **Vercel AI SDK Protocol**: Compatible streaming format
- **Virtual Threads**: Efficient I/O-bound AI operations

### Frontend
- **Intake Form**: 5-field form for returns/complaints
- **Streaming Chat**: Real-time AI responses using Vercel AI SDK
- **Image Upload**: Client-side resizing (max 1024px) before upload
- **Form Validation**: Zod schemas with React Hook Form

## Development Guidelines

See `docs/my_docs/AGENTS.md` for detailed architectural constraints and development guidelines.

### Key Constraints:
- **Embedded Deployment**: Frontend must be built into `src/main/resources/static`
- **Streaming Protocol**: Must use Vercel AI SDK Data Stream Protocol (`0:"text"`, `8:[data]`, `e:{error}`)
- **State Management**: Use `useActionState` (React 19) or `react-hook-form` for forms
- **No Redux**: Do not introduce Redux
- **Image Handling**: Resize images to max 1024px before upload

## API Endpoints

- `GET /` - Serves React frontend
- `POST /api/intake` - Submit return/complaint form
- `POST /api/chat` - Streaming chat endpoint (SSE)

## Configuration

### Backend Configuration (`application.yml`)

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: gemma3
          temperature: 0.7
server:
  port: 8080
```

### Frontend Configuration (`vite.config.ts`)

- Development proxy: `/api` → `http://localhost:8080`
- Path aliases: `@/` → `src/`

## Testing

```bash
# Backend tests
./mvnw test

# Frontend tests (when configured)
cd frontend
npm test
```

## Troubleshooting

### Frontend build fails
- Ensure Node.js 20+ is installed
- Run `cd frontend && npm install` manually
- Check `frontend/package.json` for correct dependencies

### Backend can't find static files
- Ensure Maven build completed successfully
- Check that `frontend/dist` exists after `npm run build`
- Verify `src/main/resources/static` contains built files

### Ollama connection errors
- Verify Ollama is running: `ollama serve` or check `http://localhost:11434`
- Ensure `gemma3` model is pulled: `ollama pull gemma3`
- Check `spring.ai.ollama.base-url` in `application.yml` matches your Ollama instance
- For distributed setup, update `base-url` to point to your Ollama server

## License

[Your License Here]
