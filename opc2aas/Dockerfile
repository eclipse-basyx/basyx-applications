#See https://aka.ms/customizecontainer to learn how to customize your debug container and how Visual Studio uses this Dockerfile to build your images for faster debugging.

FROM mcr.microsoft.com/dotnet/aspnet:6.0 AS base
WORKDIR /app
EXPOSE 4700

FROM mcr.microsoft.com/dotnet/sdk:6.0 AS build
WORKDIR /src
COPY ["OPC2AAS/OPC2AAS.csproj", "OPC2AAS/"]
RUN dotnet restore "OPC2AAS/OPC2AAS.csproj"
COPY . .
WORKDIR "/src/OPC2AAS"
RUN dotnet build "OPC2AAS.csproj" -c Release -o /app/build

FROM build AS publish
RUN dotnet publish "OPC2AAS.csproj" -c Release -o /app/publish

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "OPC2AAS.dll"]