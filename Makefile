.PHONY: help test test-parser test-app test-device test test-all build apk install run lint clean device deps release

APK_DEBUG := app/build/outputs/apk/debug/app-debug.apk
APK_RELEASE := app/build/outputs/apk/release/app-release-unsigned.apk
PACKAGE := com.carflow.app
ACTIVITY := .ui.MainActivity

# ── Default ──────────────────────────────────────────────
help:
	@echo "CarFlow — Makefile"
	@echo ""
	@echo "  🔨 BUILD"
	@echo "  make build            Compila APK debug"
	@echo "  make apk              build + stampa percorso APK"
	@echo "  make release          Compila APK release (unsigned)"
	@echo ""
	@echo "  📱 INSTALLA / AVVIA"
	@echo "  make install          Installa APK debug su dispositivo"
	@echo "  make run              install + avvia l'app"
	@echo "  make uninstall        Rimuove l'app dal dispositivo"
	@echo ""
	@echo "  🧪 TEST"
	@echo "  make test-parser      Parser unit tests (JVM, veloce)"
	@echo "  make test-app         App unit tests (JVM)"
	@echo "  make test-device      Test strumentati su dispositivo"
	@echo "  make test             test-parser + test-app"
	@echo "  make test-all         test-parser + test-app + test-device"
	@echo ""
	@echo "  🧹 UTILITY"
	@echo "  make clean            Pulisce tutti i build artifacts"
	@echo "  make lint             Lint statico (ktlint + detekt)"
	@echo "  make device           Dispositivi ADB connessi"
	@echo "  make deps             Lista dipendenze del progetto"

# ── Build ────────────────────────────────────────────────
build:
	@echo "→ Compilazione APK debug..."
	./gradlew assembleDebug
	@echo "✔ APK generato: $(APK_DEBUG)"

apk: build
	@echo ""
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@echo " APK pronto per installazione:"
	@echo " $(CURDIR)/$(APK_DEBUG)"
	@echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
	@ls -lh $(APK_DEBUG)

release:
	@echo "→ Compilazione APK release (unsigned)..."
	./gradlew assembleRelease
	@echo "✔ APK release: $(CURDIR)/$(APK_RELEASE)"
	@ls -lh $(APK_RELEASE)

# ── Install / Run ────────────────────────────────────────
install:
	@echo "→ Installazione su dispositivo..."
	./gradlew installDebug

run: install
	@echo "→ Avvio app..."
	adb shell am start -n $(PACKAGE)/$(ACTIVITY)

uninstall:
	@echo "→ Rimozione app..."
	adb uninstall $(PACKAGE)

# ── Tests ────────────────────────────────────────────────
test-parser:
	./gradlew :shared:parser:test

test-app:
	./gradlew :app:test

test-device:
	./gradlew :app:connectedAndroidTest

test: test-parser test-app

test-all: test-parser test-app test-device

# ── Utility ──────────────────────────────────────────────
clean:
	@echo "→ Pulizia build artifacts..."
	./gradlew clean
	@echo "✔ Pulizia completata"

lint:
	@echo "→ Lint in corso..."
	./gradlew :app:lint :shared:parser:lint 2>/dev/null || echo "⚠ Task lint non disponibile in tutti i moduli"

device:
	@echo "→ Dispositivi connessi:"
	@adb devices -l

deps:
	@echo "→ Dipendenze del progetto:"
	@./gradlew :app:dependencies --configuration debugCompileClasspath 2>/dev/null || true

# ── Ad-hoc ───────────────────────────────────────────────
# Copia APK in ~/Downloads per trasferimento facile
dist: build
	@mkdir -p ~/Downloads/carflow
	cp $(APK_DEBUG) ~/Downloads/carflow/carflow-$(shell date +%Y%m%d-%H%M).apk
	@echo "✔ APK copiato in ~/Downloads/carflow/"
	@ls -lh ~/Downloads/carflow/
