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

minikube-start-darwin:
	minikube start --memory=4g --cpus=4 --driver=hyperkit
	minikube addons enable metrics-server

minikube-start-windows:
	minikube start --memory=4g --cpus=4
	minikube addons enable metrics-server

minikube-start-linux:
	minikube start --memory=4g --cpus=4
	minikube addons enable metrics-server

minikube-start: minikube-start-$(detected_OS)

minikube-delete:
	minikube delete