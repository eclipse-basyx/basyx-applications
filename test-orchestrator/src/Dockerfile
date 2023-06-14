FROM mcr.microsoft.com/dotnet/sdk:6.0

WORKDIR /App

COPY ./TestDemonstrator /App/TestDemonstrator
COPY ./TestDemonstratorAAS /App/TestDemonstratorAAS

WORKDIR /App/TestDemonstratorAAS

RUN dotnet build .

WORKDIR /App

RUN dotnet dev-certs https --trust

ENTRYPOINT ["dotnet", "/App/TestDemonstratorAAS/bin/Debug/net6.0/TestDemonstratorAAS.dll", "80", "5000"]
