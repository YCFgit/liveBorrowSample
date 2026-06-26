.PHONY: help test integration-test package run run-memory smoke clean

help:
	@echo "Targets:"
	@echo "  make test        Run unit tests"
	@echo "  make integration-test  Run Docker-backed integration tests"
	@echo "  make package     Build application jar"
	@echo "  make run         Run with local profile"
	@echo "  make run-memory  Run with memory profile"
	@echo "  make smoke       Smoke test local endpoints"
	@echo "  make clean       Remove build output"

test:
	mvn test

integration-test:
	mvn verify -Pintegration-test

package:
	mvn clean package

run:
	mvn spring-boot:run

run-memory:
	SPRING_PROFILES_ACTIVE=memory mvn spring-boot:run

smoke:
	./scripts/smoke-test.sh

clean:
	mvn clean
