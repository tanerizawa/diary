import os
from celery import Celery

broker_url = os.getenv("CELERY_BROKER_URL", "redis://localhost:6379/0")
result_backend = os.getenv("CELERY_RESULT_BACKEND", broker_url)

celery_app = Celery(
    "dear_diary",
    broker=broker_url,
    backend=result_backend,
)

celery_app.conf.task_serializer = "json"
celery_app.conf.result_serializer = "json"
celery_app.conf.accept_content = ["json"]
celery_app.conf.timezone = "UTC"

# Automatically discover task modules within the ``app`` package so that
# running the worker with ``-A backend.app.celery_app.celery_app`` will
# properly register tasks like ``app.tasks.process_chat_sentiment``.
celery_app.autodiscover_tasks(["app"])
