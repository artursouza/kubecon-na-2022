FROM mcr.microsoft.com/dotnet/aspnet:6.0 AS base
WORKDIR /memstore
EXPOSE 3000

FROM mcr.microsoft.com/dotnet/sdk:6.0 AS build
WORKDIR /src
COPY ["DaprMemStoreComponent.csproj", "./"]
RUN dotnet restore "DaprMemStoreComponent.csproj"
COPY . .
WORKDIR "/src/."
RUN dotnet build "DaprMemStoreComponent.csproj" -c Release -o /memstore/build

FROM build AS publish
RUN dotnet publish "DaprMemStoreComponent.csproj" -c Release -o /memstore/publish

FROM base AS final
WORKDIR /memstore
COPY --from=publish /memstore/publish .
ENTRYPOINT ["dotnet", "DaprMemStoreComponent.dll"]