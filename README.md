# Dapr's Pluggable Components Example for KubeCon NA 2022

## Pre-requisites

* Install DotNet Core 6.X: https://dotnet.microsoft.com/en-us/download/dotnet/6.0
* Install grpc_cli: `brew install grpc`
* Install Node.js: https://nodejs.org/en/

## Standalone mode

Clone this repo:
```sh
git clone git@github.com:artursouza/kubecon-na-2022.git
cd kubecon-na-2022
```

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
