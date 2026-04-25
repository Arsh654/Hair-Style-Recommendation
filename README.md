# Hair-Style-Recommendation

Spring Boot app for AI-powered face-shape, hairstyle, and skin analysis from a single uploaded photo.

## What It Does

- Detects the face from an uploaded image and classifies face shape
- Recommends hairstyles with sample preview images
- Runs Groq-based skin analysis for 9 concerns:
  - Acne
  - Pigmentation
  - Uniformness
  - Pores
  - Dark Circles
  - Redness
  - Skin Tone
  - Lines
  - Hydration
- Enriches each skin concern with:
  - severity score and label
  - severity color coding
  - remediation steps
  - curated product suggestions
- Shows separate `Hair` and `Skin` tabs in the UI
- Supports re-uploading a new image directly from the page

## Tech Stack

- Java 17
- Spring Boot 3
- OpenCV for face detection
- Groq for AI vision-based skin scoring
- Plain HTML/CSS/JavaScript frontend served by Spring Boot

## Run

Set your Groq key first:

```bash
export GROQ_API_KEY=your_key_here
```

Optional overrides:

```bash
export GROQ_SKIN_MODEL=meta-llama/llama-4-scout-17b-16e-instruct
export GROQ_RESPONSES_URL=https://api.groq.com/openai/v1/chat/completions
```

Start the app:

```bash
./mvnw spring-boot:run
```

The application runs on `http://localhost:3501`.

## Test

Run the full test suite:

```bash
./mvnw test
```

## Notes

- Skin analysis is Groq-only. OpenAI integration has been removed.
- If `GROQ_SKIN_MODEL` is set to a non-vision Groq model, the app falls back to a vision-capable model automatically.
- If Groq is unavailable, hairstyle analysis still works and the UI shows the skin-analysis failure state separately.
