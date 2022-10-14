ifeq ($(OS),Windows_NT) 
    detected_OS := windows
else
    detected_OS := $(shell sh -c 'uname 2>/dev/null || echo Unknown' |  tr '[:upper:]' '[:lower:]')
endif

docker-login:
	docker login
	$(eval docker_alias := $(shell docker-credential-desktop list | jq -r '. | to_entries[] | select(.key | contains("docker.io")) | last(.value)'))

docker-build: docker-login
	docker build --pull --rm -f "pluggable-components/memstore/Dockerfile" -t $(docker_alias)/memstore:latest "pluggable-components/memstore"
	docker build --pull --rm -f "pluggable-components/discord-binding/Dockerfile" -t $(docker_alias)/discord-binding:latest "pluggable-components/discord-binding"

docker-push: docker-login
	docker push $(docker_alias)/memstore:latest
	docker push $(docker_alias)/discord-binding:latest

setup-minikube-darwin:
	minikube start --memory=4g --cpus=4 --driver=hyperkit
	minikube addons enable metrics-server

setup-minikube-windows:
	minikube start --memory=4g --cpus=4
	minikube addons enable metrics-server

setup-minikube-linux:
	minikube start --memory=4g --cpus=4
	minikube addons enable metrics-server

setup-minikube: setup-minikube-$(detected_OS)

describe-minikube-env:
	@echo "\
	export DAPR_REGISTRY=docker.io/`docker-credential-desktop list | jq -r '\
	. | to_entries[] | select(.key | contains("docker.io")) | last(.value)'`\n\
	export DAPR_TAG=dev\n\
	export DAPR_NAMESPACE=dapr-tests\n\
	export DAPR_TEST_ENV=minikube\n\
	export DAPR_TEST_REGISTRY=\n\
	export MINIKUBE_NODE_IP="

# Setup minikube
delete-minikube:
	minikube delete