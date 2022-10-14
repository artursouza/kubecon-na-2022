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

using DaprMemStoreComponent.Services;
using Mono.Unix;

public class Program
{
  public static async Task Main(string[] args)
  {
    var componentName = "memstore";
    // default directory for components
    var socketDir = "/tmp/dapr-components-sockets";
    if (!Directory.Exists(socketDir)) // creating directory if it not exists
    {
      Directory.CreateDirectory(socketDir);
    }
    var socket = $"{socketDir}/{componentName}.sock";

    if (File.Exists(socket)) // deleting socket in case of it already exists
    {
      Console.WriteLine("Removing existing socket");
      File.Delete(socket);
    }

    var builder = WebApplication.CreateBuilder(args);
    builder.Logging.ClearProviders();
    builder.Logging.AddConsole();

    // Additional configuration is required to successfully run gRPC on macOS.
    // For instructions on how to configure Kestrel and gRPC clients on macOS, visit https://go.microsoft.com/fwlink/?linkid=2099682

    // Add services to the container.
    builder.WebHost.ConfigureKestrel(options =>
                {
                  options.ListenUnixSocket(socket);
                });
    builder.Services.AddGrpc();
    // gRPC refletion is required for service discovery, do not remove it.
    builder.Services.AddGrpcReflection();

    var app = builder.Build();

    // Configure the HTTP request pipeline.
    app.MapGrpcService<MemStoreService>(); // register our memstore
                                           // gRPC refletion is required for service discovery, do not remove it.
    app.MapGrpcReflectionService();
    app.MapGet("/", () => "Communication with gRPC endpoints must be made through a gRPC client. To learn how to create a client, visit: https://go.microsoft.com/fwlink/?linkid=2086909");

    await app.StartAsync().ConfigureAwait(false);

    new UnixFileInfo(socket).FileAccessPermissions =
        FileAccessPermissions.UserRead | FileAccessPermissions.UserWrite
        | FileAccessPermissions.GroupRead | FileAccessPermissions.GroupWrite
        | FileAccessPermissions.OtherRead | FileAccessPermissions.OtherWrite;

    await app.WaitForShutdownAsync().ConfigureAwait(false);
  }
}
