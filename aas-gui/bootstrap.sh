PRJ_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
FRESH_INSTALL='0'
Yellow='\033[1;33m'
ColorOff='\033[0m'

echo -e "${Yellow}Do a clean install?  (y/n) Default:n ${ColorOff}"
read wirklich_loeschen
if [ "$wirklich_loeschen" = "y" ]; then
    FRESH_INSTALL='1'
fi

echo -e "${Yellow}Build for Production instead of starting dev-Server?  (y/n) Default:n ${ColorOff}"
read just_build_no_devserver
cd $PRJ_ROOT/Frontend/aas-web-gui
if [ "$FRESH_INSTALL" = "1" ]; then
    rm -rf node_modules
    yarn install
fi

# Start Dev Server
if [ "$just_build_no_devserver" = "y" ]; then
    yarn run build
else
    yarn dev
fi

cd $PRJ_ROOT