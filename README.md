# Dapr's Pluggable Components Example for KubeCon NA 2022

## Pre-requisites

Common:
* Make
* Dapr CLI: https://github.com/dapr/cli or `brew install dapr/tap/dapr-cli`
* grpc_cli: `brew install grpc`

For memstore example:
* DotNet Core 6.X: https://dotnet.microsoft.com/en-us/download/dotnet/6.0
* Node.js: https://nodejs.org/en/

For Discord binding example:
* JDK 11+: https://openjdk.org/install/
* Maven: https://maven.apache.org

For Kubernetes examples:
* Minikube for the examples on Kubernetes: https://minikube.sigs.k8s.io/docs/start/

For sentiment analysis component in Azure:
* Endpoint and Key for Azure's Cognitive Service for Language: 

Clone this repo:
```sh
git clone git@github.com:artursouza/kubecon-na-2022.git
cd kubecon-na-2022
```

## Standalone mode

### State store example

Run the in-memory store pluggable component:

```sh
cd pluggable-components/memstore
dotnet run
```

In a new terminal, let's create a component using the new `memstore` type.
```bash
mv ~/.dapr/components/statestore.yaml ~/.dapr/components/statestore.yam_
cat > ~/.dapr/components/statestore.yaml
apiVersion: dapr.io/v1alpha1
kind: Component
metadata:
  name: statestore
spec:
  type: state.memstore
  version: v1
  metadata:
```

Now, `statestore` is using `state.memstore` instead of `state.redis`. We will use a existing Dapr tutorial to show it in use.

```sh
cd ../../../
git clone -b v1.8.0 git@github.com:dapr/quickstarts.git
cd quickstarts/tutorials/hello-world/node
npm install
```

Run the node app:
```sh
dapr run --app-id nodeapp --app-port 3000 --dapr-http-port 3500 node app.js
```

In a new terminal, invoke the service:
```sh
dapr invoke --app-id nodeapp --method neworder --data-file sample.json
```

Now, go to the first terminal, running the pluggable components and see the log output:
```log
info: DaprMemStoreComponent.Services.MemStoreService[0]
      Init request for memstore
info: DaprMemStoreComponent.Services.MemStoreService[0]
      BulkSet request for 1 keys
info: DaprMemStoreComponent.Services.MemStoreService[0]
      BulkSet request for key nodeapp||order
```

### Input binding example

Run the Discord input binding component:

First, get a token for a bot in Discord. Then, export it as an environment variable:

```sh
export DISCORD_TOKEN=<TOKEN_GOES_HERE>
```

Run the Discord input binding component:
```sh
cd pluggable-components/discord-binding
mvn clean install
java -jar ./target/dapr-discord-binding-0.0.1-jar-with-dependencies.jar
```

In a new terminal, run an app that receives events via binding (at the root folder of this repository):
```sh
dapr run --app-id binding-app --app-port 3000 --components-path=./components/ -- npx http-echo-server 3000
```

Now, go to a channel where your bot is in and send any message. You should see the same message echoed in the terminal above.

## Kubernetes mode

Setup Minikube:
```sh
make minikube-start
```

Init Dapr:
```sh
dapr init -k
```

Monitor state of Dapr control plane:
```sh
dapr status -k
```

### State store example

Deploy:
```sh
kubctl apply -f kubernetes/memstore-example/
```

### Discord binding example

Install Redis:
```sh
helm install redis bitnami/redis --set image.tag=6.2 
```

Create Kubernetes secret for Discord's token, based of the environment variable created previously:
```sh
kubectl create secret generic discord --from-literal=token=$DISCORD_TOKEN
```

Create Kubernetes secrets for the endpoint and key for Azure Cognitive Services:
```sh
kubectl create secret generic azure-cognitiveservices-endpoint --from-literal=value=https://<YOUR_ENDPOINT_ALIAS>.cognitiveservices.azure.com/
kubectl create secret generic azure-cognitiveservices-subscriptionkey --from-literal=value=<YOUR_KEY_GOES_HERE>
```

Deploy:
```sh
kubctl apply -f kubernetes/discord-example/
```

After all PODs are running, forward the web app port:
```sh
kubectl port-forward services/viewer 8080
```

Then, open http://localhost:8080/

Now, you can open the Discord channel and post messages. Those will show up on the web app with the sentiment analysis icon next to it.