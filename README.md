# Dear Diary

A modern mental health companion combining a mobile journal with a REST API backend.

## Overview

Dear Diary provides daily journaling, mood tracking and AI-assisted conversations. It consists of an Android client built with Jetpack Compose and a FastAPI backend.

## Features

- Personal journal with sentiment analysis
- Chat interface using AI models
- Mood calendar and statistics
- Voice journal and media library
- Automatic database migrations on API startup

## Tech Stack

- **Android**: Kotlin, Jetpack Compose, Room, Retrofit, Hilt
- **Backend**: FastAPI, SQLAlchemy, Celery, Redis, Transformers

## Setup

1. Clone this repository.
2. Copy `.env.example` to `.env` in the project root and adjust values:
   - `DATABASE_URL` – database connection URL
   - `SECRET_KEY` – JWT signing key
   - `AI_API_KEY` and related `AI_API_URL`, `AI_MODEL`, `AI_PLANNER_MODEL`, `AI_GENERATOR_MODEL`
   - `CELERY_BROKER_URL` and `CELERY_RESULT_BACKEND` – Redis connection
   - `LOG_LEVEL` – optional logging level
3. For custom API endpoints, update `BASE_URL` in `app/build.gradle.kts` inside each `buildTypes` block.

## Build & Run

### Android

Ensure JDK 11 or higher is installed.

```bash
./gradlew assembleDebug    # build APK
./gradlew installDebug     # install to connected device
```

### Backend

1. Create a virtual environment and install dependencies:

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r backend/requirements.txt
```

2. Start the services (migrations run automatically on API startup):

```bash
uvicorn backend.main:app --reload
celery -A backend.app.celery_app.celery_app worker --loglevel=info
```

## Testing

- **Backend**: `pytest`
- **Android**: `./gradlew test`

## Contributing

Pull requests are welcome. Please open an issue first to discuss major changes. Make sure all tests pass before submitting.

## Conversation toolbox

The backend includes a configurable "toolbox" of communication techniques used by the
AI assistant. The default configuration is stored in `backend/app/planner_config.yaml`.
It defines a `toolbox` section mapping technique names to short instruction strings
and a `synonyms` section for alternative names. `conversation_planner` loads this file
at startup (or the path specified via the `PLANNER_CONFIG_FILE` setting) and exposes
the resulting `TOOLBOX` dictionary for the response generator. To add or modify
techniques, edit this YAML file and extend the `CommunicationTechnique` enum.
If `PLANNER_CONFIG_FILE` points to a missing or malformed file, the planner
logs a warning and falls back to empty dictionaries.
