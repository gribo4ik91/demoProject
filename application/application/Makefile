APP_DIR = .
SQL_DIR = ../../sql

start-app:
	@echo "Starting Spring Boot against a locally running PostgreSQL instance..."
	cd $(APP_DIR) && ./gradlew bootRun

start-db-docker:
	@echo "Starting PostgreSQL in Docker (optional helper for local development)..."
	cd $(SQL_DIR) && docker compose up -d

stop-db-docker:
	@echo "Stopping the optional Docker PostgreSQL instance..."
	cd $(SQL_DIR) && docker compose down

test-api:
	@echo "Running API tests..."
	cd $(APP_DIR) && ./gradlew test
