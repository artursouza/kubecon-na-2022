// ------------------------------------------------------------------------
// Copyright 2022 The Dapr Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//     http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ------------------------------------------------------------------------

using System.Collections.Concurrent;
using Dapr.Client.Autogen.Grpc.v1;
using Dapr.Proto.Components.V1;
using Google.Protobuf;
using Grpc.Core;

namespace DaprMemStoreComponent.Services;

public class MemStoreService : StateStore.StateStoreBase
{
    private readonly ILogger<MemStoreService> _logger;
    private readonly static IDictionary<string, string?> Storage = new ConcurrentDictionary<string, string?>();
    public MemStoreService(ILogger<MemStoreService> logger)
    {
        _logger = logger;
    }

    public override Task<FeaturesResponse> Features(FeaturesRequest request, ServerCallContext ctx)
    {
        return Task.FromResult(new FeaturesResponse { });
    }

    public override Task<GetResponse> Get(GetRequest request, ServerCallContext ctx)
    {
        this._logger.LogInformation("Get request for key {key}", request.Key);
        if (Storage.TryGetValue(request.Key, out var data))
        {
            return Task.FromResult(new GetResponse
            {
                Data = ByteString.CopyFromUtf8(data),
            });
        }
        return Task.FromResult(new GetResponse { }); // in case of not found you should not return any error.
    }

    public override Task<SetResponse> Set(SetRequest request, ServerCallContext ctx)
    {
        this._logger.LogInformation("Set request for key {key}", request.Key);
        Storage[request.Key] = request.Value?.ToStringUtf8();
        return Task.FromResult(new SetResponse());
    }

    public override Task<BulkSetResponse> BulkSet(BulkSetRequest request, ServerCallContext ctx)
    {
        this._logger.LogInformation("BulkSet request for {count} keys", request.Items.Count);
        foreach (var item in request.Items)
        {
            this._logger.LogInformation("BulkSet request for key {key}", item.Key);
            Storage[item.Key] = item.Value?.ToStringUtf8();
        }
        return Task.FromResult(new BulkSetResponse { });
    }

    public override Task<InitResponse> Init(InitRequest request, ServerCallContext ctx)
    {
        _logger.LogInformation("Init request for memstore");
        return Task.FromResult(new InitResponse { });
    }

    public override Task<PingResponse> Ping(PingRequest request, ServerCallContext ctx)
    {
        return Task.FromResult(new PingResponse());
    }
}
