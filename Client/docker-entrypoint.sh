#!/bin/sh
set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "${GREEN}uniAI Client Entrypoint${NC}"

# Check if node_modules needs installation
if [ ! -d "node_modules" ] || [ ! -f "node_modules/.install-complete" ]; then
  echo "${YELLOW}First run or volume wiped - installing dependencies...${NC}"
  npm install
  touch node_modules/.install-complete
  echo "${GREEN}Dependencies installed${NC}"
elif [ "package.json" -nt "node_modules/.install-complete" ]; then
  echo "${YELLOW}package.json updated - reinstalling dependencies...${NC}"
  npm install
  touch node_modules/.install-complete
  echo "${GREEN}Dependencies reinstalled${NC}"
else
  echo "${GREEN}node_modules up to date${NC}"
fi

echo "${GREEN}Starting dev server...${NC}"
exec npm run dev -- --host 0.0.0.0
