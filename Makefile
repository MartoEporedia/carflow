.PHONY: help test test-parser test-app test-all install run build clean

# Default target
help:
	@echo "CarFlow — comandi disponibili:"
	@echo ""
	@echo "  make test-parser     Parser unit tests (JVM, veloce)"
	@echo "  make test-app        App unit tests (JVM)"
	@echo "  make test-device     Test strumentati su dispositivo/emulatore connesso"
	@echo "  make test            test-parser + test-app"
	@echo "  make test-all        test-parser + test-app + test-device"
	@echo ""
	@echo "  make build           Build APK debug"
	@echo "  make install         Installa APK debug sul dispositivo connesso"
	@echo "  make run             Installa e lancia l'app"
	@echo "  make clean           Pulisce tutti i build artifacts"
	@echo ""
	@echo "  make device          Mostra dispositivi ADB connessi"

# Tests
test-parser:
	./gradlew :shared:parser:test

test-app:
	./gradlew :app:test

test-device:
	./gradlew :app:connectedAndroidTest

test: test-parser test-app

test-all: test-parser test-app test-device

# Build & Install
build:
	./gradlew assembleDebug

install:
	./gradlew installDebug

run: install
	adb shell am start -n com.carflow.app/.ui.MainActivity

# Utility
clean:
	./gradlew clean

device:
	adb devices
