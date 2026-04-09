# --- ПЕРЕМЕННЫЕ ---
APP_DIR = ./api

# === JOB 1: Подготовка инфраструктуры (БД) ===
start-db:
	@echo "🐘 Запускаем PostgreSQL..."
	docker compose up -d
	@echo "⏳ Ждем 3 секунды для инициализации базы..."
	sleep 3

# === JOB 2: Запуск приложения ===
start-app: start-db
	@echo "🚀 Запускаем Spring Boot Backend..."
	cd $(APP_DIR) && ./gradlew bootRun

# === JOB 3: Остановка всего окружения ===
stop:
	@echo "🛑 Останавливаем Spring Boot и гасим базу..."
	docker compose down
	@echo "🧹 Окружение очищено."

# === JOB 4 (ЗАДЕЛ НА БУДУЩЕЕ): Запуск тестов ===
test-api:
	@echo "🧪 Запускаем API автотесты..."
	# Здесь будет команда для твоего ATF, например:
	# cd ../my-atf && ./gradlew test