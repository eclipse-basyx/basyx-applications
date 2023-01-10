FROM node

RUN git clone https://github.com/eclipse-basyx/basyx-applications.git
RUN npm install -g @vue/cli
RUN npm install -g yarn
RUN yarn install --prefix /basyx-applications/aas-gui/Frontend/aas-web-gui

EXPOSE 3000/tcp

CMD yarn dev --prefix /basyx-applications/aas-gui/Frontend/aas-web-gui
