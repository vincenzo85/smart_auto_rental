FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY orchestrator_api.py ./

EXPOSE 8091

CMD ["uvicorn", "orchestrator_api:app", "--host", "0.0.0.0", "--port", "8091"]
