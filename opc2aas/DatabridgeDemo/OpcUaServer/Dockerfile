FROM python:3.9

# Install dependencies
RUN pip install opcua

# Copy server script to the container
COPY server.py /app/

# Set the working directory
WORKDIR /app

# Run the server script
CMD ["python", "server.py"]