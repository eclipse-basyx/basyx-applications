PRJ_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
FRESH_INSTALL='0'
Yellow='\033[1;33m'
ColorOff='\033[0m'

echo -e "${Yellow}Do a clean install?  (y/n) Default:n ${ColorOff}"
read clean_install
if [ "$clean_install" = "y" ]; then
    FRESH_INSTALL='1'
fi

echo -e "${Yellow}Build for Production instead of starting dev-Server?  (y/n) Default:n ${ColorOff}"
read build_prod
cd $PRJ_ROOT/basyx-starter-kit
if [ "$FRESH_INSTALL" = "1" ]; then
    rm -rf node_modules
    yarn install
fi

# Start Dev Server
if [ "$build_prod" = "y" ]; then
    yarn run build
else
    yarn dev --host
fi

cd $PRJ_ROOT